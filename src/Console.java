

import java.util.ArrayList;
import java.util.Scanner;

import org.bson.Document;


public class Console implements IConsole {
//    private int currentUser;
//    private Registrar r;
    private IPerson p;
    Database db;
//    db.openClient();
//    db.printAllCourses();
//    db.closeClient();
    
    Console() {
        p = null; //initialize p as -1
        db = new Database(); //r needs to keep track of student id/pw
    }
    
    public void setCurrentUser(IPerson user) {
        p = user; //IPerson object from registrar directory with matching ID
    };
    
    public IPerson getCurrentUser() {
        return p;
    };
    
    public void promptLogin() {
        Scanner s = new Scanner(System.in);
        System.out.println("What is your id?");
        String id = s.nextLine();
        System.out.println("What us your password?");
        String pw = s.nextLine();
        s.close();
        login(id, pw);
    }
    
    public void promptStudentMenu() {
        Scanner s = new Scanner(System.in);
        System.out.println("Please type the integer value for next action.");
        System.out.println("1. View current courses 2. View past courses 3. Add a new course 4. Search for courses 5. Logout");
        int selection = s.nextInt();
        
        if (selection < 1 || selection > 5) {
            System.out.println("Your selection is out of bound. Select again");
            promptStudentMenu();
        }
        
        //find course info
        //view my course info
        //update my course schedule
        
        if (selection == 1) {
            printCurrentCourses();
        } else if (selection == 2) {
            printPastCourses()
        } else if (selection == 3) {
            addCourse();
        } else if (selection == 4) {
            searchCourseBySubject();
        } else {
            logout();
        }
        s.close();
    }
    
    
    @Override
    public int login(String id, String pw) {
        db.openClient();
        Document p = db.findPersonById(id);
        db.closeClient();
        
        if (p == null) {
            System.out.println("Requested student ID doesn't exist.");
            return -1;
        }
        
        String dbpw = (String) p.get("password");
        
        if (dbpw.equals(pw)) {
            
            String firstName = (String) p.get("firstName");
            String lastName = (String) p.get("lastName");
            String program = (String) p.get("program");
            ArrayList<Document> courses = (ArrayList<Document>) p.get("courses");
            ArrayList<String> pastCourses = (ArrayList<String>) p.get("pastCourses");
            
            ArrayList<ICourse> curCourses = new ArrayList<ICourse>();
            
            for (Document d : courses) {
                ICourse c = db.findSection( (String)d.get("subject"), (int)d.get("number"), (int)d.get("section"));
                curCourses.add(c);
            }
            
            boolean isInstructor = (boolean) p.get("isInstructor");
            if (isInstructor) {
                Instructor curI = new Instructor();
                setCurrentUser(curI);
            } else {
                Student curS = new Student(firstName, lastName, id, pw, program);
                curS.setCourses(curCourses);
                curS.setPastCoursess(pastCourses);
                setCurrentUser(curS);
            }
            return 0;
            
        } else {
            System.out.println("Please check your password.");
            return -1;
        }
    }

    @Override
    public void logout() {
        setCurrentUser(null);
    }

    @Override
    public int currentUser() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    public void addCourse() {
        if (p == null) {
            System.out.println("Please first login.");
        } else if (p.isInstructor()) {
            System.out.println("Please login as student");
        } else {
            p.addCourse(String dept, int classNo);
            //error handling when exceeding course limit!
        }
    }
    
    public static void main(String[] args) {
        Console c = new Console();
        
        c.promptLogin();
        
        //ask for login
    }

}
