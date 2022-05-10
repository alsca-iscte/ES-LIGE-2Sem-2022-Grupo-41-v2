package org.biojava.nbio.core.util;


import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class PrettyXMLWriterProduct2 {
	private int namespaceSeed = 0;
	private LinkedList<List<String>> namespaceBindings = new LinkedList<List<String>>();

	public LinkedList<List<String>> getNamespaceBindings() {
		return namespaceBindings;
	}

	public String allocPrefix(String nsURI, Map<String, String> thisNamespacePrefixes) {
		String prefix = "ns" + (++namespaceSeed);
		thisNamespacePrefixes.put(nsURI, prefix);
		List<String> bindings = namespaceBindings.getLast();
		if (bindings == null) {
			bindings = new ArrayList<String>();
			namespaceBindings.removeLast();
			namespaceBindings.add(bindings);
		}
		bindings.add(nsURI);
		return prefix;
	}
}