package Backpropagation;

public class BPLearning {
	public static void main(String[] args) {
		int argNumInputs = 2;
		int argNumHidden = 4;
		double argLearningRate = 0.02;
		double argMomentumTerm = 0.9;
		double argA = -1;
		double argB = 1;
		double trainingError = 0.0;
		int epochs = 0;
		int averageEpochs = 0;
		//training data for XOR problem
		double[][] X = {{-1,-1},{-1,1},{1,-1},{1,1}};
		//traing label for XOR problem
		double[] argValue = {-1,1,1,-1};
		Backpropagation bp = new Backpropagation(argNumInputs, argNumHidden, argLearningRate, argMomentumTerm, argA, argB);
		/* perform 20 trials to get an average of number of epochs 
		 * in order to make total training error less than 0.05  
		 */
		for (int i = 1; i <= 20; i++) {
			epochs = 0;
			bp.initializeWeights();
			do {
				trainingError = 0;
				epochs++;
				for (int j = 0; j < X.length; j++) {
					trainingError += bp.train(X[j], argValue[j]);
				}
			}while(trainingError >= 0.05);
			averageEpochs += epochs;
			System.out.println(i + ".After" + epochs + " epochs, the total error is " + trainingError + ".");
		}
		averageEpochs /= 20;
		System.out.println("For these 20 trials, it takes " + averageEpochs + " epochs on average to reach a total error of less than 0.05");
		// perform one trial to get total error after each epoch
		System.out.println("For a single trial, the total error after each epoch is as follows.");
		bp.initializeWeights();
		do {
			trainingError = 0;
			for (int j = 0; j < X.length; j++) {
				trainingError += bp.train(X[j], argValue[j]);
			}
			System.out.println(trainingError);
		}while (trainingError >= 0.05);
	}

}
