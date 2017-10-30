import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.TabFolder;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;


public class HomeSystemGui {

	protected Shell shell;
	public Text txtEnergyRetailerA;
	public Text txtCostRetailerA;
	public Text txtOutputRetailerA;
	public Text txtEnergyRetailerB;
	public Text txtCostRetailerB;
	public Text txtOutputRetailerB;
	public Text txtEnergyRetailerC;
	public Text txtCostRetailerC;
	public Text txtOutputRetailerC;
	Process process;
	public Text txtEnergyNeeded;
	private Text txtBuyRangeLow;
	private Text txtBuyRangeHigh;
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			HomeSystemGui window = new HomeSystemGui();
			window.open();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(487, 470);
		shell.setText("Home Energy Trading System");
		shell.setLayout(null);
		
		TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
		tabFolder.setBounds(0, 0, 469, 423);
		
		TabItem tbtmHome = new TabItem(tabFolder, SWT.NONE);
		tbtmHome.setText("Home");
		
		TabItem tbtmRetailerA = new TabItem(tabFolder, SWT.NONE);
		tbtmRetailerA.setText("Retailer A");
		
		Composite composite_1 = new Composite(tabFolder, SWT.NONE);
		tbtmRetailerA.setControl(composite_1);
		
		Label lblEnergyRetailerA = new Label(composite_1, SWT.NONE);
		lblEnergyRetailerA.setBounds(10, 22, 52, 20);
		lblEnergyRetailerA.setText("Energy: ");
		
		Label lblCostRetailerA = new Label(composite_1, SWT.NONE);
		lblCostRetailerA.setBounds(10, 48, 38, 20);
		lblCostRetailerA.setText("Cost:");
		
		Label lblOutputRetailerA = new Label(composite_1, SWT.NONE);
		lblOutputRetailerA.setBounds(10, 91, 70, 20);
		lblOutputRetailerA.setText("Output");
		
		txtEnergyRetailerA = new Text(composite_1, SWT.BORDER);
		txtEnergyRetailerA.setText("");
		txtEnergyRetailerA.setBounds(63, 16, 78, 26);
		
		txtCostRetailerA = new Text(composite_1, SWT.BORDER);
		txtCostRetailerA.setText("");
		txtCostRetailerA.setBounds(48, 48, 78, 26);
		
		txtOutputRetailerA = new Text(composite_1, SWT.BORDER);
		txtOutputRetailerA.setText("");
		txtOutputRetailerA.setBounds(10, 116, 441, 264);
		
		

		TabItem tbtmRetailerB = new TabItem(tabFolder, SWT.NONE);
		tbtmRetailerB.setText("Retailer B");
		
		Composite composite_2 = new Composite(tabFolder, SWT.NONE);
		tbtmRetailerB.setControl(composite_2);
		
		Label lblEnergyRetailerB = new Label(composite_2, SWT.NONE);
		lblEnergyRetailerB.setBounds(10, 22, 52, 20);
		lblEnergyRetailerB.setText("Energy: ");
		
		Label lblCostRetailerB = new Label(composite_2, SWT.NONE);
		lblCostRetailerB.setBounds(10, 48, 38, 20);
		lblCostRetailerB.setText("Cost:");
		
		Label lblOutputRetailerB = new Label(composite_2, SWT.NONE);
		lblOutputRetailerB.setBounds(10, 91, 70, 20);
		lblOutputRetailerB.setText("Output");
		
		txtEnergyRetailerB = new Text(composite_2, SWT.BORDER);
		txtEnergyRetailerB.setText("");
		txtEnergyRetailerB.setBounds(63, 16, 78, 26);
		
		txtCostRetailerB = new Text(composite_2, SWT.BORDER);
		txtCostRetailerB.setText("");
		txtCostRetailerB.setBounds(48, 48, 78, 26);
		
		txtOutputRetailerB = new Text(composite_2, SWT.BORDER);
		txtOutputRetailerB.setText("");
		txtOutputRetailerB.setBounds(10, 116, 441, 264);
		
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("New Item");
		
		Composite composite = new Composite(tabFolder, SWT.NONE);
		tabItem.setControl(composite);
		
		Button btnExit = new Button(composite, SWT.NONE);
		btnExit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				System.exit(0);
			}
		});
		
		btnExit.setBounds(361, 350, 90, 30);
		btnExit.setText("Exit");
		
		Label lblEnergyNeeded = new Label(composite, SWT.NONE);
		lblEnergyNeeded.setBounds(109, 75, 115, 20);
		lblEnergyNeeded.setText("Energy Needed:");
		
		Label lblBuyRangeLow = new Label(composite, SWT.NONE);
		lblBuyRangeLow.setText("Buy Range Low:");
		lblBuyRangeLow.setBounds(109, 145, 115, 20);
		
		Label lblBuyRangeHigh = new Label(composite, SWT.NONE);
		lblBuyRangeHigh.setText("Buy Range High:");
		lblBuyRangeHigh.setBounds(109, 113, 115, 20);
		
		txtEnergyNeeded = new Text(composite, SWT.BORDER);
		txtEnergyNeeded.setText("");
		txtEnergyNeeded.setBounds(226, 72, 108, 26);
		
		txtBuyRangeLow = new Text(composite, SWT.BORDER);
		txtBuyRangeLow.setText("");
		txtBuyRangeLow.setBounds(226, 142, 108, 26);
		
		txtBuyRangeHigh = new Text(composite, SWT.BORDER);
		txtBuyRangeHigh.setText("");
		txtBuyRangeHigh.setBounds(226, 110, 108, 26);
		
		Button btnUpdateClient = new Button(composite, SWT.NONE);
		btnUpdateClient.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				MainController.updateClient(Double.valueOf(txtEnergyNeeded.toString()));
			}
		});
		btnUpdateClient.setBounds(176, 221, 103, 30);
		btnUpdateClient.setText("Update Client");


		TabItem tbtmRetailerC = new TabItem(tabFolder, SWT.NONE);
		tbtmRetailerC.setText("Retailer C");
		
		Composite composite_3 = new Composite(tabFolder, SWT.NONE);
		tbtmRetailerC.setControl(composite_3);
		
		Label lblEnergyRetailerC = new Label(composite_3, SWT.NONE);
		lblEnergyRetailerC.setBounds(10, 22, 52, 20);
		lblEnergyRetailerC.setText("Energy: ");
		
		Label lblCostRetailerC = new Label(composite_3, SWT.NONE);
		lblCostRetailerC.setBounds(10, 48, 38, 20);
		lblCostRetailerC.setText("Cost:");
		
		Label lblOutputRetailerC = new Label(composite_3, SWT.NONE);
		lblOutputRetailerC.setBounds(10, 91, 70, 20);
		lblOutputRetailerC.setText("Output");
		
		txtEnergyRetailerC = new Text(composite_3, SWT.BORDER);
		txtEnergyRetailerC.setText("");
		txtEnergyRetailerC.setBounds(63, 16, 78, 26);
		
		txtCostRetailerC = new Text(composite_3, SWT.BORDER);
		txtCostRetailerC.setText("");
		txtCostRetailerC.setBounds(48, 48, 78, 26);
		
		txtOutputRetailerC = new Text(composite_3, SWT.BORDER);
		txtOutputRetailerC.setText("");
		txtOutputRetailerC.setBounds(10, 116, 441, 264);

		
	}
}
