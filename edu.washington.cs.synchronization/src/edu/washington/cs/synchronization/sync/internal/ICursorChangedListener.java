package edu.washington.cs.synchronization.sync.internal;

import org.eclipse.core.resources.IFile;

public interface ICursorChangedListener{
    void cursorChanged(int offset);
    void editorFileChanged(IFile file);
}
