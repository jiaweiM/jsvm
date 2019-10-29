package jsvm;

/**
 * This class store a value.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 28 Oct 2019, 10:48 AM
 */
public class SVMNode implements java.io.Serializable
{
    public int index;
    public double value;

    public SVMNode() { }

    /**
     * Constructor.
     *
     * @param index index of the node
     * @param value value of the node.
     */
    public SVMNode(int index, double value)
    {
        this.index = index;
        this.value = value;
    }
}
