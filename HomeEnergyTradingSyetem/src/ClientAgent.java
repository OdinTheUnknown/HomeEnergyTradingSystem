import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import java.util.HashMap;

import jade.wrapper.*;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;

public class ClientAgent extends Agent implements ClientAgentInterface {
	private static final long serialVersionUID = 2L;
	
	//flag representing whether the client is actively looking for a provider or not
	private boolean _satisfied;
	
	//contains details on last deal made with a client (if any)
	private AID _provider;
	private double _acceptedCost;
	private double _acceptedEnergy;
	
	//the energy the client currently needs from its providers
	private double _energy;
	
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
	
	protected void takeDown() 
    {
       try { DFService.deregister(this); }
       catch (Exception e) {}
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
	
	public void Kill() {
		takeDown();
		this.doDelete();
	}

	public void TriggerDissatisfied() {
		_satisfied = false;
		_provider = null;
	}
	
	public void SetEnergyRequirement(double aEnergy) {
		_energy = aEnergy;
		TriggerDissatisfied();
	}
	
	protected void setup() {
		registerO2AInterface(ClientAgentInterface.class, this);
		Object[] args = getArguments();
		
	  	if (args != null && args.length > 0)
	  		_energy = Double.valueOf(args[0].toString());
	  	else
	  		_energy = 30;
	  	
	  	_satisfied = false;
	  	_acceptedCost = 0.0;
	  	_provider = null;
		
		//create and set service descriptions, then register to DF
		ServiceDescription sd  = new ServiceDescription();
		sd.setType("CLIENT");
		sd.setName(getLocalName());
		register(sd);
		
		ParallelBehaviour pb = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);
		pb.addSubBehaviour(new EnergyDealHandler());
		
		addBehaviour(pb);
	}
	
	private class EnergyDealHandler extends Behaviour {
		
		public void action() {
			if (!_satisfied)
			{
				//stores cost/provider of best deal (so far)
				AID bestDealProvider = null;
				double bestCostOffered = 0.0;
				
				//if there has been a previous deal, stores deal ratio (energy:cost)
				double previousDealRatio = 0.0;
				boolean previouslyHadDeal = false;
				
				//flag for if an acceptable deal is found (leading to confirmation with provider)
				boolean foundAcceptableDeal = false;
				//flag for if a counter-offer should be made
				boolean makeCounter = false;
				//first counter-offer value to be sent (if 0.0, a request will be sent instead)
				double initialCounterCost = 0.0;
				
				//flag marking new offer was made during full deal loop
				boolean dealOffered = false;
				
				if (_acceptedCost > 0.0)
				{
					previousDealRatio = _acceptedCost / _acceptedEnergy;
					previouslyHadDeal = true;
					initialCounterCost = Math.ceil(_energy * previousDealRatio) - 2;
				}
				
				//initial old counter-offer value to be sent (if 0.0, a request will be sent instead)
				double oldCounterCost = initialCounterCost;
				//next counter-cost value to be sent
				double nextCounterCost = initialCounterCost - 2;
				
				//keep looping until a deal is found (full deal loop)
				while (!foundAcceptableDeal)
				{
					dealOffered = false;
					makeCounter = false;
					MessageAgent a;
					try {
				    	 //get all registered client agents
				   		DFAgentDescription[] providerDescripts = getService("ENERGY_SUPPLIER");
				   		
				   		if (providerDescripts.length > 0)
				   		{
					   		//convert descriptions into usable AIDs
					   		AID[] providerAgents = new AID[providerDescripts.length];
					   		
					   		for (int i = 0; i < providerAgents.length; i++) 
					   			providerAgents[i] = providerDescripts[i].getName();
					   		
					   		for (AID providerAgent : providerAgents)
					   		{
						   		//build message, adding current client accordingly
						   		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
						   		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
								// We want to receive a reply in 6 secs
								msg.setReplyByDate(new Date(System.currentTimeMillis() + 6000));
								
								if (oldCounterCost > 0.0) {
									msg.setOntology("Counter");
									msg.setContent(Double.toString(_energy) +"," +Double.toString(oldCounterCost) +"," +Double.toString(nextCounterCost));
								}
								else {
									msg.setOntology("Request");
									msg.setContent(Double.toString(_energy));
								}
								
								//add receiving provider
						   		msg.addReceiver(providerAgent);
						   			
						   		try
						   		{
						   			ContainerController cc = getContainerController();
						   			Object[] args = new Object[1];
						   			args[0] = msg;
						   			AgentController ac = cc.createNewAgent(getLocalName() +"-Msgr", MessageAgent.class.getName(), args);
						   			ac.start();
						   			Thread.sleep(1000);
						   			
						   			MessageAgentInterface o2a = ac.getO2AInterface(MessageAgentInterface.class);
	
						   			boolean response = false;
						   			boolean gotCost = false;
						   			int count = 0;
						   			double cost = 0.0;
						   			
						   			while (!response && count < 6)
						   			{
						   				Thread.sleep(1000);
						   				cost = o2a.GetCostValue();
						   				
						   				if (cost == -99999999)
						   					response = true;
						   				else if (cost != 99999999) {
						   					response = true;
						   					gotCost = true;
						   				}
						   			}
						   			
						   			o2a.Kill();
						   			Thread.sleep(100);
						   			
						   			if (gotCost)
						   			{
						   				if (bestDealProvider == null || cost < bestCostOffered) {
						   					bestCostOffered = cost;
						   					bestDealProvider = providerAgent;
						   					dealOffered = true;
						   				}
						   			}
						   		}
						   		catch (Exception e) {
						   			System.out.println("Agent " +getLocalName() +": Failed to contact provider " +providerAgent +": " +e);
						   		}
					   		}
				   		
					   		//if the deal is acceptable (was a request, ratio is within reasonable range or as good as low as it will get [same lowest offer as last time] flag to exit loop, otherwise repeat)
					   		double dealRatio = bestCostOffered / _energy;
					   		
					   		if ((dealRatio - previousDealRatio) > 0.3 && previouslyHadDeal)
					   			makeCounter = true;
					   		
					   		if (!makeCounter || !dealOffered)
					   			foundAcceptableDeal = true;
					   		else {
					   			oldCounterCost = bestCostOffered;
					   			nextCounterCost = oldCounterCost - 6;
					   		}
				   		}
			       }
			       
			       catch (Exception e) {
			    	   System.out.println("Agent " +getLocalName() +": Failed to obtain energy deal: " +e);
			       }
				}
				
				//once we reach here, the client has found a deal that it is satisfied with. It sends a confirm message and saves a record for itself (doesn't care about the response from the retailer)
				_satisfied = true;
				
				_provider = bestDealProvider;
				_acceptedCost = bestCostOffered;
				_acceptedEnergy = _energy;
				
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		   		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
				// We want to receive a reply in 2 secs
				msg.setReplyByDate(new Date(System.currentTimeMillis() + 2000));
				msg.addReceiver(bestDealProvider);
				msg.setContent(_energy +"," +bestCostOffered);
				msg.setOntology("Confirm");
				
				try
		   		{
		   			ContainerController cc = getContainerController();
		   			
		   			Object[] args = new Object[1];
		   			args[0] = msg;
		   			
		   			AgentController ac = cc.createNewAgent(getLocalName() +"-Msgr", MessageAgent.class.getName(), args);
		   			ac.start();
		   			Thread.sleep(1000);
		   			
		   			MessageAgentInterface o2a = ac.getO2AInterface(MessageAgentInterface.class);
		   			Thread.sleep(2000);
		   			
		   			o2a.Kill();
		   			
		   			System.out.println("Agent " +getLocalName() +": I have agreed to a deal with " +bestDealProvider +": " +_energy +" energy for " +_acceptedCost +" cost."); //!~~
		   		}
				catch (Exception e) {	System.out.println("Agent " +getLocalName() +": Something went wrong while confirming the deal: " +e);}
			}
			else
			{
				try {
					Thread.sleep(20000);
					System.out.println("Agent " +getLocalName() +": I want a new deal...");
					TriggerDissatisfied();
				}
				catch (InterruptedException e) {	}
			}
		}

		public boolean done() {
			return false;
		}
	}
	
	private class ProviderClosingChecker extends Behaviour {
		private int exitValue;
		
		public void action() {
			
			//System.out.println(getLocalName() + ": Waiting for message");
			ACLMessage msg = receive();
			
			if (msg != null) {
				if (msg.getOntology() == "ClosingDown")
					if (_provider == msg.getSender())
					{
						TriggerDissatisfied();
						_provider = null;
					}
			}
		}

		public boolean done() {
			return false;
		}
	}
}