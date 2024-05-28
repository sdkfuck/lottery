package sdk.fuck.lottery.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import sdk.fuck.lottery.common.constants.SsqConstants;
import sdk.fuck.lottery.dataobject.SsqDO;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for fetching and processing historical lottery data.
 * This class provides methods to fetch lottery data from a specified URL
 * and write the data to a CSV file in the project's data directory.
 *
 * @author 坂本
 */
@Slf4j
@Service
public class GetLotteryHistoryDataService {

  /**
   * Fetches historical lottery data from a specified URL.
   * Parses the HTML content to extract lottery data and stores it in a list of SsqDO objects.
   *
   * @return List of SsqDO objects containing lottery data or null if no data is found.
   */
  public List<SsqDO> ssqGet() {
    try {
      // Connect to the specified URL and fetch the document
      Document doc = Jsoup.connect(SsqConstants.SSQ_HISTORY_URL).get();
      // Select the table body element containing the lottery data
      Element tbody = doc.selectFirst("tbody#tdata");
      if (ObjectUtils.isEmpty(tbody)) return null;
      // Select all table rows within the table body
      Elements trs = tbody.select("tr");
      List<SsqDO> data = new ArrayList<>();
      // Iterate over each row and extract lottery data
      for (Element tr : trs) {
        SsqDO item = new SsqDO();
        Elements tds = tr.select("td");
        item.setDateNo(tds.get(0).text().trim());
        item.setRed01(tds.get(1).text().trim());
        item.setRed02(tds.get(2).text().trim());
        item.setRed03(tds.get(3).text().trim());
        item.setRed04(tds.get(4).text().trim());
        item.setRed05(tds.get(5).text().trim());
        item.setRed06(tds.get(6).text().trim());
        item.setBlue(tds.get(7).text().trim());
        data.add(item);
      }
      return data;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Fetches historical lottery data and writes it to a CSV file.
   * The CSV file is created in the 'data' directory within the project root.
   */
  public void ssqGetAndWriteCsv() {
    // Get the project root directory
    String projectRoot = Paths.get("").toAbsolutePath().toString();
    // Create the data directory if it doesn't exist
    File dataDir = new File(projectRoot, "data");
    if (!dataDir.exists()) {
      boolean mkdirs = dataDir.mkdirs();
      if (!mkdirs) log.error("Failed to create data directory.");
    }
    // Create the CSV file in the data directory
    File csvFile = new File(dataDir, "ssq_data.csv");

    // Fetch the lottery data
    List<SsqDO> data = ssqGet();
    try (FileWriter writer = new FileWriter(csvFile)) {
      // Write the CSV header if data is not empty
      if (!CollectionUtils.isEmpty(data)) {
        writer.append(SsqConstants.CSV_HEADER).append("\n");
        // Write each row of lottery data to the CSV file
        for (SsqDO row : data) {
          writer.append(row.getDateNo()).append(",");
          writer.append(row.getRed01()).append(",");
          writer.append(row.getRed02()).append(",");
          writer.append(row.getRed03()).append(",");
          writer.append(row.getRed04()).append(",");
          writer.append(row.getRed05()).append(",");
          writer.append(row.getRed06()).append(",");
          writer.append(row.getBlue());
          writer.append("\n");
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
