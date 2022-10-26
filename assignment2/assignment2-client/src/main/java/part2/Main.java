package part2;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import part1.SkierClient;
import part1.SkierData;
import part1.SkierProducer;


public class Main {

  public static void main(String[] args) {

    //Variable Declaration
    final Integer TOTAL_REQUEST = 200000;
    BlockingQueue<SkierData> buffer = new LinkedBlockingQueue<>();
    BlockingQueue<SkierClient> resultBuffer = new LinkedBlockingQueue<>();
    Integer successfulRequestCount = 0;
    Integer failedRequestCount = 0;
    Record recordLogs = new Record();


    //create Producer
    (new Thread(new SkierProducer(TOTAL_REQUEST, 1, buffer))).start();

    Timestamp start = Timestamp.from(Instant.now()); // Start the time

    // ######################### PHASE 1 #########################
    //---------------------------------------------------------------

    Integer phaseOneThread = 32; //32
    Integer phaseOneRequestPerThread = 1000; //1000
    //Add Status Code statistic (2xx, 4xx) to results
    System.out.println();
    System.out.println("### Start of Phase 1 ###");
    executePhase(phaseOneThread, phaseOneRequestPerThread, buffer, resultBuffer, recordLogs);
    System.out.println("### End of Phase 1 ###");
    System.out.println();
    //---------------------------------------------------------------

    // ######################### PHASE 2 #########################
    //---------------------------------------------------------------

    Integer phaseTwoThread = 100;
    Integer phaseTwoRequestPerThread = 840;

    System.out.println("### Start of Phase 2 ###");
    executePhase(phaseTwoThread, phaseTwoRequestPerThread, buffer, resultBuffer, recordLogs);
    System.out.println("### End of Phase 2 ###");
    System.out.println();
    //---------------------------------------------------------------

    // ######################### PHASE 3 #########################
    //---------------------------------------------------------------
    Integer phaseThreeThread = 100;
    Integer phaseThreeRequestPerThread = 840;

    System.out.println("### Start of Phase 3 ###");
    executePhase(phaseThreeThread, phaseThreeRequestPerThread, buffer, resultBuffer, recordLogs);
    System.out.println("### End of Phase 3 ###");
    System.out.println();
    //---------------------------------------------------------------

    Timestamp end = Timestamp.from(Instant.now());

    try {
      while (resultBuffer.size() > 0) {
        SkierClient client = resultBuffer.take();
        successfulRequestCount += client.getSuccessfulReq();
        failedRequestCount += client.getFailedReq();
      }
    } catch (InterruptedException e) {
      System.out.println("Error on part2.Main! " + e.getMessage());
    }

    try {
      recordLogs.writeLogToCSV();
    } catch (IOException e) {
      System.out.println("Unable to write to CSV\n" + e.getStackTrace());
    }

    // ==== Client 2 Report ====
    Long millisecond = end.getTime() - start.getTime();
    Long second = millisecond / 1000;
    Integer throughput = Math.toIntExact((successfulRequestCount + failedRequestCount) / second);
    recordLogs.setThroughput(throughput);
    String reportClient2 = recordLogs.getReport();
    System.out.println(reportClient2);
  }

  private static void executePhase(Integer numOfThreads, Integer requestPerThread,
      BlockingQueue<SkierData> buffer, BlockingQueue<SkierClient> resultBuffer, Record recordLogs) {

    CountDownLatch latch_phase = new CountDownLatch(numOfThreads);
    try {
      ExecutorService pool = Executors.newFixedThreadPool(numOfThreads);

      System.out.println("ThreadPool created and executing with numOfThread = " + numOfThreads +
          " & requestPerThread = " + requestPerThread);

      for (int i = 0; i < numOfThreads; i++) {
        //System.out.println("inside Loop.. tid:" + i);
        SkierClient client = new SkierClient(buffer, requestPerThread, latch_phase, recordLogs);
        pool.execute(client);
        resultBuffer.add(client);
      }

      latch_phase.await();
      pool.shutdown();


    } catch (InterruptedException ie) {
      System.out.println("Failed at retrieving resultBuffer()..\n" + ie.getMessage());
    }

  }
}
