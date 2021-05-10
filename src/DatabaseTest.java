import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

public class DatabaseTest {

    Database db = new Database();
    
    @Before
    public void setUp() throws Exception {
        db.openClient();
    }

    @Test
    public void testFindSectionsByCourseAndType() {
        List<ICourse> sections = db.findSectionsByCourseAndType("CIT", 594, "Online Course");
        assertEquals(1, sections.size());
        Course section = (Course)sections.get(0);
        assertEquals("CIT", section.subject());
        assertEquals(594, section.id());
        assertEquals(501, section.section());
        assertEquals("Online Course", section.type());
        assertEquals("", section.daysToString());
        assertEquals("Rubin", section.instructorStr());
        
        
        sections = db.findSectionsByCourseAndType("CIS", 110, "Recitation");
        assertEquals(22, sections.size());
        
        
        sections = db.findSectionsByCourseAndType("NURS", 215, "Clinic");
        assertEquals(16, sections.size());
    }
    
    @Test
    public void testFindCoursesBySubject() {
        List<ICourse> courses = db.findCoursesBySubject("CIT");
        assertTrue(courses.contains(new Course("CIT", 520)));
        assertTrue(courses.contains(new Course("CIT", 582)));
        assertTrue(courses.contains(new Course("CIT", 590)));
        assertTrue(courses.contains(new Course("CIT", 591)));
        assertTrue(courses.contains(new Course("CIT", 592)));
        assertTrue(courses.contains(new Course("CIT", 593)));
        assertTrue(courses.contains(new Course("CIT", 594)));
        assertTrue(courses.contains(new Course("CIT", 595)));
        assertTrue(courses.contains(new Course("CIT", 596)));
    }
    
    @Test
    public void testPushStudentToDatabase() {
        db.deleteStudentById("sangik_id");
        db.pushStudentToDatabase("Sang Ik", "Han", "CIT", "sangik_id", "sangik_pw");
        String firstName = db.findStudentById("sangik_id").getFirstName();
        assertEquals(firstName, "SANG IK");
        db.deleteStudentById("sangik_id");
    }
    
    @Test
    public void testPushInstructorToDatabase() {
        db.deleteInstructorById("arvind_id");
        db.pushInstructorToDatabase("Arvind", "Bhusnurmath", "CIT", "arvind_id", "arvind_pw");
        String firstName = db.findInstructorById("arvind_id").getFirstName();
        assertEquals(firstName, "ARVIND");
        db.deleteInstructorById("arvind_id");
    }
    
    @Test
    public void testPushCourseToStudent() {
        int sectionCurrent;
        Student s;
        
        db.deleteStudentById("sangik_id");
        db.pushStudentToDatabase("Sang Ik", "Han", "CIT", "sangik_id", "sangik_pw");
        sectionCurrent = db.findSection("CIT", 591, 1).current();
        assertEquals(sectionCurrent, 0); //course current starts at 0
        s = db.findStudentById("sangik_id");
        assertEquals(s.getCourses().size(), 0);//no course for this student
        
        db.pushCourseToStudent("sangik_id", "CIT", 591, 1);
        
        s = db.findStudentById("sangik_id");
        ArrayList<ICourse> courses = s.getCourses();
        ICourse firstCourse = courses.get(0);
        assertEquals(firstCourse.subject(), "CIT");
        
        sectionCurrent = db.findSection("CIT", 591, 1).current();
        assertEquals(sectionCurrent, 1); //course current incremented by one
        
        db.deleteCourseFromStudent("sangik_id", "CIT", 591, 1);
        db.deleteStudentById("sangik_id");
        
    }
    
    @Test
    public void testPushPastCourseToStudent() {
        db.deleteStudentById("sangik_id");
        db.pushStudentToDatabase("Sang Ik", "Han", "CIT", "sangik_id", "sangik_pw");
        db.pushPastCourseToStudent("sangik_id", "CIT", 591);
        db.pushPastCourseToStudent("sangik_id", "CIT", 592);
        
        Student s = db.findStudentById("sangik_id");
        assertEquals("CIT 591", s.getPastCourses().get(0));
        assertEquals("CIT 592", s.getPastCourses().get(1));
        db.deleteStudentById("sangik_id");
    }
    
    @Test
    public void testSendPermRequest() {
        Instructor i;
        HashMap<String, ArrayList<String>> waitlist;
        
        db.deleteStudentById("sangik_id");
        db.deleteStudentById("philipp_id");
        db.deleteInstructorById("eric_id");
        
        db.pushStudentToDatabase("Sang Ik", "Han", "CIT", "sangik_id", "sangik_pw");
        db.pushStudentToDatabase("Philipp", "Gaissert", "CIT", "philipp_id", "philipp_pw");
 
        //CIS 557
        db.pushInstructorToDatabase("Eric", "Fouh", "CIS", "eric_id", "eric_pw");
        i = db.findInstructorById("eric_id");
        waitlist = i.getWaitlist();
        assertEquals(waitlist.size(), 0);
        
        db.sendPermRequest("sangik_id", "CIS", 557, 1);
        i = db.findInstructorById("eric_id");
        waitlist = i.getWaitlist();
        
        for (Map.Entry<String, ArrayList<String>> entry : waitlist.entrySet()) {
            assertEquals("CIS 557 1", entry.getKey());
            assertEquals("sangik_id", entry.getValue().get(0));
         }
        
        db.sendPermRequest("philipp_id", "CIS", 557, 1); //add one more permission request
        i = db.findInstructorById("eric_id");
        waitlist = i.getWaitlist();
        
        for (Map.Entry<String, ArrayList<String>> entry : waitlist.entrySet()) {
            assertEquals("CIS 557 1", entry.getKey());
            assertEquals("sangik_id", entry.getValue().get(0));
            assertEquals("philipp_id", entry.getValue().get(1));
         }
        
        db.deleteStudentById("sangik_id");
        db.deleteStudentById("philipp_id");
        db.deleteInstructorById("eric_id");
    }


    
    @After
    public void breakDown() throws Exception {
        db.closeClient();
    }
    
}
