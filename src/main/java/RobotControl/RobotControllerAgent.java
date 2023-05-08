package RobotControl;

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

    double linearSpeed = 0.1;
    double angularSpeed = 0.25;

    List<UWB> robotsArray = new ArrayList<UWB>();
    Gson gson = new Gson();

    public RobotControllerAgent() throws Exception {

        setPoints();
        initRobots();
        rabbitMQPublisher = new RabbitMQPublisher("robot_ctrl");
    }

    private void initRobots() {
        UWB a = new UWB();
        a.robot_id = 0;
        a.orientation.z = 90.0;
        robotsArray.add(a);

//        UWB b = new UWB();
//        b.robot_id = 1;
//        b.orientation.z = 90.0;
//        robotsArray.add(b);
//
//        UWB c = new UWB();
//        c.robot_id = 2;
//        c.orientation.z = 90.0;
//        robotsArray.add(c);
    }

    private void setPoints() {
        pointsArray.add(new double[]{2.0, 0.0, 90.0});
        pointsArray.add(new double[]{0.0, 2.0, 90.0});
        pointsArray.add(new double[]{1.0, 3.0, 90.0});
        pointsArray.add(new double[]{2.0, 5.0, 90.0});
        pointsArray.add(new double[]{1.0, 6.0, 90.0});
        pointsArray.add(new double[]{0.0, 6.0, 90.0});
        pointsArray.add(new double[]{0.0, 0.0, 90.0});
        pointsArray.add(new double[]{2.0, 2.0, 90.0});
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
        if (goalDistance < 0.1) {
            System.out.println("POINT UPDATED");
            pointsArray.remove(0);
        }
        if (pointsArray.isEmpty()){
            setPoints();
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
//                    System.out.println(robotPosMsgs);
                    RobotControl[] robotCtrlMsgs = new RobotControl[robotPosMsgs.length];

                    UWB robotPosMsg = robotPosMsgs[0];
                    double posX = robotPosMsg.position.x;
                    double posY = robotPosMsg.position.y;
                    double yaw = Math.toRadians(robotPosMsg.orientation.z);
                    checkCurrentGoal(getGoalDistance(posX, posY));

                    double goalX = getCurrentGoal()[0];
                    double goalY = getCurrentGoal()[1];
                    double goalAngle = Math.toRadians(getCurrentGoal()[2]);
                    double lastRotation = Math.toRadians(robotsArray.get(robotPosMsg.robot_id).orientation.z);

                    double angular;
                    double linear;

                    double pathAngle = Math.atan2(goalY - posY, goalX - posX) - yaw;
                    double diffGoalAngle = Math.atan2(Math.sin(pathAngle), Math.cos(pathAngle));

                    angular = angularSpeed * diffGoalAngle;

                    double distance = getGoalDistance(posX, posY);

                    if (distance < 0.1 || Math.abs(Math.toDegrees(diffGoalAngle)) > 25){
                        linear = 0;
                    }
                    else if (linearSpeed * distance < 0.1)
                        linear = 0.1;
                    else
                        linear = 0.2;

                    if (Math.abs(Math.toDegrees(diffGoalAngle)) < 2)
                        angular = 0;
                    else if (angular > 0)
                        angular = Math.max(angular, 0.1);
                    else
                        angular = Math.min(angular, -0.1);

                    robotsArray.set(robotPosMsg.robot_id, robotPosMsg);
                    RobotControl ctrlMsg = composeRobotControlMsg(linear, angular);
                    ctrlMsg.robot_id = robotPosMsg.robot_id;

                    robotCtrlMsgs[robotPosMsg.robot_id] = ctrlMsg;

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
