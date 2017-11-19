import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfilerAgent extends  Agent{
    private final int age = 24;
    private final String occupation = "Software Engineer";
    private final String interest = "Painting";
    ArrayList<Artifact> res;


    // Agent initialization
    @Override
    protected void setup() {
        System.out.println("Agent "+getAID().getName()+" is ready.");

        // register with DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("profiler" );
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {DFService.register(this, dfd);}
        catch (FIPAException fe) {}

        // search for tour guide
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription tsd = new ServiceDescription();
        tsd.setType("tour-guide");
        template.addServices(tsd);
        DFAgentDescription[] result;
        try {
            result = DFService.search(this, template);
            AID tourGuide = result[0].getName();
            SequentialBehaviour sb = new SequentialBehaviour();

            //starting a tour
            sb.addSubBehaviour( new getPrivateTourBehaviour (this, 10000, tourGuide));

            //starting a curator
            sb.addSubBehaviour( new getArtifactsDescription (res));

            addBehaviour( sb );
        } catch (FIPAException ex) {
            Logger.getLogger(ProfilerAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//Setup()

    private class getPrivateTourBehaviour extends TickerBehaviour {
        private final AID tourGuide;

        getPrivateTourBehaviour(ProfilerAgent agent, long period, AID tourGuide) {
            super(agent, period);
            this.tourGuide = tourGuide;
        }

        @Override
        protected void onTick() {
            //sending a request to the tour guide
            ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
            req.addReceiver(tourGuide);
            req.setContent(interest);
            send(req);

            // handling received message from tour guide
            MessageTemplate mt = MessageTemplate.MatchSender(tourGuide) ;
            ACLMessage reply = receive(mt);
            if (reply!=null)  {
                System.out.println("Agent " +getAID().getLocalName()
                        + " received the personal tour.");
                try {
                    res = (ArrayList) reply.getContentObject();
                    /*addBehaviour(new getArtifactsDescription (res));*/
                }
                catch (UnreadableException ex) {}
            }
            block();
        }
    }



    private class getArtifactsDescription extends OneShotBehaviour {
        AID curator;
        ArrayList<Artifact>tour;


        getArtifactsDescription (ArrayList<Artifact> tour) {
            this.tour = tour;
        }

        @Override
        public void action()
        {
            // search for the museum curator
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription tsd = new ServiceDescription();
            tsd.setType("curator");
            template.addServices(tsd);
            DFAgentDescription[] result;
            try {
                result = DFService.search(myAgent, template);
                curator = result[0].getName();
            } catch (FIPAException ex) {
                Logger.getLogger(ProfilerAgent.class.getName()).log(Level.SEVERE, null, ex);
            }

            //sending a request to the curator
            try {
                ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                req.addReceiver(curator);
                req.setContentObject(tour);
                send(req);
            } catch (IOException ex) {}

            //receive detailed message from the curator
            MessageTemplate mt = MessageTemplate.MatchSender(curator) ;
            ACLMessage reply = receive(mt);
            if (reply!=null)  {
                try {
                    ArrayList<Artifact> res = (ArrayList) reply.getContentObject();
                    System.out.print("Artifacts: ");
                    for(Artifact item: res){
                        System.out.print(item.getCreator() + ", ");
                        System.out.print(item.getName() + "; ");
                    }
                    System.out.println();
                    System.out.println();
                }
                catch (UnreadableException ex) {}
            }
            block();
        }
    }

    // Agent termination
    @Override
    protected void takeDown() {
        // Deregister from the yellow pages
        try {DFService.deregister(this);}
        catch (FIPAException fe) {}
        // Printout a dismissal message
        System.out.println("Agent " + getAID().getName() + " terminating.");
    }

}//ProfilerAgent