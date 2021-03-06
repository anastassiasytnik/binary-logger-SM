package homework.binary_logger_SM;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import homework.binary_logger_SM.BinaryLoggable;

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
 * WARNING: when this class is closed the iterators
 * issued with the method {@link #read()} remain open,
 * and it is the caller's responsibility to close those.
 * (Since the read can be continued after the write is done).
 */
public class FileBinaryLogger<T extends BinaryLoggable> extends BinaryLogger<T> {
  
  /** to write to the file */
  protected FileOutputStream output = null;

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
    byte[] numbers = new byte[FileBinaryReader.TWO_INTEGERS];
    Util.intToBytes(nameLen, numbers, 0);
    Util.intToBytes(data.length, numbers, Integer.BYTES);
    this.output.write(numbers);
    this.output.write(nameBytes, 2, nameLen);
    this.output.write(data);
    this.output.flush();
    this.position += FileBinaryReader.TWO_INTEGERS + nameLen + data.length;
  }

  @Override
  Iterator<T> read(String className) throws IllegalArgumentException, IOException {
    Iterator<T> result = null;
    try {
      result = (Iterator<T>) new FileBinaryReader<T>(className, this.outputFile);
    } catch (IOException ioe ) {
      // throw IO "as is"
      throw ioe;
    } catch (Exception e) {
      // throw the rest as illegal argument, cause it is.
      throw new IllegalArgumentException(e.getMessage(), e);
    }
    return result;
  }

}