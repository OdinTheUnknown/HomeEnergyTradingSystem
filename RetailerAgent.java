import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.TickerBehaviour;
import java.util.ArrayList;

public class RetailerAgent extends Agent {
	private static final long serialVersionUID = 1L;

	private class Message
	{
		public String sender;
		public String messageType;
		public double energy;
		public double oldCost;
		public double newCost;
		public boolean accept;
		public boolean shutDown;
		
		Message()
		{
			sender = "demo";
			messageType = "request";
			energy = 50;
			oldCost = 0;
			newCost = 50;
			accept = false;
			shutDown = false;
		}
		
	}
	
	private static final String STATE_IDLE = "Idle";
	private static final String STATE_NEGOTIATING = "Negotiating";
	private ArrayList<Message> _messages;
	
	public RetailerAgent() {
		
	}
	
	protected void setup() {
		FSMBehaviour fsm = new FSMBehaviour(this) {
			public int onEnd() {
				myAgent.doDelete();
				return super.onEnd();
			}
		};
		
		fsm.registerFirstState(new Sleeper(), STATE_IDLE);
		fsm.registerState(new Negotiator(), STATE_NEGOTIATING);
		fsm.registerState(new Sleeper(), STATE_IDLE);
		
		fsm.registerTransition(STATE_NEGOTIATING,STATE_IDLE, 0);
		fsm.registerTransition(STATE_NEGOTIATING,STATE_NEGOTIATING, 1);
		fsm.registerTransition(STATE_IDLE,STATE_IDLE, 0);
		fsm.registerTransition(STATE_IDLE,STATE_NEGOTIATING, 1);
		
		addBehaviour(fsm);
		_messages = new ArrayList<Message>();
	}
	
	private class Sleeper extends OneShotBehaviour {
		private int exitValue;
		
		public void action() {
			System.out.println("IDLE!");
			if (_messages.isEmpty())
				exitValue = 0;
			else
				exitValue = 1;
		
		}
		
		public int onEnd() {
			return exitValue;
		}
	}
	
	private class Negotiator extends OneShotBehaviour {
		private int exitValue;
	
		public void action() {
			System.out.println("NOT IDLE!");
			if (_messages.isEmpty())
				exitValue = 0;
			else
				exitValue = 1;
		
		}
		
		public int onEnd() {
			return exitValue;
		}
	}
}
