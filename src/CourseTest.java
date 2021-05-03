import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CourseTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test() {
        Course c = new Course("CIT", 594, 001);
        c.setDays("MW");
        c.setStartTime(15, 00);
        c.setDuration(90);
        assertEquals(90, c.duration());
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
