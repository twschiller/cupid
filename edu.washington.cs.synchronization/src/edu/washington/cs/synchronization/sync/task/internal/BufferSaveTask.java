package edu.washington.cs.synchronization.sync.task.internal;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IBuffer;

import edu.washington.cs.util.eclipse.ResourceUtility;

/**
 * A Task implementation represents that a buffer needs to be saved.
 * @author Kivanc Muslu
 */
public class BufferSaveTask implements ISaveTask
{
    /** logger for debugging. */
    private static final Logger logger = Logger.getLogger(BufferSaveTask.class.getName());
    static
    {
        logger.setLevel(Level.INFO);
    }
    /** buffer that will be saved to complete the task. */
    private final IBuffer buffer;

    /**
     * Creates a buffer save task with the given buffer.
     * 
     * @param buffer The buffer that will be saved to complete the task.
     */
    public BufferSaveTask(IBuffer buffer){
        this.buffer = buffer;
    }

    /**
     * Hash code of a buffer save task only depends on the underlying buffer.
     */
    @Override
    public int hashCode(){
        return buffer.getUnderlyingResource().getProjectRelativePath().toString().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof BufferSaveTask){
            return equals((BufferSaveTask) obj);
        }
        else{
            return false;
        }
    }

    /**
     * Compares two buffer save tasks. Returns <code>true</code> if they are equal, <code>false</code> otherwise.
     * 
     * @param other Second buffer save task that will be compared with 'this'.
     * @return <code>true</code> if they are equal, <code>false</code> otherwise.
     */
    public boolean equals(BufferSaveTask other)
    {
        return buffer.equals(other.buffer);
    }

    /**
     * {@inheritDoc} <br>
     * <br>
     * Saves the given buffer to complete the task. <br>
     * If the buffer cannot be saved for some reason (i.e., internal exception), then the same task is returned. <br>
     * Returns <code>null</code> if completed with success.
     * 
     * @return <code>null</code> if completed with success, the same task otherwise.
     */
    public void doTask()
    {
        try
        {
            IResource resource = buffer.getUnderlyingResource();
            // Make sure that the underlying resource is sync with the underlying file system.
            ResourceUtility.syncWithFileSystemIfNecessary(resource);
            buffer.save(null, false);
            logger.fine("Saved buffer for file = " + buffer.getUnderlyingResource().getFullPath());
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, "Cannot save the buffer for file = "
                    + buffer.getUnderlyingResource().getFullPath(), e);
        }
    }
}
