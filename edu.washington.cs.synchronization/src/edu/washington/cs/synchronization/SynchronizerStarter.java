package edu.washington.cs.synchronization;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import edu.washington.cs.synchronization.sync.SynchronizerCursorListener;
import edu.washington.cs.synchronization.sync.SynchronizerFileBufferListener;
import edu.washington.cs.synchronization.sync.SynchronizerResourceChangeListener;

/**
 * {@link IStartup} implementation for Project Synchronizer plug-in. <br>
 * This class includes the code that needs to execute as soon as Eclipse UI (workbench) is created.
 * 
 * @author Kivanc Muslu
 */
public class SynchronizerStarter implements IStartup
{
    /**
     * variable that indicates whether the global listener are added or not. <br>
     * Currently there are 2 global listeners: one {@link IResourceChangeListener} and one {@link IPartListener2}.
     */
    private volatile static boolean globalListenersAdded_ = false;

    /**
     * {@inheritDoc}
     * <p>
     * Creates and installs workspace wide listeners.
     * </p>
     */
    @Override
    public void earlyStartup()
    {
        initGlobalListeners();
    }

    public static void initGlobalListeners()
    {
        if (!globalListenersAdded_)
        {
            Display.getDefault().syncExec(new Thread()
            {
                public void run()
                {
                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    initPostSelectionListener(window);
                }
            });
            initResourceListener();
            initFileBufferListener();
            globalListenersAdded_ = true;
            
            cleanup();
        }
    }
    
    private static void initPostSelectionListener(IWorkbenchWindow window)
    {
        window.getSelectionService().addPostSelectionListener(SynchronizerCursorListener.getInstance());
    }

    private static void cleanup()
    {
        Thread thread = new Thread()
        {
            public void run()
            {
                ProjectSynchronizer.deleteUnusedShadows();
                ProjectSynchronizer.updateShadowWorkingSet();
            }
        };
        thread.start();
    }

    private static void initResourceListener()
    {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(new SynchronizerResourceChangeListener());
    }

    private static void initFileBufferListener()
    {
        IFileBuffer [] buffers = FileBuffers.getTextFileBufferManager().getFileBuffers();
        FileBuffers.getTextFileBufferManager().addFileBufferListener(new SynchronizerFileBufferListener());
        for (IFileBuffer buffer: buffers)
            SynchronizerFileBufferListener.attachListenerToBuffer(buffer);
    }
}
