package sdk.fuck.lottery.domain.dataobject;

public interface LotteryData {
  /**
   * Convert the lottery data object to a CSV line
   *
   * @return CSV line representing the lottery data
   */
  String toCsvLine();
}
