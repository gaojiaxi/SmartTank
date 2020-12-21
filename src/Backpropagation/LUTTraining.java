package Backpropagation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;

public class LUTTraining {
	private static final int[] HEADING = { 0, 1, 2, 3 };
	private static final int[] X_COORDINATE = { 0, 1, 2, 3, 4 };
	private static final int[] Y_COORDINATE = { 0, 1, 2, 3, 4 };
	private static final int[] ACTIONS = { 0, 1 };
	private static double[][] X;
	private static double[] argValue;
	private static Backpropagation bp;

	public static void main(String[] args) throws IOException {
		int argNumInputs = 4;
		int argNumHidden = 40;
		double argLearningRate = 0.1;
		double argMomentumTerm = 0.1;
		double argA = -1;
		double argB = 1;
		bp = new Backpropagation(argNumInputs, argNumHidden, argLearningRate, argMomentumTerm, argA, argB);
		int epochs = 0;

		double trainingError = 0;
		bp.initializeWeights();
		initializeInputVectors();
		load("E:/workspace/EECE592/bin/reinforcementLearning/LearningRobot.data/lut.txt");
		do {
			trainingError = 0;
			epochs++;
			for (int j = 0; j < X.length; j++) {
				trainingError += bp.train(X[j], argValue[j]);
			}
//System.out.println(epochs+" "+trainingError);
			System.out.println("After " + epochs + " epochs, the totalerror is " + trainingError + ".");
		} while (trainingError >= 0.001);
		System.out.println(Arrays.toString(bp.argWeightsHiddenToOutput));
		for (int i = 0; i < bp.argNumHidden; i++) {
			System.out.println(Arrays.toString(bp.argWeightsInputToHidden[i]));
		}
		save("E:/workspace/EECE592/bin/reinforcementLearning/LearningRobot.data/weights.txt");
		for (int i = 0; i < X.length; i++) {
			System.out.println(bp.outputFor(X[i]) * 100);
		}
	}

	public static void initializeInputVectors() {
		int index;
		int vectorsNum = HEADING.length * X_COORDINATE.length * Y_COORDINATE.length * ACTIONS.length;
		X = new double[vectorsNum][4];
		for (int i = 0; i < HEADING.length; i++) {
			for (int j = 0; j < X_COORDINATE.length; j++) {
				for (int k = 0; k < Y_COORDINATE.length; k++) {
					for (int l = 0; l < ACTIONS.length; l++) {

						index = i * X_COORDINATE.length * Y_COORDINATE.length * ACTIONS.length +

								j * Y_COORDINATE.length * ACTIONS.length + k * ACTIONS.length + l;
						X[index][0] = HEADING[i];
						X[index][1] = X_COORDINATE[j];
						X[index][2] = Y_COORDINATE[k];
						X[index][3] = ACTIONS[l];
					}
				}
			}
		}
	}

	public static void load(String argFileName) throws IOException {
		int numStatesActions = HEADING.length * X_COORDINATE.length * Y_COORDINATE.length * ACTIONS.length;
		argValue = new double[numStatesActions];
		File file = new File(argFileName);
		FileInputStream inputFile = new FileInputStream(file);
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputFile));
		int numStatesActionsInFile = Integer.valueOf(inputReader.readLine());
		if (numStatesActionsInFile != numStatesActions) {
			System.out.println("*** Number of pairs of states and actions in file is " + numStatesActionsInFile
					+ ", Expected" + numStatesActions);
		} else {
			for (int i = 0; i < argValue.length; i++) {
				argValue[i] = Double.parseDouble(inputReader.readLine()) / 100;
			}
		}
		inputReader.close();
	}

	public static void save(String argFileName) {
		File file = new File(argFileName);
		PrintStream saveFile = null;
		try {
			saveFile = new PrintStream(file);
		} catch (IOException e) {

			System.out.println("*** Could not create output stream for LUT save file.");
		}
		for (int i = 0; i < bp.argNumHidden + 1; i++) {
			saveFile.println(bp.argWeightsHiddenToOutput[i]);
		}
		for (int i = 0; i < bp.argNumHidden; i++) {
			for (int j = 0; j < bp.argNumInputs + 1; j++) {
				saveFile.println(bp.argWeightsInputToHidden[i][j]);
			}
		}
		saveFile.close();
	}
}