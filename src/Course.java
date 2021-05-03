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
    private IPerson instructor;
    private String type;
    
    private boolean[] days;
    private LocalTime startTime;
    private Interval meetingTime;
    
    
    private int max;
    private Collection<IPerson> students;
    
    
    
    public Course(String subject, int id, int section) {
        this.subject = subject;
        this.id = id;
        this.section = section;
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
        return (int)this.meetingTime.toDuration().getStandardMinutes();
    }
    
    public void setDuration(int duration) {
        this.meetingTime = new Interval(new Instant(this.startTime.toDateTimeToday()), new Duration(duration * 60 * 1000));
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
    

}
