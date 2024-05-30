package sdk.fuck.lottery.dataobject;

/**
 * Interface for Lottery Data Objects
 */
public interface LotteryData {
  /**
   * Convert the lottery data object to a CSV line
   *
   * @return CSV line representing the lottery data
   */
  String toCsvLine();
}
