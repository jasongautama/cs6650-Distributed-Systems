import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SkierRedisTest {

  @BeforeEach
  void setUp() {

  }

  @Test
  void writeToRedis() {
    for (int i = 0; i < 1; i++) {
      SkierRedis testRedis = new SkierRedis();
      assertTrue(testRedis.writeToRedis(new SkierData()));
    }
  }

  @Test
  void writeToRedis_testCountNumDaysASkierSkied() {
    int min = 1;
    int max = 100000;
    final int skierId = (int) (Math.random() * (max - min + 1) + min);

    final Integer EXPECTED_COUNT = 5;
    for (int i = 0; i < EXPECTED_COUNT; i++) {
      SkierData skier = new SkierData();
      skier.setSkierId(skierId);
      SkierRedis testRedis = new SkierRedis();
      testRedis.writeToRedis(new SkierData());

    }
    SkierRedis testRedis = new SkierRedis(new SkierData());

    assertEquals(testRedis.getCountNumDaysASkierSkied(skierId), EXPECTED_COUNT);
  }

  @Test
  void writeUniqueSkiersVisitedResortOnCertainDay() {
    SkierData skier = new SkierData();
    //resort_id:8#day_id:68
    skier.setResortId(8);
    skier.setDayId("68");
    SkierRedis testRedis = new SkierRedis(skier);
    testRedis.writeUniqueSkiersVisitedResortOnCertainDay();
  }

  @Test
  void query() {
  }

  @Test
  void testString() {
    System.out.println(new SkierData());
  }
}