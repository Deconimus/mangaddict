package visionCore.math;

import java.text.Normalizer;
import java.util.function.Predicate;

public class FastMath {
	
	public static final float PI = (float)(Math.PI), PI2 = (float)(Math.PI * 2.0), PI4 = (float)(Math.PI * 4.0), TAU = PI2, SQRT2 = (float)(Math.sqrt(2.0)),
								CAST_FLOAT = 0.9999999f;
	
	private static float radToOneX(float radians) {
		
		float x = radians / (PI * 0.5f);
		
		if (x < 0f) { x -= 2f * x + 2f; }
		if (x > 4f) { x -= 4f * (int)(x / 4f); }
		
		if (x > 1f && x <= 3f) {
			x = -(x - 2f);
		} else if (x > 3f && x <= 4f) {
			x -= 4f;
		}
		
		return x;
	}
	
	public static float sin(float radians) {
		
		float x = radToOneX(radians);
		
		float x2 = x * x;
		return ((((0.00015148419f * x2 - 0.00467376557f) * x2 + 0.07968967928f) * x2
				- 0.64596371106f) * x2 + 1.57079631847f) * x;
	}
	
	public static double sin(double radians) {
		double x = radians / (Math.PI * 0.5);
		
		if (x < 0.0) { x -= 2.0 * x + 2.0; }
		if (x > 4.0) { x -= 4.0 * (int)(x / 4.0); }
		
		if (x >= 1.0 && x <= 3.0) {
			x = -(x - 2.0);
		} else if (x >= 3.0 && x <= 5.0) {
			x -= 4.0;
		}
		
		double x2 = x * x;
		return ((((0.00015148419 * x2 - 0.00467376557) * x2 + 0.07968967928) * x2
				- 0.64596371106) * x2 + 1.57079631847) * x;
	}
	
	public static double cos(double radians) {
		return sin(radians + (PI * 0.5));
	}
	
	public static float cos(float radians) {
		return sin(radians + (PI * 0.5f));
	}

	public static double tan(double rad) {
		return sin(rad) / cos(rad);
	}
	
	public static float tan(float rad) {
		return sin(rad) / cos(rad);
	}
	
	public static boolean inRange(double value, double min, double max) {
		return value >= min && value <= max;
	}
	
	public static boolean inRange(float value, float min, float max) {
		return value >= min && value <= max;
	}
	
	public static boolean inRange(int value, int min, int max) {
		return value >= min && value <= max;
	}

	/** Checks if min and max are correct then clamps. */
	public static double clampToRangeC(double value, double min, double max) {
	
		return clampToRange(value, min(min, max), max(min, max));
	}
	
	public static double clampToRange(double value, double min, double max) {
		
		if (value < min) {
			value = min;
		} else if (value > max) {
			value = max;
		}
		
		return value;
		
	}

	/** Checks if min and max are correct then clamps. */
	public static float clampToRangeC(float value, float min, float max) {
	
		return clampToRange(value, min(min, max), max(min, max));
	}
	
	public static float clampToRange(float value, float min, float max) {
		
		if (value < min) {
			value = min;
		} else if (value > max) {
			value = max;
		}
		
		return value;
		
	}
	
	/** Checks if min and max are correct then clamps. */
	public static int clampToRangeC(int value, int min, int max) {
	
		return clampToRange(value, min(min, max), max(min, max));
	}
	
	public static int clampToRange(int value, int min, int max) {
		
		if (value < min) {
			value = min;
		} else if (value > max) {
			value = max;
		}
		
		return value;
		
	}
	
	/**
	 * The old byte-hacking fast inverse-squareroot.
	 * Unfortunately nowadays it's actually slower than the standart one.
	 */
	public static float invSqrt(float x) {
	    
		float xhalf = 0.5f * x;
	    int i = Float.floatToIntBits(x);	// evil floating point bit level hacking
	    i = 0x5f3759df - (i >> 1);			// what the fuck?
	    x = Float.intBitsToFloat(i);
	    x = x * (1.5f - xhalf * x * x);
	    
	    return x;
	}
	
	public static int ceilInt(double a) {
		
		return (int)(Math.ceil(a));
	}
	
	public static int ceilInt(float a) {
		
		return (int)(Math.ceil(a));
	}
	
	public static int floorInt(double a) {
		
		return (int)(Math.floor(a));
	}
	
	public static int floorInt(float a) {
		
		return (int)(Math.floor(a));
	}
	
	public static int roundInt(double a) {
		if ( a < 0 )
			return ((int) (a - 0.5));
		else
			return ((int) (a + 0.5));
	}
	
	public static float ceil(float a) {
		
		return (float)(Math.ceil(a));
	}
	
	public static float floor(float a) {
		
		return (float)(Math.floor(a));
	}
	
	public static double round(double a) {
		if ( a < 0 )
			return (double) ((int) (a - 0.5));
		else
			return (double) ((int) (a + 0.5));
	}
	
	public static int sigInt(double a) {
		
		if (a < 0.0) {
			
			return -1;
			
		} else if (a > 0.0) {
			
			 return 1;
			 
		} else { return 0; }
		
	}
	
	public static int sigInt(float a) {
		
		if (a < 0.0f) {
			
			return -1;
			
		} else if (a > 0.0f) {
			
			 return 1;
			 
		} else { return 0; }
		
	}
	
	public static int sigInt(int a) {
		
		if (a < 0) {
			
			return -1;
		}
		
		if (a > 0) {
			
			 return 1;
		}
		
		return 0;
	}
	
	public static int signum(int a) {
		
		return sigInt(a);
	}
	
	public static float signum(float a) {
		
		return Math.signum(a);
	}
	
	public static int normalizeCircular(int val, int min, int max) {
		return (int) normalizeCircular((float) val, (float) min, (float) max + 1);
	}
	
	public static long normalizeCircular(long val, long min, long max) {
		
		if (min > max) { min = min + max; max = min - max; min = min - max; }
		
		if (val < min || val > max) {
			
			long range = max - min;
			
			long loops = (val - min) / range;
			
			val -= loops * range;
			
			if (val < min) { val += range; }
		}
		
		return val;
	}
	
	public static float normalizeCircular(float val, float min, float max) {
		
		if (min > max) { min = min + max; max = min - max; min = min - max; }
		
		if (val < min || val >= max) {
		
			float range = max - min;
			
			int loops = (int) ( (val - min) / range);
			
			val -= loops * range;
			
			if ( val < min ) { val += range; }
		}
		
		return val;
	}
	
	public static float degToRad(float deg) {
		
		return normalizeCircular( (deg / 360f) * TAU, 0f, TAU );
	}
	
	public static float radToDeg(float rad) {
		
		return normalizeCircular( (rad / TAU) * 360f, 0f, 360f );
	}
	
	/** Returns the nearest multiple of "multipleOf" from "val". */
	public static int toMultiple(int val, int multipleOf) {
		
		return (int)(val / multipleOf) * multipleOf;
	}
	
	/** Returns the nearest multiple of "multipleOf" from "val". */
	public static float toMultiple(float val, float multipleOf) {
		
		return (int)(val / multipleOf) * multipleOf;
	}
	
	/** Returns the greatest common divisor of a and b using the Euclidian algorithm. */
	public static int gcd(int a, int b) {
		
	    if (a == 0) { return b; }
	    
	    while (b != 0) {
	    	
	    	if (a > b) { a -= b; } 
	    	else { b -= a; }
	    	
	    }
	    
	    return a;
	}
	
	/**
	 * Returns the greatest common divisor aswell as lambda and my.
	 * where d = gcd(a, b) and lambda * a + my * b = d.
	 * @return [0] = d, [1] = lambda, [2] = my
	 */
	public static int[] gcdLM(int a, int b){
		
		int r = a % b;
        int q = (a - r) / b;
        
        if(r == 0){
        	
        	return new int[]{b, 0, 1};
        }
        
        int [] res = gcdLM(b, r);
        
        return new int[]{res[0], res[2], res[1] - q* res[2]};
	}
	
	
	/** 136 */
	public static int giacomo(int n) {
		
		return (n * n + n) / 2;
	}
	
	public static int giaconado(int n) {
		
		return (n * n - n) / 2;
	}
	
	
	public static boolean isPOT(int n) {
		
		return (n & -n) == n;
	}
	
	public static boolean isPOT(long n) {
		
		return (n & -n) == n;
	}
	
	public static int log2(int n) {
		
		return 31 - Integer.numberOfLeadingZeros(n);
	}
	
	public static int log2(long n) {
		
		return 63 - Long.numberOfLeadingZeros(n);
	}
	
	
	public static float doubleStepsNormalized(float normalizeTo, int stepsNum, int step) {
		
		if (stepsNum < 63) { // use integer doubleVals
			
			return (float)(StrictMath.pow(2, step+1) * ((double)normalizeTo / (StrictMath.pow(2, stepsNum+1) - 2)));
		}
		
		// exploit the exponent for more numbers, might be inaccurate
		return (float)(StrictMath.pow(2, -(126-step)) * ((double)normalizeTo / (StrictMath.pow(2, -(126-stepsNum)) - (double)Float.MIN_NORMAL)));
	}
	
	public static float halfStepsNormalized(float normalizeTo, int stepsNum, int step) {
		
		return doubleStepsNormalized(normalizeTo, stepsNum, stepsNum-step-1);
	}
	
	
	/**
	 * Provides the n-th root of the give value.
	 * Substantially faster if n is a POT.
	 */
	public static float root(float value, int n) {
		
		return (float)(root((double)value, n));
	}
	
	/**
	 * Provides the n-th root of the give value.
	 * Substantially faster if n is a POT.
	 */
	public static double root(double value, int n) {
		
		if (isPOT(n)) {
			
			double val = value;
			
			for (int i = 0, exp = log2(n); i < exp; i++) {
				
				val = Math.sqrt(val);
				
			}
			
			return val;
		}
		
		return Math.pow(value, 1.0 / (double)n);
	}
	
	
	public static short min(short a, short b) { return a < b ? a : b; }
	public static int min(int a, int b) { return a < b ? a : b; }
	public static long min(long a, long b) { return a < b ? a : b; }
	public static float min(float a, float b) { return a < b ? a : b; }
	public static double min(double a, double b) { return a < b ? a : b; }
	
	public static short max(short a, short b) { return a > b ? a : b; }
	public static int max(int a, int b) { return a > b ? a : b; }
	public static long max(long a, long b) { return a > b ? a : b; }
	public static float max(float a, float b) { return a > b ? a : b; }
	public static double max(double a, double b) { return a > b ? a : b; }
	
	
	public static int digitalRoot(long n) {
		
		if (n < 0) { return -digitalRoot(-n); }
		
		while (n > 9) {
		
			int t = 0;
			
			for (long m = 10; m / 10 <= n; m *= 10) {
				
				t += (n % m) / (m / 10L);
			}
			
			n = t;
		}
		
		return (int)n;
	}
	
	
	public static double evaluateExpression(String expr) {
		
		if (expr == null || expr.trim().isEmpty()) { return 0.0; }
		expr = expr.replace(" ", "");
		
		if (expr.startsWith(")")) { expr = expr.substring(1); }
		if (expr.endsWith("(")) { expr = expr.substring(0, expr.length()-1); }
		
		if (expr.contains("(") ^ expr.contains(")")) {
			
			expr = expr.replace("(", "");
			expr = expr.replace(")", "");
		}
		
		while (expr.contains("(") && expr.contains(")")) {
			
			int si = 0, ei = 0;
			for (int i = 0; i < expr.length(); i++) {
				
				if (expr.charAt(i) == '(') {
					
					si = i;
					
				} else if (expr.charAt(i) == ')') {
					
					ei = i+1;
					break;
				}
			}
			
			String s = expr.substring(0, si);
			String m = expr.substring(si+1, ei-1);
			String e = expr.substring(ei);
			
			if (!m.isEmpty()) {
			
				m = evaluateExpression(m)+"";
			}
			
			expr = s + m + e;
		}
		
		for (int i = 2; i >= 0; i--) {
			
			expr = evalExprOpHelp(expr, i);
		}
		
		double val = Double.NaN;
		try { val = Double.parseDouble(expr); } catch (Exception e) {}
		
		return val;
	}
	
	private static String evalExprOpHelp(String expr, int order) {
		
		String op0 = " ", op1 = " ";
		
		if (order == 0) { op0 = "+"; op1 = "-"; }
		else if (order == 1) { op0 = "*"; op1 = "/"; }
		else if (order == 2) { op0 = "^"; }
		
		Predicate<String> negativeNumber = xpr -> order == 0 && xpr.startsWith("-") && !xpr.substring(1).contains("-");
		
		while (expr.contains(op0) || (expr.contains(op1) && !negativeNumber.test(expr))) {
			
			boolean minusFirst = order == 0 && expr.startsWith("-");
			
			int opNr = 0;
			int ind = expr.indexOf(op0, minusFirst ? 1 : 0);
			
			int ind1 = expr.indexOf(op1, minusFirst ? 1 : 0);
			if (ind == -1 || (ind1 < ind && ind1 >= 0)) { ind = ind1; opNr = 1; }
			
			if (ind == 0) { expr = expr.substring(1); continue; }
			if (ind == expr.length()-1) { expr = expr.substring(0, expr.length()-1); continue; }
			
			String l = "", r = "";
			int si = 0, ei = 0;
			
			for (int i = ind-1; i >= 0; i--) {
				char c = expr.charAt(i);
				
				if (!Character.isDigit(c) && c != '.' && c != '-') { break; }
				
				l = c + l;
				si = i;
				
				if (c == '-') { break; }
			}
			
			for (int i = ind+1; i < expr.length(); i++) {
				char c = expr.charAt(i);
				
				if (!Character.isDigit(c) && c != '.' && !(c == '-' && i == ind+1)) { break; }
				
				r = r + c;
				ei = i;
			}
			
			ei += 1;
			
			String s = expr.substring(0, si);
			String m = expr.substring(si, ei);
			String e = expr.substring(ei);
			
			boolean failed = false;
			
			double lv = 0.0, rv = 0.0;
			try { lv = Double.parseDouble(l); } catch (Exception ex) { failed = true; }
			try { rv = Double.parseDouble(r); } catch (Exception ex) { failed = true; }
			
			if (failed) { m = ""; }
			else {
				
				if (order == 0) {
					
					if (opNr == 0) { m = (lv + rv)+""; }
					else { m = (lv - rv)+""; }
					
				} else if (order == 1) {
				
					if (opNr == 0) { m = (lv * rv)+""; }
					else { m = (lv / rv)+""; }
					
				} else if (order == 2) {
					
					m = Math.pow(lv, rv)+"";
				}
			}
			
			expr = s+m+e;
		}
		
		return expr;
	}
	
}
