package sdk.fuck.lottery.dataobject;

import lombok.Data;

/**
 * Data Object for Double Color Ball (SSQ)
 */
@Data
public class SsqDO implements LotteryData {
  // Issue number
  String dateNo;
  // Red Ball 01
  String red01;
  // Red Ball 02
  String red02;
  // Red Ball 03
  String red03;
  // Red Ball 04
  String red04;
  // Red Ball 05
  String red05;
  // Red Ball 06
  String red06;
  // Blue Ball
  String blue;

  @Override
  public String toCsvLine() {
    return String.join(",", getDateNo(), getRed01(), getRed02(), getRed03(), getRed04(), getRed05(), getRed06(), getBlue());
  }
}
