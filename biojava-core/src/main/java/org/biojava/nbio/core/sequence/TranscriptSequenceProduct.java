package org.biojava.nbio.core.sequence;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Collections;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.transcription.TranscriptionEngine;

public class TranscriptSequenceProduct {
	private final ArrayList<CDSSequence> cdsSequenceList = new ArrayList<CDSSequence>();
	private final LinkedHashMap<String, CDSSequence> cdsSequenceHashMap = new LinkedHashMap<String, CDSSequence>();
	private StartCodonSequence startCodonSequence = null;
	private StopCodonSequence stopCodonSequence = null;

	public LinkedHashMap<String, CDSSequence> getCdsSequenceHashMap() {
		return cdsSequenceHashMap;
	}

	public StartCodonSequence getStartCodonSequence() {
		return startCodonSequence;
	}

	public StopCodonSequence getStopCodonSequence() {
		return stopCodonSequence;
	}

	/**
	* Remove a CDS or coding sequence from the transcript sequence
	* @param accession
	* @return
	*/
	public CDSSequence removeCDS(String accession) {
		for (CDSSequence cdsSequence : cdsSequenceList) {
			if (cdsSequence.getAccession().getID().equals(accession)) {
				cdsSequenceList.remove(cdsSequence);
				cdsSequenceHashMap.remove(accession);
				return cdsSequence;
			}
		}
		return null;
	}

	/**
	* Add a Coding Sequence region with phase to the transcript sequence
	* @param accession
	* @param begin
	* @param end
	* @param phase  0,1,2
	* @return
	*/
	public CDSSequence addCDS(AccessionID accession, int begin, int end, int phase,
			TranscriptSequence transcriptSequence) throws Exception {
		if (cdsSequenceHashMap.containsKey(accession.getID())) {
			throw new Exception("Duplicate accession id " + accession.getID());
		}
		CDSSequence cdsSequence = new CDSSequence(transcriptSequence, begin, end, phase);
		cdsSequence.setAccession(accession);
		cdsSequenceList.add(cdsSequence);
		Collections.sort(cdsSequenceList, new CDSComparator());
		cdsSequenceHashMap.put(accession.getID(), cdsSequence);
		return cdsSequence;
	}

	/**
	* Return a list of protein sequences based on each CDS sequence where the phase shift between two CDS sequences is assigned to the CDS sequence that starts the triplet. This can be used to map a CDS/exon region of a protein sequence back to the DNA sequence If you have a protein sequence and a predicted gene you can take the predict CDS protein sequences and align back to the protein sequence. If you have errors in mapping the predicted protein CDS regions to an the known protein sequence then you can identify possible errors in the prediction
	* @return
	*/
	public ArrayList<ProteinSequence> getProteinCDSSequences(TranscriptSequence transcriptSequence) {
		ArrayList<ProteinSequence> proteinSequenceList = new ArrayList<ProteinSequence>();
		for (int i = 0; i < cdsSequenceList.size(); i++) {
			CDSSequence cdsSequence = cdsSequenceList.get(i);
			String codingSequence = cdsSequence.getCodingSequence();
			if (transcriptSequence.getStrand() == Strand.NEGATIVE) {
				if (cdsSequence.phase == 1) {
					codingSequence = codingSequence.substring(1, codingSequence.length());
				} else if (cdsSequence.phase == 2) {
					codingSequence = codingSequence.substring(2, codingSequence.length());
				}
				if (i < cdsSequenceList.size() - 1) {
					CDSSequence nextCDSSequence = cdsSequenceList.get(i + 1);
					if (nextCDSSequence.phase == 1) {
						String nextCodingSequence = nextCDSSequence.getCodingSequence();
						codingSequence = codingSequence + nextCodingSequence.substring(0, 1);
					} else if (nextCDSSequence.phase == 2) {
						String nextCodingSequence = nextCDSSequence.getCodingSequence();
						codingSequence = codingSequence + nextCodingSequence.substring(0, 2);
					}
				}
			} else {
				if (cdsSequence.phase == 1) {
					codingSequence = codingSequence.substring(1, codingSequence.length());
				} else if (cdsSequence.phase == 2) {
					codingSequence = codingSequence.substring(2, codingSequence.length());
				}
				if (i < cdsSequenceList.size() - 1) {
					CDSSequence nextCDSSequence = cdsSequenceList.get(i + 1);
					if (nextCDSSequence.phase == 1) {
						String nextCodingSequence = nextCDSSequence.getCodingSequence();
						codingSequence = codingSequence + nextCodingSequence.substring(0, 1);
					} else if (nextCDSSequence.phase == 2) {
						String nextCodingSequence = nextCDSSequence.getCodingSequence();
						codingSequence = codingSequence + nextCodingSequence.substring(0, 2);
					}
				}
			}
			DNASequence dnaCodingSequence = null;
			try {
				dnaCodingSequence = new DNASequence(codingSequence.toUpperCase());
			} catch (CompoundNotFoundException e) {
				TranscriptSequence.logger.error("Could not create DNA coding sequence, {}. This is most likely a bug.",
						e.getMessage());
			}
			RNASequence rnaCodingSequence = dnaCodingSequence.getRNASequence(TranscriptionEngine.getDefault());
			ProteinSequence proteinSequence = rnaCodingSequence.getProteinSequence(TranscriptionEngine.getDefault());
			proteinSequence.setAccession(new AccessionID(cdsSequence.getAccession().getID()));
			proteinSequence.setParentDNASequence(cdsSequence, 1, cdsSequence.getLength());
			proteinSequenceList.add(proteinSequence);
		}
		return proteinSequenceList;
	}

	/**
	* Get the stitched together CDS sequences then maps to the cDNA
	* @return
	*/
	public DNASequence getDNACodingSequence(TranscriptSequence transcriptSequence) {
		StringBuilder sb = new StringBuilder();
		for (CDSSequence cdsSequence : cdsSequenceList) {
			sb.append(cdsSequence.getCodingSequence());
		}
		DNASequence dnaSequence = null;
		try {
			dnaSequence = new DNASequence(sb.toString().toUpperCase());
		} catch (CompoundNotFoundException e) {
			TranscriptSequence.logger.error("Could not create DNA coding sequence, {}. This is most likely a bug.",
					e.getMessage());
		}
		dnaSequence.setAccession(new AccessionID(transcriptSequence.getAccession().getID()));
		return dnaSequence;
	}

	/**
	* Get the protein sequence with user defined TranscriptEngine
	* @param engine
	* @return
	*/
	public ProteinSequence getProteinSequence(TranscriptionEngine engine, TranscriptSequence transcriptSequence) {
		DNASequence dnaCodingSequence = getDNACodingSequence(transcriptSequence);
		RNASequence rnaCodingSequence = dnaCodingSequence.getRNASequence(engine);
		ProteinSequence proteinSequence = rnaCodingSequence.getProteinSequence(engine);
		proteinSequence.setAccession(new AccessionID(transcriptSequence.getAccession().getID()));
		return proteinSequence;
	}

	/**
	* Sets the start codon sequence at given begin /  end location. Note that calling this method multiple times will replace any existing value.
	* @param accession
	* @param begin
	* @param end
	*/
	public void addStartCodonSequence(AccessionID accession, int begin, int end,
			TranscriptSequence transcriptSequence) {
		this.startCodonSequence = new StartCodonSequence(transcriptSequence, begin, end);
		startCodonSequence.setAccession(accession);
	}

	/**
	* Sets the stop codon sequence at given begin /  end location. Note that calling this method multiple times will replace any existing value.
	* @param accession
	* @param begin
	* @param end
	*/
	public void addStopCodonSequence(AccessionID accession, int begin, int end, TranscriptSequence transcriptSequence) {
		this.stopCodonSequence = new StopCodonSequence(transcriptSequence, begin, end);
		stopCodonSequence.setAccession(accession);
	}
}