package sdk.fuck.lottery.domain.dataobject;

import lombok.Data;

@Data
public class SsqDO implements LotteryData {
  String number;
  String red01;
  String red02;
  String red03;
  String red04;
  String red05;
  String red06;
  String blue;

  @Override
  public String toCsvLine() {
    return String.join(",", getNumber(), getRed01(), getRed02(), getRed03(), getRed04(), getRed05(), getRed06(), getBlue());
  }
}
