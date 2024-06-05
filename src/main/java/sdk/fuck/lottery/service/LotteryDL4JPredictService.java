package sdk.fuck.lottery.service;

import cn.hutool.core.util.StrUtil;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sdk.fuck.lottery.common.constants.SsqConstants;
import sdk.fuck.lottery.common.utils.LotteryUtils;
import sdk.fuck.lottery.domain.dataobject.SsqDO;

import java.io.File;
import java.io.IOException;

@Service
public class LotteryDL4JPredictService {
  private static final Logger logger = LoggerFactory.getLogger(LotteryDL4JPredictService.class);

  /**
   * 预测彩票数据
   *
   * @param lotteryName 彩票名称
   * @param inputData   输入数据
   * @return 预测结果
   */
  public SsqDO predict(String lotteryName, SsqDO inputData) {
    // 判断彩票名称是否为双色球
    if (StrUtil.equals(lotteryName, SsqConstants.NAME)) {
      logger.info("开始预测双色球号码...");
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
        return null;
      }

      // 准备输入数据
      int[][] redBallInputData = new int[1][numRedBalls];
      int[][] blueBallInputData = new int[1][numBlueBall];

      redBallInputData[0][0] = Integer.parseInt(inputData.getRed01());
      redBallInputData[0][1] = Integer.parseInt(inputData.getRed02());
      redBallInputData[0][2] = Integer.parseInt(inputData.getRed03());
      redBallInputData[0][3] = Integer.parseInt(inputData.getRed04());
      redBallInputData[0][4] = Integer.parseInt(inputData.getRed05());
      redBallInputData[0][5] = Integer.parseInt(inputData.getRed06());
      blueBallInputData[0][0] = Integer.parseInt(inputData.getBlue());

      // 数据标准化
      DataNormalization redNormalizer = new NormalizerStandardize(); // 创建红球数据标准化对象
      DataNormalization blueNormalizer = new NormalizerStandardize(); // 创建篮球数据标准化对象

      // 对输入数据进行标准化
      DataSet redInputDataSet = new DataSet(LotteryUtils.toINDArray(redBallInputData, 0, 1, numRedBalls), null);
      redNormalizer.fit(redInputDataSet);
      redNormalizer.transform(redInputDataSet);
      DataSet blueInputDataSet = new DataSet(LotteryUtils.toINDArray(blueBallInputData, 0, 1, numBlueBall), null);
      blueNormalizer.fit(blueInputDataSet);
      blueNormalizer.transform(blueInputDataSet);

      // 使用模型进行预测
      INDArray redPredictions = redBallNet.output(redInputDataSet.getFeatures());
      INDArray bluePredictions = blueBallNet.output(blueInputDataSet.getFeatures());

      // 将预测结果转换为实际的彩票号码
      int[] redPredictionsArray = new int[numRedBalls];
      int[] bluePredictionsArray = new int[numBlueBall];

      for (int j = 0; j < numRedBalls; j++) {
        redPredictionsArray[j] = (int) Math.round(redPredictions.getDouble(0, j));
      }
      for (int j = 0; j < numBlueBall; j++) {
        bluePredictionsArray[j] = (int) Math.round(bluePredictions.getDouble(0, j));
      }

      // 构建预测结果的 SsqDO 对象
      SsqDO predictionResult = getSsqDO(inputData, redPredictionsArray, bluePredictionsArray);
      logger.info("预测结果: {}", predictionResult);
      return predictionResult;
    } else {
      logger.error("暂不支持的彩票名称: {}", lotteryName);
      return null;
    }
  }

  /**
   * 获取预测结果的 SsqDO 对象
   *
   * @param inputData           输入数据
   * @param redPredictionsArray 红球预测结果数组
   * @param bluePredictionsArray 篮球预测结果数组
   * @return 预测结果的 SsqDO 对象
   */
  private static SsqDO getSsqDO(SsqDO inputData, int[] redPredictionsArray, int[] bluePredictionsArray) {
    SsqDO predictionResult = new SsqDO();
    predictionResult.setDateNo(inputData.getDateNo());
    predictionResult.setRed01(String.format("%02d", redPredictionsArray[0]));
    predictionResult.setRed02(String.format("%02d", redPredictionsArray[1]));
    predictionResult.setRed03(String.format("%02d", redPredictionsArray[2]));
    predictionResult.setRed04(String.format("%02d", redPredictionsArray[3]));
    predictionResult.setRed05(String.format("%02d", redPredictionsArray[4]));
    predictionResult.setRed06(String.format("%02d", redPredictionsArray[5]));
    predictionResult.setBlue(String.format("%02d", bluePredictionsArray[0]));
    return predictionResult;
  }

}
