import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.*;

/**
 *
 */
public class CLSpeedRunner {

    private static Connection getConnectionFactory() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setVirtualHost("/");
        factory.setHost("192.168.1.211");
        factory.setPort(5672);
        return factory.newConnection();
    }

    public static void main(String[] args) throws IOException, TimeoutException, ExecutionException, InterruptedException {
        int messageCount;
        ExecutorService executor = Executors.newFixedThreadPool(10);

        Connection conn = getConnectionFactory();

        Channel channel = conn.createChannel();

        channel.basicQos(1);

        messageCount = channel.queueDeclare("clspeed", true, false, false, null).getMessageCount();

        channel.close();

        for (int i = 0; i < messageCount; i++) {
            executor.submit(new CLSpeed());
        }
    }

}


