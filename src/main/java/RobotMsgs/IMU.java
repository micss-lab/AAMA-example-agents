package RobotMsgs;

public class IMU {

    public Header header = new Header();
    public Orientation orientation = new Orientation();
    public Position angular_velocity = new Position();
    public Position linear_acceleration = new Position();

}
