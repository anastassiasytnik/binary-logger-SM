package homework.binary_logger_SM;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

/******************************************************************************
 * This class will read from the file created by {@link FileBinaryLogger}
 * 
 * This class assumes that the file isn't corrupted, and all the class
 * is not required to read past the positions provided in the constructor.
 *
 * @param <T>
 */
public class FileBinaryReader<T extends BinaryLoggable> implements Iterator<T> {
  
  /** How many bytes 2 integers take */
  protected final static int TWO_INTEGERS = Integer.BYTES * 2;

  protected File inputFile;
  protected FileInputStream input = null;
  protected List<Long> positions = null;
  protected int count = 0;
  protected long currentPos = 0;
  protected T next = null;
  protected String nameT;
  
  
  public FileBinaryReader(Class<T> clazz, List<Long> positions, File file) throws IllegalArgumentException, IOException {
    if (null == file) {
      throw new IllegalArgumentException("No input file provided");
    }
    
    // Does file exists? 
    if (!file.exists()) {
      throw new FileNotFoundException("File doesn't exists or unaccessible: " + file.getAbsolutePath());
    }
    // Is it readable?
    if (!file.canRead()) {
      if (!file.setReadable(true)) {
        throw new IOException("Can't read the file: " + file.getAbsolutePath());
      }
    }
    this.nameT = clazz.getCanonicalName();
    this.inputFile = file;
    // Open to read.
    this.input = new FileInputStream(this.inputFile);
    
    this.inputFile = file;
    this.positions = positions;
    if (null != positions) {
      this.count = positions.size();
      this.currentPos = this.positions.get(0);
    }
  }

  /****************************************************************************
   * 
   * @return T
   * @throws IOException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InvocationTargetException
   * @throws NoSuchMethodException
   * @throws SecurityException
   * @throws ClassNotFoundException
   */
  @SuppressWarnings("unchecked")
  protected T read() 
      throws IOException, InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException,
      NoSuchMethodException, SecurityException, ClassNotFoundException {
    byte[] numbers = new byte[TWO_INTEGERS];
    this.next = null;
    final String PREMATURE_EOF = 
        "Corrupted file. End of file encountered prematurely: "
        + this.inputFile.getAbsolutePath();
    int actual = this.input.read(numbers);
    if (TWO_INTEGERS > actual) {
      throw new IOException(PREMATURE_EOF);
    }
    int nameLen = bytesToInt(numbers, 0);
    int dataLen = bytesToInt(numbers, Integer.BYTES);
    byte[] nameBytes = new byte[nameLen];
    actual = this.input.read(nameBytes);
    if (nameLen > actual) {
      throw new IOException(PREMATURE_EOF);
    }
    String className = new String(nameBytes, StandardCharsets.UTF_8);
    byte[] data = new byte[dataLen];
    actual = this.input.read(data);
    if (dataLen > actual) {
      throw new IOException(PREMATURE_EOF);
    }
    Object restored = Class.forName(className).getConstructor().newInstance();
    if (!(restored instanceof BinaryLoggable)) {
      throw new IllegalStateException(
          "Can only handle BinaryLoggable. This one is not: " + className
      ); 
    }
    
    try {
      this.next = (T) restored;
      next.fromBytes(data);
    } catch (ClassCastException e) {
      throw new IOException(
          "File is corrupted: unexpected type is encountered: " + className
          + " is not a " + this.nameT,
          e
      );
    } 
    
    return this.next;
  }

  public boolean hasNext() {
    return (null == next);
  }

  /**
   * {@inheritDoc}
   */
  public T next() {
    T result = this.next;
    if (null != this.positions) {
      
    }
    return result;
  }

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

}
