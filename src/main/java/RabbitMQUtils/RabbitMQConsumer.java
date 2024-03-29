package RabbitMQUtils;

import com.rabbitmq.client.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class RabbitMQConsumer {

    private final String queueName;

    private final Channel channel;

    private JSONArray msgJSON;

    private String oldMsgBody = "";
    private boolean isNewMessage;

    public RabbitMQConsumer(String queueName) throws Exception {
        this.queueName = queueName;

        channel = RabbitMQHelper.connectChannel();
    }

    private void connectQueue() throws Exception {
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
    }

    public void startConsume() throws Exception {
        connectQueue();
        channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                final String msgBody = new String(body, StandardCharsets.UTF_8); // a 'final' copy of the body that you can pass to the runnable
                final long msgTag = envelope.getDeliveryTag();

                try {
                    msgJSON = new JSONArray(msgBody);
                } catch (JSONException e) {
                    System.out.println("Message Body is not a valid JSON.");
                } finally {
                    isNewMessage = !Objects.equals(oldMsgBody, msgBody);
                    oldMsgBody = msgBody;
                }
            }
        });
    }

    public boolean getIsNewMessage() {
        return isNewMessage;
    }

    public JSONArray getMsgJSON() {
        isNewMessage = false;
        return msgJSON;
    }
}
