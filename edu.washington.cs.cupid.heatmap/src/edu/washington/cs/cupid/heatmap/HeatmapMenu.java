package edu.washington.cs.cupid.heatmap;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.CupidPlatform;
import edu.washington.cs.cupid.TypeManager;
import edu.washington.cs.cupid.capability.ICapability;

/**
 * Contibutes heatmap capabilities
 * @author Todd Schiller (tws@cs.washington.edu)
 */
public class HeatmapMenu extends CompoundContributionItem{

	@Override
	protected IContributionItem[] getContributionItems() {
		List<IContributionItem> result = Lists.newArrayList();
		
		for (ICapability<?,?> capability : CupidPlatform.getCapabilityRegistry().getCapabilities()){
			Map<String,String> parameters = Maps.newHashMap();
			parameters.put("cupid.heatmap.pipeline", capability.getUniqueId());
			
			if (TypeManager.isJavaCompatible(TypeToken.of(Number.class), capability.getReturnType())){
				CommandContributionItemParameter ccip = new CommandContributionItemParameter(
						PlatformUI.getWorkbench(), "",
						"cupid.heatmap.toggle",
						CommandContributionItem.STYLE_RADIO);
				
				ccip.label = capability.getName();
				ccip.parameters = Maps.newHashMap(parameters);
				
				CommandContributionItem item = new CommandContributionItem(ccip);
				item.setVisible(true);
				
				result.add(item);
			}
		}
	
		return result.toArray(new IContributionItem[]{});
	}
}
