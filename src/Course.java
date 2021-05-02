import java.util.Collection;

public class Course implements ICourse {

    private String subject;
    private int id;
    private int section;
    private String title;
    private String description;
    private IPerson instructor;
    private String type;
    private boolean[] days;
    private int max;
    private Collection<IPerson> students;
    
    
    @Override
    public int compareTo(ICourse o) {
        int subjComp = this.subject.compareTo(o.subject());
        if (subjComp != 0) {
            return subjComp;
        }
        int idComp = this.id - o.id();
        if (idComp != 0) {
            return idComp;
        }
        return this.section - o.section();
    }

    @Override
    public String subject() {
        return this.subject;
    }

    @Override
    public int id() {
        return this.id;
    }

    @Override
    public int section() {
        return this.section;
    }

    @Override
    public String title() {
        return this.title;
    }

    @Override
    public String description() {
        return this.description;
    }

    @Override
    public IPerson instructor() {
        return this.instructor;
    }

    @Override
    public String type() {
        return this.type;
    }

    @Override
    public boolean[] days() {
        boolean[] temp = new boolean[7];
        for (int i = 0; i < 7; i++) {
            temp[i] = days[i];
        }
        return temp;
    }

    @Override
    public int max() {
        return this.max;
    }

    @Override
    public int current() {
        return this.students.size();
    }

    @Override
    public boolean isFull() {
        return this.students.size() == this.max;
    }

}
