package sdk.fuck.lottery.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import sdk.fuck.lottery.common.constants.DltConstants;
import sdk.fuck.lottery.common.constants.SsqConstants;
import sdk.fuck.lottery.dataobject.DltDO;
import sdk.fuck.lottery.dataobject.LotteryData;
import sdk.fuck.lottery.dataobject.SsqDO;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for fetching and processing historical lottery data.
 * Provides methods to fetch lottery data from specified URLs and write it to CSV files.
 */
@Slf4j
@Service
public class GetLotteryHistoryDataService {

  /**
   * Fetches historical SSQ lottery data from a specified URL.
   * Parses the HTML content to extract lottery data and stores it in a list of SsqDO objects.
   *
   * @return List of SsqDO objects containing lottery data or null if no data is found.
   */
  public List<SsqDO> ssqGet() {
    return fetchLotteryData(SsqConstants.SSQ_HISTORY_URL, SsqDO.class);
  }

  /**
   * Fetches historical DLT lottery data from a specified URL.
   * Parses the HTML content to extract lottery data and stores it in a list of DltDO objects.
   *
   * @return List of DltDO objects containing lottery data or null if no data is found.
   */
  public List<DltDO> dltGet() {
    return fetchLotteryData(DltConstants.DLT_HISTORY_URL, DltDO.class);
  }

  /**
   * Generic method to fetch lottery data and parse it into a list of given type.
   *
   * @param url   the URL to fetch data from
   * @param clazz the class type of the lottery data objects
   * @param <T>   the type parameter
   * @return List of lottery data objects or null if no data is found
   */
  private <T extends LotteryData> List<T> fetchLotteryData(String url, Class<T> clazz) {
    try {
      Document doc = Jsoup.connect(url).get();
      Element tbody = doc.selectFirst("tbody#tdata");
      if (ObjectUtils.isEmpty(tbody)) return null;

      Elements trs = tbody.select("tr");
      List<T> data = new ArrayList<>();
      for (Element tr : trs) {
        T item = clazz.getDeclaredConstructor().newInstance();
        Elements tds = tr.select("td");

        if (clazz == SsqDO.class) {
          ((SsqDO) item).setDateNo(tds.get(0).text().trim());
          ((SsqDO) item).setRed01(tds.get(1).text().trim());
          ((SsqDO) item).setRed02(tds.get(2).text().trim());
          ((SsqDO) item).setRed03(tds.get(3).text().trim());
          ((SsqDO) item).setRed04(tds.get(4).text().trim());
          ((SsqDO) item).setRed05(tds.get(5).text().trim());
          ((SsqDO) item).setRed06(tds.get(6).text().trim());
          ((SsqDO) item).setBlue(tds.get(7).text().trim());
        } else if (clazz == DltDO.class) {
          ((DltDO) item).setDateNo(tds.get(0).text().trim());
          ((DltDO) item).setRed01(tds.get(1).text().trim());
          ((DltDO) item).setRed02(tds.get(2).text().trim());
          ((DltDO) item).setRed03(tds.get(3).text().trim());
          ((DltDO) item).setRed04(tds.get(4).text().trim());
          ((DltDO) item).setRed05(tds.get(5).text().trim());
          ((DltDO) item).setBlue01(tds.get(6).text().trim());
          ((DltDO) item).setBlue02(tds.get(7).text().trim());
        }
        data.add(item);
      }
      return data;
    } catch (IOException | ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Fetches SSQ lottery data and writes it to a CSV file in the 'data' directory within the project root.
   */
  public void ssqGetAndWriteCsv() {
    writeCsv(ssqGet(), SsqConstants.SSQ_CSV_HEADER, SsqConstants.SSQ_DATA_CSV);
  }

  /**
   * Fetches DLT lottery data and writes it to a CSV file in the 'data' directory within the project root.
   */
  public void dltGetAndWriteCsv() {
    writeCsv(dltGet(), DltConstants.DLT_CSV_HEADER, DltConstants.DLT_DATA_CSV);
  }

  /**
   * Generic method to write lottery data to a CSV file.
   *
   * @param data     List of lottery data objects
   * @param header   CSV header string
   * @param fileName name of the CSV file
   * @param <T>      type parameter
   */
  private <T extends LotteryData> void writeCsv(List<T> data, String header, String fileName) {
    if (CollectionUtils.isEmpty(data)) {
      log.error("No data to write to CSV file.");
      return;
    }

    // Get the project root directory
    String projectRoot = Paths.get("").toAbsolutePath().toString();
    // Create the data directory if it doesn't exist
    File dataDir = new File(projectRoot, "data");
    if (!dataDir.exists() && !dataDir.mkdirs()) {
      log.error("Failed to create data directory.");
      return;
    }

    // Create the CSV file in the data directory
    File csvFile = new File(dataDir, fileName);

    try (FileWriter writer = new FileWriter(csvFile)) {
      writer.append(header).append("\n");
      for (T row : data) {
        writer.append(row.toCsvLine()).append("\n");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
