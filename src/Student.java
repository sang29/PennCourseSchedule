import java.util.*;

public class Student implements IPerson{
    private String name;
    private int id;
    private int password;
    private String program;
    
    private ArrayList<Course> courses; //courses to take this upcoming semester
    private ArrayList<Course> pastCourses; //list of courses taken
//    private int[] cart;
    
    Student(int id, int password, String name, String program) {
        this.id = id;
        this.password = password;
        this.program = program;
        this.name = name;
        
        this.courses = new ArrayList<Course>();
        this.pastCourses = new ArrayList<Course>();
    }
    
    @Override
    public ArrayList<Course> getCourses() {
        return courses;
    }

    @Override
    public ArrayList<Course> getPastCourses() {
        return pastCourses;
    }

    @Override
    public void addCourse(Course c) {
       
        c.checkPrereq(); //check prerequisite
        c.checkPerm(); //check if permissoin required
        requestPerm(c);//send permission request if permission required
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

    
}
