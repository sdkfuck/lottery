package sdk.fuck.lottery.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sdk.fuck.lottery.common.constants.DltConstants;
import sdk.fuck.lottery.common.constants.SsqConstants;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
class LotteryJsoupServiceTest {
  private final LotteryJsoupService lotteryJsoupService;

  @Test
  void getUrl() {
    String[] ssqUrl = lotteryJsoupService.getUrl(SsqConstants.NAME);
    System.out.println("ssqUrl = " + Arrays.toString(ssqUrl));
    String[] dltUrl = lotteryJsoupService.getUrl(DltConstants.NAME);
    System.out.println("dltUrl = " + Arrays.toString(dltUrl));
  }

  @Test
  void getCurrentNumber() {
    String ssqNo = lotteryJsoupService.getCurrentNumber(SsqConstants.NAME);
    System.out.println("ssqNo = " + ssqNo);
    String dltNo = lotteryJsoupService.getCurrentNumber(DltConstants.NAME);
    System.out.println("dltNo = " + dltNo);
  }

  @Test
  void spider() {
    List<Map<String, String>> ssq = lotteryJsoupService.spider(SsqConstants.NAME, 1, lotteryJsoupService.getCurrentNumber(SsqConstants.NAME));
    System.out.println("ssq = " + ssq);
    List<Map<String, String>> dlt = lotteryJsoupService.spider(DltConstants.NAME, 1, lotteryJsoupService.getCurrentNumber(DltConstants.NAME));
    System.out.println("dlt = " + dlt);
  }

  @Test
  void saveDataToCSV() {
    lotteryJsoupService.saveDataToCSV(lotteryJsoupService.spider(SsqConstants.NAME, 1, lotteryJsoupService.getCurrentNumber(SsqConstants.NAME)), SsqConstants.NAME);
    lotteryJsoupService.saveDataToCSV(lotteryJsoupService.spider(DltConstants.NAME, 1, lotteryJsoupService.getCurrentNumber(DltConstants.NAME)), DltConstants.NAME);
  }

  @Test
  void run() {
    lotteryJsoupService.run(SsqConstants.NAME);
    lotteryJsoupService.run(DltConstants.NAME);
  }

  @Test
  void generateTrainCSV() {
    lotteryJsoupService.generateTrainCSV(SsqConstants.NAME, 1);
    lotteryJsoupService.generateTrainCSV(DltConstants.NAME, 1);
  }

  @Test
  void generateTestCSV() {
    lotteryJsoupService.generateTestCSV(SsqConstants.NAME, 1);
    lotteryJsoupService.generateTestCSV(DltConstants.NAME, 1);
  }
}
