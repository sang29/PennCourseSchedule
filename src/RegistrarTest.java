import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class RegistrarTest {

    IRegistrar reg;
    @Before
    public void setUp() throws Exception {
        reg = new Registrar();
    }

    @Test
    public void testParseSubjects() {
        Map<String, String> subjects = reg.parseSubjects();
        assertEquals("Art & Archaeology Of The Mediterranean World", subjects.get("AAMW"));
        assertEquals("Computer & Information Technology", subjects.get("CIT"));        
    }
    
//    @Test
//    public void testParseCatalog() {
//        Map<String, String> subjects = reg.parseSubjects();
//        System.out.println(subjects);
//        Map<String, Map<Integer, String>> catalog = reg.parseCatalog(subjects);
//        
//        
//    }
    
//    @Test
//    public void testBuildCourseDirectory() {
//        Map<String, String> subjects = reg.parseSubjects();
//        reg.buildCourseDirectory(subjects);
//    }
}
