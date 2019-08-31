package homework.binary_logger_SM;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import static junit.framework.TestCase.*;

/** Contains test data for JUnits */
public class BinaryLoggables {

  public static class Person implements BinaryLoggable {
    public String name = "";

    public Person() { }

    public Person(String name) {
      this.name = (null == name)? "" : name;
    }

    @Override
    public byte[] toBytes() throws IOException {
      return name.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void fromBytes(byte[] rawBytes) throws IOException {
      this.name =  new String(rawBytes, StandardCharsets.UTF_8);
    }

    public boolean equals(Object other) {
      if (null == other ||
          !(Person.class.getCanonicalName().equals(other.getClass().getCanonicalName()))) {
        return false;
      }
      String otherName = ((Person) other).name;
      return (null == this.name && null == otherName) ||
             (null != this.name && this.name.equals(otherName));
    }

  }

  public static class Student extends Person {
    public String school = "";

    public Student() { }

    public Student(String name, String school) {
      super(name);
      this.school = (null == school)? "" : school;
    }

    @Override
    public byte[] toBytes() throws IOException {
      return (this.name + "\n" + this.school).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void fromBytes(byte[] rawBytes) throws IOException {
      String[] data = (new String(rawBytes, StandardCharsets.UTF_8)).split("\n");
      this.name =  data[0];
      this.school = data[1];
    }

    public boolean equals(Object other) {
      if (null == other ||
          !(Person.class.getCanonicalName().equals(other.getClass().getCanonicalName()))) {
        return false;
      }
      String otherName = ((Person) other).name;
      return (null == this.name && null == otherName) ||
             (null != this.name && this.name.equals(otherName));
    }

  }

  public static class Parent extends Person implements Serializable {
    /** for versioning */
    private static final long serialVersionUID = 1L;

    public Person [] kids;

    public Parent() { }

    public Parent(String name, Person[] kids) {
      super(name);
      this.kids = (null == kids)? new Person[0] : kids;
    }

    @Override
    public byte[] toBytes() throws IOException {
      ByteArrayOutputStream bout = new ByteArrayOutputStream(100);
      ObjectOutputStream out = new ObjectOutputStream(bout);
      out.writeObject(this);
      return bout.toByteArray();
    }

    @Override
    public void fromBytes(byte[] rawBytes) throws IOException {
      ByteArrayInputStream bin = new ByteArrayInputStream(rawBytes);
      ObjectInputStream in = new ObjectInputStream(bin);
      try {
        Object item = in.readObject();
        if (item instanceof Parent) {
          Parent restored = (Parent) item;
          this.name = restored.name;
          this.kids = restored.kids;
        } else {
          throw new IOException("Not a parent: " + item.getClass().getCanonicalName());
        }
      } catch (ClassNotFoundException ex) {
        ex.printStackTrace();
        throw new IOException("Unknown class was serialized: " + ex.getMessage(), ex);
      }
    }
  }

  public final static String JOHNDOE_NAME = "John Doe";
  public final static String JANEDOE_NAME = "Jane Doe";
  public final static String JOE_NAME = "Joe Sloppy";
  public final static String SUSAN_NAME = "Susan Doe";
  public final static String JIM_NAME = "Jim Doe";
  public final static String HELENA_NAME = "Helena Montana";
  public final static String BOB_NAME = "Bob Doe";
  public final static String SCHOOL_UCLA = "UCLA";
  public final static String SCHOOL_CALPOLY = "Cal Poly";

  public final static Person JOHNDOE = new Person(JOHNDOE_NAME);
  public final static Person JANEDOE = new Person(JANEDOE_NAME);
  public final static Student STUDENT_JOE = new Student(JOE_NAME, SCHOOL_UCLA);
  public final static Student STUDENT_SUSAN = new Student(SUSAN_NAME, SCHOOL_CALPOLY);
  public final static Parent PARENT_JIM = new Parent(JIM_NAME, new Person[]{JOHNDOE, STUDENT_SUSAN});
  public final static Parent PARENT_HELENA = new Parent(HELENA_NAME, new Person[]{JOHNDOE, STUDENT_JOE});
  public final static Parent PARENT_BOB = new Parent(BOB_NAME, new Person[]{PARENT_JIM, JANEDOE});

  public static Person[] getPersons() {
    return new Person[]{JOHNDOE, JANEDOE};
  }

  public static Person[] getAllPersons() {
    return new Person[]{
      JOHNDOE, JANEDOE, STUDENT_JOE, STUDENT_SUSAN, PARENT_JIM, PARENT_HELENA, PARENT_BOB
    };
  }

  public static Student[] getStudents() {
    return new Student[]{STUDENT_JOE, STUDENT_SUSAN};
  }

  public static Parent[] getParents() {
    return new Parent[]{PARENT_JIM, PARENT_HELENA, PARENT_BOB};
  }

  public void testSanity() throws IOException {
    String testName = "Ivan";
    String testSchool = "UCLA";
    Person man = new Person(testName);
    Person restored = new Person();
    restored.fromBytes(man.toBytes());
    assertEquals("Person's name doesn't match", testName, restored.name);
    man = new Student(testName, testSchool);
    restored = new Student();
    restored.fromBytes(man.toBytes());
    assertEquals("Student's name is mismatched", testName, restored.name);
    assertEquals("Student's school is mismatched", testSchool, ((Student)restored).school);
  }
}
