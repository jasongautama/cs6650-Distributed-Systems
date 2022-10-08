package part2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class Record {
    private ArrayList<String[]> records;
    //File csvFile;
    //FileWriter fw;
    private CSVPrinter printer;
    final private String PATH;
    private Integer throughput;
    private ArrayList<Long> latencyList;
    //MeanTime, 99th, etc
  public Record() {
    Timestamp now = Timestamp.from(Instant.now());
    String filename = now.toString() + "-report.csv";
    PATH = "./output/" + filename;
    records = new ArrayList<>();
    latencyList = new ArrayList<>();
    }

  public void addLog(Timestamp startTime, String requestType, long latency, int statusCode) {


    String[] record = new String[]{startTime.toString(), requestType, String.valueOf(latency),
        String.valueOf(statusCode)};
    this.records.add(record);
    latencyList.add(latency);
  }


  public void writeLogToCSV() throws IOException {

    printer = new CSVPrinter(new FileWriter(PATH), CSVFormat.DEFAULT);
    for (String[] log: records) {
      printer.printRecord(log);
    }

    printer.flush();
    printer.close();
  }

  public String getReport() {
    Collections.sort(this.latencyList);
    //create String + call the getters
    String report = " ==== CLIENT PART 2 ====\n" +
        "mean response time: " + getMeanResponseTime() + "ms\n" +
        "median response time: " + getMedianResponseTime() + "ms\n" +
        "throughput: " + getThroughput() + "req/s\n" +
        "p99 (99th percentile): " + getP99() + "ms\n" +
        "min response time: " + getMinResponseTime() + "ms\n" +
        "max response time: " + getMaxResponseTime() + "ms\n";
    return report;
  }
  public long getMeanResponseTime() {
    Long result = 0L;
    for (int i = 0; i < latencyList.size(); i++) {
      result += latencyList.get(i);
    }

    return result / latencyList.size();
  }

  public long getMedianResponseTime() {
    int median = latencyList.size() / 2;
    return latencyList.get(median);
  }

  public void setThroughput(Integer throughput) {
    this.throughput = throughput;
  }

  public Integer getThroughput() {
    return this.throughput;
  }

  public long getP99() {

    Integer index = (int) Math.round(latencyList.size() * 0.95);
    return latencyList.get(index);
  }

  public long getMinResponseTime() {
    return latencyList.get(0);
  }

  public long getMaxResponseTime() {
    Integer size = latencyList.size();
    return latencyList.get(size - 1);
  }

}
