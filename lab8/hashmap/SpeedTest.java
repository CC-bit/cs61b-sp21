package hashmap;

import speed.InsertRandomSpeedTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

public class SpeedTest {
    public static void main(String[] args) throws IOException {
        System.out.println("diffMap() running...");
        diffMap();
        System.out.println("diffMap() finished");
        System.out.println("diffBucket() running...");
        diffBucket();
        System.out.println("diffBucket() finished");
        System.out.println("all finished");
    }

    /** Make a bunch of speed tests and write the results in file. */
    public static void diffMap() throws IOException {
        int L = 2;
        int N = 100000;
        Path result = Path.of("speedTestResults.txt");
        StringBuilder sb = new StringBuilder();
        sb.append("---------------------------------------------------------\n")
                .append("Time needed to put N random strings of length 2 into map.\n\n")
                .append("N         MyMap    ULLMap    HashMap\n");

        for (int i = 0; i < 8; i++) {
            MyHashMap<String, Integer> my = new MyHashMap<>();
            ULLMap<String, Integer> ull = new ULLMap<>();
            HashMap<String, Integer> hash = new HashMap<>();
            double myTime = InsertRandomSpeedTest.insertRandom(my, N, L);
            double ullTime = InsertRandomSpeedTest.insertRandom(ull, N, L);
            double hashTime = InsertRandomSpeedTest.insertRandom(hash, N, L);
            sb.append(N).append("    ")
                    .append(myTime).append("    ")
                    .append(ullTime).append("    ")
                    .append(hashTime).append("\n");

            N *= 2;
        }
        sb.append("\nQ: When would it be better to use a BSTMap/TreeMap instead of a HashMap?");
        Files.writeString(result, sb.toString());
    }

    public static void diffBucket() throws IOException {
        int L = 2;
        int N = 100000;
        Path result = Path.of("speedTestResults.txt");
        StringBuilder sb = new StringBuilder();
        sb.append("\n---------------------------------------------------------\n")
                .append("Time needed to put N random strings of length 2 into map.\n\n")
                .append("N         AL       HS       LL       PQ       TS\n");

        for (int i = 0; i < 8; i++) {
            MyHashMapALBuckets<String, Integer> al = new MyHashMapALBuckets<>();
            MyHashMapHSBuckets<String, Integer> hs = new MyHashMapHSBuckets<>();
            MyHashMapLLBuckets<String, Integer> ll = new MyHashMapLLBuckets<>();
            MyHashMapPQBuckets<String, Integer> pq = new MyHashMapPQBuckets<>();
            MyHashMapTSBuckets<String, Integer> ts = new MyHashMapTSBuckets<>();
            double alTime = InsertRandomSpeedTest.insertRandom(al, N, L);
            double hsTime = InsertRandomSpeedTest.insertRandom(hs, N, L);
            double llTime = InsertRandomSpeedTest.insertRandom(ll, N, L);
            double pqTime = InsertRandomSpeedTest.insertRandom(pq, N, L);
            double tsTime = InsertRandomSpeedTest.insertRandom(ts, N, L);
            sb.append(N).append("    ")
                    .append(alTime).append("    ")
                    .append(hsTime).append("    ")
                    .append(llTime).append("    ")
                    .append(pqTime).append("    ")
                    .append(tsTime).append("\n");

            N *= 2;
        }
        sb.append("\nQ: Would our hash table speed up if we were able to use a logarithmic\n"
                + " search over the TreeSet or a constant-time search over the HashSet?\n");
        Files.writeString(result, sb.toString(), StandardOpenOption.APPEND);
    }

}
