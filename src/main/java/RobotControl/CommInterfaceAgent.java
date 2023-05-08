package RobotControl;

import RabbitMQUtils.RabbitMQConsumer;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

import static jade.lang.acl.ACLMessage.INFORM;

public class CommInterfaceAgent extends Agent {

    private final RabbitMQConsumer uwbConsumer;
//    private final RabbitMQConsumer sonarConsumer;
//    private final RabbitMQConsumer imuConsumer;

    private static final String TARGET_AGENT = "AgentProducer";

    public CommInterfaceAgent() throws Exception {
//        String robotName = "robot1";

        String uwbQueueName = "uwb";
        uwbConsumer = new RabbitMQConsumer(uwbQueueName);
//        String sonarQueueName = "sonar";
//        sonarConsumer = new RabbitMQConsumer(sonarQueueName);
//        String imuQueueName = "imu";
//        imuConsumer = new RabbitMQConsumer(imuQueueName);
    }

    private void startConsumerThread() throws Exception {
//        Thread t1 = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    robotPosConsumer.startConsume();
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        });
//        t1.start();
        uwbConsumer.startConsume();
//        sonarConsumer.startConsume();
//        imuConsumer.startConsume();
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
                        if (uwbConsumer.getIsNewMessage()){
                            try {
//                                System.out.println(uwbConsumer.getMsgJSON());
                                sendACLMessage(uwbConsumer.getMsgJSON().toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
//                        if (sonarConsumer.getIsNewMessage()){
//                            try {
//                                System.out.println(sonarConsumer.getMsgJSON());
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        if (imuConsumer.getIsNewMessage()){
//                            try {
//                                System.out.println(imuConsumer.getMsgJSON());
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
                    }
                });
            }
        });

    }
}
