package sdk.fuck.lottery.algorithm;

import java.util.List;

/**
 * Lottery algorithm interface.
 * <p>
 * This interface defines methods for training and predicting lottery numbers.
 * Specific algorithm implementations can define the training and prediction logic for lottery numbers by implementing this interface.
 *
 * @param <T> Generic parameter representing the type of lottery data.
 */
public interface LotteryAlgorithm<T> {

  /**
   * Train the algorithm with the provided data and save the model.
   * <p>
   * This method takes a set of training data and uses it to train the lottery prediction model,
   * then saves the trained model to the specified location.
   *
   * @param trainingData The data used for training. Each element represents a training instance.
   */
  void trainAndSaveModel(List<T> trainingData);

  /**
   * Predict using the specified model file path.
   * <p>
   * This method takes a model file path, loads the model, and uses it to predict the input data.
   *
   * @param modelFilePath The file path where the trained model is stored.
   * @param numPredictions The number of predictions to make.
   * @return The prediction results. Each element represents the result of a prediction instance.
   */
  List<T> predictByModel(String modelFilePath, int numPredictions);
}
