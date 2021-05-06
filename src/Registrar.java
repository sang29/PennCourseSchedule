import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public Map<String, Map<Integer, String>> parseCourses(Map<String, String> subjects) {
        String catalogUrl = "https://catalog.upenn.edu/search/?P=";
        Map<String, Map<Integer, String>> subjectToCourses = new TreeMap<>();
        try {
            for (String code : subjects.keySet()) {
                Map<Integer, String> numberToTitle = new TreeMap<>();
                Document subjectCatalog = Jsoup.connect(catalogUrl + code).get();
                Elements courses = subjectCatalog.getElementsByClass("search-courseresult");
                for (Element course : courses) {
                    String courseHeading = course.getElementsByTag("h2").get(0).text();
                    int courseNumber = Integer.parseInt(courseHeading.replaceAll("\\D", "").substring(0, 3));
                    String courseTitle = courseHeading.split("\\b(\\d{3})\\b")[1].trim();
                    numberToTitle.put(courseNumber, courseTitle);
                }
                subjectToCourses.put(code, numberToTitle);
            }
            return subjectToCourses;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    public Map<String, Map<Integer, String>> parsePrereqs(Map<String, String> subjects) {
        String catalogUrl = "https://catalog.upenn.edu/search/?P=";
        Map<String, Map<Integer, String>> subjectToCourses = new TreeMap<>();
        try {
            for (String code : subjects.keySet()) {
                Map<Integer, String> courseToPrereqs = new TreeMap<>();
               
                Document subjectCatalog = Jsoup.connect(catalogUrl + code).get();
                Elements courses = subjectCatalog.getElementsByClass("search-courseresult");
                
                for (Element course : courses) {
                    String courseHeading = course.getElementsByTag("h2").get(0).text();
                    int courseNumber = Integer.parseInt(courseHeading.replaceAll("\\D", "").substring(0, 3));
                    Elements blocks = course.getElementsByClass("courseblockextra");
                    Pattern p = Pattern.compile("(?<=Prerequisite[s]*: ).*$");
                    for (Element block : blocks) {
                        String blockText = block.text();
                        if (blockText.contains("Prerequisite:") || blockText.contains("Prerequisites:")) {
                            Matcher m = p.matcher(block.text());
                            m.find();
                            String prereqs = m.group(0);
                            courseToPrereqs.put(courseNumber, prereqs);
                            break;
                        }
                        courseToPrereqs.put(courseNumber, "");
                    }
                }
                subjectToCourses.put(code, courseToPrereqs);
            }
            return subjectToCourses;
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
