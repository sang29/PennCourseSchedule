import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

public class InstructorTest {

    @Test
    public void testIsInstructor() {
        Instructor i = new Instructor("Sang Ik", "Han", "sangik_id", "sangik_pw", "CIT");
        assertTrue(i.isInstructor());
    }

    @Test
    public void testGetFirstName() {
        Instructor i = new Instructor("Sang Ik", "Han", "sangik_id", "sangik_pw", "CIT");
        assertEquals(i.getFirstName(), "Sang Ik");
    }

    @Test
    public void testGetLastName() {
        Instructor i = new Instructor("Sang Ik", "Han", "sangik_id", "sangik_pw", "CIT");
        assertEquals(i.getLastName(), "Han");
    }

    @Test
    public void testGetID() {
        Instructor i = new Instructor("Sang Ik", "Han", "sangik_id", "sangik_pw", "CIT");
        assertEquals(i.getId(), "sangik_id");
    }

    @Test
    public void testGetPassword() {
        Instructor i = new Instructor("Sang Ik", "Han", "sangik_id", "sangik_pw", "CIT");
        assertEquals(i.getPassword(), "sangik_pw");
    }

    @Test
    public void testGetCurrentCourses() {
        Instructor i = new Instructor("Sang Ik", "Han", "sangik_id", "sangik_pw", "CIT");
        Course c1 = new Course("CIT", 591);
        i.addCourse(c1);
        ArrayList<ICourse> courseList = i.getCurrentCourses();
        assertEquals(courseList.get(0).subject(), "CIT");
    }

    @Test
    public void testPrintCourses() {
        Instructor i = new Instructor("Sang Ik", "Han", "sangik_id", "sangik_pw", "CIT");
        Course c1 = new Course("CIT", 591, 1);
        i.addCourse(c1);

        OutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        System.setOut(ps);

        i.printCourses();
        assertEquals("CIT-591-001 null\n", os.toString());

        PrintStream originalOut = System.out;
        System.setOut(originalOut);

    }

    @Test
    public void testPrintWaitlist() {
        Instructor i = new Instructor("Sang Ik", "Han", "sangik_id", "sangik_pw", "CIT");
        HashMap<String, ArrayList<String>> waitlist = new HashMap<String, ArrayList<String>>();
        ArrayList<String> sampleIds = new ArrayList<String>();
        sampleIds.add("philipp_id");
        waitlist.put("CIS 557 1", sampleIds);
        i.setWaitlist(waitlist);      
        OutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        System.setOut(ps);

        i.printWaitlist();
        assertEquals("Waitlist for CIS 557 1:\n\t philipp_id\n", os.toString());

        PrintStream originalOut = System.out;
        System.setOut(originalOut);
    }

}
