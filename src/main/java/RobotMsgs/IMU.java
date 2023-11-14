package RobotMsgs;

public class IMU {

    public Header header = new Header();
    public QuaternionOrientation orientation = new QuaternionOrientation();
    public Position angular_velocity = new Position();
    public Position linear_acceleration = new Position();

}
