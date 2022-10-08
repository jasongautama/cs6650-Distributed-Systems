import com.google.gson.Gson;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "SkierServlet", value = "/skiers")
public class SkierServlet extends HttpServlet {
  private final static Integer INVALID_NUM = -1;

  private boolean validateResorts(Integer seasonID) {
    return seasonID > INVALID_NUM;
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
      ex.printStackTrace();
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
          while ((s = request.getReader().readLine()) != null) {
            sb.append(s);
          }

          SkierData skier = (SkierData) gson.fromJson(sb.toString(), SkierData.class);
          Status status = new Status();

          Boolean isResortId = validateResorts(skier.getResortId());
          Boolean isSeasonId = validateSeasons("seasons", skier.getSeasonId());
          Boolean isDayId = validateDays("days", skier.getDayId());
          Boolean isSkierId = validateSkiers("skiers", skier.getSkierId());
          Boolean isValid = isResortId && isSeasonId && isDayId && isSkierId;

          if (!isValid) {
            status.setStatus(400);
            status.setSuccess(false);
            status.setDescription("Invalid POST request!");
          } else {
            status.setStatus(201);
            status.setSuccess(true);
            status.setDescription("valid Input! successful!" +
                "resortId: " + skier.getResortId() + " \n " +
                    "seasonId: " + skier.getSeasonId() + " \n " +
                    "dayId: " + skier.getDayId() + " \n " +
                    "skierId: " + skier.getSkierId() + " \n "
                );
          }

          response.getOutputStream().print(gson.toJson(status));
          response.getOutputStream().flush();
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
}