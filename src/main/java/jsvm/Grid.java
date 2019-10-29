package jsvm;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 29 Oct 2019, 1:16 PM
 */
public class Grid implements Runnable
{
    /**
     * the real C value is 2^c
     */
    private double cBegin = -5;
    private double cEnd = 15;
    private double cStep = 2;
    private boolean useC = true;

    /**
     * the real G value is 2^g
     */
    private double gBegin = 3;
    private double gEnd = -15;
    private double gStep = -2;
    private boolean useG = true;

    private int nrFold = 5;

    private String outPath;
    private String dataPath;

    public Grid(String dataPath)
    {
        this.dataPath = dataPath;
        this.outPath = dataPath + ".out";
    }

    public void grid()
    {

    }

    @Override
    public void run()
    {

    }
}
