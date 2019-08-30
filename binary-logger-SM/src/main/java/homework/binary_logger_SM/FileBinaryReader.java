package homework.binary_logger_SM;

import java.io.Closeable;
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
public class FileBinaryReader<T extends BinaryLoggable> implements Iterator<T>, Closeable{
  
  /** How many bytes 2 integers take */
  protected final static int TWO_INTEGERS = Integer.BYTES * 2;

  protected File inputFile;
  protected FileInputStream input = null;
  protected int count = 0;
  protected long cursor = -1;
  protected long currentPos = 0;
  protected T next = null;
  protected String nameT;
  
  
  public FileBinaryReader(String tClassName, File file) throws IllegalArgumentException, IOException, ClassNotFoundException {
    if (null == file) {
      throw new IllegalArgumentException("No input file provided");
    }
    if (null == tClassName) {
      throw new IllegalArgumentException("Please provide name of the parameter class.");
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
    this.inputFile = file;
    // Check that the name is correct
    Class.forName(tClassName);
    this.nameT = tClassName;

    // Try to read next so we would know if there's anything there.
    try {
      this.read();
    } catch (Exception e) {
      // TODO separate exceptions with user-friendly message.
      throw new IOException("Impossible to read from the provided file: " + e.getMessage());
    }
  }

  /****************************************************************************
   * 
   * @return T - the next element of type T if possible.
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

    // Prepare to read;
    this.positionReader();
    byte[] numbers = new byte[TWO_INTEGERS];
    this.next = null;
    final String PREMATURE_EOF = 
        "Corrupted file. End of file encountered prematurely: "
        + this.inputFile.getAbsolutePath();

    boolean nextNotFound = true;
    int dataLen = 0;
    T candidate = null;
    long position = this.currentPos;
    do {
      // Read the 2 numbers first - length of class name and length of data array.
      int actual = this.input.read(numbers);
      if (TWO_INTEGERS > actual) {
        throw new IOException(PREMATURE_EOF);
      }
      int nameLen = bytesToInt(numbers, 0);
      dataLen = bytesToInt(numbers, Integer.BYTES);

      // Read the class name next.
      byte[] nameBytes = new byte[nameLen];
      actual = this.input.read(nameBytes);
      if (nameLen > actual) {
        throw new IOException(PREMATURE_EOF);
      }
      String className = new String(nameBytes, StandardCharsets.UTF_8);
      Class<?> restoredClass = Class.forName(className);
      Object restored = restoredClass.getConstructor().newInstance();
      if (!(restored instanceof BinaryLoggable)) {
        throw new IllegalStateException(
            "Can only handle BinaryLoggable. This one is not: " + className
        );
      }

      position += TWO_INTEGERS + nameLen;
      // What to do if the class is not T? Skip till you find T?
      // Java refuses to do instanceof check on T, Class<?> variable or Class.forName().
      // So do try the exception way.
      try {
        candidate = (T) restored;
        nextNotFound = false;
      } catch (ClassCastException e) {
        // Ok, it's not an instance of T or Java is glitching again.
        // Skip to the next
        this.input.skip(dataLen);
        position += dataLen;
      }
    } while (nextNotFound);

    // If we get here - it's the instance of T! We've got our next (hopefully).
    // Now read the data
    byte[] data = new byte[dataLen];
    int actual = this.input.read(data);
    if (dataLen > actual) {
      throw new IOException(PREMATURE_EOF);
    }
    candidate.fromBytes(data);
    this.currentPos = position + dataLen;
    this.next = candidate;
    return this.next;
  }

  protected void positionReader() throws FileNotFoundException, IOException {
    if (null == this.input) {
      this.input = new FileInputStream(this.inputFile);
      // if we need to reopen is - we might need to skip
      // to the place we left off.
      this.input.skip(this.currentPos);
      this.cursor = this.currentPos;
    }
  }

  public boolean hasNext() {
    return (null != this.next);
  }

  /**
   * {@inheritDoc}
   */
  public T next() {
    T result = this.next;
    try {
      this.next = this.read();
    } catch (Exception e) {
      // TODO what to do with it? Probably log it.
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  public void close() throws IOException {
    try {
      if (null != this.input) {
        this.input.close();
        this.input = null;
        this.cursor = -1;
      }
    } catch (IOException e) {
      throw new IOException("Couldn't close input stream.", e);
    }
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
