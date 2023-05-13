package WebService;

import com.google.gson.Gson;
import com.mongodb.client.*;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * @Author:Yu Gu AndrewID: ygu3
 *
 * The WebModel class is the model in this web service, which is used for
 * 1. Fetch f1 driver standings from the third-party Ergast api
 * 2. Fetching historic logs from MongoDB and store the new log
 * 3. Return the response string to WebServlet
 */
public class WebModel {

    static ArrayList<Log> logs;
    static PriorityQueue<Double> pointsRanks;
    static List<Map.Entry<String, Integer>> yearList;
    static List<Map.Entry<String, Integer>> positionList;

    // this method will get http request, store the log into MongoDB, then return the search results
    public static String search(String year, String position) {
        // fetch results from search parameters
        ArrayList<Driver> driverList = fetchData(year, position);
        // format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy");
        String timestamp = sdf.format(new Date(System.currentTimeMillis()));
        // add logs to db
        if (position.equals("")) position = "all";
        for (Driver driver : driverList) {
            addDBData(year, position, timestamp, driver.name, driver.constructor, driver.points);
        }
        // get all db historic logs
        getDBData();
        String result = new Gson().toJson(driverList);
        return result;
    }

    // this method will fetch data from the third-party Ergast api
    public static ArrayList<Driver> fetchData(String searchYear, String searchPosition) {
        ArrayList<Driver> driverList = new ArrayList<>();
        try {
            String searchString = "";
            if (!searchPosition.equals("")) searchString = "/" + searchPosition;
            // api format: https://ergast.com/api/f1/2022/driverStandings/1.json
            String api = "https://ergast.com/api/f1/" + searchYear + "/driverStandings" + searchString + ".json";
            String result = fetch(api);
            JSONObject json = (JSONObject) JSONValue.parse(result);
            JSONObject standingsTable = (JSONObject) ((JSONObject) json.get("MRData")).get("StandingsTable");
            JSONObject standings = (JSONObject) ((JSONArray) standingsTable.get("StandingsLists")).get(0);
            JSONArray driverStandings = (JSONArray) standings.get("DriverStandings");
            for (int i = 0; i < driverStandings.size(); i++) {
                JSONObject driver = (JSONObject) driverStandings.get(i);
                String givenName = (String) ((JSONObject) driver.get("Driver")).get("givenName");
                String familyName = (String) ((JSONObject) driver.get("Driver")).get("familyName");
                JSONArray constructorArray = (JSONArray) driver.get("Constructors");
                String constructor = (String) ((JSONObject) constructorArray.get(0)).get("name");
                String position = (String) driver.get("position");
                String points = (String) driver.get("points");
                driverList.add(new Driver(givenName, familyName, constructor, position, points));
            }

        } catch (Exception e) {
            System.out.println("Third-party API cannot get the request data");
        }
        return driverList;
    }

    // this method is used to get historical logs in MongoDB
    static public void getDBData() {
        MongoClient client = MongoClients
                .create("mongodb://ygu3:ygu3@ac-91wyje6-shard-00-00.lwmjzaf.mongodb.net:27017" +
                        ",ac-91wyje6-shard-00-01.lwmjzaf.mongodb.net:27017" +
                        ",ac-91wyje6-shard-00-02.lwmjzaf.mongodb.net:27017/test?w=majority&retryWrites=true&tls=true&authMechanism=SCRAM-SHA-1");
        MongoDatabase db = client.getDatabase("project_4");
        MongoCollection collection = db.getCollection("history");
        FindIterable<Document> iterDoc = collection.find();
        logs = new ArrayList<>();
        MongoCursor<Document> cursor = iterDoc.iterator();
        analyzeLogs(cursor);
    }

    // this method is used to create data structure for the dashboard
    static private void analyzeLogs(MongoCursor<Document> cursor) {
        // initialize hashmap to store all documents' data
        Map<String, Integer> yearCount = new HashMap<>();
        Map<String, Integer> positionCount = new HashMap<>();
        // use priority queue to store points
        pointsRanks = new PriorityQueue<>((x, y) -> Double.compare(y, x));

        while (cursor.hasNext()) {
            Document doc = cursor.next();
            String username = (String) doc.get("name");
            String timestamp = (String) doc.get("timestamp");
            String year = (String) doc.get("year");
            String position = (String) doc.get("position");
            String driverName = (String) doc.get("driverName");
            String constructor = (String) doc.get("constructor");
            String points = (String) doc.get("points");

            yearCount.putIfAbsent(year, 0);
            yearCount.put(year, yearCount.get(year) + 1);
            positionCount.putIfAbsent(position, 0);
            positionCount.put(position, positionCount.get(position) + 1);
            pointsRanks.add(Double.valueOf(points));

            // stored in logs
            logs.add(new Log(timestamp, username, year, position, driverName, constructor, points));
        }

        // rank the search frequency
        yearList = new ArrayList<>(yearCount.entrySet());
        Collections.sort(yearList, new Comparator<>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue() - o1.getValue());
            }
        });

        positionList = new ArrayList<>(positionCount.entrySet());
        Collections.sort(positionList, new Comparator<>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue() - o1.getValue());
            }
        });
    }


    // this method is used to get the most frequently searched year
    public static String getFrequentYear() {
        String result = null;
        try {
            result = yearList.get(0).getKey();
        } catch (Exception e) {
            System.out.println("No such data");
        }
        return result;
    }

    // this method is used to get the most frequently searched position
    public static String getFrequentPosition() {
        String result = null;
        try {
            result = positionList.get(0).getKey();
        } catch (Exception e) {
            System.out.println("No such data");
        }
        return result;
    }

    // this method is used to get the most frequently searched driver
    public static Double getHighestPoints() {
        Double result = null;
        try {
            result = pointsRanks.poll();
        } catch (Exception e) {
            System.out.println("No such data");
        }
        return result;
    }

    // this method is used to store new log to MongoDB
    static public void addDBData(String year, String position, String timestamp, String driverName, String constructor, String points) {
        MongoClient client = MongoClients
                .create("mongodb://ygu3:ygu3@ac-91wyje6-shard-00-00.lwmjzaf.mongodb.net:27017" +
                        ",ac-91wyje6-shard-00-01.lwmjzaf.mongodb.net:27017" +
                        ",ac-91wyje6-shard-00-02.lwmjzaf.mongodb.net:27017/test?w=majority&retryWrites=true&tls=true&authMechanism=SCRAM-SHA-1");
        MongoDatabase db = client.getDatabase("project_4");
        MongoCollection collection = db.getCollection("history");
        String username = "user";
        Document doc = new Document("name", username);
        doc.append("timestamp", timestamp);
        doc.append("year", year);
        doc.append("position", position);
        doc.append("driverName", driverName);
        doc.append("constructor", constructor);
        doc.append("points", points);
        collection.insertOne(doc);
    }

    // this method is used to fetch json data from a given url
    static private String fetch(String urlString) {
        String response = "";
        try {
            System.out.println(urlString);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String str;
            while ((str = in.readLine()) != null) {
                response += str;
            }
            in.close();
        } catch (IOException e) {
            System.out.println("Third-party API unavailable");
        }
        return response;
    }
}
