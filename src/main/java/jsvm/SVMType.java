package jsvm;

/**
 * Type of SVM.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 28 Oct 2019, 10:56 AM
 */
public enum SVMType
{
    /**
     * multi-class classification
     */
    C_SVC("c_svc"),
    /**
     * multi-class classification
     */
    NU_SVC("nu_svc"),
    ONE_CLASS("one_class"),
    /**
     * regression
     */
    EPSILON_SVR("epsilon_svr"),
    /**
     * regression
     */
    NU_SVR("nu_svr");

    private String name;

    SVMType(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public static SVMType ofIndex(int index)
    {
        return SVMType.values()[index];
    }
}
