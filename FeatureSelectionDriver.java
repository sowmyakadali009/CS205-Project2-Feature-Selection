import java.io.IOException;
import java.util.Scanner;

public class FeatureSelectionDriver {
    public static void main(String[] args) {

        // This is the Driver program for the nearest neighbors algorithm.
        Scanner sc = new Scanner(System.in);
        System.out.println("Welcome to feature selection algorithm: ");
        System.out.println("Enter the filepath: ");
        String inputFile = sc.nextLine();
        // String filename = "/Users/sowmyakadali/Downloads/CS170_small_Data__11.txt";

        System.out.println("Algorithms available:");
        System.out.println("1. Forward selection");
        System.out.println("2. Backward Elimination");
        System.out.println("Enter your choice: ");
        int algoChoice = Integer.valueOf(sc.nextLine());

        if (algoChoice < 1 || algoChoice > 2) {
            System.out.println("Invalid selection");
            System.exit(-1);
        }

        // Choice of output mode

        System.out.println("Output mode: ");
        System.out.println("1. Console");
        System.out.println("2. File");
        System.out.println("Enter your choice: ");
        int outputChoice = Integer.valueOf(sc.nextLine());
        if (outputChoice < 1 || outputChoice > 2) {
            System.out.println("Invalid selection");
            System.exit(-1);
        }

        try {
            String outputPath = "";

            double[][] data = Utils.readData(inputFile);
            Selection sl = new Selection();
            boolean writeToFile = (outputChoice == 2);
            if (writeToFile) {
                if (algoChoice == 1) {
                    outputPath = inputFile.replaceAll(".txt", "_output_forward.txt");
                } else if (algoChoice == 2) {
                    outputPath = inputFile.replaceAll(".txt", "_output_backward.txt");
                }
            }
            if (algoChoice == 1) {
                sl.forwardSelection(data, outputPath, writeToFile);
            } else if (algoChoice == 2) {
                sl.backwardElimination(data, outputPath, writeToFile);
            }
            sc.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }
}