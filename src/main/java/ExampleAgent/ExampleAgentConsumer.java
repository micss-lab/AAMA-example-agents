package ExampleAgent;

import RabbitMQUtils.RabbitMQConsumer;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import static jade.lang.acl.ACLMessage.INFORM;

public class ExampleAgentConsumer extends Agent {

    private RabbitMQConsumer robotPosConsumer;

    private static final String TARGET_AGENT = "AgentProducer";

    public ExampleAgentConsumer() throws Exception {
        String robotName = "robot1";

        String queueName = robotName + "_pos";
        robotPosConsumer = new RabbitMQConsumer(queueName);
    }

    private void startConsumerThread() {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    robotPosConsumer.startConsume();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        t1.start();
    }

    @Override
    public void setup() {
        startConsumerThread();

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                SequentialBehaviour Sequential = new SequentialBehaviour();
                addBehaviour(Sequential);
                Sequential.addSubBehaviour(new OneShotBehaviour() {
                    @Override
                    public void action() {
                        if (robotPosConsumer.getIsNewMessage()){
                            try {
                                ACLMessage messageTemplate = new ACLMessage(INFORM);
                                messageTemplate.addReceiver(new AID(TARGET_AGENT, AID.ISLOCALNAME));

                                messageTemplate.setContent(robotPosConsumer.getMsgJSON().toString());

                                send(messageTemplate);
                                System.out.println(robotPosConsumer.getMsgJSON());
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
