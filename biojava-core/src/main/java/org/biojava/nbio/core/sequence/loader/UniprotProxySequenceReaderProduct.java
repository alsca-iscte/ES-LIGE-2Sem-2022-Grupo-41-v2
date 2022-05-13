package org.biojava.nbio.core.sequence.loader;


import org.w3c.dom.Document;
import java.io.IOException;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.util.XMLHelper;
import java.io.ByteArrayInputStream;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class UniprotProxySequenceReaderProduct {
	/**
	* @param accession
	* @return
	* @throws IOException
	*/
	public Document getUniprotXML(String accession) throws IOException, CompoundNotFoundException {
		StringBuilder sb = new StringBuilder();
		if (UniprotProxySequenceReader.uniprotDirectoryCache != null
				&& UniprotProxySequenceReader.uniprotDirectoryCache.length() > 0) {
			sb = fetchFromCache(accession);
		}
		if (sb.length() == 0) {
			String uniprotURL = getUniprotbaseURL() + "/uniprot/" + accession.toUpperCase() + ".xml";
			UniprotProxySequenceReader.logger.info("Loading: {}", uniprotURL);
			sb = fetchUniprotXML(uniprotURL);
			int index = sb.indexOf("xmlns=");
			if (index != -1) {
				int lastIndex = sb.indexOf(">", index);
				sb.replace(index, lastIndex, "");
			}
			if (UniprotProxySequenceReader.uniprotDirectoryCache != null
					&& UniprotProxySequenceReader.uniprotDirectoryCache.length() > 0)
				writeCache(sb, accession);
		}
		UniprotProxySequenceReader.logger.info("Load complete");
		try {
			Document document = XMLHelper.inputStreamToDocument(new ByteArrayInputStream(sb.toString().getBytes()));
			return document;
		} catch (SAXException | ParserConfigurationException e) {
			UniprotProxySequenceReader.logger.error("Exception on xml parse of: {}", sb.toString());
		}
		return null;
	}

	private String getUniprotbaseURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public void writeCache(StringBuilder sb, String accession) throws IOException {
		File f = new File(UniprotProxySequenceReader.uniprotDirectoryCache + File.separatorChar + accession + ".xml");
		try (FileWriter fw = new FileWriter(f)) {
			fw.write(sb.toString());
		}
	}

	public StringBuilder fetchUniprotXML(String uniprotURL) throws IOException, CompoundNotFoundException {
		StringBuilder sb = new StringBuilder();
		URL uniprot = new URL(uniprotURL);
		int attempt = 5;
		List<String> errorCodes = new ArrayList<String>();
		while (attempt > 0) {
			HttpURLConnection uniprotConnection = openURLConnection(uniprot);
			int statusCode = uniprotConnection.getResponseCode();
			if (statusCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(uniprotConnection.getInputStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					sb.append(inputLine);
				}
				in.close();
				return sb;
			}
			attempt--;
			errorCodes.add(String.valueOf(statusCode));
		}
		throw new RemoteException("Couldn't fetch accession from the url " + uniprotURL
				+ " error codes on 5 attempts are " + errorCodes.toString());
	}

	private HttpURLConnection openURLConnection(URL uniprot) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	* @param key
	* @return  A string containing the contents of entry specified by key and if not found returns an empty string
	* @throws FileNotFoundException
	* @throws IOException
	*/
	public StringBuilder fetchFromCache(String key) throws FileNotFoundException, IOException {
		int index;
		File f = new File(UniprotProxySequenceReader.uniprotDirectoryCache + File.separatorChar + key + ".xml");
		StringBuilder sb = new StringBuilder();
		if (f.exists()) {
			char[] data;
			try (FileReader fr = new FileReader(f)) {
				int size = (int) f.length();
				data = new char[size];
				fr.read(data);
			}
			sb.append(data);
			index = sb.indexOf("xmlns=");
			if (index != -1) {
				int lastIndex = sb.indexOf(">", index);
				sb.replace(index, lastIndex, "");
			}
		}
		return sb;
	}
}