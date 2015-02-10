import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;


//
public class Runner {

	//global 2d array of clauses
	public static Vector<String> clauses = new Vector<String>();
	
	//public static int[][] multi = new int[500][100];
	//create list that will hold the arrays of all of the clauses
	//read the file to strings, then create array of the string's length 
	//and add it to the list of clauses
	public static void read(File file) {
		try {
			FileReader fileRead = new FileReader(file);
			BufferedReader buffRead = new BufferedReader(fileRead);
			StringBuffer stringBuff = new StringBuffer();
			String line;
			while ((line = buffRead.readLine()) != null) {
				stringBuff.append(line);
				stringBuff.append("\n");
				clauses.addElement(line);
			}
			fileRead.close();
		//System.out.println(stringBuff.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		for(int i=0; i<clauses.size(); i++){
			System.out.println(clauses.elementAt(i));
		}
		
		System.out.println(clauses.elementAt(0).charAt(2));
		System.out.println(clauses.elementAt(0).charAt(3));
		
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
			File file = new File("t3pm3-5555.spn.cnf");
			read(file);
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
