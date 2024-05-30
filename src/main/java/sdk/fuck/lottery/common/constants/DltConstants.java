package sdk.fuck.lottery.common.constants;

/**
 * Constants related to Lotto (DLT)
 */
public interface DltConstants {
  // URL for DLT historical data
  String DLT_HISTORY_URL = "https://datachart.500.com/dlt/history/newinc/history.php?start=00000";
  // CSV header for DLT data
  String DLT_CSV_HEADER = "期数,红球_1,红球_2,红球_3,红球_4,红球_5,蓝球_1,蓝球_2";
  // Filename for DLT data CSV
  String DLT_DATA_CSV = "dlt_data.csv";
  // File path for DLT model checkpoint
  String DLT_MORTAL_PTH = "dlt_mortal.pth";
}
