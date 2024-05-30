package sdk.fuck.lottery.algorithm.lstm;

import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.stereotype.Service;
import sdk.fuck.lottery.algorithm.SsqAlgorithm;
import sdk.fuck.lottery.common.constants.SsqConstants;
import sdk.fuck.lottery.dataobject.SsqDO;
import sdk.fuck.lottery.dataobject.converter.SsqDataConverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for LSTM-based SSQ lottery number prediction.
 * <p>
 * This service provides methods to train a Long Short-Term Memory (LSTM) network for predicting SSQ lottery numbers,
 * and to use the trained model to make predictions.
 */
@Slf4j
@Service
public class SsqLstmAlgorithm extends SsqAlgorithm {

  /**
   * Trains the LSTM model with the provided SSQ lottery data and saves the model.
   *
   * @param trainingData The data used for training. Each element represents a training instance.
   */
  @Override
  public void trainAndSaveModel(List<SsqDO> trainingData) {
    List<double[]> input = SsqDataConverter.convertSsqToLstmInput(trainingData);

    // Convert input data to INDArray format
    int numSamples = input.size();
    int numFeatures = 7;

    try (INDArray inputArray = Nd4j.create(numSamples, numFeatures)) {
      for (int i = 0; i < numSamples; i++) {
        inputArray.putRow(i, Nd4j.create(input.get(i)));
      }

      // Reshape input data to 3D format: [miniBatchSize, numFeatures, timeSeriesLength]
      try (INDArray input3D = inputArray.reshape(numSamples, numFeatures, 1)) {
        // Create DataSet object
        DataSet dataSet = new DataSet(input3D, input3D);

        // Define LSTM network structure
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
          .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
          .updater(new Adam(0.001))
          .list()
          .layer(new LSTM.Builder().nIn(numFeatures).nOut(50)
                                   .activation(Activation.TANH).build())
          .layer(new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                   .activation(Activation.IDENTITY).nIn(50).nOut(numFeatures).build())
          .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(1));

        // Normalize data
        NormalizerMinMaxScaler normalizer = new NormalizerMinMaxScaler(0, 1);
        normalizer.fit(dataSet);
        normalizer.transform(dataSet);

        // Train the model
        for (int i = 0; i < numSamples; i++) {
          net.fit(dataSet);
        }

        // Save the model
        String projectRoot = Paths.get("").toAbsolutePath().toString();
        File dataDir = new File(projectRoot, "models");
        if (!dataDir.exists() && !dataDir.mkdirs()) {
          log.error("Failed to create data directory.");
          return;
        }
        File modelFile = new File(dataDir, SsqConstants.SSQ_MORTAL_PTH);
        try {
          net.save(modelFile, true);
        } catch (IOException e) {
          log.error("Failed to save model", e);
        }
      }
    }
  }

  /**
   * Predicts SSQ lottery numbers using the specified model file path.
   *
   * @param modelFilePath The file path where the trained model is stored.
   * @param numPredictions The number of predictions to make.
   * @return The prediction results. Each element represents a predicted SSQ lottery number.
   */
  @Override
  public List<SsqDO> predictByModel(String modelFilePath, int numPredictions) {
    List<SsqDO> predictions = new ArrayList<>();
    try {
      MultiLayerNetwork net = MultiLayerNetwork.load(new File(modelFilePath), true);
      log.info("Model loaded successfully from {}", modelFilePath);

      // Create an empty input data
      int numFeatures = 7;
      INDArray input = Nd4j.zeros(1, numFeatures, 1);

      // Use the model to make predictions
      for (int i = 0; i < numPredictions; i++) {
        INDArray output = net.rnnTimeStep(input);
        log.info("Prediction output: {}", output);

        int numSamples = (int) output.size(0);
        for (int j = 0; j < numSamples; j++) {
          INDArray row = output.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(j));
          int[] redBalls = new int[6];
          for (int k = 0; k < 6; k++) {
            redBalls[k] = (int) Math.round(row.getDouble(k) * 32 + 1);
          }

          // Ensure unique red ball numbers
          SsqDataConverter.ensureUniqueRedBalls(redBalls);

          SsqDO prediction = new SsqDO();
          prediction.setRed01(String.format("%02d", redBalls[0]));
          prediction.setRed02(String.format("%02d", redBalls[1]));
          prediction.setRed03(String.format("%02d", redBalls[2]));
          prediction.setRed04(String.format("%02d", redBalls[3]));
          prediction.setRed05(String.format("%02d", redBalls[4]));
          prediction.setRed06(String.format("%02d", redBalls[5]));
          prediction.setBlue(String.format("%02d", (int) Math.round(row.getDouble(6) * 15 + 1)));
          predictions.add(prediction);
        }
      }
    } catch (IOException e) {
      log.error("Failed to load model", e);
    }
    return predictions;
  }

  /**
   * Reads SSQ lottery data from a CSV file and parses it into a list of SsqDO objects.
   *
   * @param filePath The file path of the CSV file.
   * @return The list of parsed SsqDO objects.
   */
  public List<SsqDO> readCsvData(String filePath) {
    List<SsqDO> dataList = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line;
      // Skip the header
      br.readLine();
      while ((line = br.readLine()) != null) {
        String[] values = line.split(",");
        SsqDO data = new SsqDO();
        data.setDateNo(values[0]);
        data.setRed01(values[1]);
        data.setRed02(values[2]);
        data.setRed03(values[3]);
        data.setRed04(values[4]);
        data.setRed05(values[5]);
        data.setRed06(values[6]);
        data.setBlue(values[7]);
        dataList.add(data);
      }
    } catch (IOException e) {
      log.error("Failed to read CSV file", e);
    }
    return dataList;
  }

  /**
   * Determines the next date number based on the data in the CSV file.
   *
   * @param filePath The file path of the CSV file.
   * @return The next date number as a string.
   */
  public String getNextDateNo(String filePath) {
    String nextDateNo = null;
    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      // Skip the first line (header)
      br.readLine();
      // Read the second line (first data row)
      String secondLine = br.readLine();
      if (secondLine != null) {
        // Parse the first data row to get the current date number
        String[] values = secondLine.split(",");
        String currentDateNo = values[0];
        // Convert the current date number to an integer and increment by 1 to get the next date number
        int currentIntDateNo = Integer.parseInt(currentDateNo);
        int nextIntDateNo = currentIntDateNo + 1;
        nextDateNo = String.valueOf(nextIntDateNo);
      }
    } catch (IOException | NumberFormatException e) {
      log.error("Failed to get next date number", e);
    }
    return nextDateNo;
  }
}
