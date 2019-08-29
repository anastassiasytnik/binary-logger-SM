package homework.binary_logger_SM;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * This class would allow us to do simple read/write using built-in Java methods.
 */
public class LoggableWrapper implements Serializable {

  /** This is just in case we add or remove the fields in the future. */
  private static final long serialVersionUID = 1L;

  /** Will contain class name for instantiating during the read */
  private String className = null;
  
  /** The result of "toBytes()" method of BinaryLoggable. (WARNING: NOT a copy!) */
  private byte[] data = null;
  
  /** Constructor required for serialization */
  public LoggableWrapper() { };
  
  /**
   * Attempts to prepare the provided object for persistence in a file
   * 
   * @param object - object to persist.
   *   
   * @throws IOException - if the object refuses to convert to byte array.
   */
  public LoggableWrapper(BinaryLoggable object) throws IOException {
    if (null != object) {
      this.className = object.getClass().getName();
      this.data = object.toBytes();
    }
  }
  
  /**
   * Creates a new object either after reading from file or just from the data.
   * 
   * @return a new instance of BinaryLoggable.
   * 
   * @throws ClassNotFoundException - if the class of wrapped BinaryLoggable is not present.
   * @throws InstantiationException - if couldn't create the BinaryLoggable object. 
   * @throws IllegalAccessException - if 0-parameter constructor of the BinaryLoggable is private.
   * @throws IllegalArgumentException - doesn't really happen with 0-parameter constructor
   * @throws InvocationTargetException - 
   *     if 0-parameter constructor of the BinaryLoggable threw an exception.
   * @throws NoSuchMethodException - if there is no 0-parameter constructor for that BinaryLoggable
   * @throws SecurityException - the actual class of BinaryLoggable is not visible for this package 
   *           or there's some problems with class Loader
   * @throws IOException - if the BinaryLoggable refused to read its data from the byte array.
   */
  public BinaryLoggable getBinaryLoggable() 
      throws ClassNotFoundException, InstantiationException, IllegalAccessException, 
             IllegalArgumentException, InvocationTargetException, NoSuchMethodException, 
             SecurityException, IOException {
    if (null == this.className || null == this.data) {
      throw new RuntimeException("Insufficient data (probably not initialized).");
    }
    Object restored = Class.forName(this.className).getConstructor().newInstance();
    BinaryLoggable result = null;
    if (!(restored instanceof BinaryLoggable)) {
      throw new IllegalStateException("Can only handle BinaryLoggable. This one is not: " + this.className); 
    } else {
      result = (BinaryLoggable) restored;
      result.fromBytes(this.data);
    }
    return result;
  }
  
  
  // ===== standard bean getters/setters ====
 
  /**
   * If initialized - returns the actual class name of the "wrapped" BinaryLoggable.
   * 
   * @return the class name or {@code null}
   */
  public String getClassName() {
    return className;
  }

  /**
   * Sets the className property - the name of the class of the "wrapped" BinaryLoggable.
   * 
   * This method will be used during de-serialization.
   * 
   * @param className - the name to remember.
   */
  public void setClassName(String className) {
    this.className = className;
  }

  /**
   * fetches the actual binary data from the wrapped BinaryLoggable.
   * 
   * WARNING! You get the actual data, not the safe copy!
   * 
   * @return the data or {@code null} if not initialized.
   */
  public byte[] getData() {
    return data;
  }

  /**
   * sets the binary data for the wrapped BinaryLoggable.
   * 
   * This method will be used during de-serialization.
   * 
   * WARNING! the data is not copied, but saved as-is.
   * 
   * @param data
   */
  public void setData(byte[] data) {
    this.data = data;
  }

}
