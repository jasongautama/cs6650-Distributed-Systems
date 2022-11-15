import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import io.swagger.client.model.LiftRide;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class rmqConsumer {
  private final static String QUEUE_NAME = "skierQueue";
  private final static String RMQ_IP_ADDRESS = "172.31.24.235"; //"52.37.80.29"; //private IP
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
    Map<Integer, LiftRide> resultMap = new ConcurrentHashMap<>();

    final Integer NUM_THREADS = 100;
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
            SkierData result = gson.fromJson(message, SkierData.class);

            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            System.out.println( "Callback thread ID = " + Thread.currentThread().getId());
            System.out.println(result.toString());
            resultMap.put(result.getSkierId(), result.getLiftRide());
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
