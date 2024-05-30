package sdk.fuck.lottery.common.constants;

/**
 * Constants related to Double Color Ball (SSQ)
 */
public interface SsqConstants {
  // URL for SSQ historical data
  String SSQ_HISTORY_URL = "https://datachart.500.com/ssq/history/newinc/history.php?start=00000";
  // CSV header for SSQ data
  String SSQ_CSV_HEADER = "期数,红球_2,红球_3,红球_3,红球_4,红球_5,红球_6,蓝球";
  // Filename for SSQ data CSV
  String SSQ_DATA_CSV = "ssq_data.csv";
  // File path for SSQ model checkpoint
  String SSQ_MORTAL_PTH = "ssq_mortal.pth";
}
