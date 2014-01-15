package com.caibowen.gplume.core;

public class Converter {
	
	public static Object to(String var, Class<?> type) {

		if (type == String.class) {
			return var;
		} else if (type == Character.class || type.getName().equals("char")) {
			
			if (var.length() == 1){
				return var.charAt(0);
			} else {
				throw new IllegalArgumentException("cannot cast java.lang.String \"" 
						+ var + "\" to Char: String lenght > 1");
			}
			
		} else if (type == Short.class || type.getName().equals("short")) {
			return Short.parseShort(var);
			
		}  else if (type == Integer.class || type.getName().equals("int")) {
			return Integer.parseInt(var);
			
		}  else if (type == Long.class || type.getName().equals("long")) {
			return Long.parseLong(var);
			
		}  else if (type == Float.class || type.getName().equals("float")) {
			return Float.parseFloat(var);
			
		}  else if (type == Double.class || type.getName().equals("double")) {
			return Double.parseDouble(var);
			
		} else {
			return var;
		}
	}
	
	public static Number to(Number number, Class<?> clazz) {
		
		return number;
	}
	
	private Converter(){}
}
