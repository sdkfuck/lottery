package sdk.fuck.lottery.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sdk.fuck.lottery.common.constants.DltConstants;
import sdk.fuck.lottery.common.constants.SsqConstants;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
class LotteryModelServiceTest {
  private final LotteryModelService lotteryModelService;

  @Test
  void createAndTrainModel() {
    lotteryModelService.createAndTrainModel(SsqConstants.NAME, 10000);
    lotteryModelService.createAndTrainModel(DltConstants.NAME, 10000);
  }

  @Test
  void evaluateModel() {
    String ssqRes = lotteryModelService.evaluateModel(SsqConstants.NAME);
    String dltRes = lotteryModelService.evaluateModel(DltConstants.NAME);
    System.out.println(ssqRes);
    System.out.println(dltRes);
  }

  @Test
  void predictNextNumbers() {
    String ssqRes = lotteryModelService.predictNextNumbers(SsqConstants.NAME);
    String dltRes = lotteryModelService.predictNextNumbers(DltConstants.NAME);
    System.out.println(ssqRes);
    System.out.println(dltRes);
  }

}
