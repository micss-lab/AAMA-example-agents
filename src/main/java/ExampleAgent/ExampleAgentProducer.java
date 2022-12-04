package ExampleAgent;

import RabbitMQUtils.RabbitMQPublisher;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.json.JSONArray;

import java.io.IOException;

public class ExampleAgentProducer extends Agent {

    private RabbitMQPublisher rabbitMQPublisher;

    public ExampleAgentProducer() throws Exception {
        String robotName = "robot1";

        String queueName = robotName + "_cmd";
//        rabbitMQPublisher = new RabbitMQPublisher(queueName);
    }

    @Override
    public void setup() {
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                //  MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(INFORM);
                ACLMessage Message=receive();
                if (Message!=null) {  /** Recieve Messages*/
                    JSONArray robotPosMsg = new JSONArray(Message.getContent());

                    System.out.println(robotPosMsg);
//                    try {
//                        rabbitMQPublisher.sendMessage(robotPosMsg);
//
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
                }
                else {
                    block();  /** Block CyclicBehaviour If there is no message **/
                }
            }
        });

    }
}
