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
    public void addCourse(ICourse c) {
        courses.add(c); 
    }
    
    public void addPastCourse(String pastCourse) {
        pastCourses.add(pastCourse);
    }
    
//    public void requestPerm(Course c) {
//        c.instructor().takePermRequest(c, this);
//    }
    
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
    public String getId() {
        return this.id;
    }

    @Override
    public void setId() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void printCourses() {
        for (ICourse c: getCourses()) {
            System.out.println(c.toString());
        }
    }
    
    public void printPastCourses() {
        for (String c : getPastCourses()) {
            System.out.printf("%s\t", c);
        }
        System.out.println();
    }
    
    public boolean meetsPrereq(String prereqStr) {
        
        ArrayList<String> pastCourses = getPastCourses();
        boolean curBool = true;
        int conj = -1;// initialized as -1, 0 = AND, 1 = OR

        // https://stackoverflow.com/questions/2118261/parse-boolean-arithmetic-including-parentheses-with-regex
        // https://stackoverflow.com/questions/6020384/create-array-of-regex-matches/46859130

        if (prereqStr.length() == 0) {
            // no prereq
            return true;
        } else {
//            String[] matches = match("prereqStr", "\\((\\w+)\\s+(and|or)\\s+(\\w)\\)|(\\w+)" );
            // "\\((\\w+)\\s+(and|or)\\s+(\\w)\\)|(\\w+)"
            // "\\([^\\)]+\\)"

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
                        System.out.println("Prerequisite has unmatching parenthesis");
                        return false;
                    }
                } else {
                    // check if this course is in
                    i++;
                    String num = allMatches.get(i);
                    String courseNo = s + " " + num;
                    localBool = pastCourses.contains(courseNo);
//                    System.out.printf("i: %d", i);
//                    System.out.println(localBool);
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
                    // error for boolean
                    System.out.printf("Boolean is not in the right format at i: %d\n", i);
                }
                i++;
            }
            return curBool;
        }

    }
    
    
    int requestPerm(int classNo) {
        return 0;
    }
    
    public static void main(String[] args) {
//        Database m = new Database();
//        m.openClient();
//        m.printAllCourses();
//        m.pushStudentToDatabase("Sang Ik", "Han", "CIT", "sangik59x", "samplePassword!@#$");
//        m.pushStudentToDatabase("Philipp", "Gaissert", "CIT", "philipp59x", "samplePassword!@#$");
//        m.pushCourseToStudent("sangik59x", "CIT", 590);
//        m.pushPastCourseToStudent("sangik59x", "CIT", 590);
//        ArrayList<String> pastCourses = new ArrayList<String>();
//        
//        System.out.println(m.checkPrereq(prereqStr, pastCourses));
//        
//        System.out.println(m.checkPrereq(prereqStr, pastCourses));
//        m.closeClient();

    }
    
}
