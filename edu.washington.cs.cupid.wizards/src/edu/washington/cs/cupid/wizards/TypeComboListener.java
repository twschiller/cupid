/*******************************************************************************
 * Copyright (c) 2013 Todd Schiller.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Todd Schiller - initial API, implementation, and documentation
 ******************************************************************************/
package edu.washington.cs.cupid.wizards;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.ui.progress.UIJob;


/**
 * A modify listener that attaches to a {@link Combo} to automatically update list with super types of the currently selected type.
 * @author Todd Schiller
 */
public class TypeComboListener implements ModifyListener {

	private final Combo combo;
	private Job findReferences = null;
	private UIJob updateList = null;
	
	/**
	 * Construct a {@link TypeComboListener}
	 * @param combo the combobox to update
	 */
	public TypeComboListener(Combo combo) {
		this.combo = combo;
	}

	@Override
	public void modifyText(ModifyEvent e) {
		final String current = combo.getText();

		if (findReferences != null){
			findReferences.cancel();
		}
		if (updateList != null){
			updateList.cancel();
		}

		findReferences = new Job("Find Type References"){
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {

					final java.util.List<IType> xs = TypeUtil.fetchTypes(current);

					updateList = new UIJob("Update Supertype List"){
						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {

							if (combo.getListVisible()){
								schedule(100);
							} else {
								if (combo.getItemCount() > 0){
									combo.remove(0, combo.getItemCount()-1);
								}

								for (IType x : xs){
									ITypeHierarchy h = null;
									try {
										h = x.newSupertypeHierarchy(null);
									} catch (JavaModelException e) {
										return Status.OK_STATUS;
									}
									for (IType s : h.getAllSupertypes(x)){
										combo.add(s.getFullyQualifiedName());
									}
								}
							}
							return Status.OK_STATUS;
						}
					};
					
					updateList.schedule();

				} catch (Exception ex){
					return Status.OK_STATUS;
				}

				return Status.OK_STATUS;
			}
		};
		findReferences.schedule();
	}

}
