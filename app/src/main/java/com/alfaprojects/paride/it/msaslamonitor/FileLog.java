package com.alfaprojects.paride.it.msaslamonitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class FileLog {

	
	
	public void FileLog(){
		
	}
	
	//Crea un file di log e lo riempie con dimm bytes
	public static void CreateFilLogAdvanced(File f, double dimmB) throws IOException

	{
    
		
	FileOutputStream file = new	
	FileOutputStream(f.getAbsolutePath(), true);

	 PrintStream Output = new PrintStream(file);
     double dimmFileMB= f.length();
	 String st="";
     while (dimmFileMB < dimmB)
     {
        
    	 
    	st="2015"
    	    + GeneratoreCasuale.randInt(1, 12)+
    	    GeneratoreCasuale.randInt(1, 31)+
    	          (int)(Math.random()*23)+(int)(Math.random()*59)+(int)(Math.random()*59)+";"
    			  +"-200"+";"
    	          +"0.1"+";"
    			  +"3G"+";"
    			  +"-127";
    	
    	
	    Output.append(st);
	    Output.println();
	    dimmFileMB= f.length();
	    st="";
     }   
	    
	 System.out.println(dimmFileMB);   
	 file.close();
	
	 
     
	}


	//Crea un file di log e lo riempie con dimm bytes
	public static void CreateFilLog(File f, double dimmMB) throws IOException

	{


		FileOutputStream file = new
				FileOutputStream(f.getAbsolutePath(), true);

		PrintStream Output = new PrintStream(file);
		double dimmFileMB= f.length()/ (1024 * 1024);
		String st="";
		while (dimmFileMB < dimmMB)
		{

			st= GeneratoreCasuale.randInt(1, 100)+"";

			Output.append(st);
			Output.println();
			dimmFileMB= f.length()/ (1024 * 1024);
			st="";
		}

		System.out.println(dimmFileMB);
		file.close();

	}
	
	
	public static final void main(String[] args) throws IOException{ 
        //Testing  
       long d=2;
       File f= new File("C:/Users/Dell/Documents/workspace/ClientProva/File/log.txt");
       CreateFilLog(f,d);
       double sizeInMb = f.length() / (1024 * 1024);
       System.out.println(sizeInMb); 
       /*
       double sizeInBytes=f.length();
       System.out.println(sizeInBytes); 
       double sizeInMb = sizeInBytes / (1024 * 1024);
       System.out.println(sizeInMb); 
       */
       
       
        
    } 
	
	
	
	
}
