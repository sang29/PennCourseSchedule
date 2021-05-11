import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;

public class Student implements IPerson {
    private String firstName;
    private String lastName;
    private String program;
    private String id;
    private String password;
    private ArrayList<ICourse> courses;
    private ArrayList<String> pastCourses;

    Student(String firstName, String lastName, String id, String password, String program) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
        this.password = password;
        this.program = program;
        this.courses = new ArrayList<ICourse>();
        this.pastCourses = new ArrayList<String>();
    }

    public void setCourses(ArrayList<ICourse> courses) {
        this.courses = courses;
    }

    public void setPastCourses(ArrayList<String> pastCourses) {
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

    public ArrayList<String> getPastCourses() {
        return pastCourses;
    }

    public void addPastCourse(String pastCourse) {
        pastCourses.add(pastCourse);
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
        for (ICourse c : getCourses()) {
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
                        System.out.println("Prerequisite has unmatching parenthesis");
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
                    System.out.printf("Course prereq not in right format. Please double check with registrar\n");
                    return true;
                    // error for boolean
                }
                i++;
            }
            return curBool;
        }
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public void addCourse(ICourse c) {
        if (this.courses == null) {
            ArrayList<ICourse> cList = new ArrayList<ICourse>();
            cList.add(c);
            setCourses(cList);
        } else if (!this.courses.contains(c)) {
            this.courses.add(c);
        } else {
            System.out.print("Requested course in already in the courses list");
        }
    }

}
