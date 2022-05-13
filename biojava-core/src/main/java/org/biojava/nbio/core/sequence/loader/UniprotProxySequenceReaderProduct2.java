package org.biojava.nbio.core.sequence.loader;


import java.util.ArrayList;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Element;
import org.biojava.nbio.core.util.XMLHelper;

public class UniprotProxySequenceReaderProduct2 {
	/**
	* Pull uniprot protein aliases associated with this sequence
	* @return
	* @throws XPathExpressionException
	*/
	public ArrayList<String> getProteinAliases() throws XPathExpressionException {
		ArrayList<String> aliasList = new ArrayList<String>();
		if (org.biojava.nbio.core.sequence.loader.UniprotProxySequenceReader.uniprotDoc == null) {
			return aliasList;
		}
		Element uniprotElement = org.biojava.nbio.core.sequence.loader.UniprotProxySequenceReader.uniprotDoc.getDocumentElement();
		Element entryElement = XMLHelper.selectSingleElement(uniprotElement, "entry");
		Element proteinElement = XMLHelper.selectSingleElement(entryElement, "protein");
		ArrayList<Element> keyWordElementList;
		getProteinAliasesFromNameGroup(aliasList, proteinElement);
		keyWordElementList = XMLHelper.selectElements(proteinElement, "component");
		for (Element element : keyWordElementList) {
			getProteinAliasesFromNameGroup(aliasList, element);
		}
		keyWordElementList = XMLHelper.selectElements(proteinElement, "domain");
		for (Element element : keyWordElementList) {
			getProteinAliasesFromNameGroup(aliasList, element);
		}
		keyWordElementList = XMLHelper.selectElements(proteinElement, "submittedName");
		for (Element element : keyWordElementList) {
			getProteinAliasesFromNameGroup(aliasList, element);
		}
		keyWordElementList = XMLHelper.selectElements(proteinElement, "cdAntigenName");
		for (Element element : keyWordElementList) {
			String cdAntigenName = element.getTextContent();
			if (null != cdAntigenName && !cdAntigenName.trim().isEmpty()) {
				aliasList.add(cdAntigenName);
			}
		}
		keyWordElementList = XMLHelper.selectElements(proteinElement, "innName");
		for (Element element : keyWordElementList) {
			String cdAntigenName = element.getTextContent();
			if (null != cdAntigenName && !cdAntigenName.trim().isEmpty()) {
				aliasList.add(cdAntigenName);
			}
		}
		keyWordElementList = XMLHelper.selectElements(proteinElement, "biotechName");
		for (Element element : keyWordElementList) {
			String cdAntigenName = element.getTextContent();
			if (null != cdAntigenName && !cdAntigenName.trim().isEmpty()) {
				aliasList.add(cdAntigenName);
			}
		}
		keyWordElementList = XMLHelper.selectElements(proteinElement, "allergenName");
		for (Element element : keyWordElementList) {
			String cdAntigenName = element.getTextContent();
			if (null != cdAntigenName && !cdAntigenName.trim().isEmpty()) {
				aliasList.add(cdAntigenName);
			}
		}
		return aliasList;
	}

	/**
	* @param aliasList
	* @param proteinElement
	* @throws XPathExpressionException
	*/
	public void getProteinAliasesFromNameGroup(ArrayList<String> aliasList, Element proteinElement)
			throws XPathExpressionException {
		ArrayList<Element> keyWordElementList = XMLHelper.selectElements(proteinElement, "alternativeName");
		for (Element element : keyWordElementList) {
			getProteinAliasesFromElement(aliasList, element);
		}
		keyWordElementList = XMLHelper.selectElements(proteinElement, "recommendedName");
		for (Element element : keyWordElementList) {
			getProteinAliasesFromElement(aliasList, element);
		}
	}

	/**
	* @param aliasList
	* @param element
	* @throws XPathExpressionException
	*/
	public void getProteinAliasesFromElement(ArrayList<String> aliasList, Element element)
			throws XPathExpressionException {
		Element fullNameElement = XMLHelper.selectSingleElement(element, "fullName");
		aliasList.add(fullNameElement.getTextContent());
		Element shortNameElement = XMLHelper.selectSingleElement(element, "shortName");
		if (null != shortNameElement) {
			String shortName = shortNameElement.getTextContent();
			if (null != shortName && !shortName.trim().isEmpty()) {
				aliasList.add(shortName);
			}
		}
	}
}