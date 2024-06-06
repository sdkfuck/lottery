package sdk.fuck.lottery.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 彩票数据服务类，用于获取彩票历史数据并保存到CSV文件中。
 */
@Service
public class LotteryJsoupService {
  private static final Logger LOGGER = LoggerFactory.getLogger(LotteryJsoupService.class);

  /**
   * 获取彩票数据的URL和路径。
   *
   * @param name 彩票名称（如ssq, dlt）
   * @return 包含基本URL和路径的字符串数组
   */
  public String[] getUrl(String name) {
    String url = "https://datachart.500.com/" + name + "/history/";
    String path = "newinc/history.php?start=%s&end=";
    return new String[]{url, path};
  }

  /**
   * 获取最新一期彩票号码。
   *
   * @param name 彩票名称（如ssq, dlt）
   * @return 最新一期彩票号码
   */
  public String getCurrentNumber(String name) {
    String[] urlComponents = getUrl(name);
    String url = urlComponents[0] + "history.shtml";
    try {
      HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
      connection.setRequestMethod("GET");

      try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "GB2312"))) {
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }

        Document doc = Jsoup.parse(response.toString());
        Element currentNumElement = doc.selectFirst("div.wrap_datachart input#end");
        assert currentNumElement != null;
        return currentNumElement.attr("value");
      }
    } catch (IOException e) {
      LOGGER.error("获取最新一期数字时出错", e);
    }
    return null;
  }

  /**
   * 爬取历史数据。
   *
   * @param name  彩票名称（如ssq, dlt）
   * @param start 起始期号
   * @param end   结束期号
   * @return 历史数据的列表
   */
  public List<Map<String, String>> spider(String name, int start, String end) {
    List<Map<String, String>> data = new ArrayList<>();
    String[] urlComponents = getUrl(name);
    String url = String.format(urlComponents[0] + urlComponents[1], start, end);

    try {
      HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
      connection.setRequestMethod("GET");

      try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "GB2312"))) {
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }

        Document doc = Jsoup.parse(response.toString());
        Elements trs = doc.select("tbody#tdata tr");
        for (Element tr : trs) {
          Map<String, String> item = new LinkedHashMap<>();  // 使用LinkedHashMap保持插入顺序
          if ("ssq".equals(name)) {
            item.put("期数", tr.child(0).text().trim());
            for (int i = 1; i <= 6; i++) {
              item.put("红球_" + i, tr.child(i).text().trim());
            }
            item.put("蓝球", tr.child(7).text().trim());
          } else if ("dlt".equals(name)) {
            item.put("期数", tr.child(0).text().trim());
            for (int i = 1; i <= 5; i++) {
              item.put("红球_" + i, tr.child(i).text().trim());
            }
            for (int j = 0; j < 2; j++) {
              item.put("蓝球_" + (j + 1), tr.child(6 + j).text().trim());
            }
          } else {
            LOGGER.warn("抱歉，没有找到数据源！");
          }
          data.add(0, item);  // 将数据插入到列表的开头
        }
        saveDataToCSV(data, name);
      }
    } catch (IOException e) {
      LOGGER.error("爬取历史数据时出错", e);
    }
    return data;
  }

  /**
   * 将数据保存到CSV文件中。
   *
   * @param data 要保存的数据
   * @param name 彩票名称（如ssq, dlt）
   */
  public void saveDataToCSV(List<Map<String, String>> data, String name) {
    String filePath = "data/" + name + "_history.csv";
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
      // 写入表头
      Map<String, String> firstRecord = data.get(0);
      List<String> headers = new ArrayList<>(firstRecord.keySet());
      writer.write(String.join(",", headers));
      writer.newLine();
      // 写入数据
      for (Map<String, String> record : data) {
        List<String> values = new ArrayList<>();
        for (String header : headers) {
          values.add(record.get(header));
        }
        writer.write(String.join(",", values));
        writer.newLine();
      }
      LOGGER.info("数据已保存到: {}", filePath);
    } catch (IOException e) {
      LOGGER.error("保存数据到CSV文件时出错", e);
    }
  }

  /**
   * 运行彩票数据服务。
   *
   * @param name 彩票名称（如ssq, dlt）
   */
  public void run(String name) {
    String currentNumber = getCurrentNumber(name);
    if (currentNumber != null) {
      LOGGER.info("【{}】最新一期期号：{}", name, currentNumber);
      LOGGER.info("正在获取【{}】数据......", name);
      try {
        Path path = Paths.get("data");
        if (!Files.exists(path)) {
          Files.createDirectories(path);
        }
        List<Map<String, String>> data = spider(name, 1, currentNumber);
        if (!data.isEmpty()) {
          LOGGER.info("【{}】数据准备就绪，共{}期, 下一步可训练模型......", name, data.size());
        } else {
          LOGGER.error("数据文件不存在！");
        }
      } catch (IOException e) {
        LOGGER.error("创建数据目录时出错", e);
      }
    } else {
      LOGGER.error("获取最新一期期号失败！");
    }

  }

  /**
   * 生成训练数据文件train.csv。
   *
   * @param name      彩票名称（如ssq, dlt）
   * @param timeSteps 时间步长
   */
  public void generateTrainCSV(String name, int timeSteps) {
    generateCSV(name, timeSteps, "train");
  }

  /**
   * 生成测试数据文件test.csv。
   *
   * @param name      彩票名称（如ssq, dlt）
   * @param timeSteps 时间步长
   */
  public void generateTestCSV(String name, int timeSteps) {
    generateCSV(name, timeSteps, "test");
  }

  /**
   * 生成训练或测试数据文件。
   *
   * @param name      彩票名称（如ssq, dlt）
   * @param timeSteps 时间步长
   * @param type      数据类型（train或test）
   */
  private void generateCSV(String name, int timeSteps, String type) {
    String historyFilePath = "data/" + name + "_history.csv";
    String outputFilePath = "data/" + name + "_" + type + ".csv";
    try (BufferedReader reader = Files.newBufferedReader(Paths.get(historyFilePath));
         BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFilePath), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

      // 读取表头
      String headerLine = reader.readLine();
      if (headerLine == null) {
        LOGGER.error("历史数据文件为空！");
        return;
      }

      // 解析表头并创建训练数据表头
      String[] headers = headerLine.split(",");
      List<String> csvHeaders = new ArrayList<>();
      for (int i = timeSteps; i > 0; i--) {
        for (int j = 1; j < headers.length; j++) { // 从1开始跳过“期数”列
          csvHeaders.add(headers[j] + "_f");
        }
      }
      for (int j = 1; j < headers.length; j++) { // 从1开始跳过“期数”列
        csvHeaders.add(headers[j] + "_l");
      }
      writer.write(String.join(",", csvHeaders));
      writer.newLine();

      // 读取数据并生成时间序列
      List<String[]> data = new ArrayList<>();
      String line;
      while ((line = reader.readLine()) != null) {
        data.add(line.split(","));
      }

      for (int i = timeSteps; i < data.size(); i++) {
        List<String> row = new ArrayList<>();
        for (int j = i - timeSteps; j < i; j++) {  // 不包括当前行作为特征
          row.addAll(Arrays.asList(data.get(j)).subList(1, data.get(j).length));
        }
        // 当前行作为标签
        row.addAll(Arrays.asList(data.get(i)).subList(1, data.get(i).length));
        writer.write(String.join(",", row));
        writer.newLine();
      }
      LOGGER.info("{}数据已生成到: {}", type.equals("train") ? "训练" : "测试", outputFilePath);
    } catch (IOException e) {
      LOGGER.error("生成{}数据文件时出错", type.equals("train") ? "训练" : "测试", e);
    }
  }
}
