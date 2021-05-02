import java.util.Collection;
import java.util.Map;

public interface IRegistrar {

    
    /**
     * @return Map of course subject codes to subject names,
     *         e.g. (CIT, Computer and Information Technology)
     */
    public Map<String, String> parseSubjects();
    
    /**
     * @return Map of course subject codes to collection of ICourse objects
     */
    public Map<String, Map<Integer, ICourse>> buildCourseDirectory(Map<String, String> subjects);
    
    
    /**
     * @return
     */
    public Map<Integer, Collection<IPerson>> buildStudentDirectory();
    
    
}
