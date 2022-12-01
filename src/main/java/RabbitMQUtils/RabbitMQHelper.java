package RabbitMQUtils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;


public class RabbitMQHelper {

    private static final String USERNAME = "dogu";
    private static final String PASSWORD = "dogu";

    public RabbitMQHelper() {}

    public static Channel connectChannel() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);
        Connection connection = factory.newConnection();
        return connection.createChannel();
    }
}
