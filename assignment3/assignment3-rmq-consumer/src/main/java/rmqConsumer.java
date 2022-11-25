import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class rmqConsumer {
  private final static String QUEUE_NAME = "skierQueue";
  private final static String RMQ_IP_ADDRESS = "172.31.24.235"; //private IP
  //private final static String RMQ_IP_ADDRESS = "35.88.136.222";
  private final static String USERNAME = "jasonmax";
  private final static Integer PORT = 5672;
  private final static String PASSWORD = "r8cLhJUecQhV7DXdekKn";

  public static void main(String[] args) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(RMQ_IP_ADDRESS);
    factory.setUsername(USERNAME);
    factory.setPassword(PASSWORD);
    factory.setPort(PORT);
    final Connection connection = factory.newConnection();
    SkierRedis redis = new SkierRedis();

    final Integer NUM_THREADS = 1500;
    final CountDownLatch latch = new CountDownLatch(NUM_THREADS);
    Runnable runnable = new Runnable() {
      @Override
      public void run() {

        final Gson gson = new Gson();
        try {
          final Channel channel = connection.createChannel();
          channel.queueDeclare(QUEUE_NAME, true, false, false, null);
          // max one message per receiver
          channel.basicQos(1);
          System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            //get message body (which is liftRide)
            String message = new String(delivery.getBody(), "UTF-8");
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            System.out.println( "Callback thread ID = " + Thread.currentThread().getId());            SkierData result = gson.fromJson(message, SkierData.class);
            //send SkierData data to redis
            Boolean isSuccess = redis.writeToRedis(result);
            if (!isSuccess) {
              System.out.println("Failed to write to Redis!");
            } else {
              System.out.println(result.toString());
            }

          };
          // process messages
          channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
        } catch (IOException ex) {
          Logger.getLogger(rmqConsumer.class.getName()).log(Level.SEVERE, null, ex);
        }
        //latch.countDown();
      }
    };

    // start threads and block to receive messages
    for (int i = 0; i < NUM_THREADS; i++) {
      Thread receive = new Thread(runnable);
      receive.start();
    }
    latch.await();
  }

}
