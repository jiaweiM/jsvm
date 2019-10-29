package jsvm;

/**
 * Type of kernel function.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 28 Oct 2019, 1:49 PM
 */
public enum KernelType
{
    /**
     * linear, u'*v
     */
    LINEAR("linear"),
    /**
     * polynomial, (gamma*u'*v + coef0)^degree
     */
    POLY("polynomial"),
    /**
     * redial basis function: exp(-gamma*|u-v|^2)
     */
    RBF("rbf"),
    /**
     * sigmoid: tanh(gamma*u'*v + coef0)
     */
    SIGMOID("sigmoid"),
    /**
     * precomputed kernel (kernel values in training_set_file)
     */
    PRECOMPUTED("precomputed");

    private String name;

    KernelType(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public static KernelType ofIndex(int index)
    {
        return KernelType.values()[index];
    }
}
