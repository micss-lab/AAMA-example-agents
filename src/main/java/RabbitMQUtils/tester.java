package RabbitMQUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class tester {

    public static void main(String[] args) throws Exception {
//        RabbitMQPublisher testPublisher = new RabbitMQPublisher("test");
        RabbitMQConsumer testConsumer =  new RabbitMQConsumer("uwb");

//        testPublisher.sendMessage(new JSONObject("{\"value\": \"adasdad\", \"onclick\": \"CreateNewDoc()\"}"));
        testConsumer.startConsume();
    }

}
