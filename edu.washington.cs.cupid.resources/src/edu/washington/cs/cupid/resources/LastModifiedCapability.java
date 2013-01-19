package edu.washington.cs.cupid.resources;

import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.CapabilityJob;
import edu.washington.cs.cupid.capability.CapabilityStatus;

public class LastModifiedCapability extends AbstractCapability<IFile, Date>{

	public LastModifiedCapability(){
		super(
				"Last Modified",
				"edu.washington.cs.cupid.resources.lastmodified",
				"The file's last modified date",
				IFile.class, Date.class,
				Flag.PURE, Flag.LOCAL);
	}

	@Override
	public CapabilityJob<IFile, Date> getJob(IFile input) {
		return new CapabilityJob<IFile, Date>(this, input){
			@Override
			protected CapabilityStatus<Date> run(IProgressMonitor monitor) {
				return CapabilityStatus.makeOk(new Date(this.input.getLocalTimeStamp()));
			}
		};
	}
}
