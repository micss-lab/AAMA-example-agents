package RabbitMQUtils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import org.json.JSONArray;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RabbitMQPublisher {
    private final String queueName;

    private final Channel channel;

    public RabbitMQPublisher(String queueName) throws Exception {
        this.queueName = queueName;
        this.channel = RabbitMQHelper.connectChannel();
    }

    public void sendMessage(String msgJSON) throws IOException {
        byte[] msgByte = msgJSON.getBytes(StandardCharsets.UTF_8);
        channel.basicPublish("", queueName, MessageProperties.MINIMAL_PERSISTENT_BASIC, msgByte);

//        System.out.println(" [x] Sent '" + msgJSON + "'");
    }
}
