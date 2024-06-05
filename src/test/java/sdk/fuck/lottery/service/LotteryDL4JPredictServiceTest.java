package sdk.fuck.lottery.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sdk.fuck.lottery.common.constants.SsqConstants;
import sdk.fuck.lottery.domain.dataobject.SsqDO;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
class LotteryDL4JPredictServiceTest {
  private final LotteryDL4JPredictService lotteryDL4JPredictService;

  @Test
  void predict() {
    // 24062,01,07,10,16,18,27,16
    SsqDO ssqDO = new SsqDO();
    ssqDO.setDateNo("24062");
    ssqDO.setRed01("01");
    ssqDO.setRed02("07");
    ssqDO.setRed03("10");
    ssqDO.setRed04("16");
    ssqDO.setRed05("18");
    ssqDO.setRed06("27");
    ssqDO.setBlue("16");
    SsqDO predict = lotteryDL4JPredictService.predict(SsqConstants.NAME, ssqDO);
    System.out.println("predict = " + predict);
  }
}
