
import java.util.List;
import java.util.Scanner;

/**
 * A class for students to register for courses, and for instructors to respond to permission
 * requests
 * 
 * @author pgaissert
 *
 */
public class Console implements IConsole {

    private IPerson currentUser;
    private Database db;

    Console() {
        currentUser = null; // initialize p as null
        db = new Database(); // r needs to keep track of student id/pw
    }

    public void setCurrentUser(IPerson user) {
        currentUser = user; // IPerson object from registrar directory with matching ID
    };

    public IPerson getCurrentUser() {
        return currentUser;
    };

    // ------------------------------------------------------------------------------------------
    /* LOGIN LOGOUT */
    public void promptLogin() {
        Scanner s = new Scanner(System.in);
        System.out.format("What is your id?\n");
        String id = s.nextLine();
        System.out.format("What us your password?\n");
        String pw = s.nextLine();

        if (login(id, pw) == -1) {
            // login(id, pw) == -1 means that the login failed
            // thus, re-prompt the user for login with the error message
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
                System.out.format("Requested student ID doesn't exist. Please try again.\n");
                db.closeClient();
                return -1;
            }
        } else {
        }

        String dbpw = user.getPassword(); // get pw from db

        if (dbpw.equals(pw)) {
            setCurrentUser(user);
            db.closeClient();
            return 0;

        } else {
            System.out.format("Please check your password and try login again.\n");
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
    // ** Prompting works differently based on whether the user is a student or
    // instructor
    public void promptStudentMenu() {
        Student loggedinStudent = (Student) getCurrentUser();

        System.out.format("Please type the integer value for next action.\n");
        System.out.format(
                "1. View current courses\n" + "2. View past courses\n" + "3. Add a new course\n"
                        + "4. View courses by subject\n" + "5. Request course permission\n"
                        + "6. Logout\n");

        Scanner s = new Scanner(System.in);

        String selectionStr;
        int selection;
        selectionStr = s.nextLine();
        try {
            selection = Integer.parseInt(selectionStr);
            if (selection < 1 || selection > 6) {
                System.out.format("Your selection is out of bound. Please try again.\n");
                promptStudentMenu();
            }

            if (selection == 1) {
                loggedinStudent.printCourses();
            } else if (selection == 2) {
                loggedinStudent.printPastCourses();
            } else if (selection == 3) {
                System.out.format("Please type subject code.\n");
                String subject = s.nextLine();
                System.out.format("Please type course number.\n");
                int number = s.nextInt();
                s.nextLine();
                System.out.format("Please type section number.\n");
                int section = s.nextInt();
                s.nextLine();
                addSection(subject, number, section);
            } else if (selection == 4) {
                System.out.format("Please type subject code of your interest.\n");
                String subject = s.nextLine();
                printCoursesBySubject(subject);

            } else if (selection == 5) {
                System.out.format("Please type subject code.\n");
                String subject = s.nextLine();
                System.out.format("Please type course number.\n");
                int number = s.nextInt();
                s.nextLine();
                System.out.format("Please type section number.\n");
                int section = s.nextInt();
                s.nextLine();
                sendPermRequest(loggedinStudent.getId(), subject, number, section);
            } else {
                logout();
            }
            System.out.format("\n");
        } catch (NumberFormatException e) {
            System.out.format("ERROR: " + s + " is not a number.\n");
        }
    }

    public void promptInstructorMenu() {
        Instructor loggedinInstructor = (Instructor) getCurrentUser();

        System.out.format("Please type the integer value for next action.\n");
        System.out.format(
                "1. View current courses\n2. View waitlist\n3. Approve waitlist\n4. Logout\n");

        Scanner s = new Scanner(System.in);
        String selectionStr;
        int selection;
        selectionStr = s.nextLine();
        try {
            selection = Integer.parseInt(selectionStr);
            if (selection < 1 || selection > 4) {
                System.out.format("Your selection is out of bound. Please try again.\n");
                promptInstructorMenu();
            }

            if (selection == 1) {
                loggedinInstructor.printCourses();
            } else if (selection == 2) {
                loggedinInstructor.printWaitlist();
            } else if (selection == 3) {
                System.out.format("Please type the subject for approval.\n");
                String subject = s.nextLine();
                System.out.format("Please type the number for approval.\n");
                int number = s.nextInt();
                s.nextLine();
                System.out.format("Please type the section for approval.\n");
                int section = s.nextInt();
                s.nextLine();
                System.out.format("Please type the student_id for approval.\n");
                String studentId = s.nextLine();
                givePermToStudent(studentId, subject, number, section);
            } else {
                logout();
            }

        } catch (NumberFormatException e) {
            System.out.format("ERROR: " + s + " is not a number.\n");
        }
    }

    // ------------------------------------------------------------------------------------------
    /* STUDENT SUBMETHODS */
    /**
     * This method will put the student id to the wait list for the instructor responsible for given
     * class The database currently gives only instructor last name, so there could be conflicts
     * when there are multiple instructors with the same last name This will be resolved only when
     * the registrar provides unique identifier for each instructor
     * 
     * @param studentId
     * @param subject
     * @param number
     * @param section
     */
    public void sendPermRequest(String studentId, String subject, int number, int section) {
        db.openClient();
        db.sendPermRequest(studentId, subject, number, section);
        db.closeClient();
        System.out.format("Your request for permission has been sent!\n");
    }

    /**
     * Add the given course to the student Note that this method is used only after the student
     * meets all the preconditions (i.e. time conflict, prerequisites, etc.)
     * 
     * @param subject
     * @param number
     * @param section
     */
    public void addSection(String subject, int number, int section) {

        if (currentUser == null) {
            System.out.format("Please first login.\n");
        } else if (currentUser.isInstructor()) {
            System.out.format("Please login as student.\n");
        } else {

            db.openClient();
            ICourse c = db.findSection(subject, number, section);
            String prereqStr = db.getPrereq(subject, number);

            if (c == null) {
                System.out.format("Requested course is not offered in current semester.\n");
                db.closeClient();
                return;
            }

            // check prereq
            if (!((Student) currentUser).meetsPrereq(prereqStr)) {
                System.out.format("You don't meet the prereq for the course.\n");
                db.closeClient();
                return;
            }

            // check if the class is full
            if (c.max() <= c.current()) {
                System.out.format("Requested section is already full.\n");
                db.closeClient();
                return;
            }

            // check if permission required
            if (db.courseNeedsPerm(subject, number)) {
                System.out.format(
                        "This course requires permission from the instructor."
                                + "Please separately send a request.\n");
                db.closeClient();
                return;
            }

            // check if the student already has 5 classes
            if (currentUser.getCurrentCourses().size() > 4) {
                System.out.format("You cannot take more than 5 classes.\n");
                db.closeClient();
                return;
            }

            for (ICourse curCourse : currentUser.getCurrentCourses()) {
                // check duplicate course in the student courses
                if (c.subject().equals(curCourse.subject()) && c.number() == curCourse.number()
                        && c.section() == curCourse.section()) {
                    System.out.format("Requested course was already added to student schedule.\n");
                    db.closeClient();
                    return;
                }
                // check class time conflict
                if (c.conflictsWith(curCourse)) {
                    System.out.format("%s has time conflicts with %s\n", c.toString(),
                            curCourse.toString());
                    db.closeClient();
                    return;
                }
            }

            // check if the course was taken in the past
            String courseNo = subject + " " + Integer.toString(number);
            Student currentStudent = (Student) currentUser;
            if (currentStudent.getPastCourses().contains(courseNo)) {
                System.out.format("Requested course is already taken previously.\n");
                db.closeClient();
                return;
            }

            // Now meets all the conditions for adding the course
            // add course for IPerson
            currentUser.addCourse(c);

            // add course to the Database
            db.pushCourseToStudent(currentUser.getId(), subject, number, section);
            db.closeClient();
            return;
        }
    }

    /**
     * Allows the student to search courses offered in given subject for curent semester
     * 
     * @param subject
     */
    public void printCoursesBySubject(String subject) {
        db.openClient();
        List<ICourse> courseList = db.findCoursesBySubject(subject);
        for (ICourse c : courseList) {
            System.out.format(c.toString() + "\n");
        }
        System.out.format("\n");
        db.closeClient();
    }

    // ------------------------------------------------------------------------------------------
    /* INSTRUCTOR SUBMETHOD */
    /**
     * Instructor will grant permission to the corresponding student_id The class will be directly
     * added to the student profile in the backend
     * 
     * @param studentId
     * @param subject
     * @param number
     * @param section
     */
    public void givePermToStudent(String studentId, String subject, int number, int section) {
        db.openClient();
        db.pushCourseToStudent(studentId, subject, number, section);
        // need to take the student off of the waitlist too!
        db.closeClient();
        System.out.format("Permission granted!\n");
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
