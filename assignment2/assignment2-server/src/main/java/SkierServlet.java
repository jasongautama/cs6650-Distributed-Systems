import com.google.gson.Gson;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.swagger.client.model.LiftRide;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import com.rabbitmq.client.Channel;

@WebServlet(name = "SkierServlet", value = "/skiers")
public class SkierServlet extends HttpServlet {
    private final static Integer INVALID_NUM = -1;

    private final static String QUEUE_NAME = "skierQueue";
    private final static String RMQ_IP_ADDRESS = "35.91.190.94";
    //private final static String RMQ_IP_ADDRESS = "localhost";

    private boolean validateResorts(Integer resortID) {
        return resortID > INVALID_NUM;
    }

    private boolean validateSeasons(String path, String seasonID) {
        return path.equals("seasons") && convertToInt(seasonID) > INVALID_NUM;
    }

    private boolean validateDays (String path, Integer dayID) {
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        processRequest(req, res);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        response.setContentType("application/json");
        Gson gson = new Gson();
        try {
            StringBuilder sb = new StringBuilder();
            String s = "";

            //get body
            while ((s = request.getReader().readLine()) != null) {
                sb.append(s);
            }

            //get url
            s = request.getRequestURI();
            String[] result = s.split("/");

            LiftRide ride = gson.fromJson(sb.toString(), LiftRide.class);
            Integer resortId = Integer.parseInt(result[3]);
            String seasonId = result[5];
            Integer dayId = Integer.parseInt(result[7]);
            Integer skierId = Integer.parseInt(result[9]);

            SkierData skier = new SkierData(ride, resortId, seasonId, dayId, skierId);
            //SkierData skier = gson.fromJson(sb.toString(), SkierData.class);

            Status status = new Status();

            if (validateJson(skier)) {
                status.setStatus(201);
                status.setSuccess(true);
                status.setDescription("valid Input! successful!" +
                    "resortId: " + skier.getResortId() + " \n " +
                    "seasonId: " + skier.getSeasonId() + " \n " +
                    "dayId: " + skier.getDayId() + " \n " +
                    "skierId: " + skier.getSkierId() + " \n "
                );

                //call the rabbitMQ
            } else {
                status.setStatus(400);
                status.setSuccess(false);
                status.setDescription("Invalid POST request!");
            }

            //TODO: may need to be replaced by LiftRide
            String strResp = gson.toJson(status);
            response.getOutputStream().print(strResp);
            response.getOutputStream().flush();

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(RMQ_IP_ADDRESS);
            factory.setUsername("jasonmax");
            factory.setPassword("r8cLhJUecQhV7DXdekKn");
            final Connection conn = factory.newConnection();
            try {
                // channel per thread
                Channel channel = conn.createChannel();
                channel.queueDeclare(QUEUE_NAME, true, false, false, null);

                String message = skier.getLiftRide().toString();//"Here's a message " +  Integer.toString(i);
                channel.basicPublish("", QUEUE_NAME, null,
                    message.getBytes(StandardCharsets.UTF_8));
                //System.out.println(message);

                channel.close();
                //System.out.println(" [All Messages  Sent '" );
            } catch (IOException | TimeoutException ex) {
                Logger.getLogger(SkierServlet.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        catch(Exception ex) {
            ex.printStackTrace();
            Status status = new Status();
            status.setSuccess(false);
            status.setDescription(ex.getMessage());
            response.getOutputStream().print(gson.toJson(status));
            response.getOutputStream().flush();
        }
    }

    private Boolean validateJson(SkierData skier) {
        Boolean isResortId = validateResorts(skier.getResortId());
        Boolean isSeasonId = validateSeasons("seasons", skier.getSeasonId());
        Boolean isDayId = validateDays("days", skier.getDayId());
        Boolean isSkierId = validateSkiers("skiers", skier.getSkierId());

        return isResortId && isSeasonId && isDayId && isSkierId;
    }
}
