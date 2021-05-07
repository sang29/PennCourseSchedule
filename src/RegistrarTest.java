import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
    
    @Test
    public void testParseCoursesAndPrereqs() {
        Map<String, String> subjects = reg.parseSubjects();
//        Map<String, Map<Integer, String>> courses = reg.parseCourses(subjects);
        Collection<ICourse> sections = ((Registrar)reg).parseSections(subjects);
        for (ICourse section : sections) {
            System.out.println(section);
        }
//        for (Entry<String, Map<Integer, String>> subject : catalog.entrySet()) {
//            for (Entry<Integer, String> course : subject.getValue().entrySet()) {
//                System.out.printf("%s %d - %s\n", subject.getKey(), course.getKey(), course.getValue());
//            }
//        }
        
    }
    
    
    
//    @Test
//    public void testBuildCourseDirectory() {
//        Map<String, String> subjects = reg.parseSubjects();
//        reg.buildCourseDirectory(subjects);
//    }
}
