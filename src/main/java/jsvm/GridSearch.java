package jsvm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 29 Oct 2019, 1:16 PM
 */
public class GridSearch
{
    private class MatchTask implements Callable<double[]>
    {
        private String file;
        private SVMParameter parameter;
        private int fold;

        public MatchTask(double c, double g, int fold, String file, SVMParameter parameter)
        {
            this.file = file;
            this.parameter = parameter;
            this.parameter.setC(Math.pow(2, c));
            this.parameter.setGamma(Math.pow(2, g));
            this.fold = fold;
        }

        @Override
        public double[] call() throws IOException
        {
            SVMTrain train = new SVMTrain(file, parameter, true, fold);
            Pair<Double, Double> result = train.train();

            return new double[]{parameter.getC(), parameter.getGamma(), result.getKey()};
        }
    }

    /**
     * the real C value is 2^c
     */
    private double cBegin = -5;
    private double cEnd = 15;
    private double cStep = 2;
    private boolean useC = true;

    /**
     * the real G value is 2^g
     */
    private double gBegin = 3;
    private double gEnd = -15;
    private double gStep = -2;
    private boolean useG = true;

    private int nrFold = 5;

    private String outPath;
    private String dataPath;
    private SVMParameter parameter;
    private int nrThread;

    public GridSearch(String dataPath, SVMParameter parameter)
    {
        this.dataPath = dataPath;
        this.outPath = dataPath + ".out";
        this.parameter = parameter;
        this.nrThread = Runtime.getRuntime().availableProcessors();
    }

    /**
     * set number of fold for cross validation.
     *
     * @param nrFold number of fold
     */
    public void setNrFold(int nrFold)
    {
        this.nrFold = nrFold;
    }

    /**
     * set the begin value of c, default to be -5.
     *
     * @param cBegin begin value of C, the actual C value is 2^C
     */
    public void setCBegin(double cBegin)
    {
        this.cBegin = cBegin;
    }

    public void setCEnd(double cEnd)
    {
        this.cEnd = cEnd;
    }

    public void setGBegin(double gBegin)
    {
        this.gBegin = gBegin;
    }

    public void setGEnd(double gEnd)
    {
        this.gEnd = gEnd;
    }

    public void setCStep(double cStep)
    {
        this.cStep = cStep;
    }

    public void setGStep(double gStep)
    {
        this.gStep = gStep;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException
    {
//        SVM.rand.setSeed(987654321);
        SVMParameter parameter = new SVMParameter();
        GridSearch gridSearch = new GridSearch("Z:\\MaoJiawei\\o-glycan\\liuluyao\\A-24-raw\\train\\train.csv.scale", parameter);
        gridSearch.grid();
    }

    /**
     * return a list of values of given step
     *
     * @param begin begin value
     * @param end   end value
     * @param step  step
     * @return list of values of given step
     */
    private static List<Double> getRange(double begin, double end, double step)
    {
        List<Double> list = new ArrayList<>();
        while (true) {
            if (step > 0 && begin > end)
                break;
            if (step < 0 && begin < end)
                break;
            list.add(begin);
            begin += step;
        }
        return list;
    }

    private static List<Double> permute(List<Double> seq)
    {
        int n = seq.size();
        if (n <= 1)
            return seq;

        int mid = n / 2;
        List<Double> left = permute(new ArrayList<>(seq.subList(0, mid)));
        List<Double> right = permute(new ArrayList<>(seq.subList(mid + 1, seq.size())));

        List<Double> ret = new ArrayList<>();
        ret.add(seq.get(mid));
        while (!left.isEmpty() || !right.isEmpty()) {
            if (!left.isEmpty())
                ret.add(left.remove(0));
            if (!right.isEmpty())
                ret.add(right.remove(0));
        }

        return ret;
    }


    public void grid() throws InterruptedException, ExecutionException
    {
        List<Double> cList = getRange(cBegin, cEnd, cStep);
        List<Double> gList = getRange(gBegin, gEnd, gStep);
        ExecutorService executorService = Executors.newFixedThreadPool(nrThread);

        List<MatchTask> tasks = new ArrayList<>();
        List<Double> cSeq = permute(cList);
        List<Double> gSeq = permute(gList);

        double nrC = cSeq.size();
        double nrG = gSeq.size();

        List<Pair<Double, Double>> jobs = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < nrC || j < nrG) {
            if (i / nrC < j / nrG) { // increase C resolution
//                List<Tuple> line = new ArrayList<>();
                for (int k = 0; k < j; k++) {
                    jobs.add(Pair.create(cSeq.get(i), gSeq.get(k)));
//                    line.add(new Tuple(cSeq.get(i), gSeq.get(k)));
                }
                i++;
//                jobs.add(line);
            } else {
                // increase g resolution
//                List<Tuple> line = new ArrayList<>();
                for (int k = 0; k < i; k++) {
                    jobs.add(Pair.create(cSeq.get(k), gSeq.get(j)));
//                    line.add(new Tuple(cSeq.get(k), gSeq.get(j)));
                }
                j++;
//                jobs.add(line);
            }
        }
        for (Pair<Double, Double> job : jobs) {
            MatchTask task = new MatchTask(job.getKey(), job.getValue(), nrFold, dataPath, new SVMParameter(parameter));
            tasks.add(task);
        }

        Double bestC = null;
        Double bestG = null;
        double bestRate = -1;

        List<Future<double[]>> futures = executorService.invokeAll(tasks);
        List<double[]> results = new ArrayList<>();
        for (Future<double[]> future : futures) {
            double[] values = future.get();
            results.add(values);
            double c = values[0];
            double g = values[1];
            double rate = values[2];
            if (bestRate < 0 || rate > bestRate || (rate == bestRate && g == bestG && c < bestC)) {
                bestC = c;
                bestG = g;
                bestRate = rate;
            }
        }

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        results.sort((o1, o2) -> Comparator.comparing((Function<double[], Double>) doubles -> doubles[1]).thenComparing(t -> t[0])
                .compare(o1, o2));

        System.out.print("\t");
        for (Double c : cList) {
            System.out.print("\t" + c);
        }
        System.out.println();
        System.out.print("\t");
        for (Double c : cList) {
            System.out.print("\t" + Math.pow(2, c));
        }

        double preG = Double.MIN_VALUE;
        for (double[] result : results) {
            double g = result[1];
            if (g != preG) {
                preG = g;
                System.out.println();
                System.out.print((Math.log(g) / Math.log(2)) + "\t" + g + "\t");
            }
            System.out.print(result[2] + "\t");
        }
        System.out.println();

        System.out.println(bestC + "\t" + bestG + "\t" + bestRate);
    }
}
