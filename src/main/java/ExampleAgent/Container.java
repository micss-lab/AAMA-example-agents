package ExampleAgent;


import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;


public class Container   {
    public static void main(String[] args) {


        try {
            Runtime runtime = Runtime.instance();
            Properties properties = new ExtendedProperties();
            properties.setProperty(Profile.GUI, "false");
            Profile profile = new ProfileImpl(properties);
            AgentContainer agentContainer=runtime.createMainContainer(profile);

            Container.start();

            AgentController agentProducer=agentContainer.createNewAgent("AgentProducer",
                    "ExampleAgent.RobotControllerAgent",new Object[]{});
            AgentController agentConsumer=agentContainer.createNewAgent("AgentConsumer",
                    "ExampleAgent.CommInterfaceAgent",new Object[]{});
            agentProducer.start();
            agentConsumer.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void start() {
        //  InitComp.initComponents(); /**Configuration of all components **/

    }

}

