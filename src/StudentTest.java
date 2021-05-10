import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class StudentTest {

    @Test
    public void testMeetsPrereq() {
        Student s = new Student("Sang Ik", "Han", "sangik_id", "sangik_pw", "CIT");
        ArrayList<String> pastCourses = new ArrayList<String>();
        s.setPastCoursess(pastCourses);
        String prereqStr = "ECON 101 AND ECON 103 AND MATH 104 AND (MATH 114 OR MATH 115)";
        assertFalse(s.meetsPrereq(prereqStr));
        
        s.addPastCourse("ECON 101");
        s.addPastCourse("ECON 103");
        s.addPastCourse("MATH 104");
        assertFalse(s.meetsPrereq(prereqStr));
        
        s.addPastCourse("MATH 114");
        assertTrue(s.meetsPrereq(prereqStr));
    }

}
