package edu.washington.cs.cupid.editor;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ILineRange;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.editor.preferences.RulerPreference;

public class RulerUtil {

	@SuppressWarnings("serial")
	private static final TypeToken<Collection<ILineRange>> RANGES_TYPE = new TypeToken<Collection<ILineRange>>() {};

	public static boolean isDocumentCapability(ICapability capability){
		return CapabilityUtil.isGenerator(capability) || 
		       TypeToken.of(IDocument.class).isAssignableFrom(CapabilityUtil.unaryParameter(capability).getType());	
	}
	
	public static boolean isFileCapability(ICapability capability){
		return CapabilityUtil.isGenerator(capability) || 
				TypeToken.of(IFile.class).isAssignableFrom(CapabilityUtil.unaryParameter(capability).getType());	
	}
	
	public static boolean isTextBufferCapability(ICapability capability){
		return CapabilityUtil.isGenerator(capability) || 
			   TypeToken.of(ITextFileBuffer.class).isAssignableFrom(CapabilityUtil.unaryParameter(capability).getType());	
	}
	
	public static boolean isRangeProvider(ICapability capability, ICapability.IOutput<?> output){
		return RANGES_TYPE.isAssignableFrom(output.getType());
	}
	
	public static RulerPreference getPreference(LineProvider provider, List<RulerPreference> preferences){
		return getPreference(provider.getCapability().getName(), provider.getOutput().getName(), preferences);
	}
	
	public static RulerPreference getPreference(String capability, String output, List<RulerPreference> preferences){
		for (RulerPreference p : preferences){
			if (p.capability.equalsIgnoreCase(capability) && p.output.equals(output)){
				return p;
			}
		}
		return null;
	}
	
	public static List<LineProvider> allLineProviders(List<RulerPreference> preferences, boolean skipDisabled){
		List<LineProvider> result = Lists.newArrayList();
		SortedSet<ICapability> capabilities = CupidPlatform.getCapabilityRegistry().getCapabilities();

		for (ICapability capability : capabilities) {
			if (isDocumentCapability(capability) || isFileCapability(capability) || isTextBufferCapability(capability)){
				for (ICapability.IOutput<?> output : capability.getOutputs()){
					if (RulerUtil.isRangeProvider(capability, output)){
						
						RulerPreference pref = getPreference(capability.getName(), output.getName(), preferences);
						
						if (!skipDisabled || pref == null || pref.enabled == true){
							result.add(new LineProvider(capability, output, pref == null ? null : pref.color));
						}
					}
				}
			}
		}
		return result;
	}
	
}
