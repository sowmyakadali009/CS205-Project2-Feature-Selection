import java.io.*;
import java.util.ArrayList;

public class Utils {
    public static double[][] readData(String filename) {
        BufferedReader br = null;
        ArrayList<ArrayList<Double>> dataList = new ArrayList<ArrayList<Double>>();
        try {
            br = new BufferedReader(new FileReader(filename));
            String line = br.readLine();

            while (line != null) {
                if (line.trim().length() > 0) {
                    ArrayList<Double> curRow = new ArrayList<Double>();
                    line = line.trim();

                    for (String part : line.split("\\s+")) {
                        curRow.add(Double.valueOf(part));
                    }
                    dataList.add(curRow);
                }
                line = br.readLine();
            }
            br.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        double[][] data = dataList.stream().map(row -> row.stream().mapToDouble(Double::doubleValue).toArray())
                .toArray(double[][]::new);
        return data;
    }
}