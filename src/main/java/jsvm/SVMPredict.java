package jsvm;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringTokenizer;

import static jsvm.SVMType.*;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 28 Oct 2019, 1:16 PM
 */
public class SVMPredict
{
    private static ISVMPrint svm_print_stdout = System.out::print;

    private static ISVMPrint svm_print_string = svm_print_stdout;

    static void info(String s)
    {
        svm_print_string.print(s);
    }

    private static double atof(String s)
    {
        return Double.parseDouble(s);
    }

    private static void exit_with_help()
    {
        System.err.print("usage: SVMPredict [options] test_file model_file output_file\n"
                + "options:\n"
                + "-b probability_estimates: whether to predict probability estimates, 0 or 1 (default 0); one-class SVM not supported yet\n"
                + "-q : quiet mode (no outputs)\n");
        System.exit(1);
    }

    /**
     * Predict default without probability.
     *
     * @param testFile  the data file
     * @param modelFile the model file
     * @throws IOException for data file IO exception.
     */
    public static void predict(String testFile, String modelFile) throws IOException
    {
        predict(testFile, modelFile, false);
    }

    /**
     * Predict
     *
     * @param testFile           the data file
     * @param modelFile          the model file
     * @param predictProbability true if predict probability.
     * @throws IOException for data file IO exception.
     */
    public static void predict(String testFile, String modelFile, boolean predictProbability) throws IOException
    {
        String outFile = testFile + ".predict";
        predict(testFile, modelFile, outFile, predictProbability);
    }

    public static void predict(String testFile, String modelFile, String outFile, boolean predictProbability) throws IOException
    {
        double error = 0;
        double sump = 0, sumt = 0, sumpp = 0, sumtt = 0, sumpt = 0;

        SVMModel model = SVM.loadModel(modelFile);
        if (model == null) {
            System.err.print("can't open model file " + modelFile + "\n");
            System.exit(1);
        }

        if (predictProbability) {
            if (SVM.checkProbabilityModel(model) == 0) {
                System.err.print("Model does not support probabiliy estimates\n");
                System.exit(1);
            }
        } else {
            if (SVM.checkProbabilityModel(model) != 0) {
                info("Model supports probability estimates, but disabled in prediction.\n");
            }
        }

        SVMType svmType = model.getSVMType();
        int nrClass = model.getNrClass();

        BufferedReader reader = Files.newBufferedReader(Paths.get(testFile));
        DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outFile)));
        double[] probEstimates = null;
        if (predictProbability) {
            if (svmType == EPSILON_SVR || svmType == NU_SVR) {
                info("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma=" + model.getSVRProbability() + "\n");
            } else {
                int[] labels = model.getLabels();
                probEstimates = new double[nrClass];
                output.writeBytes("labels");
                for (int j = 0; j < nrClass; j++)
                    output.writeBytes(" " + labels[j]);
                output.writeBytes("\n");
            }
        }

        int correct = 0;
        int total = 0;
        String line;
        while ((line = reader.readLine()) != null) {

            StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

            double target_label = atof(st.nextToken());
            int m = st.countTokens() / 2;
            SVMNode[] x = new SVMNode[m];
            for (int j = 0; j < m; j++) {
                x[j] = new SVMNode();
                x[j].index = Integer.parseInt(st.nextToken());
                x[j].value = atof(st.nextToken());
            }

            double predict_label;
            if (predictProbability && (svmType == C_SVC || svmType == NU_SVC)) {
                predict_label = SVM.predictProbability(model, x, probEstimates);
                output.writeBytes(predict_label + " ");
                for (int j = 0; j < nrClass; j++)
                    output.writeBytes(probEstimates[j] + " ");
                output.writeBytes("\n");
            } else {
                predict_label = SVM.predict(model, x);
                output.writeBytes(predict_label + "\n");
            }

            if (predict_label == target_label)
                ++correct;
            error += (predict_label - target_label) * (predict_label - target_label);
            sump += predict_label;
            sumt += target_label;
            sumpp += predict_label * predict_label;
            sumtt += target_label * target_label;
            sumpt += predict_label * target_label;
            ++total;
        }

        if (svmType == EPSILON_SVR || svmType == NU_SVR) {
            info("Mean squared error = " + error / total + " (regression)\n");
            info("Squared correlation coefficient = " +
                    ((total * sumpt - sump * sumt) * (total * sumpt - sump * sumt)) /
                            ((total * sumpp - sump * sump) * (total * sumtt - sumt * sumt)) +
                    " (regression)\n");
        } else
            info("Accuracy = " + (double) correct / total * 100 +
                    "% (" + correct + "/" + total + ") (classification)\n");

        reader.close();
        output.close();
    }

    public static void main(String[] argv)
    {
        int i, predict_probability = 0;
        svm_print_string = svm_print_stdout;

        // parse options
        for (i = 0; i < argv.length; i++) {
            if (argv[i].charAt(0) != '-') break;
            ++i;
            switch (argv[i - 1].charAt(1)) {
                case 'b':
                    predict_probability = Integer.parseInt(argv[i]);
                    break;
                case 'q':
                    svm_print_string = ISVMPrint.NO_PRINT;
                    i--;
                    break;
                default:
                    System.err.print("Unknown option: " + argv[i - 1] + "\n");
                    exit_with_help();
            }
        }
        if (i >= argv.length - 2)
            exit_with_help();
        try {
            String input = argv[i];
            String model = argv[i + 1];
            String out = argv[i + 2];

            predict(input, model, out, predict_probability == 1);
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            exit_with_help();
        }
    }
}
