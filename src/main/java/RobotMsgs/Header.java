package RobotMsgs;

public class Header {
    public Stamp stamp = new Stamp();
    public String frame_id;

}

class Stamp {
    public int sec;
    public int nanosec;
}
