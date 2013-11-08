package edu.washington.cs.cupid.wizards;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;

import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.capability.CapabilityUtil;
import edu.washington.cs.cupid.capability.ICapability;
import edu.washington.cs.cupid.capability.ICapability.IOutput;

public class PipelineUtil {

	public static TypeToken<?> calculateOutputType(List<ICapability> pipe){
		ICapability last = pipe.get(pipe.size()-1);
		IOutput<?> lastOut = CapabilityUtil.singleOutput(last);
		
		TypeToken<?> tLastIn = CapabilityUtil.isGenerator(last) ?
				TypeToken.of(Void.class) : CapabilityUtil.unaryParameter(last).getType();
		
		Type tLastOut = lastOut.getType().getType();
		
		TypeToken<?> resultType = lastOut.getType();
		
		if (tLastOut instanceof Class){
			return (TypeToken<?>) lastOut;
		}else if (tLastOut instanceof TypeVariable){
			IOutput<?> previous = CapabilityUtil.singleOutput(pipe.get(pipe.size()-2));
			resultType = TypeToken.of(resolveTypeVariable(previous.getType(), tLastIn, (TypeVariable<?>) tLastOut));
		}else if (tLastOut instanceof ParameterizedType){
			
			ParameterizedType tt = (ParameterizedType) tLastOut;
			
			boolean hasVar = false;
			
			for (Type arg : tt.getActualTypeArguments()){
				if (!(arg instanceof Class)){
					hasVar = true;
					break;
				}
			}
			
			if (hasVar){
				IOutput<?> previous = CapabilityUtil.singleOutput(pipe.get(pipe.size()-2));
				resultType = TypeToken.of(resolveTypeVariables(previous.getType(), tLastIn, (ParameterizedType) tLastOut));			
			}
		}else{
			throw new RuntimeException("Unexpected output type for capability " + last.getName());
		}
	
		return resultType;
	}
	

	/**
	 * @return <tt>inToken</tt> with the type variable instantiated.
	 */
	public static Type resolveTypeVariable(TypeToken<?> outToken, TypeToken<?> inToken, TypeVariable<?> v){
		ParameterizedType pIn = (ParameterizedType) inToken.getType();
		
		for (TypeToken<?> s : outToken.getTypes()){
			if (s.getRawType() == inToken.getRawType()){
				ParameterizedType pOut = (ParameterizedType) s.getType();
				
				for (int i = 0; i < pIn.getActualTypeArguments().length; i++){
					if (pIn.getActualTypeArguments()[i].equals(v)){
						return (pOut.getActualTypeArguments()[i]);
					}
				}
				
				throw new RuntimeException("Error locating type variable " + v.getName());
			}
		}
		
		throw new RuntimeException("Output type " + outToken + " not compatible with input type " + inToken);
	}

	/**
	 * Returns the parameterized type with the type parameters resolved.
	 */
	public static Type resolveTypeVariables(TypeToken<?> outToken, TypeToken<?> inToken, final ParameterizedType type){
		ParameterizedType pIn = (ParameterizedType) inToken.getType();
		
		if (type.getActualTypeArguments().length != 1){
			throw new IllegalArgumentException("Multi-parameter generic types output currently not supported");
		}
		
		Type v = type.getActualTypeArguments()[0];
		
		for (TypeToken<?> s : outToken.getTypes()){
			if (s.getRawType() == inToken.getRawType()){
				ParameterizedType pOut = (ParameterizedType) s.getType();
				
				for (int i = 0; i < pIn.getActualTypeArguments().length; i++){
					if (pIn.getActualTypeArguments()[i].equals(v)){
						final Type arg = pOut.getActualTypeArguments()[i];
						
						return new ParameterizedType(){
							@Override
							public Type[] getActualTypeArguments() {
								return new Type[] { arg };
							}

							@Override
							public Type getOwnerType() {
								return type.getOwnerType();
							}

							@Override
							public Type getRawType() {
								return type.getRawType();
							}
						};
					}
				}
				
				throw new RuntimeException("Error matching type variable for " + type.toString());
			}
		}
		
		throw new RuntimeException("Output type " + outToken + " not compatible with input type " + inToken);
	}
	

}
