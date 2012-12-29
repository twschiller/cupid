package edu.washington.cs.synchronization.sync.internal;

import org.eclipse.core.resources.IFile;

public interface ICursorChangedNotifier{
    void addCursorChangedListener(ICursorChangedListener listener);
    void signalCursorChange(int offset);
    void signalEditorFileChanged(IFile file);
}
