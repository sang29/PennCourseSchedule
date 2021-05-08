import java.util.Collection;
import java.util.Map;

public interface IRegistrar {

    /**
     * @return Map of course subject codes to collection of ICourse objects
     */
    public Map<String, Map<Integer, ICourse>> buildCourseDirectory();
    
    /**
     * @return Map of IDs to students
     */
    public Map<Integer, IPerson> buildStudentDirectory();
    
    /** 
     * @param subject   Course subject
     * @return          Collection of courses of the given subject
     */
    public Collection<ICourse> coursesBySubject(String subject);
    
    /** 
     * @param subject   Course subject
     * @param low       Minimum course level
     * @param high      Maximum course level
     * @return
     */
    public Collection<ICourse> coursesBySubjectAndLevel(String subject, int low, int high);
    
    /** 
     * @param subject   Course subject
     * @param low       Minimum course level
     * @return
     */
    public Collection<ICourse> coursesBySubjectAndLevel(String subject, int low);
    
    /**
     * @param subject   Course subject
     * @param id        Course ID
     * @return          Collection of prerequisite courses
     */
    public Collection<ICourse> prerequisites(String subject, int id);
    
    
}
