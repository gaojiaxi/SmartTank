package rllut;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Random;
import robocode.AdvancedRobot;
import robocode.RobocodeFileOutputStream;
import robocode.RobocodeFileWriter;
import robocode.RoundEndedEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import Interfaces.LUTInterface;

public class MyRobot extends AdvancedRobot implements LUTInterface {
	private static final double LEARNING_RATE = 0.4;
	private static final double DISCOUNT_FACTOR = 0.9;
	private static final double PROBABILITY_EPSILON = 0;
	private static final int[] HEADING = { 0, 1, 2, 3 };
	private static final int[] LOCATION = { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
	private static final int[] BEARING = { 0, 1, 2, 3 };
	private static final int[] DISTANCE = { 0, 1, 2, 3, 4 };
	private static final int[] ACTIONS = { 0, 1, 2 };
	private static int turn = 1;
	private static double accumulatedReward = 0;
	private double[][] qValueTable;
	private int[] currentState;
	private double immediateReward;
	private int lastStateIndex;
	private int currentStateIndex;
	private int lastActionIndex;
	private int selectedActionIndex;
	private int optimalActionIndex;
	private long currentTurn;
	private Enemy enemy;
	private int direction = 1;

	private double lastMyEnergy = 100;
	private double lastEnemyEnergy = 100;

	@Override
	public void run() {
		super.run();
		initialiseLUT();
		enemy = new Enemy();
		//setAdjustGunForRobotTurn(true);
		//setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		while (true) {
			if (enemy.name == null) {

				setTurnRadarRightRadians(Math.PI / 2);
			}

			execute();
		}
	}

	public void gravMovement() {
		/*
		 * double heading=getHeadingRadians(); double distance=enemy.distance; double
		 * absBearingRadians=enemy.absBearingRadians; double
		 * xForce=Math.sin(absBearingRadians)/(distance*distance); double
		 * yForce=Math.cos(absBearingRadians)/(distance*distance); double
		 * angle=Math.atan2(xForce, yForce); if(Math.abs(angle-heading)<Math.PI/2){
		 * setTurnRightRadians(Utils.normalRelativeAngle(angle-heading));
		 * //setAhead(Double.POSITIVE_INFINITY); setAhead(50); } else {
		 * setTurnRightRadians(Utils.normalRelativeAngle(angle+Math.PI-heading));
		 * //setAhead(Double.NEGATIVE_INFINITY); setBack(50); }
		 */
		double xforce = 0;
		double yforce = 0;
		double distance = enemy.distance;
		double force = -1000 / Math.pow(distance, 2);
//Find the bearing from the point to us
		double ang = normaliseBearing(Math.PI / 2 - Math.atan2(getY() - enemy.y, getX() - enemy.x));
//Add the components of this force to the total force in their respective directions

		xforce -= Math.sin(ang) * force;
		yforce -= Math.cos(ang) * force;
		/**
		 * The following four lines add wall avoidance. They will only affect us if the
		 * bot is close to the walls due to the force from the walls decreasing at a
		 * power 3.
		 **/
		xforce += 5000 / Math.pow(getRange(getX(), getY(), getBattleFieldWidth(), getY()), 3);
		xforce -= 5000 / Math.pow(getRange(getX(), getY(), 0, getY()), 3);
		yforce += 5000 / Math.pow(getRange(getX(), getY(), getX(), getBattleFieldHeight()), 3);
		yforce -= 5000 / Math.pow(getRange(getX(), getY(), getX(), 0), 3);
		//Move in the direction of our resolved force.
		goTo(getX() - xforce, getY() - yforce);
	}

	public void antiGravMovement() {
		/*
		 * double heading=getHeadingRadians(); 
		 * double distance=enemy.distance; 
		 * double absBearingRadians=enemy.absBearingRadians; 
		 * double xForce=-1000*Math.sin(absBearingRadians)/(distance*distance); 
		 * double yForce=-1000*Math.cos(absBearingRadians)/(distance*distance); 
		 * xForce += 5000/Math.pow(getRange(getX(), getY(), getBattleFieldWidth(), getY()), 3);
		 * xForce -= 5000/Math.pow(getRange(getX(), getY(), 0, getY()), 3); 
		 * yForce += 5000/Math.pow(getRange(getX(), getY(), getX(), getBattleFieldHeight()), 3);
		 * yForce -= 5000/Math.pow(getRange(getX(), getY(), getX(), 0), 3); 
		 * double angle=Math.atan2(xForce, yForce); 
		 * if(Math.abs(angle-heading)<Math.PI/2){
		 * 		setTurnRightRadians(Utils.normalRelativeAngle(angle-heading));
		 * 		//setAhead(Double.POSITIVE_INFINITY); 
		 * 		setAhead(50); 
		 * } else {
		 * 		setTurnRightRadians(Utils.normalRelativeAngle(angle+Math.PI-heading));
		 * 		//setAhead(Double.NEGATIVE_INFINITY); 
		 * 		setBack(50); 
		 * }
		 */
		double xforce = 0;
		double yforce = 0;
		double distance = enemy.distance;
		double force = -1000 / Math.pow(distance, 2);
		//Find the bearing from the point to us
		double ang = normaliseBearing(Math.PI / 2 - Math.atan2(getY() - enemy.y, getX() - enemy.x));

		//Add the components of this force to the total force in their respective directions
		xforce += Math.sin(ang) * force;
		yforce += Math.cos(ang) * force;
		/**
		 * The following four lines add wall avoidance. They will only affect us if the
		 * bot is close to the walls due to the force from the walls decreasing at a
		 * power 3.
		 **/
		xforce += 5000 / Math.pow(getRange(getX(), getY(), getBattleFieldWidth(), getY()), 3);
		xforce -= 5000 / Math.pow(getRange(getX(), getY(), 0, getY()), 3);
		yforce += 5000 / Math.pow(getRange(getX(), getY(), getX(), getBattleFieldHeight()), 3);
		yforce -= 5000 / Math.pow(getRange(getX(), getY(), getX(), 0), 3);
//Move in the direction of our resolved force.
		goTo(getX() - xforce, getY() - yforce);
	}

	public void pendulumMovement(int frequency) {
		double headingTurnAngle = Math.PI / 2 + enemy.bearingRadians;
		if (currentTurn % frequency == 0) {
			direction *= -1;
		}
		setAhead(300 * direction);
		setTurnRightRadians(headingTurnAngle);
	}

	public void radarLockEnemy() {

		double radarTurnAngle = Utils.normalRelativeAngle(enemy.absBearingRadians - getRadarHeadingRadians());

		setTurnRadarRightRadians(radarTurnAngle * 1.5);
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		super.onScannedRobot(event);
		enemy.update(event);
		if (currentTurn == 0) {
			getCurrentState();
			selectAction();
			executeAction();
		} else {
			getCurrentState();
			selectAction();
			updateQValue();
			executeAction();
		}

		radarLockEnemy();
		if (turn % 200000 == 0) {
			saveAccumulatedReward("accReward.txt");
			accumulatedReward = 0;
		}
		turn++;
		currentTurn++;
	}

	private void updateQValue(){
		getImmediateReward();
		//on-policy
		qValueTable[lastStateIndex][lastActionIndex]=
		(1-LEARNING_RATE)*qValueTable[lastStateIndex][lastActionIndex]+
		LEARNING_RATE*(immediateReward+DISCOUNT_FACTOR*qValueTable[currentStateIndex][selectedActionIndex]);
		//off-policy
		/* qValueTable[lastStateIndex][lastActionIndex]=
		
		(1-LEARNING_RATE)*qValueTable[lastStateIndex][lastActionIndex]+
		
		LEARNING_RATE*(immediateReward+DISCOUNT_FACTOR*qValueTable[currentStateIndex][optimalAction
		Index]);*/
		//without immediate reward
		/* qValueTable[lastStateIndex][lastActionIndex]=
		
		(1-LEARNING_RATE)*qValueTable[lastStateIndex][lastActionIndex]+
		
		LEARNING_RATE*(DISCOUNT_FACTOR*qValueTable[currentStateIndex][selectedActionIndex]);*/
		}

	private void selectAction() {
		double maximumQValue = Double.NEGATIVE_INFINITY;
		Random random = new Random();
		selectedActionIndex = random.nextInt(ACTIONS.length);
		currentStateIndex = indexFor(currentState);
		for (int i = 0; i < ACTIONS.length; i++) {
			if (qValueTable[currentStateIndex][i] > maximumQValue) {
				maximumQValue = qValueTable[currentStateIndex][i];
				optimalActionIndex = i;
			}
		}
		double randomNum = Math.random();
		if (randomNum >= PROBABILITY_EPSILON) {
			selectedActionIndex = optimalActionIndex;

		}
	}

	private void executeAction() {
		switch (selectedActionIndex) {
		case 0:
			gravMovement();
			break;
		case 1:
			antiGravMovement();
			break;
		case 2:
			pendulumMovement(40);
			break;
		}
		lastStateIndex = currentStateIndex;
		lastActionIndex = selectedActionIndex;
	}

	private void getCurrentState() {
		currentState[0] = getHeadingCode();
		currentState[1] = getLocationCode();
		currentState[2] = getBearingCode();
		currentState[3] = getDistanceCode();
	}

	private void getImmediateReward() {
		double currentMyEnergy = getEnergy();
		double currentEnemyEnergy = enemy.energy;
		double myEnergyChange = currentMyEnergy - lastMyEnergy;
		double enemyEnergyChange = currentEnemyEnergy - lastEnemyEnergy;
		immediateReward = myEnergyChange - enemyEnergyChange;
		lastMyEnergy = currentMyEnergy;
		lastEnemyEnergy = currentEnemyEnergy;
		accumulatedReward += immediateReward;
	}

	private int getHeadingCode() {
		double heading = getHeading();
		int headingCode = (int) (heading + 45) / 90 % 4;
		return headingCode;
	}

	private int getLocationCode() {

		int locationCode;
		double x = getX();
		double y = getY();
		double width = getBattleFieldWidth();
		double height = getBattleFieldHeight();
		if (x <= 100 && y >= height - 100) {
			locationCode = 0;
		} else if (x > 100 && x < width - 100 && y >= height - 100) {
			locationCode = 1;
		} else if (x >= width - 100 && y >= height - 100) {
			locationCode = 2;
		} else if (x >= width - 100 && y > 100 && y < height - 100) {
			locationCode = 3;
		} else if (x >= width - 100 && y <= 100) {
			locationCode = 4;
		} else if (x > 100 && x < width - 100 && y <= 100) {
			locationCode = 5;
		} else if (x <= 100 && y <= 100) {
			locationCode = 6;
		} else if (x <= 100 && y > 100 && y < height - 100) {
			locationCode = 7;
		} else {
			locationCode = 8;
		}
		return locationCode;
	}

	private int getBearingCode() {
		int bearingCode;
		double bearingRadians = enemy.bearingRadians;
		if (bearingRadians < 0) {
			bearingRadians += 2 * Math.PI;
		}
		bearingCode = (int) ((bearingRadians + Math.PI / 4) / (Math.PI / 2)) % 4;
		return bearingCode;
	}

	private int getDistanceCode() {
		int distanceCode;
		double distance = enemy.distance;
		if (distance < 100) {
			distanceCode = 0;
		} else if (distance >= 100 && distance < 200) {
			distanceCode = 1;

		} else if (distance >= 200 && distance < 300) {
			distanceCode = 2;
		} else if (distance >= 300 && distance < 400) {
			distanceCode = 3;
		} else {
			distanceCode = 4;
		}
		return distanceCode;
	}

	@Override
	public void onRoundEnded(RoundEndedEvent event) {
		getImmediateReward();
		qValueTable[lastStateIndex][lastActionIndex] = (1 - LEARNING_RATE)
				* qValueTable[lastStateIndex][lastActionIndex] + LEARNING_RATE * immediateReward;
		save("lut.txt");
	}

	@Override
	public double outputFor(double[] X) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double train(double[] X, double argValue) {
		// TODO Auto-generated method stub
		return 0;
	}

	private void saveAccumulatedReward(String argFileName) {
		File file = getDataFile(argFileName);
		try {
			RobocodeFileWriter fileWriter = new RobocodeFileWriter(file.getAbsolutePath(), true);

			fileWriter.write(String.valueOf(accumulatedReward) + "\r\n");
			fileWriter.close();
		} catch (IOException e) {
			System.out.println("*** Could not create output stream for accumulatedReward save file.");
		}
	}

	@Override
	public void save(String argFileName) {
		File file = getDataFile(argFileName);
		PrintStream saveFile = null;
		int numStatesByActions = qValueTable.length * ACTIONS.length;
		try {
			saveFile = new PrintStream(new RobocodeFileOutputStream(file));
		} catch (IOException e) {
			System.out.println("*** Could not create output stream for LUT save file.");
		}
		saveFile.println(numStatesByActions);
		for (int i = 0; i < qValueTable.length; i++) {
			for (int j = 0; j < ACTIONS.length; j++) {
				saveFile.println(qValueTable[i][j]);
			}
		}
		saveFile.close();
	}

	@Override
	public void load(String argFileName) throws IOException {
		File file = getDataFile(argFileName);
		FileInputStream inputFile = new FileInputStream(file);
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputFile));
		int numStatesActionsInFile = Integer.valueOf(inputReader.readLine());
		int numStatesActions = qValueTable.length * ACTIONS.length;
		if (numStatesActionsInFile != numStatesActions) {
			System.out.println("*** Number of pairs of states and actions in file is " + numStatesActionsInFile
					+ ", Expected " + numStatesActions);
		} else {
			for (int i = 0; i < qValueTable.length; i++) {
				for (int j = 0; j < ACTIONS.length; j++) {
					qValueTable[i][j] = Double.parseDouble(inputReader.readLine());
				}
			}
		}
		inputReader.close();
	}

	@Override
	public void initialiseLUT() {
		int statesLength = HEADING.length * LOCATION.length * BEARING.length * DISTANCE.length;

		qValueTable = new double[statesLength][ACTIONS.length];
		File file = getDataFile("lut.txt");
		if (file.length() != 0) {
			try {

				load("lut.txt");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			for (int i = 0; i < statesLength; i++) {
				for (int j = 0; j < ACTIONS.length; j++) {
					qValueTable[i][j] = 0;
				}
			}
		}
		currentState = new int[5];
	}

	@Override
	public int indexFor(int[] X) {
		int index = 0;
		index += LOCATION.length * BEARING.length * DISTANCE.length * indexOfArray(HEADING, X[0]);
		index += BEARING.length * DISTANCE.length * indexOfArray(LOCATION, X[1]);
		index += DISTANCE.length * indexOfArray(BEARING, X[2]);
		index += indexOfArray(DISTANCE, X[3]);
		return index;
	}

	private int indexOfArray(int[] array, int num) {
		int index = -1;
		for (int i = 0; i < array.length; i++) {
			if (array[i] == num) {
				index = i;
				break;
			}
		}
		return index;
	}

	private double getRange(double x1, double y1, double x2, double y2) {
		double xo = x2 - x1;
		double yo = y2 - y1;
		double h = Math.sqrt(xo * xo + yo * yo);

		return h;
	}

	private double normaliseBearing(double ang) {
		if (ang > Math.PI)
			ang -= 2 * Math.PI;
		if (ang < -Math.PI)
			ang += 2 * Math.PI;
		return ang;
	}

	private void goTo(double x, double y) {
		double dist = 20;
		double angle = Math.toDegrees(absbearing(getX(), getY(), x, y));
		double r = turnTo(angle);
		setAhead(dist * r);
	}

	private double absbearing(double x1, double y1, double x2, double y2) {
		double xo = x2 - x1;
		double yo = y2 - y1;
		double h = getRange(x1, y1, x2, y2);
		if (xo > 0 && yo > 0) {
			return Math.asin(xo / h);
		}
		if (xo > 0 && yo < 0) {
			return Math.PI - Math.asin(xo / h);
		}
		if (xo < 0 && yo < 0) {
			return Math.PI + Math.asin(-xo / h);
		}
		if (xo < 0 && yo > 0) {
			return 2.0 * Math.PI - Math.asin(-xo / h);
		}
		return 0;
	}

	private int turnTo(double angle) {
		double ang;

		int dir;
		ang = normaliseBearing(getHeading() - angle);
		if (ang > 90) {
			ang -= 180;
			dir = -1;
		} else if (ang < -90) {
			ang += 180;
			dir = -1;
		} else {
			dir = 1;
		}
		setTurnLeft(ang);
		return dir;
	}

	private class Enemy {
		private String name = null;
		private double distance = -1;
		private double bearingRadians = -1;
		private double absBearingRadians = -1;
		private double energy = 100;
		private double x;
		private double y;

		private void update(ScannedRobotEvent event) {
			name = event.getName();
			distance = event.getDistance();
			bearingRadians = event.getBearingRadians();
			absBearingRadians = Utils.normalAbsoluteAngle(bearingRadians + getHeadingRadians());
			energy = event.getEnergy();
			x = getX() + distance * Math.sin(bearingRadians + getHeadingRadians());
			y = getY() + distance * Math.cos(bearingRadians + getHeadingRadians());
		}
	}

	@Override
	public void save(File argFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int indexFor(double[] X) {
		// TODO Auto-generated method stub
		return 0;
	}
}