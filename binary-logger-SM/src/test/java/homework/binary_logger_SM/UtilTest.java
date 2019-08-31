package homework.binary_logger_SM;

import junit.framework.TestCase;


public class UtilTest extends TestCase {

  public void testIntToFromBytes() {
    int[] test = {555, -7777777, Integer.MAX_VALUE, Integer.MIN_VALUE};
    byte[] bytes = new byte[Integer.BYTES * test.length];

    for (int i = 0; i < test.length; i++) {
      int offset = i * Integer.BYTES;
      Util.intToBytes(test[i], bytes, offset );
      assertEquals(
        "Failed for " + test[i] + " in position " + offset,
        test[i],
        Util.bytesToInt(bytes, offset)
      );
    }

    // Now test that saving other values didn't erase previous.
    for (int i = 0; i < test.length; i++) {
      int offset = i * Integer.BYTES;
      assertEquals(
        "Failed for " + test[i] + " in position " + offset,
        test[i],
        Util.bytesToInt(bytes, offset)
      );
    }

  }

}
