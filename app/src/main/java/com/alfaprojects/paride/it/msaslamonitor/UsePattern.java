package com.alfaprojects.paride.it.msaslamonitor;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


public class UsePattern {
	HashMap<Date,PatternItem> 	mydata		=	new HashMap<Date,PatternItem>();
	Date					  	startDate;
	Date					  	finishDate;
	Calendar calendar 			= GregorianCalendar.getInstance(); // creates a new calendar instance
	int							threshold;  
	int							wifiThreshold;
	public Context				aContext;

	public UsePattern(String logfilepath,int batteryThreshold,int wifiThreshold,Context myContext){
	    try{
			this.aContext	=	myContext;
			BufferedReader br	=	new BufferedReader(new InputStreamReader(aContext.getAssets().open(logfilepath)));
	        String line = null;
	        this.threshold		=	batteryThreshold;
	        this.wifiThreshold	=	wifiThreshold;
			try {
				line = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	        while (line != null) {
	        	//parse file
	        	System.out.println(line);
	        	String day	=	line.substring(0, 14);
	        	String[]	strings=	line.split(";");
	        	PatternItem myItem	=	new PatternItem();
	        	double level	=	Double.parseDouble(strings[2])*100;
	        	int intlevel	=	(int)level;
	        	System.out.println("Wifi level "+strings[1]+" battery level "+intlevel+"%");
	        	myItem.batteryLevel	=	intlevel;
	        	myItem.wifiLevel	=	Integer.parseInt(strings[1]);
	        	System.out.println(day);
	        	DateFormat df = new SimpleDateFormat("yyyyMMddkkmmss");
	        	Date result	=	null;
	        	try {
					result =  df.parse(day);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
	        	System.out.println(result.toString());
	        	if(this.startDate==null){
	        		this.startDate	=	result;
	        	}
	        	this.finishDate	=	result;
	        	calendar.setTime(result);       	
	        	System.out.println(this.getQuarter(calendar.get(Calendar.MINUTE)));
	        	String nday	=	calendar.get(Calendar.DAY_OF_MONTH)+"";
	        	String month=	(calendar.get(Calendar.MONTH)+1)+"";
	        	String year	=	calendar.get(Calendar.YEAR)+"";
	        	String hour	=	calendar.get(Calendar.HOUR_OF_DAY)+"";
	        	try {
					nday	=	this.fixsize(nday);
		        	month	=	this.fixsize(month);
		        	hour	=	this.fixsize(hour);
		        	DateFormat df2 = new SimpleDateFormat("yyyyMMddkkmm");
		        	Date newDate	=	df2.parse(year+month+nday+hour+this.getQuarter(calendar.get(Calendar.MINUTE)));
		        	System.out.println(year+month+nday+hour+this.getQuarter(calendar.get(Calendar.MINUTE)));
		        	System.out.println(newDate.toString());
		        	mydata.put(newDate, myItem);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            line = br.readLine();
	            
	        }
	        System.out.println(this.mydata.toString());
	        System.out.println("Observation window " + this.startDate.toString() + " " + this.finishDate.toString());
	        
	    } catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private String getQuarter(int minutes){
		if(minutes<15){
			return "00";
		}
		else if(minutes <30){
			return "15";
		}
		else if(minutes <45){
			return "30";
		}
		else return "45";
	}
	
	private int getQuarterofDay(Date myDate){
		calendar.setTime(myDate);
		int hour	=	calendar.get(Calendar.HOUR_OF_DAY);
		int minutes	=	calendar.get(Calendar.MINUTE);
		int quarter	=	(hour*4)+(minutes/15);
		return quarter;
	}
	
	private String fixsize(String s) throws Exception{
		if(s.length()==1){
			return "0"+s;
		}
		else if(s.length()==2){
			return s;
		}
		else throw new Exception("Wrong format");
	}

	public int[][] getHighBatteryMatrix(Date startDate,Date finishDate){ //intersection among all days
		int[][] matrix	=	new int[7][96];
		for(int i=0;i<7;i++){
			for(int j=0;j<96;j++){
				matrix[i][j]	=	2; //2 means NOT AVAILABLE
			}
		}
		Set<Date> datesInHash	=	this.mydata.keySet();
		Iterator<Date>	anIterator	=	datesInHash.iterator();
		while(anIterator.hasNext()){
			Date current		=	anIterator.next();
			if(current.after(startDate)&&current.before(finishDate)){
				calendar.setTime(current);
				PatternItem myItem	=	this.mydata.get(current);
				int dayIndex		=	(calendar.get(Calendar.DAY_OF_WEEK)+5)%7;
				int quarterOfDay	=	this.getQuarterofDay(current);
				//System.out.println(current.toString()+" BATTERIA "+myItem.batteryLevel+" WIFI "+myItem.wifiLevel);
				if(matrix[dayIndex][quarterOfDay]==2){
					if(myItem.batteryLevel>=this.threshold){
						matrix[dayIndex][quarterOfDay]	=	1;
					}
					else{
						matrix[dayIndex][quarterOfDay]	=	0;
					}
				}
				else if(matrix[dayIndex][quarterOfDay]	==	1){
					if(myItem.batteryLevel<this.threshold){
						matrix[dayIndex][quarterOfDay]	=	0;
					}
				}	
			}
		}
		return matrix;
	}
	public int[] getAndValues(int[][] matrix, int startday, int finishday, int startq, int finishq){
		int nquarter		=	finishq-startq;
		int[]			res	=	new int[nquarter];
		for(int j=startq;j<finishq;j++){
			res[j-startq]	=	1;
			for(int i=startday;i<=finishday;i++){
				if(matrix[i][j]==0||matrix[i][j]==2){
					res[j-startq]=0;
				}
			}
		}
		return res;
		
	}
	
	public int[][] getHighWifiMatrix(Date startDate,Date endDate){ //intersection among all days (quarter by quarter)
		int[][] matrix	=	new int[7][96];
		for(int i=0;i<7;i++){
			for(int j=0;j<96;j++){
				matrix[i][j]	=	2; //2 means NOT AVAILABLE
			}
		}
		Set<Date> datesInHash	=	this.mydata.keySet();
		Iterator<Date>	anIterator	=	datesInHash.iterator();
		while(anIterator.hasNext()){
			Date current		=	anIterator.next();
			//System.out.println(current.toString()+" "+startDate.toString()+" "+current.after(startDate));
			if(current.after(startDate)&&current.before(endDate)){
				calendar.setTime(current);
				PatternItem myItem	=	this.mydata.get(current);
				int dayIndex		=	(calendar.get(Calendar.DAY_OF_WEEK)+5)%7;
				int quarterOfDay	=	this.getQuarterofDay(current);
				//System.out.println(current.toString()+" BATTERIA "+myItem.batteryLevel+" WIFI "+myItem.wifiLevel);
				if(matrix[dayIndex][quarterOfDay]==2){
					if(myItem.wifiLevel>=this.wifiThreshold){
						matrix[dayIndex][quarterOfDay]	=	1;
					}
					else{
						matrix[dayIndex][quarterOfDay]	=	0;
					}
				}
				else if(matrix[dayIndex][quarterOfDay]	==	1){
					if(myItem.wifiLevel<this.wifiThreshold){
						matrix[dayIndex][quarterOfDay]	=	0;
					}
				}
			}
		}
		return matrix;
	}
	/*
	public static void main(String[] args) {
		//carica il file di log, popola le matrici prendendo la soglia per la batteria (%) e per il wifi (livello)
		UsePattern test	=	new UsePattern("log.txt",30,-80);
		//restituisce la matrice calcolata sull'intero periodo di misura, per calcolare su intervalli diversi cambiare le date
		int[][] myMatrix	=	test.getHighBatteryMatrix(test.startDate,test.finishDate);
		//stampa i valori 7 giorni della settimana, 96 quarti d'ora per giorno (24*4)
		System.out.println("----------------------------BATTERY-----------------------------");
		for(int i=0;i<7;i++){
			System.out.print(i+"|");
			for(int j=0;j<96;j++){
				System.out.print(myMatrix[i][j]+" ");
			}
			System.out.print("\n");
		}
		//TODO RICORDATI DI USARE QUESTA!!! fa l'and "in verticale" giorno per giorno (sull'intervallo primi 2 parametri) sui quarti d'ora indicati (ultimi 2 argomenti
		//permette facilmente di fare analisi in fascia oraria nei giorni desiderati (es: 8-22 dal lunedi al venerdi)
		int[] res	=	test.getAndValues(myMatrix, 0, 6, 40, 50);
		System.out.print("\n");
		for(int i=0;i<10;i++){
			System.out.print(res[i]+" ");
		}
		System.out.print("\n");
		//System.out.println(test.getAndValues(myMatrix, 0, 2, 0, 5));
		System.out.println("----------------------------WIFI-----------------------------");
		myMatrix	=	test.getHighWifiMatrix(test.startDate,test.finishDate);
		for(int i=0;i<7;i++){
			System.out.print(i+"|");
			for(int j=0;j<96;j++){
				System.out.print(myMatrix[i][j]+" ");
			}
			System.out.print("\n");
		}
		System.out.println("---------------FINE-----------------");
		//decisore impostato su tabella di verita' (l'ho rifatta io, va verificata con Serena
		Decisor testd 	=	 new Decisor(test.getHighBatteryMatrix(test.startDate, test.finishDate),test.getHighWifiMatrix(test.startDate, test.finishDate));
		Date today		=	new Date();
		Date tomorrow	=	new Date(today.getTime()+(1000*60*60*24));
		Date after60min	=	new Date(today.getTime()+(1000*60*60));
		int result 		=	testd.decide(0, 0, new Date(), after60min);
		System.out.println("PREVISIONE "+result);
		TaskInstance pippo	=	new TaskInstance(1,"sumA/cardA",3600,20,new Date(),after60min);
		//TODO codice che calcola quanto tempo ci metto ad eseguire il task sul cloud e sul mobile, faccio il confronto (vd come genero il grafico su android) e decido dove eseguire
		//TODO genera dati in modo che sia "a cavallo" tra soglia offload
	}*/
}
