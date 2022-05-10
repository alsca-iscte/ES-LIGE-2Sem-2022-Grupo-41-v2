package org.biojava.nbio.core.search.io.blast;


import java.util.List;
import org.biojava.nbio.core.sequence.template.Sequence;
import java.util.Map;
import java.util.HashMap;

public class BlastXMLParserProduct {
	private List<Sequence> queryReferences;
	private List<Sequence> databaseReferences;
	private Map<String, Sequence> queryReferencesMap;
	private Map<String, Sequence> databaseReferencesMap;

	public List<Sequence> getQueryReferences() {
		return queryReferences;
	}

	public void setQueryReferences(List<Sequence> queryReferences) {
		this.queryReferences = queryReferences;
	}

	public List<Sequence> getDatabaseReferences() {
		return databaseReferences;
	}

	public void setDatabaseReferences(List<Sequence> databaseReferences) {
		this.databaseReferences = databaseReferences;
	}

	public Map<String, Sequence> getQueryReferencesMap() {
		return queryReferencesMap;
	}

	public Map<String, Sequence> getDatabaseReferencesMap() {
		return databaseReferencesMap;
	}

	/**
	* fill the map association between sequences an a unique id
	*/
	public void mapIds() {
		if (queryReferences != null) {
			queryReferencesMap = new HashMap<String, Sequence>(queryReferences.size());
			for (int counter = 0; counter < queryReferences.size(); counter++) {
				String id = "Query_" + (counter + 1);
				queryReferencesMap.put(id, queryReferences.get(counter));
			}
		}
		if (databaseReferences != null) {
			databaseReferencesMap = new HashMap<String, Sequence>(databaseReferences.size());
			for (int counter = 0; counter < databaseReferences.size(); counter++) {
				String id = "gnl|BL_ORD_ID|" + (counter);
				databaseReferencesMap.put(id, databaseReferences.get(counter));
			}
		}
	}
}