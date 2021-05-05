
public class Console implements IConsole {
//    private int currentUser;
    private Registrar r;
    private IPerson p;
    
    Console() {
        p = null; //initialize p as -1
        r = new Registrar(); //r needs to keep track of student id/pw
    }
    
    public void setCurrentUser(IPerson user) {
        p = user; //IPerson object from registrar directory with matching ID
    };
    
    public IPerson getCurrentUser() {
        return p;
    };
    
    @Override
    public int login(int id, int pw) {
        //IPerson curP = r.getUser(id) 
        if (curP.getPassword() == pw) {
            setCurrentUser(curP);
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public int logout(int id, int pw) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int currentUser() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    public void addCourse() {
        if (p == null) {
            //Please first login!
        } else if (p.isInstructor()) {
            //Please login as student
        } else {
            p.addCourse(String dept, int classNo);
            //error handling when exceeding course limit!
        }
    }

}
