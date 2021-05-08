import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.mongodb.client.MongoCollection;

public class Registrar implements IRegistrar {

    Database db;
    
    public Registrar() {
        db = new Database();
    }
    
    
    @Override
    public Map<String, Map<Integer, ICourse>> buildCourseDirectory() {
//        Map<String, Collection<ICourse>> courseDirectory = new HashMap<>();
//        try {
//            MongoCollection subjects = db.getCollection("subjects"); 
//            for (Entry<String, Map<Integer, String>> subject : catalog.entrySet()) {
//                String code = subject.getKey();
//                Map<Integer, String> courseTitlesPerId = subject.getValue();
//                String timeTableUrl = "https://registrar.upenn.edu/timetable/" + code + ".html";
//                Document timeTableDoc = Jsoup.connect(timeTableUrl).get();
//                String timeTableText = timeTableDoc.getElementsByTag("pre").get(0)
//                        .getElementsByTag("p").get(1).text();
//                Scanner scnr = new Scanner(timeTableText);
//                while (scnr.hasNextLine()) {
//                    String line = scnr.nextLine();
//                    if (Character.isAlphabetic(line.charAt(0))) {
//
//                    }
//                    int courseId = Integer.parseInt(line.replaceAll("\\D", "").substring(0, 3));
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return null;
    }

    @Override
    public Map<Integer, IPerson> buildStudentDirectory() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<ICourse> prerequisites(String subject, int id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<ICourse> coursesBySubject(String subject) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<ICourse> coursesBySubjectAndLevel(String subject, int low, int high) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<ICourse> coursesBySubjectAndLevel(String subject, int low) {
        // TODO Auto-generated method stub
        return null;
    }

}
