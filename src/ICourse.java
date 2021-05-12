import org.joda.time.Interval;
import org.joda.time.LocalTime;

/**
 * An interface for a representing a UPenn course
 * @author Philipp Gaissert & Sangik Han
 *
 */
public interface ICourse extends Comparable<ICourse> {
    
    /* For use with the days boolean array */
    public static final int M = 0;
    public static final int T = 1;
    public static final int W = 2;
    public static final int R = 3;
    public static final int F = 4;
    
    /**
     * @return subject code, e.g. "CIT"
     */
    public String subject();
    
    /**
     * @return course id number, e.g. 594
     */
    public int number();
    
    /**
     * @return course section, e.g. 001, 201
     */
    public int section();
    
    /**
     * @return course title
     */
    public String title();
    
    /**
     * @return course instructor
     */
    public String instructor();
    
    /**
     * @return course type, e.g. "Lecture", "Recitation"
     */
    public String type();
    
    /**
     * @return course units, e.g. 1.0
     */
    public double units();
    
    /**
     * @return meeting days, as boolean array of length 7
     */
    public boolean[] days();
    
    /**
     * @return start time, represented by a LocalTime object
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
     * @param c Another ICourse object
     * @return true if the courses have a time conflict
     */
    public boolean conflictsWith(ICourse c);
    
    
    /**
     * @param o Another Object
     * @return  true if o is an ICourse object with the same subject, number, and section
     */
    boolean equals(Object o);
    
}
