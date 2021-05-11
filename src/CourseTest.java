import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;

import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

public class CourseTest {

    @Before
    public void setUp() throws Exception {
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
    }
}
