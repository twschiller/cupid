package edu.washington.cs.cupid.wizards.ui;

import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.google.common.collect.Lists;

import edu.washington.cs.cupid.capability.linear.ILinearCapability;
import edu.washington.cs.cupid.wizards.internal.Getter;

public class SelectCapabilityPage extends WizardPage {

	public interface SelectListener{
		public void onSelect(ILinearCapability<?,?> capability);
	}
	
	private Composite container = null;
	private Class<?> startType;
	private final List<SelectListener> listeners = Lists.newArrayList();
	
	protected SelectCapabilityPage(Class<?> startType) {
		super("Select a capability");
		this.setMessage("Select a capability or data accessor for the formatting rule");
		this.setTitle("Select capability");
		this.startType = startType;
	}
	
	public void addSelectListener(SelectListener listener){
		listeners.add(listener);
	}
	
	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		
		Group existing = new Group(container, SWT.BORDER);
		existing.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		existing.setText("Select Capability");
		existing.setLayout(new GridLayout());
		
		Combo matching = new Combo(existing, SWT.DROP_DOWN | SWT.READ_ONLY); 
		matching.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		Group getters = new Group(container, SWT.BORDER);
		getters.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		getters.setText("Select Accessor");
		getters.setLayout(new GridLayout());
				
		final SelectAccessorWidget selectGetter = new SelectAccessorWidget(getters, startType, SWT.NONE);
		selectGetter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		selectGetter.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				try{
					Getter<?,?> g = selectGetter.getGetter();
					for (SelectListener listener : listeners){
						listener.onSelect(g);
					}
				}catch(Exception ex){
					// NO OP?
				}
			}
		});
		
		this.setControl(container);
	}
}
