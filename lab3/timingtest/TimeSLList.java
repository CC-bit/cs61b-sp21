package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }
    private static class DoOneTest{
        public int n;
        public double time;
        public int ops;
        public DoOneTest(int n, int ops) {
            this.n = n;
            this.ops= ops;
            this.time = calcTime();
        }

        private double calcTime() {
            SLList<Integer> test = new SLList<Integer>();
            for (int i = 0; i < n; i += 1) {
                test.addFirst(1);
            }
            Stopwatch sw = new Stopwatch();
            for (int i = 0; i < ops; i += 1) {
                test.getLast();
            }
            return sw.elapsedTime();
        }
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        int n = 1000, m = 10000, rows = 8, inputs = 2;
        AList<int[]> tests = new AList<int[]>();
        for (int i = 0; i < rows; i += 1) {
            int[] t = new int[inputs];
            t[0] = n;
            t[1] = m;
            n *= 2;
            tests.addLast(t);
        }

        AList<Integer> Ns = new AList<Integer>();
        AList<Double> times = new AList<Double>();
        AList<Integer> opCount = new AList<Integer>();

        for (int i = 0; i < tests.size(); i += 1) {
            DoOneTest doOne = new DoOneTest(tests.get(i)[0], tests.get(i)[1]);
            Ns.addLast(doOne.n);
            times.addLast(doOne.time);
            opCount.addLast(doOne.ops);
        }

        printTimingTable(Ns, times, opCount);
    }

    public static void main(String[] args) {timeGetLast();}

}
