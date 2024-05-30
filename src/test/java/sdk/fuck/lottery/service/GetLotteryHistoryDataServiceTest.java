package sdk.fuck.lottery.service;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@AllArgsConstructor(onConstructor_={@Autowired})
@SpringBootTest
class GetLotteryHistoryDataServiceTest {
  private final GetLotteryHistoryDataService getLotteryHistoryDataService;

  @Test
  void ssqGetAndWriteCsv() {
    getLotteryHistoryDataService.ssqGetAndWriteCsv();
  }

  @Test
  void dltGetAndWriteCsv() {
    getLotteryHistoryDataService.dltGetAndWriteCsv();
  }
}
