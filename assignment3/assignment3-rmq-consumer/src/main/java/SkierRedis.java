import java.util.Set;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class SkierRedis {
  //private static final String HOST_IP = "54.184.176.199";
  private static final String HOST_IP = "172.31.4.67"; //redis-aws private IP
  private SkierData skier;
  private JedisPool client;

  public SkierRedis() {
    configJedisPool();
  }

  public SkierRedis(SkierData data) {
    configJedisPool();
    this.skier = data;
  }

  private void configJedisPool() {
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    this.client = new JedisPool(poolConfig, HOST_IP, 6379);
  }

  public boolean writeToRedis(SkierData skier) {
    this.skier = skier;
    System.out.println(this.skier);
    try (Jedis jedis = this.client.getResource()) {
      return writeNumDaysASkierSkied(jedis) &&
          writeUniqueSkiersVisitedResortOnCertainDay(jedis);
    }
  }


  /**
   * Q1. "For skier N, how many days have they skied this season?"
   * A. key: "skier_id#days:'skierID'", value: dayId
   * @return: true if value added to list successfully; false if already exist
   */
  private Boolean writeNumDaysASkierSkied(Jedis jedis) {

    String strSkierId = "skier_id#days:" + this.skier.getSkierId();
    String dayId = this.skier.getDayId();
    final long SUCCESSFUL = 1;
//    try (Jedis jedis = this.client.getResource()) {
      //add skier_id#days to a list for each skier
      long status = jedis.sadd(strSkierId, dayId);
      return status == SUCCESSFUL;
//    }
  }

  //Q2. For skier 49076, what are the vertical totals for each ski day?

  //Q4. "How many unique skiers visited resort 6 on day 2?"
  //key: "resort_id:6#days:2", value: [set of skierId] -> return len()
  //"resort_id:1#day_id:1" [123,234,456,321]
  //resort_id:1#day_id:2 [456,321,139]
  //resort_id:2#day_id:2 [123]
  //resort_id:2#day_id:3 [123,234]
  /**
   *
   * @return true if successfully written to DB; false otherwise
   */
  public Boolean writeUniqueSkiersVisitedResortOnCertainDay(Jedis jedis) {
    //String strSkierId = "skier_id#days:" + this.skier.getSkierId();
    //String dayId = this.skier.getDayId();
    String resortId = String.valueOf(this.skier.getResortId());
    String key = "resort_id:" + resortId + "#day_id:" + this.skier.getDayId();
    final int SUCCESSFUL = 1;

    //try (Jedis jedis = this.client.getResource()) {
      String skierId = String.valueOf(this.skier.getSkierId());
      long status = jedis.sadd(key, skierId);
      return status == SUCCESSFUL;
    //}
  }

  public Integer getCountNumDaysASkierSkied(Integer skierId) {
    String strSkierId = "skier_id#days:" + skierId;
    try (Jedis jedis = this.client.getResource()) {
      Set<String> days = jedis.smembers(strSkierId);
      for (String day: days) {
        System.out.print(day + ",");
      }
      return days.size();
    }
  }

}
