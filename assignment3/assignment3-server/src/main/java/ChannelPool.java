import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChannelPool {
  // used to store and distribute channels
  private final BlockingQueue<Channel> pool;
  // fixed size pool
  private int capacity;
  // used to create channels
  private ChannelFactory factory;


  public ChannelPool(int maxSize, ChannelFactory factory) {
    this.capacity = maxSize;
    pool = new LinkedBlockingQueue<>(capacity);
    this.factory = factory;
    for (int i = 0; i < capacity; i++) {
      Channel chan;
      try {
        chan = this.factory.create();
        pool.put(chan);
      } catch (Exception ex) {
        Logger.getLogger(ChannelPool.class.getName()).log(Level.SEVERE, null, ex);
      }

    }
  }

  public Channel borrowObject() throws RuntimeException {

    try {
      return pool.take();
    } catch (InterruptedException e) {
      throw new RuntimeException("Error: no channels available" + e.toString());
    }
  }

  public void returnObject(Channel channel) {
    if (channel != null) {
      pool.add(channel);
    }
  }

  public void close() {
    // pool.close();
  }
}
