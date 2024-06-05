package sdk.fuck.lottery.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sdk.fuck.lottery.common.constants.SsqConstants;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
class LotteryDL4JTrainServiceTest {
  private final LotteryDL4JTrainService lotteryDL4JTrainService;

  @Test
  void train() {
    lotteryDL4JTrainService.train(SsqConstants.NAME);
  }

  @Test
  void evaluate() {
    lotteryDL4JTrainService.evaluate(SsqConstants.NAME);
  }
}
