package edu.washington.cs.cupid.pipes.views;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.LinearPipeline;

/**
 * User dialog for creating a serial pipeline from two capabilities.
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class InformationPipelineDialog extends Dialog{

	// TODO add description field
	
	// Input
	
	private final Set<ICapability<?,?>> cAvailable;
	
	// Fields
	
	private GridLayout layout;
	private Combo fFirstCapability;
	private Combo fSecondCapability;
	private Text fName;
	private Label fOutMessage;
	
	// Output

	private String pName = "New Pipeline";
	private ICapability<?,?>[] csPipeline;
	
	public InformationPipelineDialog(IShellProvider parentShell, Set<ICapability<?,?>> available) {
		super(parentShell);
		this.cAvailable = available;
	}
	
	@SuppressWarnings("rawtypes")
	public LinearPipeline<?,?> getPipeline(){
		return new LinearPipeline(pName, "No description", csPipeline);	
	}
	
	/**
	 * Add available capabilities to a combo box
	 * @param the combo box
	 */
	private void populateCombo(Combo combo){
		for (ICapability<?,?> capability : cAvailable){
			combo.add(capability.getName());
		}
	}
	
	private ICapability<?,?> find(String capabilityName){
		for (ICapability<?,?> capability : cAvailable){
			if (capability.getName().equals(capabilityName)){
				return capability;
			}
		}
		return null;
	}
	
	private void updateMessage(String message){
		this.fOutMessage.setText(message);
		this.fOutMessage.update();
	}
	
	private boolean isCompatible(ICapability<?,?> capability, TypeToken<?> input){
		TypeToken<?> declared = capability.getParameterType();
		
		if (capability.isLocal()){
			// actual input must be "bigger" than 
			return declared.equals(input);
		}else{
			return declared.equals(input);
		}
	}
	
	private void updateType(){
		csPipeline = null;
		
		List<ICapability<?,?>> pipeline = Lists.newArrayList();
		pipeline.add(find(fFirstCapability.getText()));
		pipeline.add(find(fSecondCapability.getText()));
		
		ICapability<?,?> cFirst = pipeline.get(0);
		
		if (cFirst == null){
			updateMessage("Select capabilities");
			return;
		}else{
			TypeToken<?> result = cFirst.getReturnType();
			for (int i = 1; i < pipeline.size(); i++){
				ICapability<?,?> next = pipeline.get(i);
				
				if (next == null){
					updateMessage("Select capabilities");
					return;
				}else if (!isCompatible(next, result)){
					updateMessage("Inconsistent capability type. Input: " + next.getParameterType().toString() + " Output:" + result.toString() );
					return;
				}else{
					result = next.getReturnType();	
				}
			}
			updateMessage(cFirst.getParameterType().toString() + " -> " + result.toString());
			csPipeline = new ICapability<?,?>[] { find(fFirstCapability.getText()) , find(fSecondCapability.getText())};
		}
	}
	
	protected Control createDialogArea(final Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		
		layout = new GridLayout(2, false);
		composite.setLayout(layout);
		
		new Label(composite, SWT.LEFT | SWT.SINGLE).setText("Pipeline Name:");
		
		fName = new Text(composite, SWT.LEFT);
		fName.setText("New Pipeline");
		fName.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				pName = fName.getText();
			}
		});
		
		new Label(composite, SWT.LEFT).setText("First Capability:");
		fFirstCapability = new Combo(composite, SWT.DROP_DOWN);
		populateCombo(fFirstCapability);
		fFirstCapability.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				updateType();
			}
		});
		
		new Label(composite, SWT.LEFT).setText("Second Capability:");
		fSecondCapability = new Combo(composite, SWT.DROP_DOWN);
		populateCombo(fSecondCapability);
		fSecondCapability.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				updateType();
			}
		});
		
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = SWT.LEFT;
		gridData.widthHint = 600;
		
		fOutMessage = new Label(composite, SWT.LEFT);
		fOutMessage.setText("No capabilities selected");
		fOutMessage.setLayoutData(gridData);
		fOutMessage.setSize(600, 30);
	
		composite.pack();
		return composite;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Create Information Pipeline");
		newShell.setSize(600, 250);
	}
}
