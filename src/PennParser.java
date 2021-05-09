import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PennParser implements IPennParser {

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
                    int courseNumber = Integer
                            .parseInt(courseHeading.replaceAll("\\D", "").substring(0, 3));
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

    @Override
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
                    int courseNumber = Integer
                            .parseInt(courseHeading.replaceAll("\\D", "").substring(0, 3));
                    Elements blocks = course.getElementsByClass("courseblockextra");
                    Pattern p = Pattern.compile("(?<=Prerequisite[s]*: ).*$");
                    for (Element block : blocks) {
                        String blockText = block.text();
                        if (blockText.contains("Prerequisite:")
                                || blockText.contains("Prerequisites:")) {
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
    public Collection<ICourse> parseSections(Map<String, String> subjects) {
        Collection<ICourse> sections = new TreeSet<>();
        try {
            for (Entry<String, String> subject : subjects.entrySet()) {
                String code = subject.getKey();
                if (code.equals("BENF")) code = "BENFG";
                String timeTableUrl = "https://www.registrar.upenn.edu/timetable/"
                        + code.toLowerCase() + ".html";
//                System.out.println(timeTableUrl);
                Document timeTable = Jsoup.connect(timeTableUrl).get();
                code = subject.getKey();
                Elements elements = timeTable.getElementsByTag("pre").get(0).getElementsByTag("p");
                String timeTableText = "";
                if (elements.get(0).text().contains("*")) {
                    timeTableText = elements.get(2).text();
                } else {
                    timeTableText = elements.get(1).text();
                }
                Scanner s = new Scanner(timeTableText);
                while (s.hasNextLine()) {
                    String line;
                    do {
                        line = s.nextLine();
                    } while (line.isBlank());
//                    System.out.println(line);
                    if (line.substring(0, code.length()).equals(code)) {
                        Pattern p = Pattern.compile("(?<=" + code + "\\s*-)[0-9]{3}");
                        Matcher m = p.matcher(line);
                        m.find();
                        int courseNumber = Integer.parseInt(m.group(0));
                        p = Pattern.compile("([0-9]\\.)?[0-9](?= CU)");
                        m = p.matcher(line);
                        m.find();
                        double units = Double.parseDouble(m.group(0));
                        while (s.hasNextLine() && !((line = s.nextLine()).isBlank())) {
                            if (isNumeric(line.substring(1, 4))) {
                                Course c = parseSectionsHelper(line, code, courseNumber, s);
                                if (c.type().equals("Lecture")) {
                                    c.setUnits(units);
                                } else {
                                    c.setUnits(0);
                                }
                                sections.add(c);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {

        }
        return sections;
    }

    //---------------------------------------------------------------------------------------------
    
    /**
     * 
     * @param str           String containing section info from the UPenn time table
     * @param code          Subject code, e.g. "CIT"
     * @param courseNumber  Course number, e.g. 594
     * @param scnr          Scanner object 
     * @return              Course object for a distinct section
     */
    private Course parseSectionsHelper(String str, String code, int courseNumber, Scanner s) {
        Pattern p;
        Matcher m;
        String[] info = str.trim().split("\\s+");
//        System.out.println(Arrays.toString(info));
        int sectionNumber = Integer.parseInt(info[0]);
        Course c = new Course(code, courseNumber, sectionNumber);
        String instructor = "";
        c.setType(parseType(info[1]));
        if (info[2].equals("TBA") || info[2].equals("TBD")) {
            if (info.length > 3) {
                instructor = info[3].toLowerCase();
            }
        } else {
            c.setDays(info[2]);
            p = Pattern.compile("[0-9APM:-]+");
            m = p.matcher(info[3]);
            m.find();
            parseMeetingTime(m.group(0), c);
            if (info.length > 4) {
                instructor = info[4].toLowerCase();
            }
        }
        if (instructor.length() > 0) {
            instructor = instructor.substring(0, 1).toUpperCase() + instructor.substring(1);
            int i;
            if ((i = instructor.indexOf("/")) != -1) {
                instructor = instructor.replace("/" + instructor.charAt(i + 1),
                        "/" + Character.toUpperCase(instructor.charAt(i + 1)));
            }
        }
        c.setInstructor(instructor);
        while (s.hasNextLine() && !(str.contains("MAX:") || str.contains("MAX W/")))
            str = s.nextLine().trim();
        if (!s.hasNextLine()) return c;
        p = Pattern.compile("(?<=MAX.*: )\\d+");
        m = p.matcher(str);
        m.find();
        c.setMax(Integer.parseInt(m.group(0)));
        return c;
    }

    private String parseType(String typeCode) {
        switch (typeCode) {
            case "LEC":
                return "Lecture";
            case "REC":
                return "Recitation";
            case "SEM":
                return "Seminar";
            case "IND":
                return "Independent Study";
            case "MST":
                return "Master's Thesis";
            case "LAB":
                return "Laboratory";
            case "DIS":
                return "Dissertation";
            case "CLN":
                return "Clinic";
            case "FLD":
                return "Academic Field Study";
            case "STU":
                return "Studio";
            case "ONL":
                return "Online Course";
            case "SRT":
                return "Senior Thesis"; 
            default:
                return "???";
        }
    }

    public void parseMeetingTime(String meetingTime, Course section) {
        int startHour, startMinute, endHour, endMinute;
        // Split the start and end time strings
        String[] endpoints = meetingTime.split("-");
        // Check if the end time is AM or PM
        boolean endPM = endpoints[1].contains("PM");
        
        // Check if the end time contains a colon
        if (endpoints[1].contains(":")) {
            // If the end time contains a colon, split the hour and minutes
            String[] end = endpoints[1].split(":");
            // Parse the integer value of the hour
            endHour = Integer.parseInt(end[0]);
            // If the end time is PM and the hour is NOT 12, add 12 to the hour value  
            if (endPM && endHour != 12) {
                endHour += 12;
            }
            // Parse the integer value of the minutes
            endMinute = Integer.parseInt(end[1].substring(0, 2));
        } else {
            // Parse the integer value of the hour from the substring before "AM" or "PM"
            endHour = Integer.parseInt(endpoints[1].replace("AM", "").replace("PM", ""));
            // If the end time is PM and the hour is NOT 12, add 12 to the hour value
            if (endPM && endHour != 12) {
                endHour += 12;
            }
            // Set the minute value to 0
            endMinute = 0;
        }
        
        // Check if the start time contains a colon
        if (endpoints[0].contains(":")) {
            // If the end time contains a colon, split the hour and minutes
            String[] start = endpoints[0].split(":");
            // Parse the integer value of the hour
            startHour = Integer.parseInt(start[0]);
            // Parse the integer value of the minutes
            startMinute = Integer.parseInt(start[1].substring(0, 2));
        } else {
            String startTime = endpoints[0].replace("AM", "").replace("PM", "");
            startHour = Integer.parseInt(startTime);
            startMinute = 0;
        }
        

        if (endPM) {
            if (((60 * endHour) + endMinute) - ((60 * (startHour + 12)) + startMinute) >= 45) {
                startHour += 12;
            }
        }
        
        int duration = ((60 * endHour) + endMinute) - ((60 * startHour) + startMinute);
        
        section.setStartTime(startHour, startMinute);
        section.setDuration(duration);
    }

    private static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

//    public static void main(String[] args) {
//        PennParser parser = new PennParser();
//        String str = "CIS -105  COMP DATA EXPLORATION             1 CU\n" + " GROUP 1 SECTIONS\n"
//                + "     REGISTRATION REQUIRED FOR LEC, REC\n"
//                + "     FROM WITHIN THIS GROUP OF SECTIONS\n" + "     FORMAL REASONING COURSE\n"
//                + "     FORMAL REASONING & ANALYSIS\n"
//                + " 001 LEC MWF 10:15-11:15AM           GREENBERG C\n"
//                + "     MAX: 70                \n"
//                + "          RECITATION                        0 CU\n"
//                + " 201 REC M 3:30-5PM                  GREENBERG C\n"
//                + "     MAX: 35                \n"
//                + " 202 REC M 5:15-6:45PM               GREENBERG C\n"
//                + "     MAX: 35                \n"
//                + "          COMP DATA EXPLORATION             1 CU\n" + " GROUP 2 SECTIONS\n"
//                + "     REGISTRATION REQUIRED FOR LEC, REC\n"
//                + "     FROM WITHIN THIS GROUP OF SECTIONS\n" + "     BENJAMIN FRANKLIN SEMINARS\n"
//                + "     FORMAL REASONING COURSE\n" + "     FORMAL REASONING & ANALYSIS\n"
//                + " 002 LEC MWF 1:45-2:45PM           BHUSNURMATH A\n"
//                + "     MAX: 25                \n"
//                + "          RECITATION                        0 CU\n"
//                + " 205 REC T 5:15-6:45PM             BHUSNURMATH A\n"
//                + "     MAX: 25                \n" + "\n"
//                + "CIS -110  INTRO TO COMP PROG                1 CU\n"
//                + "     REGISTRATION REQUIRED FOR LEC, REC\n" + "     FORMAL REASONING COURSE\n"
//                + " 001 LEC MWF 12-1PM                   FOUH/SMITH\n"
//                + "     FORMAL REASONING COURSE\n" + "     MAX: 175               \n"
//                + " 002 LEC MWF 1:45-2:45PM              FOUH/SMITH\n"
//                + "     MAX: 175               \n"
//                + "          RECITATION                        0 CU\n"
//                + "     FORMAL REASONING COURSE\n"
//                + " 201 REC M 3:30-5:30PM                     STAFF\n"
//                + "     FORMAL REASONING COURSE\n" + "     MAX: 20                \n"
//                + " 202 REC M 5:15-7:15PM                     STAFF\n"
//                + "     FORMAL REASONING COURSE\n" + "     MAX: 20                \n"
//                + " 203 REC M 7-9PM                           STAFF\n"
//                + "     FORMAL REASONING COURSE\n" + "     MAX: 20                \n"
//                + " 204 REC T 12-2PM                          STAFF\n"
//                + "     FORMAL REASONING COURSE\n" + "     MAX: 20                \n"
//                + " 205 REC T 1:45-3:45PM                     STAFF\n"
//                + "     FORMAL REASONING COURSE\n" + "     MAX: 20                \n"
//                + " 206 REC T 3:30-5:30PM                     STAFF\n"
//                + "     FORMAL REASONING COURSE\n" + "     MAX: 20                \n"
//                + " 207 REC T 5:15-7:15PM                     STAFF\n"
//                + "     FORMAL REASONING COURSE\n" + "     MAX: 20                \n"
//                + " 208 REC M 1:45-2:45PM                     STAFF\n"
//                + "     FORMAL REASONING COURSE\n" + "     MAX: 20                \n"
//                + " 209 REC M 1:45-2:45PM                     STAFF\n"
//                + "     FORMAL REASONING COURSE\n" + "     MAX: 20                \n"
//                + " 210 REC M 3:30-4:30PM                     STAFF\n"
//                + "     FORMAL REASONING COURSE\n" + "     MAX: 20                \n"
//                + " 211 REC M 5:15-6:15PM                     STAFF\n"
//                + "     FORMAL REASONING COURSE\n" + "     MAX: 20                \n"
//                + " 212 REC M 7-8PM                           STAFF\n"
//                + "     FORMAL REASONING COURSE\n" + "     MAX: 20                \n"
//                + " 213 REC M 8:30-9:30PM                     STAFF\n"
//                + "     FORMAL REASONING COURSE\n" + "     MAX: 20                \n"
//                + " 214 REC T 10:15-11:15AM                   STAFF\n"
//                + "     FORMAL REASONING COURSE\n" + "     MAX: 20                \n"
//                + " 215 REC T 12-1PM                          STAFF\n"
//                + "     FORMAL REASONING COURSE\n" + "     MAX: 20                \n"
//                + " 216 REC T 3:30-4:30PM                     STAFF\n"
//                + "     FORMAL REASONING COURSE\n" + "     MAX: 20                \n"
//                + " 217 REC T 1:45-2:45PM                     STAFF\n"
//                + "     FORMAL REASONING COURSE\n" + "     MAX: 20                \n"
//                + " 218 REC T 3:30-4:30PM                     STAFF\n"
//                + "     FORMAL REASONING COURSE\n" + "     MAX: 20                \n"
//                + " 219 REC T 5:15-6:15PM                     STAFF\n"
//                + "     FORMAL REASONING COURSE\n" + "     MAX: 20                \n"
//                + " 220 REC T 7-8PM                           STAFF\n"
//                + "     FORMAL REASONING COURSE\n" + "     MAX: 20                \n"
//                + " 221 REC T 8:30-9:30PM                     STAFF\n"
//                + "     FORMAL REASONING COURSE\n" + "     MAX: 20                \n"
//                + " 222 REC T 7-9PM                           STAFF\n"
//                + "     MAX: 20                \n" + " ";
//
//        Scanner s = new Scanner(str);
//        String code = "CIS";
//        while (s.hasNextLine()) {
//            String line = s.nextLine();
//            if (line.substring(0, code.length()).equals(code)) {
//                Pattern p = Pattern.compile("(?<=" + code + " -)[0-9]{3}");
//                Matcher m = p.matcher(line);
//                m.find();
//                int courseNumber = Integer.parseInt(m.group(0));
//                p = Pattern.compile("([0-9]\\.)?[0-9](?= CU)");
//                m = p.matcher(line);
//                m.find();
//                while (s.hasNextLine() && !(line = s.nextLine()).isBlank()) {
//                    if (isNumeric(line.substring(1, 4))) {
//                        ICourse c = parser.parseSectionsHelper(line, code, courseNumber, s);
//                        System.out.println(c);
//                        System.out.println(c.instructor().getName());
//                        System.out.println(c.max());
//                    }
//                }
//            }
//        }
//
//    }
}
