package edu.washington.cs.cupid.mapview.internal;

import org.eclipse.jface.viewers.LabelProvider;

/**
 * Graph label provider that just uses {@link LabelProvider#getText(Object)}.
 * @author Todd Schiller
 */
public class GraphLabelProvider extends LabelProvider {

	@Override
	public final String getText(final Object element) {
		return super.getText(element);
	}
	
}
