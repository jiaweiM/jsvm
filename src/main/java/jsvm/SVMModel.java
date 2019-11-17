package jsvm;

import java.io.*;
import java.util.Arrays;
import java.util.StringTokenizer;

import static jsvm.KernelType.*;
import static jsvm.SVMType.*;

public class SVMModel implements java.io.Serializable
{
    public SVMParameter param;    // parameter
    public int nrClass;        // number of classes, = 2 in regression/one class svm
    public int l;            // total #SV
    public SVMNode[][] SV;    // SVs (SV[l])
    public double[][] sv_coef;    // coefficients for SVs in decision functions (sv_coef[k-1][l])
    public double[] rho;        // constants in decision functions (rho[k*(k-1)/2])
    public double[] probA;         // pariwise probability information
    public double[] probB;
    public int[] sv_indices;       // sv_indices[0,...,nSV-1] are values in [1,...,num_traning_data] to indicate SVs in the training set

    // for classification only

    public int[] label;        // label of each class (label[k])
    public int[] nSV;        // number of SVs for each class (nSV[k])
    // nSV[0] + nSV[1] + ... + nSV[k-1] = l


    public SVMModel() { }

    public SVMModel(String file) throws IOException
    {
        this(new BufferedReader(new FileReader(file)));
    }

    public SVMModel(BufferedReader reader) throws IOException
    {
        rho = null;
        probA = null;
        probB = null;
        label = null;
        nSV = null;

        // read header
        if (!readHeader(reader)) {
            throw new IllegalArgumentException("ERROR: failed to read model");
        }

        // read sv_coef and SV
        int m = nrClass - 1;
        sv_coef = new double[m][l];
        SV = new SVMNode[l][];
        for (int i = 0; i < l; i++) {
            String line = reader.readLine();
            StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

            for (int k = 0; k < m; k++)
                sv_coef[k][i] = atof(st.nextToken());
            int n = st.countTokens() / 2;
            SV[i] = new SVMNode[n];
            for (int j = 0; j < n; j++) {
                SV[i][j] = new SVMNode();
                SV[i][j].index = atoi(st.nextToken());
                SV[i][j].value = atof(st.nextToken());
            }
        }

        reader.close();
    }

    private boolean readHeader(BufferedReader fp)
    {
        param = new SVMParameter();
        // parameters for training only won't be assigned, but arrays are assigned as null for safety
        param.nrWeight = 0;
        param.weightLabel = null;
        param.weight = null;

        try {
            while (true) {
                String cmd = fp.readLine();
                String arg = cmd.substring(cmd.indexOf(' ') + 1);

                if (cmd.startsWith("svm_type")) {
                    boolean found = false;
                    for (SVMType value : SVMType.values()) {
                        if (arg.contains(value.getName())) {
                            param.svmType = value;
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        System.err.print("unknown svm type.\n");
                        return false;
                    }
                } else if (cmd.startsWith("kernel_type")) {
                    boolean found = false;
                    for (KernelType value : KernelType.values()) {
                        if (arg.contains(value.getName())) {
                            param.kernelType = value;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        System.err.print("unknown kernel function.\n");
                        return false;
                    }
                } else if (cmd.startsWith("degree"))
                    param.degree = atoi(arg);
                else if (cmd.startsWith("gamma"))
                    param.gamma = atof(arg);
                else if (cmd.startsWith("coef0"))
                    param.coef0 = atof(arg);
                else if (cmd.startsWith("nr_class"))
                    nrClass = atoi(arg);
                else if (cmd.startsWith("total_sv"))
                    l = atoi(arg);
                else if (cmd.startsWith("rho")) {
                    int n = nrClass * (nrClass - 1) / 2;
                    rho = new double[n];
                    StringTokenizer st = new StringTokenizer(arg);
                    for (int i = 0; i < n; i++)
                        rho[i] = atof(st.nextToken());
                } else if (cmd.startsWith("label")) {
                    int n = nrClass;
                    label = new int[n];
                    StringTokenizer st = new StringTokenizer(arg);
                    for (int i = 0; i < n; i++)
                        label[i] = atoi(st.nextToken());
                } else if (cmd.startsWith("probA")) {
                    int n = nrClass * (nrClass - 1) / 2;
                    probA = new double[n];
                    StringTokenizer st = new StringTokenizer(arg);
                    for (int i = 0; i < n; i++)
                        probA[i] = atof(st.nextToken());
                } else if (cmd.startsWith("probB")) {
                    int n = nrClass * (nrClass - 1) / 2;
                    probB = new double[n];
                    StringTokenizer st = new StringTokenizer(arg);
                    for (int i = 0; i < n; i++)
                        probB[i] = atof(st.nextToken());
                } else if (cmd.startsWith("nr_sv")) {
                    int n = nrClass;
                    nSV = new int[n];
                    StringTokenizer st = new StringTokenizer(arg);
                    for (int i = 0; i < n; i++)
                        nSV[i] = atoi(st.nextToken());
                } else if (cmd.startsWith("SV")) {
                    break;
                } else {
                    System.err.print("unknown text in model file: [" + cmd + "]\n");
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static double atof(String s)
    {
        return Double.parseDouble(s);
    }

    private static int atoi(String s)
    {
        return Integer.parseInt(s);
    }

    public void saveModel(String modeFile) throws IOException
    {
        DataOutputStream fp = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(modeFile)));

        fp.writeBytes("svm_type " + param.svmType.getName() + "\n");
        fp.writeBytes("kernel_type " + param.kernelType.getName() + "\n");

        if (param.kernelType == POLY)
            fp.writeBytes("degree " + param.degree + "\n");

        if (param.kernelType == POLY || param.kernelType == RBF || param.kernelType == SIGMOID)
            fp.writeBytes("gamma " + param.gamma + "\n");

        if (param.kernelType == POLY || param.kernelType == SIGMOID)
            fp.writeBytes("coef0 " + param.coef0 + "\n");

        fp.writeBytes("nr_class " + nrClass + "\n");
        fp.writeBytes("total_sv " + l + "\n");

        fp.writeBytes("rho");
        for (int i = 0; i < nrClass * (nrClass - 1) / 2; i++)
            fp.writeBytes(" " + this.rho[i]);
        fp.writeBytes("\n");

        if (this.label != null) {
            fp.writeBytes("label");
            for (int i = 0; i < nrClass; i++)
                fp.writeBytes(" " + this.label[i]);
            fp.writeBytes("\n");
        }

        if (this.probA != null) // regression has probA only
        {
            fp.writeBytes("probA");
            for (int i = 0; i < nrClass * (nrClass - 1) / 2; i++)
                fp.writeBytes(" " + this.probA[i]);
            fp.writeBytes("\n");
        }
        if (this.probB != null) {
            fp.writeBytes("probB");
            for (int i = 0; i < nrClass * (nrClass - 1) / 2; i++)
                fp.writeBytes(" " + this.probB[i]);
            fp.writeBytes("\n");
        }

        if (this.nSV != null) {
            fp.writeBytes("nr_sv");
            for (int i = 0; i < nrClass; i++)
                fp.writeBytes(" " + this.nSV[i]);
            fp.writeBytes("\n");
        }

        fp.writeBytes("SV\n");

        for (int i = 0; i < l; i++) {
            for (int j = 0; j < nrClass - 1; j++)
                fp.writeBytes(sv_coef[j][i] + " ");

            SVMNode[] p = SV[i];
            if (param.kernelType == PRECOMPUTED)
                fp.writeBytes("0:" + (int) (p[0].value));
            else
                for (SVMNode svmNode : p)
                    fp.writeBytes(svmNode.index + ":" + svmNode.value + " ");
            fp.writeBytes("\n");
        }

        fp.close();
    }

    /**
     * @return {@link SVMType} of this model.
     */
    public SVMType getSVMType()
    {
        return param.getSVMType();
    }

    public int getNrClass()
    {
        return nrClass;
    }

    /**
     * @return the indices of support vectors.
     */
    public int[] getSVIndices()
    {
        if (sv_indices != null)
            return Arrays.copyOf(sv_indices, l);
        return new int[0];
    }

    /**
     * @return number of support vector.s
     */
    public int getNumberOfSV()
    {
        return l;
    }

    /**
     * @return the probability of SVR
     */
    public double getSVRProbability()
    {
        if ((param.svmType == EPSILON_SVR || param.svmType == NU_SVR) && probA != null)
            return probA[0];
        else {
            System.err.print("Model doesn't contain information for SVR probability inference\n");
            return 0;
        }
    }

    /**
     * @return true if this model support probability.
     */
    public boolean isSupportProbability()
    {
        return ((param.svmType == C_SVC || param.svmType == NU_SVC) && probA != null && probB != null) ||
                ((param.svmType == EPSILON_SVR || param.svmType == NU_SVR) &&
                        probA != null);
    }

    /**
     * @return labels of this model, return an empty array if these is no labels in this model.
     */
    public int[] getLabels()
    {
        if (label != null)
            return Arrays.copyOf(label, nrClass);
        return new int[0];
    }
}
