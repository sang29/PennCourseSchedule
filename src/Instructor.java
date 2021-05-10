import java.util.ArrayList;
import java.util.HashMap;

public class Instructor implements IPerson{
    private String firstName;
    private String lastName;
    private String id;
    private String password;
    private String program;
    private ArrayList<ICourse> courses; //list of courses to teach
    private HashMap<String, ArrayList<String>> waitlist;
    
    Instructor(String firstName, String lastName, String id, String password, String program) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
        this.password = password;
        this.program = program;
        this.courses = new ArrayList<ICourse>();
        this.waitlist = new HashMap<String, ArrayList<String>>();
    }
    
    public void setCourses(ArrayList<ICourse> courses) {
        this.courses = courses;
    }
    
    public void setWaitlist(HashMap<String, ArrayList<String>> waitlist) {
        this.waitlist = waitlist;
    }
    
    public HashMap<String, ArrayList<String>> getWaitlist() {
        return this.waitlist;
    }
    
    @Override
    public String getFirstName() {
        return this.firstName;
    }
    
    @Override
    public String getLastName() {
        return this.lastName;
    }


    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return this.id;
    }

    
    @Override
    public void printCourses() {
        for (ICourse c: getCourses()) {
            System.out.println(c.toString());
        }
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

    @Override
    public ArrayList<ICourse> getCourses() {
        return this.courses;
    }

    @Override
    public void addCourse(ICourse c) {
        if (!this.courses.contains(c)) {
            this.courses.add(c);
        } else {
            System.out.print("Requested course in already in the courses list");
        }
    }

    @Override
    public void dropCourse(ICourse c) {
        if (this.courses.contains(c)) {
            this.courses.remove(c);
        } else {
            System.out.print("Requested course is not in the courses list");
        } 
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public Boolean isInstructor() {
        return true;
    }

}
