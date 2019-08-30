package homework.binary_logger_SM;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
  
  /** to write to the file */
  private FileOutputStream output = null;
  
  /** to be able to read (FAST) only the certain sub-type. */
  private HashMap<String, List<Integer>> typeMap = new HashMap<String, List<Integer>>();

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
    
    // This is a question whether we should open the file for writing right away.
    // This would depend on frequency of writes. If it is frequent - we better keep it open.
    // Otherwise we could check for null and open on first write.
    this.output = new FileOutputStream(file, true);
  }

  public void close() throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  void write(T loggable) throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  Iterator<T> read(Class<T> clazz) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

}
