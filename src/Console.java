

import java.util.List;
import java.util.Scanner;

public class Console implements IConsole {
    private IPerson p; // currentUser
    private Database db;
    private Boolean isInstructor;

    Console() {
        p = null; // initialize p as null
        db = new Database(); // r needs to keep track of student id/pw
    }

    public void setCurrentUser(IPerson user) {
        p = user; // IPerson object from registrar directory with matching ID
    };

    public IPerson getCurrentUser() {
        return p;
    };

    // ------------------------------------------------------------------------------------------
    /* LOGIN LOGOUT */
    public void promptLogin() {
        Scanner s = new Scanner(System.in);
        System.out.println("What is your id?");
        String id = s.nextLine();
        System.out.println("What us your password?");
        String pw = s.nextLine();

        if (login(id, pw) == -1) {
            promptLogin();
        }
    }

    @Override
    public int login(String id, String pw) {
        db.openClient();
        IPerson user = db.findStudentById(id);

        if (user == null) {
            user = db.findInstructorById(id);
            if (user == null) {
                System.out.println("Requested student ID doesn't exist. Please try again");
                db.closeClient();
                return -1;
            } else {
                isInstructor = true;
            }
        } else {
            isInstructor = false;
        }

        String dbpw = user.getPassword(); // get pw from db

        if (dbpw.equals(pw)) {
            setCurrentUser(user);
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

    // ------------------------------------------------------------------------------------------
    /* MENU PROMPTING */
    public void promptStudentMenu() {
        Student loggedinStudent = (Student) getCurrentUser();

        System.out.println("Please type the integer value for next action.");
        System.out.printf(
                "1. View current courses \n2. View past courses \n3. Add a new course \n4. View courses by subject \n5. Request course permission \n6. Logout \n");

        Scanner s = new Scanner(System.in);

        String selectionStr;
        int selection;
        selectionStr = s.nextLine();
        try {
            selection = Integer.parseInt(selectionStr);
            if (selection < 1 || selection > 6) {
                System.out.println("Your selection is out of bound. Select again");
                promptStudentMenu();
            }

            if (selection == 1) {
                loggedinStudent.printCourses();
            } else if (selection == 2) {
                loggedinStudent.printPastCourses();
            } else if (selection == 3) {
                System.out.println("Please type subject code.");
                String subject = s.nextLine();
                System.out.println("Please type course number.");
                int number = s.nextInt();
                s.nextLine();
                System.out.println("Please type section number.");
                int section = s.nextInt();
                s.nextLine();
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
                s.nextLine();
                System.out.println("Please type section number.");
                int section = s.nextInt();
                s.nextLine();
                sendPermRequest(loggedinStudent.getId(), subject, number, section);
            } else {
                logout();
            }
            System.out.println();
        } catch (NumberFormatException e) {
            System.out.println("ERROR: " + s + " is not a number.");
        }
    }

    public void promptInstructorMenu() {
        Instructor loggedinInstructor = (Instructor) getCurrentUser();

        System.out.println("Please type the integer value for next action.");
        System.out.printf("1. View current courses \n2. View waitlist \n3. Approve waitlist \n4. Logout \n");

        Scanner s = new Scanner(System.in);
        String selectionStr;
        int selection;
        selectionStr = s.nextLine();
        try {
            selection = Integer.parseInt(selectionStr);
            if (selection < 1 || selection > 4) {
                System.out.println("Your selection is out of bound. Select again");
                promptInstructorMenu();
            }

            if (selection == 1) {
                loggedinInstructor.printCourses();
            } else if (selection == 2) {
                loggedinInstructor.printWaitlist();
            } else if (selection == 3) {
                System.out.println("Please type the subject for approval.");
                String subject = s.nextLine();
                System.out.println("Please type the number for approval.");
                int number = s.nextInt();
                s.nextLine();
                System.out.println("Please type the section for approval.");
                int section = s.nextInt();
                s.nextLine();
                System.out.println("Please type the student_id for approval.");
                String student_id = s.nextLine();
                givePermToStudent(student_id, subject, number, section);
            } else {
                logout();
            }

        } catch (NumberFormatException e) {
            System.out.println("ERROR: " + s + " is not a number.");
        }

    }

    // ------------------------------------------------------------------------------------------
    /* STUDENT SUBMETHODS */
    public void sendPermRequest(String studentId, String subject, int number, int section) {
        db.openClient();
        db.sendPermRequest(studentId, subject, number, section);
        db.closeClient();
        System.out.println("Just sent a permission request!");
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
            if (p.getCourses().size() > 4) {
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
                    System.out.printf("%s has time conflicts with %s\n", c.toString(), curCourse.toString());
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

    // ------------------------------------------------------------------------------------------
    /* INSTRUCTOR SUBMETHOD */
    public void givePermToStudent(String studentId, String subject, int number, int section) {
        db.openClient();
        db.pushCourseToStudent(studentId, subject, number, section);
        // need to take the student off of the waitlist too!
        db.closeClient();
        System.out.println("Permission granted");
    }

    // ------------------------------------------------------------------------------------------
    /* COMMON SUBMETHOD */
    public void printCoursesBySubject(String subject) {
        db.openClient();
        List<ICourse> courseList = db.findCoursesBySubject(subject);
        for (ICourse c : courseList) {
            System.out.println(c.toString());
        }
        System.out.println();
        db.closeClient();
    }

    // ------------------------------------------------------------------------------------------
    /* CONSOLE MAIN */
    public static void main(String[] args) {

        Console c = new Console();
        c.promptLogin();

        while (true) {
            if (c.getCurrentUser() == null) {
                // prompt login again if the user logged out
                c.promptLogin();
            } else {
                if (!c.getCurrentUser().isInstructor()) {
                    // prompt until the user logs out
                    c.promptStudentMenu();
                } else {
                    c.promptInstructorMenu();
                }
            }
        }
    }

}
