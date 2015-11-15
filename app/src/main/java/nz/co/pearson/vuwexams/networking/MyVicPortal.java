package nz.co.pearson.vuwexams.networking;

import android.net.Uri;
import android.text.Html;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nz.co.pearson.vuwexams.networking.exceptions.AuthenticationError;
import nz.co.pearson.vuwexams.networking.exceptions.DataSourceError;
import nz.co.pearson.vuwexams.networking.exceptions.ParseError;

public class MyVicPortal implements DataSource {
    private String username;
    private String password;
    private static CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
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

    private static final Pattern GET_UID_PATTERN = Pattern.compile("document\\.cplogin\\.uuid\\.value=\"((?:[a-f0-9]+-)+[a-f0-9]+)\"");
    private static final Pattern GET_STUDENT_RECORDS_LOGIN_TOKEN = Pattern.compile("document\\.location=\"([^\"]+)\";");

    private static final String LOGIN_SUCCESSFUL_HINT = "loginok";

    public static String debugData = null;

    public MyVicPortal(String username, String password) {
        this.username = username;
        this.password = password;
        CookieHandler.setDefault(cookieManager);
    }

    @Override
    public boolean authenticate() throws DataSourceError {
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

            if (!sendPostRequest(URL_LOGIN, postData).contains(LOGIN_SUCCESSFUL_HINT)) {
                throw new AuthenticationError();
            }
            Matcher loginTicket = GET_STUDENT_RECORDS_LOGIN_TOKEN.matcher(getPage(URL_STUDENT_RECORDS_SESSION_HANDOFF));
            if(!loginTicket.find()) {
                throw new DataSourceError();
            }
            String loginUrl = loginTicket.group(1);
            getPage(loginUrl);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DataSourceError();
        }
        return false;
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
                int gradepoint = Integer.valueOf(singleCourse.get(7).text());
                int pointsGained = Integer.valueOf(singleCourse.get(8).text());

                courses.add(new Course(year, grade, code, title, period, points, efts, registration, gradepoint, pointsGained));
            }
        }
        return courses;
    }

    private String getPage(String url) throws IOException {
        return(getPage(new URL(url)));
    }

    private String getPage(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        if(lastPage != null) {
            connection.setRequestProperty("Referer", lastPage);
        }
        lastPage = url.toString();
        log(String.format("Got webpage: %d %s", connection.getResponseCode(), connection.getResponseMessage()));
        connection.setReadTimeout(2000);
        InputStream input = connection.getInputStream();
        return(readInputStream(input));
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
