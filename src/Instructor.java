
public class Instructor implements IPerson{
    private String name;
    private int id;
    private int password;
    private String program;
    
    private int[] courses; //list of courses to teach
    
    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setName(String name) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int getId() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setId() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int login(int id, int pw) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int logout(int id, int pw) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void printSchedule() {
        // TODO Auto-generated method stub
        
    }
    
    //change course info -> maybe get rid of it later?
    /**
     * 
     * @param courseNo
     * @param cap - set the cap to this new number
     * @return
     */
    public int changeCourseCap(int courseNo, int cap) {
        return 0;
    }
    
    void takePermRequest(Course c, IPerson student) {
        
    }
    
    /**
     * 
     * @return 2D matrix with courseNo and studentID for request
     */
    public int[][] permRequests(){
        return null;
    }
    
    //give permission to the student for given class
    /**
     * 
     * @param studentId
     * @param courseNo
     * @return -1 upon failure and 0 upon success
     * potential failure cases - course already full,
     * given studentId is not on the request list
     * courseNo doesn't belong to the instructor
     */
    int givePerm(int studentId, int courseNo) {
        return 0;
    }
    
    

}
