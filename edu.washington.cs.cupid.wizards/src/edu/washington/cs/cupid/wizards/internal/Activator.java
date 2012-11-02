package edu.washington.cs.cupid.wizards.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.google.common.collect.Lists;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapabilityChangeListener;
import edu.washington.cs.cupid.capability.ICapabilityPublisher;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements ICapabilityPublisher{

	// The plug-in ID
	public static final String PLUGIN_ID = "edu.cs.washington.cs.cupid.wizards"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private HydrationService hydrate = new HydrationService();
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	@Override
	public void addChangeListener(ICapabilityChangeListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeChangeListener(ICapabilityChangeListener listener) {
		// TODO Auto-generated method stub
		
	}
	
	public HydrationService getHydrationService(){
		return hydrate;
	}
	
	@Override
	public ICapability<?, ?>[] publish() {
		
		File dir = CupidPlatform.getPipelineDirectory();
		
		FilenameFilter filter = new FilenameFilter(){
			@Override
			public boolean accept(File dirName, String name) {
				return name.endsWith(".arrow");
			}
		};
		
		List<ICapability<?,?>> result = Lists.newArrayList();
		
		if (dir.isDirectory()){
			
			for (File file : dir.listFiles(filter)){
				try {
					result.add(hydrate.hydrate(file));
				} catch (FileNotFoundException e) {
					throw new RuntimeException("Error locating " + file.getAbsolutePath());
				} catch (IOException e) {
					throw new RuntimeException("Error reading " + file.getAbsolutePath());
				} catch (ClassNotFoundException e) {
					throw new RuntimeException("Error hydrating capability from file " + file.getAbsolutePath(), e);
				}
			}
		}else if (!dir.exists()){
			// NO OP
		}else{
			throw new RuntimeException(dir.getAbsolutePath() + " is not a valid directory");
		}
		
		return result.toArray(new ICapability[]{});
	}

}
