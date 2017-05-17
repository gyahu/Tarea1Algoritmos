/*
*
* Last test : 16/05/2017
* Arguments : 300 3000 input-2-9-normal-0.25.csv
* This means, B = 300 bytes | M = 3000 | input = input-2-9-normal-0.25.csv
* RENAME FILE TO Main.java before compiling and running.
* */

package dcc.daa;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Main {


    static int B;
    static int M;
    static int m;
    static Random nameGenerator = new Random();

    public static void main(String[] args) throws IOException{
	    // write your code here
        Path inputPath;
        FileChannel input;
        Long N;
        int S;

        inputPath = Paths.get(args[2]);
        input = FileChannel.open(inputPath);

        N = input.size();
        B = Integer.parseInt(args[0]);
        M = Integer.parseInt(args[1]);
        m = (int)Math.ceil(M/B);

        List<Path> paths = phaseOne(input, "x");

        // The following just prints in the console the names of the files in which the sorted runs where stored:
        for (Path path: paths) {
            System.out.println(path.toString());
        }


    }

    /*
    * Phase one of the external mergeSort algorithm. It takes an input file and produces n/m sorted runs which are
    * stored in secondary memory for the second phase of the algorithm (Merge). These runs are sorted by the coordinate
    * indicated by the argument coordToSort.
    *
    * FileChannel input : input file.
    * String coordToSort : coordinate to sort the file by. (can be "x" or "y")
    *
    * Returns : List of paths to the files where each sorted run was written to.
    * */
    public static List<Path> phaseOne(FileChannel input, String coordToSort) throws IOException{
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
            if(c == '\n' && !byteBuffer.hasRemaining()){
                EOBuffer = true;
            }
            // This case happens when the last chunk read left us at the end of a line.
            if(c == '\n' && byteBuffer.hasRemaining()){
                c = (char)byteBuffer.get();
            }
            while(c != '\n'){
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
        if(lastCharRead != '\n'){
            bufferPosition--;
            lastCharRead = (char)byteBuffer.get(bufferPosition);
            while(lastCharRead != '\n'){
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
    *  String coordtoSort : coordinate to sort by segments. (Values can be "x" or "y")
    *
    *  Its void since the sort is done in-place.
    * */
    public static void sortSegmentsRun(List<int[]> segmentsRun, String coordToSort){
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
        //Path path = Paths.get(String.format("%d.csv", nameGenerator.nextInt()));
        Files.write(path, sortedRun);
    }

    /*
    * Comparation function for sorting the random sample in getPivots.
    * Constructor: SegmentComparator
    * */
    static private class SegmentComparator implements Comparator<int[]> {

        String cts;
        /*
        * Constructor for the class Comparator.
        * coordToSort : coordinate by which we are sorting the file (x or y).
        * */
        private SegmentComparator(String coordToSort){
            cts = coordToSort;
        }

        /*
        * segmentOne : First segment involved in comparison.
        * segmentTwo : Second segment involved in comparison.
        * */
        public int compare(int[] segmentOne, int[] segmentTwo){
            // Notice that the homework pdf states that there a some difference in sorting by x or y.
            if (cts.equals("x")){
                if (Integer.compare(segmentOne[0], segmentTwo[0]) == 0){
                    return Integer.compare(segmentOne[2], segmentTwo[2]);
                }else{
                    return Integer.compare(segmentOne[0], segmentTwo[0]);
                }

            }

            // Here we need to check if a segment is vertical or horizontal to decide ties between segments.
            if (cts.equals("y")){

                // Case 1: The two segments both have the same y coordinates, ie:
                //  S1_y1 = S2_y1 and S1_y2 = S2_y2
                if (Integer.compare(segmentOne[1], segmentTwo[1]) == 0 && Integer.compare(segmentOne[3], segmentTwo[3]) == 0){

                    // Note that a segment is vertical if both of its y coordinates are the same.
                    // So if Segment one is vertical, we claim it larger by comparison.
                    if(Integer.compare(segmentOne[0], segmentOne[2]) == 0){
                        return 1;
                    }else{
                        return -1;
                    }
                    // Case 2: The two segments differ in at least one y coordinate.
                }else{
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
