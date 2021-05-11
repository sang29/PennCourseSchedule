import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        return this.id;
    }

    
    @Override
    public void printCourses() {
        for (ICourse c: getCourses()) {
            System.out.println(c.toString());
        }
    }
    
    //returns null since instructor doesn't have data field for past courses
    @Override
    public ArrayList<String> getPastCourses() {
        return null;
    }
    
    public void printWaitlist() {
        if (this.waitlist.size() == 0) {
            System.out.println("Your waitlist is empty");
        } else {
            for (Map.Entry<String, ArrayList<String>> entry : this.waitlist.entrySet()) {
                System.out.printf("Waitlist for %s:\n", entry.getKey());
                ArrayList<String> studentList = entry.getValue();
                for (String s : studentList) {
                    System.out.printf("\t %s\n", s);
                }
            }
        }
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
    public String getPassword() {
        return this.password;
    }

    @Override
    public Boolean isInstructor() {
        return true;
    }

}
