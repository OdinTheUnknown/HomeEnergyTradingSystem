import jade.core.AID;
import java.util.Iterator;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class Sender extends Agent{
	protected void setup() {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent("farewell, Ray Penbar");
		//msg.setContent("confirm,40,86.5,86.5");
		msg.addReceiver(new AID("TestAgent",AID.ISLOCALNAME));
		
		Iterator receivers = msg.getAllIntendedReceiver();
		
			send(msg);
	}
}
