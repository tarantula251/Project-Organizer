package model;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class DataIO {

	public DataIO() {
	}

	public void writeToXml(Event event, String filename) {		
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("EventsCollection");
			doc.appendChild(rootElement);

			Element eventHeader = doc.createElement("Event");
			rootElement.appendChild(eventHeader);

			Attr attr = doc.createAttribute("id");
			attr.setValue(Integer.toString(event.getIndex()));
			eventHeader.setAttributeNode(attr);

			Element title = doc.createElement("title");
			title.appendChild(doc.createTextNode(event.getTitle()));
			eventHeader.appendChild(title);

			Element description = doc.createElement("description");
			description.appendChild(doc.createTextNode(event.getDescription()));
			eventHeader.appendChild(description);

			Element location = doc.createElement("location");
			location.appendChild(doc.createTextNode(event.getLocation()));
			eventHeader.appendChild(location);

			Element startDate = doc.createElement("startDate");
			startDate.appendChild(doc.createTextNode(event.getStartDate()));
			eventHeader.appendChild(startDate);

			if(event.getStartTime() != null) {
				Element startTime = doc.createElement("startTime");
				startTime.appendChild(doc.createTextNode(event.getStartTime()));
				eventHeader.appendChild(startTime);				
			}
			
			Element endDate = doc.createElement("endDate");
			endDate.appendChild(doc.createTextNode(event.getEndDate()));
			eventHeader.appendChild(endDate);
			
			if(event.getEndTime() != null) {
				Element endTime = doc.createElement("endTime");
				endTime.appendChild(doc.createTextNode(event.getEndTime()));
				eventHeader.appendChild(endTime);				
			}	
			if(event.getAlarmDateTime() != null) {
				Element timerDateTime = doc.createElement("timerDateTime");
				timerDateTime.appendChild(doc.createTextNode(event.getAlarmDateTime().toString()));
				eventHeader.appendChild(timerDateTime);
			}
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			
			DOMSource source = new DOMSource(doc);			
			File outputFile = new File(filename);
			StreamResult result = new StreamResult(outputFile);
			transformer.transform(source, result);
						
			System.out.println("New file created and saved!");
				
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

	public void parseXml(String filePath) {
		try {
			ArrayList<String> values = new ArrayList<String>();
			
			File fXmlFile = new File(filePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
					
			NodeList nList = doc.getElementsByTagName("Event");

			for (int item = 0; item < nList.getLength(); item++) {

				Node nNode = nList.item(item);
				System.out.println("\nCurrent Element :" + nNode.getParentNode().getNodeName());
				
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					
					Element eElement = (Element) nNode;
					
					values.add(eElement.getNodeName() + " id: " + eElement.getAttribute("id"));
					
					values.add("title: " + eElement.getElementsByTagName("title").item(0).getTextContent());
					values.add("description: " + eElement.getElementsByTagName("description").item(0).getTextContent());
					values.add("location: " + eElement.getElementsByTagName("location").item(0).getTextContent());
					values.add("startDate: " + eElement.getElementsByTagName("startDate").item(0).getTextContent());
					values.add("startTime: " + eElement.getElementsByTagName("startTime").item(0).getTextContent());
					values.add("endDate: " + eElement.getElementsByTagName("endDate").item(0).getTextContent());
					values.add("endTime: " + eElement.getElementsByTagName("endTime").item(0).getTextContent());
					values.add("timerDateTime: " + eElement.getElementsByTagName("timerDateTime").item(0).getTextContent());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> readXml(String filePath) throws IOException {		
		ArrayList<String> readFileContent = new ArrayList<String>();		
		try {
			readFileContent = (ArrayList<String>) Files.readAllLines(Paths.get(filePath));						
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return readFileContent;
	}

	public void appendXml(Event event, String filename) throws SAXException, IOException {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(filename);
	        Element rootElement = doc.getDocumentElement();
	        
	        Element eventHeader = doc.createElement("Event");
			rootElement.appendChild(eventHeader);

			Attr attr = doc.createAttribute("id");
			attr.setValue(Integer.toString(event.getIndex()));
			eventHeader.setAttributeNode(attr);

			Element title = doc.createElement("title");
			title.appendChild(doc.createTextNode(event.getTitle()));
			eventHeader.appendChild(title);

			Element description = doc.createElement("description");
			description.appendChild(doc.createTextNode(event.getDescription()));
			eventHeader.appendChild(description);

			Element location = doc.createElement("location");
			location.appendChild(doc.createTextNode(event.getLocation()));
			eventHeader.appendChild(location);

			Element startDate = doc.createElement("startDate");
			startDate.appendChild(doc.createTextNode(event.getStartDate()));
			eventHeader.appendChild(startDate);
			
			if(event.getStartTime() != null) {
				Element startTime = doc.createElement("startTime");
				startTime.appendChild(doc.createTextNode(event.getStartTime()));
				eventHeader.appendChild(startTime);
			}

			Element endDate = doc.createElement("endDate");
			endDate.appendChild(doc.createTextNode(event.getEndDate()));
			eventHeader.appendChild(endDate);		
			
			if(event.getEndTime() != null) {
				Element endTime = doc.createElement("endTime");
				endTime.appendChild(doc.createTextNode(event.getEndTime()));
				eventHeader.appendChild(endTime);
			}
			
			Element timerDateTime = doc.createElement("timerDateTime");
			timerDateTime.appendChild(doc.createTextNode(event.getAlarmDateTime().toString()));
			eventHeader.appendChild(timerDateTime);
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(filename);
			transformer.transform(source, result);
			
			System.out.println("Text appended and saved!");

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

}
