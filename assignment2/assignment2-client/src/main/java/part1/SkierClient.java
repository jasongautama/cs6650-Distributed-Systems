package part1;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import io.swagger.client.api.SkiersApi;
import io.swagger.client.*;
import part2.Record;

public class SkierClient implements Runnable {
  private Integer successfulReq;
  private Integer failedReq;
  private BlockingQueue<SkierData> buffer;
  private Integer numOfRequest;
  private CountDownLatch latch;
  private String url;

  private Record recordLogs;

  public Integer getSuccessfulReq() {
    return successfulReq;
  }

  public Integer getFailedReq() {
    return failedReq;
  }

  public Record getRecordLogs() {
    return recordLogs;
  }

  public SkierClient(BlockingQueue<SkierData> buffer,
      Integer numOfRequest, CountDownLatch latch, Record recordLogs) {
    this.successfulReq = 0;
    this.failedReq = 0;
    this.buffer = buffer;
    this.numOfRequest = numOfRequest;
    this.latch = latch;
    this.url = "http://localhost:8080/assignment2_server_war_exploded";
//    this.url = "http://ec2-34-222-0-117.us-west-2.compute.amazonaws.com:8080/"
//        + "assignment1-1.0-SNAPSHOT/skiers/";
    this.recordLogs = recordLogs;
  }

  public SkierClient(BlockingQueue<SkierData> buffer, Integer numOfRequest, CountDownLatch latch) {
    this(buffer, numOfRequest, latch, null);
  }

  /**
   * When an object implementing interface {@code Runnable} is used to create a thread, starting the
   * thread causes the object's {@code run} method to be called in that separately executing
   * thread.
   * <p>
   * The general contract of the method {@code run} is that it may take any action whatsoever.
   *
   * @see Thread#run()
   */
  @Override
  public void run() {
    try {
      for (int i = 0; i < numOfRequest; i++) {
        this.post(buffer.take());
      }
      latch.countDown();
    } catch (ApiException | InterruptedException e) {
      System.out.println("Error occurred in Client run()..\n" + e.getMessage());
    }
  }

  private void post(SkierData skier) throws ApiException {

    SkiersApi apiSkier = new SkiersApi();
    ApiClient apiClient = apiSkier.getApiClient();
    apiClient.setBasePath(url);
    apiClient.setConnectTimeout(500000);
    apiClient.setReadTimeout(500000);
    Integer failedCounter = 0;
    try {
      while (failedCounter < 5) {
        Timestamp start = Timestamp.from(Instant.now()); // Start the time
        System.out.println("resortId:" + skier.getResortId());
        ApiResponse<Void> resp = apiSkier.writeNewLiftRideWithHttpInfo(skier.getLiftRide(),
            skier.getResortId(), skier.getSeasonId(), skier.getDayId(), skier.getSkierId());
        Timestamp end = Timestamp.from(Instant.now()); // Start the time
        Long latency = end.getTime() - start.getTime();

        if (recordLogs != null) {
          recordLogs.addLog(start, "POST", latency, resp.getStatusCode());
        }
        if (resp.getStatusCode() == 200 || resp.getStatusCode() == 201) {
          this.successfulReq += 1;
          return;
        }

        failedCounter += 1;

      }
      throw new ApiException("Network is currently down");
    } catch (ApiException e) {
      System.out.println("FailedRequest!");
      e.printStackTrace();
    }

    this.failedReq += 1;
  }
}
