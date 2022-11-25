import io.swagger.client.model.LiftRide;

public class SkierData {

  private LiftRide liftRide;
  private int resortId;
  private String seasonId;
  private String dayId;
  private int skierId;

  public int getResortId() {
    return resortId;
  }

  public String getSeasonId() {
    return seasonId;
  }

  public String getDayId() {
    return dayId;
  }

  public int getSkierId() {
    return skierId;
  }

  public void setSkierId(int skierId) {
    this.skierId = skierId;
  }

  public LiftRide getLiftRide() {
    return liftRide;
  }

  public void setLiftRide(LiftRide liftRide) {
    this.liftRide = liftRide;
  }

  public void setResortId(int resortId) {
    this.resortId = resortId;
  }

  public void setSeasonId(String seasonId) {
    this.seasonId = seasonId;
  }

  public void setDayId(String dayId) {
    this.dayId = dayId;
  }

  public SkierData() {
    final String SEASON_YEAR = "2022";
    //final String DAY_ID = "1";

    int min = 1;
    int max = 100000;
    int skierId = (int) (Math.random() * (max - min + 1) + min);
    max = 10;
    int resortId = (int) (Math.random() * (max - min + 1) + min);
    max = 40;
    int liftRideId = (int) (Math.random() * (max - min + 1) + min);
    max = 360;
    int randTime = (int) (Math.random() * (max - min + 1) + min);
    max = 365;
    int dayId = (int) (Math.random() * (max - min + 1) + min);
    final String DAY_ID = String.valueOf(dayId);

    this.resortId = resortId;
    this.seasonId = SEASON_YEAR;
    this.dayId = DAY_ID;
    this.skierId = skierId;
    this.liftRide = new LiftRide();
    this.liftRide.setLiftID(liftRideId);
    this.liftRide.setTime(randTime);

  }

  @Override
  public String toString() {
    return "{" +
        formatKey("resortId") + resortId + ',' +
        formatKey("seasonId") + seasonId + ',' +
        formatKey("dayId") + dayId + ',' +
        formatKey("skierId") + skierId + ',' +
        formatKey("LiftRide") + '{' +
          formatKey("time") + liftRide.getTime() + ',' +
          formatKey("liftID") + liftRide.getLiftID() +
        "}" +
    "}";
  }

  private String formatKey(String word) {
    return '"' + word + '"' + ':';
  }
}