import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.io.FileNotFoundException;


//
public class Runner {
	//public Vector<Integer> clauses = new Vector<Integer>();
	//public Vector[] clauses;
	// public static List<List<Integer>> clauses;
	public static List<int[]> clauses = new ArrayList<int[]>();

	//GA vals
	public String select, cross; //selection and crossover methods
	public double cprob; 	//crossover probability

	//PBIL vals
	public double [] prob;
	public int [][] samples;
	public double plr, nlr, mamt;

	//Shared vals
	public int pop, iter;
	public double mprob;
	public int vars, totalClauses;

	Random randGen = new Random();
	



	public void pbil(){
		//initialize probability vector to 0.5
		for(int i=0; i<vars; i++){
			prob[i]=0.5;
		}
		// for(int i=0; i<vars; i++){
		// 	System.out.printf(" %f ",prob.elementAt(i));
		// }
		int iB=0;
		int iW=0;
		while(iter>0){
			for(int i=0; i<pop; i++) {
				genSamples(i);
			}	

			//update probability vector towards best solution
			for(int k=0; k<vars; k++){
				prob[k]=prob[k]*(1-plr)+samples[iB][k]*plr;
			}

			//update probability away from worst solution
			for(int l=0; l<vars; l++){
				if(samples[iB][l] != samples[iW][l]){
					prob[l]=prob[l]*(1-nlr)+samples[iB][l]*nlr;
				}
			}

			//mutate probability vector
			int mutDir=1;
			for(int q=0; q<vars; q++){
				if(randGen.nextDouble() <= mprob){
					if(randGen.nextDouble() > 0.5){
						mutDir=1;
					}
					else{
						mutDir=0;
					}
					prob[q]=prob[q]*(1-mamt)+mutDir*mamt;
				}
			}
			iter--;
		}

	}


	// public void eval(){
	// 	int iB, iW;  
	// 	for(int i=0; i<pop; i++){
	// 		for(int j=0; j<vars; j++){
	// 			for(int k=0; k<totalClauses; k++){
	// 				(samples.elementAt(i)).elementAt(j); 
	// 			}
	// 		}

	// 	}
	// }


	public void genSamples(int i){
		for(int j=0; j<vars; j++){
			if(randGen.nextDouble() <= prob[j]){
				samples[i][j]=1;
				//System.out.printf("%d", samples.[i][j]);
			}
			else{
				samples[i][j]=0;
				//System.out.printf("%d", samples.[i][j]);
			}
		}
	}

	public Runner(){
	}

	public void ga(){
		//
	}

	
	public void boltmannSelection(){
		//evolutionary fitness of individual

		//sum of evolutionary fitnesses of all individuals
	}
	
	//public static int[][] multi = new int[500][100];
	//create list that will hold the arrays of all of the clauses
	//read the file to strings, then create array of the string's length 
	//and add it to the list of clauses
	public void read(String filePath) {
		Scanner scanner;
		try {
           scanner = new Scanner(new File(filePath));
        } catch(FileNotFoundException fnfe) { 
            System.out.println(fnfe.getMessage());
            return;
        } 
        //Get Header
        String header = scanner.nextLine();
        String[] headerVals = header.split(" ");
        System.out.println(Arrays.toString(headerVals));
        vars = Integer.parseInt(headerVals[2]);
        totalClauses = Integer.parseInt(headerVals[3]);

        // Scanner scanner = new Scanner(new File(filePath));
		while (scanner.hasNext()){
			String line = scanner.nextLine();
			String[] valStrings = line.split("\\s+");
			int valArray[] = new int[valStrings.length-1];
			for(int i=0; i<valStrings.length-1; i++){
				// System.out.println(valStrings[i]);
				valArray[i] = Integer.parseInt(valStrings[i]);
				// System.out.println(valArray[i]);
			}
		    clauses.add(valArray);
		}
		scanner.close();
	
		for(int i=0; i<clauses.size(); i++){
			System.out.println(Arrays.toString(clauses.get(i)));
		}
		
	}

	public static void main(String[] args){
		// TODO Auto-generated method stub
		 	Runner evolAlg = new Runner();
			// File file = new File(args[0]);
			evolAlg.read(args[0]);
			if(args[7].equals("p")){
				evolAlg.pop=Integer.parseInt(args[1]);
				evolAlg.plr=Double.parseDouble(args[2]);
				evolAlg.nlr=Double.parseDouble(args[3]);
				evolAlg.mprob=Double.parseDouble(args[4]);
				evolAlg.mamt=Double.parseDouble(args[5]);
				evolAlg.iter=Integer.parseInt(args[6]);
				evolAlg.samples = new int[evolAlg.pop][evolAlg.vars];
				evolAlg.prob = new double[evolAlg.vars];
				evolAlg.pbil();
			} else if(args[7].equals("g")){
				evolAlg.pop=Integer.parseInt(args[1]);
				evolAlg.select=args[2];
				evolAlg.cross=args[3];
				evolAlg.cprob=Double.parseDouble(args[4]);
				evolAlg.mprob=Double.parseDouble(args[5]);
				evolAlg.iter=Integer.parseInt(args[6]);
				evolAlg.ga();
			}
			// evolAlg.clauses = new ArrayList<List<Integer>>(evolAlg.totalClauses);
			// evolAlg.clauses = new Vector[evolAlg.totalClauses];
			// for(int i = 0; i < evolAlg.totalClauses; i++){
   // 				evolAlg.clauses[i] = new Vector();
   // 			}
	}
}

/*
posting changes:
git pull
git add -A
git commit -m "message"
git push

if it gives you shit, just do:
git stash
git add -A
git commit -m "message"
git push

*/
