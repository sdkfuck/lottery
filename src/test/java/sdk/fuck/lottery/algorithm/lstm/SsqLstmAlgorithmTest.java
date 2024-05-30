package sdk.fuck.lottery.algorithm.lstm;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sdk.fuck.lottery.dataobject.SsqDO;

import java.util.List;

@AllArgsConstructor(onConstructor_ = {@Autowired})
@SpringBootTest
class SsqLstmAlgorithmTest {
  private final SsqLstmAlgorithm ssqLstmAlgorithm;

  @Test
  void trainAndSaveModel() {
    List<SsqDO> trainingData = ssqLstmAlgorithm.readCsvData("D:\\JetBrains\\workspace\\lottery\\data\\ssq_data.csv");
    ssqLstmAlgorithm.trainAndSaveModel(trainingData);
  }

  @Test
  void predictByModel() {
    List<SsqDO> predictions = ssqLstmAlgorithm.predictByModel("D:\\JetBrains\\workspace\\lottery\\models\\ssq_mortal.pth", 5);
    String nextDateNo = ssqLstmAlgorithm.getNextDateNo("D:\\JetBrains\\workspace\\lottery\\data\\ssq_data.csv");
    for (SsqDO prediction : predictions) {
      prediction.setDateNo(nextDateNo);
      System.out.println("prediction = " + prediction);
    }
  }
}
