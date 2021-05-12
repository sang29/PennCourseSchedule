import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * A class for parsing subjects, courses, and sections available at UPenn. There is no consolidated
 * courses database that is available to students, so we had to build our own by parsing multiple
 * pages on the UPenn website.
 * 
 * @author Philipp Gaissert & Sang Ik Han
 *
 */
public class PennParser implements IPennParser {

    @Override
    public Map<String, String> parseSubjects() {
        String courseIndexUrl =
                "https://srfs.upenn.edu/registration-catalog-calendar/timetables/main";
        // Create a TreeMap that will map each subject code to the full name of the subject
        Map<String, String> subjects = new TreeMap<>();
        try {
            // Get the HTML content of the course index
            Document courseIndex = Jsoup.connect(courseIndexUrl).get();
            // Get the HTML elements for each row in the table of course subjects
            Elements subjectTableRows = courseIndex.getElementsByTag("table").get(0)
                    .getElementsByTag("tr");
            // For each row element in the table
            for (Element row : subjectTableRows) {
                // Get the columns in the row
                Elements cols = row.getElementsByTag("td");
                // Get the subject code from the first column
                String subjectCode = cols.get(0).text();
                // Get the subject name from the second column
                String subjectName = cols.get(1).getElementsByTag("a").text();
                // Add an entry for the subject code and subject name to the map
                subjects.put(subjectCode, subjectName);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return subjects;
    }

    public Map<String, Map<Integer, String[]>> parseCourses(Map<String, String> subjects) {
        String catalogBaseUrl = "https://catalog.upenn.edu/search/?P=";
        // Create a TreeMap that will map each subject code to another Map
        // The nested Maps will map a course number to the full name of the course
        Map<String, Map<Integer, String[]>> subjectToCoursesMap = new TreeMap<>();
        // If the subjects is null or an empty map, return an empty map
        if (subjects == null || subjects.isEmpty()) {
            return subjectToCoursesMap;
        }
        Pattern p = Pattern.compile("(?<=Prerequisite[s]*: ).*$");
        try {
            // Iterate over each subject code
            for (String code : subjects.keySet()) {
                // Create a TreeMap that will map each course number to the full name of the course
                Map<Integer, String[]> numberToInfoMap = new TreeMap<>();
                // Get the HTML content of this subject's page in the UPenn catalog
                Document subjectCatalog = Jsoup.connect(catalogBaseUrl + code).get();
                // Get the HTML elements for each course of this subject
                Elements courses = subjectCatalog.getElementsByClass("search-courseresult");
                // For each course element
                for (Element course : courses) {
                    String[] titleAndPrereqs = new String[2];
                    // Get the heading (contains the course number and course title)
                    String courseHeading = course.getElementsByTag("h2").get(0).text();
                    // Parse the course number from the heading
                    int courseNumber = Integer
                            .parseInt(courseHeading.replaceAll("\\D", "").substring(0, 3));
                    // Parse the course title from the heading
                    titleAndPrereqs[0] = courseHeading.split("\\b(\\d{3})\\b")[1].trim();
                    Elements blocks = course.getElementsByClass("courseblockextra");
                    // For each block
                    for (Element block : blocks) {
                        // Get the text content of the block
                        String blockText = block.text();
                        // If the block contains information about the course's prerequisites
                        if (blockText.contains("Prerequisite:")
                                || blockText.contains("Prerequisites:")) {
                            // Parse out the important information
                            Matcher m = p.matcher(block.text());
                            m.find();
                            titleAndPrereqs[1] = m.group(0);
                            // Add an entry for the course number and the course's prerequisites
                            numberToInfoMap.put(courseNumber, titleAndPrereqs);
                            break;
                        }
                        // If prerequisites were not found, add an entry with an empty string
                        titleAndPrereqs[1] = "";
                    }
                    // Add an entry for the course number and course title to this subject's map
                    numberToInfoMap.put(courseNumber, titleAndPrereqs);
                }
                // Add an entry for the subject code and courses map to overall map
                subjectToCoursesMap.put(code, numberToInfoMap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return subjectToCoursesMap;
    }

    @Override
    public Collection<ICourse> parseSections(Map<String, String> subjects) {
        // Create a TreeSet that will hold all sections in the upcoming semester
        Collection<ICourse> sections = new TreeSet<>();
        try {
            for (Entry<String, String> subject : subjects.entrySet()) {
                String code = subject.getKey();
                // Edge case: There is no https://www.registrar.upenn.edu/timetable/benf.html
                // Instead, it is https://www.registrar.upenn.edu/timetable/benfg.html
                if (code.equals("BENF")) {
                    code = "BENFG";
                }
                String timeTableUrl = "https://www.registrar.upenn.edu/timetable/"
                        + code.toLowerCase() + ".html";
                // Get the HTML content of this subject's time table
                Document timeTable = Jsoup.connect(timeTableUrl).get();
                code = subject.getKey();
                Elements elements = timeTable.getElementsByTag("pre").get(0).getElementsByTag("p");
                String timeTableText;
                // Get the text content that has information for each section
                // Edge case: Some pages have an extra <p> element
                if (elements.get(0).text().contains("*")) {
                    timeTableText = elements.get(2).text();
                } else {
                    timeTableText = elements.get(1).text();
                }
                // Create a scanner for reading in the text line-by-line
                Scanner s = new Scanner(timeTableText);
                while (s.hasNextLine()) {
                    String line;
                    // Skip blank lines
                    do {
                        line = s.nextLine();
                    } while (line.isBlank());
                    // If the line starts with the subject code, we are at the start of a course
                    if (line.substring(0, code.length()).equals(code)) {
                        Pattern p = Pattern.compile("(?<=" + code + "\\s*-)[0-9]{3}");
                        Matcher m = p.matcher(line);
                        m.find();
                        // Parse the course number from the line
                        int courseNumber = Integer.parseInt(m.group(0));
                        p = Pattern.compile("([0-9]\\.)?[0-9](?= CU)");
                        m = p.matcher(line);
                        m.find();
                        // Parse the course units from the line
                        double units = Double.parseDouble(m.group(0));
                        // Read all lines in the time table for this course
                        while (s.hasNextLine() && !((line = s.nextLine()).isBlank())) {
                            // If the line starts with a section number
                            if (isNumeric(line.substring(1, 4))) {
                                // Create a Course object for this section
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

    /**
     * @param str          String containing section info from the UPenn time table
     * @param code         Subject code, e.g. "CIT"
     * @param courseNumber Course number, e.g. 594
     * @param scnr         Scanner object
     * @return Course object for a distinct section
     */
    private Course parseSectionsHelper(String str, String code, int courseNumber, Scanner s) {
        Pattern p;
        Matcher m;
        // Split up the line using white space as a delimiter
        String[] info = str.trim().split("\\s+");
        // Parse the section number
        int sectionNumber = Integer.parseInt(info[0]);
        // Create a new Course object with the subject code, course number, and section number
        Course c = new Course(code, courseNumber, sectionNumber);
        String instructor = "";
        // Parse the section type and set it as the Course object's type
        c.setType(parseType(info[1]));
        // Parse the section instructor
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
        // Check if additional formatting needs to be done for the instructor
        if (instructor.length() > 0) {
            instructor = instructor.substring(0, 1).toUpperCase() + instructor.substring(1);
            int i;
            if ((i = instructor.indexOf("/")) != -1) {
                instructor = instructor.replace("/" + instructor.charAt(i + 1),
                        "/" + Character.toUpperCase(instructor.charAt(i + 1)));
            }
        }
        // Set the Course object's instructor
        c.setInstructorStr(instructor);
        while (s.hasNextLine() && !(str.contains("MAX:") || str.contains("MAX W/"))) {
            str = s.nextLine().trim();
        }
        // If the end of the time table has been reached, return the course object
        if (!s.hasNextLine()) {
            return c;
        }
        // Otherwise, get the max capacity from the following line
        p = Pattern.compile("(?<=MAX.*: )\\d+");
        m = p.matcher(str);
        m.find();
        c.setMax(Integer.parseInt(m.group(0)));
        return c;
    }

    /**
     * Sets a Course object's startTime and duration given a string that contains a meeting time
     * 
     * @param meetingTime String that contains a course's meeting time, e.g. "10:30-12PM"
     * @param section     Course object to have to have its startTime and duration set
     */
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

        // Determine whether the start time is AM or PM
        // If the end time is PM
        if (endPM) {
            // If the duration would be ≥ 45 minutes with a PM start time, add 12 to its hour value
            if (((60 * endHour) + endMinute) - ((60 * (startHour + 12)) + startMinute) >= 45) {
                startHour += 12;
            }
        }

        int duration = ((60 * endHour) + endMinute) - ((60 * startHour) + startMinute);

        section.setStartTime(startHour, startMinute);
        section.setDuration(duration);
    }

    /**
     * @param typeCode
     * @return unabbreviated section type
     */
    public String parseType(String typeCode) {
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

    /**
     * @param str
     * @return true if the string is a number, false otherwise
     */
    private static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

}
