/*******************************************************************************
 * Copyright (c) 2013 Todd Schiller.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Todd Schiller - initial API, implementation, and documentation
 ******************************************************************************/
package edu.washington.cs.cupid;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.Output;
import edu.washington.cs.cupid.capability.ICapability.IOutput;
import edu.washington.cs.cupid.internal.TypeAdapterRegistry;
import edu.washington.cs.cupid.types.ITypeAdapter;
import edu.washington.cs.cupid.types.ITypeAdapterRegistry;

/**
 * Manages the Cupid type hierarchy.
 * @author Todd Schiller
 */
public final class TypeManager {
	
	private static final ITypeAdapterRegistry ADAPTER_REGISTRY = new TypeAdapterRegistry();
	
	private TypeManager() {
		// NO OP
	}
	
	private static final Map<Class<?>, Class<?>> BOXED_TYPES = ImmutableMap.<Class<?>, Class<?>>builder()
			.put(boolean.class, Boolean.class)
			.put(byte.class, Byte.class)
		    .put(char.class, Character.class) 
		    .put(short.class, Short.class)
		    .put(int.class, Integer.class)
		    .put(long.class, Long.class)
		    .put(float.class, Float.class)
		    .put(double.class, Double.class)
		    .put(void.class, Void.class)
		    .put(boolean[].class, Boolean[].class)
			.put(byte[].class, Byte[].class)
		    .put(char[].class, Character[].class) 
		    .put(short[].class, Short[].class)
		    .put(int[].class, Integer[].class)
		    .put(long[].class, Long[].class)
		    .put(float[].class, Float[].class)
		    .put(double[].class, Double[].class)
		    .build();
	
	/**
	 * Returns the Cupid type adapter registry.
	 * @return the Cupid type adapter registry
	 */
	public static ITypeAdapterRegistry getTypeAdapterRegistry() {
		return ADAPTER_REGISTRY;
	}
	
	private static Set<Class<?>> getClassesBfs(Class<?> clazz) {
		// adapted from: http://stackoverflow.com/questions/9797212/finding-the-nearest-common-superclass-or-superinterface-of-a-collection-of-cla
		
		Set<Class<?>> classes = Sets.newLinkedHashSet();
	    Set<Class<?>> nextLevel = Sets.newLinkedHashSet();
	    nextLevel.add(clazz);
	    do {
	        classes.addAll(nextLevel);
	        Set<Class<?>> thisLevel = Sets.newLinkedHashSet(nextLevel);
	        nextLevel.clear();
	        for (Class<?> each : thisLevel) {
	            Class<?> superClass = each.getSuperclass();
	            if (superClass != null && superClass != Object.class) {
	                nextLevel.add(superClass);
	            }
	            for (Class<?> eachInt : each.getInterfaces()) {
	                nextLevel.add(eachInt);
	            }
	        }
	    } while (!nextLevel.isEmpty());
	    return classes;
	}

	/**
	 * Returns the common set of superclasses sorted by distance from the first class?
	 * @param classes
	 * @return
	 */
	public static List<Class<?>> commonSuperClass(Class<?>... classes) {
		// adapted from: http://stackoverflow.com/questions/9797212/finding-the-nearest-common-superclass-or-superinterface-of-a-collection-of-cla
			
	    Set<Class<?>> result = new LinkedHashSet<Class<?>>(getClassesBfs(classes[0]));
	    for (Class<?> clazz : classes){
	    	result.retainAll(getClassesBfs(clazz));
	    }
	    return Lists.newArrayList(result);
	}
	
	private static boolean isPrimitive(Type type){
		return type instanceof Class && ((Class<?>) type).isPrimitive();
	}
		
	/**
	 * Return the associated boxed or boxed array type for <code>type</code>. Returns <code>type</code>, if <code>type</code> is
	 * not a primitive or primitive array type.
	 * @param type the type token
	 * @return the associated boxed or boxed array type
	 */
	public static TypeToken<?> boxType(TypeToken<?> type){
		if (isPrimitive(type.getType())){
			return TypeToken.of(BOXED_TYPES.get((Class<?>) type.getType()));
		} else {
			return type;
		}
	}
	
	public static boolean isBoxedType(TypeToken<?> type){
		return BOXED_TYPES.values().contains(type.getRawType());
	}
	
	/**
	 * Returns <code>true</code> iff <code>lhs</code> can be assigned to <code>rhs</code> according to
	 * Java's standard typing rules. Uses {@link TypeToken#isAssignableFrom(TypeToken)}.
	 * @param lhs the left-hand side 
	 * @param rhs the right-hand side
	 * @return <code>true</code> iff <code>lhs</code> can be assigned to <code>rhs</code> modulo
	 * Java's standard typing.
	 */
	public static boolean isJavaCompatible(final TypeToken<?> lhs, final TypeToken<?> rhs) {
		return lhs.isAssignableFrom(rhs);
	}
	
	public static String simpleTypeName(final TypeToken<?> type) {
		return simpleTypeName(type.getType());
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
	 * for <code>parameter</code>.
	 * 
	 * @param capability the parameter
	 * @param argument the argument
	 * @return <code>true</code> iff argument <code>argument</code> can be supplied as the argument
	 * for <code>capability</code>
	 */
	public static boolean isCompatible(final ICapability.IParameter<?> parameter, final Object argument) {
		return isCompatible(parameter, TypeToken.of(argument.getClass()));
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
	public static boolean isCompatible(final ICapability.IParameter<?> parameter, final TypeToken<?> argumentType) {
		
		TypeToken<?> parameterType = parameter.getType();
		
		if (parameterType.equals(TypeToken.of(Void.class))) {
			// parameter does not expect any input
			return true;
	
		} else if (isJavaCompatible(parameterType, argumentType)) {
			// Java's standard typing rules work
			return true;	
		} else if (parameterType.getType() instanceof ParameterizedType) {
			if (parameterType.getRawType().isAssignableFrom(argumentType.getRawType())) {
				// check if type is all variables (i.e., fully generic)
				for (Type arg : ((ParameterizedType) parameterType.getType()).getActualTypeArguments()) {
					if (!(arg instanceof TypeVariable)) {
						return parameterType.isAssignableFrom(argumentType);
					}
				}
				return true;
			} else {
				return false;
			}
		} else {
			return ADAPTER_REGISTRY.getTypeAdapter(argumentType, parameterType) != null;
		}
	}

	/**
	 * @param capability the capability
	 * @param argument the suggested argument
	 * @return <code>argument</code> iff it is compatible with <code>capability</code>; 
	 * a corresponding compatible argument, otherwise
	 * @see {@link TypeManager#isCompatible(ICapability, Object)}
	 */
	public static Object getCompatible(final ICapability.IParameter<?> parameter, final Object argument) {
		if (parameter.getType().equals(TypeToken.of(Void.class))) {
			return argument;
		} else if (isJavaCompatible(parameter.getType(), TypeToken.of(argument.getClass()))) {
			return argument;
		} else {
			@SuppressWarnings("rawtypes")
			ITypeAdapter adapter = ADAPTER_REGISTRY.getTypeAdapter(
					TypeToken.of(argument.getClass()), 
					parameter.getType());
			
			if (adapter == null) {
				throw new IllegalArgumentException("Argument is not compatible with capability");
			} else {
				return adapter.adapt(argument);
			}	
		}	
	}
	
	public static TypeToken<?> forName(String qualifiedName) throws ClassNotFoundException{
		return TypeToken.of(Class.forName(qualifiedName));
	}
}
