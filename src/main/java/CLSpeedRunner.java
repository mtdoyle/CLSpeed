import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

/**
 *
 */
public class CLSpeedRunner {

    private static Connection getConnectionFactory() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setVirtualHost("/");
        factory.setHost("localhost");
        factory.setPort(5672);
        return factory.newConnection();
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        int messageCount;
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<CLSpeed> addressList = new ArrayList<CLSpeed>();

        Connection conn = getConnectionFactory();

        Channel channel = conn.createChannel();

        channel.basicQos(1);

        messageCount = channel.queueDeclare("clspeed", true, false, false, null).getMessageCount();

        addressList.add(new CLSpeed("123 Main St, Minneapolis, MN 55442"));

        for (int i = 0; i < messageCount; i++){
            executor.submit(addressList.get(i));
        }

    }

}
