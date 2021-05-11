
public class ProfileInit {
    public static void main(String[] args) {
        //Profile generation for demo
        Database m = new Database();
        m.openClient();
        m.deleteStudentById("sangik1_id");
        m.deleteStudentById("sangik2_id");
        m.deleteStudentById("philipp_id");
        m.deleteInstructorById("eric_id");
        
        m.pushStudentToDatabase("Sang Ik1", "Han", "CIT", "sangik1_id", "sangik1_pw");
        m.pushStudentToDatabase("Sang Ik2", "Han", "ECON", "sangik2_id", "sangik2_pw");
        m.pushStudentToDatabase("Philipp", "Gaissert", "CIT", "philipp_id", "philipp_pw");
        
        //push prereq for ECON 104 1
        m.pushPastCourseToStudent("sangik2_id", "ECON", 101);
        m.pushPastCourseToStudent("sangik2_id", "ECON", 103);
        m.pushPastCourseToStudent("sangik2_id", "MATH", 104);
        m.pushPastCourseToStudent("sangik2_id", "MATH", 114);
        
        //fill out 4 courses to later show
        //CIS 320 1 time conflict with CIS 436 401
        m.pushCourseToStudent("sangik1_id", "CIT", 591, 1);
        m.pushCourseToStudent("sangik1_id", "CIT", 592, 1);
        m.pushCourseToStudent("sangik1_id", "CIT", 593, 1);
        m.pushCourseToStudent("sangik1_id", "CIS", 320, 1);
        
        //update current of a course to show max limit has reached
        
        //CIS 557 requires permission from Eric Fouh
        m.pushInstructorToDatabase("Eric", "Fouh", "CIS", "eric_id", "eric_pw");
        m.closeClient();
    }
}
