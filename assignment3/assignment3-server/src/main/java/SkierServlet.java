import com.google.gson.Gson;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.swagger.client.model.LiftRide;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import com.rabbitmq.client.Channel;

@WebServlet(name = "SkierServlet", value = "/skiers")
public class SkierServlet extends HttpServlet {

  private final static Integer INVALID_NUM = -1;
//  private final static String QUEUE_NAME = "skierQueue";
//  //private final static String RMQ_IP_ADDRESS = "172.31.24.235";
//  private final static String RMQ_IP_ADDRESS = "localhost";
//  private final static String USERNAME = "jasonmax";
//  //private final static String USERNAME = "guest";
//  private final static Integer PORT = 5672;
//  private final static String PASSWORD = "r8cLhJUecQhV7DXdekKn";
  //private final static String PASSWORD = "guest";
  private ChannelPool channelPool;


  /**
   * A convenience method which can be overridden so that there's no need to call
   * <code>super.init(config)</code>.
   *
   * <p>Instead of overriding , simply override
   * this method and it will be called by
   * <code>GenericServlet.init(ServletConfig config)</code>.
   * The <code>ServletConfig</code> object can still be retrieved via {@link #getServletConfig}.
   *
   */
  /*
  @Override
  public void init() {
    //setup RMQ connection
    ConnectionFactory connFactory = new ConnectionFactory();
    connFactory.setHost(RMQ_IP_ADDRESS);
    connFactory.setUsername(USERNAME);
    connFactory.setPassword(PASSWORD);
    connFactory.setPort(PORT);


    try {
      final Connection conn = connFactory.newConnection();
      ChannelFactory factory = new ChannelFactory(conn);
      final int THREAD_MAX_SIZE = 10;
      this.channelPool = new ChannelPool(THREAD_MAX_SIZE, factory);

    } catch (IOException e) {
      System.err.println("IOException");
      throw new RuntimeException(e);
    } catch (TimeoutException e) {
      System.err.println("TimeoutException");
      throw new RuntimeException(e);
    }

  }
*/
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    processRequest(req, res);
  }

  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    response.setContentType("application/json");
    Gson gson = new Gson();

    StringBuilder sb = new StringBuilder();
    String s = "";

    //get body response, which is the LiftRide
    while ((s = request.getReader().readLine()) != null) {
      sb.append(s);
    }

    //parse url
    s = request.getRequestURI();
    String[] result = s.split("/");

    Integer resortId = Integer.parseInt(result[3]);
    String seasonId = result[5];
    Integer dayId = Integer.parseInt(result[7]);
    Integer skierId = Integer.parseInt(result[9]);
    LiftRide ride = gson.fromJson(sb.toString(), LiftRide.class);

    //construct data to SkierData class
    SkierData skier = new SkierData(ride, resortId, seasonId, dayId, skierId);
    Status status = new Status();

    if (validateJson(skier)) {
      status.setStatus(201);
      status.setSuccess(true);
      status.setDescription("valid Input! successful!\n" +
          "resortId: " + skier.getResortId() + " \n " +
          "seasonId: " + skier.getSeasonId() + " \n " +
          "dayId: " + skier.getDayId() + " \n " +
          "skierId: " + skier.getSkierId() + " \n "
      );
    } else {
      status.setStatus(400);
      status.setSuccess(false);
      status.setDescription("Invalid POST request!");
    }

    String strResp = gson.toJson(status);
    response.getOutputStream().print(strResp);
    response.getOutputStream().flush();
    /*
    try {
      //send data to RMQ
      if (status.isSuccess()) {

        //take Channel from BlockingQueue Channel pool
        Channel channel = this.channelPool.borrowObject();

        //article: https://www.codetd.com/en/article/7884630
        //queue, durable, exclusive, autoDelete, arguments
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        //send to RMQ
        String skierDataJson = gson.toJson(skier);
        channel.basicPublish("", QUEUE_NAME, null,
            skierDataJson.getBytes(StandardCharsets.UTF_8));
        //System.out.println(skierDataJson);

        this.channelPool.returnObject(channel);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      status = new Status();
      status.setSuccess(false);
      status.setDescription(ex.getMessage());
      response.getOutputStream().print(gson.toJson(status));
      response.getOutputStream().flush();
    }
     */


  }

  private Boolean validateJson(SkierData skier) {
    Boolean isResortId = validateResorts(skier.getResortId());
    Boolean isSeasonId = validateSeasons("seasons", skier.getSeasonId());
    Boolean isDayId = validateDays("days", skier.getDayId());
    Boolean isSkierId = validateSkiers("skiers", skier.getSkierId());

    return isResortId && isSeasonId && isDayId && isSkierId;
  }


  private boolean validateResorts(Integer resortID) {
    return resortID > INVALID_NUM;
  }

  private boolean validateSeasons(String path, String seasonID) {
    return path.equals("seasons") && convertToInt(seasonID) > INVALID_NUM;
  }

  private boolean validateDays(String path, Integer dayID) {
    final Integer MIN_DAY = 0;
    final Integer MAX_DAY = 367;
    return path.equals("days") && (dayID > MIN_DAY && dayID < MAX_DAY);
  }

  private boolean validateSkiers(String path, Integer skierID) {
    return path.equals("skiers") && skierID > INVALID_NUM;
  }

  private Integer convertToInt(String id) {
    Integer numId = -1;
    try {
      numId = Integer.valueOf(id);
    } catch (NumberFormatException ex) {
      //ex.printStackTrace();
      System.out.println("Exception in convertToInt()" + ex.toString());
    }
    return numId;
  }

}
