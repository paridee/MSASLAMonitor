package com.alfaprojects.paride.it.msaslamonitor;

import java.util.Date;
import java.util.HashMap;

public class Task {
	private int	id;
	private String 	formula;
	private int		expiration; //expires after N seconds
	private double	threshold;	//threshold could be not consistent with its value on DB, some divine entity could update the local value with DB value
	public 	HashMap<Integer,Double> heurstics 	=	new HashMap<Integer,Double>();
	public	int[]						keys	=	new int[20];

	public Task(){
		super();
	}
	
	public Task(int id, String formula, int expiration,
			double threshold) {
		super();
		this.id			=	id;
		this.formula 	= 	formula;
		this.expiration = 	expiration;
		this.threshold 	= 	threshold;
	}

	public String getFormula() {
		return formula;
	}

	public int getExpiration() {
		return expiration;
	}

	public double getThreshold() {
		return threshold;
	}
	
	public int getId(){
		return id;
	}

	public void setHeuristic(String key,int maxnumberofelements,GeneratoreCasuale aGenerator){ //poniamo che ho 1 solo item nell hashmap dei dati
		int n	=	20;
		for(int i=20;i>0;i--){
			int k		=	i*(maxnumberofelements/n);
			double[] testdata2	=	new double[k];
			TaskInstance newTask	=	new TaskInstance(this.getId(),this.getFormula(),this.getExpiration(),this.getThreshold(),Singletons.currentSimulatedTime,Singletons.currentSimulatedTime,this.heurstics,this.keys);
			for(int j=0;j<k;j++) {
				double data=0;
				if(aGenerator!=null){
					if(aGenerator.toString().equals("Pareto")){
						//System.out.println("Task.java calculating Pareto heuristic");
						data	=	aGenerator.getRandom(3,2);
					}
					else if(aGenerator.toString().equals("Exponential")){
						//System.out.println("Task.java calculating Exponential heuristic");
						data	=   aGenerator.getRandom(0.25);
					}
					else{
						//System.out.println("Task.java calculating Default heuristic");
						testdata2[j]			=	j%50;
					}
					if (data<0.01){
						data	=	0;
					}
					testdata2[j]	=	data;
				}
				else{
					testdata2[j]			=	j%50;
				}

				//testdata2[j] = meanvalue;
			}
			newTask.addToRawData(key, testdata2);
			long start 	= 	System.currentTimeMillis();
			//System.out.println("DATI "+newTask.getRawdata()+" val k "+k);
			double res2	=	ExpressionSolver.getResult(this.getFormula(), newTask.getRawdata());
			long end	= 	System.currentTimeMillis()-start;
			System.out.println("ci ho messo per "+k+" elementi questo tempo "+end);
			this.heurstics.put(k, (double) end / k);
			keys[i-1]	=	k;
			System.out.println("TaskInstance calcolata per numero element " + k + " valore " + (double)end/k);
		}

		//long average	=	0;
		/*
		for(int i=0;i<20;i++){
			int k		=	maxnumberofelements;
			double[] testdata2	=	new double[k];
			TaskInstance newTask	=	new TaskInstance(this.getId(),this.getFormula(),this.getExpiration(),this.getThreshold(),this.startDate,this.endDate);
			for(int j=0;j<k;j++) {
				testdata2[j] = meanvalue;
				newTask.addToRawData(key, testdata2);
			}

			long start 	= 	System.currentTimeMillis();
			//System.out.println("DATI "+newTask.getRawdata()+" val k "+k);
			double res2	=	ExpressionSolver.getResult(this.getFormula(), newTask.getRawdata());
			long end	= 	System.currentTimeMillis()-start;
			System.out.println("ci ho messo per "+k+" elementi questo tempo "+end);
			average	=	average+end;
			System.out.println("TaskInstance calcolata per numero element " + k + " valore " + (double)end/k);
		}
		System.out.println("ci ho messo per "+maxnumberofelements+" elementi IN MEDIA questo tempo "+(average/20));
		*/

	}

	public int[] getKeys() {
		return keys;
	}

	public void setKeys(int[] keys) {
		this.keys = keys;
	}

	public HashMap<Integer, Double> getHeurstics() {
		return heurstics;
	}

}
