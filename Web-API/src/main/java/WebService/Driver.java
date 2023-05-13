package WebService;

/*
 * @Author:Yu Gu AndrewID: ygu3
 *
 * The Log class is used to store driver data
 */
public class Driver {
    String name;
    String constructor;
    String position;
    String points;

    Driver(String givenName, String familyName, String constructor, String position, String points) {
        this.name = givenName + " " + familyName;
        this.constructor = constructor;
        this.position = position;
        this.points = points;
    }
}
