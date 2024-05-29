package sdk.fuck.lottery.dataobject;

import lombok.Data;

@Data
public class DltDO implements LotteryData {
  String dateNo;
  String red01;
  String red02;
  String red03;
  String red04;
  String red05;
  String blue01;
  String blue02;

  @Override
  public String toCsvLine() {
    return String.join(",", getDateNo(), getRed01(), getRed02(), getRed03(), getRed04(), getRed05(), getBlue01(), getBlue02());
  }
}
