import java.util.Arrays;
import java.util.HashSet;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

public class Selection {
    private int ROWS = 0;
    private int COLS = 0;
    private int curLevelWrong = 0;
    private final int MIN_SAMPLE_ROWS = 2000;
    Random rand;

    Selection() {
        rand = new Random(System.currentTimeMillis());
    }

    // This method is used to copy the array
    private double[][] deepcopy(double[][] data) {
        double[][] copy = Arrays.stream(data).map(double[]::clone).toArray(double[][]::new);
        return copy;
    }

    // This method is used for copying the Hashset
    private HashSet<Integer> deepcopy(HashSet<Integer> hs) {
        HashSet<Integer> copy = new HashSet<Integer>();
        for (int val : hs) {
            copy.add(val);
        }
        return copy;
    }

    // In this method, we update every column apart from the one in the current set
    // and featureToAdd to zero.

    private double[][] crossValidation(double[][] data, HashSet<Integer> currentSet, int featureToAdd) {
        double[][] tempData = deepcopy(data);
        for (int i = 1; i < COLS; i++) {
            if (!currentSet.contains(i) && i != featureToAdd) {
                for (int j = 0; j < ROWS; j++) {
                    tempData[j][i] = 0;
                }
            }
        }

        return tempData;
    }

    // Calculating Euclidian distance
    private double calculateDistance(double[] r1, double[] r2, int startIdx) {
        double distance = 0;
        for (int i = startIdx; i < r1.length; i++) {
            double diff = (r1[i] - r2[i]) * (r1[i] - r2[i]);
            distance += diff;
        }
        return distance;
    }

    // Converting the feature into a string
    private String getFeatureStr(HashSet<Integer> s, int num) {
        StringBuilder sb = new StringBuilder();
        sb.append(Arrays.toString(s.toArray()).replaceAll("\\[", "").replaceAll("\\]", ""));
        if (num != -1) {
            if (s.size() > 0) {
                sb.append(", ");
            }
            sb.append(num);
        }
        return "{" + sb.toString() + "}";
    }

    // In this method, we check if the distance between two features is minimun and
    // if it does not match with the class label it is classified as wrongCount and
    // loss(accuracy) is calculated
    private double calculateAccuracy(double[][] data, HashSet<Integer> currentSet, int featureToAdd) {
        double[][] tempData = crossValidation(data, currentSet, featureToAdd);
        int wrongCount = 0;
        for (int i = 0; i < ROWS; i++) {
            double curLabel = tempData[i][0];
            double minDist = Double.MAX_VALUE;
            double checkLabel = 0;

            for (int j = 0; j < ROWS; j++) {
                if (i != j) {
                    double curDistance = calculateDistance(tempData[i], tempData[j], 1);
                    if (curDistance <= minDist) {
                        minDist = curDistance;
                        checkLabel = data[j][0];
                    }
                }
            }
            if (curLabel != checkLabel) {
                ++wrongCount;
            }
            if (wrongCount > curLevelWrong) {
                return -1.0;
            }
        }

        if (wrongCount < curLevelWrong) {
            curLevelWrong = wrongCount;
        }
        double loss = Math.round(((ROWS - wrongCount) / (double) ROWS) * 10000.0) / 10000.0;
        return loss;
    }

    // https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle

    // For random sampling of data
    private double[][] randomSampleData(double[][] data) {
        double[][] tmpData = deepcopy(data);
        for (int i = ROWS - 1; i > 0; i--) {
            int randomIdx = rand.nextInt(i + 1);
            double[] tmpRow = tmpData[i];
            tmpData[i] = tmpData[randomIdx];
            tmpData[randomIdx] = tmpRow;
        }

        double[][] sampledData = new double[MIN_SAMPLE_ROWS][COLS];
        for (int i = 0; i < MIN_SAMPLE_ROWS; i++) {
            sampledData[i] = Arrays.copyOf(tmpData[i], COLS);
        }
        return sampledData;
    }

    // Forward selection - Adding each feature and checking the accuracy, choosing
    // the best features(s)
    public void forwardSelection(double[][] data, String outputPath, boolean writeToFile) throws IOException {
        PrintStream stream = null;
        if (writeToFile) {
            File f = new File(outputPath);
            stream = new PrintStream(f);
            System.setOut(stream);
        }

        long startTime = System.currentTimeMillis(), endTime = -1;
        double timeDiff = -1;
        ROWS = data.length;
        COLS = data[0].length;

        if (ROWS > MIN_SAMPLE_ROWS) {
            data = randomSampleData(data);
            ROWS = data.length;
            COLS = data[0].length;
        }

        HashSet<Integer> currentSet = new HashSet<Integer>();
        double maxAccuracy = 0;

        String bestFeatureSet = "";

        for (int i = 0; i < COLS - 1; i++) {
            double curMaxAccuracy = 0;
            int featureToAdd = -1;
            curLevelWrong = Integer.MAX_VALUE;

            System.out.printf("On level %d of search tree\n", i + 1);

            for (int j = 0; j < COLS - 1; j++) {
                // Current set holds the desirable features. Check if the set does not already
                // contain a feature
                // And calculate the accuracy with the feature.
                if (!currentSet.contains(j + 1)) {
                    System.out.printf("\tConsider adding feature %d\n", j + 1);
                    double accuracy = calculateAccuracy(data, currentSet, j + 1);
                    String featuresStr = getFeatureStr(currentSet, j + 1);
                    if (accuracy == -1.0) {
                        System.out.printf("\tAccuracy after using feature(s)%s is lower than %.4f\n",
                                featuresStr,
                                curMaxAccuracy);
                    } else {
                        System.out.printf("\tAccuracy after using feature(s)%s is %.4f\n",
                                featuresStr, accuracy);
                    }
                    // Update the accuracies if it is greater than current accuracy
                    if (accuracy > curMaxAccuracy) {
                        curMaxAccuracy = accuracy;
                        System.out.printf("\tUpdated accuracy: %.4f\n", curMaxAccuracy);
                        featureToAdd = j + 1;
                    }
                    if (accuracy > maxAccuracy) {
                        maxAccuracy = accuracy;
                        bestFeatureSet = getFeatureStr(currentSet, featureToAdd);
                    }
                }
            }

            if (featureToAdd != -1) {
                currentSet.add(featureToAdd);
            }
            String featuresStr = getFeatureStr(currentSet, -1);
            System.out.printf("Feature set %s was best, accuracy is %.4f\n", featuresStr, curMaxAccuracy);
        }
        endTime = System.currentTimeMillis();
        timeDiff = (endTime - startTime) / 1000.0;
        System.out.printf("Time taken: %.2f seconds\n", timeDiff);
        System.out.printf("Best features: %s\n", bestFeatureSet);
        System.out.printf("Best accuracy: %.4f\n", maxAccuracy);
        if (stream != null) {
            stream.close();
        }
    }

    public void backwardElimination(double[][] data, String outputPath, boolean writeToFile) throws IOException {
        PrintStream stream = null;
        if (writeToFile) {
            File f = new File(outputPath);
            stream = new PrintStream(f);
            System.setOut(stream);
        }
        long startTime = System.currentTimeMillis(), endTime = -1;
        double timeDiff = -1;
        ROWS = data.length;
        COLS = data[0].length;

        if (ROWS > MIN_SAMPLE_ROWS) {
            data = randomSampleData(data);
            ROWS = data.length;
            COLS = data[0].length;
        }

        HashSet<Integer> currentSet = new HashSet<Integer>();
        double maxAccuracy = 0;

        // Current set has all the features
        String bestFeatureSet = "";
        for (int i = 1; i < COLS; i++) {
            currentSet.add(i);
        }

        // Checking accuracy and which feature to remove based on low accuracy
        for (int i = 0; i < COLS - 1; i++) {
            int featureToRemove = -1;
            double curLevelBestAccuracy = 0;
            String curLevelBestFeatures = "";
            curLevelWrong = Integer.MAX_VALUE;

            System.out.printf("On level %d of search tree\n", i + 1);

            for (int j = 0; j < COLS - 1; j++) {
                if (currentSet.contains(j + 1)) {
                    System.out.printf("\tConsider removing feature %d\n", j + 1);
                    HashSet<Integer> tempFeatureSet = deepcopy(currentSet);
                    tempFeatureSet.remove(j + 1);

                    double curAccuracy = calculateAccuracy(data, tempFeatureSet, 0);
                    System.out.printf("\t%.4f\n", curAccuracy);
                    String featuresStr = getFeatureStr(tempFeatureSet, -1);

                    // if (i + 1 == COLS - 1) {
                    // System.out.printf("\tAccuracy at level %d is %.4f\n", i + 1, curAccuracy);
                    // }

                    if (i == 0 && j == 0) {
                        maxAccuracy = curAccuracy;
                    }
                    if (j == 0) {
                        curLevelBestFeatures = featuresStr;
                    }
                    if (curAccuracy == -1.0 || curAccuracy == 0.0) {
                        System.out.printf("\tAccuracy after using feature(s)%s is lower than %.4f\n",
                                featuresStr,
                                curLevelBestAccuracy);
                    } else {
                        System.out.printf("\tAccuracy after using feature(s)%s is %.4f\n",
                                featuresStr, curAccuracy);
                    }
                    // Updating accuracies and removing features
                    if (curAccuracy > curLevelBestAccuracy) {
                        curLevelBestAccuracy = curAccuracy;
                        curLevelBestFeatures = featuresStr;
                        System.out.printf("\tUpdated accuracy: %.4f\n", curLevelBestAccuracy);
                        featureToRemove = j + 1;
                    }

                    if (curLevelBestAccuracy > maxAccuracy) {
                        maxAccuracy = curLevelBestAccuracy;
                        bestFeatureSet = curLevelBestFeatures;
                    }
                }
            }
            if (featureToRemove != -1) {
                currentSet.remove(featureToRemove);
            }
            // String featuresStr = getFeatureStr(currentSet, -1);
            System.out.printf("Feature set %s was best, accuracy is %.4f\n", curLevelBestFeatures,
                    curLevelBestAccuracy);
        }
        endTime = System.currentTimeMillis();
        timeDiff = (endTime - startTime) / 1000.0;
        System.out.printf("Time taken: %.2f seconds\n", timeDiff);
        System.out.printf("Best features: %s\n", bestFeatureSet);
        System.out.printf("Best accuracy: %.4f\n", maxAccuracy);
        if (stream != null) {
            stream.close();
        }
    }
}