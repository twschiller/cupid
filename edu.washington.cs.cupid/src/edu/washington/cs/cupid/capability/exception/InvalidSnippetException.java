package edu.washington.cs.cupid.capability.exception;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticCollector;

import com.google.common.reflect.TypeToken;

public class InvalidSnippetException extends Exception {

	private TypeToken<?> inputType;
	private TypeToken<?> outputType;
	private String snippet;
	private DiagnosticCollector<?> diagnostics;
	
	private static final long serialVersionUID = 2L; 
	
	public InvalidSnippetException(final TypeToken<?> inputType, final TypeToken<?> outputType, 
			String snippet,
			final DiagnosticCollector<?> diagnostics) {
		this.inputType = inputType;
		this.outputType = outputType;
		this.snippet = snippet;
		this.diagnostics = diagnostics;
	}

	public DiagnosticCollector<?> getDiagnostics() {
		return diagnostics;
	}

	public TypeToken<?> getInputType() {
		return inputType;
	}

	public TypeToken<?> getOutputType() {
		return outputType;
	}

	public String getSnippet() {
		return snippet;
	}

	@Override
	public String getMessage() {
		for (Diagnostic<?> x : this.diagnostics.getDiagnostics()){
			if (x.getKind() == Kind.ERROR){
				return x.getMessage(null);
			}
		}
		return "Unknown compilation error";
	}
	
	
}
