package edu.washington.cs.cupid.jdt.types;

import edu.washington.cs.cupid.types.ITypeAdapter;
import edu.washington.cs.cupid.types.ITypeAdapterPublisher;

public class JdtTypeAdapters implements ITypeAdapterPublisher{

	@Override
	public ITypeAdapter<?, ?>[] publish() {
		return new ITypeAdapter<?,?>[] { new JavaProjectAdapter(), new JavaResourceAdapter() };
	}

}
