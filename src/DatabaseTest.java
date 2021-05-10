import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.joda.time.LocalTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DatabaseTest {

    Database db = new Database();
    
    @Before
    public void setUp() throws Exception {
        db.openClient();
    }

    @Test
    public void testDatabaseInitialization() {
        // Switch over to the "demo" database
        db.switchDatabase("demo");
        // Confirm that we are connected to the "demo" database
        assertEquals("demo", db.name());
        // Clear all existing collections in the database
        db.clearCollections();
        // Confirm that the collections have been cleared
        assertEquals(0, db.collections().size());
        // Initialize "subjects", "courses", and "sections_fall2021" collections
        db.initializeDatabase();
        
        // Confirm that the "demo" database has the expected collections
        List<String> collections = db.collections();
        assertTrue(collections.contains("subjects"));
        assertTrue(collections.contains("courses"));
        assertTrue(collections.contains("sections_fall2021"));
        
        // Confirm that course
        List<ICourse> courses = db.findCoursesBySubject("CIT");
        assertTrue(courses.contains(new Course("CIT", 590)));
        assertTrue(courses.contains(new Course("CIT", 591)));
        assertTrue(courses.contains(new Course("CIT", 592)));
        assertTrue(courses.contains(new Course("CIT", 593)));
        assertTrue(courses.contains(new Course("CIT", 594)));
        assertTrue(courses.contains(new Course("CIT", 595)));
        assertTrue(courses.contains(new Course("CIT", 596)));
        
        courses = db.findCourseBySubjectAndNumber("ZULU", 451);
        System.out.println(courses);       
        
        List<ICourse> sections = db.findSectionsByCourseAndType("CIT", 594, "Online Course");
        assertEquals(1, sections.size());
        
        Course section = (Course)sections.get(0);
        assertEquals("CIT", section.subject());
        assertEquals(594, section.id());
        assertEquals(501, section.section());
        assertEquals("Online Course", section.type());
        assertEquals("", section.daysToString());
        assertEquals("Rubin", section.instructor().getName());
        
        section = db.findSection("CIS", 557, 001);
        assertEquals("CIS", section.subject());
        assertEquals(557, section.id());
        assertEquals(001, section.section());
        assertEquals("Lecture", section.type());
        assertEquals("TR", section.daysToString());
        assertEquals(new LocalTime(13, 45), section.startTime());
        assertEquals(90, section.duration());
        assertEquals("Fouh", section.instructor().getName());
        
        sections = db.findSectionsByCourseAndType("CIS", 110, "Recitation");
        assertEquals(22, sections.size());
        
        
        sections = db.findSectionsByCourseAndType("NURS", 215, "Clinic");
        assertEquals(16, sections.size());
        
        
        
        
        
    }
    
    @After
    public void breakDown() throws Exception {
        db.closeClient();
    }
    
}
