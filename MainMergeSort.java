/*
* For this script to run correctly one must have a folder in the same directory filled with the input files.
* The name of this folder is passed in as the 3 argument.
* Usage: Main B M InputsFolder CoordinateToSortBy
* Last test : 21/05/2017
* Arguments : 40000 400000 Inputs x
* This means, B = 4000 bytes | M = 40000 | inputs folder = Input
*
* */

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class Main {


    static int B;
    static int M;
    static int m;
    static Random nameGenerator = new Random();
    static int numberOfDiskIO = 0;

    public static void main(String[] args) throws IOException{

        // We receive as an argument the directory where we previusly stored all the generated inputs.
        Path inputsFolderPath = Paths.get(args[2]);
        // Path to output file where we will write the following:
        // Input-Name.csv , Number of disk IO's.
        Path resultPath = Paths.get("results.csv");

        B = Integer.parseInt(args[0]);
        M = Integer.parseInt(args[1]);
        m = (int)Math.ceil(M/B);
        char coordToSort = args[3].charAt(0);


        // Here we store the paths to all input files.
        List<Path> inputPaths;
        try (Stream<Path> paths = Files.walk(inputsFolderPath)) {
            inputPaths = paths.filter(Files::isRegularFile).collect(Collectors.toList());
        }

        // We iterate through the input files, calling mergeSort on each one of them and counting
        // the number of disk IO's.
        for (Path aPath: inputPaths) {
            numberOfDiskIO = 0;
            List<Path> sortedFilePath = externalMergeSort(aPath, coordToSort);
            // If after de MergeSort procedure, specifically after the merge part we have more than one
            // sorted run, it means something went wrong with the merge.
            if(sortedFilePath.size() != 1){
                System.out.println("ERROR: Merge ended with more than one resulting sorted run.");
                break;
            }
            // Construct the line to be written to the file.
            String line = String.format(aPath.getFileName().toString()+","+"%d"+System.lineSeparator(), numberOfDiskIO);
            // Write to it.
            Files.write(resultPath, line.getBytes(), CREATE, APPEND);
            System.out.println("Finished sorting file: "+aPath.getFileName().toString());
        }


    }
    /*
    *  External MergeSort procedure. Given a path to the input file to be sorted and the coordinate to it by it
    *  proceeds to run the External MergeSort procedure. This complies of the following:
    *  1. Phase one: Generate n/m sorted runs and write them to secondary memory.
    *  2. Phase two: At each step merge m sorted runs into one.
    *     Repeat this last step until there is only one sorted run.
    *
    *  Path inputPath : Path to the input file to be sorted.
    *  char coordToSort : coordinate to sort the file by. (can be 'x' or 'y')
    * */
    public static List<Path> externalMergeSort(Path inputPath, char coordToSort) throws IOException{
        FileChannel anInput = FileChannel.open(inputPath);
        List<Path> phaseOnePaths = phaseOne(anInput, coordToSort);
        phaseOnePaths = phaseTwo(phaseOnePaths, coordToSort);
        return phaseOnePaths;
    }

    /*
    * Phase one of the external mergeSort algorithm. It takes an input file and produces n/m sorted runs which are
    * stored in secondary memory for the second phase of the algorithm (Merge). These runs are sorted by the coordinate
    * indicated by the argument coordToSort.
    *
    * FileChannel input : input file.
    * char coordToSort : coordinate to sort the file by. (can be 'x' or 'y')
    *
    * Returns : List of paths to the files where each sorted run was written to.
    * */
    public static List<Path> phaseOne(FileChannel input, char coordToSort) throws IOException{
        List<Path> sortedRuns = new ArrayList<>();
        List<String> linesToWrite;
        List<int[]> segmentsRun;

        while(Math.abs(input.position() - input.size()) > 1){

            segmentsRun = getSegmentsRun(input);
            sortSegmentsRun(segmentsRun, coordToSort);

            linesToWrite = segmentsRunToString(segmentsRun);

            Path path = generateSegmentsRunName();
            sortedRuns.add(path);

            writeSortedRunToSM(linesToWrite, path);
        }

        return sortedRuns;
    }

    /*
    * Phase two of the external mergeSort algorithm. It takes a list of paths to sorted runs and performs phase two steps
    * until there is only one path in the lists of paths. The amount of paths decreases because at each step of the phase two
    * we merge O(m) sorted runs into one large sorted run.
    *
    * List<Path> phaseOneOutput : Phase one output, ie, a list of paths to the sorted run generated by phaseOne method.
    * char coordToSort : coordinate to sort the file by. (can be 'x' or 'y')
    * */
    public static List<Path> phaseTwo(List<Path> phaseOneOutput, char coordToSort) throws IOException{
        List<Path> output = phaseOneOutput;
        while(output.size() != 1){
            output = phaseTwoStep(output, coordToSort);
        }

        return output;

    }

    /*
    * A phase two step consists basically in merging O(m) sorted runs into a single one in secondary memory.
    * This is done by taking the first m sorted runs in the sortedRuns list and continuously selecting the minimum
    * between each one of them and storing it in an output buffer. Once this buffer reaches a size of B we flush it
    * to secondary memory.
    *
    * List<Path> sortedRuns : List containing paths to each sorted run generated previously by another phase two step
    * or the phase one. The idea here is to take m of these and after the merge is completed we delete them.
    * char coordToSort : coordinate to sort the file by. (can be 'x' or 'y')
    * */
    public static List<Path> phaseTwoStep(List<Path> sortedRuns, char coordToSort) throws IOException{
        List<ArrayDeque> inputBuffers = new ArrayList<>();
        List<FileChannel> inputFiles = new ArrayList<>();
        /* Path to file were we will store our generated merged run */
        Path outputFilePath = generateSegmentsRunName();

        /* This for loop basically initializes the input buffers and files. */
        for (Path pathToInput: sortedRuns) {
            /* We need to break from the loop when we have m - 1 input buffers (O(m)) */
            if(inputBuffers.size() == m - 1){
                break;
            }
            /* FileChannel input to allow us to read from the file from disk whenever its corresponding inputbuffer is empty. */
            FileChannel input = FileChannel.open(pathToInput);
            /* The next two lines are the actual stuffing of the input buffer. */
            List<int[]> segments = segmentsRunToInt(readBlockFromFile(input,B));
            ArrayDeque segmentsQueue = new ArrayDeque(segments);
            /* Now we add the input buffer to the input buffers list and the Filechannel to the files list. */
            inputBuffers.add(segmentsQueue);
            inputFiles.add(input);
        }

        /* We need to keep track of the size of the output buffer so we know when to perform a write to disk. */
        int outputBufferSize = 0;
        List<String> outputBuffer = new ArrayList<>();

        /* Iterate until we have consumed all the input buffers. */
        while(inputBuffers.size() > 0){

            /* Before anything, we need to check if the output buffer is full (= size ~ B).
            *  If it is we need to perform a write to disk operation.
            *  */

            if(outputBufferSize < B){
                /* We get the index associated with the input buffer which contains the minimum segment. */
                int minLocation = getInputBuffersMinLocation(inputBuffers, new SegmentComparator(coordToSort));

                /* Typical parsing. */
                int [] segment = (int[])inputBuffers.get(minLocation).removeFirst();
                String minSegment = String.format("%d,%d,%d,%d", segment[0], segment[1], segment[2], segment[3]);

                outputBuffer.add(minSegment);
                outputBufferSize += minSegment.getBytes().length;


                /* This if checks whether the input buffer from which we took the minimum is now empty or not. */
                if(inputBuffers.get(minLocation).size() == 0){
                    /* If there is anything left in the corresponding input file then we load a chunk of data to the input buffer. */
                    if(Math.abs(inputFiles.get(minLocation).position() - inputFiles.get(minLocation).size()) > 1){
                        inputBuffers.get(minLocation).addAll(segmentsRunToInt(readBlockFromFile(inputFiles.get(minLocation), B)));
                    }
                    /* Else, it means that the input buffer is empty and the corresponding file has nothing new to read from. Thus
                    *  we have reached the end of that sorted run and we can delete it. */
                    else{
                        inputBuffers.remove(minLocation);
                        Files.delete(sortedRuns.get(minLocation));
                        inputFiles.remove(minLocation);
                        sortedRuns.remove(minLocation);
                    }
                }

            } else{
                writeSortedRunToSM(outputBuffer,outputFilePath);
                outputBuffer.clear();
                outputBufferSize = 0;
            }

        }

        /* Before returning we write the remaining segments in the output buffer to the output file. */
        writeSortedRunToSM(outputBuffer,outputFilePath);


        /* We add the newly created sorted run -its path- (product of merging O(m) sorted runs) to the lists of sorted runs. */
        sortedRuns.add(outputFilePath);

        return sortedRuns;
    }

    /*
    * Takes a list of queues where each one of them represents an input buffer of a sorted run. This method
    * peeks the first elements of each buffer and returns the index of the list the contains the minimum.
    *
    * List<ArrayDeque> inputBuffers : List of queues representing input buffers.
    * Comparator segmentComparator : segmentComparator class instance used to compare each segment and decide the minimum.
    * */
    public static int getInputBuffersMinLocation(List<ArrayDeque> inputBuffers, Comparator segmentComparator){
        int [] min = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
        int index = 0;
        for (Deque segmentsDeque: inputBuffers) {
            int [] segment = (int[])segmentsDeque.getFirst();
            if(segmentComparator.compare(min, segment) == 1){
                min = segment;
                index = inputBuffers.indexOf(segmentsDeque);
            }
        }

        return index;
    }

    /*
    * Takes a List of segments, where each segment is an array of integers of the form [x_1, y_1, x_2, y_2]
    * and returns a List of Strings where each component is a string of the form "x_1,y_1,x_2,y_2".
    *
    * List<int[]> segmentsRun : List of segments to "convert".
    * Returns : List of segments in String representation explained above.
    * */
    public static List<String> segmentsRunToString(List<int[]> segmentsRun){
        List <String> linesToWrite = new ArrayList<>();
        for (int[] segment: segmentsRun) {
            String line = String.format("%d,%d,%d,%d", segment[0], segment[1], segment[2], segment[3]);
            linesToWrite.add(line);
        }
        return linesToWrite;
    }

    /*
    * Takes a List of segments, where each segment is a string of the form "x_1,y_1,x_2,y_2"
    * and returns a List of int[] where each component of the list is an array of ints of the form [x_1, y_1, x_2, y_2].
    *
    * List<String> segmentsRun : List of segments to "convert".
    * Returns : List of segments in int[] representation explained above.
    * */
    public static List<int[]> segmentsRunToInt(List<String> segmentsRun){
        List <int[]> segments = new ArrayList<>();
        for (String line: segmentsRun) {
            int [] segment = Stream.of(line.split(",")).mapToInt(Integer::parseInt).toArray();
            segments.add(segment);
        }
        return segments;
    }

    /*
    * Generates a name for storing a sorted run in secondary memory.
    * To generate random names it used nameGenerator, which is an instance of Random.
    * Returns : Path containing name where a sorted run will be stored in secondary memory.
    * */
    public static Path generateSegmentsRunName(){
        return Paths.get(String.format("%d.csv", nameGenerator.nextInt()));
    }

    /*
    * Given an input File of size N, we wish to take chunks of size B from it and start the sorting process.
    * This method returns a buffer filled with the segments inside the chunk of size B of the input file.
    * FileChannel input : Input file. Using a FileChannel allows us read B bytes from the file.
    * int B : Size of chunks we read from the file (bytes).
    *
    * Returns : List<String> where each component is a String representing a segment. Each String looks like "x_1,y_1,x_2,y_2"
    * */
    public static List<String> readBlockFromFile(FileChannel input, int B) throws IOException {

        /*
        * A big problem in reading text files by a certain amount of bytes is dealing with line cuts.
        * It could happen that we wish to read X bytes from the file and that means reading a non integer
        * amount of lines. In this case we need to rewind the file pointer to the last \n read. So we can start
        * from there the next time.
        * */

        numberOfDiskIO++;

        boolean EOBuffer = false;
        // Here we create our byte buffer with a fixed size B.
        ByteBuffer byteBuffer = ByteBuffer.allocate(B);
        // We read from the file. Starting from the last position we were in.
        input.read(byteBuffer);
        // To read from the buffer we need to move the pointer position to the beggining.
        byteBuffer.flip();

        // Lines is the list in which we will store the segments read from the chunk of size B.
        List<String> lines = new ArrayList<>();
        String lineString = "";
        char c;

        // We read from the buffer while it still has bytes to read.
        while(byteBuffer.hasRemaining()){

            // We get the first char from the buffer and check if it is a end of line.
            c = (char)byteBuffer.get();
            if(c == System.lineSeparator().charAt(0) && !byteBuffer.hasRemaining()){
                EOBuffer = true;
            }
            // This case happens when the last chunk read left us at the end of a line.
            if(c == System.lineSeparator().charAt(0) && byteBuffer.hasRemaining()){
                c = (char)byteBuffer.get();
            }
            while(c != System.lineSeparator().charAt(0)){
                lineString += c;
                /*
                * While we don't see an end of line we keep on reading the line. Mind the fact that if we are in a line
                * cutted by the read method we would never see an end of line.
                * For this, if the bytebuffer does not have any more bytes to get and we haven't reached and end of line
                * it means we need to stop reading.
                * */
                if(!byteBuffer.hasRemaining()){
                    EOBuffer = true;
                    break;
                }
                c = (char)byteBuffer.get();
            }
            if(EOBuffer){
                break;
            }
            /*
             * This one-liner simply gets the lineString of the form "x1,y1,x2,y2", strips it by ',' and converts each
             * component to an integer, then an array of integers.
            */
            //System.out.println(lineString);
            //segments = Stream.of(lineString.split(",")).mapToInt(Integer::parseInt).toArray();
            lines.add(lineString);
            lineString = "";
        }


        /*
        * Now if we ended up in the middle of a line we need to calculate how much we need to rewing the input file
        * position, so we can read the whole line the next time.
        * */
        int fileRewind = 0;
        int bufferPosition = byteBuffer.position()-1;
        char lastCharRead = (char)byteBuffer.get(bufferPosition);
        if(lastCharRead != System.lineSeparator().charAt(0)){
            bufferPosition--;
            lastCharRead = (char)byteBuffer.get(bufferPosition);
            while(lastCharRead != System.lineSeparator().charAt(0)){
                fileRewind++;
                bufferPosition--;
                lastCharRead = (char)byteBuffer.get(bufferPosition);
            }
        }
        // Finally, we update de input file's position to the end of the last line we were able to read completely.
        input.position(input.position() - fileRewind - 1);


        return lines;
    }

    /*
    *  Given segmentsRun with each component inside of it of the form [x_1, y_1, x_2, y_2]
    *  it uses the SegmentComparator to sort the list given a coordinate (coordToSort)
    *
    *  int [] segmentsRun : a run of unordered segments.
    *  char coordtoSort : coordinate to sort by segments. (Values can be 'x' or 'y')
    *
    *  Its void since the sort is done in-place.
    * */
    public static void sortSegmentsRun(List<int[]> segmentsRun, char coordToSort){
        Collections.sort(segmentsRun, new SegmentComparator(coordToSort));
    }

    /*
    * Reads m times blocks of size B from the input. Each block is a set of lines in the input.
    * It puts them all together in the segmentRun List.
    *
    * FileChannel input: Input file.
    *
    * Returns: List<int[]> of segments. Each component of the list a segment represented in an array of ints
    * of the form [x_1, y_1, x_2, y_2]
    * */
    public static List<int[]> getSegmentsRun(FileChannel input) throws  IOException{
        List<String> blockReadFromFile;
        List<int[]> segmentsRun = new ArrayList<>();

        for (int i = 0; i < m; i++) {
            blockReadFromFile = readBlockFromFile(input, B);
            for (String line: blockReadFromFile) {
                int [] segment = Stream.of(line.split(",")).mapToInt(Integer::parseInt).toArray();
                segmentsRun.add(segment);
            }
        }

        return segmentsRun;
    }

    /*
    * Gets a sorted run of segments and writes it to secondary memory in the specified path.
    * List<String> sortedRun : List of sorted segments (single run).
    * Path path : Path to file in which the sorted run is going to be written.
    * */
    public static void writeSortedRunToSM(List<String> sortedRun, Path path) throws IOException{
        numberOfDiskIO++;
        Files.write(path, sortedRun,CREATE,APPEND);
    }

    /*
    * Comparation function for sorting the random sample in getPivots.
    * Constructor: SegmentComparator
    * */
    static private class SegmentComparator implements Comparator<int[]> {

        char cts;
        /*
        * Constructor for the class Comparator.
        * coordToSort : coordinate by which we are sorting the file (x or y).
        * */
        private SegmentComparator(char coordToSort){
            cts = coordToSort;
        }

        /*
        * segmentOne : First segment involved in comparison.
        * segmentTwo : Second segment involved in comparison.
        * */
        public int compare(int[] segmentOne, int[] segmentTwo){
            // Notice that the homework pdf states that there a some difference in sorting by x or y.
            if (cts == 'x'){
                if (Integer.compare(segmentOne[0], segmentTwo[0]) == 0){
                    return Integer.compare(segmentOne[2], segmentTwo[2]);
                }else{
                    return Integer.compare(segmentOne[0], segmentTwo[0]);
                }

            }

            // Here we need to check if a segment is vertical or horizontal to decide ties between segments.
            if (cts == 'y'){

                // Case 1: The two segments both have the same y coordinates, ie:
                //  S1_y1 = S2_y1 and S1_y2 = S2_y2
                if (Integer.compare(segmentOne[1], segmentTwo[1]) == 0 && Integer.compare(segmentOne[3], segmentTwo[3]) == 0){

                    // Note that a segment is vertical if both of its x coordinates are the same.
                    // If both segments are vertical its a tie.
                    if(Integer.compare(segmentOne[0], segmentOne[2]) == 0 && Integer.compare(segmentTwo[0], segmentTwo[2]) == 0){
                        return 0;
                    }
                    // If segmentOne is vertical and segmentTwo horizontal, then segmentOne > segmentTwo. We return 1.
                    else if(Integer.compare(segmentOne[0], segmentOne[2]) == 0 && Integer.compare(segmentTwo[0], segmentTwo[2]) != 0){
                        return 1;
                    }
                    // If segmentOne if horizontal and segmentTwo is vertical, then segmentOne < segmentTwo. We return -1.
                    else if(Integer.compare(segmentOne[0], segmentOne[2]) != 0 && Integer.compare(segmentTwo[0], segmentTwo[2]) == 0){
                        return -1;
                    }
                    // If both segments are horizontal and have the same y coordinates, then its a tie.
                    else{
                        return 0;
                    }

                }
                // Case 2: The two segments differ in at least one y coordinate.
                else{
                    if (Integer.compare(segmentOne[1], segmentTwo[1]) == 0){
                        return Integer.compare(segmentOne[3], segmentTwo[3]);
                    }else{
                        return Integer.compare(segmentOne[1], segmentTwo[1]);
                    }

                }
            }

            return 0;
        }
    }

}
