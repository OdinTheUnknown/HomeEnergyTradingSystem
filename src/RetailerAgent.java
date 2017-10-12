import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;


public class RetailerAgent extends Agent {
	private static final long serialVersionUID = 1L;
	
	private class Message {
		public AID sender;
		public String messageType;
		public double energy;
		public double oldCost;
		public double newCost;
		
		Message()
		{
			sender = new AID();
			messageType = "request";
			energy = 50.0;
			oldCost = 0.0;
			newCost = 50.0;
		}
		
		public Message(AID Sender, String MessageType, double Energy, double OldCost, double NewCost)
		{
			sender = Sender;
			messageType = MessageType;
			energy = Energy;
			oldCost = OldCost;
			newCost = NewCost;
		}
		
	}
	
	private static final String STATE_IDLE = "Idle";
	private static final String STATE_NEGOTIATING = "Negotiating";
	private static final String STATE_MESSAGERECEIVING = "MessageReceiving";
	private static final String STATE_FINISHING = "Finishing";
	
	private int _energyRate;
	
	private ArrayList<Message> _messages;
	private int _messageStorageLimit;
	private boolean _shutDown;
	
	public RetailerAgent() {
		_energyRate = 1;
	}
	
	public RetailerAgent(int energyRate) {
		_energyRate = energyRate;
	}
	
	protected void setup() {
		//FSM - main FSM loop - sleeps or negotiates, ends when shut down flag triggered
		FSMBehaviour fsm = new FSMBehaviour(this) {
			public int onEnd() {
				myAgent.doDelete();
				return super.onEnd();
			}
			
		};
		
		//States registration
		fsm.registerFirstState(new Sleeper(), STATE_IDLE);
		fsm.registerState(new Negotiator(), STATE_NEGOTIATING);
		fsm.registerState(new Sleeper(), STATE_IDLE);
		fsm.registerLastState(new Finisher(), STATE_FINISHING);
		
		//state transitions
		fsm.registerTransition(STATE_NEGOTIATING,STATE_IDLE, 0);
		fsm.registerTransition(STATE_NEGOTIATING,STATE_NEGOTIATING, 1);
		fsm.registerTransition(STATE_IDLE, STATE_FINISHING, 2);
		
		fsm.registerTransition(STATE_IDLE,STATE_IDLE, 0);
		fsm.registerTransition(STATE_IDLE,STATE_NEGOTIATING, 1);
		fsm.registerTransition(STATE_NEGOTIATING, STATE_FINISHING, 2);
		
		FSMBehaviour fsmMessaging = new FSMBehaviour(this) {
			public int onEnd() {
				myAgent.doDelete();
				return super.onEnd();
			}
		};
		
		fsmMessaging.registerFirstState(new MessageReader(), STATE_MESSAGERECEIVING);
		
		fsmMessaging.registerTransition(STATE_MESSAGERECEIVING, STATE_MESSAGERECEIVING, 0);
		
		//add behaviors
		ParallelBehaviour pb = new ParallelBehaviour(ParallelBehaviour.WHEN_ANY);
		pb.addSubBehaviour(fsmMessaging);
		pb.addSubBehaviour(fsm);
		addBehaviour(pb);
		
		//instantiate messages array
		_messages = new ArrayList<Message>();
	}
	
	private class Sleeper extends OneShotBehaviour {
		private int exitValue;
		
		public void action() {
			//System.out.println("IDLE!");
			if (_messages.isEmpty())
				exitValue = 0;
			else
				exitValue = 1;
			
			if (_shutDown)
				exitValue = 2;
		}
		
		public int onEnd() {
			return exitValue;
		}
	}
	
	private class Negotiator extends OneShotBehaviour {
		private int exitValue;
	
		public void action() {
			System.out.println("NOT IDLE!");
			
			switch (_messages.get(0).messageType) {
				case "request": {
					System.out.println(getLocalName() +": I handled a request message!");
					break;
				}
				case "counter": {
					System.out.println(getLocalName() +": I handled a counter-offer message!");
					break;
				}
				case "confirm": {
					System.out.println(getLocalName() +": I handled a confirm!");
					break;
				}
				default: {
					break;
				}
			}
			
			_messages.remove(0);
			
			if (_messages.isEmpty())
				exitValue = 0;
			else
				exitValue = 1;
			
			if (_shutDown)
				exitValue = 2;
		}
		
		public int onEnd() {
			return exitValue;
		}
	}
	
	private class MessageReader extends OneShotBehaviour {
		private int exitValue;
		
		public void action() {
			
			//System.out.println(getLocalName() + ": Waiting for message");
			ACLMessage msg = receive();
			
			if (msg!=null) {
				if (msg.getContent().equals("farewell, Ray Penbar")) {
					_shutDown = true;
				}
				else {
					String[] messageSplit = msg.getContent().split(",");
					System.out.println(messageSplit[1]);
					
					switch (messageSplit[0]) {
						case "request": {
							System.out.println(getLocalName() +": " +msg.getSender() +" has made a request for " +messageSplit[1] +" energy.");
							AID send = msg.getSender();
							String type = messageSplit[0];
							int a = Integer.parseInt(messageSplit[1]);
							Message temp = new Message(send, type, a, 0.0, 0.0);
							_messages.add(temp);
							break;
						}
						case "counter": {
							System.out.println(getLocalName() +": " +msg.getSender() +" has considered my offer of " +messageSplit[1] +" energy for " +messageSplit[2] +"cost, and has counter-offered " +messageSplit[3] +"cost.");
							AID send = msg.getSender();
							String type = messageSplit[0];
							int a = Integer.parseInt(messageSplit[1]);
							double b = Double.parseDouble(messageSplit[2]);
							double c = Double.parseDouble(messageSplit[3]);
							Message temp = new Message(send, type, a, b, c);
							_messages.add(temp);
							break;
						}
						case "confirm": {
							//FOR NOW, VICTORY MESSAGE
							System.out.println(getLocalName() +": Hooray! I just made a sale to " +msg.getSender().toString() +"!\n" +messageSplit[1] +" energy for " +Double.parseDouble(messageSplit[2]) + "cost.");
							
							//ADD A MESSAGE LIKE NORMAL, handle storing info later
							AID send = msg.getSender();
							String type = messageSplit[0];
							int a = Integer.parseInt(messageSplit[1]);
							double b = Double.parseDouble(messageSplit[2]);
							Message temp = new Message(send, type, a, b, b);
							_messages.add(temp);
							
							break;
						}
						default: {
							break;
						}
					}
				}
			}
		}
		
		public int onEnd() {
			return exitValue;
		}
	}
	
	private class Finisher extends OneShotBehaviour {
	
		public void action() {
			System.out.println(getLocalName() +": Shutting down!");
		}
	}
}
