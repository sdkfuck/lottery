package sdk.fuck.lottery.service;

import lombok.extern.slf4j.Slf4j;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.stereotype.Service;
import sdk.fuck.lottery.common.utils.LotteryUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class LotteryLSTMService {

  // 单个训练批次数量
  public int batchSize = 3;
  // 特征数量
  public int numPossibleLabels = 7;
  // 特征开始的索引
  public int labelIndex = 7;
  // 打印分数的频率
  public int printIterations = 1;

  /**
   * 训练彩票模型
   *
   * @param lotteryName 彩票名称
   */
  public void train(String lotteryName, int numEpochs) {
    log.info("开始训练{}模型", lotteryName);
    String trainCsvPath = LotteryUtils.getLotteryDataTrainCsvPath(lotteryName);
    buildModel(lotteryName, trainCsvPath, numEpochs);
    log.info("训练完成");
  }

  /**
   * 构建深度学习模型
   *
   * @param lotteryName  彩票名称
   * @param trainCsvPath 训练数据集CSV文件路径
   * @param numEpochs    训练轮数
   */
  private void buildModel(String lotteryName, String trainCsvPath, int numEpochs) {
    // 加载训练数据
    DataSetIterator trainData = loadData(trainCsvPath, batchSize, numPossibleLabels, labelIndex);

    // 创建 LSTM 网络配置
    MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
      .seed(12345)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT) // 优化算法
      .updater(new Adam(0.005)) // 更新器
      .weightInit(WeightInit.XAVIER) // 权重初始化
      .list()
      .layer(new LSTM.Builder() // 添加 LSTM 层
                                .nIn(numPossibleLabels)
                                .nOut(100)
                                .activation(Activation.TANH) // 激活函数
                                .build())
      .layer(new LSTM.Builder() // 添加 LSTM 层
                                .nIn(100)
                                .nOut(100)
                                .activation(Activation.TANH) // 激活函数
                                .build())
      .layer(new RnnOutputLayer.Builder() // 添加 RNN 输出层
                                          .nIn(100)
                                          .nOut(numPossibleLabels)
                                          .activation(Activation.IDENTITY) // 激活函数
                                          .lossFunction(LossFunctions.LossFunction.MSE) // 损失函数
                                          .build())
      .backpropType(BackpropType.Standard) // 反向传播类型
      .build();

    // 创建模型
    MultiLayerNetwork model = new MultiLayerNetwork(conf);
    model.init();
    model.setListeners(new ScoreIterationListener(printIterations));

    // 打印输出形状以调试
    if (trainData.hasNext()) {
      DataSet dataSet = trainData.next();
      INDArray features = dataSet.getFeatures();
      INDArray labels = dataSet.getLabels();
      INDArray preOutput = model.output(features, false);

      log.info("Features shape: {}", Arrays.toString(features.shape()));
      log.info("Labels shape: {}", Arrays.toString(labels.shape()));
      log.info("PreOutput shape: {}", Arrays.toString(preOutput.shape()));
    }

    // 训练模型
    model.fit(trainData, numEpochs);
    trainData.reset();

    try {
      model.save(new File(LotteryUtils.getLotteryModelPath(lotteryName))); // 保存模型
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 加载训练数据
   *
   * @param filePath          训练数据集CSV文件路径
   * @param miniBatchSize     单个训练批次数量
   * @param numPossibleLabels 特征数量
   * @param labelIndex        特征开始的索引
   * @return DataSetIterator
   */
  private static DataSetIterator loadData(String filePath, int miniBatchSize, int numPossibleLabels, int labelIndex) {
    CSVSequenceRecordReader reader = new CSVSequenceRecordReader(1, ","); // 创建 CSVSequenceRecordReader
    try {
      reader.initialize(new FileSplit(new File(filePath))); // 初始化数据读取器
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
    return new SequenceRecordReaderDataSetIterator(reader, miniBatchSize, numPossibleLabels, labelIndex, true);
  }

  /**
   * 评估彩票模型
   *
   * @param lotteryName 彩票名称
   */
  public void evaluate(String lotteryName) {
    log.info("开始评估{}模型", lotteryName);

    // 加载测试数据
    String testCsvPath = LotteryUtils.getLotteryDataTestCsvPath(lotteryName);
    DataSetIterator testData = loadData(testCsvPath, batchSize, numPossibleLabels, labelIndex);

    // 加载模型
    MultiLayerNetwork model;
    try {
      model = ModelSerializer.restoreMultiLayerNetwork(new File(LotteryUtils.getLotteryModelPath(lotteryName)));
    } catch (IOException e) {
      log.error("加载模型失败", e);
      return;
    }

    // 记录红球预测正确数量和总数量
    int totalRedCorrectCount = 0;
    int totalRedCount = 0;

    // 记录篮球预测正确数量和总数量
    int blueCorrectCount = 0;
    int blueTotalCount = 0;

    // 逐个样本评估模型
    while (testData.hasNext()) {
      DataSet dataSet = testData.next();
      INDArray features = dataSet.getFeatures();
      INDArray labels = dataSet.getLabels();

      // 使用模型进行预测
      INDArray predicted = model.output(features, false);

      // 检查红球预测结果与真实结果
      List<Integer> actualReds = new ArrayList<>();
      List<Integer> predictedReds = new ArrayList<>();
      for (int i = 0; i < 6; i++) {
        actualReds.add((int) labels.getDouble(0, i));
        predictedReds.add((int) predicted.getDouble(0, i));
      }

      // 计算红球预测正确的数量
      int redCorrectCount = 0;
      for (int predictedRed : predictedReds) {
        if (actualReds.contains(predictedRed)) {
          redCorrectCount++;
        }
      }

      totalRedCorrectCount += redCorrectCount;
      totalRedCount += actualReds.size();

      // 检查篮球预测结果与真实结果
      double actualBlue = labels.getDouble(0, 6);
      double predictedBlue = predicted.getDouble(0, 6);

      // 计算篮球预测准确率
      if (Math.abs(actualBlue - predictedBlue) < 0.5) {
        blueCorrectCount++;
      }
      blueTotalCount++;
    }

    // 计算红球和篮球的预测准确率
    double redAccuracy = (double) totalRedCorrectCount / totalRedCount * 100;
    double blueAccuracy = (double) blueCorrectCount / blueTotalCount * 100;
    log.info(String.format("红球预测准确率: %.2f%%", redAccuracy));
    log.info(String.format("篮球预测准确率: %.2f%%", blueAccuracy));
    log.info("评估完成");
  }

  /**
   * 预测下一期彩票号码
   *
   * @param lotteryName 彩票名称
   */
  public void predict(String lotteryName) {
    log.info("开始预测{}下一期号码", lotteryName);

    // 加载彩票模型
    MultiLayerNetwork model;
    try {
      model = ModelSerializer.restoreMultiLayerNetwork(new File(LotteryUtils.getLotteryModelPath(lotteryName)));
    } catch (IOException e) {
      log.error("加载模型失败", e);
      return;
    }

    String trainCsvPath = LotteryUtils.getLotteryDataTrainCsvPath(lotteryName);
    DataSet dataSet = loadLastDataSet(trainCsvPath);
    INDArray inputData = dataSet.getLabels();
    INDArray outputData = model.output(inputData, false);

    String[] inputStrings = formatIntArray(inputData);
    String[] predictionStrings = formatIntArray(outputData);

    log.info("输入：\n红球：{} 篮球：{}", inputStrings[0], inputStrings[1]);
    log.info("预测结果：\n红球：{} 篮球：{}", predictionStrings[0], predictionStrings[1]);

    log.info("预测完成");
  }

  // 格式化INDArray为String数组
  private String[] formatIntArray(INDArray array) {
    int[] intArray = new int[7];
    for (int j = 0; j < 7; j++) {
      intArray[j] = array.getInt(0, j);
    }
    String[] strings = new String[3];
    strings[0] = String.format("%02d,%02d,%02d,%02d,%02d,%02d", intArray[0], intArray[1], intArray[2], intArray[3], intArray[4], intArray[5]);
    strings[1] = String.format("%02d", intArray[6]);
    return strings;
  }

  // 使用SequenceRecordReaderDataSetIterator加载最后一行的数据
  private DataSet loadLastDataSet(String csvFilePath) {
    CSVSequenceRecordReader reader = new CSVSequenceRecordReader(1, ",");
    try {
      reader.initialize(new FileSplit(new File(csvFilePath)));
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }

    DataSetIterator iterator = new SequenceRecordReaderDataSetIterator(reader, 1, numPossibleLabels, labelIndex, true);
    DataNormalization normalizer = new NormalizerStandardize();
    normalizer.fit(iterator);
    iterator.setPreProcessor(normalizer);

    // 获取最后一个DataSet
    DataSet lastDataSet = null;
    while (iterator.hasNext()) {
      lastDataSet = iterator.next();
    }
    return lastDataSet;
  }

}
