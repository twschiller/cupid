package edu.washington.cs.cupid.mapview.internal;

import org.eclipse.jface.viewers.LabelProvider;

/**
 * Graph label provider that just uses {@link LabelProvider#getText(Object)}.
 * @author Todd Schiller
 */
public class GraphLabelProvider extends LabelProvider{

	@Override
	public String getText(Object element) {
		return super.getText(element);
	}
	
}
