package com.alfaprojects.paride.it.msaslamonitor;

import java.util.HashMap;


public class ExpressionSolver {
	private static double eval(final String str) {
	    class Parser {
	        int pos = -1, c;

	        void eatChar() {
	            c = (++pos < str.length()) ? str.charAt(pos) : -1;
	        }

	        void eatSpace() {
	            while (Character.isWhitespace(c)) eatChar();
	        }

	        double parse() {
	            eatChar();
	            double v = parseExpression();
	            if (c != -1) throw new RuntimeException("Unexpected: " + (char)c);
	            return v;
	        }

	        // Grammar:
	        // expression = term | expression `+` term | expression `-` term
	        // term = factor | term `*` factor | term `/` factor | term brackets
	        // factor = brackets | number | factor `^` factor
	        // brackets = `(` expression `)`

	        double parseExpression() {
	            double v = parseTerm();
	            for (;;) {
	                eatSpace();
	                if (c == '+') { // addition
	                    eatChar();
	                    v += parseTerm();
	                } else if (c == '-') { // subtraction
	                    eatChar();
	                    v -= parseTerm();
	                } else {
	                    return v;
	                }
	            }
	        }

	        double parseTerm() {
	            double v = parseFactor();
	            for (;;) {
	                eatSpace();
	                if (c == '/') { // division
	                    eatChar();
	                    v /= parseFactor();
	                } else if (c == '*' || c == '(') { // multiplication
	                    if (c == '*') eatChar();
	                    v *= parseFactor();
	                } else {
	                    return v;
	                }
	            }
	        }

	        double parseFactor() {
	            double v;
	            boolean negate = false;
	            eatSpace();
	            if (c == '+' || c == '-') { // unary plus & minus
	                negate = c == '-';
	                eatChar();
	                eatSpace();
	            }
	            if (c == '(') { // brackets
	                eatChar();
	                v = parseExpression();
	                if (c == ')') eatChar();
	            } else { // numbers
	                StringBuilder sb = new StringBuilder();
	                while ((c >= '0' && c <= '9') || c == '.') {
	                    sb.append((char)c);
	                    eatChar();
	                }
	                if (sb.length() == 0) throw new RuntimeException("Unexpected: " + (char)c);
	                v = Double.parseDouble(sb.toString());
	            }
	            eatSpace();
	            if (c == '^') { // exponentiation
	                eatChar();
	                v = Math.pow(v, parseFactor());
	            }
	            if (negate) v = -v; // unary minus is applied after exponentiation; e.g. -3^2=-9
	            return v;
	        }
	    }
	    return new Parser().parse();
	}
	
	private static String findSum(String input,HashMap<String,double[]> values){
		String temp	=	input;
		while(temp.contains("sum")){
			//System.out.println("Trovata sottostringa sum in "+temp);
			int start	=	temp.indexOf("sum");
			String temp2	=	temp.substring(0,start);
			int after	=	start+4;
			if(after<=temp.length()){
				String elID	=	temp.substring(after-1, after);
				//System.out.println("Cerco elemento in hashmap "+elID);
				double[] myValues	=	(double[]) values.get(elID);
				//System.out.println("elementi trovati "+getSum(myValues));
				temp2	=	temp2+getSum(myValues)+temp.substring(after);
			}
			temp	=	temp2;
		}
		return temp;
	}
	
	private static String findProd(String input,HashMap<String,double[]> values){
		String temp	=	input;
		while(temp.contains("prod")){
			//System.out.println("Trovata sottostringa prod in "+temp);
			int start	=	temp.indexOf("prod");
			String temp2	=	temp.substring(0,start);
			int after	=	start+5;
			if(after<=temp.length()){
				String elID	=	temp.substring(after-1, after);
				//System.out.println("Cerco elemento in hashmap "+elID);
				double[] myValues	=	(double[]) values.get(elID);
				//System.out.println("elementi trovati "+myValues.toString());
				temp2	=	temp2+getProd(myValues)+temp.substring(after);
			}
			temp	=	temp2;
		}
		return temp;
	}
	
	private static String findCard(String input,HashMap<String,double[]> values){
		String temp	=	input;
		while(temp.contains("card")){
			//System.out.println("Trovata sottostringa card in "+temp);
			int start	=	temp.indexOf("card");
			String temp2	=	temp.substring(0,start);
			int after	=	start+5;
			if(after<=temp.length()){
				String elID	=	temp.substring(after-1, after);
				//System.out.println("Cerco elemento in hashmap "+elID);
				double[] myValues	=	(double[]) values.get(elID);
				//System.out.println("elementi trovati "+myValues.toString());
				temp2	=	temp2+myValues.length+temp.substring(after);
			}
			temp	=	temp2;
		}
		return temp;
	}
	
	private static String getSum(double[]list){
		String out="(";
		for(int i=0;i<list.length;i++){
			if(i>0){
				out=out+"+"+list[i];
			}
			else{
				out=out+list[0];
			}
		}
		return out+")";
	}
	
	private static String getProd(double[]list){
		String out="(";
		for(int i=0;i<list.length;i++){
			if(i>0){
				out=out+"*"+list[i];
			}
			else{
				out=out+list[0];
			}
		}
		return out+")";
	}
	
	public static double getResult(String expression,HashMap<String,double[]> values){
		return eval(findCard(findSum(findProd(expression,values),values),values));
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		double [] testlist = new double[4];
		testlist[0]=2;
		testlist[1]=2;
		testlist[2]=6;
		testlist[3]=6;
		double [] testlist2 = new double[4];
		testlist2[0]=5;
		testlist2[1]=6;
		testlist2[2]=7;
		testlist2[3]=8;
		HashMap<String,double[]> testhash	=	new HashMap<String,double[]>();
		testhash.put("A", testlist);
		testhash.put("B", testlist2);
		
		String test		=	"(sumA/cardA)*prodA";
		double res	=	getResult(test,testhash);
		System.out.println("RISULTATO "+res);
	}

}
