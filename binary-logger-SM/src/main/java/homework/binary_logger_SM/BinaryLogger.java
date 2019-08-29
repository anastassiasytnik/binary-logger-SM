package homework.binary_logger_SM;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * BinaryLogger logs serialized instances of {@link BinaryLoggable} into file.
 * It does so in such a way that it is possible to stream these instances back
 * in an iterative fashion via the {@link #read(File, Class)} method.
 */
public abstract class BinaryLogger<T extends BinaryLoggable> implements AutoCloseable {

  protected File outputFile;
  
  public BinaryLogger(File file) {
      this.outputFile = file;
  }

 /**
  * Writes the serialized instance.
  *
  * @param loggable an instance of {@code BinaryLoggable} that needs to be logged
  * @throws IOException if any IO operation fails
  */
  abstract void write(T loggable) throws IOException;

  /**
  * Read and iterate through instances persisted in the given file.
  *
  * @param clazz a class of the type T, clazz should have a public no-arg constructor
  * @throws IOException if any IO operation fails
  */
  abstract Iterator<T> read(Class<T> clazz) throws IOException;
}

