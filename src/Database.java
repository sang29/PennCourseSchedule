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
import java.util.regex.Matcher;
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
     * @return name of the MongoDB database
     */
    public String name() {
        return db.getName();
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

    /**
     * @return list of names of collections in the MongoDB database
     */
    public List<String> collections() {
        List<String> temp = new LinkedList<>();
        db.listCollectionNames().into(temp);
        return temp;
    }

    /**
     * Parse all subject codes and names from the UPenn catalog and pushes them to the database
     */
    public void pushSubjectsToDatabase() {
        // If a client hasn't been opened, do nothing
        if (mongoClient == null) return;
        if (!collections().contains("subjects")) {
            db.createCollection("subjects");
        }
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
            Document doc = new Document("code", subject.getKey()).append("name",
                    subject.getValue());
            // Add the Document to the list
            docs.add(doc);
        }
        // Push the documents to the "subjects" collection
        collection.insertMany(docs);
    }

    /**
     * Parse all courses from the UPenn catalog and pushes them to the database
     */
    public void pushCoursesToDatabase() {
        // If a client hasn't been opened, do nothing
        if (mongoClient == null) return;
        if (!collections().contains("courses")) {
            db.createCollection("courses");
        }
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

    /**
     * Parses all course prerequisites from the UPenn catalog and pushes them to the database
     */
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

    /**
     * Parses all sections from the UPenn time table and pushes them to the database
     */
    public void pushSectionsToDatabase() {
        // If a client hasn't been opened, do nothing
        if (mongoClient == null) return;
        if (!collections().contains("sections_fall2021")) {
            db.createCollection("sections_fall2021");
        }
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
    
    public void initializeDatabase() {
        pushSubjectsToDatabase();
        pushCoursesToDatabase();
        pushPrereqsToDatabase();
        pushSectionsToDatabase();
    }

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
                .append("lastName", lastName.toUpperCase()).append("program", program)
                .append("id", id).append("password", password).append("courses", courses)
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

        List<String> courses = new ArrayList<>(); // current courses that the instructor is teaching
        List<Document> waitlist = new ArrayList<>(); // waitlist for permission

        Document doc = new Document("firstName", firstName.toUpperCase())
                .append("lastName", lastName.toUpperCase()).append("program", program)
                .append("id", id).append("password", password).append("courses", courses)
                .append("pastCourses", waitlist);

        collection.insertOne(doc);
    }

    public void pushCourseToStudent(String id, String subject, int number, int section) {
        if (mongoClient == null) return;
        MongoCollection<Document> studentCollection = db.getCollection("students");
        MongoCollection<Document> sectionCollection = db.getCollection("sections_fall2021");
//        MongoCollection<Document> courses = db.getCollection("courses");

//        MongoCollection<Document> instructors = db.getCollection("instructors");

//        // find the courseID
//        Document s = sections
//                .find(and(eq("subject", subject), eq("number", number), eq("section", section)))
//                .first();
//        Document c = courses.find(and(eq("subject", subject), eq("number", number))).first();
//
//        if (s == null) {
//            System.out.println("Requested course is not offered in current semester.");
//            return;
//        }
//
//        String courseNo = subject + " " + Integer.toString(number);
//
//        Document p = people.find(eq("id", id)).first();
//
//        if (p == null) {
//            System.out.println("Requested ID doesn't exist.");
//            return;
//        }
//        ArrayList<Document> currentCourses = (ArrayList<Document>) p.get("courses");
//        ArrayList<String> pastCourses = (ArrayList<String>) p.get("pastCourses");
//
//        // check the pre-req for the class
//        String prereqStr = getPrereq(subject, number);
//        if (!checkPrereq(prereqStr, pastCourses)) {
//            System.out.println("You don't meet the prereq for the course.");
//            return;
//        }
//
//        // check if the class is full
//        int max = (int) s.get("max");
//        int current = (int) s.get("current");
//        if (current == max) {
//            System.out.println("Requested section is already full.");
//            return;
//        }
//
//        // check if permission is requested
//        if (c.getBoolean("permission")) {
//            System.out.println("This class needs instructor permission.");
//            System.out.println("Sending permission request to the instructor...");
//            System.out.println("Your class will be added once the instructor approves.");
//
//            Document wait = new Document();
//            wait.append("student_id", p.get("id")).append("subject", s.get("subject"))
//                    .append("number", s.get("number")).append("section", s.get("section"));
//
//            String instructor = (String) s.get("instructor");
//            String firstInstructor = instructor.split("/")[0]; // in case there are multiple
//                                                               // instructors
//
//            instructors.updateOne(eq("lastName", firstInstructor),
//                    new Document().append("$push", new Document("waitlist", wait)));
//            return;
//        }
//        if (currentCourses.size() >= 5) {
//            System.out.println("You cannot take more than five courses per semester.");
//            return;
//        }
//
//        ICourse requestedICourse = findSection(subject, number, section);
//        for (Document currentCourse : currentCourses) {
//            String curSubject = currentCourse.getString("subject");
//            int curNumber = currentCourse.getInteger("number");
//            int curSection = currentCourse.getInteger("section");
//
//            ICourse curICourse = findSection(curSubject, curNumber, curSection);
//
//            // check duplicate course in the student courses
//            if (curSubject.equals(subject) && curNumber == number && curSection == section) {
//                System.out.println("Requested course was already added to student schedule.");
//                return;
//            }
//
//            // check class time conflict
//            if (requestedICourse.conflictsWith(curICourse)) {
//                System.out.println(
//                        "Requested course has time conflicts with current course selection.");
//                return;
//            }
//        }
//
//        // check if the course was taken in the past
//        if (pastCourses.contains(courseNo)) {
//            System.out.println("Requested course is already taken previously.");
//            return;
//        }

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

    public String getPrereq(String subject, int number) {
        MongoCollection<Document> courses = db.getCollection("courses");
        Document c = courses.find(and(eq("subject", subject), eq("number", number))).first();
        String prereqStr = (String) c.get("prerequisites");
        return prereqStr;
    }

    public Document findStudentById(String id) {
        MongoCollection<Document> students = db.getCollection("students");
        Document c = students.find(eq("id", id)).first();
        return c;
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
            FindIterable<Document> matching = coursesCollection.find(and(eq("subject", subject), eq("number", number))); 
            String title = "";
            if (matching.first() != null) {
                title = matching.first().getString("title");
            }

            c.setTitle(title);
            courses.add(c);
        }
        return courses;
    }

    public List<ICourse> findCourseBySubjectAndNumber(String subject, int number) {
        List<ICourse> courses = new LinkedList<>();
        MongoCollection<Document> sectionsCollection = db.getCollection("sections_fall2021");
        MongoCollection<Document> coursesCollection = db.getCollection("courses");
        FindIterable<Document> docs = sectionsCollection
                .find(and(eq("subject", subject), eq("number", number)));
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

    public ICourse findSection(String subject, int number, int section) {

        MongoCollection<Document> sectionsCollection = db.getCollection("sections_fall2021");
        MongoCollection<Document> coursesCollection = db.getCollection("courses");
        Document doc = sectionsCollection
                .find(and(eq("subject", subject), eq("number", number), eq("section", section)))
                .first();
        if (doc == null) {
            System.out.println("Requested course section does not exist. Please try another one.");
            return null;
        }
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

        return c;
    }

    public Boolean courseNeedsPerm(String subject, int number) {
        MongoCollection<Document> coursesCollection = db.getCollection("courses");
        Document doc = coursesCollection.find(and(eq("subject", subject), eq("number", number)))
                .first();
        return doc.getBoolean("permission");
    }

    void sendPermRequest(String student_id, String subject, int number, int section) {
        MongoCollection<Document> sections = db.getCollection("sections_fall2021");
        MongoCollection<Document> instructors = db.getCollection("instructors");

        Document wait = new Document();
        wait.append("student_id", student_id).append("subject", subject).append("number", number)
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

    public static void main(String[] args) {
        Database m = new Database();
        m.openClient();
//        m.printAllCourses();
//        m.pushStudentToDatabase("Sang Ik", "Han", "CIT", "sangik_id", "sangik_pw");
//        m.pushStudentToDatabase("Philipp", "Gaissert", "CIT", "philipp_id", "philipp_pw");
//        m.pushCourseToStudent("sangik_id", "CIT", 590, 1);
//        m.pushCourseToStudent("sangik_id", "CIT", 592, 1);
//        m.pushCourseToStudent("sangik_id", "CIT", 593, 1);
//        m.pushPastCourseToStudent("sangik_id", "CIT", 591);
//        m.pushPastCourseToStudent("sangik_id", "CIT", 593);
        m.closeClient();
    }

}
