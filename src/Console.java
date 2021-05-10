
import java.util.ArrayList;
import java.util.List;
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
        p = null; // initialize p as -1
        db = new Database(); // r needs to keep track of student id/pw
    }

    public void setCurrentUser(IPerson user) {
        p = user; // IPerson object from registrar directory with matching ID
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

        System.out.println("Are you an instructor? (Y/N)");
        String instructorStr = s.nextLine();

        Boolean isInstructor = false;
        if (instructorStr.equals("Y")) {
            isInstructor = true;
        } else if (instructorStr.equals("N")) {
            isInstructor = false;
        } else {
            System.out.println("Please type valid input");
            s.close();
            promptLogin();
        }
//        s.close();

        if (login(id, pw, isInstructor) == -1) {
            promptLogin();
        };
    }

    public void promptStudentMenu() {

        System.out.println("Please type the integer value for next action.");
        System.out.printf(
                "1. View current courses \n2. View past courses \n3. Add a new course \n4. View courses by subject \n5. Request course permission \n6. Logout \n");

        Scanner s = new Scanner(System.in);
        int selection = s.nextInt();
        s.nextLine();

        if (selection < 1 || selection > 6) {
            System.out.println("Your selection is out of bound. Select again");
            promptStudentMenu();
        }

        Student loggedinStudent = (Student) getCurrentUser();

        if (selection == 1) {
            loggedinStudent.printCourses();
        } else if (selection == 2) {
            loggedinStudent.printPastCourses();
        } else if (selection == 3) {
            System.out.println("Please type subject code.");
            String subject = s.nextLine();
            System.out.println("Please type course number.");
            int number = s.nextInt();
            System.out.println("Please type section number.");
            int section = s.nextInt();
            addSection(subject, number, section);
        } else if (selection == 4) {
            System.out.println("Please type subject code of your interest.");
            String subject = s.nextLine();
            printCoursesBySubject(subject);

        } else if (selection == 5) {
            System.out.println("Please type subject code.");
            String subject = s.nextLine();
            System.out.println("Please type course number.");
            int number = s.nextInt();
            System.out.println("Please type section number.");
            int section = s.nextInt();

            db.openClient();
            db.sendPermRequest(loggedinStudent.getId(), subject, number, section);
            db.closeClient();
        } else {
            logout();
        }
//        s.close();
    }

    @Override
    public int login(String id, String pw, boolean isInstructor) {
        db.openClient();
        Document p = db.findStudentById(id);

        if (p == null) {
            System.out.println("Requested student ID doesn't exist. Please try again");
            db.closeClient();
            return -1;
        }

        String dbpw = p.getString("password"); // get pw from db

        if (dbpw.equals(pw)) {

            String firstName = p.getString("firstName");
            String lastName = p.getString("lastName");
            String program = p.getString("program");
            System.out.printf("Welcome %s %s in %s program!\n", firstName, lastName, program);
            ArrayList<Document> courses = (ArrayList<Document>) p.get("courses");
            ArrayList<String> pastCourses = (ArrayList<String>) p.get("pastCourses");

            ArrayList<ICourse> curCourses = new ArrayList<ICourse>();

            for (Document d : courses) {
                ICourse c = db.findSection(d.getString("subject"), d.getInteger("number"), d.getInteger("section"));
//                System.out.printf("%s", c.toString());
                curCourses.add(c);
            }

            if (isInstructor) {
                Instructor curI = new Instructor();
                setCurrentUser(curI);
            } else {
                Student curS = new Student(firstName, lastName, id, pw, program);
                curS.setCourses(curCourses);
                curS.setPastCoursess(pastCourses);
                setCurrentUser(curS);
            }
            db.closeClient();
            return 0;

        } else {
            System.out.println("Please check your password and try login again.");
            db.closeClient();
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

    public void printCoursesBySubject(String subject) {
        db.openClient();
        List<ICourse> courseList = db.findCoursesBySubject(subject);
        for (ICourse c : courseList) {
            System.out.println(c.toString());
        }
        System.out.println();
        db.closeClient();
    }

    public void addSection(String subject, int number, int section) {

        if (p == null) {
            System.out.println("Please first login.");
        } else if (p.isInstructor()) {
            System.out.println("Please login as student");
        } else {

            db.openClient();
            ICourse c = db.findSection(subject, number, section);
            String prereqStr = db.getPrereq(subject, number);

            if (c == null) {
                System.out.println("Requested course is not offered in current semester.");
                db.closeClient();
                return;
            }

            // check prereq
            if (!((Student) p).meetsPrereq(prereqStr)) {
                System.out.println("You don't meet the prereq for the course.");
                db.closeClient();
                return;
            }

            // check if the class is full
            if (c.max() <= c.current()) {
                System.out.println("Requested section is already full.");
                db.closeClient();
                return;
            }

            // check if permission required
            if (db.courseNeedsPerm(subject, number)) {
                System.out
                        .println("This class needs instructor permission. Please separately send permission request.");
                db.closeClient();
                return;
            }

            // check if the student already has 5 classes
            if (p.getCourses().size() > 5) {
                System.out.println("You cannot take more than 5 classes.");
                db.closeClient();
                return;
            }

            for (ICourse curCourse : p.getCourses()) {
                // check duplicate course in the student courses
                if (c.subject().equals(curCourse.subject()) && c.id() == curCourse.id()
                        && c.section() == curCourse.section()) {
                    System.out.println("Requested course was already added to student schedule.");
                    db.closeClient();
                    return;
                }
                // check class time conflict
                if (c.conflictsWith(curCourse)) {
                    System.out.println("Requested course has time conflicts with current course selection.");
                    db.closeClient();
                    return;
                }
            }

            // check if the course was taken in the past
            String courseNo = subject + " " + Integer.toString(number);
            if (p.getPastCourses().contains(courseNo)) {
                System.out.println("Requested course is already taken previously.");
                db.closeClient();
                return;
            }

            // Now meets all the conditions for adding the course

            // add course for IPerson
            p.addCourse(c);

            // add course to the Database
            db.pushCourseToStudent(p.getId(), subject, number, section);
            db.closeClient();
            return;
        }
    }

    public static void main(String[] args) {
        Console c = new Console();

        c.promptLogin();

        while (c.getCurrentUser() != null) {
            if (!c.getCurrentUser().isInstructor()) {
                // prompt until the user logs out
                c.promptStudentMenu();
            }

        }
//        c.promptStudentMenu();

        // ask for login
    }

}
