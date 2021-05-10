import static com.mongodb.client.model.Accumulators.first;
import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.sort;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bson.Document;

import com.mongodb.ConnectionString;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;

public class Database {

    private ConnectionString uri;
    private MongoClient mongoClient;
    private MongoDatabase db;

    public Database() {
        // This string contains credentials for connecting to the MongoDB Atlas cluster
        uri = new ConnectionString(
                "mongodb+srv://registrar:IeNHciZmmRfrYyG5@cit594-penn-registrar.3gicr.mongodb.net/registrar?retryWrites=true&w=majority");
        // Disable logging by the MongoDB driver
        Logger logger = Logger.getLogger("org.mongodb.driver");
        logger.setLevel(Level.SEVERE);
    }

    // ------------------------------------------------------------------------------------------
    /* DATABASE CONNECTION */

    /**
     * Opens a client for connecting to the "registrar" database in the cluster
     */
    public void openClient() {
        // If a client is already opened, do nothing
        if (mongoClient != null) return;
        // Otherwise, create a new client to connect to the cluster
        mongoClient = MongoClients.create(uri);
        // Connect to the "registrar" database
        db = mongoClient.getDatabase("registrar");
    }

    /**
     * @return name of the MongoDB database
     */
    public String name() {
        return db.getName();
    }

    /**
     * Closes the MongoDB client
     */
    public void closeClient() {
        // If a client hasn't been opened, do nothing
        if (mongoClient == null) return;
        mongoClient.close();
        mongoClient = null;
        db = null;
    }

    /**
     * Switches the MongoDB client to another database in the cluster (meant for demo)
     * @param dbName
     */
    public void switchDatabase(String dbName) {
        // If a client hasn't been opened, do nothing
        if (mongoClient == null || db.getName().equals(dbName)) return;
        db = mongoClient.getDatabase(dbName);
    }

    /**
     * Clears and removes all collections in the current database (meant for demo)
     */
    public void clearCollections() {
        if (db.getName().equals("registrar")) return;
        for (String collection : collections()) {
            db.getCollection(collection).drop();
        }
    }

    /**
     * @return list of names of collections in the MongoDB database
     */
    public List<String> collections() {
        List<String> temp = new LinkedList<>();
        db.listCollectionNames().into(temp);
        return temp;
    }

    // ------------------------------------------------------------------------------------------
    /* INITIALIZING SUBJECTS, COURSES, & SECTIONS */

    /**
     * Parse all subject codes and names from the UPenn catalog and pushes them to the database
     */
    public void pushSubjectsToDatabase() {
        // If a client isn't open or "subjects" already exists, do nothing
        if (mongoClient == null
                || db.getName().equals("registrar")
                || collections().contains("subjects"))
            return;
        // Get the "subjects" collection from the database
        MongoCollection<Document> collection = db.getCollection("subjects");
        // Create a PennParser object
        PennParser p = new PennParser();
        // Get the subject codes and names from the UPenn catalog
        Map<String, String> subjects = p.parseSubjects();
        // Create an empty list for holding Documents (BSON, not Jsoup)
        List<Document> docs = new LinkedList<>();
        // For each subject's code-name pair
        for (Entry<String, String> subject : subjects.entrySet()) {
            // Create a new Document, e.g. {code: "CIT", name: "Computer & Information
            // Technology"}
            Document doc = new Document("code", subject.getKey())
                    .append("name", subject.getValue());
            // Add the Document to the list
            docs.add(doc);
        }
        // Push the documents to the "subjects" collection
        collection.insertMany(docs);
    }

    /**
     * Parses all courses from the UPenn catalog and pushes them to the database
     */
    public void pushCoursesToDatabase() {
        // If a client hasn't been opened, do nothing
        if (mongoClient == null
                || db.getName().equals("registrar")
                || !collections().contains("subjects")
                || collections().contains("courses"))
            return;
        db.createCollection("courses");
        MongoCollection<Document> collection = db.getCollection("courses");
        // Create a PennParser object
        PennParser p = new PennParser();
        // Parse all subjects from the UPenn catalog
        Map<String, String> subjects = p.parseSubjects();
        // Parse all courses from the UPenn catalog
        Map<String, Map<Integer, String[]>> catalog = p.parseCoursesAndPrereqs(subjects);
        // Create a new LinkedList to hold the course Documents to push to the collection
        List<Document> docs = new LinkedList<>();
        // Iterate over all courses of all subjects
        for (Entry<String, Map<Integer, String[]>> subject : catalog.entrySet()) {
            for (Entry<Integer, String[]> course : subject.getValue().entrySet()) {
                // Check if instructor permission is required for the course
                boolean permission = Pattern
                        .compile(Pattern.quote("permission"), Pattern.CASE_INSENSITIVE)
                        .matcher(course.getValue()[1]).find();
                // Create a new Document with the following fields:
                // subject, number, title, permission, prerequisites
                Document doc = new Document("subject", subject.getKey())
                        .append("number", course.getKey())
                        .append("title", course.getValue()[0])
                        .append("permission", permission)
                        .append("prerequisites", course.getValue()[1]);
                // Add the Document to the list
                docs.add(doc);
            }
        }
        // Push the documents to the "subjects" collection
        collection.insertMany(docs);
    }

    /**
     * Parses all sections from the UPenn time table and pushes them to the database
     */
    public void pushSectionsToDatabase() {
        // If a client hasn't been opened, do nothing
        if (mongoClient == null
                || db.getName().equals("registrar")
                || !collections().contains("subjects")
                || !collections().contains("courses")
                || collections().contains("sections_fall2021"))
            return;
        MongoCollection<Document> collection = db.getCollection("sections_fall2021");
        // Create a PennParser object
        PennParser p = new PennParser();
        Map<String, String> subjects = p.parseSubjects();
        Collection<ICourse> sections = p.parseSections(subjects);
        List<Document> docs = new LinkedList<>();
        for (ICourse section : sections) {
            int start = 0;
            if (section.startTime() != null) {
                start = (60 * section.startTime().getHourOfDay())
                        + section.startTime().getMinuteOfHour();
            }
            Document doc = new Document("subject", section.subject())
                    .append("number", section.id())
                    .append("section", section.section())
                    .append("type", section.type())
                    .append("instructor", section.instructorStr())
                    .append("days", ((Course) section).daysToString())
                    .append("startTime", start)
                    .append("duration", section.duration())
                    .append("max", section.max())
                    .append("current", 0)
                    .append("units", section.units());
            docs.add(doc);
        }
        collection.insertMany(docs);
    }

    public void initializeDatabase() {
        pushSubjectsToDatabase();
        pushCoursesToDatabase();
        pushSectionsToDatabase();
    }

    // ------------------------------------------------------------------------------------------
    /* STUDENTS & INSTRUCTORS */

    public void pushStudentToDatabase(String firstName, String lastName, String program, String id,
            String password) {
        // If a client hasn't been opened, do nothing
        if (mongoClient == null) return;
        if (!collections().contains("students")) {
            db.createCollection("students");
        }
        // Get the "subjects" collection from the database
        MongoCollection<Document> collection = db.getCollection("students");
        Document d = collection.find(eq("id", id)).first();
        if (d != null) {
            System.out.println("id already exists, please try another one.");
            return;
        }
        List<Document> courses = new ArrayList<>();
        List<String> pastCourses = new ArrayList<>();
        Document doc = new Document("firstName", firstName.toUpperCase())
                .append("lastName", lastName.toUpperCase())
                .append("program", program)
                .append("id", id)
                .append("password", password)
                .append("courses", courses)
                .append("pastCourses", pastCourses);
        collection.insertOne(doc);
    }

    public void pushInstructorToDatabase(String firstName, String lastName, String program,
            String id, String password) {
        // If a client hasn't been opened, do nothing
        if (mongoClient == null) return;
        // Get the "subjects" collection from the database
        MongoCollection<Document> collection = db.getCollection("instructors");

        Document d = collection.find(eq("id", id)).first();
        if (d != null) {
            System.out.println("id already exists, please try another one.");
            return;
        }

        List<Document> courses = new ArrayList<>(); // current courses that the instructor is
                                                    // teaching
        List<Document> waitlist = new ArrayList<>(); // waitlist for permission

        Document doc = new Document("firstName", firstName.toUpperCase())
                .append("lastName", lastName.toUpperCase())
                .append("program", program).append("id", id)
                .append("password", password)
                .append("courses", courses)
                .append("waitlist", waitlist);

        collection.insertOne(doc);
    }

    public void pushCourseToStudent(String id, String subject, int number, int section) {
        if (mongoClient == null) return;
        MongoCollection<Document> studentCollection = db.getCollection("students");
        MongoCollection<Document> sectionCollection = db.getCollection("sections_fall2021");
        Document studentCourse = new Document();
        studentCourse.append("subject", subject).append("number", number).append("section",
                section);

        studentCollection.updateOne(eq("id", id),
                new Document().append("$push", new Document("courses", studentCourse)));
        // increment current for section
        sectionCollection.updateOne(
                and(eq("subject", subject), eq("number", number), eq("section", section)),
                new Document().append("$inc", new Document("current", 1)));
        // add student ID

    }

    public void pushPastCourseToStudent(String student_id, String subject, int number) {
        // same syntax as pushCourseToStudent
        MongoCollection<Document> studentCollection = db.getCollection("students");
        String courseNo = subject + " " + Integer.toString(number);
//        Document studentDoc = studentCollection.find(eq("id", student_id)).first();

        studentCollection.updateOne(eq("id", student_id),
                new Document().append("$push", new Document("pastCourses", courseNo)));
        return;
    }

    // ------------------------------------------------------------------------------------------
    /* QUERIES */

    public String getPrereq(String subject, int number) {
        MongoCollection<Document> courses = db.getCollection("courses");
        Document c = courses.find(and(eq("subject", subject), eq("number", number))).first();
        String prereqStr = (String) c.get("prerequisites");
        return prereqStr;
    }

    public Student findStudentById(String id) {
        MongoCollection<Document> studentCollection = db.getCollection("students");
        Document student = studentCollection.find(eq("id", id)).first();

        if (student == null) {
            return null;
        }

        String pw = student.getString("password");
        String firstName = student.getString("firstName");
        String lastName = student.getString("lastName");
        String program = student.getString("program");
        ArrayList<Document> courses = (ArrayList<Document>) student.get("courses");
        ArrayList<String> pastCourses = (ArrayList<String>) student.get("pastCourses");

        ArrayList<ICourse> curCourses = new ArrayList<ICourse>();

        for (Document d : courses) {
            ICourse c = findSection(d.getString("subject"), d.getInteger("number"),
                    d.getInteger("section"));
            curCourses.add(c);
        }

        Student s = new Student(firstName, lastName, id, pw, program);
        s.setCourses(curCourses);
        s.setPastCourses(pastCourses);

        return s;
    }

    public void deleteStudentById(String id) {
        MongoCollection<Document> studentCollection = db.getCollection("students");
        studentCollection.deleteOne(eq("id", id));
    }

    public Instructor findInstructorById(String id) {
        MongoCollection<Document> instructors = db.getCollection("instructors");
        Document instructor = instructors.find(eq("id", id)).first();

        if (instructor == null) {
            return null;
        }

        String pw = instructor.getString("password");
        String firstName = instructor.getString("firstName");
        String lastName = instructor.getString("lastName");
        String program = instructor.getString("program");
        ArrayList<Document> courses = (ArrayList<Document>) instructor.get("courses");
        ArrayList<Document> waitlist = (ArrayList<Document>) instructor.get("waitlist");

        ArrayList<ICourse> curCourses = new ArrayList<ICourse>();
        HashMap<String, ArrayList<String>> curWaitlist = new HashMap<String, ArrayList<String>>();

        for (Document d : courses) {
            ICourse c = findSection(d.getString("subject"), d.getInteger("number"),
                    d.getInteger("section"));
            curCourses.add(c);
        }

        for (Document d : waitlist) {
            String subject = d.getString("subject");
            int number = d.getInteger("number");
            int section = d.getInteger("section");
            String student_id = d.getString("student_id");
//            ICourse c = new Course(subject, number, section);
            String c = subject + " " + Integer.toString(number) + " " + Integer.toString(section);

            ArrayList<String> studentList = curWaitlist.getOrDefault(c, new ArrayList<String>());
//            System.out.printf("adding %s to waitlist\n", student_id);
            studentList.add(student_id);
            curWaitlist.put(c, studentList);
        }

        Instructor i = new Instructor(firstName, lastName, id, pw, program);
        i.setCourses(curCourses);
        i.setWaitlist(curWaitlist);

//        Document wait = new Document();
//        wait.append("student_id", student_id).append("subject", subject).append("number", number).append("section",
//                section);

        return i;
    }

    public void deleteInstructorById(String id) {
        MongoCollection<Document> instructorCollection = db.getCollection("instructors");
        instructorCollection.deleteOne(eq("id", id));
    }

    public void deleteCourseFromStudent(String id, String subject, int number, int section) {
        MongoCollection<Document> studentCollection = db.getCollection("students");
        MongoCollection<Document> sectionCollection = db.getCollection("sections_fall2021");
        Document course = new Document().append("subject", subject)
                .append("number", number)
                .append("section", section);

        // delete from student schedule
        studentCollection.updateOne(eq("id", id),
                new Document().append("$pull", new Document("courses", course)));

        // decrement current field of the section
        sectionCollection.updateOne(
                and(eq("subject", subject), eq("number", number), eq("section", section)),
                new Document().append("$inc", new Document("current", -1)));
    }

    /**
     * @param subject Subject code, e.g. "CIT"
     * @return List of Course objects for each course in the given subject
     */
    public List<ICourse> findCoursesBySubject(String subject) {
        List<ICourse> courses = new LinkedList<>();
        MongoCollection<Document> sectionsCollection = db.getCollection("sections_fall2021");
        MongoCollection<Document> coursesCollection = db.getCollection("courses");
        AggregateIterable<Document> docs = sectionsCollection
                .aggregate(Arrays.asList(
                        match(eq("subject", subject)),
                        group("$number", first("subject", "$subject"), first("number", "$number")),
                        sort(Sorts.ascending("number"))));
        for (Document doc : docs) {
            Integer number = doc.getInteger("number");
            Course c = new Course(subject, number);
            FindIterable<Document> matching = coursesCollection
                    .find(and(eq("subject", subject), eq("number", number)));
            String title = "";
            if (matching.first() != null) {
                title = matching.first().getString("title");
            }
            c.setTitle(title);
            courses.add(c);
        }
        return courses;
    }

    // I think that this should be "findSectionsBySubjectAndNumber"
    // Another method would be "findCourseBySubjectAndNumber"
    public List<ICourse> findCourseBySubjectAndNumber(String subject, int number) {
        List<ICourse> courses = new LinkedList<>();
        MongoCollection<Document> sectionsCollection = db.getCollection("sections_fall2021");
        MongoCollection<Document> coursesCollection = db.getCollection("courses");
        FindIterable<Document> docs = sectionsCollection
                .find(and(eq("subject", subject), eq("number", number)));
        for (Document doc : docs) {
            Course c = createCourseFromDocument(doc, coursesCollection);
            courses.add(c);
        }
        return courses;
    }

    public List<ICourse> findSectionsByCourseAndType(String subject, int number, String type) {
        List<ICourse> sections = new LinkedList<>();
        MongoCollection<Document> sectionsCollection = db.getCollection("sections_fall2021");
        MongoCollection<Document> coursesCollection = db.getCollection("courses");
        FindIterable<Document> docs = sectionsCollection
                .find(and(eq("subject", subject), eq("number", number), eq("type", type)));
        for (Document doc : docs) {

            Course c = createCourseFromDocument(doc, coursesCollection);

            sections.add(c);
        }
        return sections;
    }

    public Course findSection(String subject, int number, int section) {
        MongoCollection<Document> sectionsCollection = db.getCollection("sections_fall2021");
        MongoCollection<Document> coursesCollection = db.getCollection("courses");
        Document doc = sectionsCollection
                .find(and(eq("subject", subject), eq("number", number), eq("section", section)))
                .first();
        if (doc == null) {
            System.out.println("Requested course section does not exist. Please try another one.");
            return null;
        }
        Course c = createCourseFromDocument(doc, coursesCollection);
        return c;
    }

    /**
     * Creates a Course object using a document from the database
     * 
     * @param doc
     * @param coll
     * @return
     */
    private Course createCourseFromDocument(Document doc,
            MongoCollection<Document> coursesCollection) {
        Course c = new Course(doc.getString("subject"), doc.getInteger("number"),
                doc.getInteger("section"));
        c.setType(doc.getString("type"));
        c.setInstructorStr(doc.getString("instructor"));
        c.setDays(doc.getString("days"));
        int startTime = doc.getInteger("startTime");
        c.setStartTime(startTime / 60, startTime % 60);
        c.setDuration(doc.getInteger("duration"));
        c.setMax(doc.getInteger("max"));
        c.setCurrent(doc.getInteger("current"));
        c.setUnits(doc.getDouble("units"));
        String title = coursesCollection.find(
                and(
                        eq("subject", doc.getString("subject")),
                        eq("number", doc.getInteger("number"))))
                .first().getString("title");
        c.setTitle(title);
        return c;
    }

    // ------------------------------------------------------------------------------------------
    /* PERMISSION */

    public boolean courseNeedsPerm(String subject, int number) {
        MongoCollection<Document> coursesCollection = db.getCollection("courses");
        Document doc = coursesCollection.find(and(eq("subject", subject), eq("number", number)))
                .first();
        return doc.getBoolean("permission");
    }

    public void sendPermRequest(String student_id, String subject, int number, int section) {
        MongoCollection<Document> sections = db.getCollection("sections_fall2021");
        MongoCollection<Document> instructors = db.getCollection("instructors");

        Document wait = new Document();
        wait.append("student_id", student_id)
                .append("subject", subject)
                .append("number", number)
                .append("section", section);

        Document s = sections
                .find(and(eq("subject", subject), eq("number", number), eq("section", section)))
                .first();

        if (s == null) {
            System.out.println("Requested course is not offered in current semester.");
            return;
        }

        String instructor = (String) s.get("instructor");
        String firstInstructor = instructor.split("/")[0]; // in case there are multiple
                                                           // instructors

        instructors.updateOne(eq("lastName", firstInstructor),
                new Document().append("$push", new Document("waitlist", wait)));
    }

    // ------------------------------------------------------------------------------------------
    /* PRINT METHODS */

    /**
     * Gets all subjects from the database and prints them
     */
    public void printAllSubjects() {
        // If a client hasn't been opened, do nothing
        if (mongoClient == null) return;
        // Get the "subjects" collection from the database
        MongoCollection<Document> collection = db.getCollection("subjects");
        // Get all documents from the collection
        // (using a new empty document as the query gets you all documents)
        FindIterable<Document> docs = collection.find(new Document());
        // For each document
        for (Document doc : docs) {
            // Print out the subject code and name
            System.out.printf("%s\t%s\n", doc.get("code"), doc.get("name"));
        }
    }

    /**
     * Gets all courses from the database and prints them
     */
    public void printAllCourses() {
        // If a client hasn't been opened, do nothing
        if (mongoClient == null) return;
        // Get the "subjects" collection from the database
        MongoCollection<Document> collection = db.getCollection("courses");
        // Get all documents from the collection
        // (using a new empty document as the query gets you all documents)
        FindIterable<Document> docs = collection.find(new Document());
        // For each document
        for (Document doc : docs) {
            // Print out the course
            System.out.printf("%4s %03d - %s\n", doc.get("subject"), doc.get("number"),
                    doc.get("title"));
        }
    }

    public static void main(String[] args) {
//        Database db = new Database();
//        db.openClient();
//        Instructor i;
//        HashMap<ICourse, ArrayList<String>> waitlist;
//        
//        db.deleteStudentById("sangik_id");
//        db.pushStudentToDatabase("Sang Ik", "Han", "CIT", "sangik_id", "sangik_pw");
// 
//        //CIS 557
//        db.pushInstructorToDatabase("Eric", "Fouh", "CIS", "eric_id", "eric_pw");
//        i = db.findInstructorById("eric_id");
//        waitlist = i.getWaitlist();
//        
//        db.sendPermRequest("sangik_id", "CIS", 557, 1);
//        i = db.findInstructorById("eric_id");
//        waitlist = i.getWaitlist();
//        for (Map.Entry<ICourse, ArrayList<String>> entry : waitlist.entrySet()) {
//            System.out.printf("%s\n", entry.getKey().subject());
//            System.out.printf("%d\n", entry.getKey().id());
//            System.out.printf("%s\n", entry.getValue().get(0));
////            assertEquals(entry.getKey().id(), 557);
////            assertEquals(entry.getKey().section(), 1);
////            assertEquals(entry.getValue().get(0), "sangik_id");
//         }
//        
//        db.deleteStudentById("sangik_id");
//        db.deleteInstructorById("eric_id");
//        db.closeClient();

    }

}
