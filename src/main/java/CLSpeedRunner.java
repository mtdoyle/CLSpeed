import com.rabbitmq.client.*;

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
        ExecutorService executor = Executors.newFixedThreadPool(1);
        List<CLSpeed> addressList = new ArrayList<CLSpeed>();

        Connection conn = getConnectionFactory();

        Channel channel = conn.createChannel();

        channel.basicQos(1);

        messageCount = channel.queueDeclare("clspeed", true, false, false, null).getMessageCount();

        for (int i = 0; i < messageCount; i++){
            GetResponse response = channel.basicGet("clspeed", false);
            if (response == null){
                //no message received
            } else {
                AMQP.BasicProperties props = response.getProps();
                byte[] body = response.getBody();
                long deliveryTag = response.getEnvelope().getDeliveryTag();
                executor.submit(new CLSpeed(new String(body, "UTF-8")));
                channel.basicAck(deliveryTag, false);
            }

        }

    }

}
