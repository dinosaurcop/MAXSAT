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
	public double plr, nlr, mamt;

	//Shared vals
	public String alg;
	public int [][] samples;
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
			if(randGen.nextDouble() <= prob[j]){ //alexi: for ga, the initial prob threshold could vary as well
				samples[i][j]=1;
				//System.out.printf("%d", samples.[i][j]);
			} else{
				samples[i][j]=0;
				//System.out.printf("%d", samples.[i][j]);
			}
		}
	}

	//evaluate fitness of given individual (int array). Returns fitness score
	//equal to the total number of clauses that it does NOT miss (i.e. hit count)
	//note: we can either check mismatched clauses, or get more elaborate 
	//with how many vals within those clauses are mismatched. Majercick says just
	//count clauses, but I've left it flexible here for now.
	public int fitness(int []worm){
		int clauseMisses = 0;
		for(int i=0; i<totalClauses; i++){
			//check clause list at i, each in int [] 
			int clause[] = clauses.get(i);
			int valMisses = 0;
			for(int j=0; j<clauses.length; j++){
				if(clause[j]<0){ //neg
					int index = abs(clause[j]);
					if(worm[index]==1){valMisses+=1;}
				} else if(worm[clause[j]]==0){valMisses+=1;} //pos
			}
			if (valMisses > 0){
				clauseMisses += 1;
			}
		}
		return totalClauses-clauseMisses;
	}

	//breed individuals selected for reproduction
	public void runBreeding(int []reproducers){
		int[][] oldSamples = samples;
		for(int i=0; i<pop; i++){
			//randomly select two individuals
			int parents[] = new int[2];
			while(1){ //make sure individuals are unique
				parents[0] = randGen.nextInt(pop);
				parents[1] = randGen.nextInt(pop);
				if(parents[0] != parents[1]){break;}
			}
			//Uniform Crossover
			if(randGen.nextDouble() <= cprob){
				if(cross.eqauls("uc")){
					int p = 0; //start on first parent
					for(int j=0; j<vars; j++){
						if(randGen.nextDouble <= 0.47){ //slightly less than 50% chance of switching
							// p = Math.abs(p-1); //switches p, 0 -> 1 or 1 -> 0
							p ^= 1; //switches p, 0 -> 1 or 1 -> 0
						}
						samples[i][j] = oldSamples[parents[p]][j];
					}
				} else if(cross.equals("1c")){
					//Single Point Crossover
				}
			} else {
				//no crossover, pick the first individual
				samples[i] = oldSamples[parents[0]];
			}
			

		}
	}

	public int[] rsGen(int []fitnesses){}
	public int[] tsGen(int []fitnesses){}

	public int[] bsGen(int []fitnesses){
		int fitnessSum = 0; //sum of evolutionary fitnesses of all individuals
		int i;
		for(i=0; i<pop; i++){
			fitnessSum += Math.exp(fitnesses[i]); //e^fitness
		}
		List<int[]> selected = new ArrayList<int[]>()
		for(i=0; i<pop; i++){
			double selectProb = Math.exp(fitnesses[i])/fitnessSum; //fitness of individual/sum
			if(randGen.nextDouble() <= selectProb){
				selected.add(samples[i]);
			}
		}
		int selectedArray[] = new int[selected.size()];
		for(i=0; i<selected.size(); i++){
			selectedArray[i] = selected.get(i);
		}
		return selectedArray;
	}

	public void ga(){
		//generate individuals
		int i;
		for(i=0; i<pop; i++){
			genSamples(i);
		}
		//while iteration < iter & not all clauses satisfied
		for(i=0; i<iter; i++){
			//for each individual, generate fitness val (if any satisfy all clauses, return)
			int fitness[] = new int[pop];
			for(int j=0; j<pop; j++){
				fitness[j] = fitness(samples[j]);
				if(fitness[j]==totalClauses){
					System.out.println("Found perfect match!");
					System.out.println(Arrays.toString(samples[j]));
            		// return;
				}
			}

			//use selection to pick individuals for reproduction
			int selected[];
			int selectionMethod = 3;
			switch (select){
				case "ts":
					selected = tsGen(fitness);
					break;
				case "rs":
					selected = rsGen(fitness);
					break;
				case "bs":
					selected = bsGen(fitness);
					break;
				default:
					System.out.println("Selection method must match 'ts', 'rs', or 'bs'");

			}
			//use crossover to breed individuals
			runBreeding(selected);
			//set population to the new individuals
		}

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

	public Runner(){
	}

	public static void main(String[] args){
		// TODO Auto-generated method stub
		 	Runner evolAlg = new Runner();
			// File file = new File(args[0]);
			evolAlg.read(args[0]);
			evolAlg.alg = args[7];
			if(evolAlg.alg.equals("p")){
				evolAlg.pop=Integer.parseInt(args[1]);
				evolAlg.plr=Double.parseDouble(args[2]);
				evolAlg.nlr=Double.parseDouble(args[3]);
				evolAlg.mprob=Double.parseDouble(args[4]);
				evolAlg.mamt=Double.parseDouble(args[5]);
				evolAlg.iter=Integer.parseInt(args[6]);
				evolAlg.samples = new int[evolAlg.pop][evolAlg.vars];
				evolAlg.prob = new double[evolAlg.vars];
				evolAlg.pbil();
			} else if(evolAlg.alg.equals("g")){
				evolAlg.pop=Integer.parseInt(args[1]);
				evolAlg.select=args[2];
				evolAlg.cross=args[3];
				evolAlg.cprob=Double.parseDouble(args[4]);
				evolAlg.mprob=Double.parseDouble(args[5]);
				evolAlg.iter=Integer.parseInt(args[6]);
				evolAlg.samples = new int[evolAlg.pop][evolAlg.vars];
				evolAlg.prob = new double[evolAlg.vars];
				for(int i=0; i<evolAlg.vars; i++){
					evolAlg.prob[i] = 0.5; 
				}
				evolAlg.ga();
			} else {
				System.out.println("Evolutionary algorithm incorrectly entered.");
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
