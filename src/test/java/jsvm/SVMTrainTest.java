package jsvm;

import org.testng.annotations.Test;

import java.io.IOException;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 28 Oct 2019, 10:56 PM
 */
public class SVMTrainTest
{
    String train1 = "D:\\data\\datasets\\train.1";
    String test1 = "D:\\data\\datasets\\test.1";

    @Test
    public void test() throws IOException
    {
        SVMTrain train = new SVMTrain(train1);
        train.train();
        SVMPredict.predict(test1, train.getModelFileName(), false);

//        new SVMTrain(args);
    }

    @Test
    public void testScale() throws IOException
    {
        String trainData = "D:\\data\\datasets\\train.1";
        String testData = "D:\\data\\datasets\\test.1";
        String trainDataScale = "D:\\data\\datasets\\train.1.scale";
        String trainDataRange = "D:\\data\\datasets\\train.1.range";
        String trainDataScaleModel = "D:\\data\\datasets\\train.1.scale.model";
        String testDataScale = "D:\\data\\datasets\\test.1.scale";

        SVMScale scale = new SVMScale();
        scale.scaleTrain(trainData, trainDataRange, trainDataScale);
        scale.scaleTest(testData, trainDataRange, testDataScale);

        SVMTrain train = new SVMTrain(trainDataScale);
        train.train();
        SVMPredict.predict(testDataScale, trainDataScaleModel, false);

    }
}