package ExampleAgent;

import RabbitMQUtils.RabbitMQPublisher;
import RobotMsgs.RobotControl;
import RobotMsgs.UWB;
import com.google.gson.Gson;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RobotControllerAgent extends Agent {

    private RabbitMQPublisher rabbitMQPublisher;

    List<double[]> pointsArray = new ArrayList<double[]>();

    double linearSpeed = 0.5;
    double angularSpeed = 0.5;

    List<UWB> robotsArray = new ArrayList<UWB>();
    Gson gson = new Gson();

    public RobotControllerAgent() throws Exception {

        pointsArray.add(new double[]{2.0, 0.0, 90.0});
        pointsArray.add(new double[]{2.0, 2.0, 90.0});
        pointsArray.add(new double[]{0.0, 2.0, 90.0});
        pointsArray.add(new double[]{0.0, 0.0, 90.0});
        initRobots();
        rabbitMQPublisher = new RabbitMQPublisher("robot_ctrl");
    }

    private void initRobots() {
        UWB a = new UWB();
        a.robot_id = 0;
        a.orientation.z = 0.0;
        robotsArray.add(a);

        UWB b = new UWB();
        b.robot_id = 1;
        b.orientation.z = 0.0;
        robotsArray.add(b);

        UWB c = new UWB();
        c.robot_id = 2;
        c.orientation.z = 0.0;
        robotsArray.add(c);
    }

    private double[] getCurrentGoal() {
        return pointsArray.get(0);
    }

    private double getGoalDistance(double curr_pos_x, double curr_pos_y) {
        double x_diff = getCurrentGoal()[0] - curr_pos_x;
        double y_diff = getCurrentGoal()[1] - curr_pos_y;

        return Math.sqrt(Math.pow(x_diff, 2) + Math.pow(y_diff, 2));
    }

    private void checkCurrentGoal(double goalDistance) {
        if (goalDistance < 0.05) {
            System.out.println("POINT UPDATED");
            pointsArray.remove(0);
        }
    }

    private RobotControl composeRobotControlMsg(double linear_x, double angular_z) {
        RobotControl msg = new RobotControl();

        msg.position.x = linear_x;
        msg.orientation.z = angular_z;

        return msg;
    }

    @Override
    public void setup() {
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                //  MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(INFORM);
                ACLMessage Message = receive();
                if (Message != null) {  /** Recieve Messages*/
//                    JSONArray robotPosMsgs = new JSONArray(Message.getContent());
                    UWB[] robotPosMsgs = gson.fromJson(Message.getContent(), UWB[].class);

                    System.out.println(robotPosMsgs);
                    RobotControl[] robotCtrlMsgs = new RobotControl[3];

                    for (UWB robotPosMsg : robotPosMsgs) {
                        double goalX = getCurrentGoal()[0];
                        double goalY = getCurrentGoal()[1];
                        double goalAngle = Math.toRadians(getCurrentGoal()[2]);
                        double lastRotation = robotsArray.get(robotPosMsg.robot_id).orientation.z;

                        double posX = robotPosMsg.position.x;
                        double posY = robotPosMsg.position.y;
                        double yaw = Math.toRadians(robotPosMsg.orientation.z);

                        double angular;
                        double linear;

                        double pathAngle = Math.atan2(goalY - posY, goalX - posX);

                        if (pathAngle < -(Math.PI / 4) || pathAngle > Math.PI / 40) {
                            if (goalY < 0 && posY < goalY)
                                pathAngle = -2 * Math.PI + pathAngle;
                            else if (goalY >= 0 && posY > goalY)
                                pathAngle = 2 * Math.PI + pathAngle;
                        }
                        if (lastRotation > Math.PI-0.1 && yaw <= 0)
                            yaw = 2*Math.PI + yaw;
                        else if (lastRotation < -Math.PI+0.1 && yaw > 0)
                            yaw = -2*Math.PI + yaw;

                        angular = angularSpeed * pathAngle - yaw;

                        double distance = getGoalDistance(posX, posY);
                        linear = Math.min(linearSpeed * distance, 0.1);

                        if (angular > 0)
                            angular = Math.min(angular, 1.5);
                        else
                            angular = Math.min(angular, -1.5);

                        robotsArray.set(robotPosMsg.robot_id, robotPosMsg);
                        RobotControl ctrlMsg = composeRobotControlMsg(linear, angular);
                        ctrlMsg.robot_id = robotPosMsg.robot_id;

                        robotCtrlMsgs[robotPosMsg.robot_id] = ctrlMsg;
                    }

                    try {
                        String msgString = gson.toJson(robotCtrlMsgs);
                        rabbitMQPublisher.sendMessage(msgString);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    block();  /** Block CyclicBehaviour If there is no message **/
                }
            }
        });

    }
}
