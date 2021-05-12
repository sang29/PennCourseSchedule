import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that represents an instructor in the database
 * @author Sanghik Han & Philipp Gaissert
 *
 */
public class Instructor implements IPerson {
    private String firstName;
    private String lastName;
    private String id;
    private String password;
    private String program;
    private ArrayList<ICourse> currentCourses; // list of courses to teach
    private HashMap<String, ArrayList<String>> waitlist;

    Instructor(String firstName, String lastName, String id, String password, String program) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
        this.password = password;
        this.program = program;
        this.currentCourses = new ArrayList<ICourse>();
        this.waitlist = new HashMap<String, ArrayList<String>>();
    }
    
    // ------------------------------------------------------------------------------------------
    /* IPERSON IMPLEMENTATION */
    
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
        for (ICourse c : getCurrentCourses()) {
            System.out.format(c.toString() + "\n");
        }
    }
    
    @Override
    public ArrayList<ICourse> getCurrentCourses() {
        return this.currentCourses;
    }

    @Override
    public void addCourse(ICourse c) {
        if (!this.currentCourses.contains(c)) {
            this.currentCourses.add(c);
        } else {
            System.out.format("Requested course in already in the courses list.\n");
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
    
    // ------------------------------------------------------------------------------------------
    /* INSTRUCTOR SPECIFIC METHODS */
    /**
     * Takes in the course list for the student and sets the student course list
     * 
     * @param courses: ArrayList<ICourse>
     */
    public void setCurrentCourses(ArrayList<ICourse> courses) {
        this.currentCourses = courses;
    }
    
    /**
     * Takes in the wait list for classes that the instructor is responsible for
     * 
     * @param waitlist, which maps each course to a list of student id's waiting for this class
     */
    public void setWaitlist(HashMap<String, ArrayList<String>> waitlist) {
        this.waitlist = waitlist;
    }
    
    /**
     * Getter for waitlist
     * @return
     */
    public HashMap<String, ArrayList<String>> getWaitlist() {
        return this.waitlist;
    }
    /**
     * Prints the list of student for each class that the instructor is responsible for
     */
    public void printWaitlist() {
        if (this.waitlist.size() == 0) {
            System.out.format("Your waitlist is empty.\n");
        } else {
            for (Map.Entry<String, ArrayList<String>> entry : this.waitlist.entrySet()) {
                System.out.format("Waitlist for %s:\n", entry.getKey());
                ArrayList<String> studentList = entry.getValue();
                for (String s : studentList) {
                    System.out.format("\t %s\n", s);
                }
            }
        }
    }

   

}
