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
import org.biojava.nbio.core.sequence.loader.GenbankProxySequenceReader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;


public class GenbankProxySequenceReaderProduct {
	public BufferedInputStream getBufferedInputStream(String accessionID, String db)
			throws IOException, InterruptedException {
		BufferedInputStream inStream = null;
		if (org.biojava.nbio.core.sequence.loader.GenbankProxySequenceReader.genbankDirectoryCache != null && org.biojava.nbio.core.sequence.loader.GenbankProxySequenceReader.genbankDirectoryCache.length() > 0) {
			File f = new File(org.biojava.nbio.core.sequence.loader.GenbankProxySequenceReader.genbankDirectoryCache + File.separatorChar + accessionID + ".gb");
			if (f.exists()) {
				GenbankProxySequenceReader.logger.debug("Reading: {}", f.toString());
				inStream = new BufferedInputStream(new FileInputStream(f));
			} else {
				InputStream in = getEutilsInputStream(accessionID, db);
				copyInputStreamToFile(in, f);
				inStream = new BufferedInputStream(new FileInputStream(f));
			}
		} else {
			inStream = new BufferedInputStream(getEutilsInputStream(accessionID, db));
		}
		return inStream;
	}

	public void copyInputStreamToFile(InputStream in, File f) throws IOException, InterruptedException {
		try (FileOutputStream out = new FileOutputStream(f)) {
			byte[] buffer = new byte[1024];
			int len = in.read(buffer);
			while (len != -1) {
				out.write(buffer, 0, len);
				len = in.read(buffer);
				if (Thread.interrupted()) {
					in.close();
					out.close();
					throw new InterruptedException();
				}
			}
			in.close();
		}
	}

	public InputStream getEutilsInputStream(String accessionID, String db) throws IOException {
		String genbankURL = GenbankProxySequenceReader.eutilBaseURL + "efetch.fcgi?db=" + db + "&id=" + accessionID
				+ "&rettype=gb&retmode=text";
		GenbankProxySequenceReader.logger.trace("Loading: {}", genbankURL);
		URL genbank = new URL(genbankURL);
		URLConnection genbankConnection = genbank.openConnection();
		return genbankConnection.getInputStream();
	}
}