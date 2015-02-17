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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;



public class Runner {
	static class sampleObj{
		int fitness;
		int index;
		public sampleObj(int i, int fit) {
	        index = i;
	        fitness = fit;
	    }
	}

	public static List<int[]> clauses = new ArrayList<int[]>();

	//GA vals
	public String select, cross; //selection and crossover methods
	public double cprob; 	//crossover probability
	public double startingTemp = 20.0, coolingRate = 2.0;

	//PBIL vals
	public double [] prob;
	public double plr, nlr, mAmt;

	//Shared vals
	public String fileName;
	public String alg;
	public int [][] samples;
	public int pop, iter;
	public double mProb;
	public int vars, totalClauses;
	public long time;

	Random randGen = new Random();
	
	public void printResults(int bestFitness, int bestIndex, int bestIteration){
		double percent = (double) bestFitness / totalClauses *100;
		//name of file
		System.out.printf("The name of the file is: %s %n", fileName);
		//number of variables and clauses 
		System.out.printf("The number of variables is: %d. The number of clauses is: %d %n", vars, totalClauses);
		//the number and percentage of clauses of best assignment
		System.out.printf("The number of satisfied clauses of the best assignment is: %d %n", bestFitness);
		System.out.printf("The percentage of clauses of the best assignment is: %f %n", percent);
		//assignment of the results
		System.out.println("The assignment of the clauses is: ");
		for(int a=0; a<vars; a++){
			if(samples[bestIndex][a]==0){System.out.printf("%d ", -a-1);}
			else{System.out.printf("%d, ", a+1);}
		}
		//the iteration during which the best assignment was found
		System.out.printf("%nThe best assignment was found on the %d iteration %n", bestIteration);

	}

	public void pbil(){
		//initialize probability vector to 0.5
		for(int i=0; i<vars; i++){
			prob[i]=0.5;
		}
		
		int iterBest = -1;
		int bestOverallFitness = -1;
		int bestIndex = -1;

		for(int w=0; w<iter; w++) {
			//generate samples based on probability vectors
				int fB=0; //fitness of best individual 
				int fW=totalClauses; //fitness worst individual
				int iB=-1; //index of best individual
				int iW=-1; //index of worst individual
				int fitness[]=new int[pop];


			for(int i=0; i<pop; i++) {
				genSamples(i);
				fitness[i] = fitness(samples[i]);
				//System.out.printf(" %d", fitness[i]);
				if(fitness[i]==totalClauses){
					System.out.println("Found perfect match!");
					iterBest=iter;
            		break;
				}
				if(fitness[i]>fB){ //updates best individual
					fB=fitness[i];
					if(fB>bestOverallFitness){
						iterBest = w;
					}
					iB=i;
				}
				if(fitness[i]<fW){ //updates worst dividual
					fW=fitness[i];
					iW=i;
				}
			}	

			//update probability vector towards best solution
			for(int k=0; k<vars; k++){
				prob[k]=prob[k]*(1-plr)+samples[iB][k]*plr;
				//System.out.printf(" %f", prob[k]);
				if(prob[k]>=1 || prob[k]<=0){
					System.out.println("Probability vector has exceeded the boundaries of (0,1).");
					break;
				}
			}

			//update probability away from worst solution
			for(int l=0; l<vars; l++){
				if(samples[iB][l] != samples[iW][l]){
					prob[l]=prob[l]*(1-nlr)+samples[iB][l]*nlr;
				}
				if(prob[l]>=1 || prob[l]<=0){
					System.out.println("Probability vector has exceeded the boundaries of (0,1).");
					break;
				}
			}

			//mutate probability vector
			int mutDir;
			for(int q=0; q<vars; q++){
				if(randGen.nextDouble() <= mProb){
					if(randGen.nextDouble() > 0.5){
						mutDir=1;
					}
					else{
						mutDir=0;
					}
					prob[q]=prob[q]*(1-mAmt)+mutDir*mAmt;
					if(prob[q]>=1 || prob[q]<=0){
						System.out.println("Probability vector has exceeded the boundaries of (0,1).");
						break;
					}
				}
			}

			bestOverallFitness = fB;
			bestIndex=iB;
		}
		printResults(bestOverallFitness, bestIndex, iterBest);
		// double percent = (double) bestOverallFitness / totalClauses *100;
		// //name of file
		// System.out.printf("The name of the file is: %s %n", fileName);
		// //number of variables and clauses 
		// System.out.printf("The number of variables is: %d. The number of clauses is: %d %n", vars, totalClauses);
		// //the number and percentage of clauses of best assignment
		// System.out.printf("The number of satisfied clauses of the best assignment is: %d %n", bestOverallFitness);
		// System.out.printf("The percentage of clauses of the best assignment is: %f %n", percent);
		// //assignment of the results
		// System.out.println("The assignment of the clauses is: ");
		// for(int a=0; a<vars; a++){
		// 	if(samples[bestIndex][a]==0){System.out.printf("%d ", -a-1);}
		// 	else{System.out.printf("%d, ", a+1);}
		// }
		// //the iteration during which the best assignment was found
		// System.out.printf("%nThe best assignment was found on the %d iteration %n", iterBest);
	}

	public void genSamples(int i){
		for(int j=0; j<vars; j++){
			if(randGen.nextDouble() <= prob[j]){ //alexi: for ga, the initial prob threshold could vary as well
				samples[i][j]=1;
			} else{
				samples[i][j]=0;
			}
		}
	}



	//evaluate fitness of given individual (int array). Returns fitness score
	//equal to the total number of clauses that it does NOT miss (i.e. hit count)
	//note: we can either check mismatched clauses, or get more elaborate 
	//with how many vals within those clauses are mismatched. Majercick says just
	//count clauses, but I've left it flexible here for now.
	public int fitness(int []individual){
		int clauseMisses = 0;
		for(int i=0; i<totalClauses; i++){
			//check clause list at i, each in int [] 
			int clause[] = clauses.get(i);
			int valMisses = 0;
			for(int j=0; j<clause.length; j++){
				int index = Math.abs(clause[j])-1;
				if(clause[j]<0){ //neg
					if(individual[index]==1){valMisses+=1;}
				} else if(individual[index]==0){valMisses+=1;} //pos
			}
			if (valMisses == clause.length){
				clauseMisses += 1;
			}
		}
		return totalClauses-clauseMisses;
	}



	//breed individuals selected for reproduction
	//accepts array of indexes of selected samples (correspond to individuals in global samples array)
	public void runBreeding(int []reproducers){
		System.out.println(Arrays.toString(reproducers));
		int[][] oldSamples = samples;
		for(int i=0; i<pop; i++){
			//randomly select two individuals
			int parents[] = new int[2];
			boolean picking = true;
			while(picking){ //make sure individuals are unique //-------------> ADELA: CHANGED FROM 1 TO TRUE
				// System.out.printf("parent selection loop\n");
				parents[0] = randGen.nextInt(reproducers.length);
				parents[1] = randGen.nextInt(reproducers.length);
				// System.out.printf("parent 1 index: %d  parent 2 index: %d\n", parents[0], parents[1]);
				parents[0] = reproducers[parents[0]];
				parents[1] = reproducers[parents[1]];
				if(parents[0] != parents[1]){picking=false;}
			}
			//Uniform Crossover
			if(randGen.nextDouble() <= cprob){	//crossover prob determines whether or not crossover occurs
				if(cross.equals("uc")){
					int p = 0; //start on first parent
					for(int j=0; j<vars; j++){
						if(randGen.nextDouble() <= 0.47){ //slightly less than 50% chance of switching
							// p = Math.abs(p-1); //switches p, 0 -> 1 or 1 -> 0
							p ^= 1; //switches p, 0 -> 1 or 1 -> 0
						}
						//MUTATION
						if(randGen.nextDouble() <= mProb){
							samples[i][j] = randGen.nextInt(2*vars + 1) - vars;
						} else {
							samples[i][j] = oldSamples[parents[p]][j];
						}
						
					}
				} else if(cross.equals("1c")){ //Single Point Crossover
					//select point
					int crosspoint = randGen.nextInt(vars);
					int p = 0;
					//create child
					for(int j=0; j<vars; j++){
						if(j==crosspoint){
							p = 1;
						}
						//MUTATION
						if(randGen.nextDouble() <= mProb){
							samples[i][j] = randGen.nextInt(2*vars + 1) - vars;
						} else {
							samples[i][j] = oldSamples[parents[p]][j];
						}
					}
				}
			} else {
				//no crossover, pick the first individual
				for(int j=0; j<vars; j++){
					//MUTATION
					if(randGen.nextDouble() <= mProb){
							samples[i][j] = randGen.nextInt(2*vars + 1) - vars;
					} else {
						samples[i][j] = oldSamples[parents[0]][j];
					}
				}
			}		

		}
	}

	public int[] rsGen(int []fitnesses){
		//sort individuals by fitness (low->high)
		//fitnesses is array of fitness scores based by individual's index,
		//so we need to effectively flip the array so that index = rank, and val = index
		//1. create new array of sampleObjects (contain both the sample and their fitness score)
		sampleObj[] sampleObjects = new sampleObj[pop];
		int rankSum = 0;
		int i;
		for(i=0; i<pop; i++){
			sampleObjects[i] = new sampleObj(i, fitnesses[i]);
			rankSum += i;
		}
		Arrays.sort(sampleObjects);
		System.out.println(Arrays.asList(sampleObjects));
		//2. select individuals with probability rank/ranksum
		List<Integer> selected = new ArrayList<Integer>();
		for(i=0; i<pop; i++){
			if(randGen.nextDouble() <= i/rankSum){
				selected.add(sampleObjects[i].index);
			}
		}
		//convert list to array
		int selectedArray[] = new int[selected.size()];
		for(i=0; i<selected.size(); i++){
			selectedArray[i] = selected.get(i);
		}
		//return
		return selectedArray;

	}


	public int[] tsGen(int []fitnesses){
		int gCount = (int) (pop*0.4) + 2; //ensures at least 2 parents per gen
		int wCount = (int) (gCount * 0.6) + 2;
		// System.out.printf("Running Tournament Selection. gCount: %d and wCount: %d\n", gCount, wCount);
		// System.out.println(Arrays.toString(fitnesses));
		// List<Integer> gladiators = new ArrayList<Integer>();
		Map<Integer, Boolean> gladiators = new HashMap<Integer, Boolean>();
		//randomly select gCount individuals for competition pool (gladiators)
		int i;
		for(i = 0; i<gCount; i++){
			int sel = randGen.nextInt(pop);
			if(gladiators.containsKey(sel)){
				i--;
			} else {
				gladiators.put(sel, true);
			}
		}

		//put gladiators into sorted array (high -> low)
		int[] gArray = new int[gCount];
		i = 0;
		for (Integer key : gladiators.keySet()) {
			// System.out.printf("key:%d\n",key);
			// System.out.println(Arrays.toString(gArray));
			for(int j=0; j<=i; j++){
				if(j==i || i == 0){ //if end of added values reached, or just starting
					gArray[j] = key;
				} else if(fitnesses[gArray[j]] < fitnesses[key]){ //if individual with > fitness reached, insert
					// System.out.printf("superior found. new i and f: %d - %d, prior: %d - %d, at index: %d", key, fitnesses[key], gArray[j], fitnesses[gArray[j]], j);
					for(int k=i; k>j; k--){ //move all smaller individuals up 1
						gArray[k] = gArray[k-1];
					}
					gArray[j] = key;
					// System.out.println(Arrays.toString(gArray));
					break;
				}
			}
		    i++;
		}
		//select first k individuals from the gArray
		int[] selected = new int[wCount];
		for(i=0; i<wCount; i++){
			selected[i] = gArray[i];
		}
		// System.out.println(Arrays.toString(selected));
		return selected;
	}


	//Boltzmann Selection - returns array of index values of samples for reproduction
	public int[] bsGen(int []fitnesses, int iteration){
		double fitnessAvg, fitnessSum = 0; //sum of evolutionary fitnesses of all individuals
		double temp = startingTemp - (iteration*coolingRate);
		if(temp<= 0){temp=coolingRate;}

		int i;
		double[] bsFitnesses = new double[fitnesses.length];
		// temp = 1;
		for(i=0; i<pop; i++){
			bsFitnesses[i] = Math.exp(((double)fitnesses[i] / temp)); //scale fitness to objective state
			fitnessSum += bsFitnesses[i]; //e^fitness
		}
		// fitnessAvg = fitnessSum/pop;
		// fitnessSum = 0;
		// System.out.printf("Fitness avg: %f\n", fitnessAvg);
		// for(i=0; i<pop; i++){
		// 	bsFitnesses[i] = bsFitnesses[i]/fitnessAvg;
		// 	fitnessSum += bsFitnesses[i];
		// 	System.out.printf("expected val for [%d] == %f\n", i, bsFitnesses[i]);
		// }
		List<Integer> selected = new ArrayList<Integer>();
		// int selected[] = new int[pop];
		for(i=0; i<pop; i++){
			double selectProb = 60 * bsFitnesses[i]/fitnessSum; //fitness of individual/sum * constant for scaling
			System.out.printf("prob: %f  fitness: %f   /boltzFSUM: %f\n", selectProb, bsFitnesses[i], fitnessSum);
			if(randGen.nextDouble() <= selectProb){
				System.out.printf("indiv selected, prob: %f  boltzFSUM: %f\n", selectProb, fitnessSum);
				selected.add(i);
			}
		}
		int selectedArray[] = new int[selected.size()];
		for(i=0; i<selectedArray.length; i++){
			selectedArray[i] = selected.get(i);
		}
		return selectedArray;
	}

	public void ga(){
		//generate individuals
		int i, bestIndex=0, bestIteration=0, bestFitness=0;
		for(i=0; i<pop; i++){
			genSamples(i);
		}
		boolean foundPerfect = false;
		//while iteration < iter & not all clauses satisfied
		for(i=0; i<iter; i++){
			System.out.printf("Iteration: %d\n", i);
			//for each individual, generate fitness val (if any satisfy all clauses, return)
			int fitness[] = new int[pop];
			for(int j=0; j<pop; j++){
				fitness[j] = fitness(samples[j]);
				if(fitness[j]==totalClauses){
					System.out.println("Found perfect match!");
					System.out.println(Arrays.toString(samples[j]));
					bestIndex = j;
					bestIteration = i;
					bestFitness = fitness[j];
					foundPerfect = true;
					break;
				} else {
					if(fitness[j]>bestFitness){
						bestIndex=j;
						bestIteration = i;
						bestFitness = fitness[j];
					}
				}
			}
			if(foundPerfect){break;} //if universal match found, break
			//if umatch not found, breed next generation
			//use selection to pick individuals for reproduction
			int selected[]; //array of indexes of selected invididuals in samples array
			int selectionMethod = 3;
			switch (select){
				case "ts":
					selected = tsGen(fitness);
					break;
				case "rs":
					selected = rsGen(fitness);
					break;
				case "bs":
					selected = bsGen(fitness, i);
					break;
				default:
					selected = new int[]{0};
					System.out.println("Selection method must match 'ts', 'rs', or 'bs'");

			}
			System.out.printf("Pop: %d, Selected length: %d\n", pop, selected.length);
			//use crossover to breed individuals
			runBreeding(selected);
		}
		printResults(bestFitness, bestIndex, bestIteration);
		return;
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
	
		// for(int i=0; i<clauses.size(); i++){
		// 	System.out.println(Arrays.toString(clauses.get(i)));
		// }
		
	}



	public Runner(){
	}



	public static void main(String[] args){
		//long start = System.nanoTime();
		Runner evolAlg = new Runner();

		long start = System.currentTimeMillis();
		// process command line arguments
		if (args.length != 8){
	    	System.out.println();
	    	System.out.println("java Runner file individuals selection/learning rate crossover/negative learning rate pC/pM pM/mutation amount generations GA/PBIL");
	   		System.out.println("    individuals  = number of individuals in population (int)");
	    	System.out.println("    selection    = type of selection of breeding pool (string):");
	    	System.out.println("                     ts   = tournament selection - implies ts1");
	    	System.out.println("                            ts1 = same individual cannot compete against self");
	    	System.out.println("                            ts2 = same individual can compete against self");
	    	System.out.println("                     rs   = rank based selection");
	   		System.out.println("                     bs   = Boltzmann selection");
	   		System.out.println("    learning rate = (0,1) (double)");
	    	System.out.println("    crossover    = crossover method (string):");
	    	System.out.println("                     1c   = 1-point crossover");
	    	System.out.println("                     2c   = 2-point crossover");
	    	System.out.println("                     uc   = uniform crossover");
	    	System.out.println("    negative learning rate = (0,1) (double)");
	    	System.out.println("    pC           = crossover probability (double)");
	   	 	System.out.println("    pM           = mutation probability (double)");
	   	 	System.out.println("    mutation amount = (0,1) (double)");
	    	System.out.println("    generations  = max number of generations to run (int)");
	    	System.out.println();
	    	System.exit(1); // prevent the program from continuing without the correct inputs
		}

			evolAlg.fileName=args[0];
			evolAlg.read(evolAlg.fileName); //gets vars, totalClauses, and builds clauses list (of arrays)
			evolAlg.alg = args[7];
			if(evolAlg.alg.equals("p")){
				evolAlg.pop=Integer.parseInt(args[1]);
				evolAlg.plr=Double.parseDouble(args[2]);
				evolAlg.nlr=Double.parseDouble(args[3]);
				evolAlg.mProb=Double.parseDouble(args[4]);
				evolAlg.mAmt=Double.parseDouble(args[5]);
				evolAlg.iter=Integer.parseInt(args[6]);
				evolAlg.samples = new int[evolAlg.pop][evolAlg.vars];
				evolAlg.prob = new double[evolAlg.vars];
				evolAlg.pbil();
			} else if(evolAlg.alg.equals("g")){
				evolAlg.pop=Integer.parseInt(args[1]);
				evolAlg.select=args[2];
				evolAlg.cross=args[3];
				evolAlg.cprob=Double.parseDouble(args[4]);
				evolAlg.mProb=Double.parseDouble(args[5]);
				evolAlg.iter=Integer.parseInt(args[6]);
				evolAlg.samples = new int[evolAlg.pop][evolAlg.vars];
				evolAlg.prob = new double[evolAlg.vars];
				for(int i=0; i<evolAlg.vars; i++){ 
					evolAlg.prob[i] = 0.5; 
				}
				evolAlg.ga();
			} else {
				System.out.println("Evolutionary algorithm incorrectly entered.");
				if(evolAlg.plr<=0 || evolAlg.plr>=1 || evolAlg.nlr<=0 || evolAlg.nlr>=1 ){
					System.out.println("The learning rate and negative learning rate should have a probability in (0,1)");
				}
				System.exit(1);
			}

			evolAlg.time = System.currentTimeMillis() - start;
			System.out.printf("The program ran for %d milliseconds.%n", evolAlg.time);
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
