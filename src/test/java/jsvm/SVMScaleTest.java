package jsvm;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 28 Oct 2019, 10:35 PM
 */
public class SVMScaleTest
{
    @Test
    public void test()
    {
        String file = "D:\\data\\datasets\\train.1";
        SVMScale svmScale = new SVMScale();
//        svmScale.scaleTrain(file);
        svmScale.scaleTest("D:\\data\\datasets\\test.1", file + ".range");
    }
}