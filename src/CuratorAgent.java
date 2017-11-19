import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.states.MsgReceiver;

import java.io.IOException;
import java.util.ArrayList;


public class CuratorAgent extends Agent{

    private Artifact[] museum;

    protected void setup(){
        System.out.println("Agent " + getAID().getLocalName() + " is ready.");
        updateArtifact();

        /*register agent with the DF*/
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType("curator");
        sd1.setName(getLocalName());
        dfd.addServices(sd1);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException ex) {}


        /*Behaviour for handling incoming search request for artifact*/
        addBehaviour(new ArtifactRequestReceiverServer());


    }

    /*  Cyclic behaviour to for handling incoming search request for artifact to serve messages from buyer agents.
        Such behaviours must be continuously running (cyclic behaviours) and, at each execution of their action() method,
        must check if a message has been received and process it. Every behaviour has a member variable called myAgent
        that points to the agent that is executing the behaviour
    * */
    private class ArtifactRequestReceiverServer extends CyclicBehaviour{
        Agent a;
        MessageTemplate m;
        private void ArtifactRequestReceiverServer(Agent a,MessageTemplate msgtemplate ){
             msgtemplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
             this.a = a;
             this.m = msgtemplate;
        }


        public void action(){
            checkNewArtifactReq(a, m);
            block();
        }

        private void checkNewArtifactReq(Agent a, MessageTemplate m){
            addBehaviour(new ReceiveMessages(a, m));
        }

    }

    private class ReceiveMessages extends MsgReceiver{

        private ReceiveMessages(Agent a, MessageTemplate m){
            super(a, m, Long.MAX_VALUE,null,null);
        }

        @Override
        protected void handleMessage(ACLMessage msg) {

            System.out.println(getAID().getLocalName() + "has receive request from tour guide");
            if (msg.getContent() != null && msg.getContent().equals("museum-catalog")) {
                //All catalogue request message received, process it
                try {
                    //Message from tour agent
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContentObject(museum);
                    send(reply);
                    System.out.println(getAID().getLocalName() +"has sent message back to Tour Guide");

                    }
                catch (IOException ex) { }

            } else if(msg.getContent() != null) {
                try {
                    //Message from profiler
                    ArrayList<Integer> items = (ArrayList) msg.getContentObject();
                    ArrayList<Artifact> res = new ArrayList();
                    items.stream().forEach((item) -> {
                        for (Artifact museum1 : museum) { //accesses the Artifact references in museum and assigns them to museum1
                            if (museum1.getId() == item) {
                                res.add(museum1);
                            }
                            //ArtifactManager am = (ArtifactManager) museum1.getId(item);
                        }
                    });
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContentObject(res);
                    send(reply);
                    }
                catch (UnreadableException | IOException ex) { }

            } else{
                // The requested artifact is NOT available in the museum.
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.REFUSE);
                String name = "The artifact requested is NOT available!!!";
                reply.setContent(name);
                send(reply);

            }
        }
    }


       /*agent clean up*/


/*    This method stored hard corded use info when the user inserts a new */
    public void updateArtifact(){

        museum = new Artifact[3];
        museum [0]= new Artifact(67, "Mona Lisa", "Leonardo da Vinci", "Painting");
        museum [1] = new Artifact(12, "The Scream", "Edvard Munch", "Painting");
        museum [2] = new Artifact(75, "David", "Michelangelo", "Sculpture");
    }

    // Agent termination
    @Override protected void takeDown() {
        // Deregister from the yellow pages
        try {DFService.deregister(this);}
        catch (FIPAException fe) {}
        // Printout a dismissal message
        System.out.println("Agent " + getAID().getName() + " terminating.");
    }

}

