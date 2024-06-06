package sdk.fuck.lottery.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sdk.fuck.lottery.common.constants.DltConstants;
import sdk.fuck.lottery.common.constants.SsqConstants;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
class LotteryLSTMServiceTest {
  private final LotteryLSTMService lotteryLSTMService;

  @Test
  void train() {
    lotteryLSTMService.train(SsqConstants.NAME, 10000);
    lotteryLSTMService.train(DltConstants.NAME, 10000);
  }

  @Test
  void evaluate() {
    lotteryLSTMService.evaluate(SsqConstants.NAME);
    lotteryLSTMService.evaluate(DltConstants.NAME);
  }

  @Test
  void predict() {
    lotteryLSTMService.predict(SsqConstants.NAME);
    lotteryLSTMService.predict(DltConstants.NAME);
  }
}
