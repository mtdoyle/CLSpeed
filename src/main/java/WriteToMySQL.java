import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by mike on 3/16/16.
 */
public class WriteToMySQL {
    Connection conn = null;
    String street;
    String city;
    String zip;
    String lat;
    String lon;
    String garbage;
    String state = "MN";

    public WriteToMySQL(String address, String speed){
        String[] addressSplit = address.split(",");
        street = addressSplit[0];
        city = addressSplit[1];
        zip = addressSplit[2];
        lat = addressSplit[3];
        lon = addressSplit[4];
        garbage = addressSplit[5];

        try {
            conn =
                    DriverManager.getConnection("jdbc:mysql://192.168.1.211/clspeed?" +
                            "user=clspeed&password=clspeed");

            String sql = String.format("insert into clspeed " +
                    "(street, city, state, zip, speed, emm_lat, emm_lng, emm_acc)" +
                    "values ('%s', '%s', '%s', '%s', %s, %s, %s, '%s')",
                    street, city, state, zip, speed, lat, lon, garbage);

            conn.createStatement().execute(sql);
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    protected void writeToDB(String address, String speed) {
        System.out.println("hi");
    }

}
