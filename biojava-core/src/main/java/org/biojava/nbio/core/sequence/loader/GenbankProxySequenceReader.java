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
 * @author Karl Nicholas <github:karlnicholas>
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 * Created on 08-08-2013
 *
 */
package org.biojava.nbio.core.sequence.loader;

import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompoundSet;
import org.biojava.nbio.core.sequence.features.*;
import org.biojava.nbio.core.sequence.io.GenbankSequenceParser;
import org.biojava.nbio.core.sequence.io.GenericGenbankHeaderParser;
import org.biojava.nbio.core.sequence.template.AbstractSequence;
import org.biojava.nbio.core.sequence.template.Compound;
import org.biojava.nbio.core.sequence.template.CompoundSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * @author Karl Nicholas <github:karlnicholas>
 * @author Jacek Grzebyta <github:jgrzebyta>
 */
public class GenbankProxySequenceReader<C extends Compound> extends StringProxySequenceReader<C> implements FeaturesKeyWordInterface, DatabaseReferenceInterface, FeatureRetriever {

	public GenbankProxySequenceReaderProduct genbankProxySequenceReaderProduct = new GenbankProxySequenceReaderProduct();

	public final static Logger logger = LoggerFactory.getLogger(GenbankProxySequenceReader.class);

	public static final String eutilBaseURL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/"; //
	public static String genbankDirectoryCache = null;
	public GenbankSequenceParser<AbstractSequence<C>, C> genbankParser;
	public GenericGenbankHeaderParser<AbstractSequence<C>, C> headerParser;
	public String header;
	public Map<String, List<AbstractFeature<AbstractSequence<C>, C>>> features;


	/**
	 *
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws CompoundNotFoundException
	 */
	public GenbankProxySequenceReader(
			String genbankDirectoryCache,
			String accessionID,
			CompoundSet<C> compoundSet ) throws IOException, InterruptedException, CompoundNotFoundException {

		setGenbankDirectoryCache(genbankDirectoryCache);
		setCompoundSet(compoundSet);

		String db = compoundSet instanceof AminoAcidCompoundSet ? "protein" : "nuccore";

		InputStream inStream = getBufferedInputStream(accessionID, db);
		genbankParser = new GenbankSequenceParser<AbstractSequence<C>, C>();

		setContents(genbankParser.getSequence(new BufferedReader(new InputStreamReader(inStream)), 0));
		headerParser = genbankParser.getSequenceHeaderParser();
		header = genbankParser.getHeader();
		features = genbankParser.getFeatures();

		if (compoundSet.getClass().equals(AminoAcidCompoundSet.class)) {
			if (!genbankParser.getCompoundType().equals(compoundSet)) {
				logger.error("Declared compount type {} does not mach the real: {}", genbankParser.getCompoundType().toString(), compoundSet.toString());
				throw new IOException("Wrong declared compound type for: " + accessionID);
			}
		}

		inStream.close();
	}

	private InputStream getBufferedInputStream(String accessionID, String db) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Local directory cache of Genbank that can be downloaded
	 *
	 * @return the uniprotDirectoryCache
	 */
	public String getGenbankDirectoryCache() {
		return genbankDirectoryCache;
	}

	/**
	 * @param genbankDirectoryCache
	 */
	public void setGenbankDirectoryCache(String genbankDirectoryCache) {
		if (genbankDirectoryCache != null) {
			File f = new File(genbankDirectoryCache);
			if (!f.exists()) {
				f.mkdirs();
			}
		}
		this.genbankDirectoryCache = genbankDirectoryCache;
	}

	public String getHeader() {
		return header;
	}

	public GenericGenbankHeaderParser<AbstractSequence<C>, C> getHeaderParser() {
		return headerParser;
	}
	@Override
	public Map<String, List<AbstractFeature<AbstractSequence<C>, C>>> getFeatures() {
		return features;
	}

	@Override
	public Map<String, List<DBReferenceInfo>> getDatabaseReferences() {
		return genbankParser.getDatabaseReferences();
	}

	@Override
	public List<String> getKeyWords() {
		return genbankParser.getKeyWords();
	}
	
}
