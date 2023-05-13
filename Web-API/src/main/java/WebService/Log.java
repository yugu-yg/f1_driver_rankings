package WebService;

/*
 * @Author:Yu Gu AndrewID: ygu3
 *
 * The Log class is used to store log data
 */
public class Log {
    String timestamp;
    String username;
    String year;
    String position;
    String driverName;
    String constructor;
    String points;

    Log(String timestamp, String username, String year, String position, String driverName, String constructor, String points) {
        this.timestamp = timestamp;
        this.username = username;
        this.year = year;
        this.position = position;
        this.driverName = driverName;
        this.constructor = constructor;
        this.points = points;
    }

    @Override
    public String toString() {
        return ". Time: " + timestamp + ", User:" + username + ", Year: " + year + ", Position: " + position + ", Driver: " + driverName + ", Constructor: " + constructor + ", Points: " + points;
    }
}
