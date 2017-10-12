import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;

public class RetailerAgentV2 extends Agent implements RetailerAgentInterface {
	private static final long serialVersionUID = 1L;
	
	//used for generating random numbers
	private Random _random = new Random();
	
	//energy rate dictates initial request responses when no previous sale records exist
	private double _energyRate;
	
	//key-value records of all accepted energy-cost combos
	private HashMap<Double, Double> _saleRecords = new HashMap<Double, Double>();
	
	// Method for registering service
	void register( ServiceDescription sd)
    {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        dfd.addServices(sd); // An agent can register one or more services

        // Register the agent and its services
        try {  
            DFService.register(this, dfd );  
        }
        catch (FIPAException fe) { fe.printStackTrace(); }
    }
	
	// Method for finding and returning list of agents registered under service description
	DFAgentDescription[] getService( String service )
	{
		DFAgentDescription dfd = new DFAgentDescription();
   		ServiceDescription sd = new ServiceDescription();
   		sd.setType( service );
		dfd.addServices(sd);
		try
		{
			DFAgentDescription[] result = DFService.search(this, dfd);
			return result;
		}
		catch (Exception fe) {}
      	return null;
	}

	
	public void kill() {
		//get all registered client agents
		DFAgentDescription[] clientDescripts = getService("CLIENTS");
		
		//convert descriptions into usable AIDs
		AID[] clientAgents = new AID[clientDescripts.length];
		
		for (int i = 0; i < clientAgents.length; i++) 
			clientAgents[i] = clientDescripts[i].getName();
		
		//build message, adding all clients accordingly
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setOntology("ClosingDown");
		
		//send message to each client, informing them that this retailer is being deleted
		for (AID clientAgent : clientAgents)
			msg.addReceiver(clientAgent);
			
		send(msg);
		
		this.doDelete();
	}

	public void SetEnergyRate(double aEnergyRate) {
		if (aEnergyRate < _energyRate)
			_saleRecords.clear();
		
		_energyRate = aEnergyRate;
	}
	
	//set up behaviours and/or default variable values
	protected void setup() {
		Object[] args = getArguments();
		
	  	if (args != null && args.length > 0)
	  		_energyRate = Double.valueOf(args[0].toString());
	  	else
	  		_energyRate = 1;
	  	
	  	System.out.println("energy rate is " +Double.toString(_energyRate));
		
		//create and set service descriptions, then register to DF
		ServiceDescription sd  = new ServiceDescription();
		sd.setType("ENERGY_SUPPLIER");
		sd.setName(getLocalName());
		register(sd);
		
		ParallelBehaviour pb = new ParallelBehaviour(ParallelBehaviour.WHEN_ANY);
		pb.addSubBehaviour(new RequestResponder(this));
		pb.addSubBehaviour(new CounterResponder(this));
		pb.addSubBehaviour(new ConfirmResponder(this));
		
		
		addBehaviour(pb);
	}
	
    // Method to de-register the service (on take down)
    protected void takeDown() 
    {
       try {
    	   DFService.deregister(this);
    	   }
       catch (Exception e) {}
    }
	
	private class RequestResponder extends SimpleAchieveREResponder {
		private boolean success;
		private double energy;
		private double cost;
		
		public RequestResponder(Agent a) {
			super(a, MessageTemplate.and(
					MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
					MessageTemplate.MatchOntology("Request")));
			
			energy = 0.0;
			cost = 0.0;
			success = false;
			System.out.println("Agent "+getLocalName()+": Request Responder is now live!");
		}
		
		protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
			//registerHandleRequest(new Request());
			System.out.println("Agent "+getLocalName()+": REQUEST received from "+request.getSender().getName()+". Action is "+request.getContent());

			// We agree to perform the action. Note that in the FIPA-Request
			// protocol the AGREE message is optional. Return null if you
			// don't want to send it.
			
			System.out.println("Agent "+getLocalName()+": Message - '" +request.getContent() +"'");
			
			try
			{
				energy = Double.parseDouble(request.getContent());
				
				ACLMessage agree = request.createReply();
				agree.setPerformative(ACLMessage.AGREE);
				
				System.out.println("Agent "+getLocalName()+": Agree");
				//registerHandleRequest(new Request());
				Action();
				return agree;
			}
			catch (NumberFormatException e)
			{
				ACLMessage refuse = request.createReply();
				refuse.setPerformative(ACLMessage.REFUSE);
				
				System.out.println("Agent " +getLocalName() +": Refuse");
				return refuse;
			}
		}
		
		protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
			if (success) {
				System.out.println("Agent "+getLocalName()+": Action successfully performed");
				ACLMessage inform = request.createReply();
				inform.setPerformative(ACLMessage.INFORM);
				inform.setContent(Double.toString(cost));
				success = false;
				return inform;
			}
			else {
				System.out.println("Agent "+getLocalName()+": Action failed");
				success = false;
				throw new FailureException("unexpected-error");
				
			}
		}
		
		public void Action() {
	    	if (_saleRecords.containsKey(energy))
	    		cost = _saleRecords.get(energy);
	    	else
	    	{
	    		cost = (energy * _energyRate) + (_random.nextInt(15) + 1);
	    	}
	    	
	    	success = true;
	    }
	}
		
	private class CounterResponder extends SimpleAchieveREResponder {
		private boolean success;
		private double energy;
		private double oldCost;
		private double cost;
		
		public CounterResponder(Agent a) {
			super(a, MessageTemplate.and(
					MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
					MessageTemplate.MatchOntology("Counter")));
			
			energy = 0.0;
			cost = 0.0;
			success = false;
			System.out.println("Agent "+getLocalName()+": Counter Reresponder is now live!");
		}
		
		protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
			//registerHandleRequest(new Request());
			System.out.println("Agent "+getLocalName()+": REQUEST received from "+request.getSender().getName()+". Action is "+request.getContent());

			// We agree to perform the action. Note that in the FIPA-Request
			// protocol the AGREE message is optional. Return null if you
			// don't want to send it.
			
			
			
			System.out.println("Agent "+getLocalName()+": Message - '" +request.getContent() +"'");
			
			try
			{
				String[] messageSplit = request.getContent().split(",");
				
				if (messageSplit.length < 3)
					throw new NumberFormatException("Not enough arguments");
				
				energy = Double.parseDouble(messageSplit[0]);
				oldCost = Double.parseDouble(messageSplit[1]);
				cost = Double.parseDouble(messageSplit[2]);
				
				ACLMessage agree = request.createReply();
				agree.setPerformative(ACLMessage.AGREE);
				
				System.out.println("Agent "+getLocalName()+": Agree");
				//registerHandleRequest(new Request());
				Action();
				return agree;
			}
			catch (NumberFormatException e)
			{
				ACLMessage refuse = request.createReply();
				refuse.setPerformative(ACLMessage.REFUSE);
				
				System.out.println("Agent " +getLocalName() +": Refuse");
				return refuse;
			}
			
			/*// We refuse to perform the action
			System.out.println("Agent "+getLocalName()+": Refuse");
			ACLMessage refuse = request.createReply();
			refuse.setPerformative(ACLMessage.REFUSE);
			return refuse;*/
		}
		
		protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
			if (success) {
				System.out.println("Agent "+getLocalName()+": Action successfully performed");
				ACLMessage inform = request.createReply();
				inform.setPerformative(ACLMessage.INFORM);
				inform.setContent(Double.toString(cost));
				success = false;
				return inform;
			}
			else {
				System.out.println("Agent "+getLocalName()+": Action failed");
				success = false;
				throw new FailureException("unexpected-error");
				
			}
		}
		
		public void Action() {
	    	
	    	double min = (energy * _energyRate) + 1;
	    	
	    	if (cost <= min)
	    		cost = min;
	    	else
	    		cost = cost - ((oldCost - cost) * _random.nextDouble());
	    	
	    	success = true;
	    }
	}
			
	private class ConfirmResponder extends SimpleAchieveREResponder {
		private boolean success;
		private double energy;
		private double cost;
		
		public ConfirmResponder(Agent a) {
			super(a, MessageTemplate.and(
					MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
					MessageTemplate.MatchOntology("CONFIRM")));
			
			energy = 0.0;
			cost = 0.0;
			success = false;
			System.out.println("Agent "+getLocalName()+": Confirm Reresponder is now live!");
		}
		
		protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
			//registerHandleRequest(new Request());
			System.out.println("Agent "+getLocalName()+": REQUEST received from "+request.getSender().getName()+". Action is "+request.getContent());

			// We agree to perform the action. Note that in the FIPA-Request
			// protocol the AGREE message is optional. Return null if you
			// don't want to send it.
			
			
			
			System.out.println("Agent "+getLocalName()+": Message - '" +request.getContent() +"'");
			
			try
			{
				String[] messageSplit = request.getContent().split(",");
				
				if (messageSplit.length < 2)
					throw new NumberFormatException("Not enough arguments");
				energy = Double.parseDouble(messageSplit[0]);
				cost = Double.parseDouble(messageSplit[1]);
				
				ACLMessage agree = request.createReply();
				agree.setPerformative(ACLMessage.AGREE);
				
				System.out.println("Agent "+getLocalName()+": Agree");
				//registerHandleRequest(new Request());
				Action();
				return agree;
			}
			catch (NumberFormatException e)
			{
				ACLMessage refuse = request.createReply();
				refuse.setPerformative(ACLMessage.REFUSE);
				
				System.out.println("Agent " +getLocalName() +": Refuse");
				return refuse;
			}
			
			/*// We refuse to perform the action
			System.out.println("Agent "+getLocalName()+": Refuse");
			ACLMessage refuse = request.createReply();
			refuse.setPerformative(ACLMessage.REFUSE);
			return refuse;*/
		}

		protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
			
			if (success) {
				System.out.println("Agent "+getLocalName()+": Action successfully performed");
				ACLMessage inform = request.createReply();
				inform.setPerformative(ACLMessage.INFORM);
				success = false;
				return inform;
			}
			else {
				System.out.println("Agent "+getLocalName()+": Action failed");
				success = false;
				throw new FailureException("unexpected-error");
				
			}
		}
		
		public void Action() {
			if (_saleRecords.containsKey(energy))
			{
				if (_saleRecords.get(energy) > cost)
				{
					_saleRecords.remove(energy);
					_saleRecords.put(energy, cost);
				}
			}
			else
				_saleRecords.put(energy, cost);
			
			System.out.println("NEW RECORDED SALE");
			
			success = true;
		}
	}
}
