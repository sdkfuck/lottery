package sdk.fuck.lottery.service;

import cn.hutool.core.util.StrUtil;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sdk.fuck.lottery.common.constants.SsqConstants;
import sdk.fuck.lottery.common.utils.LotteryUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class LotteryDL4JTrainService {
  // 日志记录器
  private static final Logger logger = LoggerFactory.getLogger(LotteryDL4JTrainService.class);

  /**
   * 训练彩票数据模型
   *
   * @param lotteryName 彩票名称
   */
  public void train(String lotteryName) {
    // 判断彩票名称是否为双色球
    if (StrUtil.equals(lotteryName, SsqConstants.NAME)) {
      logger.info("开始训练双色球...");
      int batchSize = 100; // 每个批次的数据数量
      int numRedBalls = 6; // 红球的数量
      int numBlueBall = 1; // 篮球的数量

      // 读取CSV数据
      List<Integer[]> redBallFeatureList = LotteryUtils.readCsvToIntegerListWithoutFirstAndLastLines(lotteryName);
      List<Integer[]> redBallLabelList = LotteryUtils.readCsvToIntegerListWithoutFirstTwoLines(lotteryName);
      List<Integer[]> blueBallFeatureList = LotteryUtils.readCsvToIntegerListWithoutFirstAndLastLines(lotteryName);
      List<Integer[]> blueBallLabelList = LotteryUtils.readCsvToIntegerListWithoutFirstTwoLines(lotteryName);

      int[][] redBallFeatureData = LotteryUtils.prepareRedBallData(redBallFeatureList);
      int[][] redBallLabelData = LotteryUtils.prepareRedBallData(redBallLabelList);
      int[][] blueBallFeatureData = LotteryUtils.prepareBlueBallData(blueBallFeatureList);
      int[][] blueBallLabelData = LotteryUtils.prepareBlueBallData(blueBallLabelList);

      // 数据标准化
      DataNormalization redNormalizer = new NormalizerStandardize(); // 创建红球数据标准化对象
      DataNormalization blueNormalizer = new NormalizerStandardize(); // 创建篮球数据标准化对象

      // 构建红球 LSTM 网络
      MultiLayerConfiguration redBallConf = buildRedBallLSTMConf(numRedBalls);
      MultiLayerNetwork redBallNet = new MultiLayerNetwork(redBallConf);
      redBallNet.setListeners(new ScoreIterationListener(batchSize));
      redBallNet.init(); // 初始化网络
      // 构建篮球 LSTM 网络
      MultiLayerConfiguration blueBallConf = buildBlueBallLSTMConf(numBlueBall);
      MultiLayerNetwork blueBallNet = new MultiLayerNetwork(blueBallConf);
      blueBallNet.setListeners(new ScoreIterationListener(batchSize));
      blueBallNet.init(); // 初始化网络

      // 训练红球和篮球模型
      int nEpochs = 1; // todo 训练轮数，根据你的电脑性能来改
      for (int i = 0; i < nEpochs; i++) {
        for (int j = 0; j < redBallFeatureData.length - batchSize; j++) {
          // 准备红球数据集
          DataSet redDataSet = new DataSet(
            LotteryUtils.toINDArray(redBallFeatureData, j, batchSize, numRedBalls),
            LotteryUtils.toINDArray(redBallLabelData, j, batchSize, numRedBalls)
          );
          redNormalizer.fit(redDataSet); // 数据标准化拟合
          redNormalizer.transform(redDataSet); // 转换数据
          redBallNet.fit(redDataSet); // 训练红球模型

          // 准备篮球数据集
          DataSet blueDataSet = new DataSet(
            LotteryUtils.toINDArray(blueBallFeatureData, j, batchSize, numBlueBall),
            LotteryUtils.toINDArray(blueBallLabelData, j, batchSize, numBlueBall)
          );
          blueNormalizer.fit(blueDataSet); // 数据标准化拟合
          blueNormalizer.transform(blueDataSet); // 转换数据
          blueBallNet.fit(blueDataSet); // 训练篮球模型
        }
      }
      logger.info("训练完毕...");
      // 保存模型
      try {
        redBallNet.save(new File(LotteryUtils.getLotteryRedModelPath(lotteryName)));
        blueBallNet.save(new File(LotteryUtils.getLotteryBlueModelPath(lotteryName)));
        logger.info("模型保存完毕...");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * 构建 LSTM 配置
   *
   * @param numBalls 球的数量
   * @return MultiLayerConfiguration
   */
  private static MultiLayerConfiguration buildLSTMConf(int numBalls) {
    // todo 创建LSTM网络配置，根据你的电脑性能来
    return new NeuralNetConfiguration.Builder()
      .seed(123) // 设置种子
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT) // 设置优化算法
      .updater(new Adam(0.005)) // 设置优化器
      .list() // 构建网络层
      .layer(new LSTM.Builder()
               .activation(Activation.TANH) // 设置激活函数
               .nIn(numBalls) // 输入节点数
               .nOut(100) // 输出节点数
               .build())
      .layer(new LSTM.Builder()
               .activation(Activation.TANH) // 设置激活函数
               .nIn(100) // 输入节点数
               .nOut(100) // 输出节点数
               .build())
      .layer(new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
               .activation(Activation.IDENTITY) // 设置激活函数
               .nIn(100) // 输入节点数
               .nOut(numBalls) // 输出节点数
               .build())
      .build();
  }

  /**
   * 构建篮球 LSTM 配置
   *
   * @param numBlueBall 篮球数量
   * @return MultiLayerConfiguration
   */
  private MultiLayerConfiguration buildBlueBallLSTMConf(int numBlueBall) {
    return buildLSTMConf(numBlueBall);
  }

  /**
   * 构建红球 LSTM 配置
   *
   * @param numRedBalls 红球数量
   * @return MultiLayerConfiguration
   */
  private MultiLayerConfiguration buildRedBallLSTMConf(int numRedBalls) {
    return buildLSTMConf(numRedBalls);
  }

  /**
   * 评价彩票数据模型
   *
   * @param lotteryName 彩票名称
   */
  public void evaluate(String lotteryName) {
    // 判断彩票名称是否为双色球
    if (StrUtil.equals(lotteryName, SsqConstants.NAME)) {
      logger.info("开始评价双色球模型...");
      int numRedBalls = 6; // 红球的数量
      int numBlueBall = 1; // 篮球的数量

      // 加载已保存的模型
      MultiLayerNetwork redBallNet;
      MultiLayerNetwork blueBallNet;
      try {
        redBallNet = MultiLayerNetwork.load(new File(LotteryUtils.getLotteryRedModelPath(lotteryName)), true);
        blueBallNet = MultiLayerNetwork.load(new File(LotteryUtils.getLotteryBlueModelPath(lotteryName)), true);
      } catch (IOException e) {
        logger.error("加载模型失败: {}", e.getMessage());
        return;
      }

      // 读取CSV数据
      List<Integer[]> redBallFeatureList = LotteryUtils.readCsvToIntegerListWithoutFirstAndLastLines(lotteryName);
      List<Integer[]> redBallLabelList = LotteryUtils.readCsvToIntegerListWithoutFirstTwoLines(lotteryName);
      List<Integer[]> blueBallFeatureList = LotteryUtils.readCsvToIntegerListWithoutFirstAndLastLines(lotteryName);
      List<Integer[]> blueBallLabelList = LotteryUtils.readCsvToIntegerListWithoutFirstTwoLines(lotteryName);

      int[][] redBallFeatureData = LotteryUtils.prepareRedBallData(redBallFeatureList);
      int[][] redBallLabelData = LotteryUtils.prepareRedBallData(redBallLabelList);
      int[][] blueBallFeatureData = LotteryUtils.prepareBlueBallData(blueBallFeatureList);
      int[][] blueBallLabelData = LotteryUtils.prepareBlueBallData(blueBallLabelList);

      // 数据标准化
      DataNormalization redNormalizer = new NormalizerStandardize(); // 创建红球数据标准化对象
      DataNormalization blueNormalizer = new NormalizerStandardize(); // 创建篮球数据标准化对象

      // 对测试数据进行标准化
      DataSet redTestDataSet = new DataSet(
        LotteryUtils.toINDArray(redBallFeatureData, 0, redBallFeatureData.length, numRedBalls),
        LotteryUtils.toINDArray(redBallLabelData, 0, redBallLabelData.length, numRedBalls)
      );
      redNormalizer.fit(redTestDataSet);
      redNormalizer.transform(redTestDataSet);
      DataSet blueTestDataSet = new DataSet(
        LotteryUtils.toINDArray(blueBallFeatureData, 0, blueBallFeatureData.length, numBlueBall),
        LotteryUtils.toINDArray(blueBallLabelData, 0, blueBallLabelData.length, numBlueBall)
      );
      blueNormalizer.fit(blueTestDataSet);
      blueNormalizer.transform(blueTestDataSet);

      // 使用模型进行预测
      INDArray redPredictions = redBallNet.output(redTestDataSet.getFeatures());
      INDArray bluePredictions = blueBallNet.output(blueTestDataSet.getFeatures());

      // 将预测结果转换为实际的彩票号码
      int[][] redPredictionsArray = new int[(int) redPredictions.size(0)][numRedBalls];
      int[][] bluePredictionsArray = new int[(int) bluePredictions.size(0)][numBlueBall];

      for (int i = 0; i < redPredictions.size(0); i++) {
        for (int j = 0; j < numRedBalls; j++) {
          redPredictionsArray[i][j] = (int) Math.round(redPredictions.getDouble(i, j));
        }
      }
      for (int i = 0; i < bluePredictions.size(0); i++) {
        for (int j = 0; j < numBlueBall; j++) {
          bluePredictionsArray[i][j] = (int) Math.round(bluePredictions.getDouble(i, j));
        }
      }

      // 打印预测的彩票号码和实际的彩票号码
      logger.info("红球预测结果:");
      printPredictions(redPredictionsArray, redBallLabelData);
      logger.info("篮球预测结果:");
      printPredictions(bluePredictionsArray, blueBallLabelData);

      // 计算准确率等指标
      int correctRedPredictions = 0;
      int correctBluePredictions = 0;
      for (int i = 0; i < redBallLabelData.length; i++) {
        boolean redCorrect = true;
        boolean blueCorrect = true;
        for (int j = 0; j < numRedBalls; j++) {
          if (redPredictionsArray[i][j] != redBallLabelData[i][j]) {
            redCorrect = false;
            break;
          }
        }
        for (int j = 0; j < numBlueBall; j++) {
          if (bluePredictionsArray[i][j] != blueBallLabelData[i][j]) {
            blueCorrect = false;
            break;
          }
        }
        if (redCorrect) correctRedPredictions++;
        if (blueCorrect) correctBluePredictions++;
      }

      double redAccuracy = (double) correctRedPredictions / (redBallLabelData.length);
      double blueAccuracy = (double) correctBluePredictions / (blueBallLabelData.length);

      logger.info("红球预测准确率: {}", redAccuracy);
      logger.info("篮球预测准确率: {}", blueAccuracy);
    }
  }

  /**
   * 打印预测结果
   *
   * @param predictions 预测数组
   * @param actual      实际数组
   */
  private void printPredictions(int[][] predictions, int[][] actual) {
    for (int i = 0; i < predictions.length; i++) {
      logger.info("预测: {}, 实际: {}", Arrays.toString(predictions[i]), Arrays.toString(actual[i]));
    }
  }

}
