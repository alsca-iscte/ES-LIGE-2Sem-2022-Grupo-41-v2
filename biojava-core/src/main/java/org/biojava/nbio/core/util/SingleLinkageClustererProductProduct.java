package org.biojava.nbio.core.util;


import java.util.ArrayList;
import org.biojava.nbio.core.util.SingleLinkageClusterer.LinkedPair;

public class SingleLinkageClustererProductProduct {
	private boolean isScoreMatrix;
	private ArrayList<Integer> indicesToCheck;

	public boolean getIsScoreMatrix() {
		return isScoreMatrix;
	}

	public void setIsScoreMatrix(boolean isScoreMatrix) {
		this.isScoreMatrix = isScoreMatrix;
	}

	public ArrayList<Integer> getIndicesToCheck() {
		return indicesToCheck;
	}

	public void setIndicesToCheck(ArrayList<Integer> indicesToCheck) {
		this.indicesToCheck = indicesToCheck;
	}

	/**
	* The linkage function: minimum of the 2 distances (i.e. single linkage clustering)
	* @param d1
	* @param d2
	* @return
	*/
	public double link(double d1, double d2) {
		if (isScoreMatrix) {
			return Math.max(d1, d2);
		} else {
			return Math.min(d1, d2);
		}
	}

	public LinkedPair getClosestPair(SingleLinkageClusterer singleLinkageClusterer, double[][] thisMatrix) {
		LinkedPair closestPair = null;
		if (isScoreMatrix) {
			double max = 0.0;
			for (int i : indicesToCheck) {
				for (int j : indicesToCheck) {
					if (j <= i)
						continue;
					if (thisMatrix[i][j] >= max) {
						max = thisMatrix[i][j];
						closestPair = singleLinkageClusterer.new LinkedPair(i, j, max);
					}
				}
			}
		} else {
			double min = Double.MAX_VALUE;
			for (int i : indicesToCheck) {
				for (int j : indicesToCheck) {
					if (j <= i)
						continue;
					if (thisMatrix[i][j] <= min) {
						min = thisMatrix[i][j];
						closestPair = singleLinkageClusterer.new LinkedPair(i, j, min);
					}
				}
			}
		}
		return closestPair;
	}
}