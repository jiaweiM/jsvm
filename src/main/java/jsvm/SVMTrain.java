package jsvm;

import java.io.IOException;
import java.nio.file.Paths;

import static jsvm.KernelType.PRECOMPUTED;
import static jsvm.KernelType.RBF;
import static jsvm.SVMType.*;


/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 28 Oct 2019, 9:46 AM
 */
public class SVMTrain
{
    private static final ISVMPrint NO_PRINT = s -> { };

    private SVMParameter parameter;
    private SVMProblem problem;
    private SVMModel model;
    private String inputFileName;
    private String modelFileName;
    private String errorMsg;
    private boolean crossValidation;
    private int nrFold;

    public SVMTrain(String inputFileName)
    {
        this(inputFileName, new SVMParameter(), false, 0);
    }

    public SVMTrain(String inputFileName, SVMParameter parameter, boolean crossValidation, int nrFold)
    {
        this.inputFileName = inputFileName;
        this.modelFileName = inputFileName + ".model";
        this.parameter = parameter;
        this.crossValidation = crossValidation;
        this.nrFold = nrFold;
    }

    public SVMTrain(String inputFileName, String modelFileName, SVMParameter parameter,
            boolean crossValidation, int nrFold)
    {
        this.inputFileName = inputFileName;
        this.modelFileName = modelFileName;
        this.parameter = parameter;
        this.crossValidation = crossValidation;
        this.nrFold = nrFold;
    }

    public String getModelFileName()
    {
        return modelFileName;
    }

    public SVMTrain(String[] args) throws IOException
    {
        parameter = new SVMParameter();
        parameter.svmType = C_SVC;
        parameter.kernelType = RBF;
        parameter.degree = 3; // degree in kernel function
        parameter.gamma = 0;    // 1/num_features
        parameter.coef0 = 0; // coef0
        parameter.nu = 0.5; // nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5)
        parameter.cacheSize = 100; // cache memory size in MB (default 100)
        parameter.C = 1; // C of C-SVC,epsilon-SVR,nu-SVR
        parameter.eps = 1e-3; // tolerance of termination criterion (default 0.001)
        parameter.p = 0.1; // the epsilon in loss function of epsilon-SVR (default 0.1)
        parameter.shrinking = 1; // whether to use the shrinking heuristics, 0 or 1 (default 1)
        parameter.probability = 0; // whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0)
        parameter.nrWeight = 0; // set the parameter C of class i to weight*C, for C-SVC (default 1)
        parameter.weightLabel = new int[0]; //
        parameter.weight = new double[0]; // n-fold cross validation mode
        crossValidation = false;

        parseCMD(args);
        this.problem = new SVMProblem(Paths.get(inputFileName));
        if (parameter.gamma == 0 && problem.getMaxIndex() > 0) {
            parameter.gamma = 1.0 / problem.getMaxIndex();
        }
        if (parameter.kernelType == PRECOMPUTED) {
            for (int i = 0; i < problem.l; i++) {
                if (problem.x[i][0].index != 0) {
                    System.err.print("Wrong kernel matrix: first column must be 0:sample_serial_number\n");
                    System.exit(1);
                }
                if ((int) problem.x[i][0].value <= 0 || (int) problem.x[i][0].value > problem.getMaxIndex()) {
                    System.err.print("Wrong input format: sample_serial_number out of range\n");
                    System.exit(1);
                }
            }
        }

        errorMsg = SVM.checkParameter(problem, parameter);
        if (errorMsg != null) {
            System.err.print("ERROR: " + errorMsg + "\n");
            System.exit(1);
        }

        if (crossValidation) {
            do_cross_validation();
        } else {
            model = SVM.train(problem, parameter);
            SVM.saveModel(modelFileName, model);
        }
    }

    public void train() throws IOException
    {
        this.problem = new SVMProblem(Paths.get(inputFileName));
        if (parameter.gamma == 0 && problem.getMaxIndex() > 0) {
            parameter.gamma = 1.0 / problem.getMaxIndex();
        }
        if (parameter.kernelType == PRECOMPUTED) {
            for (int i = 0; i < problem.l; i++) {
                if (problem.x[i][0].index != 0) {
                    System.err.print("Wrong kernel matrix: first column must be 0:sample_serial_number\n");
                    System.exit(1);
                }
                if ((int) problem.x[i][0].value <= 0 || (int) problem.x[i][0].value > problem.getMaxIndex()) {
                    System.err.print("Wrong input format: sample_serial_number out of range\n");
                    System.exit(1);
                }
            }
        }

        errorMsg = SVM.checkParameter(problem, parameter);
        if (errorMsg != null) {
            System.err.print("ERROR: " + errorMsg + "\n");
            System.exit(1);
        }

        if (crossValidation) {
            do_cross_validation();
        } else {
            model = SVM.train(problem, parameter);
            SVM.saveModel(modelFileName, model);
        }
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
     * set true if do cross validation.
     *
     * @param crossValidation true if do cross validation.
     */
    public void setCrossValidation(boolean crossValidation)
    {
        this.crossValidation = crossValidation;
    }

    private static void run(String[] args)
    {
        SVMParameter parameter = new SVMParameter();

        boolean crossValidation = false;
        int nrFold = 0;
        ISVMPrint printFunc = null;
        int i;
        for (i = 0; i < args.length; i++) {
            if (args[i].charAt(0) != '-') break;

            if (++i >= args.length)
                exitWithHelp();

            switch (args[i - 1].charAt(1)) {
                case 's':
                    parameter.setSVMType(SVMType.ofIndex(Integer.parseInt(args[i])));
                    break;
                case 't':
                    parameter.setKernelType(KernelType.ofIndex(Integer.parseInt(args[i])));
                    break;
                case 'd':
                    parameter.setDegree(Integer.parseInt(args[i]));
                    break;
                case 'g':
                    parameter.setGamma(atof(args[i]));
                    break;
                case 'r':
                    parameter.setCoef0(atof(args[i]));
                    break;
                case 'n':
                    parameter.setNu(atof(args[i]));
                    break;
                case 'm':
                    parameter.setCacheSize(atof(args[i]));
                    break;
                case 'c':
                    parameter.setC(atof(args[i]));
                    break;
                case 'e':
                    parameter.setEps(atof(args[i]));
                    break;
                case 'p':
                    parameter.setP(atof(args[i]));
                    break;
                case 'h':
                    parameter.setShrinking(Integer.parseInt(args[i]));
                    break;
                case 'b':
                    parameter.setProbability(Integer.parseInt(args[i]));
                    break;
                case 'q':
                    printFunc = NO_PRINT;
                    i--;
                    break;
                case 'v':
                    crossValidation = true;
                    nrFold = Integer.parseInt(args[i]);
                    if (nrFold < 2) {
                        System.err.print("n-fold cross validation: n must >= 2\n");
                        exitWithHelp();
                    }
                    break;
                case 'w': {
                    ++parameter.nrWeight;
                    int[] oldLabel = parameter.weightLabel;
                    parameter.weightLabel = new int[parameter.nrWeight];
                    System.arraycopy(oldLabel, 0, parameter.weightLabel, 0, parameter.nrWeight - 1);

                    double[] oldWeight = parameter.weight;
                    parameter.weight = new double[parameter.nrWeight];
                    System.arraycopy(oldWeight, 0, parameter.weight, 0, parameter.nrWeight - 1);

                    parameter.weightLabel[parameter.nrWeight - 1] = Integer.parseInt(args[i - 1].substring(2));
                    parameter.weight[parameter.nrWeight - 1] = atof(args[i]);
                    break;
                }
                default:
                    System.err.print("Unknown option: " + args[i - 1] + "\n");
                    exitWithHelp();
            }
        }

        SVM.setPrintFunc(printFunc);
        if (i >= args.length)
            exitWithHelp();

        String inputFileName = args[i];
        String modelFileName;
        if (i < args.length - 1)
            modelFileName = args[i + 1];
        else {
            int p = args[i].lastIndexOf('/');
            ++p;    // whew...
            modelFileName = args[i].substring(p) + ".model";
        }

        SVMTrain train = new SVMTrain(inputFileName, modelFileName, parameter, crossValidation, nrFold);

    }

    public static void main(String[] args) throws IOException
    {
        run(args);
//        new SVMTrain(args);
    }

    private void do_cross_validation()
    {
        int i;
        int total_correct = 0;
        double total_error = 0;
        double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
        double[] target = new double[problem.l];

        SVM.svm_cross_validation(problem, parameter, nrFold, target);
        if (parameter.svmType == EPSILON_SVR ||
                parameter.svmType == NU_SVR) {
            for (i = 0; i < problem.l; i++) {
                double y = problem.y[i];
                double v = target[i];
                total_error += (v - y) * (v - y);
                sumv += v;
                sumy += y;
                sumvv += v * v;
                sumyy += y * y;
                sumvy += v * y;
            }
            System.out.print("Cross Validation Mean squared error = " + total_error / problem.l + "\n");
            System.out.print("Cross Validation Squared correlation coefficient = " +
                    ((problem.l * sumvy - sumv * sumy) * (problem.l * sumvy - sumv * sumy)) /
                            ((problem.l * sumvv - sumv * sumv) * (problem.l * sumyy - sumy * sumy)) + "\n"
            );
        } else {
            for (i = 0; i < problem.l; i++)
                if (target[i] == problem.y[i])
                    ++total_correct;
            System.out.print("Cross Validation Accuracy = " + 100.0 * total_correct / problem.l + "%\n");
        }
    }

    private static double atof(String s)
    {
        double d = Double.parseDouble(s);
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            System.err.print("NaN or Infinity in input\n");
            System.exit(1);
        }
        return (d);
    }

    private void parseCMD(String[] args)
    {
        ISVMPrint printFunc = null;
        int i;
        for (i = 0; i < args.length; i++) {
            if (args[i].charAt(0) != '-') break;

            if (++i >= args.length)
                exitWithHelp();

            switch (args[i - 1].charAt(1)) {
                case 's':
                    parameter.svmType = SVMType.ofIndex(Integer.parseInt(args[i]));
                    break;
                case 't':
                    parameter.kernelType = KernelType.ofIndex(Integer.parseInt(args[i]));
                    break;
                case 'd':
                    parameter.degree = Integer.parseInt(args[i]);
                    break;
                case 'g':
                    parameter.gamma = atof(args[i]);
                    break;
                case 'r':
                    parameter.coef0 = atof(args[i]);
                    break;
                case 'n':
                    parameter.nu = atof(args[i]);
                    break;
                case 'm':
                    parameter.cacheSize = atof(args[i]);
                    break;
                case 'c':
                    parameter.C = atof(args[i]);
                    break;
                case 'e':
                    parameter.eps = atof(args[i]);
                    break;
                case 'p':
                    parameter.p = atof(args[i]);
                    break;
                case 'h':
                    parameter.shrinking = Integer.parseInt(args[i]);
                    break;
                case 'b':
                    parameter.probability = Integer.parseInt(args[i]);
                    break;
                case 'q':
                    printFunc = NO_PRINT;
                    i--;
                    break;
                case 'v':
                    crossValidation = true;
                    nrFold = Integer.parseInt(args[i]);
                    if (nrFold < 2) {
                        System.err.print("n-fold cross validation: n must >= 2\n");
                        exitWithHelp();
                    }
                    break;
                case 'w':
                    ++parameter.nrWeight;
                {
                    int[] old = parameter.weightLabel;
                    parameter.weightLabel = new int[parameter.nrWeight];
                    System.arraycopy(old, 0, parameter.weightLabel, 0, parameter.nrWeight - 1);
                }
                {
                    double[] old = parameter.weight;
                    parameter.weight = new double[parameter.nrWeight];
                    System.arraycopy(old, 0, parameter.weight, 0, parameter.nrWeight - 1);
                }
                parameter.weightLabel[parameter.nrWeight - 1] = Integer.parseInt(args[i - 1].substring(2));
                parameter.weight[parameter.nrWeight - 1] = atof(args[i]);
                break;
                default:
                    System.err.print("Unknown option: " + args[i - 1] + "\n");
                    exitWithHelp();
            }
        }

        SVM.setPrintFunc(printFunc);

        if (i >= args.length)
            exitWithHelp();

        inputFileName = args[i];

        if (i < args.length - 1)
            modelFileName = args[i + 1];
        else {
            int p = args[i].lastIndexOf('/');
            ++p;    // whew...
            modelFileName = args[i].substring(p) + ".model";
        }
    }

    private static void exitWithHelp()
    {
        System.out.print(
                "Usage: SVMTrain [options] training_set_file [model_file]\n"
                        + "options:\n"
                        + "-s svm_type : set type of SVM (default 0)\n"
                        + "	0 -- C-SVC		(multi-class classification)\n"
                        + "	1 -- nu-SVC		(multi-class classification)\n"
                        + "	2 -- one-class SVM\n"
                        + "	3 -- epsilon-SVR	(regression)\n"
                        + "	4 -- nu-SVR		(regression)\n"
                        + "-t kernel_type : set type of kernel function (default 2)\n"
                        + "	0 -- linear: u'*v\n"
                        + "	1 -- polynomial: (gamma*u'*v + coef0)^degree\n"
                        + "	2 -- radial basis function: exp(-gamma*|u-v|^2)\n"
                        + "	3 -- sigmoid: tanh(gamma*u'*v + coef0)\n"
                        + "	4 -- precomputed kernel (kernel values in training_set_file)\n"
                        + "-d degree : set degree in kernel function (default 3)\n"
                        + "-g gamma : set gamma in kernel function (default 1/num_features)\n"
                        + "-r coef0 : set coef0 in kernel function (default 0)\n"
                        + "-c cost : set the parameter C of C-SVC, epsilon-SVR, and nu-SVR (default 1)\n"
                        + "-n nu : set the parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5)\n"
                        + "-p epsilon : set the epsilon in loss function of epsilon-SVR (default 0.1)\n"
                        + "-m cachesize : set cache memory size in MB (default 100)\n"
                        + "-e epsilon : set tolerance of termination criterion (default 0.001)\n"
                        + "-h shrinking : whether to use the shrinking heuristics, 0 or 1 (default 1)\n"
                        + "-b probability_estimates : whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0)\n"
                        + "-wi weight : set the parameter C of class i to weight*C, for C-SVC (default 1)\n"
                        + "-v n : n-fold cross validation mode\n"
                        + "-q : quiet mode (no outputs)\n"
        );
        System.exit(1);
    }
}
