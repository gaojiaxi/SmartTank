package reinforcementLearning;

import Interfaces.NeuralNetInterface;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Random;
import robocode.AdvancedRobot;
import robocode.BattleEndedEvent;
import robocode.DeathEvent;
import robocode.RobocodeFileOutputStream;
import robocode.RobocodeFileWriter;
import robocode.RoundEndedEvent;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;

public class SmartBot extends AdvancedRobot implements NeuralNetInterface {
	private static final double LEARNING_RATE = 0.2;
	private static final double DISCOUNT_FACTOR = 0.8;
	private static final double PROBABILITY_EPSILON = 0;
	private static final int[] ACTIONS = { 0, 1 };

	private static int winNum = 0;
	private double winRate;
	private static int play = 1;
	private static double accumulatedReward = 0;
	private double averageReward;
	private double immediateReward;
	private int selectedActionIndex;
	private long currentPlay;
	private double lastMyEnergy = 100;
	private double lastEnemyEnergy = 100;
	private double currentEnemyEnergy;
	private static int argNumInputs = 4;
	private static int argNumHidden = 40;
	private double argLearningRate = 0.1;
	private double argMomentumTerm = 0.1;
	private double argA = -1;
	private double argB = 1;
	private static double[] argWeightsHiddenToOutput;
	private static double[][] argWeightsInputToHidden;
	private static double[] argWeightsChangeHiddenToOutput;
	private static double[][] argWeightsChangeInputToHidden;
	private double[] hiddenOutputs;
	private static double[] currentInputVector;
	private static double[] optimalInputVector;
	private static double[] lastInputVector;
	private double targetValue;
	private double[] lastOutput;
	static {
		currentInputVector = new double[4];
		optimalInputVector = new double[4];
		lastInputVector = new double[4];
		argWeightsHiddenToOutput = new double[argNumHidden + 1];
		argWeightsInputToHidden = new double[argNumHidden][argNumInputs + 1];
		argWeightsChangeHiddenToOutput = new double[argNumHidden + 1];
		argWeightsChangeInputToHidden = new double[argNumHidden][argNumInputs + 1];
	}

	@Override

	public void run() {
		initializeWeights();
		setAdjustRadarForRobotTurn(true);
		while (true) {
			if (currentPlay == 0) {
				double heading = getHeading();
				if (heading < 180) {
					turnLeft(heading);
				} else {
					turnRight(360 - heading);
				}
				ahead(90);
			}

			setTurnRadarRight(45);
			execute();
		}
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		super.onScannedRobot(event);
		currentEnemyEnergy = event.getEnergy();
		if (currentPlay == 0) {
			selectAction();
			executeAction();
		} else {
			selectAction();
			updateQValue();
			train(lastInputVector, targetValue);
			saveError("error0.txt", 0);
			saveError("error1.txt", 1);
			executeAction();
		}
		/*
		 * if(play%2000==0){ averageReward=accumulatedReward/play;
		 * saveAverageReward("accReward.txt"); }
		 */
		play++;
		currentPlay++;
	}

	private void updateQValue() {

		getImmediateReward();
		// on-policy
		targetValue = (1 - LEARNING_RATE) * outputFor(lastInputVector)
				+ LEARNING_RATE * (immediateReward / 100 + DISCOUNT_FACTOR * outputFor(currentInputVector));
		// off-policy
		/*
		 * targetValue=(1-LEARNING_RATE)*outputFor(lastInputVector)+
		 * LEARNING_RATE*(immediateReward+DISCOUNT_FACTOR*outputFor(optimalInput
		 * Vector));
		 */
		// without immediate reward
		/*
		 * targetValue=(1-LEARNING_RATE)*outputFor(lastInputVector)+
		 * LEARNING_RATE*(DISCOUNT_FACTOR*outputFor(currentInputVector));
		 */
	}

	private void selectAction() {
		double maximumQValue = Double.NEGATIVE_INFINITY;
		Random random = new Random();
		selectedActionIndex = random.nextInt(ACTIONS.length);
		int optimalActionIndex = 0;
		for (int i = 0; i < ACTIONS.length; i++) {
			if (outputFor(getInputVector(i)) > maximumQValue) {
				maximumQValue = outputFor(getInputVector(i));
				optimalActionIndex = i;
			}
		}
		double randomNum = Math.random();
		if (randomNum >= PROBABILITY_EPSILON) {
			selectedActionIndex = optimalActionIndex;
		}
		currentInputVector = getInputVector(selectedActionIndex);
		optimalInputVector = getInputVector(optimalActionIndex);
	}

	private void executeAction() {
		switch (selectedActionIndex) {
		case 0:
			ahead(90);
			break;
		case 1:
			turnRight(90);
			ahead(90);

			break;
		}
		lastInputVector = currentInputVector;
	}

	private double[] getInputVector(int action) {
		double[] inputVector = new double[4];
		inputVector[0] = getHeadingCode();
		inputVector[1] = getCoordinateCode(getX());
		inputVector[2] = getCoordinateCode(getY());
		inputVector[3] = action;
		return inputVector;
	}

	private void getImmediateReward() {
		double currentMyEnergy = getEnergy();
		double myEnergyChange = currentMyEnergy - lastMyEnergy;
		double enemyEnergyChange = currentEnemyEnergy - lastEnemyEnergy;
		immediateReward = myEnergyChange - enemyEnergyChange;
		lastMyEnergy = currentMyEnergy;
		lastEnemyEnergy = currentEnemyEnergy;
		accumulatedReward += immediateReward;
	}

	private double getHeadingCode() {
		double heading = getHeading();
		double headingCode = (int) (heading + 45) / 90 % 4;
		return headingCode;
	}

	private double getCoordinateCode(double coordinate) {
		double coordinateCode;
		coordinateCode = coordinate / 160;
		if (coordinateCode > 4) {
			coordinateCode = 4;
		}
		return coordinateCode;
	}

	/*
	 * @Override public void onWin(WinEvent event) { winNum++; int
	 * roundNum=getRoundNum()+1; winRate=(double)winNum/roundNum;
	 * 
	 * saveWinRate("winRate.txt"); }
	 * 
	 * @Override public void onDeath(DeathEvent event) { int
	 * roundNum=getRoundNum()+1; winRate=(double)winNum/roundNum;
	 * saveWinRate("winRate.txt"); }
	 */
	@Override
	public void onRoundEnded(RoundEndedEvent event) {
		getImmediateReward();
		targetValue = (1 - LEARNING_RATE) * outputFor(lastInputVector) + LEARNING_RATE * immediateReward;
//save("weights.txt");
	}

	@Override
	public void onBattleEnded(BattleEndedEvent event) {
		save("weights.txt");
	}

	@Override
	public double outputFor(double[] X) {
		double outputValue = 0.0;
		hiddenOutputs = new double[argNumHidden];
//calculate the output of every hidden unit
		for (int i = 0; i < argNumHidden; i++) {
			for (int j = 0; j < argNumInputs + 1; j++) {
				if (j == argNumInputs) {
					hiddenOutputs[i] += argWeightsInputToHidden[i][j];
				} else {

					hiddenOutputs[i] += argWeightsInputToHidden[i][j] * X[j];

				}
			}
			hiddenOutputs[i] = customSigmoid(hiddenOutputs[i]);
		}
		//calculate the output of the network
		for (int i = 0; i < argNumHidden + 1; i++) {
			if (i == argNumHidden) {
				outputValue += argWeightsHiddenToOutput[i];
			} else {

				outputValue += argWeightsHiddenToOutput[i] * hiddenOutputs[i];
			}
		}
		outputValue = customSigmoid(outputValue);
		return outputValue;
	}

	@Override
	public double train(double[] X, double argValue) {
		double outputValue = outputFor(X);

		double trainingError = 0.5 * (argValue - outputValue) * (argValue - outputValue);

		double

		outputErrorTerm = derivativeOfCustomSigmoid(outputValue) * (argValue - outputValue);

		double hiddenErrorTerm[] = new double[argNumHidden];
		double[] lastWeightsChangeHiddenToOutput = new double[argNumHidden + 1];
		double[][] lastWeightsChangeInputToHidden = new double[argNumHidden][argNumInputs + 1];
		for (int i = 0; i < argNumHidden; i++) {
			hiddenErrorTerm[i] = derivativeOfCustomSigmoid(hiddenOutputs[i]) * argWeightsHiddenToOutput[i]
					* outputErrorTerm;
		}
		for (int i = 0; i < argNumHidden + 1; i++) {
			lastWeightsChangeHiddenToOutput[i] = argWeightsChangeHiddenToOutput[i];
		}
		for (int i = 0; i < argNumHidden; i++) {
			for (int j = 0; j < argNumInputs + 1; j++) {
				lastWeightsChangeInputToHidden[i][j] = argWeightsChangeInputToHidden[i][j];
			}
		}
		for (int i = 0; i < argNumHidden + 1; i++) {
			if (i == argNumHidden) {
				argWeightsChangeHiddenToOutput[i] = argLearningRate * outputErrorTerm
						+ argMomentumTerm * lastWeightsChangeHiddenToOutput[i];

			} else {

				argWeightsChangeHiddenToOutput[i] = argLearningRate * outputErrorTerm * hiddenOutputs[i]
						+ argMomentumTerm * lastWeightsChangeHiddenToOutput[i];

			}
		}
		for (int i = 0; i < argNumHidden; i++) {
			for (int j = 0; j < argNumInputs + 1; j++) {
				if (j == argNumInputs) {

					argWeightsChangeInputToHidden[i][j] = argLearningRate * hiddenErrorTerm[i]
							+ argMomentumTerm * lastWeightsChangeInputToHidden[i][j];

				} else {

					argWeightsChangeInputToHidden[i][j] = argLearningRate * hiddenErrorTerm[i] * X[j]
							+ argMomentumTerm * lastWeightsChangeInputToHidden[i][j];

				}
			}
		}
		for (int i = 0; i < argNumHidden + 1; i++) {
			argWeightsHiddenToOutput[i] += argWeightsChangeHiddenToOutput[i];
		}
		for (int i = 0; i < argNumHidden; i++) {
			for (int j = 0; j < argNumInputs + 1; j++) {
				argWeightsInputToHidden[i][j] += argWeightsChangeInputToHidden[i][j];

			}
		}
		return trainingError;
	}

	private double calcError(int vectorNum) {
		double[] inputVector;
		if (vectorNum == 0) {
			inputVector = new double[] { 1, 1, 1, 0 };
		} else {
			inputVector = new double[] { 2, 2, 2, 1 };
		}
		double thisOutput = outputFor(inputVector);
		double error = thisOutput - lastOutput[vectorNum];
		lastOutput[vectorNum] = thisOutput;
		return error;
	}

	private void saveError(String argFileName, int vectorNum) {
		File file = getDataFile(argFileName);
		try {
			RobocodeFileWriter fileWriter = new RobocodeFileWriter(file.getAbsolutePath(), true);
			fileWriter.write(String.valueOf(calcError(vectorNum)) + "\r\n");
			fileWriter.close();
		} catch (IOException e) {
			System.out.println("*** Could not create output stream for error save file.");
		}
	}

	private void saveWinRate(String argFileName) {
		File file = getDataFile(argFileName);
		try {
			RobocodeFileWriter fileWriter = new RobocodeFileWriter(file.getAbsolutePath(), true);

			fileWriter.write(String.valueOf(winRate) + "\r\n");
			fileWriter.close();
		} catch (IOException e) {
			System.out.println("*** Could not create output stream for winRate save file.");
		}
	}

	private void saveAverageReward(String argFileName) {
		File file = getDataFile(argFileName);
		try {
			RobocodeFileWriter fileWriter = new RobocodeFileWriter(file.getAbsolutePath(), true);

			fileWriter.write(String.valueOf(averageReward) + "\r\n");
			fileWriter.close();
		} catch (IOException e) {
			System.out.println("*** Could not create output stream for averageReward save file.");
		}
	}

	public void save(String argFileName) {
		File file = getDataFile(argFileName);
		PrintStream saveFile = null;
		try {
			saveFile = new PrintStream(new

			RobocodeFileOutputStream(file));
		} catch (IOException e) {
			System.out.println("*** Could not create output stream for LUT save file.");
		}
		for (int i = 0; i < argNumHidden + 1; i++) {
			saveFile.println(argWeightsHiddenToOutput[i]);
		}
		for (int i = 0; i < argNumHidden; i++) {
			for (int j = 0; j < argNumInputs + 1; j++) {
				saveFile.println(argWeightsInputToHidden[i][j]);
			}
		}
		saveFile.close();
	}

	@Override
	public void load(String argFileName) throws IOException {
		File file = getDataFile(argFileName);
		FileInputStream inputFile = new FileInputStream(file);
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputFile));
		for (int i = 0; i < argNumHidden + 1; i++) {
			argWeightsHiddenToOutput[i] = Double.parseDouble(inputReader.readLine());
		}
		for (int i = 0; i < argNumHidden; i++) {
			for (int j = 0; j < argNumInputs + 1; j++) {
				argWeightsInputToHidden[i][j] = Double.parseDouble(inputReader.readLine());
			}
		}
		inputReader.close();
	}

	@Override

	public void save(File argFile) {
	}

	@Override
	public double sigmoid(double x) {
		double result = 1 / (1 + Math.exp(-x));
		return result;
	}

	@Override
	public double customSigmoid(double x) {
		double result = (argB - argA) * sigmoid(x) + argA;
		return result;
	}

	public double derivativeOfCustomSigmoid(double y) {
		double result = (y - argA) * (argB - y) / (argB - argA);
		return result;
	}

	@Override
	public void initializeWeights() {
		if (play == 1) {
			try {
				load("weights.txt");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		lastOutput = new double[2];
		lastOutput[0] = outputFor(new double[] { 1, 1, 1, 0 });
		lastOutput[1] = outputFor(new double[] { 2, 2, 2, 1 });
	}

	@Override
	public void zeroWeights() {
		// TODO Auto-generated method stub
		
	}
}