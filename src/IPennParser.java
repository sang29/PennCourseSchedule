import java.util.Collection;
import java.util.Map;

public interface IPennParser {
    
    
    /**
     * @return Map of subject codes to subject names,
     *         e.g. (CIT, Computer and Information Technology)
     */
    public Map<String, String> parseSubjects();
    
    /**
     * @param subjects  Map of subject codes to subject names
     * @return          Map of subject codes to Map of course numbers to course titles
     */
    public Map<String, Map<Integer, String>> parseCourses(Map<String, String> subjects);
    
    /**
     * 
     * @param subjects  Map of subject codes to subject names
     * @return          Map of subject codes to Map of course numbers to prerequisites
     */
    public Map<String, Map<Integer, String>> parsePrereqs(Map<String, String> subjects);
      
    /**
     * @param subjects  Map of subject codes to subject names
     * @return          Collection of all sections of all courses
     */
    public Collection<ICourse> parseSections(Map<String, String> subjects);
}
