package sdk.fuck.lottery.service;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.stereotype.Service;
import sdk.fuck.lottery.common.constants.SsqConstants;
import sdk.fuck.lottery.common.utils.LotteryUtils;
import sdk.fuck.lottery.domain.dataobject.DltDO;
import sdk.fuck.lottery.domain.dataobject.SsqDO;

import java.util.List;

/**
 * LotteryDataProcessorService 负责处理和生成彩票数据集。
 */
@Service
public class LotteryDataProcessorService {

  /**
   * 根据彩票名称创建数据集。
   *
   * @param lotteryName 彩票名称，可以是 "双色球" 或 "大乐透"。
   * @return 生成的 DataSet 对象，包含特征和标签。
   */
  public DataSet createDataSet(String lotteryName) {
    return getDataSet(lotteryName);
  }

  private DataSet getDataSet(String lotteryName) {
    int numSamples;
    List<SsqDO> ssqDOList = null;
    List<DltDO> dltDOList = null;

    if (SsqConstants.NAME.equals(lotteryName)) {
      ssqDOList = LotteryUtils.readAndParseSsqCsv();
      numSamples = ssqDOList.size();
    } else {
      dltDOList = LotteryUtils.readAndParseDltCsv();
      numSamples = dltDOList.size();
    }

    if (numSamples <= 2) {
      throw new RuntimeException("Number of samples is less than 2");
    }

    int featuresSamples = numSamples - 1;
    final int featureSize = 7;  // 每个样本的特征数目
    // 创建 n x n 的数组（几行几列）
    INDArray features = Nd4j.zeros(featuresSamples, featureSize);
    INDArray labels = Nd4j.zeros(featuresSamples, featureSize);

    if (SsqConstants.NAME.equals(lotteryName)) {
      for (int i = 0; i < featuresSamples; i++) {
        SsqDO current = ssqDOList.get(i);
        SsqDO next = ssqDOList.get(i + 1);

        // 填充 features
        features.putRow(i, Nd4j.create(new double[]{
          Double.parseDouble(current.getRed01()),
          Double.parseDouble(current.getRed02()),
          Double.parseDouble(current.getRed03()),
          Double.parseDouble(current.getRed04()),
          Double.parseDouble(current.getRed05()),
          Double.parseDouble(current.getRed06()),
          Double.parseDouble(current.getBlue())
        }));

        // 填充 labels
        labels.putRow(i, Nd4j.create(new double[]{
          Double.parseDouble(next.getRed01()),
          Double.parseDouble(next.getRed02()),
          Double.parseDouble(next.getRed03()),
          Double.parseDouble(next.getRed04()),
          Double.parseDouble(next.getRed05()),
          Double.parseDouble(next.getRed06()),
          Double.parseDouble(next.getBlue())
        }));
      }
    } else {
      for (int i = 0; i < featuresSamples; i++) {
        DltDO current = dltDOList.get(i);
        DltDO next = dltDOList.get(i + 1);

        // 填充 features
        features.putRow(i, Nd4j.create(new double[]{
          Double.parseDouble(current.getRed01()),
          Double.parseDouble(current.getRed02()),
          Double.parseDouble(current.getRed03()),
          Double.parseDouble(current.getRed04()),
          Double.parseDouble(current.getRed05()),
          Double.parseDouble(current.getBlue01()),
          Double.parseDouble(current.getBlue02())
        }));

        // 填充 labels
        labels.putRow(i, Nd4j.create(new double[]{
          Double.parseDouble(next.getRed01()),
          Double.parseDouble(next.getRed02()),
          Double.parseDouble(next.getRed03()),
          Double.parseDouble(next.getRed04()),
          Double.parseDouble(next.getRed05()),
          Double.parseDouble(next.getBlue01()),
          Double.parseDouble(next.getBlue02())
        }));
      }
    }

    return new DataSet(features, labels);
  }
}
