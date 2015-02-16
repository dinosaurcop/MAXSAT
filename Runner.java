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



public class Runner {

	public static List<int[]> clauses = new ArrayList<int[]>();

	//GA vals
	public String select, cross; //selection and crossover methods
	public double cprob; 	//crossover probability

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

	Random randGen = new Random();
	


	public void pbil(){
		//initialize probability vector to 0.5
		for(int i=0; i<vars; i++){
			prob[i]=0.5;
		}

		int fB=fitness(samples[0]); //fitness of best individual 
		int fW=fitness(samples[0]); //fitness worst individual
		int iB=0; //index of best individual
		int iW=0; //index of worst individual
		int fitness[] = new int[pop];
		while(iter>0){
			//generate samples based on probability vectors

			for(int i=0; i<pop; i++) {
				genSamples(i);
				fitness[i] = fitness(samples[i]);
				if(fitness[i]==totalClauses){
					System.out.println("Found perfect match!");
            		break;
				}
				else if(fitness[i]>fB){ //updates best individual
					fB=fitness[i];
					iB=i;
				}
				else if(fitness[i]<fW){ //updates worst dividual
					fW=fitness[i];
					iW=i;
				}
			}	

			//update probability vector towards best solution
			for(int k=0; k<vars; k++){
				prob[k]=prob[k]*(1-plr)+samples[iB][k]*plr;
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
			int mutDir=1;
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
			iter--;
		}
		// //name of file
		// System.out.printf("The name of the file is: %s", fileName);
		// //number of variables and clauses 
		// System.out.printf("The number of variables is: %d. The number of clauses is: %d", vars, totalClauses);
		// //the number and percentage of clauses of best assignment
		// System.out.printf("The number of satisfied clauses of the best assignment is: %d,", fB);
		// System.out.printf("The percentage of clauses of the best assignment is: ")
		// //assignment of the results
		// System.out.println("The assignment of the clauses is: ");
		// for(int a=; a<vars; a++){
		// 	if(samples[iB][a]>0){System.out.printf("%d ", )}
		// 	else{System.out.printf}
		// }
		// //the iteration during which the best assignment was found
		// System.out.printf("The best assignment was found on the %d iteration", iter);
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
	public int fitness(int []worm){
		int clauseMisses = 0;
		for(int i=0; i<totalClauses; i++){
			//check clause list at i, each in int [] 
			int clause[] = clauses.get(i);
			int valMisses = 0;
			for(int j=0; j<clauses.size  (); j++){
				if(clause[j]<0){ //neg
					int index = Math.abs(clause[j]);
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
			while(true){ //make sure individuals are unique //-------------> ADELA: CHANGED FROM 1 TO TRUE
				parents[0] = randGen.nextInt(reproducers.length);
				parents[1] = randGen.nextInt(reproducers.length);
				parents[0] = reproducers[parents[0]];
				parents[1] = reproducers[parents[1]];
				if(parents[0] != parents[1]){break;}
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
						if(randGen.nextDouble() <= mprob){
							samples[i][j] = rand.nextInt(2*vars + 1) - vars;
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
						if(randGen.nextDouble() <= mprob){
							samples[i][j] = rand.nextInt(2*vars + 1) - vars;
						} else {
							samples[i][j] = oldSamples[parents[p]][j];
						}
					}
				}
			} else {
				//no crossover, pick the first individual
				for(int j=0; j<vars; j++){
					//MUTATION
					if(randGen.nextDouble() <= mprob){
							samples[i][j] = rand.nextInt(2*vars + 1) - vars;
					} else {
						samples[i][j] = oldSamples[parents[0]][j];
					}
				}
			}		

		}
	}



<<<<<<< HEAD
public int partition(int array[], int left, int right) {
      int i=left, j=right;
      int temp;
      int mid = array[(left + right)/2];
     
      while (i<=j) {
            while (array[i]<mid) {
                  i++;
              }
            while (array[j]>mid){
                  j--;
              }
            if (i<=j) {
                  temp = array[i];
                  array[i] = array[j];
                  array[j] = temp;
                  i++;
                  j--;
            }
      }
     
      return i;
}
 
	public void quickSort(int array[], int lower, int higher) {
      int index = partition(array, lower, higher);
      if (lower < index - 1)
            quickSort(array, lower, index - 1);
      if (index < higher)
            quickSort(array, index, higher);
}
=======
	public int[] rsGen(int []fitnesses){return new int[]{0};}

>>>>>>> 72d67cb64f443725bac81eb5b01516992b1bdb22

	public int[] rsGen(int []fitnesses){}

	public int[] tsGen(int []fitnesses){return new int[]{0};}



	public int[] bsGen(int []fitnesses){
		int fitnessSum = 0; //sum of evolutionary fitnesses of all individuals
		int i;
		for(i=0; i<pop; i++){
			fitnessSum += Math.exp(fitnesses[i]); //e^fitness
		}
		List<Integer> selected = new ArrayList<Integer>();
		// int selected[] = new int[pop];
		for(i=0; i<pop; i++){
			double selectProb = Math.exp(fitnesses[i])/fitnessSum; //fitness of individual/sum
			if(randGen.nextDouble() <= selectProb){
				selected.add(i);
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
					selected = bsGen(fitness);
					break;
				default:
					selected = new int[]{0};
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

		Runner evolAlg = new Runner();

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
