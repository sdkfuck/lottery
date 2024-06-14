package sdk.fuck.lottery.common.utils;

import cn.hutool.core.util.StrUtil;
import sdk.fuck.lottery.common.constants.DltConstants;
import sdk.fuck.lottery.common.constants.SsqConstants;
import sdk.fuck.lottery.domain.dataobject.DltDO;
import sdk.fuck.lottery.domain.dataobject.SsqDO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LotteryUtils 提供读取和解析彩票数据的实用方法。
 */
public class LotteryUtils {

  /**
   * 读取指定路径的 CSV 文件并返回行列表。
   *
   * @param filePath CSV 文件路径
   * @return 文件行列表
   */
  public static List<String> readCsv(String filePath) {
    try {
      return Files.readAllLines(Paths.get(filePath));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 解析双色球的 CSV 数据。
   *
   * @param lines CSV 文件行列表
   * @return 解析后的 SsqDO 对象列表
   */
  public static List<SsqDO> parseSsqCsv(List<String> lines) {
    List<SsqDO> results = new ArrayList<>();

    // 跳过标题行，从第二行开始读取
    for (int i = 1; i < lines.size(); i++) {
      String line = lines.get(i);
      String[] fields = line.split(",");
      if (fields.length != 8) {
        throw new IllegalArgumentException("CSV format is incorrect");
      }

      SsqDO ssqDO = new SsqDO();
      ssqDO.setNumber(fields[0]);
      ssqDO.setRed01(fields[1]);
      ssqDO.setRed02(fields[2]);
      ssqDO.setRed03(fields[3]);
      ssqDO.setRed04(fields[4]);
      ssqDO.setRed05(fields[5]);
      ssqDO.setRed06(fields[6]);
      ssqDO.setBlue(fields[7]);

      results.add(ssqDO);
    }

    // 反转列表，使期号由小到大排列
    Collections.reverse(results);
    return results;
  }

  /**
   * 解析大乐透的 CSV 数据。
   *
   * @param lines CSV 文件行列表
   * @return 解析后的 DltDO 对象列表
   */
  public static List<DltDO> parseDltCsv(List<String> lines) {
    List<DltDO> results = new ArrayList<>();

    // 跳过标题行，从第二行开始读取
    for (int i = 1; i < lines.size(); i++) {
      String line = lines.get(i);
      String[] fields = line.split(",");
      if (fields.length != 8) {
        throw new IllegalArgumentException("CSV format is incorrect");
      }

      DltDO dltDO = new DltDO();
      dltDO.setNumber(fields[0]);
      dltDO.setRed01(fields[1]);
      dltDO.setRed02(fields[2]);
      dltDO.setRed03(fields[3]);
      dltDO.setRed04(fields[4]);
      dltDO.setRed05(fields[5]);
      dltDO.setBlue01(fields[6]);
      dltDO.setBlue02(fields[7]);

      results.add(dltDO);
    }

    // 反转列表，使期号由小到大排列
    Collections.reverse(results);
    return results;
  }

  /**
   * 读取并解析双色球的 CSV 文件。
   *
   * @return 解析后的 SsqDO 对象列表
   */
  public static List<SsqDO> readAndParseSsqCsv() {
    String filePath = getLotteryDataCsvPath(SsqConstants.NAME);
    List<String> lines = readCsv(filePath);
    return parseSsqCsv(lines);
  }

  /**
   * 读取并解析大乐透的 CSV 文件。
   *
   * @return 解析后的 DltDO 对象列表
   */
  public static List<DltDO> readAndParseDltCsv() {
    String filePath = getLotteryDataCsvPath(DltConstants.NAME);
    List<String> lines = readCsv(filePath);
    return parseDltCsv(lines);
  }

  /**
   * 获取指定彩票名称对应的 CSV 文件路径。
   *
   * @param lotteryName 彩票名称
   * @return 对应的 CSV 文件路径
   */
  public static String getLotteryDataCsvPath(String lotteryName) {
    // 获取项目根目录的绝对路径
    String projectRootPath = new File("").getAbsolutePath();
    // 根据彩票名称返回相应的CSV文件路径
    String csvFilePath = StrUtil.equals(lotteryName, SsqConstants.NAME) ?
                         projectRootPath + SsqConstants.SSQ_DATA_CSV_PATH :
                         projectRootPath + DltConstants.DLT_DATA_CSV_PATH;
    // 创建目录
    File csvFileDir = new File(csvFilePath).getParentFile();
    if (!csvFileDir.exists() && !csvFileDir.mkdirs()) {
      throw new RuntimeException("Failed to create directory: " + csvFileDir.getAbsolutePath());
    }
    return csvFilePath;
  }

  /**
   * 获取指定彩票名称对应的模型文件路径。
   *
   * @param lotteryName 彩票名称
   * @return 对应的模型文件路径
   */
  public static String getLotteryModelPath(String lotteryName) {
    // 获取项目根目录的绝对路径
    String projectRootPath = new File("").getAbsolutePath();
    // 根据彩票名称返回相应的模型文件路径
    String redModelPath = StrUtil.equals(lotteryName, SsqConstants.NAME) ?
                          projectRootPath + SsqConstants.SSQ_MODEL_PATH :
                          projectRootPath + DltConstants.DLT_MODEL_PATH;
    // 创建目录
    File redModelDir = new File(redModelPath).getParentFile();
    if (!redModelDir.exists() && !redModelDir.mkdirs()) {
      throw new RuntimeException("Failed to create directory: " + redModelDir.getAbsolutePath());
    }
    return redModelPath;
  }

  // 打印双色球结果
  public static void printSsqResults(List<SsqDO> ssqDOList) {
    System.out.println("双色球结果：");
    System.out.println("期号\t红球01\t红球02\t红球03\t红球04\t红球05\t红球06\t蓝球");

    for (SsqDO ssqDO : ssqDOList) {
      System.out.printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s%n",
                        ssqDO.getNumber(),
                        ssqDO.getRed01(),
                        ssqDO.getRed02(),
                        ssqDO.getRed03(),
                        ssqDO.getRed04(),
                        ssqDO.getRed05(),
                        ssqDO.getRed06(),
                        ssqDO.getBlue());
    }
  }

  // 打印大乐透结果
  public static void printDltResults(List<DltDO> dltDOList) {
    System.out.println("大乐透结果：");
    System.out.println("期号\t红球01\t红球02\t红球03\t红球04\t红球05\t蓝球01\t蓝球02");

    for (DltDO dltDO : dltDOList) {
      System.out.printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s%n",
                        dltDO.getNumber(),
                        dltDO.getRed01(),
                        dltDO.getRed02(),
                        dltDO.getRed03(),
                        dltDO.getRed04(),
                        dltDO.getRed05(),
                        dltDO.getBlue01(),
                        dltDO.getBlue02());
    }
  }
}
