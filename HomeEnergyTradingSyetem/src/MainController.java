import jade.wrapper.*;
import jade.core.Profile;
import jade.core.ProfileImpl;


public class MainController {
	private static HomeSystemGui ui;
	private static ContainerController mainCtrl = getContainerController();
	
	public static void main(String[] args)
	{
		ui.main(args);
		setupAgents();
		
		
	}
	
	private static void setupAgents()
	{
		AgentController ac = mainCtrl.createNewAgent("RetailerAgentA", RetailerAgent.class, args);
		ac.start();
		Thread.sleep(1000);
	}
	
	private static void setupUi()
	{
		  
	}
	
	public static void updateClient(double val)
	{
		client.SetEnergyRequirement(val);
	}
	
	public static void updateRetailers()
	{
		
	}
	
	public static void updateUi()
	{
		
	}
	//What this needs to do
	//start retailer agents
	//start home agent
	//get starting info from retailer and home agents for ui
	//begin negotiation
	//show agent changes 
}
