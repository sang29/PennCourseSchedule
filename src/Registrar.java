import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Registrar implements IRegistrar {

    @Override
    public Map<String, String> parseSubjects() {
        String courseIndexUrl = "https://srfs.upenn.edu/registration-catalog-calendar/timetables/main";
        Map<String, String> subjects = new TreeMap<>();
        try {
            Document courseIndex = Jsoup.connect(courseIndexUrl).get();
            Element courseSubjectTable = courseIndex.getElementsByTag("table").get(0);
            Elements courseSubjects = courseSubjectTable.getElementsByTag("tr");
            for (Element row : courseSubjects) {
                Elements cols = row.getElementsByTag("td");
                String subjectCode = cols.get(0).text();
                String subjectName = cols.get(1).getElementsByTag("a").text();
                subjects.put(subjectCode, subjectName);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return subjects;
    }

    @Override
    public Map<String, Map<Integer, ICourse>> buildCourseDirectory(Map<String, String> subjects) {
        String baseUrl = "https://catalog.upenn.edu/search/?P=";
        Map<String, Collection<ICourse>> courseDirectory = new HashMap<>();
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("courses.tsv"));
            for (String code : subjects.keySet()) {
                Document subjectCatalog = Jsoup.connect(baseUrl + code).get();
                Elements courses = subjectCatalog.getElementsByClass("search-courseresult");
                for (Element course : courses) {
                    String courseHeading = course.getElementsByTag("h2").get(0).text();
                    int courseId = Integer.parseInt(courseHeading.replaceAll("\\D", "").substring(0, 3));
                    String courseTitle = courseHeading.split("\\d{3}")[1].trim();
                    bw.write(cbuf);
                    System.out.printf("%4s\t%03d\t%s\n", code, courseId, courseTitle);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<Integer, Collection<IPerson>> buildStudentDirectory() {
        // TODO Auto-generated method stub
        return null;
    }

}
