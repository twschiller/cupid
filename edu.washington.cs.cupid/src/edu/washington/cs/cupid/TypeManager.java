package edu.washington.cs.cupid;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.internal.TypeAdapterRegistry;
import edu.washington.cs.cupid.types.ITypeAdapter;
import edu.washington.cs.cupid.types.ITypeAdapterRegistry;

/**
 * Manages the Cupid type hierarchy.
 * @author Todd Schiller
 */
public final class TypeManager {
	
	private static final ITypeAdapterRegistry ADAPTER_REGISTRY = new TypeAdapterRegistry();
	
	private static final Set<Primitive> PRIMITIVE_TYPES = Sets.newHashSet(
			Primitive.make(int.class, Integer.class),
			Primitive.make(double.class, Double.class),
			Primitive.make(float.class, Float.class),
			Primitive.make(boolean.class, Boolean.class));
	
	private TypeManager() {
		
	}
	
	/**
	 * Returns the Cupid type adapter registry.
	 * @return the Cupid type adapter registry
	 */
	public static ITypeAdapterRegistry getTypeAdapterRegistry() {
		return ADAPTER_REGISTRY;
	}
	
	/**
	 * Returns <code>true</code> iff <code>lhs</code> can be assigned to <code>rhs</code> modulo
	 * Java's standard typing rules and boxing.
	 * @param lhs the left-hand side 
	 * @param rhs the right-hand side
	 * @return <code>true</code> iff <code>lhs</code> can be assigned to <code>rhs</code> modulo
	 * Java's standard typing rules and boxing
	 */
	public static boolean isJavaCompatible(final TypeToken<?> lhs, final TypeToken<?> rhs) {
		return lhs.isAssignableFrom(rhs) || isPrimitiveCompatible(lhs, rhs);
	}
	
	private static boolean isPrimitiveCompatible(final TypeToken<?> lhs, final TypeToken<?> rhs) {
		for (Primitive rule : PRIMITIVE_TYPES) {
			if (lhs.getRawType().equals(rule.primitive) && rhs.getRawType().equals(rule.boxed)) {
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns a human-readable name for <code>type</code>.
	 * @param type the type
	 * @return a human-readable name for <code>type</code>.
	 */
	public static String simpleTypeName(final Type type) {
		StringBuilder result = new StringBuilder();
		
		if (type instanceof Class) {
			result.append(((Class<?>) type).getSimpleName());
		} else if (type instanceof ParameterizedType) {
			List<String> params = Lists.newArrayList();
			
			for (Type param : ((ParameterizedType) type).getActualTypeArguments()) {
				params.add(simpleTypeName(param));
			}
			
			result.append(simpleTypeName(((ParameterizedType) type).getRawType()))
				  .append("<").append(Joiner.on(", ").join(params)).append(">");
		} else if (type instanceof GenericArrayType) {
			result.append(simpleTypeName(((GenericArrayType) type).getGenericComponentType())).append("[]");
		} else if (type instanceof TypeVariable) {
			result.append(((TypeVariable<?>) type).getName());
		}
		
		return result.toString();
	}
	
	/**
	 * Returns <code>true</code> iff argument <code>argument</code> can be supplied as the argument
	 * for <code>capability</code>.
	 * 
	 * @param capability the capability
	 * @param argument the argument
	 * @return <code>true</code> iff argument <code>argument</code> can be supplied as the argument
	 * for <code>capability</code>
	 */
	public static boolean isCompatible(final ICapability<?, ?> capability, final Object argument) {
		return isCompatible(capability, TypeToken.of(argument.getClass()));
	}
	
	/**
	 * Returns <code>true</code> iff an argument of type <code>argumentType</code> can be supplied as the argument
	 * for <code>capability</code>.
	 * 
	 * @param capability the capability
	 * @param argumentType the argument type
	 * @return <code>true</code> iff an argument of type <code>argumentType</code> can be supplied as the argument
	 * for <code>capability</code>
	 */
	public static boolean isCompatible(final ICapability<?, ?> capability, final TypeToken<?> argumentType) {
		
		if (capability == null) {
			throw new NullPointerException("Capability cannot be null");
		} else if (capability.getParameterType() == null) {
			throw new NullPointerException("Capability " + capability.getName() + " has null parameter type");
		}
		
		TypeToken<?> parameterType = capability.getParameterType();
		
		if (parameterType.equals(ICapability.UNIT_TOKEN)) {
			// capability does not expect any input
			return true;
	
		} else if (isJavaCompatible(capability.getParameterType(), argumentType)) {
			// Java's standard typing rules work
			return true;
	
		} else if (parameterType.getType() instanceof ParameterizedType) {
			if (parameterType.getRawType().isAssignableFrom(argumentType.getRawType())) {
				// check if type is all variables (i.e., fully generic)
				for (Type arg : ((ParameterizedType) parameterType.getType()).getActualTypeArguments()) {
					if (!(arg instanceof TypeVariable)) {
						return capability.getParameterType().isAssignableFrom(argumentType);
					}
				}
				return true;
			} else {
				return false;
			}
		} else {
			return ADAPTER_REGISTRY.getTypeAdapter(argumentType, capability.getParameterType()) != null;
		}
	}

	/**
	 * @param capability the capability
	 * @param argument the suggested argument
	 * @return <code>argument</code> iff it is compatible with <code>capability</code>; 
	 * a corresponding compatible argument, otherwise
	 * @see {@link Utility#isCompatible(ICapability, Object)}
	 * @see {@link Utility#corresponding(Object)}
	 */
	@SuppressWarnings("unchecked")
	public static Object getCompatible(final ICapability<?, ?> capability, final Object argument) {
		if (capability.getParameterType().equals(ICapability.UNIT_TOKEN)) {
			return argument;
		} else if (isJavaCompatible(capability.getParameterType(), TypeToken.of(argument.getClass()))) {
			return argument;
		} else {
			@SuppressWarnings("rawtypes")
			ITypeAdapter adapter = ADAPTER_REGISTRY.getTypeAdapter(
					TypeToken.of(argument.getClass()), 
					capability.getParameterType());
			
			if (adapter == null) {
				throw new IllegalArgumentException("Argument is not compatible with capability");
			} else {
				return adapter.adapt(argument);
			}	
		}	
	}

	@SuppressWarnings("rawtypes")
	private static final class Primitive {
		private Class primitive;
		private Class boxed;
		
		private Primitive(final Class primitive, final Class boxed) {
			super();
			this.primitive = primitive;
			this.boxed = boxed;
		}
		
		private static Primitive make(final Class primitive, final Class boxed) {
			return new Primitive(primitive, boxed);
		}
	}
	
}
