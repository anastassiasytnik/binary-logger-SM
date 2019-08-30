package homework.binary_logger_SM;

/**
 * For static methods.
 */
public class Util {

  /****************************************************************************
   * Reads the integer from byte array buffer.
   *
   * This method is stolen from java.io.Bits.getInt().
   * Unfortunately both the method and the class are not visible or could call.
   *
   * WARNING: this method is not fool-proof for speed. Please use with care:
   * make sure the offset is at least 4 positions from the end of the buffer.
   * (4 == Integer.BYTES)
   *
   * @param buffer - the byte array containing integer in question.
   *       Make sure it's not null and at least Integer.BYTES long.
   * @param offset - the position in the buffer where the integer starts.
   *       Make sure the offset is at least buffer.length - Integer.BYTES
   *
   * @return the value of the integer.
   */
  public static int bytesToInt(byte[] buffer, int offset) {
    return ((buffer[offset + 3] & 0xFF)      ) +
           ((buffer[offset + 2] & 0xFF) <<  8) +
           ((buffer[offset + 1] & 0xFF) << 16) +
           ((buffer[offset    ]       ) << 24);
  }

  /****************************************************************************
   * Converts an integer to a byte array.
   *
   * This method is stolen from java.io.Bits.putInt().
   * Unfortunately both the method and the class are not visible or could call.
   *
   * WARNING: this method is not fool-proof for speed. Please use with care:
   * make sure the buffer length is sufficient and the offset is at least
   * 4 positions from the end of the buffer.
   *
   * @param value - the integer to convert to bytes
   * @param buffer - the byte array to put the converted integer to.
   *     Make sure it's not null and at least Integer.BYTES long.
   * @param offset - the offset from which the converted integer should start.
   *       Make sure the offset is at least buffer.length - Integer.BYTES
   */
  public static void intToBytes(int value, byte[] buffer, int offset) {
    buffer[offset + 3] = (byte) (value       );
    buffer[offset + 2] = (byte) (value >>>  8);
    buffer[offset + 1] = (byte) (value >>> 16);
    buffer[offset    ] = (byte) (value >>> 24);
  }

}
