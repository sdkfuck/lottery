package sdk.fuck.lottery.common.utils;

import org.junit.jupiter.api.Test;
import sdk.fuck.lottery.common.constants.SsqConstants;

import java.util.List;

class LotteryUtilsTest {

  @Test
  void readCsvToIntegerList() {
  }

  @Test
  void prepareRedBallData() {
    List<Integer[]> dataList = LotteryUtils.readCsvToIntegerList(SsqConstants.NAME);
    int[][] redBallData = LotteryUtils.prepareRedBallData(dataList);

    // 循环打印红球数据，格式美观
    System.out.println("Red Ball Data:");
    for (int i = 0; i < redBallData.length; i++) {
      System.out.printf("Sample %02d: ", i + 1);
      for (int j = 0; j < redBallData[i].length; j++) {
        System.out.printf("%02d ", redBallData[i][j]);
      }
      System.out.println();
    }
  }

  @Test
  void prepareBlueBallData() {
    List<Integer[]> dataList = LotteryUtils.readCsvToIntegerList(SsqConstants.NAME);
    int[][] blueBallData = LotteryUtils.prepareBlueBallData(dataList);

    // 循环打印蓝球数据，格式美观
    System.out.println("Blue Ball Data:");
    for (int i = 0; i < blueBallData.length; i++) {
      System.out.printf("Sample %02d: ", i + 1);
      for (int j = 0; j < blueBallData[i].length; j++) {
        System.out.printf("%02d ", blueBallData[i][j]);
      }
      System.out.println();
    }
  }

  @Test
  void getLotteryDataCsvPath() {
  }

  @Test
  void getLotteryRedModelPath() {
  }

  @Test
  void getLotteryBlueModelPath() {
  }
}
