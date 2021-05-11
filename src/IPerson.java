import java.util.*;

public interface IPerson {
    
    /**
     * GETTERS & SETTERS
     * @return
     */
    String getFirstName();
    String getLastName();
    String getId();
    String getPassword();
    ArrayList<String> getPastCourses();
    
    /**
     * 
     * @return list of course objects under the current person
     */
    ArrayList<ICourse> getCourses();
    
    
    /**
     * 
     * @return boolean value of whether the person is an instructor
     */
    Boolean isInstructor();
    
    /**
     * 
     * @param c course object to be added to the person
     */
    void addCourse(ICourse c);
  
    /**
     * print out the current schedule (for both student 
     * and instructor) 
     * 
     * print out error message if the user is not logged in
     */
    void printCourses();
    
    
    
    
}
