package edu.washington.cs.cupid.capability.snippet;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.PlatformObject;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

public class SnippetEvalManager {

	private int counter = 0;
	public static final String METHOD_NAME = "run";
	public static final String VALUE_NAME = "val";
	
	private static SnippetEvalManager instance = new SnippetEvalManager();
		
	private HashMap<SnippetCapability<?,?>, Method> snippets = Maps.newHashMap();
	
	private SnippetEvalManager(){
		// NO OP
	}
	
	public static SnippetEvalManager getInstance(){
		return instance;
	}
	
	public <I,V> V run(SnippetCapability<I,V> capability, I input) throws Exception {
		if (!snippets.containsKey(capability)){
			compile(capability);
		}
		
		Method m = snippets.get(capability);
		return (V) m.invoke(null, input);
	}
	

	private class CompilationStatus{
		public DiagnosticCollector<JavaFileObject> msgs;
		public String fullName;
		
		public CompilationStatus(DiagnosticCollector<JavaFileObject> msgs,
				String fullName) {
		
			this.msgs = msgs;
			this.fullName = fullName;
		}
	}
	
	private String findJar(Class<?> clazz){
		ProtectionDomain d = clazz.getProtectionDomain();
		CodeSource s = d.getCodeSource();
		
		if (s == null){
			return null;
		}else{
			URL u = s.getLocation();
			return new File(u.getPath()).getAbsolutePath();
		}	
	}
	
	private Set<Class<?>> getDependentClasses(Class<?> clazz){
		Set<Class<?>> result = Sets.newHashSet();
		
		Class<?> current = clazz;
		
		while (current != null){
			result.add(current);
			for (Class<?> i : current.getInterfaces()){
				result.addAll(getDependentClasses(i));
			}
			current = current.getSuperclass();
		}
		
		return result;
	}
	
	private CompilationStatus compile(JavaCompiler compiler, JavaFileManager fileManager, TypeToken<?> inputType, TypeToken<?> outputType, String snippet){
		int id = counter++;
		
		String simpleName = "SnippetClass" + id;
        String fullName = simpleName;
		
        String outputClass = outputType.getRawType().getName();
        String inputClass = inputType.getRawType().getName();
        
        snippet = snippet.trim();
        snippet = snippet.endsWith(";") ? snippet : (snippet  + ";");
        
        // Here we specify the source code of the class to be compiled
        StringBuilder src = new StringBuilder();
        src.append("import " + inputType.getRawType().getName() + ";\n");
        src.append("public class " + simpleName + " {\n");
        src.append("    public static " + outputClass + " " + METHOD_NAME + "(" + inputClass + " " + VALUE_NAME + ") {\n");
        src.append("       try{\n");
        src.append("        " + snippet + "\n");
        src.append("       }catch(Exception xx__){\n");
        src.append("          throw new RuntimeException(xx__);\n");
        src.append("       }\n");
        src.append("    }\n");
        src.append("}\n");
        
        List<JavaFileObject> jfiles = Lists.newArrayList();
        jfiles.add(new CharSequenceJavaFileObject(fullName, src));

        Set<String> jars = Sets.newHashSet();
        for (Class<?> c : getDependentClasses(inputType.getRawType())){
        	jars.add(findJar(c));
        }
        jars.add(findJar(IResource.class));
        jars.add(findJar(PlatformObject.class));
        
        String cp = Joiner.on(System.getProperty("path.separator")).skipNulls().join(jars);
        
        List<String> optionList = Lists.newArrayList();
        // set compiler's classpath to be same as the runtime's
        optionList.addAll(Arrays.asList("-classpath", cp));
        
        DiagnosticCollector<JavaFileObject> msgs = new DiagnosticCollector<JavaFileObject>();
        compiler.getTask(null, fileManager, msgs, optionList, null, jfiles).call();
         
        return new CompilationStatus(msgs, fullName);
	}
	
	public DiagnosticCollector<JavaFileObject> tryCompile(TypeToken<?> inputType, TypeToken<?> outputType, String snippet){
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		JavaFileManager fileManager = new ClassFileManager(
				SnippetEvalManager.class.getClassLoader(), 
				compiler.getStandardFileManager(null, null, null));
		return compile(compiler, fileManager, inputType, outputType, snippet).msgs;
	}
	
	/**
	 * http://www.javablogging.com/dynamic-in-memory-compilation/
	 * @param capability
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	private void compile(SnippetCapability<?,?> capability) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		// We get an instance of JavaCompiler. Then
        // we create a file manager
        // (our custom implementation of it)
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		
        ClassFileManager fileManager = new
		            ClassFileManager(
		            		SnippetEvalManager.class.getClassLoader(), 
		            		compiler.getStandardFileManager(null, null, null));
        
		CompilationStatus s = compile(compiler, fileManager, capability.getInputType(), capability.getOutputType(), capability.getSnippet());
		
        // Creating an instance of our compiled class 
        Class<?> clazz = fileManager.getClassLoader(null).loadClass(s.fullName);
        
        Method m = clazz.getMethods()[0];
        
        snippets.put(capability, m);
	}
}
