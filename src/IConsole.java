
public interface IConsole {
    /**
     * Log in the user and give the user edit privilege 
     * for their own course schedule
     * 
     * @param id
     * @param pw
     * @return -1 upon failure, 0 upon success
     */
    int login(String id, String pw, boolean isInstructor);
    
    /**
     * Log out the user and take away the edit privilege 
     */
    void logout();
    
    /**
     * 
     * @return Id of the currently logged in user, -1 if no one is logged in
     */
    int currentUser();
}
