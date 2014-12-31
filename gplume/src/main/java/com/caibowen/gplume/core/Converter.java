/*******************************************************************************
 * Copyright 2014 Bowen Cai
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.caibowen.gplume.core;

import com.caibowen.gplume.misc.Assert;
import com.caibowen.gplume.misc.Klass;
import com.caibowen.gplume.misc.Str;
import com.caibowen.gplume.misc.Str.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 
 * 
 * 
 * @author BowenCai
 *
 */
public class Converter {
	
	/**
	 * No exception type convert.
	 * 
	 * why not exception? Exception throwing is just too expensive in the web
	 * environment. With hundreds/thousands concurrent request, constant
	 * exception is unacceptable.
	 * 
	 * Instead of throwing exceptions every where, null return value is used to
	 * indicate exception/error.
	 * 
	 * @author BowenCai
	 *
	 */
	public static final class slient {

		@Nullable
		public static Long toLong(String string) {
			try {
				return Converter.toLong(string);
			} catch (Exception e) {
				return null;
			}
		}

		@Nullable
		public static Integer toInteger(String string) {
			Long var = slient.toLong(string);
			return var == null ? null : var.intValue();
		}

		@Nullable
		public static Short toShort(String string) {
			Long var = slient.toLong(string);
			return var == null ? null : var.shortValue();
		}

		@Nullable
		public static Double toDouble(String string) {
			try {
				return Converter.toDouble(string);
			} catch (Exception e) {
				return null;
			}
		}

		@Nullable
		public static Float toFloat(String string) {
			Double var = slient.toDouble(string);
			return var == null ? null : var.floatValue();
		}

		@Nullable
		public static Boolean toBool(String string) {
			try {
				return Converter.toBool(string);
			} catch (Exception e) {
				return null;
			}
		}

		@Nullable
		public static Date toDate(String text) {
			try {
				return Converter.toDate(text);
			} catch (Exception e) {
				return null;
			}
		}

		@Nullable
		public static String toSimpleDate(Date date) {
			try {
				return Converter.toSimpleDate(date);
			} catch (Exception e) {
				return null;
			}
		}
		
		/**
		 *  exception free string cast
		 *  
		 *  literal will be translated to:
		 *  byte Byte Byte[]
		 *  int Integer
		 *  ...
		 *  double Double
		 *  BigDecimal
		 *  BigInteger
		 *  
		 *  Enum, literal 'enum1' -> enumType.Enum1 // caseInSensitive
		 *  
		 *  java.lang.Class by 'Class.Forname'
		 *  java.lang.Object by 'Class.Forname().newInstance'
		 *  
		 *  
		 * @param var
		 * @param type
		 * @return value or null if cast failed
		 */
		@Nullable
		public static<T> Object translateStr(String var, Class<T> type) {

			if (var == null || type == null || type == String.class) {
				return var;
			}
			var = var.trim();
			
			if (type == byte[].class || type == Byte[].class) {
				return var.getBytes();
			 
			} else if (type == Boolean.class || type == boolean.class) {
				return toBool(var);
				
			} else if (type == Character.class || type == char.class) {
				if (var.length() == 1){
					return var.charAt(0);
				} else {
					return null;
				}
				
			} else if (type == char[].class || type == Character[].class) {
				return var.toCharArray();
				
			} else if (type == Short.class || type == short.class) {
				return toShort(var);
				
			}  else if (type == Integer.class || type == int.class) {
				return toInteger(var);
				
			}  else if (type == Long.class || type == long.class) {
				return toLong(var);
				
			}  else if (type == Float.class || type == float.class) {
				return toFloat(var);
				
			}  else if (type == Double.class || type == double.class) {
				return toDouble(var);
				
			} else if (type == Date.class) {
				return toDate(var);
				
			} else if (type == java.math.BigDecimal.class) {
				try {
					return new BigDecimal(var);
				} catch (Exception e) {
					return null;
				}
				
			} else if (type == java.math.BigInteger.class) {
				try {
					return new BigInteger(var);
				} catch (Exception e) {
					return null;
				}
				
			} else if (type.isEnum()) {
				T[] es = type.getEnumConstants();
				for (T t : es) {
					if (t.toString().equalsIgnoreCase(var)) {
						return t;
					}
				}
				return null;
				
			} else if (type == Class.class) {
				try {
					return Class.forName(var);
				} catch (Exception e) {
					return null;
				}
				
			} else if (type == Object.class) {
				Object obj = null;
				try {
					Class<?> klass = Class.forName(var);
					obj = klass.newInstance();
				} catch (Exception e) {
				}
				return obj;
				
			} else {
				return var;
			}
		}
		
	}
//-----------------------------------------------------------------------------

	public static List translateList(List<String> varList,
										Class<?> beanClass,
										String propName) throws Exception {

		Class<?> targetClass = null;
		Set<Field> fieldset = Klass.getEffectiveField(beanClass);
		boolean isArray = false;
		for (Field field : fieldset) {
			if (field.getName().equals(propName)) {
				Class<?> propClass = field.getType();
				if (propClass.isArray()) {
					targetClass = propClass.getComponentType();
					isArray = true;
				} else {
					List<Class<?>> argClasses = TypeTraits.findParamTypes(
							beanClass, propName);
					if (argClasses != null && argClasses.size() == 1) {
						targetClass = argClasses.get(0);
					} else {
						throw new IllegalArgumentException(
								"expect 1 generic type, get[" + argClasses + "]");
					}
				}
			}
		}
		if (targetClass == null) {
			throw new NoSuchMethodException("failed deduce list generic parameter");
		}
		ArrayList<Object> coreVars = new ArrayList<>(varList.size());
		for (String liter : varList) {
			coreVars.add(slient.translateStr(liter, targetClass));
		}
		if (isArray) {
			Object[] objs = (Object[]) Array.newInstance(targetClass, coreVars.size());
			for (int i = 0; i < objs.length; i++) {
				objs[i] = coreVars.get(i);
			}
			return Arrays.asList(objs);
			
		} else {
			return coreVars;
		}
	}
	
	@Nullable
	public static Number castNumber(Number var, Class<?> type) {
		
		if (var == null || type == null) {
			return var;
		}
		
		if (var.getClass() == type) {
			return var;
			
		} else if (type == Short.class || type == short.class) {
			return var.shortValue();
			
		}  else if (type == Integer.class || type == int.class) {
			return var.intValue();
			
		}  else if (type == Long.class || type == long.class) {
			return var.longValue();
			
		}  else if (type == Float.class || type == float.class) {
			return var.floatValue();
			
		}  else if (type == Double.class || type == double.class) {
			return var.doubleValue();
			
		} else {
			return var;
		}
	}
//	public static final String STR_MAX_SHORT = Integer.toString(Short.MAX_VALUE);
//	public static final String STR_MIN_SHORT = Integer.toString(Short.MIN_VALUE);
//	public static final String STR_MAX_INT = Integer.toString(Integer.MAX_VALUE);
//	public static final String STR_MIN_INT = Integer.toString(Integer.MIN_VALUE);
//	public static final String STR_MAX_LONG = Long.toString(Long.MAX_VALUE);
//	public static final String STR_MIN_LONG = Long.toString(Long.MIN_VALUE);

//-----------------------------------------------------------------------------
//			exception free c-style conversion
//-----------------------------------------------------------------------------
	
	@Nonnull
	public static Long toLong(String string) {
		if (string == null) {
			throw new NullPointerException();
		}
		string = string.trim();
		if (Str.Patterns.INTERGER.matcher(string).matches()) {
			return Long.decode(string);
		} else {
			throw new NumberFormatException("cannot parse [" + string + "] to integer");
		}
	}
	
	@Nonnull
	public static Integer toInteger(String string) {
		return toLong(string).intValue();
	}
	
	@Nonnull
	public static Short toShort(String string) {
		return toLong(string).shortValue();
	}

	@Nonnull
	public static Double toDouble(String string) {
		if (string == null) {
			return null;
		}
		string = string.trim();
		if (Str.Patterns.FLOAT_NUMBER.matcher(string).matches()) {
			return Double.valueOf(string);
		} else {
			throw new NumberFormatException("cannot parse [" + string + "] to double");
		}
	}
	
	@Nonnull
	public static Float toFloat(String string) {
		return toDouble(string).floatValue();
	}
	
	@Nonnull
	public static Boolean toBool(String string) {
		if (string == null)
			return null;

		String cs = string.trim().toLowerCase();
		switch (cs) {
			case "true":
				return true;
			case "false":
				return false;
			case "yes":
				return true;
			case "no":
				return false;
			case "1":
				return true;
			case "0":
				return false;
			default:
				throw new IllegalArgumentException("Could not convert[" + string + "] to Boolean");
		}
	}

    public static BigInteger toBigInteger(final String str) {
        if (str == null) {
            return null;
        }
        int pos = 0; // offset within string
        int radix = 10;
        boolean negate = false; // need to negate later?
        if (str.startsWith("-")) {
            negate = true;
            pos = 1;
        }
        if (str.startsWith("0x", pos) || str.startsWith("0x", pos)) { // hex
            radix = 16;
            pos += 2;
        } else if (str.startsWith("#", pos)) { // alternative hex (allowed by Long/Integer)
            radix = 16;
            pos ++;
        } else if (str.startsWith("0", pos) && str.length() > pos + 1) { // octal; so long as there are additional digits
            radix = 8;
            pos ++;
        } // default is to treat as decimal

        final BigInteger value = new BigInteger(str.substring(pos), radix);
        return negate ? value.negate() : value;
    }
    

    /**
     * <p>Turns a string value into a java.lang.Number.</p>
     *
     * <p>If the string starts with {@code 0x} or {@code -0x} (lower or upper case) or {@code #} or {@code -#}, it
     * will be interpreted as a hexadecimal Integer - or Long, if the number of digits after the
     * prefix is more than 8 - or BigInteger if there are more than 16 digits.
     * </p>
     * <p>Then, the value is examined for a type qualifier on the end, i.e. one of
     * <code>'f','F','d','D','l','L'</code>.  If it is found, it starts 
     * trying to Converter.to successively larger types from the type specified
     * until one is found that can represent the value.</p>
     *
     * <p>If a type specifier is not found, it will check for a decimal point
     * and then try successively larger types from <code>Integer</code> to
     * <code>BigInteger</code> and from <code>Float</code> to
    * <code>BigDecimal</code>.</p>
    * 
     * <p>
     * Integral values with a leading {@code 0} will be interpreted as octal; the returned number will
     * be Integer, Long or BigDecimal as appropriate.
     * </p>
     *
     * <p>Returns <code>null</code> if the string is <code>null</code>.</p>
     *
     * <p>This method does not trim the input string, i.e., strings with leading
     * or trailing spaces will generate NumberFormatExceptions.</p>
     *
     * @param str  String containing a number, may be null
     * @return Number created from the string (or null if the input is null)
     * @throws NumberFormatException if the value cannot be converted
     */
    public static Number createNumber(final String str) throws NumberFormatException {
        if (str == null) {
            return null;
        }
        if (!Utils.notBlank(str)) {
            throw new NumberFormatException("A blank string is not a valid number");
        }
        // Need to deal with all possible hex prefixes here
        final String[] hex_prefixes = {"0x", "0X", "-0x", "-0X", "#", "-#"};
        int pfxLen = 0;
        for(final String pfx : hex_prefixes) {
            if (str.startsWith(pfx)) {
                pfxLen += pfx.length();
                break;
            }
        }
        if (pfxLen > 0) { // we have a hex number
            char firstSigDigit = 0; // strip leading zeroes
            for(int i = pfxLen; i < str.length(); i++) {
                firstSigDigit = str.charAt(i);
                if (firstSigDigit == '0') { // count leading zeroes
                    pfxLen++;
                } else {
                    break;
                }
            }
            final int hexDigits = str.length() - pfxLen;
            if (hexDigits > 16 || (hexDigits == 16 && firstSigDigit > '7')) { // too many for Long
                return Converter.toBigInteger(str);
            }
            if (hexDigits > 8 || (hexDigits == 8 && firstSigDigit > '7')) { // too many for an int
                return Converter.toLong(str);
            }
            return Converter.toInteger(str);
        }
        final char lastChar = str.charAt(str.length() - 1);
        String mant;
        String dec;
        String exp;
        final int decPos = str.indexOf('.');
        final int expPos = str.indexOf('e') + str.indexOf('E') + 1; // assumes both not present
        // if both e and E are present, this is caught by the checks on expPos (which prevent IOOBE)
        // and the parsing which will detect if e or E appear in a number due to using the wrong offset

        int numDecimals = 0; // Check required precision (LANG-693)
        if (decPos > -1) { // there is a decimal point

            if (expPos > -1) { // there is an exponent
                if (expPos < decPos || expPos > str.length()) { // prevents double exponent causing IOOBE
                    throw new NumberFormatException(str + " is not a valid number.");
                }
                dec = str.substring(decPos + 1, expPos);
            } else {
                dec = str.substring(decPos + 1);
            }
            mant = str.substring(0, decPos);
            numDecimals = dec.length(); // gets number of digits past the decimal to ensure no loss of precision for floating point numbers.
        } else {
            if (expPos > -1) {
                if (expPos > str.length()) { // prevents double exponent causing IOOBE
                    throw new NumberFormatException(str + " is not a valid number.");
                }
                mant = str.substring(0, expPos);
            } else {
                mant = str;
            }
            dec = null;
        }
        if (!Character.isDigit(lastChar) && lastChar != '.') {
            if (expPos > -1 && expPos < str.length() - 1) {
                exp = str.substring(expPos + 1, str.length() - 1);
            } else {
                exp = null;
            }
            //Requesting a specific type..
            final String numeric = str.substring(0, str.length() - 1);
            final boolean allZeros = isAllZeros(mant) && isAllZeros(exp);
            switch (lastChar) {
                case 'l' :
                case 'L' :
                    if (dec == null
                        && exp == null
                        && (numeric.charAt(0) == '-' 
                        && Str.Utils.isDigits(numeric.substring(1)) 
                        	||  Str.Utils.isDigits(numeric))) {
                        try {
                            return Converter.toLong(numeric);
                        } catch (final NumberFormatException nfe) { // NOPMD
                            // Too big for a long
                        }
                        return Converter.toBigInteger(numeric);

                    }
                    throw new NumberFormatException(str + " is not a valid number.");
                case 'f' :
                case 'F' :
                    try {
                        final Float f = toFloat(numeric);
                        if (!(f.isInfinite() || (f.floatValue() == 0.0F && !allZeros))) {
                            //If it's too big for a float or the float value = 0 and the string
                            //has non-zeros in it, then float does not have the precision we want
                            return f;
                        }

                    } catch (final NumberFormatException nfe) { // NOPMD
                        // ignore the bad number
                    }
                    //$FALL-THROUGH$
                case 'd' :
                case 'D' :
                    try {
                        final Double d = Converter.toDouble(numeric);
                        if (!(d.isInfinite() || (d.floatValue() == 0.0D && !allZeros))) {
                            return d;
                        }
                    } catch (final NumberFormatException nfe) { // NOPMD
                        // ignore the bad number
                    }
                    try {
                        return Converter.toBigDecimal(numeric);
                    } catch (final NumberFormatException e) { // NOPMD
                        // ignore the bad number
                    }
                    //$FALL-THROUGH$
                default :
                    throw new NumberFormatException(str + " is not a valid number.");

            }
        }
        //User doesn't have a preference on the return type, so let's start
        //small and go from there...
        if (expPos > -1 && expPos < str.length() - 1) {
            exp = str.substring(expPos + 1, str.length());
        } else {
            exp = null;
        }
        if (dec == null && exp == null) { // no decimal point and no exponent
            //Must be an Integer, Long, Biginteger
            try {
                return Converter.toInteger(str);
            } catch (final NumberFormatException nfe) { // NOPMD
                // ignore the bad number
            }
            try {
                return Converter.toLong(str);
            } catch (final NumberFormatException nfe) { // NOPMD
                // ignore the bad number
            }
            return Converter.toBigInteger(str);
        }

        //Must be a Float, Double, BigDecimal
        final boolean allZeros = isAllZeros(mant) && isAllZeros(exp);
        try {
            if(numDecimals <= 7){// If number has 7 or fewer digits past the decimal point then make it a float
                final Float f = Converter.toFloat(str);
                if (!(f.isInfinite() || (f.floatValue() == 0.0F && !allZeros))) {
                    return f;
                }
            }
        } catch (final NumberFormatException nfe) { // NOPMD
            // ignore the bad number
        }
        try {
            if(numDecimals <= 16){// If number has between 8 and 16 digits past the decimal point then make it a double
                final Double d = Converter.toDouble(str);
                if (!(d.isInfinite() || (d.doubleValue() == 0.0D && !allZeros))) {
                    return d;
                }
            }
        } catch (final NumberFormatException nfe) { // NOPMD
            // ignore the bad number
        }
        return Converter.toBigDecimal(str);
    }
    
    
    /**
     * <p>Convert a <code>String</code> to a <code>BigDecimal</code>.</p>
     * 
     * <p>Returns <code>null</code> if the string is <code>null</code>.</p>
     *
     * @param str  a <code>String</code> to convert, may be null
     * @return converted <code>BigDecimal</code> (or null if the input is null)
     * @throws NumberFormatException if the value cannot be converted
     */
    public static BigDecimal toBigDecimal(final String str) {
        if (str == null) {
            return null;
        }
        // handle JDK1.3.1 bug where "" throws IndexOutOfBoundsException
        if (!Str.Utils.notBlank(str)) {
            throw new NumberFormatException("A blank string is not a valid number");
        }
        if (str.trim().startsWith("--")) {
            // this is protection for poorness in java.lang.BigDecimal.
            // it accepts this as a legal value, but it does not appear 
            // to be in specification of class. OS X Java parses it to 
            // a wrong value.
            throw new NumberFormatException(str + " is not a valid number.");
        }
        return new BigDecimal(str);
    }
    private static boolean isAllZeros(final String str) {
        if (str == null) {
            return true;
        }
        for (int i = str.length() - 1; i >= 0; i--) {
            if (str.charAt(i) != '0') {
                return false;
            }
        }
        return str.length() > 0;
    }
	/**
	 * cast String to to date(year, month, day), not hour, minutes, or second !
	 * 
	 * <pre>
	 * toDate("2014-2-3") => Mon Feb 03 00:00:00 CST 2014
	 * 
	 * toDate("2014-2") => Sat Feb 01 00:00:00 CST 2014
	 * 
	 * toDate("2014") => Wed Jan 01 00:00:00 CST 2014
	 * 
	 * </pre>
	 * 
	 * @param text
	 * @return null if failed
	 * @throws ParseException 
	 */
	@Nonnull
	public static Date toDate(String text) throws ParseException {
		if (text == null) {
			throw new NullPointerException();
		}
		text = text.trim();
		if (text.length() > 7) {
			if (PTN_FORMAT_DAY.matcher(text).matches()) {
				return FORMAT_DAY.parse(text);
			}
			
		} else if (text.length() > 5) {
			if (PTN_FORMAT_MONTH.matcher(text).matches()) {
				return FORMAT_MONTH.parse(text);
			}
			
		} else if (text.length() > 3) {
			if (PTN_FORMAT_YEAR.matcher(text).matches()) {
				return FORMAT_YEAR.parse(text);
			}
		}
		throw new IllegalArgumentException("cannot cast["  + text + "]to date");
	}
	
	/**
	 * 
	 * @param date
	 * @return yyyy-MM-dd
	 */
	@Nonnull
	public static String toSimpleDate(Date date) {
		if (date == null) {
			throw new NullPointerException();
		}
		return FORMAT_DAY.format(date);
	}
	
	private static final Pattern PTN_FORMAT_DAY;
	private static final Pattern PTN_FORMAT_MONTH;
	private static final Pattern PTN_FORMAT_YEAR;

	private static final SimpleDateFormat FORMAT_YEAR;
	private static final SimpleDateFormat FORMAT_MONTH;
	private static final SimpleDateFormat FORMAT_DAY;
	
	
	private static final HashMap<String, Class<?>> TYPE_TABLE;
	static {
		PTN_FORMAT_DAY = Pattern.compile("((19|20)\\d\\d)-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])");
		PTN_FORMAT_MONTH = Pattern.compile("((19|20)\\d\\d)-(0?[1-9]|1[012])");
		PTN_FORMAT_YEAR = Pattern.compile("((19|20)\\d\\d)");
		
		FORMAT_YEAR = new SimpleDateFormat("yyyy");
		FORMAT_MONTH = new SimpleDateFormat("yyyy-MM");
		FORMAT_DAY = new SimpleDateFormat("yyyy-MM-dd");
		
		
		TYPE_TABLE = new HashMap<String, Class<?>>(128);
		
		TYPE_TABLE.put("byte", byte.class);
		TYPE_TABLE.put("Byte", Byte.class);
		TYPE_TABLE.put("byte[]", byte[].class);
		TYPE_TABLE.put("Byte[]", Byte[].class);
		
		TYPE_TABLE.put("boolean", boolean.class);
		TYPE_TABLE.put("Boolean", Boolean.class);
		TYPE_TABLE.put("boolean[]", boolean[].class);
		TYPE_TABLE.put("Boolean[]", Boolean[].class);

		TYPE_TABLE.put("char", char.class);
		TYPE_TABLE.put("Character", Character.class);
		TYPE_TABLE.put("char[]", char[].class);
		TYPE_TABLE.put("Character[]", Character[].class);
		
		TYPE_TABLE.put("short", short.class);
		TYPE_TABLE.put("Short", Short.class);
		TYPE_TABLE.put("short[]", short[].class);
		TYPE_TABLE.put("Short[]", Short[].class);
		
		TYPE_TABLE.put("int", int.class);
		TYPE_TABLE.put("Integer", Integer.class);
		TYPE_TABLE.put("int[]", int[].class);
		TYPE_TABLE.put("Integer[]", Integer[].class);
		
		TYPE_TABLE.put("long", long.class);
		TYPE_TABLE.put("Long", Long.class);
		TYPE_TABLE.put("long[]", long[].class);
		TYPE_TABLE.put("Long[]", Long[].class);

		TYPE_TABLE.put("float", float.class);
		TYPE_TABLE.put("Float", Float.class);
		TYPE_TABLE.put("float[]", float[].class);
		TYPE_TABLE.put("Float[]", Float[].class);
		
		TYPE_TABLE.put("double", double.class);
		TYPE_TABLE.put("Double", Double.class);
		TYPE_TABLE.put("double[]", double[].class);
		TYPE_TABLE.put("Double[]", Double[].class);

		TYPE_TABLE.put("BigDecimal", BigDecimal.class);
		TYPE_TABLE.put("BigInteger", BigInteger.class);
		TYPE_TABLE.put("BigDecimal[]", BigDecimal[].class);
		TYPE_TABLE.put("BigInteger[]", BigInteger[].class);

		TYPE_TABLE.put("string", String.class);
		TYPE_TABLE.put("String", String.class);
		TYPE_TABLE.put("string[]", String[].class);
		TYPE_TABLE.put("String[]", String[].class);

		TYPE_TABLE.put("enum", Enum.class);
		TYPE_TABLE.put("Enum", Enum.class);
		TYPE_TABLE.put("enum[]", Enum[].class);
		TYPE_TABLE.put("Enum[]", Enum[].class);
		
		TYPE_TABLE.put("date", java.util.Date.class);
		TYPE_TABLE.put("Date", java.util.Date.class);
		TYPE_TABLE.put("date[]", java.util.Date[].class);
		TYPE_TABLE.put("Date[]", java.util.Date[].class);

		TYPE_TABLE.put("Class", Class.class);
		TYPE_TABLE.put("Class[]", Class[].class);
		TYPE_TABLE.put("Type", Type.class);
		TYPE_TABLE.put("Void", Void.class);
		TYPE_TABLE.put("void", void.class);
		TYPE_TABLE.put("Object", Object.class);
		TYPE_TABLE.put("Object[]", Object[].class);

		TYPE_TABLE.put("Throwable", Throwable.class);
		TYPE_TABLE.put("Exception", Exception.class);
		TYPE_TABLE.put("Error", Error.class);
	}
	

	@Nonnull
	public static Class<?> getClass(String typeName) {
		Class<?> v = TYPE_TABLE.get(typeName);
		Assert.notNull(v, "unknow class[" + typeName + "]");
		return v;
	}

	
	private Converter(){}
}
