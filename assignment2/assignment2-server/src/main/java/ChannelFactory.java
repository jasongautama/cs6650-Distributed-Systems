import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ChannelFactory extends BasePooledObjectFactory<Channel> {

  //valid RMQ connection
  private final Connection connection;
  // used to count created channels for debugging
  private int count;

  public ChannelFactory(Connection connection) {
    this.connection = connection;
    this.count = 0;
  }

  @Override
  synchronized public Channel create() throws Exception {
    count ++;
    Channel chan = connection.createChannel();
    
    // Uncomment the line below to validate the expected number of channels are being created
    // System.out.println("Channel created: " + count);
    return chan;

  }

  @Override
  public PooledObject<Channel> wrap(Channel channel) {
    //System.out.println("Wrapping channel");
    /*
    DefaultPooledObject(T object) - https://commons.apache.org/proper/commons-pool/apidocs/org/apache/commons/pool2/impl/DefaultPooledObject
      - Def: Creates a new instance that wraps the provided object so that the pool can
                track the state of the pooled object.
    */
    return new DefaultPooledObject<>(channel);
  }

  public int getChannelCount() {
    return count;
  }

}
