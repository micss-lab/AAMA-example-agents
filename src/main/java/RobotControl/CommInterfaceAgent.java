package RobotControl;

import RabbitMQUtils.RabbitMQConsumer;
import RobotMsgs.IMU;
import RobotMsgs.Sonar;
import RobotMsgs.UWB;
import com.google.gson.Gson;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Arrays;

import static jade.lang.acl.ACLMessage.INFORM;

public class CommInterfaceAgent extends Agent {

    private final RabbitMQConsumer uwbConsumer;
    private final RabbitMQConsumer sonarConsumer;
    private final RabbitMQConsumer imuConsumer;

    private static final String TARGET_AGENT = "AgentProducer";

    Gson gson = new Gson();

    public CommInterfaceAgent() throws Exception {
        String uwbQueueName = "uwb";
        uwbConsumer = new RabbitMQConsumer(uwbQueueName);
        String sonarQueueName = "sonar";
        sonarConsumer = new RabbitMQConsumer(sonarQueueName);
        String imuQueueName = "imu";
        imuConsumer = new RabbitMQConsumer(imuQueueName);
    }

    private void startConsumerThread() throws Exception {
        uwbConsumer.startConsume();
        sonarConsumer.startConsume();
        imuConsumer.startConsume();
    }

    private void sendACLMessage(String msgBody) {
        ACLMessage messageTemplate = new ACLMessage(INFORM);
        messageTemplate.addReceiver(new AID(TARGET_AGENT, AID.ISLOCALNAME));
        messageTemplate.setContent(msgBody);

        send(messageTemplate);
    }

    @Override
    public void setup() {
        try {
            startConsumerThread();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                SequentialBehaviour Sequential = new SequentialBehaviour();
                addBehaviour(Sequential);
                Sequential.addSubBehaviour(new OneShotBehaviour() {
                    @Override
                    public void action() {
                        if (uwbConsumer.getIsNewMessage()) {
                            try {
                                // Example for sending the recieved UWB message to RobotController Agent
                                sendACLMessage(uwbConsumer.getMsgJSON().toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (sonarConsumer.getIsNewMessage()) {
                            try {
                                // Example for parsing the Sonar message into a JSON object. Check RobotMsgs package.
                                Sonar[] sonarArray = gson.fromJson(sonarConsumer.getMsgJSON().toString(), Sonar[].class);
                                System.out.println(sonarArray[0].header.frame_id);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (imuConsumer.getIsNewMessage()) {
                            try {
                                IMU[] imuArray = gson.fromJson(imuConsumer.getMsgJSON().toString(), IMU[].class);
                                System.out.println(imuArray[0].header.frame_id);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });

    }
}
