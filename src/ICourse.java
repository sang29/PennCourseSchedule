import org.joda.time.Interval;
import org.joda.time.LocalTime;

public interface ICourse extends Comparable<ICourse> {
    
    public static final int M = 0;
    public static final int T = 1;
    public static final int W = 2;
    public static final int R = 3;
    public static final int F = 4;
    
    /**
     * @return subject code, e.g. CIT
     */
    public String subject();
    
    /**
     * @return course id number, e.g. 594
     */
    public int id();
    
    /**
     * @return course section, e.g. 001, 201
     */
    public int section();
    
    /**
     * @return course title
     */
    public String title();
    
    /**
     * @return course description
     */
    public String description();
    
    /**
     * @return course instructor
     */
    public String instructorStr();
    
    /**
     * @return course type, e.g. "Lecture", "Recitation"
     */
    public String type();
    
    /**
     * @return course units
     */
    public double units();
    
    /**
     * @return meeting days, as boolean array of length 7
     */
    public boolean[] days();
    
    /**
     * @return start time
     */
    public LocalTime startTime(); 
    
    /**
     * @return duration, in minutes
     */
    public int duration();
       
    
    /**
     * @return meeting time, as an Interval object
     */
    public Interval meetingTime();
    
    
    /**
     * @return maximum enrollment
     */
    public int max();
    
    /**
     * @return current enrollment
     */
    public int current();
    
    /**
     * @return true if fully enrolled, false otherwise
     */
    public boolean isFull();
    
    /**
     * @param c Another course
     * @return true if the courses have a time conflict
     */
    public boolean conflictsWith(ICourse c);
    
    boolean equals(Object o);
    
}
