import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import io.swagger.client.model.LiftRide;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class rmqConsumer {
  private final static String QUEUE_NAME = "skierQueue";
  //private final static String RMQ_IP_ADDRESS = "localhost";
  private final static String RMQ_IP_ADDRESS = "35.91.190.94";
  private Map<String, String> map = new ConcurrentHashMap<>();
  public static void main(String[] args) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(RMQ_IP_ADDRESS);
    factory.setUsername("jasonmax");
    factory.setPassword("r8cLhJUecQhV7DXdekKn");
    factory.setPort(5672);
    final Connection connection = factory.newConnection();

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


            //Map<String, LiftRide> resultMap = gson.fromJson(message, Map.class);
            //LiftRide ride = gson.fromJson(message, LiftRide.class);
//            for (String liftId: resultMap.keySet()) {
//
//            }

            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            System.out.println( "Callback thread ID = " + Thread.currentThread().getId());
            System.out.println(message);//ride.toString());
            //System.out.println( "Callback thread ID = " + Thread.currentThread().getId()
            //    + " Received '" + message + "'");

            //


          };
          // process messages
          channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
        } catch (IOException ex) {
          Logger.getLogger(rmqConsumer.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    };

    // start threads and block to receive messages
    final Integer THREAD_MAX = 10;
    for (int i = 0; i < THREAD_MAX; i++) {
      Thread receive = new Thread(runnable);
      receive.start();

    }
    //Thread recv2 = new Thread(runnable);
    //recv2.start();
  }

}
