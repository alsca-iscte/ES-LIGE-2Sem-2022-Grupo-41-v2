package org.biojava.nbio.core.sequence.io;


import java.util.Map;
import org.biojava.nbio.core.sequence.io.IUPACParser.IUPACTable;
import java.util.HashMap;

public class IUPACParserProduct2 {
	private IUPACParserProduct iUPACParserProduct = new IUPACParserProduct();
	private Map<String, IUPACTable> nameLookup;

	public IUPACParserProduct getIUPACParserProduct() {
		return iUPACParserProduct;
	}

	public void populateLookups(IUPACParser iUPACParser) {
		if (nameLookup == null) {
			nameLookup = new HashMap<String, IUPACTable>();
			iUPACParser.setIdLookup(new HashMap<Integer, IUPACTable>());
			for (IUPACTable t : iUPACParserProduct.getTables()) {
				nameLookup.put(t.getName(), t);
				iUPACParser.getIdLookup().put(t.getId(), t);
			}
		}
	}

	/**
	* Returns a table by its name
	*/
	public IUPACTable getTable(String name, IUPACParser iUPACParser) {
		populateLookups(iUPACParser);
		return nameLookup.get(name);
	}
}