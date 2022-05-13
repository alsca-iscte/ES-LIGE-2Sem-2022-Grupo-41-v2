package org.biojava.nbio.core.util;


import java.util.ArrayList;
import org.biojava.nbio.core.util.SingleLinkageClusterer.LinkedPair;
import java.util.Map;
import java.util.Set;
import java.util.Locale;

public class SingleLinkageClustererProduct {
	private SingleLinkageClustererProductProduct singleLinkageClustererProductProduct = new SingleLinkageClustererProductProduct();
	private double[][] matrix;
	private int numItems;
	public void setMatrix(double[][] matrix) {
		this.matrix = matrix;
	}

	public void setIsScoreMatrix(boolean isScoreMatrix) {
		singleLinkageClustererProductProduct.setIsScoreMatrix(isScoreMatrix);
	}

	public int getNumItems() {
		return numItems;
	}

	public void setNumItems(int numItems) {
		this.numItems = numItems;
	}

	public double getDistance(int first, int second) {
		return matrix[Math.min(first, second)][Math.max(first, second)];
	}

	/**
	* Calculate the hierarchical clustering and store it in dendrogram array This is the naive implementation (o(n3)) of single linkage clustering as outlined in wikipedia: http://en.wikipedia.org/wiki/Single-linkage_clustering
	*/
	public void clusterIt(SingleLinkageClusterer singleLinkageClusterer) {
		singleLinkageClusterer.setDendrogram(new LinkedPair[numItems - 1]);
		SingleLinkageClusterer.logger.debug("Initial matrix: \n" + matrixToString());
		for (int m = 0; m < numItems - 1; m++) {
			updateIndicesToCheck(m, singleLinkageClusterer);
			LinkedPair pair = singleLinkageClustererProductProduct.getClosestPair(singleLinkageClusterer, this.matrix);
			merge(pair);
			singleLinkageClusterer.getDendrogram2()[m] = pair;
		}
	}

	/**
	* Merge 2 rows/columns of the matrix by the linkage function (see  {@link #link(double,double)}
	* @param closestPair
	*/
	public void merge(LinkedPair closestPair) {
		int first = closestPair.getFirst();
		int second = closestPair.getSecond();
		for (int other = 0; other < numItems; other++) {
			matrix[Math.min(first, other)][Math.max(first, other)] = singleLinkageClustererProductProduct.link(getDistance(first, other),
					getDistance(second, other));
		}
	}

	public void updateIndicesToCheck(int m, SingleLinkageClusterer singleLinkageClusterer) {
		if (singleLinkageClustererProductProduct.getIndicesToCheck() == null) {
			singleLinkageClustererProductProduct.setIndicesToCheck(new ArrayList<Integer>(numItems));
			for (int i = 0; i < numItems; i++) {
				singleLinkageClustererProductProduct.getIndicesToCheck().add(i);
			}
		}
		if (m == 0)
			return;
		singleLinkageClustererProductProduct.getIndicesToCheck().remove(new Integer(singleLinkageClusterer.getDendrogram2()[m - 1].getFirst()));
	}

	/**
	* The linkage function: minimum of the 2 distances (i.e. single linkage clustering)
	* @param d1
	* @param d2
	* @return
	*/
	public double link(double d1, double d2) {
		return singleLinkageClustererProductProduct.link(d1, d2);
	}

	public LinkedPair getClosestPair(SingleLinkageClusterer singleLinkageClusterer) {
		return singleLinkageClustererProductProduct.getClosestPair(singleLinkageClusterer, this.matrix);
	}

	public int firstClusterId(Map<Integer, Set<Integer>> clusters, int i,
			SingleLinkageClusterer singleLinkageClusterer) {
		int firstClusterId = -1;
		for (int cId : clusters.keySet()) {
			Set<Integer> members = clusters.get(cId);
			if (members.contains(singleLinkageClusterer.getDendrogram2()[i].getFirst())) {
				firstClusterId = cId;
			}
		}
		return firstClusterId;
	}

	public boolean isWithinCutoff(int i, double cutoff, SingleLinkageClusterer singleLinkageClusterer) {
		if (singleLinkageClustererProductProduct.getIsScoreMatrix()) {
			if (singleLinkageClusterer.getDendrogram2()[i].getClosestDistance() > cutoff) {
				return true;
			} else {
				return false;
			}
		} else {
			if (singleLinkageClusterer.getDendrogram2()[i].getClosestDistance() < cutoff) {
				return true;
			} else {
				return false;
			}
		}
	}

	public String matrixToString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < numItems; i++) {
			for (int j = 0; j < numItems; j++) {
				if (i == j) {
					sb.append(String.format("%6s ", "x"));
				} else if (i < j) {
					if (matrix[i][j] == Double.MAX_VALUE)
						sb.append(String.format("%6s ", "inf"));
					else
						sb.append(String.format(Locale.US, "%6.2f ", matrix[i][j]));
				} else {
					if (matrix[j][i] == Double.MAX_VALUE)
						sb.append(String.format("%6s ", "inf"));
					else
						sb.append(String.format(Locale.US, "%6.2f ", matrix[j][i]));
				}
			}
			sb.append("\n");
		}
		sb.append("\n");
		return sb.toString();
	}
}