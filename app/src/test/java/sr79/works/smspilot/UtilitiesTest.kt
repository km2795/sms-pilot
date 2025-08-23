package sr79.works.smspilot

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class UtilitiesTest {
  @Test
  fun stringToBoolean_Test() {
    assertEquals(Utilities.stringToBoolean("FAlse"), false)
    assertEquals(Utilities.stringToBoolean("true"), true)
    assertEquals(Utilities.stringToBoolean("null"), null)
    assertEquals(Utilities.stringToBoolean("false"), false)
    assertEquals(Utilities.stringToBoolean("true"), true)
    assertEquals(Utilities.stringToBoolean("right"), null)
  }
}