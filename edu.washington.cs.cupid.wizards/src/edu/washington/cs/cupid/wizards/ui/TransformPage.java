package edu.washington.cs.cupid.wizards.ui;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.debug.ui.JDISourceViewer;
import org.eclipse.jdt.internal.debug.ui.contentassist.DynamicTypeContext;
import org.eclipse.jdt.internal.debug.ui.contentassist.IJavaDebugContentAssistContext;
import org.eclipse.jdt.internal.debug.ui.contentassist.JavaDebugContentAssistProcessor;
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
import org.eclipse.swt.widgets.Composite;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.snippet.SnippetEvalManager;

@SuppressWarnings("restriction")
public class TransformPage extends WizardPage {

	private JDISourceViewer fViewer;
	private IContentAssistProcessor fCompletionProcessor;	
	
	private final IDocument document = new Document();
	
	private TypeToken<?> inputType;
	private final TypeToken<?> outputType;
	
	public TransformPage(TypeToken<?> outputType){
		super("Write a transform expression");
		this.outputType = outputType;
	}
	
	public void setInputType(TypeToken<?> inputType){
		this.inputType = inputType;
		
		this.setTitle("Transform " + inputType.getRawType().getSimpleName() + " to " + outputType.getRawType().getSimpleName());
		this.setMessage("Write a Java expression with variable " + SnippetEvalManager.VALUE_NAME);
	
		Class<?> c = outputType.getRawType();
		if (c.isPrimitive()){
			if (c == boolean.class) document.set("return true;"); //$NON-NLS-1$
			else if (c == double.class) document.set("return 0.0d;"); //$NON-NLS-1$
			else if (c == float.class) document.set("return 0.0f;"); //$NON-NLS-1$
			else document.set("return 0;"); //$NON-NLS-1$
		}else{
			if (c == Boolean.class) document.set("return true;"); //$NON-NLS-1$
			else document.set("return null;"); //$NON-NLS-1$
		}
	}
	
	@Override
	public void createControl(Composite parent) {	
		// JavaBreakpointConditionEditor
		// http://git.eclipse.org/c/jdt/eclipse.jdt.debug.git/tree/org.eclipse.jdt.debug.ui/ui/org/eclipse/jdt/debug/ui/breakpoints/JavaBreakpointConditionEditor.java
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		
		fViewer = new JDISourceViewer(container, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.LEFT_TO_RIGHT);
		fViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fViewer.setInput(document);
		
		IJavaDebugContentAssistContext context = new DynamicTypeContext(new DynamicTypeContext.ITypeProvider() {
			@Override
			public IType getType() throws CoreException {
				return null;
			}
		});
		
		fCompletionProcessor = new JavaDebugContentAssistProcessor(context);
		fViewer.configure(new DisplayViewerConfiguration() {
			@Override
			public IContentAssistProcessor getContentAssistantProcessor() {
				return fCompletionProcessor;
			}
		});
		
		fViewer.addTextListener(new ITextListener(){
			@Override
			public void textChanged(TextEvent event) {
				
				DiagnosticCollector<JavaFileObject> msgs = 
						SnippetEvalManager.getInstance().tryCompile(inputType, outputType, document.get());
				
				if (msgs.getDiagnostics().isEmpty()){
					TransformPage.this.setPageComplete(true);
					TransformPage.this.setErrorMessage(null);
					TransformPage.this.setMessage("The expression is valid", DialogPage.INFORMATION);
				}else{
					TransformPage.this.setMessage(null);
					TransformPage.this.setErrorMessage(msgs.getDiagnostics().get(0).getMessage(null));	
					TransformPage.this.setPageComplete(false);
				}
			}
		});
		
		this.setControl(container);
	}
}
