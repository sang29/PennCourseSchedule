import java.util.*;

public interface IPerson {
    
    /*
     * maybe login/logout needs to be in a console?
     */
    
    String getName();
    void setName(String name);
    int getId();
    void setId();
    
    /**
     * 
     * @return list of course objects under the current person
     */
    ArrayList<ICourse> getCourses();
    
    /**
     * 
     * @return list of past courses
     */
    ArrayList<String> getPastCourses();
    
    /**
     * 
     * @return boolean value of whether the person is an instructor
     */
    Boolean isInstructor();
    
    /**
     * 
     * @param c course object to be added to the person
     */
    void addCourse(String subject, int number);
    
    /**
     * 
     * @param c course object to be dropped by the person
     */
    void dropCourse(String subject, int number);
//    
//    /**
//     * Log in the user and give the user edit privilege 
//     * for their own course schedule
//     * 
//     * @param id
//     * @param pw
//     * @return -1 upon failure, 0 upon success
//     */
//    int login(int id, int pw);
//    
//    /**
//     * Log out the user and take away the edit privilege 
//     * 
//     * @param id
//     * @param pw
//     * @return -1 upon failure, 0 upon success
//     */
//    int logout(int id, int pw);
//    
//    /**
//     * print out the current schedule (for both student 
//     * and instructor) 
//     * 
//     * print out error message if the user is not logged in
//     */
//    void printSchedule();
    
    
    
    
}
