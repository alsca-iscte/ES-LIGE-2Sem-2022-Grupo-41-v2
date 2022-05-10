package org.biojava.nbio.core.sequence.io;


import java.io.InputStream;
import java.util.List;
import org.biojava.nbio.core.sequence.io.IUPACParser.IUPACTable;
import java.util.ArrayList;
import org.biojava.nbio.core.sequence.io.util.IOUtils;

public class IUPACParserProduct {
	private InputStream is;
	private List<IUPACTable> tables;

	public void setIs(InputStream is) {
		this.is = is;
	}

	public List<IUPACTable> parseTables() {
		List<IUPACTable> localTables = new ArrayList<IUPACTable>();
		List<String> lines = IOUtils.getList(is);
		Integer id = null;
		String name, aa, starts, baseone, basetwo, basethree;
		name = aa = starts = baseone = basetwo = basethree = null;
		for (String line : lines) {
			if (line.equalsIgnoreCase("//")) {
				localTables.add(new IUPACParser.IUPACTable(name, id, aa, starts, baseone, basetwo, basethree));
				name = aa = starts = baseone = basetwo = basethree = null;
				id = null;
			} else {
				String[] keyValue = line.split("\\s*=\\s*");
				if (keyValue[0].equals("AAs")) {
					aa = keyValue[1];
				} else if (keyValue[0].equals("Starts")) {
					starts = keyValue[1];
				} else if (keyValue[0].equals("Base1")) {
					baseone = keyValue[1];
				} else if (keyValue[0].equals("Base2")) {
					basetwo = keyValue[1];
				} else if (keyValue[0].equals("Base3")) {
					basethree = keyValue[1];
				} else {
					name = keyValue[0];
					id = Integer.parseInt(keyValue[1]);
				}
			}
		}
		return localTables;
	}

	/**
	* Returns a list of all available IUPAC tables
	*/
	public List<IUPACTable> getTables() {
		if (tables == null) {
			tables = parseTables();
		}
		return tables;
	}
}