package dcc.daa;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.stream.Stream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Main {

    static Random bucketNameGenerator = new Random();

    public static void main(String[] args) throws IOException{

        Path inputPath;
        FileChannel input;
        Long N;
        int B;
        int M;
        int S;

        inputPath = Paths.get(args[2]);
        input = FileChannel.open(inputPath);

        N = input.size();
        B = Integer.parseInt(args[0]);
        M = Integer.parseInt(args[1]);

        /* //Debbuging:
        System.out.println(String.format("B = %d, M = %d, N= %d\n",B, M, N));

        System.out.println(input.position()+"\n");
        List<int[]> segments = getInputBuffer(input, B);
        for (int[] segment : segments) {
            System.out.println(String.format("%d, %d, %d, %d\n", segment[0],segment[1],segment[2],segment[3]));
        }
        //System.out.println(input.position()+"\n");

        segments = getInputBuffer(input, B);
        for (int[] segment : segments) {
            System.out.println(String.format("%d, %d, %d, %d\n", segment[0],segment[1],segment[2],segment[3]));
        }
        //System.out.println(input.position()+"\n");

        segments = getInputBuffer(input, B);
        for (int[] segment : segments) {
            System.out.println(String.format("%d, %d, %d, %d\n", segment[0],segment[1],segment[2],segment[3]));
        }

        //System.out.println(input.position()+"\n");
        //List<Path> testGetBuckets = getBuckets(5);
	    //testGetBuckets.forEach(System.out::println);

	    */
        /*
        List<Integer> pivots = getPivots(50, inputPath.toString(), N.intValue(), "x");

        for (int p: pivots) {
            System.out.print(p+" ");
        }
        */

    }


    /*
    *  Finds the S-1 pivots for distribution sort.
    *  S: See pdf. Corresponds to min(m , n/m)
    *  path: Path to file we are sorting later on with the pivots found here.
    *  lengthOfFile: Number of lines in the file. This is used as an upper bound to generate the random sample of lines.
    *  coordToSort: Coordinate by which we are sorting the file (x or y).
    * */
    public static List<Integer> getPivots(int S, String path, int lengthOfFile, String coordToSort){
        try{
            // First we need to take S*log(S) samples at random from the file.
            RandomAccessFile raf = new RandomAccessFile(path,"rw");
            int sampleSize = (int)Math.floor(S*Math.log(S));
            // We will store out sampled segments here:
            List<int[]> segments = new ArrayList<>();

            // These correspond to line numbers. For every line we have a segment, so we need sampleSize line numbers
            // generated at random.
            int sampleIndexes[] = new Random().ints(1, lengthOfFile).distinct().limit(sampleSize).toArray();


            for (int index: sampleIndexes) {
                // We position ourselves at the byte position inside the file.
                raf.seek((long) index);
                // We need to do this because the index can leave us in the middle of a line. So readline reads until
                // finding a \n character. The next time we do readline we are sure we are reading a whole line.
                raf.readLine();
                // This fancy one-liner simple reads the line in which we are, splits it by ',' and generates
                // an array of integers segments = [x_1 , y_1, x_2, y_2].
                int segment[] = Stream.of(raf.readLine().split(",")).mapToInt(Integer::parseInt).toArray();
                segments.add(segment);
            }

            // As stated in the pdf we sort the sampled segments. See SegmentComparator.
            Collections.sort(segments, new SegmentComparator(coordToSort));

            // We take the first Log(S) segments from the sample.
            List<int[]> selectedSegments = new ArrayList<>();
            for (int i = 1; i < S; i++) {
                selectedSegments.add(segments.get((int)Math.floor(i*Math.log(S))));
            }


            List<Integer> pivots = new ArrayList<>();

            // Depending on which coordinate we are sorting by we add the corresponding one to the pivots list.
            if(coordToSort.equals("x")){
                for (int[] s: selectedSegments){
                    pivots.add(s[0]);
                }
            }

            if(coordToSort.equals("y")) {
                for (int[] s : selectedSegments) {
                    pivots.add(s[1]);
                }
            }

            return pivots;

        }
        catch (Exception e){

            e.printStackTrace();
            return null;
        }


    }
    /*
    * Comparation function for sorting the random sample in getPivots.
    * Constructor: SegmentComparator
    * */
    static private class SegmentComparator implements Comparator<int[]>{

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

    /*
    * Generates in memory buffers for each of S buckets. We represent each bucket in memory as a List.
    * It returns a list of buckets, so its a list of lists.
    * These are restricted to a fixed size of B. We will need to check the size of each buffer after we add
    * a segment to it.
    * S: See pdf. Corresponds to min(m , n/m)
    * */
    public List<List<Integer>> getBucketsBuffers(int S){
        List<List<Integer>> bucketBufferList = new ArrayList<>();
        for (int i = 0; i < S; i++) {
            bucketBufferList.add(new ArrayList<Integer>());
        }
        return bucketBufferList;
    }

    /*
    * Returns a list of S Paths, each corresponding to one bucket.
    * We use bucketNameGenerator to give each file a different name.
    * All of the buckets will be stored in a directory called "buckets".
    * S: See pdf. Corresponds to min(m , n/m)
    * */
    public static List<Path> getBuckets(int S){
        List<Path> buckets = new ArrayList<>();
        for (int i = 0; i < S; i++) {
            // Paths.get just Constructs a Path, in which every argument is a component of the path.
            Path bucketFile = Paths.get("buckets", String.format("%d.csv", bucketNameGenerator.nextInt()));
            buckets.add(bucketFile);
        }

        return buckets;
    }

    /*
    * Given an input File of size N, we wish to take chunks of size B from it and start the sorting process.
    * This method returns a buffer filled with the segments inside the chunk of size B of the input file.
    * input : Input file. Using a FileChannel allows us read B bytes from the file.
    * B : Size of chunks we read from the file (bytes).
    * */
    public static List<int[]> getInputBuffer(FileChannel input, int B) throws IOException{

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
        List<int[]> lines = new ArrayList<>();
        String lineString = "";
        char c;

        int segments[];

        // We read from the buffer while it still has bytes to read.
        while(byteBuffer.hasRemaining()){

            // We get the first char from the buffer and check if it is a end of line.
            c = (char)byteBuffer.get();
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
            segments = Stream.of(lineString.split(",")).mapToInt(Integer::parseInt).toArray();
            lines.add(segments);
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
        input.position(input.position() - fileRewind);

        return lines;
    }





}
