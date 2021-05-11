import static org.junit.Assert.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

import java.util.ArrayList;

import org.junit.Test;

public class StudentTest {

    @Test
    public void testMeetsPrereq() {
        Student s = new Student("Sang Ik", "Han", "sangik_id", "sangik_pw", "CIT");
        ArrayList<String> pastCourses = new ArrayList<String>();
        s.setPastCourses(pastCourses);
        String prereqStr = "ECON 101 AND ECON 103 AND MATH 104 AND (MATH 114 OR MATH 115)";
        assertFalse(s.meetsPrereq(prereqStr));

        s.addPastCourse("ECON 101");
        s.addPastCourse("ECON 103");
        s.addPastCourse("MATH 104");
        assertFalse(s.meetsPrereq(prereqStr));

        s.addPastCourse("MATH 114");
        assertTrue(s.meetsPrereq(prereqStr));
    }

    @Test
    public void testIsInstructor() {
        Student s = new Student("Sang Ik", "Han", "sangik_id", "sangik_pw", "CIT");
        assertFalse(s.isInstructor());
    }

    @Test
    public void testGetFirstName() {
        Student s = new Student("Sang Ik", "Han", "sangik_id", "sangik_pw", "CIT");
        assertEquals(s.getFirstName(), "Sang Ik");
    }

    @Test
    public void testGetLastName() {
        Student s = new Student("Sang Ik", "Han", "sangik_id", "sangik_pw", "CIT");
        assertEquals(s.getLastName(), "Han");
    }

    @Test
    public void testGetID() {
        Student s = new Student("Sang Ik", "Han", "sangik_id", "sangik_pw", "CIT");
        assertEquals(s.getId(), "sangik_id");
    }

    @Test
    public void testGetPassword() {
        Student s = new Student("Sang Ik", "Han", "sangik_id", "sangik_pw", "CIT");
        assertEquals(s.getPassword(), "sangik_pw");
    }

    @Test
    public void testGetCourses() {
        Student s = new Student("Sang Ik", "Han", "sangik_id", "sangik_pw", "CIT");
        Course c1 = new Course("CIT", 591);
        s.addCourse(c1);
        ArrayList<ICourse> courseList = s.getCurrentCourses();
        assertEquals(courseList.get(0).subject(), "CIT");
    }

    @Test
    public void testPrintCourses() {
        Student s = new Student("Sang Ik", "Han", "sangik_id", "sangik_pw", "CIT");
        Course c1 = new Course("CIT", 591, 1);
        s.addCourse(c1);

        OutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        System.setOut(ps);

        s.printCourses();
        assertEquals("CIT-591-001 null\n", os.toString());

        PrintStream originalOut = System.out;
        System.setOut(originalOut);

    }

    @Test
    public void testPrintPastCourses() {
        Student s = new Student("Sang Ik", "Han", "sangik_id", "sangik_pw", "CIT");
        s.addPastCourse("CIT 591 1");

        OutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        System.setOut(ps);

        s.printPastCourses();
        assertEquals("CIT 591 1\t\n", os.toString());

        PrintStream originalOut = System.out;
        System.setOut(originalOut);

    }

}
