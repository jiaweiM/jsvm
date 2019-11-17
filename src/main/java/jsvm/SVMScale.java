package jsvm;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Formatter;

/**
 * This is a tool for scaling input data file.
 *
 * @author JiaweiMao
 * @version 1.1.0
 * @since 28 Oct 2019, 1:24 PM
 */
public class SVMScale
{
    @Parameter(names = "-l", description = "x scaling lower limit")
    private double xLower = -1.0;

    @Parameter(names = "-u", description = "x scaling upper limit")
    private double xUpper = 1.0;

    @Parameter(names = "-yl", description = "y scaling lower limit, provide it if do y scaling")
    private double yLower;

    @Parameter(names = "-yu", description = "y scaling upper limit, provide it if do y scaling")
    private double yUpper;

    @Parameter(names = "-y", description = "do y scaling")
    private boolean y_scaling = false;

    @Parameter(description = "data file", required = true)
    private String dataFile;

    @Parameter(names = "-o", description = "Output scaled data file")
    private String outFile;

    @Parameter(names = "-s", description = "output scaling parameters path")
    private String saveParameterFile;

    @Parameter(names = "-r", description = "restore scaling parameters file")
    private String restoreParameterFile;

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

    public SVMScale() { }

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
     */
    private void updateMaxIndex()
    {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(dataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.trim().split(SVMProblem.SPLITTER);
                for (int i = 1; i < values.length; i++) {
                    String value = values[i];
                    if (value.trim().isEmpty()) {
                        continue;
                    }
                    int index = Integer.parseInt(values[i]);
                    if (index > maxIndex)
                        maxIndex = index;
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
     */
    private void updateMaxIndexFromRangeFile()
    {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(restoreParameterFile))) {
            if (reader.read() == 'y') { // scaling y
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
                if (index > maxIndex)
                    maxIndex = index;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateLimit()
    {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(dataFile))) {
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
     */
    public void updateLimitRestore()
    {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(restoreParameterFile))) {
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

    private void writeRangeFile()
    {
        Formatter formatter = new Formatter(new StringBuilder());
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(saveParameterFile))) {

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

    public void scale()
    {
        if (!(xUpper > xLower) || (y_scaling && !(yUpper > yLower))) {
            throw new IllegalArgumentException("Inconsistent lower/upper specification");
        }

        if (restoreParameterFile != null && saveParameterFile != null) {
            throw new IllegalArgumentException("Cannot use -r and -s simultaneously");
        }

        /* assumption: min index of attributes is 1 */
        /* pass 1: find out max index of attributes */
        maxIndex = 0;
        if (restoreParameterFile != null) {
            updateMaxIndexFromRangeFile();
        }

        updateMaxIndex();
        try {
            featureMax = new double[maxIndex + 1];
            featureMin = new double[maxIndex + 1];
        } catch (OutOfMemoryError e) {
            System.err.println("can't allocate enough memory");
            System.exit(1);
        }

        Arrays.fill(featureMax, -Double.MAX_VALUE);
        Arrays.fill(featureMin, Double.MAX_VALUE);
        updateLimit();

        /* pass 2.5: save/restore feature_min/feature_max */
        if (restoreParameterFile != null) {
            updateLimitRestore();
        }

        if (saveParameterFile != null) {
            writeRangeFile();
        }

        /* pass 3: scale */
        scale(dataFile, outFile);

        if (new_num_nonzeros > numNonzeros)
            System.err.print("WARNING: original #nonzeros " + numNonzeros + "\n"
                    + "         new      #nonzeros " + new_num_nonzeros + "\n"
                    + "Use -l 0 if many original feature values are zeros\n");
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
        this.dataFile = dataFile;
        this.saveParameterFile = saveParamFile;
        this.outFile = outFile;
        this.restoreParameterFile = restoreFile;

        if (!(xUpper > xLower) || (y_scaling && !(yUpper > yLower))) {
            throw new IllegalArgumentException("Inconsistent lower/upper specification");
        }

        if (restoreFile != null && saveParamFile != null) {
            throw new IllegalArgumentException("Cannot use -r and -s simultaneously");
        }

        /* assumption: min index of attributes is 1 */
        /* pass 1: find out max index of attributes */
        maxIndex = 0;
        if (restoreFile != null) {
            updateMaxIndexFromRangeFile();
        }

        updateMaxIndex();
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

        updateLimit();

        /* pass 2.5: save/restore feature_min/feature_max */
        if (restoreFile != null) {
            updateLimitRestore();
        }

        if (saveParamFile != null) {
            writeRangeFile();
        }

        /* pass 3: scale */
        scale(dataFile, outFile);

        if (new_num_nonzeros > numNonzeros)
            System.err.print("WARNING: original #nonzeros " + numNonzeros + "\n"
                    + "         new      #nonzeros " + new_num_nonzeros + "\n"
                    + "Use -l 0 if many original feature values are zeros\n");
    }

    public static void main(String[] argv)
    {
        SVMScale scale = new SVMScale();
        JCommander commander = JCommander.newBuilder().addObject(scale).build();
        commander.setProgramName("SVMScale");
        try {
            commander.parse(argv);
            scale.scale();
        } catch (Exception e) {
            commander.usage();
        }
    }
}
