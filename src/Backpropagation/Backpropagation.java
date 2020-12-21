package Backpropagation;

import java.io.File;
import java.io.IOException;

import Interfaces.NeuralNetInterface;
public class Backpropagation implements NeuralNetInterface {
	
	int argNumInputs;
	int argNumHidden;
	double argLearningRate;
	double argMomentumTerm;
	double argA;
	double argB;
	double[] argWeightsHiddenToOutput;
	double[][] argWeightsInputToHidden;
	double[] argWeightsChangeHiddenToOutput;
	double[][] argWeightsChangeInputToHidden;
	double[] hiddenOutputs;
	
	
	public Backpropagation(
			int argNumInputs,
			int argNumHidden,
			double argLearningRate,
			double argMomentumTerm,
			double argA,
			double argB) {
		this.argNumInputs = argNumInputs;
		this.argNumHidden = argNumHidden;
		this.argLearningRate = argLearningRate;
		this.argMomentumTerm = argMomentumTerm;
		this.argA = argA;
		this.argB = argB;
		this.argWeightsHiddenToOutput = new double[argNumHidden + 1];
		this.argWeightsInputToHidden = new double[argNumHidden][argNumInputs + 1];
		this.argWeightsChangeHiddenToOutput = new double[argNumHidden + 1];
		this.argWeightsChangeInputToHidden = new double[argNumHidden][argNumInputs + 1];
	}
	@Override
	public double outputFor(double[] X) {
		double outputValue = 0.0;
		this.hiddenOutputs = new double[argNumHidden];
		//calculate the output of hidden layer
		for (int i = 0; i < argNumHidden; i++) {
			for (int j = 0; j < argNumInputs + 1; j++) {
				// bias term
				if (j == argNumInputs) {
					hiddenOutputs[i] += argWeightsInputToHidden[i][j];
				}else {
					hiddenOutputs[i] += argWeightsInputToHidden[i][j] * X[j];
				}	
			}
			hiddenOutputs[i] = customSigmoid(hiddenOutputs[i]);
		}
		//calculate the output of neural network
		for (int i = 0; i < argNumHidden + 1; i++) {
			// bias term
			if (i == argNumHidden) {
				outputValue += argWeightsHiddenToOutput[i];
			}else {
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
		double outputErrorTerm = derivativeOfCustomSigmoid(outputValue) * (argValue - outputValue);
		double[] hiddenErrorTerm = new double[argNumHidden];
		double[] lastWeightsChangeHiddenToOutput = new double[argNumHidden + 1];
		double[][] lastWeightsChangeInputToHidden = new double[argNumHidden][argNumInputs + 1];
		for (int i = 0; i < argNumHidden; i++) {
			hiddenErrorTerm[i] = derivativeOfCustomSigmoid(hiddenOutputs[i]) * argWeightsHiddenToOutput[i] * outputErrorTerm;
		}
		for (int i = 0; i < argNumHidden + 1; i++) {
			lastWeightsChangeHiddenToOutput[i] = argWeightsChangeHiddenToOutput[i];
		}
		for (int i = 0; i < argNumHidden; i++) {
			for (int j = 0; j < argNumInputs + 1; j++) {
				lastWeightsChangeInputToHidden[i][j] = argWeightsChangeInputToHidden[i][j];
			}
		}
		
		//calculate derivative of error with respect to Hidden to Output layer weights 
		for (int i = 0; i < argNumHidden + 1; i++) {
			if (i == argNumHidden) {
				argWeightsChangeHiddenToOutput[i] = argLearningRate * outputErrorTerm + argMomentumTerm * lastWeightsChangeHiddenToOutput[i];
			}else {
				argWeightsChangeHiddenToOutput[i] = argLearningRate * outputErrorTerm * hiddenOutputs[i] + argMomentumTerm * lastWeightsChangeHiddenToOutput[i];
			}
		}
		//calculate derivative of error with respect to Input to Hidden layer weights 
		for (int i = 0; i < argNumHidden; i++) {
			for (int j = 0; j < argNumInputs + 1; j++) {
				if (j == argNumInputs) {
					argWeightsChangeInputToHidden[i][j] = argLearningRate * hiddenErrorTerm[i] + argMomentumTerm * lastWeightsChangeInputToHidden[i][j];
				} else {
					argWeightsChangeInputToHidden[i][j] = argLearningRate * hiddenErrorTerm[i] * X[j] + argMomentumTerm * lastWeightsChangeInputToHidden[i][j];
				}
			}
		}
		//update Hidden to Output layer weights
		for (int i = 0; i < argNumHidden + 1; i++) {
			argWeightsHiddenToOutput[i] += argWeightsChangeHiddenToOutput[i];
		}
		//update Input to Hidden layer weights
		for (int i = 0; i < argNumHidden; i++) {
			for (int j = 0; j < argNumInputs + 1; j++) {
				argWeightsInputToHidden[i][j] += argWeightsChangeInputToHidden[i][j];
			}
		}
		return trainingError;
	}

	@Override
	public void save(File argFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void load(String argFileName) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double sigmoid(double x) {
		double result = 1/(1 + Math.exp(-x));
		return result;
	}

	@Override
	public double customSigmoid(double x) {
		 /**
		 * This method implements a general sigmoid with asymptotes bounded by (a,b)
		 * @param x The input
		 * @return f(x) = b_minus_a / (1 + e(-x)) - minus_a
		 */
		double result = (argB - argA) * sigmoid(x) + argA;
		return result;
	}
	public double derivativeOfCustomSigmoid(double y) {
		double result = (y - argA) * (argB - y)/(argB - argA);
		return result;
	}

	@Override
	public void initializeWeights() {
		// initialize hidden to output layer weights to random values between -1 and 1 for bipolar representation
		for (int i = 0; i < argNumHidden + 1; i++) {
			argWeightsHiddenToOutput[i] = 2 * Math.random() - 1;
		}
		// initialize input to hidden layer weights to random values between -1 and 1 for bipolar representation
		for (int i = 0; i < argNumHidden; i++) {
			for (int j = 0; j < argNumInputs + 1; j++) {
				argWeightsInputToHidden[i][j] = 2 * Math.random() - 1;
			}
		}
	}

	@Override
	public void zeroWeights() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void save(String argFileName) {
		// TODO Auto-generated method stub
		
	}
	
	
	

}
