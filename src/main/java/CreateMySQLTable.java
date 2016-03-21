import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by mike on 3/16/16.
 */
public class CreateMySQLTable {

    public void createNewTable(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
        Calendar cal = Calendar.getInstance();
        String currDate = dateFormat.format(cal.getTime());
        String charset = "[CHARACTER SET utf8 COLLATE utf8_bin]";

        String sql = String.format("create table if not exists clspeed_%1$s" +
                "(street varchar(100) %2$s, city varchar(100) %2$s, state varchar(2) %2$s, zip int(5), speed int(4), " +
                "emm_lat decimal(12,10), emm_lng decimal(12,10), emm_acc varchar(20) %2$s)"
                , currDate, charset);

        executeStatement(sql);
    }

    protected void executeStatement(String sql){
        Connection conn = null;
        try {
            conn =
                DriverManager.getConnection("jdbc:mysql://192.168.1.211/clspeed?" +
                    "user=clspeed&password=clspeed");

            conn.createStatement().execute(sql);
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

}
