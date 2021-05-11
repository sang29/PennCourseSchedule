import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Scanner;

import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

public class CourseTest {

    @Before
    public void setUp() throws Exception {
    }

    
    @Test
    public void testEqualsAndCompareTo() {
        Course c = new Course("CIT", 594, 001);
        Course c2 = new Course("CIT", 594);
        assertNotEquals(c, c2);
        c2.setSection(001);
        assertEquals(c, c2);
        
        
        assertEquals(c, new Course("CIT", 594, 001));
        assertNotEquals(c, new Course("ESE", 594, 001));
        assertNotEquals(c, new Course("CIT", 593, 001));
        assertNotEquals(c, new Course("CIT", 594, 201));
        assertNotEquals(c, new PennParser());
        
        
        int comp = c.compareTo(new Course("CIT", 594, 001));
        assertEquals(0, comp);
        comp = c.compareTo(new Course("CIT", 594, 201));
        assertTrue(comp < 0);
        comp = c.compareTo(new Course("CIT", 593, 001));
        assertTrue(comp > 0);
    }
    
    @Test
    public void testSetters() {
        Course c = new Course("CIT", 594, 001);
        c.setTitle("Data Structures & Software Design");
        assertEquals("Data Structures & Software Design", c.title());
        c.setInstructorStr("Fouh");
        assertEquals("Fouh", c.instructorStr());
        c.setType("Lecture");
        assertEquals("Lecture", c.type());
        c.setUnits(1.0);
        assertEquals(1.0, c.units(), 0.1);
        c.setMax(50);
        assertEquals(50, c.max());
        assertEquals("CIT-594-001 Data Structures & Software Design", c.toString());
        assertFalse(c.isFull());
        assertEquals(0, c.current());
        c.setCurrent(50);
        assertEquals(50, c.current());
        assertTrue(c.isFull());
        
    }
    
    
    @Test
    public void testMeetingTime() {
        Course c = new Course("CIT", 594, 001);
        c.setDays("MW");
        c.setStartTime(10, 30);
        c.setDuration(90);
        assertEquals(90, c.duration());
        assertEquals(new LocalTime(10, 30), c.startTime());
    }
    
    @Test
    public void testMeetingDays() {
        Course c = new Course("CIT", 594, 001);
        c.setDays("MW");
        assertEquals("MW", c.daysToString());
        boolean[] days = {true, false, true, false, false};
        assertArrayEquals(days, c.days());
    }
    
    @Test
    public void testConflictsWith() {
        Course c1 = new Course("CIT", 595, 001);
        c1.setDays("MW");
        c1.setStartTime(10, 30);
        c1.setDuration(90);
        
        Course c2 = new Course("CIS", 548, 001);
        c2.setDays("MW");
        c2.setStartTime(10, 30);
        c2.setDuration(90);
        assertTrue(c1.conflictsWith(c2));
        c2.setDays("TR");
        assertFalse(c1.conflictsWith(c2));
        
    }
}
