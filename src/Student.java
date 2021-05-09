import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;

public class Student implements IPerson{
    private String firstName;
    private String lastName;
    private String program;
    private String id;
    private String password;
    private ArrayList<ICourse> courses;
    private ArrayList<String> pastCourses;
//    Database db;
    
//    private ArrayList<Course> courses; //courses to take this upcoming semester
//    private ArrayList<Course> pastCourses; //list of courses taken
    
    Student(String firstName, String lastName, String id, String password, String program) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
        this.password = password;
        this.program = program;
//        this.courses = courses;
//        this.pastCourses = pastCourses;
//        this.db = new Database();
    }
    
    public void setCourses(ArrayList<ICourse> courses) {
        this.courses = courses;
    }
    
    public void setPastCoursess(ArrayList<String> pastCourses) {
        this.pastCourses = pastCourses;
    }
    
    @Override
    public Boolean isInstructor() {
        return false;
    };
    
    @Override
    public ArrayList<ICourse> getCourses() {
        return courses;
    }

    @Override
    public ArrayList<String> getPastCourses() {
        return pastCourses;
    }

    @Override
    public void addCourse(String subject, int number) {
        db.openClient();
        String prereqStr = db.getPrereq(subject, number);
        db.closeClient();
        
        ArrayList<String> pastCourses = getPastCourses();
        
        if (!checkPrereq(prereqStr, pastCourses)) {
            System.out.println("You don't meet the prerequisites for this class.");
            return;
        }
        
        

//        c.checkPerm(); //check if permissoin required
//        requestPerm(c);//send permission request if permission required
        
        //create IPerson instructor field in a class so that we can send the request to the instructor.
        courses.add(c);
        
    }
    
    
    public void requestPerm(Course c) {
        c.instructor().takePermRequest(c, this);
    }
    
    public void dropCourse(Course c) {
        if (!courses.contains(c)) {
            System.out.printf("Course: %s doesn't exist in the user's course list.\n", c.title());
            return;
        }
        courses.remove(c);
    }

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
    
    
    int requestPerm(int classNo) {
        return 0;
    }
    
    public static void main(String[] args) {
        Database m = new Database();
        m.openClient();
        m.printAllCourses();
        m.pushStudentToDatabase("Sang Ik", "Han", "CIT", "sangik59x", "samplePassword!@#$");
        m.pushStudentToDatabase("Philipp", "Gaissert", "CIT", "philipp59x", "samplePassword!@#$");
        m.pushCourseToStudent("sangik59x", "CIT", 590);
        m.pushPastCourseToStudent("sangik59x", "CIT", 590);
        ArrayList<String> pastCourses = new ArrayList<String>();
        String prereqStr = "ECON 101 AND ECON 103 AND MATH 104 AND (MATH 114 OR MATH 115)";
        System.out.println(m.checkPrereq(prereqStr, pastCourses));
        pastCourses.add("ECON 101");
        pastCourses.add("ECON 103");
        pastCourses.add("MATH 104");
        pastCourses.add("MATH 114");
        System.out.println(m.checkPrereq(prereqStr, pastCourses));
        m.closeClient();

    }
    
}
