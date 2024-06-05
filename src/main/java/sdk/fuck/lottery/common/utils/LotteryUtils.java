package sdk.fuck.lottery.common.utils;

import cn.hutool.core.util.StrUtil;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import sdk.fuck.lottery.common.constants.DltConstants;
import sdk.fuck.lottery.common.constants.SsqConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LotteryUtils {

  /**
   * 读取指定彩票名称的CSV文件，将其内容转换为一个包含浮点数组的列表
   *
   * @param lotteryName 彩票名称
   * @return 包含CSV文件内容的浮点数组列表，每个数组代表CSV文件中的一行
   */
  public static List<Integer[]> readCsvToIntegerList(String lotteryName) {
    // 获取CSV文件的路径
    String csvFilePath = getLotteryDataCsvPath(lotteryName);
    // 用于存储CSV文件数据的列表
    List<Integer[]> dataList = new ArrayList<>();
    // 使用try-with-resources语法自动关闭资源
    try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
      String line;
      boolean isFirstLine = true;
      // 逐行读取CSV文件
      while ((line = br.readLine()) != null) {
        if (isFirstLine) {
          // 跳过表头行
          isFirstLine = false;
          continue;
        }
        // 按逗号分割每行数据
        String[] values = line.split(",");
        // 创建一个长度为8的浮点数组用于存储每行数据
        Integer[] rowValues = new Integer[8];
        // 将每个字符串转换为浮点数并存入数组
        for (int i = 0; i < values.length; i++) {
          rowValues[i] = Integer.parseInt(values[i]);
        }
        // 将数组添加到列表中
        dataList.add(rowValues);
      }
    } catch (IOException e) {
      // 捕获读取文件时的异常并抛出自定义异常
      throw new RuntimeException("Error reading CSV file", e);
    }
    // 将列表倒序
    Collections.reverse(dataList);
    // 返回包含CSV文件内容的列表
    return dataList;
  }

  /**
   * 读取指定彩票名称的CSV文件，将其内容转换为一个包含浮点数组的列表，移除第一行和第二行
   *
   * @param lotteryName 彩票名称
   * @return 包含CSV文件内容的浮点数组列表，每个数组代表CSV文件中的一行（不包含第一行和第二行）
   */
  public static List<Integer[]> readCsvToIntegerListWithoutFirstTwoLines(String lotteryName) {
    // 获取CSV文件的路径
    String csvFilePath = getLotteryDataCsvPath(lotteryName);
    // 用于存储CSV文件数据的列表
    List<Integer[]> dataList = new ArrayList<>();
    // 使用try-with-resources语法自动关闭资源
    try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
      String line;
      int lineCount = 0;
      // 逐行读取CSV文件
      while ((line = br.readLine()) != null) {
        lineCount++;
        if (lineCount <= 2) {
          // 跳过前两行
          continue;
        }
        // 按逗号分割每行数据
        String[] values = line.split(",");
        // 创建一个长度为8的浮点数组用于存储每行数据
        Integer[] rowValues = new Integer[8];
        // 将每个字符串转换为浮点数并存入数组
        for (int i = 0; i < values.length; i++) {
          rowValues[i] = Integer.parseInt(values[i]);
        }
        // 将数组添加到列表中
        dataList.add(rowValues);
      }
    } catch (IOException e) {
      // 捕获读取文件时的异常并抛出自定义异常
      throw new RuntimeException("Error reading CSV file", e);
    }
    // 将列表倒序
    Collections.reverse(dataList);
    // 返回包含CSV文件内容的列表
    return dataList;
  }

  /**
   * 读取指定彩票名称的CSV文件，将其内容转换为一个包含整数数组的列表，移除第一行和最后一行
   *
   * @param lotteryName 彩票名称
   * @return 包含CSV文件内容的整数数组列表，每个数组代表CSV文件中的一行（不包含第一行和最后一行）
   */
  public static List<Integer[]> readCsvToIntegerListWithoutFirstAndLastLines(String lotteryName) {
    // 获取CSV文件的路径
    String csvFilePath = getLotteryDataCsvPath(lotteryName);
    // 用于存储CSV文件数据的列表
    List<Integer[]> dataList = new ArrayList<>();
    // 使用try-with-resources语法自动关闭资源
    try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
      String line;
      boolean firstLineSkipped = false; // 标记是否跳过第一行
      List<String> lines = new ArrayList<>();
      // 逐行读取CSV文件
      while ((line = br.readLine()) != null) {
        if (!firstLineSkipped) {
          firstLineSkipped = true;
          continue; // 跳过第一行
        }
        lines.add(line);
      }
      // 移除最后一行
      if (!lines.isEmpty()) {
        lines.remove(lines.size() - 1);
      }
      // 处理每一行数据
      for (String l : lines) {
        // 按逗号分割每行数据
        String[] values = l.split(",");
        // 创建一个长度为8的整数数组用于存储每行数据
        Integer[] rowValues = new Integer[8];
        // 将每个字符串转换为整数并存入数组
        for (int i = 0; i < values.length; i++) {
          rowValues[i] = Integer.parseInt(values[i]);
        }
        // 将数组添加到列表中
        dataList.add(rowValues);
      }
    } catch (IOException e) {
      // 捕获读取文件时的异常并抛出自定义异常
      throw new RuntimeException("Error reading CSV file", e);
    }
    // 返回包含CSV文件内容的列表
    return dataList;
  }

  /**
   * 准备红球数据
   */
  public static int[][] prepareRedBallData(List<Integer[]> dataList) {
    int[][] redBallData = new int[dataList.size()][6]; // 二维数组，不包括期号
    for (int i = 0; i < dataList.size(); i++) {
      Integer[] row = dataList.get(i);
      for (int j = 0; j < 6; j++) {
        redBallData[i][j] = row[j + 1]; // 红球数据列
      }
    }
    return redBallData;
  }

  /**
   * 准备篮球数据
   */
  public static int[][] prepareBlueBallData(List<Integer[]> dataList) {
    int[][] blueBallData = new int[dataList.size()][1]; // 二维数组，不包括期号
    for (int i = 0; i < dataList.size(); i++) {
      Integer[] row = dataList.get(i);
      blueBallData[i][0] = row[7]; // 篮球数据列
    }
    return blueBallData;
  }

  /**
   * 将二维数组转换为三维特征INDArray
   *
   * @param data       数据数组
   * @param startIndex 起始索引
   * @param batchSize  批量大小
   * @param nums       数量
   * @return 转换后的INDArray
   */
  public static INDArray toINDArray(int[][] data, int startIndex, int batchSize, int nums) {
    int timeSeriesLength = 1; // 时间步长为1
    INDArray array = Nd4j.create(new int[]{batchSize, nums, timeSeriesLength}, 'f'); // 创建INDArray
    for (int i = 0; i < batchSize; i++) {
      for (int j = 0; j < nums; j++) {
        array.putScalar(new int[]{i, j, 0}, data[startIndex + i][j]); // 填充数据
      }
    }
    return array;
  }

  /**
   * 获取指定彩票名称对应的CSV文件路径
   *
   * @param lotteryName 彩票名称
   * @return 对应的CSV文件路径
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

  public static String getLotteryRedModelPath(String lotteryName) {
    // 获取项目根目录的绝对路径
    String projectRootPath = new File("").getAbsolutePath();
    // 根据彩票名称返回相应的模型文件路径
    String redModelPath = StrUtil.equals(lotteryName, SsqConstants.NAME) ?
                          projectRootPath + SsqConstants.SSQ_MODEL_RED_PATH :
                          projectRootPath + DltConstants.DLT_MODEL_RED_PATH;
    // 创建目录
    File redModelDir = new File(redModelPath).getParentFile();
    if (!redModelDir.exists() && !redModelDir.mkdirs()) {
      throw new RuntimeException("Failed to create directory: " + redModelDir.getAbsolutePath());
    }
    return redModelPath;
  }

  public static String getLotteryBlueModelPath(String lotteryName) {
    // 获取项目根目录的绝对路径
    String projectRootPath = new File("").getAbsolutePath();
    // 根据彩票名称返回相应的模型文件路径
    String blueModelPath = StrUtil.equals(lotteryName, SsqConstants.NAME) ?
                           projectRootPath + SsqConstants.SSQ_MODEL_BLUE_PATH :
                           projectRootPath + DltConstants.DLT_MODEL_BLUE_PATH;
    // 创建目录
    File blueModelDir = new File(blueModelPath).getParentFile();
    if (!blueModelDir.exists() && !blueModelDir.mkdirs()) {
      throw new RuntimeException("Failed to create directory: " + blueModelDir.getAbsolutePath());
    }
    return blueModelPath;
  }

}