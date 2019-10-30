package jsvm;

import java.util.Arrays;

import static jsvm.SVMType.EPSILON_SVR;
import static jsvm.SVMType.NU_SVR;

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
     * @return labels of this model, return an empty array if these is no labels in this model.
     */
    public int[] getLabels()
    {
        if (label != null)
            return Arrays.copyOf(label, nrClass);
        return new int[0];
    }
}
