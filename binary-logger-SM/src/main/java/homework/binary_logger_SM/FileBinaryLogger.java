package homework.binary_logger_SM;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/******************************************************************************
 * An implementation of BinaryLogger that uses FileOutputStream.
 * 
 * The {@code BinaryLoggable}s are saved as bytes to the file 
 * in the following format: 
 *   first there's a number that is the length of the byte representation of 
 *                                actual class of the {@code BinaryLoggable}
 *   then there's a number that is the length of the byte array of the data
 *                      which is the result of calling the toBytes() method 
 *   then there's a string (as bytes) that is the class name,
 *   and finally there's the byte data.
 *   
 * During the writing the data in the file is indexed by different classes.                                            
 * So if you instantiated FileBinaryLogger<BinaryLoggable>, but use it
 * to save 3 different implementations of BinaryLoggable - then you can 
 * call {@link #read(Class<T>)} on any of the implementations, and 
 * get the iterator that returns only that implementation.
 * TODO test if that last statement even possible, adjust if needed.
 */
public class FileBinaryLogger<T extends BinaryLoggable> extends BinaryLogger<T> {
  
  protected final static int TWO_INTEGERS = Integer.BYTES * 2;
  
  /** to write to the file */
  protected FileOutputStream output = null;

  /** to be able to read (FAST) only the certain sub-type. */
  protected HashMap<String, List<Long>> typeMap = new HashMap<String, List<Long>>();

  /** amount of bytes already written */
  protected long position = 0L;

  /****************************************************************************
   * Checks whether the provided file exists, readable and writable.
   * 
   * If not - tries to amend the situation. Prepares for the file for writing.
   * Initializes the types map.
   * 
   * @param file - file to write to.
   * 
   * @throws IllegalArgumentException - if the parameter is null
   * @throws FileNotFoundException - if file is not located after
   *    it has been checked out good. 
   * @throws IOException -if file doesn't exists and cannot be created,
   *      or if the file permissions prevent logger from doing the work.
   */
  public FileBinaryLogger(File file) 
      throws IllegalArgumentException, FileNotFoundException, IOException {
    super(file);
    // Simple check that actually the parent should have done.
    if (null == file) {
      throw new IllegalArgumentException("No output file provided");
    }
    
    // Does file exists? 
    try {
      file.createNewFile();
    } catch (IOException e) {
      throw new IOException(
          "The file didn't exist and couldn't be created. Check permissions.\n"
           + e.getMessage(),
           e
      );
    }

    // Is it readable/writable?
    // This piece could be implemented in more details correctly (so not EVERYBODY can write).
    // This implementation assumes that encompassing directory has permissions that properly protect this file.
    if (!file.canRead()) {
      if (!file.setReadable(true)) {
        throw new IOException("Can't read from the provided file: " + file.getPath());
      }
    }
    if (!file.canWrite()) {
      if (!file.setWritable(true)) {
        throw new IOException("Can't write to the provided file: " + file.getPath());
      }
    }
    
  }

  /**
   * {@inheritDoc}
   */
  public void close() throws IOException {
    // QUESTION: Should we also close the issued Iterators??
    // For now we leave to to caller of the "read" method.
    try {
      if (null != this.output) {
        this.output.flush();
        this.output.close();
        this.output = null;
      }
    } catch (IOException e) {
      throw new IOException("Couldn't close output stream.", e);
    }
  }

  @Override
  void write(T loggable) throws IOException {
    if (null == loggable) {
      return;
    }
    // Re-open stream if it was closed earlier.
    if (null == this.output) {
      this.output = new FileOutputStream(this.outputFile, true);
    }
    String className = loggable.getClass().getCanonicalName();
    byte[] nameBytes = className.getBytes(StandardCharsets.UTF_8);
    int nameLen = nameBytes.length - 2;
    byte[] data = loggable.toBytes();
    byte[] numbers = new byte[TWO_INTEGERS];
    intToBytes(nameLen, numbers, 0);
    intToBytes(data.length, numbers, Integer.BYTES);
    this.output.write(numbers);
    this.output.write(nameBytes, 2, nameLen);
    this.output.write(data);
    this.output.flush();
    this.position += TWO_INTEGERS + nameLen + data.length;
    List<Long> crumbs = this.typeMap.get(className);
    if (null == crumbs) {
      crumbs = new ArrayList<Long>();
      this.typeMap.put(className, crumbs);
    }
    crumbs.add(position);
  }

  @Override
  Iterator<T> read(Class<T> clazz) throws IOException {
    // TODO Auto-generated method stub
    return null;
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
