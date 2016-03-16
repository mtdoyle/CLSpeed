import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class CLSpeedRunner {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<CLSpeed> addressList = new ArrayList<CLSpeed>();

        addressList.add(new CLSpeed("123 Main St, Minneapolis, MN 55442"));

        for (int i = 0; i < addressList.size() - 1; i++){
            executor.submit(addressList.get(i));
        }

    }


}
