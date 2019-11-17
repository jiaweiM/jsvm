package jsvm;

import com.beust.jcommander.Parameter;

import java.util.Arrays;

import static jsvm.KernelType.RBF;
import static jsvm.SVMType.C_SVC;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 28 Oct 2019, 10:55 AM
 */
public class SVMParameter implements Cloneable, java.io.Serializable
{
    @Parameter(names = "-s", description = "type of SVM")
    SVMType svmType = C_SVC;

    @Parameter(names = "-t", description = "type of kernel function")
    KernelType kernelType = RBF;

    @Parameter(names = "-d", description = "degree in kernel function")
    int degree = 3;    // for poly

    @Parameter(names = "-g", description = "gamma in kernel function (default 1/num_features)")
    double gamma = 0;    // for poly/rbf/sigmoid

    @Parameter(names = "-r", description = "coef0 in kernel function")
    double coef0 = 0;    // for poly/sigmoid

    // these are for training only
    @Parameter(names = "-m", description = "cache memory size in MB")
    double cacheSize = 100; // in MB
    /**
     * tolerance of termination criterion (default 0.001)
     */
    @Parameter(names = "-e", description = "tolerance of termination criterion")
    double eps = 1e-3;    // stopping criteria

    @Parameter(names = "-c", description = "parameter C of C-SVC, epsilon-SVR, and nu-SVR")
    double C = 1;

    int nrWeight;        // for C_SVC
    int[] weightLabel;    // for C_SVC
    double[] weight;        // for C_SVC

    @Parameter(names = "-n", description = "nu of nu-SVC, one-class SVM, and nu-SVR")
    double nu = 0.5;

    @Parameter(names = "-p", description = "epsilon in loss function of epsilon-SVR")
    double p = 0.1;    // for EPSILON_SVR

    @Parameter(names = "-h", description = "whether to use the shrinking heuristics")
    boolean shrinking = true;
    /**
     * whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0)
     */
    @Parameter(names = "-b", description = "whether to train a SVC or SVR model for probability estimates")
    boolean probability = false;

    public SVMParameter(SVMParameter parameter)
    {
        this.svmType = parameter.svmType;
        this.kernelType = parameter.kernelType;
        this.degree = parameter.degree;
        this.gamma = parameter.gamma;
        this.coef0 = parameter.coef0;
        this.cacheSize = parameter.cacheSize;
        this.eps = parameter.eps;
        this.C = parameter.C;
        this.nrWeight = parameter.nrWeight;
        this.weightLabel = Arrays.copyOf(parameter.weightLabel, parameter.weightLabel.length);
        this.weight = Arrays.copyOf(parameter.weight, parameter.weight.length);
        this.nu = parameter.nu;
        this.p = parameter.p;
        this.shrinking = parameter.shrinking;
        this.probability = parameter.probability;
    }

    public SVMParameter()
    {
        nrWeight = 0; // set the parameter C of class i to weight*C, for C-SVC (default 1)
        weightLabel = new int[0]; //
        weight = new double[0]; // n-fold cross validation mode
    }

    /**
     * @return the {@link SVMType}
     */
    public SVMType getSVMType()
    {
        return svmType;
    }

    /**
     * setter of the svmType
     *
     * @param svmType {@link SVMType}
     */
    public void setSVMType(SVMType svmType)
    {
        this.svmType = svmType;
    }

    /**
     * @return the {@link KernelType} type.
     */
    public KernelType getKernelType()
    {
        return kernelType;
    }

    /**
     * setter of the {@link KernelType}
     *
     * @param kernelType {@link KernelType}
     */
    public void setKernelType(KernelType kernelType)
    {
        this.kernelType = kernelType;
    }

    /**
     * @return the degree.
     */
    public int getDegree()
    {
        return degree;
    }

    public void setDegree(int degree)
    {
        this.degree = degree;
    }

    public double getGamma()
    {
        return gamma;
    }

    public void setGamma(double gamma)
    {
        this.gamma = gamma;
    }

    public double getCoef0()
    {
        return coef0;
    }

    public void setCoef0(double coef0)
    {
        this.coef0 = coef0;
    }

    public double getNu()
    {
        return nu;
    }

    public void setNu(double nu)
    {
        this.nu = nu;
    }

    public double getCacheSize()
    {
        return cacheSize;
    }

    public void setCacheSize(double cacheSize)
    {
        this.cacheSize = cacheSize;
    }

    public double getC()
    {
        return C;
    }

    public void setC(double c)
    {
        C = c;
    }

    public double getEps()
    {
        return eps;
    }

    public void setEps(double eps)
    {
        this.eps = eps;
    }

    public double getP()
    {
        return p;
    }

    public void setP(double p)
    {
        this.p = p;
    }

    public boolean isShrinking()
    {
        return shrinking;
    }

    public void setShrinking(boolean shrinking)
    {
        this.shrinking = shrinking;
    }

    public boolean isProbability()
    {
        return probability;
    }

    public void setProbability(boolean probability)
    {
        this.probability = probability;
    }

    public int getNrWeight()
    {
        return nrWeight;
    }

    public void setNrWeight(int nrWeight)
    {
        this.nrWeight = nrWeight;
    }

    public int[] getWeightLabel()
    {
        return weightLabel;
    }

    public void setWeightLabel(int[] weightLabel)
    {
        this.weightLabel = weightLabel;
    }

    public double[] getWeight()
    {
        return weight;
    }

    public void setWeight(double[] weight)
    {
        this.weight = weight;
    }

    public Object clone()
    {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
