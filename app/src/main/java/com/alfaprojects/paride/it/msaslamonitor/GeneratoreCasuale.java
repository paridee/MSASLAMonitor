package com.alfaprojects.paride.it.msaslamonitor;

import java.util.Random;


/** 
 * RandomNumberGenerator creates random numbers according to one of the following distributions:
 * Poisson, Exponential, Geometric, Pareto, ParetoBounded, Uniform or Constant 
 *  
 * The method getRandom() will return the next random value.  
 * It can use a provided stream Random r, if needed, or use the default stream. 
 *  
 * For most of the distributions a single distribution parameter is needed.  
 * USAGE: double rand = Exponential.getRandom(0.25); 
 *  
 * When the wrong number of parameters is passed, IllegalArgumentException is thrown. 
 *  
 * Contact: zxy754219@gmail.com 
 *  
 * @author alexz3 
 *  
 * 
 */ 
public enum GeneratoreCasuale {     
     
    Poisson { 
        @Override 
        public double getRandom(Random r, double lambda) { 
            double L = Math.exp(-lambda); 
            int k = 0; 
            double p = 1.0; 
            do { 
                k++; 
                p = p * r.nextDouble(); 
            } while (p > L); 

            return k - 1; 
        } 
    },  
    Exponential { 
        @Override 
        public double getRandom(Random r, double p) { 
            return -(Math.log(r.nextDouble()) / p); 
        } 
    },  
    Geometric { 
        @Override 
        public double getRandom(Random r, double geoSeed) { 
            double p = 1.0 / ((double) geoSeed); 
            return (int)(Math.ceil(Math.log(r.nextDouble())/Math.log(1.0-p))); 
        } 
    },  
    Pareto {         
        @Override 
        public double getRandom(Random r, double alpha, double xM) { 
            double v = r.nextDouble(); 
            while (v == 0){ 
                v = r.nextDouble(); 
            } 
             
            return xM / Math.pow(v, 1.0/alpha); 
        } 
    }, 
    ParetoBounded {     
        @Override 
        public double getRandom(Random r, double alpha, double L, double H) { 
            double u = r.nextDouble(); 
            while (u == 0){ 
                u = r.nextDouble(); 
            } 
             
            double x = -(u*Math.pow(H,alpha)-u*Math.pow(L,alpha)-Math.pow(H,alpha)) /  
                            (Math.pow(H*L,alpha)); 
            return Math.pow(x, -1.0/alpha); 
        } 
    },     
    Uniform { 
        @Override 
        public double getRandom(Random r, double p) { 
            return r.nextDouble() * p; 
        } 
    },  
    Constant { 
        @Override 
        public double getRandom(Random r, double N) {             
            return N; 
        } 
    }; 

    public double getRandom(double p) throws IllegalArgumentException {         
        return getRandom(defaultR, p);         
    } 
     
    public double getRandom(double a, double b) throws IllegalArgumentException {         
        return getRandom(defaultR, a, b);         
    } 
     
    public double getRandom(double a, double b, double c) throws IllegalArgumentException {        
        return getRandom(defaultR, a, b, c);         
    } 
     
    public double getRandom(Random r, double p) throws IllegalArgumentException { 
        throw new IllegalArgumentException();         
    } 
     
    public double getRandom(Random r, double a, double b) throws IllegalArgumentException{ 
        throw new IllegalArgumentException(); 
    } 

    public double getRandom(Random r, double a, double b, double c) throws IllegalArgumentException{
        throw new IllegalArgumentException(); 
    } 
         
    public static final Random defaultR = new Random(); 
    
    public static double randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
    
   /* public static int genPareto(){
    	GeneratoreCasuale testStat = Pareto;
    	double lambda = 1.0 / 25.0;
    	return (int) testStat.getRandom(lambda,1,1);
    }
   */
    
    
    public static final void main(String[] args){ 
        //Testing  
         
        GeneratoreCasuale testStat = Exponential;
        double lambda = 1.0 / 25.0; 
        System.out.println(" Testing stat: "+ testStat+"  with lambda: "+ lambda);
        for (int i = 0; i < 1000; i++){ 
            System.out.println(testStat.getRandom(lambda)); 
        }       
      
        testStat=Pareto;
        System.out.println(" Testing stat: "+ testStat+"  with lambda: "+ lambda);
        System.out.println(testStat.getRandom(defaultR, 1,2)); 
        
    } 

}


