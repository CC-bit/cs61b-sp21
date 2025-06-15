package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeAList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# tests", "microsec/op");
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
        public DoOneTest(int ops) {
            this.ops= ops;
            changeNandTime(ops);
        }

        private void changeNandTime(int ops) {
            AList<Integer> test = new AList<Integer>();
            Stopwatch sw = new Stopwatch();
            for (int i = 0; i < ops; i += 1) {
                test.addLast(1);
            }
            time = sw.elapsedTime();
            n = test.size();
        }
    }

    public static void timeAListConstruction() {
        // TODO: YOUR CODE HERE
        AList<Integer> tests= new AList<Integer>();
        int n = 1000, rows = 8;
        for (int i = 0; i < rows; i += 1) {
            tests.addLast(n);
            n *= 2;
        }

        AList<Integer> Ns = new AList<Integer>();
        AList<Double> times = new AList<Double>();
        AList<Integer> opCount = new AList<Integer>();

        for (int i = 0; i < tests.size(); i += 1) {
            DoOneTest doOne = new DoOneTest(tests.get(i));
            Ns.addLast(doOne.n);
            times.addLast(doOne.time);
            opCount.addLast(doOne.ops);
        }

        printTimingTable(Ns, times, opCount);

    }

    public static void main(String[] args) {timeAListConstruction();}
}
