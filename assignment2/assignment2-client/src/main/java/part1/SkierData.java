package part1;

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

  public LiftRide getLiftRide() {
    return liftRide;
  }

  public SkierData() {
    final String SEASON_YEAR = "2022";
    final String DAY_ID = "1";

    int min = 1;
    int max = 100000;
    int skierId = (int) (Math.random() * (max - min + 1) + min);
    max = 10;
    int resortId = (int) (Math.random() * (max - min + 1) + min);
    int liftRideId = (int) (Math.random() * (max - min + 1) + min);
    int randTime = (int) (Math.random() * (max - min + 1) + min);

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
        "resortId:" + resortId +
        ", seasonId:'" + seasonId + '\'' +
        ", dayId:" + dayId +
        ", skierId:" + skierId +
        '}';
  }
}
