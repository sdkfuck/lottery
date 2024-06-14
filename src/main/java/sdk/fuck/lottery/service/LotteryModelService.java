package sdk.fuck.lottery.service;

import jakarta.annotation.Resource;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.stereotype.Service;
import sdk.fuck.lottery.common.constants.SsqConstants;
import sdk.fuck.lottery.common.utils.LotteryUtils;
import sdk.fuck.lottery.domain.dataobject.DltDO;
import sdk.fuck.lottery.domain.dataobject.SsqDO;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 彩票模型服务类，用于创建、训练、保存、加载和预测彩票号码
 */
@Service
public class LotteryModelService {

  @Resource
  private LotteryDataProcessorService dataProcessorService;

  /**
   * 创建并训练模型
   *
   * @param lotteryName 彩票名称
   * @param epochs      训练的轮数
   */
  public void createAndTrainModel(String lotteryName, int epochs) {
    // 获取数据集
    DataSet dataSet = dataProcessorService.createDataSet(lotteryName);
    INDArray features = dataSet.getFeatures();
    INDArray labels = dataSet.getLabels();
    DataSetIterator iterator = new ListDataSetIterator<>(Collections.singletonList(dataSet), features.rows());

    MultiLayerNetwork model;
    File modelFile = new File(LotteryUtils.getLotteryModelPath(lotteryName));

    if (modelFile.exists()) {
      // 如果模型存在，则加载模型
      model = loadModel(lotteryName);
    } else {
      // 定义神经网络配置
      MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
        .weightInit(WeightInit.XAVIER)
        .updater(new Adam())
        .list()
        .layer(new DenseLayer.Builder()
                 .nIn(features.columns())  // 输入层
                 .nOut(128)
                 .activation(Activation.RELU)
                 .build())
        .layer(new DenseLayer.Builder()
                 .nOut(64)
                 .activation(Activation.RELU)
                 .build())
        .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)  // 输出层
                                                                        .activation(Activation.IDENTITY)
                                                                        .nOut(labels.columns())
                                                                        .build())
        .build();

      // 初始化模型
      model = new MultiLayerNetwork(conf);
      model.init();
    }

    // 每100次 打印迭代的分数
    model.setListeners(new ScoreIterationListener(100));

    // 训练模型
    for (int i = 0; i < epochs; i++) {
      iterator.reset();
      model.fit(iterator);
    }

    // 保存模型
    saveModel(model, lotteryName);
  }

  /**
   * 保存模型到文件
   *
   * @param model       模型
   * @param lotteryName 彩票名称
   */
  private void saveModel(MultiLayerNetwork model, String lotteryName) {
    try {
      ModelSerializer.writeModel(model, LotteryUtils.getLotteryModelPath(lotteryName), true);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 加载模型
   *
   * @param lotteryName 彩票名称
   * @return 加载的模型
   */
  public MultiLayerNetwork loadModel(String lotteryName) {
    try {
      return ModelSerializer.restoreMultiLayerNetwork(new File(LotteryUtils.getLotteryModelPath(lotteryName)));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 评估模型的准确率和中奖概率
   *
   * @param lotteryName 彩票名称
   * @return 模型的评估结果，包括中奖概率
   */
  public String evaluateModel(String lotteryName) {
    // 获取数据集
    DataSet dataSet = dataProcessorService.createDataSet(lotteryName);
    INDArray features = dataSet.getFeatures();
    INDArray labels = dataSet.getLabels();
    MultiLayerNetwork model = loadModel(lotteryName);

    int totalSamples = features.rows();
    int firstPrizeCount = 0;
    int secondPrizeCount = 0;
    int thirdPrizeCount = 0;
    int fourthPrizeCount = 0;
    int fifthPrizeCount = 0;
    int sixthPrizeCount = 0;

    // 遍历数据集中的每一行
    for (int i = 0; i < totalSamples; i++) {
      int[] predictedNumbers;
      int[] actualNumbers;
      try (INDArray input = features.getRow(i).reshape(1, features.columns());
           INDArray actualOutput = labels.getRow(i).reshape(1, labels.columns())) {

        // 进行预测
        INDArray predictedOutput = model.output(input);

        // 将预测结果和实际结果转换为整数
        predictedNumbers = predictedOutput.toIntVector();
        actualNumbers = actualOutput.toIntVector();
      }

      // 评估双色球
      Set<Integer> predictedRed = new HashSet<>();
      Set<Integer> actualRed = new HashSet<>();
      if (lotteryName.equals(SsqConstants.NAME)) {

        for (int j = 0; j < 6; j++) {
          predictedRed.add(predictedNumbers[j]);
          actualRed.add(actualNumbers[j]);
        }
        int predictedBlue = predictedNumbers[6];
        int actualBlue = actualNumbers[6];

        int redMatchCount = 0;
        for (int number : predictedRed) {
          if (actualRed.contains(number)) {
            redMatchCount++;
          }
        }
        boolean blueMatch = predictedBlue == actualBlue;

        if (redMatchCount == 6 && blueMatch) {
          firstPrizeCount++;
        } else if (redMatchCount == 6) {
          secondPrizeCount++;
        } else if (redMatchCount == 5 && blueMatch) {
          thirdPrizeCount++;
        } else if (redMatchCount == 5 || (redMatchCount == 4 && blueMatch)) {
          fourthPrizeCount++;
        } else if ((redMatchCount == 4) || (redMatchCount == 3 && blueMatch)) {
          fifthPrizeCount++;
        } else if (blueMatch) {
          sixthPrizeCount++;
        }
      }
      // 评估大乐透
      else {
        Set<Integer> predictedBlue = new HashSet<>();
        Set<Integer> actualBlue = new HashSet<>();

        for (int j = 0; j < 5; j++) {
          predictedRed.add(predictedNumbers[j]);
          actualRed.add(actualNumbers[j]);
        }
        for (int j = 5; j < 7; j++) {
          predictedBlue.add(predictedNumbers[j]);
          actualBlue.add(actualNumbers[j]);
        }

        int redMatchCount = 0;
        int blueMatchCount = 0;

        for (int number : predictedRed) {
          if (actualRed.contains(number)) {
            redMatchCount++;
          }
        }
        for (int number : predictedBlue) {
          if (actualBlue.contains(number)) {
            blueMatchCount++;
          }
        }

        if (redMatchCount == 5 && blueMatchCount == 2) {
          firstPrizeCount++;
        } else if (redMatchCount == 5 && blueMatchCount == 1) {
          secondPrizeCount++;
        } else if (redMatchCount == 5 || (redMatchCount == 4 && blueMatchCount == 2)) {
          thirdPrizeCount++;
        } else if ((redMatchCount == 4 && blueMatchCount == 1) || (redMatchCount == 3 && blueMatchCount == 2)) {
          fourthPrizeCount++;
        } else if ((redMatchCount == 4) || (redMatchCount == 3 && blueMatchCount == 1) || (redMatchCount ==
                                                                                           2 && blueMatchCount == 2)) {
          fifthPrizeCount++;
        } else if ((redMatchCount == 3) || (redMatchCount == 1 && blueMatchCount == 2) || (redMatchCount == 2 && blueMatchCount == 1)) {
          sixthPrizeCount++;
        }
      }
    }

    // 计算中奖概率
    double totalPrizes = firstPrizeCount + secondPrizeCount + thirdPrizeCount + fourthPrizeCount + fifthPrizeCount + sixthPrizeCount;
    double firstPrizeProbability = calculateProbability(firstPrizeCount, totalPrizes);
    double secondPrizeProbability = calculateProbability(secondPrizeCount, totalPrizes);
    double thirdPrizeProbability = calculateProbability(thirdPrizeCount, totalPrizes);
    double fourthPrizeProbability = calculateProbability(fourthPrizeCount, totalPrizes);
    double fifthPrizeProbability = calculateProbability(fifthPrizeCount, totalPrizes);
    double sixthPrizeProbability = calculateProbability(sixthPrizeCount, totalPrizes);

    // 生成评估结果
    return String.format("""
                         评估结果：
                         一等奖：%d次，概率：%.2f%%
                         二等奖：%d次，概率：%.2f%%
                         三等奖：%d次，概率：%.2f%%
                         四等奖：%d次，概率：%.2f%%
                         五等奖：%d次，概率：%.2f%%
                         六等奖：%d次，概率：%.2f%%
                         总样本数：%d
                         """,
                         firstPrizeCount, firstPrizeProbability, secondPrizeCount, secondPrizeProbability,
                         thirdPrizeCount, thirdPrizeProbability, fourthPrizeCount, fourthPrizeProbability,
                         fifthPrizeCount, fifthPrizeProbability, sixthPrizeCount, sixthPrizeProbability, totalSamples);
  }

  /**
   * 计算中奖概率
   *
   * @param prizeCount  当前奖级的中奖次数
   * @param totalPrizes 总中奖次数
   * @return 中奖概率
   */
  private double calculateProbability(int prizeCount, double totalPrizes) {
    return totalPrizes == 0 ? 0 : prizeCount / totalPrizes * 100;
  }

  /**
   * 预测下一期的号码
   *
   * @param lotteryName 彩票名称
   * @return 模型预测的下一期号码
   */
  public String predictNextNumbers(String lotteryName) {
    // 创建输入数据
    INDArray input = Nd4j.zeros(1, 7);
    if (lotteryName.equals(SsqConstants.NAME)) {
      // 获取双色球最后一期的数据
      List<SsqDO> ssqDOS = LotteryUtils.readAndParseSsqCsv();
      SsqDO lastRecord = ssqDOS.get(ssqDOS.size() - 1);
      input.putRow(0, Nd4j.create(new double[]{
        Double.parseDouble(lastRecord.getRed01()),
        Double.parseDouble(lastRecord.getRed02()),
        Double.parseDouble(lastRecord.getRed03()),
        Double.parseDouble(lastRecord.getRed04()),
        Double.parseDouble(lastRecord.getRed05()),
        Double.parseDouble(lastRecord.getRed06()),
        Double.parseDouble(lastRecord.getBlue())
      }));
    } else {
      // 获取大乐透最后一期的数据
      List<DltDO> dltDOS = LotteryUtils.readAndParseDltCsv();
      DltDO lastRecord = dltDOS.get(dltDOS.size() - 1);
      input.putRow(0, Nd4j.create(new double[]{
        Double.parseDouble(lastRecord.getRed01()),
        Double.parseDouble(lastRecord.getRed02()),
        Double.parseDouble(lastRecord.getRed03()),
        Double.parseDouble(lastRecord.getRed04()),
        Double.parseDouble(lastRecord.getRed05()),
        Double.parseDouble(lastRecord.getBlue01()),
        Double.parseDouble(lastRecord.getBlue02())
      }));
    }
    // 加载模型
    MultiLayerNetwork model = loadModel(lotteryName);
    // 进行预测
    INDArray output = model.output(input);
    // 将预测结果转换为整数并格式化为字符串
    int[] predictedNumbers = output.toIntVector();
    if (lotteryName.equals(SsqConstants.NAME)) {
      // 双色球结果格式化
      return String.format("双色球：红球 %d %d %d %d %d %d 篮球 %d",
                           predictedNumbers[0], predictedNumbers[1], predictedNumbers[2],
                           predictedNumbers[3], predictedNumbers[4], predictedNumbers[5],
                           predictedNumbers[6]);
    } else {
      // 大乐透结果格式化
      return String.format("大乐透：红球 %d %d %d %d %d 蓝球 %d %d",
                           predictedNumbers[0], predictedNumbers[1], predictedNumbers[2],
                           predictedNumbers[3], predictedNumbers[4],
                           predictedNumbers[5], predictedNumbers[6]);
    }
  }

}
