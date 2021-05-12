import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that represents a student in the database
 * @author Sanghik Han & Philipp Gaissert
 *
 */
public class Student implements IPerson {
    private String firstName;
    private String lastName;
    private String program;
    private String id;
    private String password;
    private ArrayList<ICourse> currentCourses;
    private ArrayList<String> pastCourses;

    Student(String firstName, String lastName, String id, String password, String program) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
        this.password = password;
        this.program = program;
        this.currentCourses = new ArrayList<ICourse>();
        this.pastCourses = new ArrayList<String>();
    }

    // ------------------------------------------------------------------------------------------
    /* IPERSON IMPLEMENTATION */
    @Override
    public Boolean isInstructor() {
        return false;
    }

    @Override
    public ArrayList<ICourse> getCurrentCourses() {
        return currentCourses;
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
        for (ICourse c : getCurrentCourses()) {
            System.out.format(c.toString() + "\n");
        }
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public void addCourse(ICourse c) {
        if (this.currentCourses == null) {
            ArrayList<ICourse> cList = new ArrayList<ICourse>();
            cList.add(c);
            setCurrentCourses(cList);
        } else if (!this.currentCourses.contains(c)) {
            this.currentCourses.add(c);
        } else {
            System.out.format("Requested course in already in the courses list.\n");
        }
    }

    // ------------------------------------------------------------------------------------------
    /* STUDENT SPECIFIC METHODS */
    /**
     * Takes in the course list for the student and sets the student course list
     * 
     * @param courses: ArrayList<ICourse>
     */
    public void setCurrentCourses(ArrayList<ICourse> courses) {
        this.currentCourses = courses;
    }

    /**
     * Takes in the past course list for the student and sets the past course list Note that past
     * courses are string and not ICourse This is because we don't have as many details about past
     * courses from registrar
     * 
     * @param pastCourses ArrayList<String>
     */
    public void setPastCourses(ArrayList<String> pastCourses) {
        this.pastCourses = pastCourses;
    }

    /**
     * Getter for pastCourses
     */
    public ArrayList<String> getPastCourses() {
        return pastCourses;
    }

    /**
     * Add a course to the pastCourse list
     * 
     * @param pastCourse
     */
    public void addPastCourse(String pastCourse) {
        pastCourses.add(pastCourse);
    }

    /**
     * Prints out all the past courses Helps the user to check whether they meet the requirement
     */
    public void printPastCourses() {
        for (String c : getPastCourses()) {
            System.out.format("%s\t", c);
        }
        System.out.format("\n");
    }

    /**
     * Checks if the student's past courses meet the given prerequisite
     * 
     * @param prereqStr, This variable should come from the course information using Databse
     * @return boolean of whether the student meets the give prerequisite
     */
    public boolean meetsPrereq(String prereqStr) {

        ArrayList<String> pastCourses = getPastCourses();
        boolean curBool = true;
        int conj = -1;// initialized as -1, 0 = AND, 1 = OR

        if (prereqStr.length() == 0) {
            // no prereq
            return true;
        } else {

            ArrayList<String> allMatches = new ArrayList<String>();
            Matcher m = Pattern.compile("\\([^\\)]+\\)|(AND|OR)|(\\w+)").matcher(prereqStr);
            while (m.find()) {
                allMatches.add(m.group());
            }

            int i = 0;
            while (i < allMatches.size()) {
                boolean localBool;

                String s = allMatches.get(i);
                // parse again if it's within parenthesis
                if (s.charAt(0) == '(') {
                    if (s.charAt(s.length() - 1) == ')') {
                        // parse again
                        s = s.substring(1, s.length());
                        localBool = meetsPrereq(s);
                    } else {
                        // return error since it was not properly closed
                        System.out.format("Prerequisite has unmatching parenthesis.\n");
                        return false;
                    }
                } else {
                    // check if this course is in
                    i++;
                    String num = allMatches.get(i);
                    String courseNo = s + " " + num;
                    localBool = pastCourses.contains(courseNo);
                }

                if (conj == -1) {
                    // start of the loop
                    curBool = localBool;
                } else if (conj == 0) {
                    // AND
                    curBool = curBool && localBool;
                } else {
                    // OR
                    curBool = curBool || localBool;
                }

                if (i == allMatches.size() - 1) {
                    // reached the end of parsed array
                    return curBool;
                }

                i++;
                String bool = allMatches.get(i);

                if (bool.equals("AND") || bool.equals("and")) {
                    conj = 0;
                } else if (bool.equals("OR") || bool.equals("or")) {
                    conj = 1;
                } else {
                    System.out.format(
                            "Course prereq not in right format."
                                    + " Please double check with registrar\n");
                    return true;
                    // error for boolean
                }
                i++;
            }
            return curBool;
        }
    }

}
