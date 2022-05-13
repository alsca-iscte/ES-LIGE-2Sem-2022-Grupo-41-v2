/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */
package org.biojava.nbio.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;



/**
 * An implementation of a single linkage clusterer
 *
 * See http://en.wikipedia.org/wiki/Single-linkage_clustering
 *
 * @author Jose Duarte
 */
public class SingleLinkageClusterer {

	private SingleLinkageClustererProduct singleLinkageClustererProduct = new SingleLinkageClustererProduct();

	public static final Logger logger = LoggerFactory.getLogger(SingleLinkageClusterer.class);

	public class LinkedPair {

		private int first;
		private int second;
		private double closestDistance;

		public LinkedPair(int first, int second, double minDistance) {
			this.first = first;
			this.second = second;
			this.closestDistance = minDistance;
		}

		public int getFirst() {
			return first;
		}

		public int getSecond() {
			return second;
		}

		public double getClosestDistance() {
			return closestDistance;
		}

		@Override
		public String toString() {

			String closestDistStr = null;
			if (closestDistance==Double.MAX_VALUE) {
				closestDistStr = String.format("%6s", "inf");
			} else {
				closestDistStr = String.format(Locale.US, "%6.2f",closestDistance);
			}

			return "["+first+","+second+"-"+closestDistStr+"]";
		}

	}

	private LinkedPair[] dendrogram;

	//private Set<Integer> toSkip;

	/**
	 * Constructs a new SingleLinkageClusterer
	 * Subsequently use {@link #getDendrogram()} to get the full tree
	 * or {@link #getClusters(double)} to get the clusters at a certain cutoff in the tree
	 * Please note that the matrix will be altered during the clustering procedure. A copy must be
	 * made before by the user if needing to use the original matrix further.
	 * @param matrix the distance matrix with distance values in j>i half, all other values will be ignored
	 * @param isScoreMatrix if false the matrix will be considered a distance matrix: lower values (distances) mean closer objects,
	 * if true the matrix will be considered a score matrix: larger values (scores) mean closer objects
	 * @throws IllegalArgumentException if matrix not square
	 */
	public SingleLinkageClusterer(double[][] matrix, boolean isScoreMatrix) {
		singleLinkageClustererProduct.setMatrix(matrix);
		singleLinkageClustererProduct.setIsScoreMatrix(isScoreMatrix);

		if (matrix.length!=matrix[0].length) {
			throw new IllegalArgumentException("Distance matrix for clustering must be a square matrix");
		}

		singleLinkageClustererProduct.setNumItems(matrix.length);

	}

	/**
	 * Get the full dendrogram (size n-1) result of the hierarchical clustering
	 * @return
	 */
	public LinkedPair[] getDendrogram() {
		if (dendrogram==null) {
			singleLinkageClustererProduct.clusterIt(this);
		}

		return dendrogram;
	}

	/**
	 * Get the clusters by cutting the dendrogram at given cutoff
	 * @param cutoff
	 * @return Map from cluster numbers to indices of the cluster members
	 */
	public Map<Integer, Set<Integer>> getClusters(double cutoff) {

		if (dendrogram==null) {
			singleLinkageClustererProduct.clusterIt(this);
		}

		Map<Integer, Set<Integer>> clusters = new TreeMap<Integer, Set<Integer>>();

		int clusterId = 1;

		for (int i=0;i<singleLinkageClustererProduct.getNumItems()-1;i++) {

			if (singleLinkageClustererProduct.isWithinCutoff(i, cutoff, this)) {

				//int containingClusterId = getContainingCluster(clusters, dendrogram[i]);

				int firstClusterId = singleLinkageClustererProduct.firstClusterId(clusters, i, this);
				int secondClusterId = secondClusterId(clusters, i);
				if (firstClusterId==-1 && secondClusterId==-1) {
					// neither member is in a cluster yet, let's assign a new cluster and put them both in
					Set<Integer> members = new TreeSet<Integer>();
					members.add(dendrogram[i].getFirst());
					members.add(dendrogram[i].getSecond());
					clusters.put(clusterId, members);
					clusterId++;
				} else if (firstClusterId!=-1 && secondClusterId==-1) {
					// first member was in firstClusterId already, we add second
					clusters.get(firstClusterId).add(dendrogram[i].getSecond());
				} else if (secondClusterId!=-1 && firstClusterId==-1) {
					// second member was in secondClusterId already, we add first
					clusters.get(secondClusterId).add(dendrogram[i].getFirst());
				} else {
					// both were in different clusters already
					// we need to join them: necessarily one must be of size 1 and the other of size>=1
					Set<Integer> firstCluster = clusters.get(firstClusterId);
					Set<Integer> secondCluster = clusters.get(secondClusterId);
					if (firstCluster.size()<secondCluster.size()) {
						logger.debug("Joining cluster "+firstClusterId+" to cluster "+secondClusterId);
						// we join first onto second
						for (int member : firstCluster) {
							secondCluster.add(member);
						}
						clusters.remove(firstClusterId);
					} else {
						logger.debug("Joining cluster "+secondClusterId+" to cluster "+firstClusterId);
						// we join second onto first
						for (int member : secondCluster) {
							firstCluster.add(member);
						}
						clusters.remove(secondClusterId);
					}
				}

				logger.debug("Within cutoff:     "+dendrogram[i]);

			} else {

				logger.debug("Not within cutoff: "+dendrogram[i]);

			}
		}

		// reassigning cluster numbers by creating a new map (there can be gaps in the numbering if cluster-joining happened)
		Map<Integer,Set<Integer>> finalClusters = new TreeMap<Integer, Set<Integer>>();
		int newClusterId = 1;
		for (int oldClusterId:clusters.keySet()) {
			finalClusters.put(newClusterId, clusters.get(oldClusterId));
			newClusterId++;
		}

		// anything not clustered is assigned to a singleton cluster (cluster with one member)
		for (int i=0;i<singleLinkageClustererProduct.getNumItems();i++) {
			boolean isAlreadyClustered = isAlreadyClustered(finalClusters, i);
			if (!isAlreadyClustered) {
				Set<Integer> members = new TreeSet<Integer>();
				members.add(i);
				finalClusters.put(newClusterId, members);
				newClusterId++;
			}

		}

		logger.debug("Clusters: \n"+clustersToString(finalClusters));

		return finalClusters;
	}

	private int secondClusterId(Map<Integer, Set<Integer>> clusters, int i) {
		int secondClusterId = -1;
		for (int cId : clusters.keySet()) {
			Set<Integer> members = clusters.get(cId);
			if (members.contains(dendrogram[i].getSecond())) {
				secondClusterId = cId;
			}
		}
		return secondClusterId;
	}

	private boolean isAlreadyClustered(Map<Integer, Set<Integer>> finalClusters, int i) {
		boolean isAlreadyClustered = false;
		for (Set<Integer> cluster : finalClusters.values()) {
			if (cluster.contains(i)) {
				isAlreadyClustered = true;
				break;
			}
		}
		return isAlreadyClustered;
	}

	private String clustersToString(Map<Integer,Set<Integer>> finalClusters) {
		StringBuilder sb = new StringBuilder();
		for (int cId:finalClusters.keySet()) {
			sb.append(cId).append(": ");
			for (int member:finalClusters.get(cId)) {
				sb.append(member).append(" ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public void setDendrogram(LinkedPair[] dendrogram) {
		this.dendrogram = dendrogram;
	}

	public LinkedPair[] getDendrogram2() {
		return dendrogram;
	}

}
