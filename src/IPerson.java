
public interface IPerson {
    
    /*
     * maybe login/logout needs to be in a console?
     */
    
    String getName();
    void setName();
    int getId();
    void setId();
    
    /**
     * Log in the user and give the user edit privilege 
     * for their own course schedule
     * 
     * @param id
     * @param pw
     * @return -1 upon failure, 0 upon success
     */
    int login(int id, int pw);
    
    /**
     * Log out the user and take away the edit privilege 
     * 
     * @param id
     * @param pw
     * @return -1 upon failure, 0 upon success
     */
    int logout(int id, int pw);
    
    /**
     * print out the current schedule (for both student 
     * and instructor) 
     * 
     * print out error message if the user is not logged in
     */
    void printSchedule();
    
    
    
    
}
