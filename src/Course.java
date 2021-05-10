import java.util.Collection;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalTime;

public class Course implements ICourse {

    private String subject;
    private int id;
    private int section;
    private String title;
    private String description;
    private String instructorStr;
    private String type;
    private double units;
    
    private boolean[] days;
    private LocalTime startTime;
    private Interval meetingTime;
    
    
    private int max;
    private int current;
    private Collection<IPerson> students;
    
    
    
    public Course(String subject, int id, int section) {
        this.subject = subject;
        this.id = id;
        this.section = section;
        this.days = new boolean[5];
    }
    
    public Course(String subject, int id) {
        this.subject = subject;
        this.id = id;
        this.days = new boolean[5];
    }
        
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
    public boolean equals(Object o) {
        if (o instanceof Course) {
            return this.subject.equals(((Course)o).subject())
                    && this.id == ((Course)o).id()
                    && this.section == ((Course)o).section();
        }
        return false;
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
    
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String instructorStr() {
        return this.instructorStr;
    }
    
//    @Override
//    public boolean equals(Object o) {
//        return o == this;
//    }
    
    public void setInstructorStr(String instructor) {
        this.instructorStr = instructor;
        
    }

    @Override
    public String type() {
        return this.type;
    }
    
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public double units() {
        return this.units;
    }
    
    public void setUnits(double units) {
        this.units = units;
    }
    
    @Override
    public boolean[] days() {
        boolean[] temp = new boolean[5];
        for (int i = 0; i < 5; i++) {
            temp[i] = days[i];
        }
        return temp;
    }
    
    public void setDays(String days) {
        for (int i = 0; i < days.length(); i++) {
            switch (days.charAt(i)) {
                case 'M':
                    this.days[M] = true;
                    break;
                case 'T':
                    this.days[T] = true;
                    break;
                case 'W':
                    this.days[W] = true;
                    break;
                case 'R':
                    this.days[R] = true;
                    break;
                case 'F':
                    this.days[F] = true;
                    break;
            }
        }
        
    }
    
    public String daysToString() {
        StringBuilder sb = new StringBuilder();
        if (days[M]) sb.append('M');
        if (days[T]) sb.append('T');
        if (days[W]) sb.append('W');
        if (days[R]) sb.append('R');
        if (days[F]) sb.append('F');
        return sb.toString();
    }
    
    public Interval meetingTime() {
        return this.meetingTime;
    }
    
    @Override
    public LocalTime startTime() {
        return this.startTime;
    }
    
    public void setStartTime(int hourOfDay, int minuteOfHour) {
        this.startTime = new LocalTime(hourOfDay, minuteOfHour);
    }

    @Override
    public int duration() {
        if (this.meetingTime == null) {
            return 0;
        }
        return (int)this.meetingTime.toDuration().getStandardMinutes();
    }
    
    public void setDuration(int duration) {
        this.meetingTime = new Interval(new Instant(this.startTime.toDateTimeToday()), new Duration(duration * 60 * 1000));
    }
    
    @Override
    public int max() {
        return this.max;
    }

    public void setMax(int max) {
        this.max = max;
    }
    
    @Override
    public int current() {
        if (students == null) {
            return this.current;
        }
        return this.students.size();
    }
    
    public void setCurrent(int current) {
        this.current = current;
    }

    @Override
    public boolean isFull() {
        return this.students.size() == this.max;
    }

    @Override
    public boolean conflictsWith(ICourse c) {
        boolean[] otherDays = c.days();
        for (int i = 0; i < 5; i++) {
            if (this.days[i] && otherDays[i]) {
                if (this.meetingTime.overlaps(c.meetingTime())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s-%d-%03d %s", subject, id, section, title);
    }

    @Override
    public String description() {
        // TODO Auto-generated method stub
        return null;
    }
    

}
