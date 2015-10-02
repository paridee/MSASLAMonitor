package com.alfaprojects.paride.it.msaslamonitor;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

//decisore SE POSTICIPARE O NO, basato su tabella verita' in cui i campi sono(in ordine): BATTERIA ORA WIFI ORA BATTERIA DOPO WIFI DOPO
public class Decisor {
	int decisionTable [][][][] = new int[2][2][2][2];
	int wifimatrix[][];
	int batterymatrix[][];
	Calendar calendar 			= GregorianCalendar.getInstance(); // creates a new calendar instance
	//arguments: even if int type please enter only 0 or 1 (0 means low, 1 means high)
	//returns 0 if execute now, return 1 if decide later,  return 2 if don't care
	public Decisor(int[][] batterymatrix, int[][] wifimatrix){
		super();
		decisionTable[0][0][0][0]	=	0;
		decisionTable[0][0][0][1]	=	1;
		decisionTable[0][0][1][0]	=	1;
		decisionTable[0][0][1][1]	=	1;
		decisionTable[0][1][0][0]	=	0;
		decisionTable[0][1][0][1]	=	0;
		decisionTable[0][1][1][0]	=	2;
		decisionTable[0][1][1][1]	=	1;
		decisionTable[1][0][0][0]	=	0;
		decisionTable[1][0][0][1]	=	2;
		decisionTable[1][0][1][0]	=	0;
		decisionTable[1][0][1][1]	=	1;
		decisionTable[1][1][0][0]	=	0;
		decisionTable[1][1][0][1]	=	0;
		decisionTable[1][1][1][0]	=	0;
		decisionTable[1][1][1][1]	=	0;
		this.batterymatrix			=	batterymatrix;
		this.wifimatrix				=	wifimatrix;
	}
	
	public int decide(int battery_now, int wifi_now, Date now, Date later){
		if(now.after(later)){
			return 0;	//if expired than compute
		}
		Calendar c	=	Calendar.getInstance();
		c.setTime(now);
		int dayOfWeekNow	=	c.get(Calendar.DAY_OF_WEEK)-1;
		int quarterOfDayNow	=	this.getQuarterofDay(now);
		c.setTime(later);
		int dayOfWeekLater	=	c.get(Calendar.DAY_OF_WEEK)-1;
		int quarterOfDayLat	=	this.getQuarterofDay(later);


		if(dayOfWeekLater<dayOfWeekNow){
			System.out.println("Decisor first case day of week now "+dayOfWeekNow+" later "+dayOfWeekLater);
			for(int i=dayOfWeekNow;i<7;i++){
				//in the inital day check only a part
				if(i==dayOfWeekNow){ //implicit: dayOfWeekNow!=dayOfWeekLater so i check for the whole initial day
					for(int j=quarterOfDayNow;j<96;j++){
						int value	=	decide(battery_now,wifi_now,this.batterymatrix[i][j],this.wifimatrix[i][j]);
						if(value	==	1){
							return value;
						}
					}
				}
				//in the remaining days check the whole day
				else{
					for(int j=0;j<96;j++){
						int value	=	decide(battery_now,wifi_now,this.batterymatrix[i][j],this.wifimatrix[i][j]);
						if(value	==	1){
							return value;
						}
					}
				}
			}
			for(int i=0;i<dayOfWeekLater;i++){
				if(i==dayOfWeekLater){	//last day, i check from 00:00 to the final quarter of hour
					for(int j=0;j<=quarterOfDayLat;j++){
						int value	=	decide(battery_now,wifi_now,this.batterymatrix[i][j],this.wifimatrix[i][j]);
						if(value	==	1){
							return value;
						}
					}
				}
				//in the remaining days check the whole day
				else{
					for(int j=0;j<96;j++){
						int value	=	decide(battery_now,wifi_now,this.batterymatrix[i][j],this.wifimatrix[i][j]);
						if(value	==	1){
							return value;
						}
					}
				}
			}
		}
		else{
			System.out.println("Decisor second case day of week now "+dayOfWeekNow+" later "+dayOfWeekLater);
			for(int i=dayOfWeekNow;i<=dayOfWeekLater;i++){
				if(dayOfWeekNow==dayOfWeekLater){
					for(int j=quarterOfDayNow;j<=quarterOfDayLat;j++){
						int value	=	decide(battery_now,wifi_now,this.batterymatrix[i][j],this.wifimatrix[i][j]);
						if(value	==	1){
							return value;
						}
					}
				}
				else if(i==dayOfWeekNow){ //implicit: dayOfWeekNow!=dayOfWeekLater so i check for the whole initial day
					for(int j=quarterOfDayNow;j<96;j++){
						int value	=	decide(battery_now,wifi_now,this.batterymatrix[i][j],this.wifimatrix[i][j]);
						if(value	==	1){
							return value;
						}
					}
				}
				else if(i==dayOfWeekLater){	//last day, i check from 00:00 to the final quarter of hour
					for(int j=0;j<=quarterOfDayLat;j++){
						int value	=	decide(battery_now,wifi_now,this.batterymatrix[i][j],this.wifimatrix[i][j]);
						if(value	==	1){
							return value;
						}
					}
				}
				else{
					for(int j=0;j<96;j++){
						int value	=	decide(battery_now,wifi_now,this.batterymatrix[i][j],this.wifimatrix[i][j]);
						if(value	==	1){
							return value;
						}
					}
				}
			}
		}

		return 0;
	}
	
	//uso interno, NON usare esplicitamente
	public int decide(int battery_now, int wifi_now, int battery_later, int wifi_later){
		if(battery_later==2){
			battery_later	=	0;	//replace don't care
		}
		if(wifi_later==2){
			wifi_later	=	0;	//replace don't care
		}
		return this.decisionTable[battery_now][wifi_now][battery_later][wifi_later];
	}
	
	private int getQuarterofDay(Date myDate){
		calendar.setTime(myDate);
		int hour	=	calendar.get(Calendar.HOUR_OF_DAY);
		int minutes	=	calendar.get(Calendar.MINUTE);
		int quarter	=	(hour*4)+(minutes/15);
		return quarter;
	}
}
