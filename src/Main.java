import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

public class Main {

    public static void main(String[] args)  throws StaleProxyException{
        Runtime rt = Runtime.instance();
        rt.setCloseVM(true);
        rt.createMainContainer(new ProfileImpl()).createNewAgent("rma", "jade.tools.rma.rma", new Object[]{}).start();

        AgentContainer c = rt.createAgentContainer(new ProfileImpl());
        c.createNewAgent("Profiler", "ProfilerAgent", new Object[]{}).start();
        c.createNewAgent("Curator", "CuratorAgent", new Object[]{}).start();
        c.createNewAgent("TourGuide", "TourGuideAgent", new Object[]{}).start();
    }

}