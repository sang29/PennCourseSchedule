import static com.mongodb.client.model.Accumulators.first;
import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.sort;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

    ConnectionString uri;
    MongoClient mongoClient;
    MongoDatabase db;

    public Database() {
        // This string contains credentials for connecting to the MongoDB Atlas cluster
        uri = new ConnectionString(
                "mongodb+srv://registrar:IeNHciZmmRfrYyG5@cit594-penn-registrar.3gicr.mongodb.net/registrar?retryWrites=true&w=majority");
        // Disable logging by the MongoDB driver
        Logger logger = Logger.getLogger("org.mongodb.driver");
        logger.setLevel(Level.SEVERE);
    }

    /**
     * Opens a client for connecting to the "registrar" database in the cluster
     */
    public void openClient() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create(uri);
            db = mongoClient.getDatabase("registrar");
        }
        // If a client is already opened, do nothing
    }

    /**
     * Closes the client
     */
    public void closeClient() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
        }
        // If a client hasn't been opened, do nothing
    }

    /* DON'T CALL THIS!!! IT WILL ADD DUPLICATES!! */
    public void pushSubjectsToDatabase() {
        // If a client hasn't been opened, do nothing
        if (mongoClient == null) return;
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
            // Create a new Document, e.g. {code: "CIT", name: "Computer & Information Technology"}
            Document doc = new Document("code", subject.getKey()).append("name",
                    subject.getValue());
            // Add the Document to the list
            docs.add(doc);
        }
        // Push the documents to the "subjects" collection
        collection.insertMany(docs);
    }

    /* DON'T CALL THIS!!! IT WILL ADD DUPLICATES!! */
    public void pushCoursesToDatabase() {
        // If a client hasn't been opened, do nothing
        if (mongoClient == null) return;
        MongoCollection<Document> collection = db.getCollection("courses");
        // Create a PennParser object
        PennParser p = new PennParser();
        Map<String, String> subjects = p.parseSubjects();
        Map<String, Map<Integer, String>> catalog = p.parseCourses(subjects);
        List<Document> docs = new LinkedList<>();
        for (Entry<String, Map<Integer, String>> subject : catalog.entrySet()) {
            for (Entry<Integer, String> course : subject.getValue().entrySet()) {
                Document doc = new Document("subject", subject.getKey())
                        .append("number", course.getKey()).append("title", course.getValue());
                docs.add(doc);
            }
        }
        // Push the documents to the "subjects" collection
        collection.insertMany(docs);
    }

    public void pushPrereqsToDatabase() {
        // If a client hasn't been opened, do nothing
        if (mongoClient == null) return;
        MongoCollection<Document> collection = db.getCollection("courses");
        // Create a PennParser object
        PennParser p = new PennParser();
        Map<String, String> subjects = p.parseSubjects();
        Map<String, Map<Integer, String>> prereqMap = p.parsePrereqs(subjects);
        for (Entry<String, Map<Integer, String>> subject : prereqMap.entrySet()) {
            for (Entry<Integer, String> course : subject.getValue().entrySet()) {
                boolean permission = Pattern
                        .compile(Pattern.quote("permission"), Pattern.CASE_INSENSITIVE)
                        .matcher(course.getValue()).find();
                collection.updateOne(
                        and(eq("subject", subject.getKey()), eq("number", course.getKey())),
                        combine(set("permission", permission),
                                set("prerequisites", course.getValue())));
            }
        }
    }

    public void pushSectionsToDatabase() {
        // If a client hasn't been opened, do nothing
        if (mongoClient == null) return;
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
            Document doc = new Document("subject", section.subject()).append("number", section.id())
                    .append("section", section.section()).append("type", section.type())
                    .append("instructor", section.instructor().getName())
                    .append("days", ((Course) section).daysToString()).append("startTime", start)
                    .append("duration", section.duration()).append("max", section.max())
                    .append("current", 0).append("units", section.units());
            docs.add(doc);
        }
        collection.insertMany(docs);
    }

    public void pushStudentToDatabase(String firstName, String lastName, String program, String id,
            String password) {
        // If a client hasn't been opened, do nothing
        if (mongoClient == null) return;
        // Get the "subjects" collection from the database
        MongoCollection<Document> collection = db.getCollection("students");

        Document d = collection.find(eq("id", id)).first();
        if (d != null) {
            System.out.println("id already exists, please try another one.");
            return;
        }

        List<Integer> courses = new ArrayList<>();
        List<Integer> pastCourses = new ArrayList<>();

        Document doc = new Document("firstName", firstName).append("lastName", lastName)
                .append("program", program).append("id", id).append("password", password)
                .append("courses", courses).append("pastCourses", pastCourses);

        collection.insertOne(doc);
    }

    public void pushCourseToStudent(String id, String subject, int number) {
        if (mongoClient == null) return;

        MongoCollection<Document> courses = db.getCollection("courses");
        MongoCollection<Document> students = db.getCollection("students");

        // find the courseID
        Document c = courses.find(and(eq("subject", subject), eq("number", number))).first();

        if (c == null) {
            System.out.println("Requested course is not offered in current semester.");
            return;
        }

        String courseNo = subject + " " + Integer.toString(number);

        // check the pre-req for the class
        // check class time conflict

        // check duplicate course in the student courses
        Document s = students.find(eq("id", id)).first();

        if (s == null) {
            System.out.println("Requested student ID doesn't exist.");
            return;
        }
        ArrayList<String> currentCourses = (ArrayList<String>) s.get("courses");
        ArrayList<String> pastCourses = (ArrayList<String>) s.get("pastCourses");

        if (currentCourses.size() >= 5) {
            System.out.println("You cannot take more than five courses per semester.");
            return;
        }

        if (currentCourses.contains(courseNo)) {
            System.out.println("Requested course was already added to student schedule.");
            return;
        }

        if (pastCourses.contains(courseNo)) {
            System.out.println("Requested course is already taken previously.");
            return;
        }

        // find the student and add course ID to its course collection
        students.updateOne(eq("id", id),
                new Document().append("$push", new Document("courses", courseNo)));
    }

    public void pushPastCourseToStudent(String id, String subject, int number) {
        // same syntax as pushCourseToStudent
        return;
    }

    public String getPrereq(String subject, int number) {
        MongoCollection<Document> courses = db.getCollection("courses");
        Document c = courses.find(and(eq("subject", subject), eq("number", number))).first();
        String prereqStr = (String) c.get("prerequisites");
        return prereqStr;
    }

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

    public List<ICourse> findCoursesBySubject(String subject) {
        List<ICourse> courses = new LinkedList<>();
        MongoCollection<Document> sectionsCollection = db.getCollection("sections_fall2021");
        MongoCollection<Document> coursesCollection = db.getCollection("courses");
        AggregateIterable<Document> docs = sectionsCollection
                .aggregate(Arrays.asList(match(eq("subject", subject)),
                        group("$number", first("subject", "$subject"), first("number", "$number")),
                        sort(Sorts.ascending("number"))));
        for (Document doc : docs) {
            Integer number = doc.getInteger("number");
            Course c = new Course(subject, number);
            String title = coursesCollection.find(and(eq("subject", subject), eq("number", number)))
                    .first().getString("title");
            c.setTitle(title);
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
            Course c = new Course(doc.getString("subject"), doc.getInteger("number"),
                    doc.getInteger("section"));
            c.setType(doc.getString("type"));
            c.setInstructor(doc.getString("instructor"));
            c.setDays(doc.getString("days"));
            int startTime = doc.getInteger("startTime");
            c.setStartTime(startTime / 60, startTime % 60);
            c.setDuration(doc.getInteger("duration"));
            c.setMax(doc.getInteger("max"));
            c.setCurrent(doc.getInteger("current"));
            c.setUnits(doc.getDouble("units"));
            String title = coursesCollection.find(and(eq("subject", doc.getString("subject")),
                    eq("number", doc.getInteger("number")))).first().getString("title");
            c.setTitle(title);
            sections.add(c);
        }
        return sections;
    }

    public static void main(String[] args) {
        Database m = new Database();
        m.openClient();
        m.printAllCourses();
        m.closeClient();
    }

}
