import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
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
    public Map<String, Map<Integer, String>> parseCatalog(Map<String, String> subjects) {
        String catalogUrl = "https://catalog.upenn.edu/search/?P=";
        Map<String, Map<Integer, String>> catalog = new HashMap<>();
        try {
            if (!Files.exists(Paths.get("courses.tsv"))) {
                BufferedWriter bw = new BufferedWriter(new FileWriter("courses.tsv"));
                for (String code : subjects.keySet()) {
                    Map<Integer, String> courseMap = new HashMap<>();
                    Document subjectCatalog = Jsoup.connect(catalogUrl + code).get();
                    Elements courses = subjectCatalog.getElementsByClass("search-courseresult");
                    for (Element course : courses) {
                        String courseHeading = course.getElementsByTag("h2").get(0).text();
                        int courseId = Integer
                                .parseInt(courseHeading.replaceAll("\\D", "").substring(0, 3));
                        String courseTitle = courseHeading.split("\\b(\\d{3})\\b")[1].trim();
                        courseMap.put(courseId, courseTitle);
                        Elements extras = course.getElementsByClass("courseblockextra");
                        Elements prereqElements;
                        StringBuilder prereqs = new StringBuilder();
                        for (Element extra : extras) {
                            if (extra.text().contains("Prerequisite")) {
                                prereqElements = extra.getElementsByTag("a");
                                for (Element prereqElement : prereqElements) {
                                    prereqs.append(prereqElement.text());
                                    prereqs.append("\t");
                                }
                                
                            }
                        }
                        bw.write(String.format("%4s\t%03d\t%s\t%s", code, courseId, courseTitle, prereqs.toString()));
                        bw.newLine();
                    }
                    catalog.put(code, courseMap);
                }
                bw.close();
                return catalog;
            }
            BufferedReader br = new BufferedReader(new FileReader("courses.tsv"));
            String line;
            String previousSubject = "XXX";
            Map<Integer, String> courseMap = new HashMap<>();
            while ((line = br.readLine()) != null) {                
                String[] courseInfo = line.trim().split("\t");
                System.out.println(courseInfo.length);
                String subject = courseInfo[0];
                int id = Integer.parseInt(courseInfo[1]);
                System.out.println(subject + id);
                String title = courseInfo[2];
                if (!subject.equals(previousSubject)) {
                    if (courseMap.size() != 0) {
                        catalog.put(previousSubject, courseMap);
                        courseMap = new HashMap<>();
                    }
                }
                previousSubject = subject;
                courseMap.put(id, title);
            }
            catalog.put(previousSubject, courseMap);
            return catalog;
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Map<Integer, ICourse>> buildCourseDirectory(Map<String, Map<Integer, String>> catalog) {
        Map<String, Collection<ICourse>> courseDirectory = new HashMap<>();
        try {
            for (Entry<String, Map<Integer, String>> subject : catalog.entrySet()) {
                String code = subject.getKey();
                Map<Integer, String> courseTitlesPerId = subject.getValue();
                String timeTableUrl = "https://registrar.upenn.edu/timetable/" + code + ".html";
                Document timeTableDoc = Jsoup.connect(timeTableUrl).get();
                String timeTableText = timeTableDoc.getElementsByTag("pre").get(0)
                        .getElementsByTag("p").get(1).text();
                Scanner scnr = new Scanner(timeTableText);
                while (scnr.hasNextLine()) {
                    String line = scnr.nextLine();
                    if (Character.isAlphabetic(line.charAt(0))) {
                        
                    }
                    int courseId = Integer.parseInt(line.replaceAll("\\D", "").substring(0, 3));
                    
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
