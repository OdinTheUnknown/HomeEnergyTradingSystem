import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import java.util.HashMap;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREInitiator;
import jade.proto.SimpleAchieveREResponder;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;

public class MessageAgent extends Agent implements MessageAgentInterface {
	private static final long serialVersionUID = 3L;
	private double _returnCost;
	
	@Override
	public void Kill() {
		this.doDelete();
	}

	@Override
	public double GetCostValue() {
		return _returnCost;
	}
	
	protected void setup() {
		Object[] args = getArguments();
		ACLMessage msg = (ACLMessage)args[0];
		
		registerO2AInterface(MessageAgentInterface.class, this);
		
		_returnCost = 99999999;
		
		addBehaviour(new SimpleAchieveREInitiator(this, msg) {
			protected void handleInform(ACLMessage inform) {
				try {
				_returnCost = Double.valueOf(inform.getContent());
				}
				
				catch (Exception e) {
					_returnCost = -99999999;
				}
			}
			protected void handleRefuse(ACLMessage refuse) {
				_returnCost = -99999999;
			}
			protected void handleFailure(ACLMessage failure) {
				if (failure.getSender().equals(myAgent.getAMS())) {
					// FAILURE notification from the JADE runtime: the receiver
					// does not exist
					_returnCost = -99999999;
				}
				else {
					System.out.println("Agent "+failure.getSender().getName()+" failed to perform the requested action");
				}
			}
		} );
	}

}
