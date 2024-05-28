package sdk.fuck.lottery.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import sdk.fuck.lottery.common.constants.SsqConstants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GetLotteryHistoryDataService {

  public List<Map<String, String>> ssqGet() {
    try {
      Document doc = Jsoup.connect(SsqConstants.SSQ_HISTORY_URL).get();
      Element tbody = doc.selectFirst("tbody#tdata");
      if (ObjectUtils.isEmpty(tbody)) return null;
      Elements trs = tbody.select("tr");
      List<Map<String, String>> data = new ArrayList<>();
      for (Element tr : trs) {
        Map<String, String> item = new HashMap<>();
        Elements tds = tr.select("td");
        item.put("期数", tds.get(0).text().trim());
        for (int i = 0; i < 6; i++) {
          item.put("红球_" + (i + 1), tds.get(i + 1).text().trim());
        }
        item.put("蓝球", tds.get(7).text().trim());
        data.add(item);
      }
      return data;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void ssqGetAndWriteCsv() {
    // Get the project root directory
    String projectRoot = Paths.get("").toAbsolutePath().toString();
    // Create the data directory if it doesn't exist
    File dataDir = new File(projectRoot, "data");
    if (!dataDir.exists()) {
      boolean mkdirs = dataDir.mkdirs();
      if (!mkdirs) System.err.println("Failed to create data directory.");
    }
    // Create the CSV file in the data directory
    File csvFile = new File(dataDir, "ssq_data.csv");

    List<Map<String, String>> data = ssqGet();
    try (FileWriter writer = new FileWriter(csvFile)) {
      if (!CollectionUtils.isEmpty(data)) {
        String[] header = data.get(0).keySet().toArray(new String[0]);
        writer.append(String.join(",", header)).append("\n");
        for (Map<String, String> row : data) {
          for (String key : header) {
            writer.append(row.getOrDefault(key, "")).append(",");
          }
          writer.append("\n");
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
