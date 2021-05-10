import static org.junit.Assert.*;

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
    public void testDatabase() {
        assertEquals("registrar", db.name());
        List<String> collections = db.collections();
        assertTrue(collections.contains("subjects"));
        assertTrue(collections.contains("courses"));
        assertTrue(collections.contains("sections_fall2021"));
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
        assertEquals("Rubin", section.instructor().getName());
        
        
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

    
    @After
    public void breakDown() throws Exception {
        db.closeClient();
    }
    
}
