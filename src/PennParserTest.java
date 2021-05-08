import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class PennParserTest {

    PennParser p;

    @Before
    public void setUp() throws Exception {
        p = new PennParser();
    }

    @Test
    public void testPennParser() {
        Map<String, String> subjects = p.parseSubjects();
        assertEquals("Art & Archaeology Of The Mediterranean World", subjects.get("AAMW"));
        assertEquals("Computer & Information Technology", subjects.get("CIT"));
        assertEquals("Zulu", subjects.get("ZULU"));
        assertEquals("Electrical & Systems Engineering", subjects.get("ESE"));
        assertEquals("Wharton Undergraduate", subjects.get("WH"));

        Map<String, Map<Integer, String>> courses = p.parseCourses(subjects);
        // misspelled on website
        assertEquals("Data Structures and Sofware Design", courses.get("CIT").get(594));
        assertEquals("Operating Systems Design and Implementation", courses.get("CIS").get(548));
        assertEquals("Existential Despair", courses.get("RELS").get(256));
        assertEquals("Operating Systems Design and Implementation", courses.get("CIS").get(548));
        assertEquals("Philadelphia, 1700-2000", courses.get("HIST").get(367));

        Collection<ICourse> sections  = p.parseSections(subjects);
        assertTrue(sections.contains(new Course("CIT", 594, 501)));
        assertTrue(sections.contains(new Course("ARTH", 729, 401)));
    }

}
