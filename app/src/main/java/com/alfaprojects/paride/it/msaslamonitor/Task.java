package com.alfaprojects.paride.it.msaslamonitor;

public class Task {
	private int	id;
	private String 	formula;
	private int		expiration; //expires after N seconds
	private double	threshold;	//threshold could be not consistent with its value on DB, some divine entity could update the local value with DB value
	
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
}
