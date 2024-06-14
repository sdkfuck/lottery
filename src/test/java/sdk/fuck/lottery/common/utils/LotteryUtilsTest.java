package sdk.fuck.lottery.common.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sdk.fuck.lottery.domain.dataobject.DltDO;
import sdk.fuck.lottery.domain.dataobject.SsqDO;

import java.util.List;

class LotteryUtilsTest {

  @Test
  void testReadAndParseSsqCsv() {
    // 设定一个测试 CSV 文件路径
    List<SsqDO> ssqDOList = LotteryUtils.readAndParseSsqCsv();

    // 验证结果
    Assertions.assertNotNull(ssqDOList);
    Assertions.assertFalse(ssqDOList.isEmpty());

    LotteryUtils.printSsqResults(ssqDOList);
  }

  @Test
  void testReadAndParseDltCsv() {
    // 设定一个测试 CSV 文件路径
    List<DltDO> dltDOList = LotteryUtils.readAndParseDltCsv();

    // 验证结果
    Assertions.assertNotNull(dltDOList);
    Assertions.assertFalse(dltDOList.isEmpty());

    LotteryUtils.printDltResults(dltDOList);
  }

  @Test
  void testGetLotteryDataCsvPath() {
    String ssqPath = LotteryUtils.getLotteryDataCsvPath("ssq");
    Assertions.assertTrue(ssqPath.contains("ssq"));

    String dltPath = LotteryUtils.getLotteryDataCsvPath("dlt");
    Assertions.assertTrue(dltPath.contains("dlt"));
  }

  @Test
  void testGetLotteryModelPath() {
    String ssqModelPath = LotteryUtils.getLotteryModelPath("ssq");
    Assertions.assertTrue(ssqModelPath.contains("ssq"));

    String dltModelPath = LotteryUtils.getLotteryModelPath("dlt");
    Assertions.assertTrue(dltModelPath.contains("dlt"));
  }
}
