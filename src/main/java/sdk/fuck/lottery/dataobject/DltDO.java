package sdk.fuck.lottery.dataobject;

import lombok.Data;

/**
 * Data Object for Lotto (DLT)
 */
@Data
public class DltDO implements LotteryData {
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
  // Blue Ball 01
  String blue01;
  // Blue Ball 02
  String blue02;

  @Override
  public String toCsvLine() {
    return String.join(",", getDateNo(), getRed01(), getRed02(), getRed03(), getRed04(), getRed05(), getBlue01(), getBlue02());
  }
}
