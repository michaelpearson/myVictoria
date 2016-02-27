package nz.co.pearson.vuwexams.networking;

import android.net.Uri;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nz.co.pearson.vuwexams.networking.exceptions.AuthenticationError;
import nz.co.pearson.vuwexams.networking.exceptions.DataSourceError;
import nz.co.pearson.vuwexams.networking.models.ClassEvent;
import nz.co.pearson.vuwexams.networking.models.Course;

public class MyVicPortal implements DataSource {
    private String username;
    private String password;
    private static CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    private static boolean isAuthenticated = false;

    private String lastPage = null;

    private static final int MAX_LENGTH = 1048576; //1MB

    private static final String FORM_NAME_USERNAME = "user";
    private static final String FORM_NAME_UID_TOKEN = "uuid";
    private static final String FORM_NAME_PASSWORD = "pass";

    private static final String URL_GET_LOGIN_TOKEN = "https://my.vuw.ac.nz/cp/home/displaylogin";
    private static final String URL_GET_REQUIRED_COOKIES = "https://my.vuw.ac.nz/cp/home/loginf";
    private static final String URL_LOGIN = "https://my.vuw.ac.nz/cp/home/login";
    private static final String URL_STUDENT_RECORDS_SESSION_HANDOFF = "http://my.vuw.ac.nz/cp/ip/login?sys=sctssb&url=https://student-records.vuw.ac.nz/pls/webprod/bwskfshd.P_CrseSchd";
    private static final String URL_ACADEMIC_HISTORY = "https://student-records.vuw.ac.nz/pls/webprod/bwsxacdh.P_FacStuInfo";

    private static final String URL_TIMETABLE_PAGE = "https://student-records.vuw.ac.nz/pls/webprod/bwskfshd.P_CrseSchd?start_date_in=%d/%d/%d";

    private static final Pattern GET_UID_PATTERN = Pattern.compile("document\\.cplogin\\.uuid\\.value=\"((?:[a-f0-9]+-)+[a-f0-9]+)\"");
    private static final Pattern GET_STUDENT_RECORDS_LOGIN_TOKEN = Pattern.compile("document\\.location=\"([^\"]+)\";");

    private static final String LOGIN_SUCCESSFUL_HINT = "loginok";

    static {
        CookieHandler.setDefault(cookieManager);
    }

    public MyVicPortal(String username, String password) {
        this.username = username;
        this.password = password;

    }

    @Override
    public boolean authenticate() throws DataSourceError {
        if(isAuthenticated) {
            return true;
        }
        if(username == null || password == null || username.equals("") || password.equals("")) {
            throw new DataSourceError();
        }
        try {
            Matcher matches = GET_UID_PATTERN.matcher(getPage(URL_GET_LOGIN_TOKEN));
            if(!matches.find()) {
                throw new DataSourceError();
            }
            Map<String, String> postData = new HashMap<>();
            postData.put(FORM_NAME_PASSWORD, password);
            postData.put(FORM_NAME_USERNAME, username);
            postData.put(FORM_NAME_UID_TOKEN, matches.group(1));
            getPage(URL_GET_REQUIRED_COOKIES);

            String page = sendPostRequest(URL_LOGIN, postData);
            if (!page.contains(LOGIN_SUCCESSFUL_HINT)) {
                Log.i("Auth", page);
                throw new AuthenticationError();
            }
            Matcher loginTicket = GET_STUDENT_RECORDS_LOGIN_TOKEN.matcher(getPage(URL_STUDENT_RECORDS_SESSION_HANDOFF));
            if(!loginTicket.find()) {
                throw new DataSourceError();
            }
            String loginUrl = loginTicket.group(1);
            getPage(loginUrl);

            isAuthenticated = true;
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            throw new DataSourceError();
        }
    }

    private void dumpCookies() {
        log("Cookies:");
        for(HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
            log(String.format("URL: %s Value: %s", cookie.toString(), cookie.getValue()));
        }
    }

    private void dumpMap(Map<String, String> map) {
        for(Map.Entry<String, String> e : map.entrySet()) {
            log(String.format("Key: %s, Value: %s", e.getKey(), e.getValue()));
        }
    }


    @Override
    public List<Course> retrieveCourses() throws DataSourceError {
        if(!isAuthenticated) {
            authenticate();
        }
        List<Course> courses = new ArrayList<>();

        String academicHistoryPage;
        try {
            academicHistoryPage = getPage(URL_ACADEMIC_HISTORY);
        } catch(IOException e) {
            throw new AuthenticationError();
        }
        Document document = Jsoup.parse(academicHistoryPage);
        Elements periodTables = document.getElementsByAttributeValue("summary", "This table displays the student course history information.");
        Elements programTables = document.getElementsByAttributeValue("summary", "This table displays the student degree history information.");

        int i = 1;
        for(Element allCourses : periodTables) {
            Elements singleCourses = allCourses.getElementsByTag("tr");
            int year = 0;
            try {
                year = Integer.valueOf(programTables.get(i++).getElementsByTag("td").first().text().substring(0, 4));
                //TODO: there is a small possibility that vic won't have changed there system in 7,985 years. Could be a problem.
            } catch(Exception e) {
                e.printStackTrace();
                //Its hardly surprising that this line would crash
            }
            for(Element singleCourseRow : singleCourses) {
                Elements singleCourse = singleCourseRow.getElementsByTag("td");
                if(singleCourse.size() < 9) {
                    continue;
                }
                String code = singleCourse.get(0).text();
                String title = singleCourse.get(1).text();
                String period = singleCourse.get(2).text();
                int points = Integer.valueOf(singleCourse.get(3).text());
                double efts = Double.valueOf(singleCourse.get(4).text());
                String registration = singleCourse.get(5).text();
                String grade = singleCourse.get(6).text();
                int gradepoint = 0;
                try {
                    gradepoint = Integer.valueOf(singleCourse.get(7).text());
                } catch (Exception ignore) {}
                int pointsGained = Integer.valueOf(singleCourse.get(8).text());

                courses.add(new Course(year, grade, code, title, period, points, efts, registration, gradepoint, pointsGained));
            }
        }
        return courses;
    }

    @Override
    public List<ClassEvent> getEvents(int month, int year) throws DataSourceError {
        if(!isAuthenticated) {
            authenticate();
        }
        List<ClassEvent> build = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        while(calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DATE, -1);
        }

        int oldMonth = calendar.get(Calendar.MONTH);
        try {
            getWeek(build, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
            calendar.add(Calendar.DATE, 7);
            getWeek(build, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
            calendar.add(Calendar.DATE, 7);
            getWeek(build, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
            calendar.add(Calendar.DATE, 7);
            getWeek(build, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
            calendar.add(Calendar.DATE, 7);
            if (calendar.get(Calendar.MONTH) == oldMonth) {
                getWeek(build, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
            }
        } catch (IOException e) {
            throw new DataSourceError();
        }
        List<ClassEvent> filtered = new ArrayList<>();
        for(ClassEvent e : build) {
            if(e.getStartMonth() == month) {
                filtered.add(e);
            }
        }
        return filtered;
    }

    /**
     * Expects that the day of the month is a monday
     * @param build
     * @param day
     * @param month
     * @param year
     * @throws DataSourceError
     * @throws IOException
     */
    private void getWeek(List<ClassEvent> build, int day, int month, int year) throws DataSourceError, IOException {
        Log.i("Scraper", "Get week");
        Document page = Jsoup.parse(getPage(String.format(URL_TIMETABLE_PAGE, day, month, year)));
        Elements events = page.getElementsByClass("ddlabel");
        for(Element event : events) {
            if(event.children().size() > 0 && event.child(0).tagName().equals("a")) {

                int dayOffset;
                if(event.siblingElements().size() == 6) {
                    dayOffset = event.elementSiblingIndex();
                } else {
                    dayOffset = event.elementSiblingIndex() - 1;
                }

                String[] lines = event.child(0).html().split("<br>");
                String className = lines[0];
                Calendar[] times = parseTimes(day + dayOffset, month, year, lines[2]);
                String location = lines[3];
                build.add(new ClassEvent(times[0], times[1], className, location));
            }
        }
    }

    private static Calendar[] parseTimes(int day, int month, int year, String times) {
        String[] split = times.split("-");
        Calendar startTime = parseTime(day, month, year, split[0]);
        Calendar endTime = parseTime(day, month, year, split[1]);
        return new Calendar[] {startTime, endTime};
    }

    private static Calendar parseTime(int day, int month, int year, String time) {
        String[] split = time.split("[ :]");
        int hours = Integer.valueOf(split[0]) + (split[2].equals("pm") && !split[0].equals("12") ? 12 : 0);
        int minutes = Integer.valueOf(split[1]);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.HOUR_OF_DAY, hours);
        c.set(Calendar.MINUTE, minutes);
        c.set(Calendar.SECOND, 0);
        return c;
    }

    private String getPage(String url) throws IOException {
        return(getPage(new URL(url)));
    }

    private String getPage(URL url) throws IOException {
        return(getPage(url, true));
    }

    private String getPage(URL url, boolean shouldRetry) throws IOException {
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        if(lastPage != null) {
            connection.setRequestProperty("Referer", lastPage);
        }
        lastPage = url.toString();
        log(String.format("Got webpage: %d %s %s", connection.getResponseCode(), connection.getResponseMessage(), connection.getURL().toString()));
        if(connection.getResponseCode() == 403 && shouldRetry) {
            isAuthenticated = false;
            try {
                authenticate();
                return(getPage(url, false));
            } catch (DataSourceError ignore) {}
        }
        connection.setReadTimeout(2000);
        InputStream input = connection.getInputStream();
        String page = readInputStream(input);
        if(page.contains("break-in attempt")) {
            isAuthenticated = false;
            try {
                authenticate();
                return(getPage(url, false));
            } catch (DataSourceError ignore) {}
        }
        return(page);
    }

    private String sendPostRequest(String url, Map<String, String> data) throws IOException {
        return sendPostRequest(new URL(url), data);
    }
    private String sendPostRequest(URL url, Map<String, String> data) throws IOException {
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setReadTimeout(1000);
        connection.setConnectTimeout(3000);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        Uri.Builder builder = new Uri.Builder();
        for(Map.Entry<String, String> kv : data.entrySet()) {
            builder.appendQueryParameter(kv.getKey(), kv.getValue());
        }
        OutputStream output = connection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
        writer.write(builder.build().getEncodedQuery());
        writer.flush();
        writer.close();
        output.close();
        connection.getResponseMessage();
        InputStream inputStream = connection.getInputStream();
        return readInputStream(inputStream);
    }

    private String readInputStream(InputStream input) throws IOException {
        byte[] response = new byte[MAX_LENGTH];
        int position = 0;
        int d;
        while((d = input.read()) != -1) {
            response[position++] = (byte) (d & 0xFF);
            if(position > MAX_LENGTH) {
                throw new RuntimeException("Max size reached");
            }
        }
        return new String(response, Charset.forName("UTF-8"));
    }

    private void log(String message) {
        Log.i("Portal", message);
    }
}
