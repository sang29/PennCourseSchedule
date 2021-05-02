
public interface ICourse extends Comparable<ICourse> {
    
    
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
    public IPerson instructor();
    
    /**
     * @return course type, e.g. "Lecture", "Recitation"
     */
    public String type();
    
    /**
     * @return meeting days, as boolean array of length 7
     */
    public boolean[] days();
    
     
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
}
