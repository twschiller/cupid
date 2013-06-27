package edu.washington.cs.cupid.scripting.java;

import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.debug.ui.JDISourceViewer;
import org.eclipse.jdt.internal.debug.ui.contentassist.JavaDebugContentAssistProcessor;
import org.eclipse.jdt.internal.debug.ui.contentassist.TypeContext;
import org.eclipse.jdt.internal.debug.ui.display.DisplayViewerConfiguration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.snippet.SnippetEvalManager;

/**
 * Editor for a code snippet that will be run by the {@link SnippetEvalManager}.
 * A hidden class is constructed in the Cupid project to provide a basis for type
 * completion.
 * @author Todd Schiller
 */
@SuppressWarnings("restriction")
public class SnippetSourceView extends Composite {

	public interface ModifyListener{
		void onModify(String snippet, DiagnosticCollector<JavaFileObject> msgs);
	}
	
	private JDISourceViewer fViewer;
	private JavaDebugContentAssistProcessor fCompletionProcessor;

	private TypeToken<?> inputType;
	private TypeToken<?> outputType;
	private IType context;
	
	private final List<ModifyListener> listeners = Lists.newArrayList();
	
	private static long snippetId = 42L;	
	
	public SnippetSourceView(Composite parent, int style) {
		super(parent, style);
		
		this.setLayout(new GridLayout());
		
		fViewer = new JDISourceViewer(this, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.LEFT_TO_RIGHT);
		fViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final IDocument document = new Document("return " + SnippetEvalManager.VALUE_NAME);
		fViewer.setDocument(document);
		
		fViewer.addTextListener(new ITextListener(){
			@Override
			public void textChanged(TextEvent event) {
				tryCompile();
			}
		});	
	}

	private void tryCompile(){
		if (inputType == null || outputType == null) return;
		
		String txt = fViewer.getDocument().get();
		
		DiagnosticCollector<JavaFileObject> msgs = 
				SnippetEvalManager.getInstance().tryCompile(inputType, outputType, txt);
		
		for (ModifyListener listener : listeners){
			listener.onModify(txt, msgs);
		}
	}
	
	public void setSnippetType(TypeToken<?> inputType, TypeToken<?> outputType){
		Preconditions.checkNotNull(inputType);
		Preconditions.checkNotNull(outputType);
		
		this.inputType = inputType;
		this.outputType = outputType;
		fViewer.unconfigure();	
		
		tryCompile();
	}
	
	public void enableContentAssist() throws Exception{
		this.setupSnippetContext();
		this.setupContentAssist();		
	}
	
	public void addModifyListener(ModifyListener listener){
		listeners.add(listener);
	}
	
	private void setupContentAssist() throws JavaModelException, BadLocationException  {
		String src = context.getCompilationUnit().getSource();
		IDocument doc = new Document(src);
		int offset = doc.getLineOffset(doc.getNumberOfLines() - 3);
		
		fCompletionProcessor = new JavaDebugContentAssistProcessor(new TypeContext(context, offset));
		
		fViewer.configure(new DisplayViewerConfiguration() {
			public IContentAssistProcessor getContentAssistantProcessor() {
				return fCompletionProcessor;
			}
		});
	}

	private void setupSnippetContext() throws CoreException{
		IJavaProject project = CupidScriptingPlugin.getDefault().getCupidJavaProject();
		String snippetName = "Snippet" + (snippetId++);
		
		String outputClass = outputType.getRawType().getName();
		String inputClass = inputType.getRawType().getName();
		
		// Here we specify the source code of the class to be compiled
        StringBuilder src = new StringBuilder();
        src.append("import " + inputType.getRawType().getName() + ";\n");
        src.append("public class " + snippetName + " {\n");
        src.append("    public static " + outputClass + " " + SnippetEvalManager.METHOD_NAME + "(" + inputClass + " " + SnippetEvalManager.VALUE_NAME + ") {\n");
        src.append("         throw new RuntimeException();\n");
        src.append("    }\n");
        src.append("}\n");
		
		context = JavaProjectManager.createSnippetContext(project, snippetName, 
				inputType, outputType, 
				src.toString(), new NullProgressMonitor());
	}
	
	public String getSnippet(){
		return fViewer.getDocument().get();
	}
	
	public void performCleanup() {
		try{
			JavaProjectManager.deleteSnippetContext(context, new NullProgressMonitor());
		}catch(Exception ex){
			CupidScriptingPlugin.getDefault().logError("Error deleting snippet context", ex);
		}finally{
			context = null;	
		}
	}	
}
