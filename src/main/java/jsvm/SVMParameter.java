package jsvm;

import static jsvm.KernelType.RBF;
import static jsvm.SVMType.C_SVC;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 28 Oct 2019, 10:55 AM
 */
public class SVMParameter implements Cloneable, java.io.Serializable
{
    public SVMType svmType;
    public KernelType kernelType;
    public int degree;    // for poly
    public double gamma;    // for poly/rbf/sigmoid
    public double coef0;    // for poly/sigmoid

    // these are for training only
    /**
     * cache memory size in MB (default 100)
     */
    public double cacheSize; // in MB
    /**
     * tolerance of termination criterion (default 0.001)
     */
    public double eps;    // stopping criteria
    /**
     * cost for C_SVC, EPSILON_SVR and NU_SVR
     */
    public double C;
    public int nrWeight;        // for C_SVC
    public int[] weightLabel;    // for C_SVC
    public double[] weight;        // for C_SVC
    /**
     * nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5)
     */
    public double nu;
    /**
     * the epsilon in loss function of epsilon-SVR (default 0.1)
     */
    public double p;    // for EPSILON_SVR
    /**
     * whether to use the shrinking heuristics, 0 or 1 (default 1)
     */
    public int shrinking;    // use the shrinking heuristics
    /**
     * whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0)
     */
    public int probability;

    public SVMParameter()
    {
        svmType = C_SVC;
        kernelType = RBF;
        degree = 3; // degree in kernel function
        gamma = 0;    // 1/num_features
        coef0 = 0; // coef0
        nu = 0.5; // nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5)
        cacheSize = 100; // cache memory size in MB (default 100)
        C = 1; // C of C-SVC,epsilon-SVR,nu-SVR
        eps = 1e-3; // tolerance of termination criterion (default 0.001)
        p = 0.1; // the epsilon in loss function of epsilon-SVR (default 0.1)
        shrinking = 1; // whether to use the shrinking heuristics, 0 or 1 (default 1)
        probability = 0; // whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0)
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

    public int getShrinking()
    {
        return shrinking;
    }

    public void setShrinking(int shrinking)
    {
        this.shrinking = shrinking;
    }

    public int getProbability()
    {
        return probability;
    }

    public void setProbability(int probability)
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
