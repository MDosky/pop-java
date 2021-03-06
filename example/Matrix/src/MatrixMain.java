import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import popjava.annotation.POPClass;
import lib.*;

/**
 * Main class of the Matrix application
 * 
 * @author Beat Wolf
 * 
 */

@POPClass(isDistributable = false)
public class MatrixMain {

	private static final int NB_MAX_MACHINES = 200;
	private static final String MACHINE_LIST = "";//"machines.ip";

	/**
	 * This class holds the description of a machine
	 * 
	 * @author Beat Wolf
	 * 
	 */
	private static class Machine {
		String name;
		int cores;
	}

	/**
	 * Returns the list of all machines defined in the specified file. If the
	 * specified file is not valid or empty, the localhost machine will be
	 * returned with 1 core.
	 * 
	 * @param fileName
	 * @return
	 */
	private static List<Machine> getAvailableMachines(String fileName) {
		List<Machine> machines = new ArrayList<Machine>();
		File file = new File(fileName);

		if (file.exists() && file.canRead()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));

				String line = null;
				while ((line = reader.readLine()) != null) {
					String[] tokens = line.split(" ");
					if (tokens.length == 2) {
						Machine machine = new Machine();
						machine.name = tokens[0];
						machine.cores = Integer.parseInt(tokens[1]);
						machines.add(machine);
					}
				}
			} catch (IOException e) {
				machines.clear();
			}
		}

		if (machines.size() == 0) {
			Machine machine = new Machine();
			machine.name = "localhost";
			machine.cores = 1;
			machines.add(machine);
		}

		return machines;
	}

	public static void main(String[] args) {

		if (args.length < 3) {
			System.err
					.println("Usage: popjrun objmap MatrixMain size divLine divCol [resultFileName]");
			return;
		}

		// Parse parameters
		int Alines, Acols, Bcols;
		Alines = Acols = Bcols = Integer.parseInt(args[0]);
		int divLine = Integer.parseInt(args[1]);
		int divCol = Integer.parseInt(args[2]);

		// Optional output file
		String resultFileName = null;
		if (args.length >= 4) {
			resultFileName = args[3];
		}

		int nbWorker = divLine * divCol;

		// Get the available machines
		List<Machine> machines = getAvailableMachines(MACHINE_LIST);
		int nbOfMachines = machines.size();
		
		System.out.println("mainpopc has started with " + nbWorker + " tasks.");
		System.out.println("Initializing arrays...");
		System.out.println("\nParameters are:");
		System.out.println("Matrix Size=" + Alines + ", Blocs of lines="
				+ divLine + ", Blocs of columns=" + divCol + ", Workers="
				+ nbWorker + "\n");
		
		Matrix2Dlc a = new Matrix2Dlc(Alines,Acols);
		Matrix2Dcl b = new Matrix2Dcl(Acols,Bcols);  
		// Randomly initialize Matrix a and b 
		a.init();
		b.init();
		
		/*a.initInc();
		b.initInc();*/
		
		//Set all to 1
		/*a.fill(1);
		b.fill(1);*/
		
		/*a.display();
		System.out.println("*");
		b.display();
		System.out.println("=");*/

		MatrixWorker [][] mw = new MatrixWorker[divLine][divCol];

		Timer timer = new Timer();
		double initTime, compTime, sendTime;

		System.out.println("\nStarting Matrix multiplication program...");
		timer.start(); // ---------------------------------------------- Start
						// Timer
		// Create the workers
		
		// Actual number of lin/col in a bloc
		int lines = 1;
		int cols = 1; 

		int averline = Alines / divLine; // Average number of lines in a bloc
		int avercol = Bcols / divCol; // Average number of col. in a bloc
		int extraL, extraC; // Remaining lines or column

		extraL = Alines % divLine;
		for (int i = 0; i < divLine; i++) {
			extraC = Bcols % divCol;
			if (extraL > 0) {
				lines = averline + 1;
				extraL--;
			} else {
				lines = averline;
			}
			for (int j = 0; j < divCol; j++) {
				if (extraC > 0) {
					cols = avercol + 1;
					extraC--;
				} else {
					cols = avercol;
				}
				// Create workers
				// printf("Start worker on machine %s\n", (const
				// char*)(*(machine[(j+i*divCol)%nbOfMachines].name)));
				mw[i][j] = new MatrixWorker(j + i * divCol, lines, Acols, cols,
						machines.get((j + i * divCol) % nbOfMachines).name,
						machines.get((j + i * divCol) % nbOfMachines).cores);
			}
		}
		
		// Get time to create all workers
		initTime = timer.elapsed(); // ---------------------- Initialisation Time

		// send the bloc of Matrix A and the Matrix B and launch the computation
		int offsetL, offsetC;
		offsetL = 0;
		for (int i = 0; i < divLine; i++) {
			offsetC = 0;
			for (int j = 0; j < divCol; j++) {
				// Launch computation
				Matrix2Dlc l = a.getLinesBloc(offsetL, lines);
				Matrix2Dcl c = b.getColsBloc(offsetC, cols);				
				mw[i][j].solve(l, c);
				offsetC = offsetC + cols;
			}
			offsetL = offsetL + lines;
		}

		// Get time to send all data to workers and launch computation
		sendTime = timer.elapsed() - initTime; // ------------------ Sending
												// Time
		
		// Create a matrix for getting the results
		Matrix2Dlc res = null;

		// Declare vector to store wait time of workers
		double [][] workerTw = new double[divLine][divCol];
		// Declare vector to store computing time of workers
		double [][] workerTc = new double[divLine][divCol];
		// Get the results and put inside matrix A
		offsetL = 0;
		for (int i = 0; i < divLine; i++) {
			offsetC = 0;
			for (int j = 0; j < divCol; j++) {
				res = mw[i][j].getResult();
				workerTw[i][j] = mw[i][j].getWaitTime();
				workerTc[i][j] = mw[i][j].getComputeTime();
				a.setBloc(offsetL, offsetC, res);
				offsetC = offsetC + res.getCols();
			}
			offsetL = offsetL + res.getLines();
		}

		// Get the elapsed time since all data have been sent (computing time)
		compTime = timer.elapsed() - sendTime - initTime; // --- Computing Time
		timer.stop();

		DecimalFormat df = new DecimalFormat("#.###");
		System.out.println("Times (init, send and computing) = "+df.format(initTime)+
				", "+df.format(sendTime)+", "+df.format(compTime)+" sec\n\n");
		System.out.println("...End of matrix multiplication");
		//res.display(5);
		
		a.display(5);
		
		// Storage of Results and Parametres in the file resultFileName
		if (resultFileName != null) {
			File outFile = new File(resultFileName);
			if (outFile.exists() && outFile.canWrite()) {
				try{
					BufferedWriter out = new BufferedWriter(new FileWriter(outFile));

					out.write(String.format("%d\t%d\t%d\t%g\t%g\t%g",
                Alines, divLine, divCol, initTime, sendTime, compTime));
					for (int i = 0; i < divLine; i++) {
						for (int j = 0; j < divCol; j++) {
							out.write(String.format("\t%g\t%g", workerTw[i][j],workerTc[i][j]));
						}
					}

					out.write("\n");
					out.close();
				}catch(IOException e){
					e.printStackTrace();
				}
				
			} else {
				System.out
						.println("ERROR OPENING result file - no results has been saved !!");
			}
		}

		// Delete the workers
		/*
		 * for (int i=0; i<divLine; i++){ for (int j=0; j<divCol; j++){ if
		 * (mw[i][j]!=NULL) delete mw[i][j]; } }
		 */
	}
}