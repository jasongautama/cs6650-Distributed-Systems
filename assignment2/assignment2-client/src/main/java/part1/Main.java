package part1;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
  public static void main(String[] args) {

    //Variable Declaration
    BlockingQueue<SkierData> buffer = new LinkedBlockingQueue<>();
    BlockingQueue<SkierClient> resultBuffer = new LinkedBlockingQueue<>();
    Integer successfulRequestCount = 0;
    Integer failedRequestCount = 0;


    final Integer NUM_OF_REQUEST = 320;//3200;
    final Integer NUM_OF_THREADS = 1;
    //create Producer
    (new Thread(new SkierProducer(NUM_OF_REQUEST, NUM_OF_THREADS, buffer))).start();

    Timestamp start = Timestamp.from(Instant.now()); // Start the time

    // ######################### PHASE 1 #########################
    //---------------------------------------------------------------

    Integer phaseOneThread = 32;
    Integer phaseOneRequestPerThread = 10;//100;
    //Add Status Code statistic (2xx, 4xx) to results
    System.out.println();
    System.out.println("### Start of Phase 1 ###");
    executePhase(phaseOneThread, phaseOneRequestPerThread, buffer, resultBuffer);
    System.out.println("### End of Phase 1 ###\n");
    //---------------------------------------------------------------

    // ######################### PHASE 2 #########################
    //---------------------------------------------------------------
//
//    Integer phaseTwoThread = 100;
//    Integer phaseTwoRequestPerThread = 840;
//
//    System.out.println("### Start of Phase 2 ###");
//    executePhase(phaseTwoThread, phaseTwoRequestPerThread, buffer, resultBuffer);
//    System.out.println("### End of Phase 2 ###");
//    System.out.println();
    //---------------------------------------------------------------

    // ######################### PHASE 3 #########################
    //---------------------------------------------------------------
//    Integer phaseThreeThread = 100;
//    Integer phaseThreeRequestPerThread = 840;
//
//    System.out.println("### Start of Phase 3 ###");
//    executePhase(phaseThreeThread, phaseThreeRequestPerThread, buffer, resultBuffer);
//    System.out.println("### End of Phase 3 ###");
//    System.out.println();
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

    System.out.println("==== CLIENT PART 1 REPORT ====");
    System.out.println("# of Successful Request:" + successfulRequestCount);
    System.out.println("# of Unsuccessful Request: " + failedRequestCount);
    Long millisecond = end.getTime() - start.getTime();
    Long second = millisecond / 1000;
    System.out.println("Time taken for all Phases to complete: " + millisecond + "ms");
    System.out.println("Throughput (req/s): " + (successfulRequestCount + failedRequestCount) /
        second + "req/s");
  }

  private static void executePhase(Integer numOfThreads, Integer requestPerThread,
      BlockingQueue<SkierData> buffer, BlockingQueue<SkierClient> resultBuffer) {

    CountDownLatch latch_phase = new CountDownLatch(numOfThreads);
    try {
      ExecutorService pool = Executors.newFixedThreadPool(numOfThreads);

      System.out.println("ThreadPool created and executing with numOfThread = " + numOfThreads +
          " & requestPerThread = " + requestPerThread);

      for (int i = 0; i < numOfThreads; i++) {
        //System.out.println("inside Loop.. tid:" + i);
        SkierClient client = new SkierClient(buffer, requestPerThread, latch_phase);
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
