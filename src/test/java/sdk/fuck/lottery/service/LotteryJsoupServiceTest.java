package sdk.fuck.lottery.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sdk.fuck.lottery.common.constants.DltConstants;
import sdk.fuck.lottery.common.constants.SsqConstants;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
class LotteryJsoupServiceTest {
  private final LotteryJsoupService lotteryJsoupService;

  @Test
  void run() {
    lotteryJsoupService.run(SsqConstants.NAME);
    lotteryJsoupService.run(DltConstants.NAME);
  }

}
