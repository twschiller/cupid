package edu.washington.cs.cupid.wizards.ui;

import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.debug.ui.JDISourceViewer;
import org.eclipse.jdt.internal.debug.ui.contentassist.JavaDebugContentAssistProcessor;
import org.eclipse.jdt.internal.debug.ui.contentassist.TypeContext;
import org.eclipse.jdt.internal.debug.ui.display.DisplayViewerConfiguration;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.linear.ILinearCapability;
import edu.washington.cs.cupid.capability.snippet.SnippetEvalManager;
import edu.washington.cs.cupid.scripting.java.CupidScriptingPlugin;
import edu.washington.cs.cupid.scripting.java.JavaProjectManager;
import edu.washington.cs.cupid.wizards.internal.Activator;

@SuppressWarnings("restriction")
public class SelectCapabilityPage extends WizardPage {

	public interface SelectListener{
		public void onSelect(ILinearCapability<?,?> capability);
	}
	
	private JDISourceViewer fViewer;
	
	private final TypeToken<?> outputType;
	
	private Composite container = null;
	private Class<?> startType;
	private final List<SelectListener> listeners = Lists.newArrayList();
	
	private final IDocument document = new Document();
	private IType context;
	private static long snippetId = 42L;
	
	private JavaDebugContentAssistProcessor fCompletionProcessor;
	
	protected SelectCapabilityPage(Class<?> startType,  TypeToken<?> outputType) {
		super("Select");
		this.setTitle("Define formatting predicate for " + startType.getSimpleName());
		this.setMessage("Define a predicate for the rule");
		this.startType = startType;
		this.outputType = outputType;
	}
	
	public void addSelectListener(SelectListener listener){
		listeners.add(listener);
	}
	
	@Override
	public void createControl(Composite parent) {
		final TypeToken<?> inputType = TypeToken.of(startType);
		
		container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		
		Label lbl = new Label(container, SWT.LEFT);
		lbl.setText("Select capability (Optional):");
		Combo matching = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY); 
		matching.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			
		fViewer = new JDISourceViewer(container, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.LEFT_TO_RIGHT);
		
		GridData d = new GridData(SWT.FILL, SWT.FILL, true, true);
		d.horizontalSpan = 2;
		fViewer.getControl().setLayoutData(d);
		fViewer.setInput(document);
		
		// TODO: configure completion assist
		// fViewer.configure(new SnippetViewerConfiguration());
		
		
		try{
			this.setupSnippetContext();
		
			String src = context.getCompilationUnit().getSource();
			IDocument doc = new Document(src);
			int offset = doc.getLineOffset(doc.getNumberOfLines() - 3);
			
			fCompletionProcessor = new JavaDebugContentAssistProcessor(new TypeContext(context, offset));
			
			fViewer.configure(new DisplayViewerConfiguration() {
				public IContentAssistProcessor getContentAssistantProcessor() {
						return fCompletionProcessor;
				}
			});
		
		}catch (Exception ex){
			Activator.getDefault().logError("Error creating snippet context", ex);
		}
		
		fViewer.addTextListener(new ITextListener(){
			@Override
			public void textChanged(TextEvent event) {
				
				DiagnosticCollector<JavaFileObject> msgs = 
						SnippetEvalManager.getInstance().tryCompile(inputType, outputType, document.get());
				
				if (msgs.getDiagnostics().isEmpty()){
					SelectCapabilityPage.this.setPageComplete(true);
					SelectCapabilityPage.this.setErrorMessage(null);
					SelectCapabilityPage.this.setMessage("The expression is valid", DialogPage.INFORMATION);
				}else{
					SelectCapabilityPage.this.setMessage(null);
					SelectCapabilityPage.this.setErrorMessage(msgs.getDiagnostics().get(0).getMessage(null));	
					SelectCapabilityPage.this.setPageComplete(false);
				}
			}
		});
		
	
		this.document.set("return " + SnippetEvalManager.VALUE_NAME);
		this.setControl(container);
	}
	
	private void setupSnippetContext() throws CoreException{
		IJavaProject project = CupidScriptingPlugin.getDefault().getCupidJavaProject();
		String snippetName = "Snippet" + (snippetId++);
		
		TypeToken<?> inputType = TypeToken.of(startType);
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
				TypeToken.of(startType), outputType, 
				src.toString(), new NullProgressMonitor());
	}
	
	public void performCleanup() {
		try{
			JavaProjectManager.deleteSnippetContext(context, new NullProgressMonitor());
		}catch(Exception ex){
			Activator.getDefault().logError("Error deleting snippet context", ex);
		}finally{
			context = null;	
		}
	}	
}
