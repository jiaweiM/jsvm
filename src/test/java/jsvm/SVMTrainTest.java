package jsvm;


import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 28 Oct 2019, 10:56 PM
 */
class SVMTrainTest
{
    private final String data1 = "D:\\data\\datasets\\train.1";
    private final String test1 = "D:\\data\\datasets\\test.1";
    private final String data2 = "D:\\data\\datasets\\train.2";
    private final String data3 = "D:\\data\\datasets\\train.3";
    private final String data4 = "D:\\data\\datasets\\svmguide4";
    private final String test4 = "D:\\data\\datasets\\svmguide4.t";


    @Test
    void testTrain1() throws IOException
    {
        testDirect(data1, test1);
    }

    @Test
    void testScaleTrain1() throws IOException
    {
        testScaleFirst(data1, test1);
    }

    @Test
    void testTrain1Grid(){

    }

    @Test
    void scaleGlycan()
    {
        SVMScale scale = new SVMScale();
        scale.setXLower(0);
        scale.scaleTrain("Z:\\MaoJiawei\\o-glycan\\liuluyao\\A-24-raw\\train\\train.csv");
    }

    @Test
    void test4() throws IOException, ExecutionException, InterruptedException
    {
        SVMScale scale = new SVMScale();
        scale.scaleTrain(data4);

        SVMParameter parameter = new SVMParameter();
//        GridSearch search = new GridSearch(data4 + ".scale", parameter);
//        search.grid();
        parameter.setC(2048);
        parameter.setGamma(0.03125);

        scale.scaleTest(test4, data4 + ".range");
//        scale.scaleTrain(test4);
        SVMTrain train = new SVMTrain(data4 + ".scale", parameter);
        train.train();
        SVMPredict.predict(test4 + ".scale", data4 + ".scale.model");
    }

    private void testDirect(String file, String test) throws IOException
    {
        SVMTrain train = new SVMTrain(file);
        train.train();
        SVMPredict.predict(test, file + ".model");
    }

    private void testScaleFirst(String file, String test) throws IOException
    {
        SVMScale svmScale = new SVMScale();
        svmScale.scaleTrain(file);
        svmScale.scaleTest(test, file + ".range");
        SVMTrain train = new SVMTrain(file + ".scale");
        train.train();

        SVMPredict.predict(test + ".scale", file + ".scale.model");
    }

    @Test
    void testScale() throws IOException
    {
        String trainData = "D:\\data\\datasets\\train.1";
        String testData = "D:\\data\\datasets\\test.1";
        String trainDataScale = "D:\\data\\datasets\\train.1.scale";
        String trainDataRange = "D:\\data\\datasets\\train.1.range";
        String trainDataScaleModel = "D:\\data\\datasets\\train.1.scale.model";
        String testDataScale = "D:\\data\\datasets\\test.1.scale";

        SVMScale scale = new SVMScale();
        scale.scaleTrain(trainData);
        scale.scaleTest(testData, trainDataRange);

        SVMParameter parameter = new SVMParameter();
//        parameter.setC(Math.pow(2, 7));
//        parameter.setGamma(Math.pow(2, -1));
//        SVMTrain train = new SVMTrain(trainDataScale, parameter, true, 5);
//        Pair<Double, Double> train2 = train.train();
//        System.out.println(train2.getKey());
        SVMTrain train = new SVMTrain(trainDataScale);
        train.train();
        SVMPredict.predict(testDataScale, trainDataScaleModel, false);

    }

    @Test
    void test2() throws IOException
    {
        String trainData = "D:\\data\\datasets\\train.2";
        String trainscale = "D:\\data\\datasets\\train.2.scale";

        SVMScale scale = new SVMScale();
        scale.scaleTrain(trainData);

        SVMTrain train = new SVMTrain(trainscale);
        train.setCrossValidation(true);
        train.setNrFold(5);
        Pair<Double, Double> result = train.train();
        System.out.println(result.getKey());
    }

    @Test
    void test3() throws IOException
    {
        String trainData = "D:\\data\\datasets\\train.3";
        String trainDataScale = "D:\\data\\datasets\\train.3.scale";
        String trainDataScaleModel = "D:\\data\\datasets\\train.3.scale.model";
        String trainDataRange = "D:\\data\\datasets\\train.3.range";
        String trainDataModel = "D:\\data\\datasets\\train.3.model";
        String testData = "D:\\data\\datasets\\test.3";
        String testDataScale = "D:\\data\\datasets\\test.3.scale";

        SVMScale svmScale = new SVMScale();
        svmScale.scaleTrain(trainData);
        svmScale.scaleTest(testData, trainDataRange);

        trainData = trainDataScale;
        SVMTrain train = new SVMTrain(trainData);
        System.out.println(Math.pow(2, 11));
        System.out.println(Math.pow(2, -5));
        train.setC(Math.pow(2, 11));
        train.setG(Math.pow(2, -5));
        train.train();
        testData = testDataScale;
        trainDataModel = trainDataScaleModel;
        SVMPredict.predict(testData, trainDataModel, false);

    }

    @Test
    void testCross() throws IOException
    {
        String data = "D:\\data\\datasets\\train.3.scale";
        SVMParameter parameter = new SVMParameter();
        parameter.setC(Math.pow(2, 7));
        parameter.setGamma(Math.pow(2, -3));
        SVMTrain train = new SVMTrain(data, parameter, true, 5);
        train.train();
    }
}