package jsvm;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Formatter;

/**
 * This is a tool for scaling input data file.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 28 Oct 2019, 1:24 PM
 */
public class SVMScale
{
    /**
     * x scaling lower limit (default -1)
     */
    private double xLower = -1.0;
    /**
     * x scaling upper limit (default +1)
     */
    private double xUpper = 1.0;
    /**
     * y scaling limits (default: no y scaling)
     */
    private double yLower;
    private double yUpper;
    private boolean y_scaling = false;
    private double[] featureMax;
    private double[] featureMin;
    /**
     * maximum y-value of the data
     */
    private double yMax = -Double.MAX_VALUE;
    private double yMin = Double.MAX_VALUE;
    private int maxIndex;
    private long numNonzeros = 0;
    /**
     * non zeros values after scale.
     */
    private long new_num_nonzeros = 0;

    private static void exit_with_help()
    {
        System.out.print(
                "Usage: SVMScale [options] data_filename\n"
                        + "options:\n"
                        + "-l lower : x scaling lower limit (default -1)\n"
                        + "-u upper : x scaling upper limit (default +1)\n"
                        + "-y y_lower y_upper : y scaling limits (default: no y scaling)\n"
                        + "-s save_filename : save scaling parameters to save_filename\n"
                        + "-r restore_filename : restore scaling parameters from restore_filename\n"
        );
        System.exit(1);
    }

    /**
     * @return max index in the dataset.
     */
    public int getMaxIndex()
    {
        return maxIndex;
    }

    /**
     * set the lower limit of x
     *
     * @param lower x lower limit, default to be -1.
     */
    public void setXLower(double lower)
    {
        this.xLower = lower;
    }

    /**
     * set the upper limit of x
     *
     * @param upper x upper limit, default to be 1.0
     */
    public void setXUpper(double upper)
    {
        this.xUpper = upper;
    }

    /**
     * set the lower limit of y
     *
     * @param y_lower y lower limit
     */
    public void setYLower(double y_lower)
    {
        this.yLower = y_lower;
    }

    /**
     * set the upper limit of y
     *
     * @param y_upper y upper limit
     */
    public void setYUpper(double y_upper)
    {
        this.yUpper = y_upper;
    }

    /**
     * find the max index and number of points in the dataset.
     *
     * @param file dataset file.
     */
    private void updateMaxIndex(String file)
    {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.trim().split(SVMProblem.SPLITTER);
                for (int i = 1; i < values.length; i++) {
                    String value = values[i];
                    if (value.trim().isEmpty()) {
                        continue;
                    }
                    int index = Integer.parseInt(values[i]);
                    maxIndex = Math.max(maxIndex, index);
                    numNonzeros++;
                    i++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * update max index
     *
     * @param file the parameter file generate previously.
     */
    private void updateMaxIndexFromRangeFile(String file)
    {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(file))) {
            if (reader.read() == 'y') {
                reader.readLine(); //y
                reader.readLine(); // yLower, yUpper
                reader.readLine(); // yMin, yMax
            }
            reader.readLine(); //x
            reader.readLine(); // xLower, xUpper

            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(SVMProblem.SPLITTER);
                int index = Integer.parseInt(values[0]);
                this.maxIndex = Math.max(maxIndex, index);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateLimit(String file)
    {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.trim().split(SVMProblem.SPLITTER);

                double target = Double.parseDouble(values[0]);
                yMax = Math.max(yMax, target);
                yMin = Math.min(yMin, target);

                int nextIndex = 1;
                for (int i = 1; i < values.length; i++) {
                    if (values[i].trim().isEmpty())
                        continue;

                    int index = Integer.parseInt(values[i]);
                    double value = Double.parseDouble(values[i + 1]);

                    for (int j = nextIndex; j < index; j++) {
                        featureMin[j] = Math.min(featureMin[j], 0);
                        featureMax[j] = Math.max(featureMax[j], 0);
                    }
                    featureMax[index] = Math.max(featureMax[index], value);
                    featureMin[index] = Math.min(featureMin[index], value);
                    nextIndex = index + 1;
                    i++;
                }
                for (int j = nextIndex; j <= maxIndex; j++) {
                    featureMax[j] = Math.max(featureMax[j], 0);
                    featureMin[j] = Math.min(featureMin[j], 0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * update data range.
     *
     * @param file parameter file generate previously.
     */
    public void updateLimitRestore(String file)
    {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(file))) {
            reader.mark(2);
            if (reader.read() == 'y') {
                reader.readLine(); // skip '\n' after 'y'
                String line = reader.readLine();
                String[] lowerUpper = line.split(SVMProblem.SPLITTER);
                yLower = Double.parseDouble(lowerUpper[0]);
                yUpper = Double.parseDouble(lowerUpper[1]);

                line = reader.readLine();
                String[] minMax = line.split(" ");
                yMin = Double.parseDouble(minMax[0]);
                yMax = Double.parseDouble(minMax[1]);
                y_scaling = true;
            } else {
                reader.reset();
            }

            if (reader.read() == 'x') {
                reader.readLine();
                String line = reader.readLine();
                String[] s = line.split(SVMProblem.SPLITTER);
                xLower = Double.parseDouble(s[0]);
                xUpper = Double.parseDouble(s[1]);

                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(SVMProblem.SPLITTER);
                    int index = Integer.parseInt(values[0]);
                    double min = Double.parseDouble(values[1]);
                    double max = Double.parseDouble(values[2]);
                    if (index <= maxIndex) {
                        featureMin[index] = min;
                        featureMax[index] = max;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeRangeFile(String out)
    {
        Formatter formatter = new Formatter(new StringBuilder());
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(out))) {

            if (y_scaling) {
                formatter.format("y\n");
                formatter.format("%.16g %.16g\n", yLower, yUpper);
                formatter.format("%.16g %.16g\n", yMin, yMax);
            }
            formatter.format("x\n");
            formatter.format("%.16g %.16g\n", xLower, xUpper);
            for (int i = 1; i <= maxIndex; i++) {
                if (featureMin[i] != featureMax[i]) {
                    formatter.format("%d %.16g %.16g\n", i, featureMin[i], featureMax[i]);
                }
            }
            writer.write(formatter.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void scale(String file, String targetFile)
    {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(file));
             PrintWriter writer = new PrintWriter(targetFile)) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.trim().split(SVMProblem.SPLITTER);
                double target = Double.parseDouble(values[0]);
                writer.print(getTarget(target) + " ");

                for (int i = 1; i < values.length; i++) {
                    if (values[i].trim().isEmpty())
                        continue;

                    int index = Integer.parseInt(values[i]);
                    double value = Double.parseDouble(values[i + 1]);
                    i++;

                    if (featureMin[index] == featureMax[index])
                        continue;

                    double val = getValue(index, value);
                    writer.print(index + ":" + val + " ");
                }
                writer.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double getTarget(double value)
    {
        if (y_scaling) {
            if (value == yMin)
                return yLower;
            if (value == yMax)
                return yUpper;
            return yLower + (yUpper - yLower) * (value - yMin) / (yMax - yMin);
        }

        return value;
    }

    private double getValue(int index, double value)
    {
        double rValue;
        if (value == featureMin[index])
            rValue = xLower;
        else if (value == featureMax[index])
            rValue = xUpper;
        else
            rValue = xLower + (xUpper - xLower) * (value - featureMin[index]) / (featureMax[index] - featureMin[index]);

        if (rValue != 0) {
            new_num_nonzeros++;
        }
        return rValue;
    }

    public void scaleTest(String dataFile, String paramFile)
    {
        String outFile = dataFile + ".scale";
        scale(dataFile, null, outFile, paramFile);
    }

    public void scaleTest(String dataFile, String paramFile, String outFile)
    {
        scale(dataFile, null, outFile, paramFile);
    }

    /**
     * Execute the scale after setting parameters, write scale parameter to file name end with ".range",
     * writer scaled data to file name end with ".scale";
     *
     * @param dataFile the data file
     */
    public void scaleTrain(String dataFile)
    {
        String saveParamFile = dataFile + ".range";
        String outFile = dataFile + ".scale";

        this.scale(dataFile, saveParamFile, outFile, null);
    }

    /**
     * Execute the scale after setting parameters.
     *
     * @param dataFile      the data file
     * @param saveParamFile path to save parameter
     * @param outFile       output file.
     */
    public void scaleTrain(String dataFile, String saveParamFile, String outFile)
    {
        this.scale(dataFile, saveParamFile, outFile, null);
    }

    /**
     * Execute the scale after setting parameters, restoreFile and saveParamFile can only exits one, the other should be null.
     *
     * @param dataFile      data file
     * @param saveParamFile path to save parameter
     * @param outFile       data file after scaling.
     * @param restoreFile   parameter generated previously
     */
    private void scale(String dataFile, String saveParamFile, String outFile, String restoreFile)
    {
        if (!(xUpper > xLower) || (y_scaling && !(yUpper > yLower))) {
            throw new IllegalArgumentException("Inconsistent lower/upper specification");
        }

        if (restoreFile != null && saveParamFile != null) {
            throw new IllegalArgumentException("Cannot use -r and -s simultaneously");
        }

//        SVMProblem problem = new SVMProblem(Paths.get(dataFile));

        /* assumption: min index of attributes is 1 */
        /* pass 1: find out max index of attributes */
        maxIndex = 0;
        if (restoreFile != null) {
            updateMaxIndexFromRangeFile(restoreFile);
        }

//        maxIndex = problem.getMaxIndex();
//        this.numNonzeros = problem.size();
        updateMaxIndex(dataFile);
        try {
            featureMax = new double[maxIndex + 1];
            featureMin = new double[maxIndex + 1];
        } catch (OutOfMemoryError e) {
            System.err.println("can't allocate enough memory");
            System.exit(1);
        }

        for (int i = 0; i <= maxIndex; i++) {
            featureMax[i] = -Double.MAX_VALUE;
            featureMin[i] = Double.MAX_VALUE;
        }

        updateLimit(dataFile);
//        double[] y = problem.getY();
//        for (double v : y) {
//            yMax = Math.max(yMax, v);
//            yMin = Math.min(yMin, v);
//        }

//        SVMNode[][] x = problem.getX();
//        for (SVMNode[] nodes : x) {
//            int nextIndex = 1;
//            for (SVMNode node : nodes) {
//                int index = node.getIndex();
//                double value = node.getValue();
//                for (int j = nextIndex; j < index; j++) {
//                    featureMin[j] = Math.min(featureMin[j], 0);
//                    featureMax[j] = Math.max(featureMax[j], 0);
//                }
//                featureMin[index] = Math.min(featureMin[index], value);
//                featureMax[index] = Math.max(featureMax[index], value);
//                nextIndex = index + 1;
//            }
//            for (int j = nextIndex; j <= maxIndex; j++) {
//                featureMin[j] = Math.min(featureMin[j], 0);
//                featureMax[j] = Math.max(featureMax[j], 0);
//            }
//        }

        /* pass 2.5: save/restore feature_min/feature_max */
        if (restoreFile != null) {
            updateLimitRestore(restoreFile);
        }

        if (saveParamFile != null) {
            writeRangeFile(saveParamFile);
        }

        /* pass 3: scale */
        scale(dataFile, outFile);
//        try (PrintWriter writer = new PrintWriter(outFile)) {
//            for (int i = 0; i < y.length; i++) {
//                writer.print(y[i] + " ");
//                SVMNode[] nodes = x[i];
//
//                for (SVMNode node : nodes) {
//                    int index = node.getIndex();
//                    if (featureMin[index] == featureMax[index])
//                        continue;
//
//                    double value = getValue(index, node.value);
//                    writer.print(index + ":" + value + " ");
//                }
//                writer.println();
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

        if (new_num_nonzeros > numNonzeros)
            System.err.print("WARNING: original #nonzeros " + numNonzeros + "\n"
                    + "         new      #nonzeros " + new_num_nonzeros + "\n"
                    + "Use -l 0 if many original feature values are zeros\n");
    }

    private static void run(String[] argv)
    {
        SVMScale svmScale = new SVMScale();
        int i;
        String saveParamFile = null;
        String restoreFile = null;
        String dataFile;
        String outDataFile = null;

        for (i = 0; i < argv.length; i++) {
            if (argv[i].charAt(0) != '-') break;
            ++i;
            switch (argv[i - 1].charAt(1)) {
                case 'l':
                    svmScale.setXLower(Double.parseDouble(argv[i]));
                    break;
                case 'u':
                    svmScale.setXUpper(Double.parseDouble(argv[i]));
                    break;
                case 'y':
                    svmScale.setYLower(Double.parseDouble(argv[i]));
                    ++i;
                    svmScale.setYUpper(Double.parseDouble(argv[i]));
                    svmScale.y_scaling = true;
                    break;
                case 's':
                    saveParamFile = argv[i];
                    break;
                case 'r':
                    restoreFile = argv[i];
                    break;
                case 'o':
                    outDataFile = argv[i];
                    break;
                default:
                    System.err.println("unknown option");
                    exit_with_help();
            }
        }

        if (argv.length != i + 1)
            exit_with_help();

        dataFile = argv[i];

        svmScale.scale(dataFile, saveParamFile, outDataFile, restoreFile);
    }

    public static void main(String[] argv)
    {
        run(argv);
    }
}
