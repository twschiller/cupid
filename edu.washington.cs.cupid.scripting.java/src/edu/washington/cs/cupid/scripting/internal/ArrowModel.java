package edu.washington.cs.cupid.scripting.internal;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ArrowModel {

	private String name;
	private String description;
	private String uniqueId;
	private String parameterType;
	private String returnType;
	
	public ArrowModel(String name, String description, String uniqueId,
			String parameterType, String returnType) {
		super();
		this.name = name;
		this.description = description;
		this.uniqueId = uniqueId;
		this.parameterType = parameterType;
		this.returnType = returnType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getParameterType() {
		return parameterType;
	}

	public void setParameterType(String parameterType) {
		this.parameterType = parameterType;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public StringBuffer asXML() throws ParserConfigurationException, TransformerException{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("arrow");
		doc.appendChild(rootElement);
		
		add(doc, rootElement, "name", name);
		add(doc, rootElement, "description", description);
		add(doc, rootElement, "uniqueId", uniqueId);
		add(doc, rootElement, "parameterType", parameterType);
		add(doc, rootElement, "returnType", returnType);
		
		Element jobElement = doc.createElement("job");
		rootElement.appendChild(jobElement);
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(doc), new StreamResult(writer));
		return writer.getBuffer();
	}
	
	private void add(Document doc, Element parent, String name, String value){
		Element element = doc.createElement(name);
		element.appendChild(doc.createTextNode(value));
		parent.appendChild(element);
	}
	
}
