package org.biojava.nbio.core.sequence.location.template;


import java.io.Serializable;

public class AbstractLocationProduct implements Serializable {
	private boolean partialOn5prime = false;
	private boolean partialOn3prime = false;

	public boolean getPartialOn5prime() {
		return partialOn5prime;
	}

	public void setPartialOn5prime(boolean partialOn5prime) {
		this.partialOn5prime = partialOn5prime;
	}

	public boolean getPartialOn3prime() {
		return partialOn3prime;
	}

	public void setPartialOn3prime(boolean partialOn3prime) {
		this.partialOn3prime = partialOn3prime;
	}

	public boolean isPartial() {
		return partialOn5prime || partialOn3prime;
	}
}