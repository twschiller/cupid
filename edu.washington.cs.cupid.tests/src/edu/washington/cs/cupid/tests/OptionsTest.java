package edu.washington.cs.cupid.tests;

import java.util.List;
import java.util.Map;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.AbstractCapability;
import edu.washington.cs.cupid.capability.options.ConfigurableCapabilityJob;
import edu.washington.cs.cupid.capability.options.IConfigurableCapability;
import edu.washington.cs.cupid.capability.options.Option;
import edu.washington.cs.cupid.capability.options.OptionManager;
import edu.washington.cs.cupid.capability.options.Options;

public class OptionsTest extends AbstractCapability<Void, List<Object>> implements IConfigurableCapability<Void, List<Object>> {


	public static final OptionManager optionManager;
	static {
		optionManager = new OptionManager(new Option<?>[] {
				new Option<Integer>("Required Integer", Integer.class, Integer.valueOf(0)),
				new Option<Integer>("Optional Integer", Integer.class, Integer.valueOf(0), true),
				new Option<String>("Required String", String.class, "default value"),
				new Option<String>("Optional String", String.class, null, true),
				new Option<Boolean>("Required Boolean", Boolean.class, false),
				new Option<Double>("Required Double", Double.class, Double.valueOf(16.4)),
				new Option<Double>("Optional Double", Double.class, null, true),
		});
	}
	
	public OptionsTest() {
		super("Options Test", "edu.washington.cs.cupid.tests.options",
		"Has options of every type",
		TypeToken.of(Void.class), new TypeToken<List<Object>>(){},
		Flag.PURE, Flag.LOCAL); 
	}

	@Override
	public List<Option<?>> getOptions() {
		return optionManager.getOptions();
	}

	@Override
	public Option<?> getOption(String name) throws IllegalArgumentException {
		return optionManager.getOption(name);
	}

	@Override
	public ConfigurableCapabilityJob<Void, List<Object>> getJob(Void input, Options options) {
		return null;
	}

	@Override
	public ConfigurableCapabilityJob<Void, List<Object>> getJob(Void input) {
		return null;
	}
}
