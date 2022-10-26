package part1;

import java.util.concurrent.BlockingQueue;

public class SkierProducer implements Runnable {

  private Integer numOfRequest;
  private Integer numOfThreads; //optional
  private BlockingQueue<SkierData> buffer;

  public SkierProducer(Integer numOfRequest, Integer numOfThreads, BlockingQueue buffer) {
    this.numOfRequest = numOfRequest;
    this.numOfThreads = numOfThreads;
    this.buffer = buffer;
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
    final Integer TOTAL_REQUEST = numOfRequest * numOfThreads;

    try {
      System.out.println("Produce part1.SkierData to Buffer...");
      for (int i = 0; i < TOTAL_REQUEST; i++) {
        buffer.put(new SkierData());
      }

      System.out.println("...Completed adding part1.SkierData to Buffer");
    } catch (InterruptedException ie) {
      System.out.println("Exception occurred in Producer..\n" + ie.getMessage());
    }
  }
}
