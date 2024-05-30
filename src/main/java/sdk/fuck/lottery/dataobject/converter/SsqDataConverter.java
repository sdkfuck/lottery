package sdk.fuck.lottery.dataobject.converter;

import sdk.fuck.lottery.dataobject.SsqDO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Converter class for transforming SsqDO objects into a format suitable for LSTM network training.
 */
public class SsqDataConverter {

  // Range of red balls in Double Color Ball: 01 - 33
  private static final int RED_BALL_MIN = 1;
  private static final int RED_BALL_MAX = 33;
  // Range of blue balls in Double Color Ball: 01 - 16
  private static final int BLUE_BALL_MIN = 1;
  private static final int BLUE_BALL_MAX = 16;

  private static final Random RANDOM = new Random();

  // Convert red ball number to integer
  private static int convertRedBallToInt(String redBall) {
    return Integer.parseInt(redBall);
  }

  // Convert blue ball number to integer
  private static int convertBlueBallToInt(String blueBall) {
    return Integer.parseInt(blueBall);
  }

  // Method to ensure unique red ball numbers
  public static void ensureUniqueRedBalls(int[] redBalls) {
    Set<Integer> redBallsSet = new HashSet<>();
    for (int i = 0; i < redBalls.length; i++) {
      while (redBallsSet.contains(redBalls[i])) {
        redBalls[i] = RANDOM.nextInt(RED_BALL_MAX - RED_BALL_MIN + 1) + RED_BALL_MIN;
      }
      redBallsSet.add(redBalls[i]);
    }
  }

  // Convert SsqDO objects to a format suitable for training LSTM networks
  public static List<double[]> convertSsqToLstmInput(List<SsqDO> ssqData) {
    List<double[]> lstmInput = new ArrayList<>();
    for (SsqDO ssq : ssqData) {
      double[] input = new double[7]; // 7 input features: red balls 01-06, blue ball

      int[] redBalls = new int[6];

      // Get red ball numbers
      redBalls[0] = convertRedBallToInt(ssq.getRed01());
      redBalls[1] = convertRedBallToInt(ssq.getRed02());
      redBalls[2] = convertRedBallToInt(ssq.getRed03());
      redBalls[3] = convertRedBallToInt(ssq.getRed04());
      redBalls[4] = convertRedBallToInt(ssq.getRed05());
      redBalls[5] = convertRedBallToInt(ssq.getRed06());

      // Ensure unique red ball numbers
      ensureUniqueRedBalls(redBalls);

      // Set red ball number features
      input[0] = (redBalls[0] - RED_BALL_MIN) / (double) (RED_BALL_MAX - RED_BALL_MIN);
      input[1] = (redBalls[1] - RED_BALL_MIN) / (double) (RED_BALL_MAX - RED_BALL_MIN);
      input[2] = (redBalls[2] - RED_BALL_MIN) / (double) (RED_BALL_MAX - RED_BALL_MIN);
      input[3] = (redBalls[3] - RED_BALL_MIN) / (double) (RED_BALL_MAX - RED_BALL_MIN);
      input[4] = (redBalls[4] - RED_BALL_MIN) / (double) (RED_BALL_MAX - RED_BALL_MIN);
      input[5] = (redBalls[5] - RED_BALL_MIN) / (double) (RED_BALL_MAX - RED_BALL_MIN);
      // Set blue ball number feature
      input[6] = (convertBlueBallToInt(ssq.getBlue()) - BLUE_BALL_MIN) / (double) (BLUE_BALL_MAX - BLUE_BALL_MIN);

      lstmInput.add(input);
    }
    return lstmInput;
  }
}
