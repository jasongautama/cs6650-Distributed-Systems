import io.swagger.client.model.LiftRide;

public class SkierData {
  private LiftRide liftRide;
  private Integer resortId;
  private String seasonId;
  private Integer dayId;
  private Integer skierId;

  public SkierData(LiftRide liftRide, Integer resortId, String seasonId, Integer dayId,
      Integer skierId) {
    this.liftRide = liftRide;
    this.resortId = resortId;
    this.seasonId = seasonId;
    this.dayId = dayId;
    this.skierId = skierId;
  }

  public Integer getResortId() {
    return resortId;
  }

  public String getSeasonId() {
    return seasonId;
  }

  public Integer getDayId() {
    return dayId;
  }

  public Integer getSkierId() {
    return skierId;
  }

  public LiftRide getLiftRide() {
    return this.liftRide;
  }

}
