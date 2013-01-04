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
import edu.washington.cs.cupid.internal.Utility;
import edu.washington.cs.cupid.types.ITypeAdapter;
import edu.washington.cs.cupid.types.ITypeAdapterRegistry;

/**
 * Manages the Cupid type hierarchy.
 * @author Todd Schiller
 */
public final class TypeManager {
	
	private static final ITypeAdapterRegistry adapterRegistry = new TypeAdapterRegistry();
	
	private static final Set<Primitive> primitiveTypes = Sets.newHashSet(
			Primitive.make(int.class, Integer.class),
			Primitive.make(double.class, Double.class),
			Primitive.make(float.class, Float.class),
			Primitive.make(boolean.class, Boolean.class));
	
	public static ITypeAdapterRegistry getTypeAdapterRegistry(){
		return adapterRegistry;
	}
	
	public static boolean isJavaCompatible(TypeToken<?> lhs, TypeToken<?> rhs){
		return lhs.isAssignableFrom(rhs) || isPrimitiveCompatible(lhs, rhs);
	}
	
	private static boolean isPrimitiveCompatible(TypeToken<?> lhs, TypeToken<?> rhs){
		for (Primitive rule : primitiveTypes){
			if (lhs.getRawType().equals(rule.primitive) && rhs.getRawType().equals(rule.boxed)){
					return true;
			}
		}
		return false;
	}
	
	public static String simpleTypeName(Type type){
		StringBuilder result = new StringBuilder();
		
		if (type instanceof Class){
			result.append(((Class<?>)type).getSimpleName());
		}else if (type instanceof ParameterizedType){
			List<String> params = Lists.newArrayList();
			
			for (Type param : ((ParameterizedType) type).getActualTypeArguments() ){
				params.add(simpleTypeName(param));
			}
			
			result.append(simpleTypeName(((ParameterizedType) type).getRawType()))
				  .append("<").append(Joiner.on(", ").join(params)).append(">");
		}else if (type instanceof GenericArrayType){
			result.append(simpleTypeName(((GenericArrayType) type).getGenericComponentType())).append("[]");
		}else if (type instanceof TypeVariable){
			result.append(((TypeVariable<?>) type).getName());
		}
		
		return result.toString();
	}
	
	public static boolean isCompatible(ICapability<?,?> capability, Object argument){
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
	public static boolean isCompatible(ICapability<?,?> capability, TypeToken<?> argumentType){
		
		if (capability == null){
			throw new NullPointerException("Capability cannot be null");
		}else if (capability.getParameterType() == null){
			throw new NullPointerException("Capability " + capability.getName() + " has null parameter type");
		}
		
		TypeToken<?> parameterType = capability.getParameterType();
		
		if (parameterType.equals(ICapability.UNIT_TOKEN)){
			// capability does not expect any input
			return true;
	
		}else if (isJavaCompatible(capability.getParameterType(), argumentType)){
			// Java's standard typing rules work
			return true;
	
		}else if (parameterType.getType() instanceof ParameterizedType){
			if (parameterType.getRawType().isAssignableFrom(argumentType.getRawType())){
				// check if type is all variables (i.e., fully generic)
				for (Type arg : ((ParameterizedType) parameterType.getType()).getActualTypeArguments()){
					if (!(arg instanceof TypeVariable)){
						return capability.getParameterType().isAssignableFrom(argumentType);
					}
				}
				return true;
			}else{
				return false;
			}
		}else{
			return adapterRegistry.getTypeAdapter(argumentType, capability.getParameterType()) != null;
		}
	}

	/**
	 * @param capability the capability
	 * @param argument the suggested argument
	 * @return <code>argument</code> iff it is compatible with <code>capability</code>; 
	 * a corresponding compatible argument, otherwise
	 * @throws Exception 
	 * @throws IllegalArgument Exception iff a compatible argument cannot be found
	 * @see {@link Utility#isCompatible(ICapability, Object)}
	 * @see {@link Utility#corresponding(Object)}
	 */
	@SuppressWarnings("unchecked")
	public static Object getCompatible(ICapability<?,?> capability, Object argument){
		if (capability.getParameterType().equals(ICapability.UNIT_TOKEN)){
			return argument;
		}else if (isJavaCompatible(capability.getParameterType(), TypeToken.of(argument.getClass()))){
			return argument;
		}else{
			@SuppressWarnings("rawtypes")
			ITypeAdapter adapter = adapterRegistry.getTypeAdapter(
					TypeToken.of(argument.getClass()), 
					capability.getParameterType());
			
			if (adapter == null){
				throw new IllegalArgumentException("Argument is not compatible with capability");
			}else{
				return adapter.adapt(argument);
			}	
		}	
	}

	@SuppressWarnings("rawtypes")
	private static class Primitive{
		Class primitive;
		Class boxed;
		
		public Primitive(Class primitive, Class boxed) {
			super();
			this.primitive = primitive;
			this.boxed = boxed;
		}
		
		public static Primitive make(Class primitive, Class boxed){
			return new Primitive(primitive, boxed);
		}
	}
	
}
