import java.util.Collection;
import java.util.Map;

public interface IPennParser {

    /**
     * Parses all subjects (code & name) from the UPenn catalog
     * 
     * @return Map of subject codes to subject names, e.g. (CIT, Computer and Information
     *         Technology)
     */
    public Map<String, String> parseSubjects();

    /**
     * Parses all possible courses (number & title) from the UPenn catalog
     * 
     * @param subjects Map of subject codes to subject names
     * @return Map of subject codes to Map of course numbers to course titles
     */
    public Map<String, Map<Integer, String>> parseCourses(Map<String, String> subjects);

    /**
     * Parses all course prerequisites (as strings) from the UPenn catalog
     * 
     * @param subjects Map of subject codes to subject names
     * @return Map of subject codes to Map of course numbers to prerequisites
     */
    public Map<String, Map<Integer, String>> parsePrereqs(Map<String, String> subjects);

    /**
     * Parses all available course sections for the upcoming semester from the UPenn time table
     * 
     * @param subjects Map of subject codes to subject names
     * @return Collection of all sections of all courses
     */
    public Collection<ICourse> parseSections(Map<String, String> subjects);
}
