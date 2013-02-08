package edu.washington.cs.cupid.jobs;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.washington.cs.cupid.internal.CupidActivator;
import edu.washington.cs.cupid.preferences.PreferenceConstants;

public class JobManager implements IPropertyChangeListener {

	private static final int MILLISECONDS_PER_SECOND = 1000;
	
	private static final int REAP_INTERVAL_IN_MILLIS = 1000;
	
	/**
	 * Family -> Last Job Submit Time. Used smooth killing of jobs when clicking around the UI
	 */
	private Map<Object, Long> jobs;

	private Set<Object> cancelled;

	private long reapThresholdInSeconds;
	
	private JobReaper reaper = new JobReaper();
	
	private static String id(Object family){
		return Integer.toHexString(family.hashCode());
	}
	
	public JobManager() {
		jobs = Maps.newHashMap();
		cancelled = Sets.newHashSet();
		
		IPreferenceStore preferences = CupidActivator.getDefault().getPreferenceStore();
		reapThresholdInSeconds = preferences.getInt(PreferenceConstants.P_INSPECTOR_KILL_TIME_SECONDS);
		preferences.addPropertyChangeListener(this);
	}
	
	public synchronized void stop(){
		reaper.cancel();
	}
	
	/**
	 * Registers the job family with the job manager; if the job family is marked for cancellation,
	 * unmarks the job.
	 * @param family the job family
	 */
	public synchronized void register(Object family) {
		if (!jobs.containsKey(family)){
			jobs.put(family, System.currentTimeMillis());
		}
		cancelled.remove(family);
	}
	
	/**
	 * If the job is tracked by the job manager, marks the job family for cancellation, and kicks
	 * off a reaper job.
	 * @param family
	 */
	public synchronized void cancel(Object family) {
		if (jobs.containsKey(family)){
			cancelled.add(family);
			reaper.schedule(reapThresholdInSeconds * MILLISECONDS_PER_SECOND);
		} else {
			CupidActivator.getDefault().logInformation("Job not tracked: " + id(family));
		}
	}
	
	public synchronized void cancel(Set<? extends Object> families) {
		for (Object family : families){
			if (jobs.containsKey(family)){
				cancelled.add(family);
			} else {
				CupidActivator.getDefault().logInformation("Job not tracked: " + id(family));
			}
		}
		reaper.schedule(reapThresholdInSeconds * MILLISECONDS_PER_SECOND);
	}
	
	public synchronized void cancelNow(Object family){
		jobs.remove(family);
		cancelled.remove(family);
		Job.getJobManager().cancel(family);
	}
	
	public synchronized void cancelNow(Collection<Object> families){
		for (Object family : families) {
			cancelNow(family);
		}
	}
	
	private class JobReaper extends Job {
		public JobReaper() {
			super("Reap Cupid Jobs");
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			try {
				synchronized (JobManager.this) {
					monitor.beginTask("Reap Selection Jobs", jobs.size() * 2);
					
					long now = System.currentTimeMillis();
					
					List<Object> old = Lists.newLinkedList();
					for (Object key : cancelled) {
						long last = jobs.get(key);
						if (now - last > (reapThresholdInSeconds * MILLISECONDS_PER_SECOND)) {
							old.add(key);
						}
						monitor.worked(1);
					}
					
					for (Object family : old) {
						Job.getJobManager().cancel(family);
						jobs.remove(family);
						cancelled.remove(family);
					}
					
					if (!cancelled.isEmpty()){
						this.schedule(REAP_INTERVAL_IN_MILLIS);
					}
					
					return Status.OK_STATUS;
				}
			} finally {
				monitor.done();
			}
		}
	}

	@Override
	public final void propertyChange(final PropertyChangeEvent event) {
		if (event.getProperty().equals(PreferenceConstants.P_INSPECTOR_KILL_TIME_SECONDS)) {
			reapThresholdInSeconds = (Integer) event.getNewValue();
		}
	}
}
