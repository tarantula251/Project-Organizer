package model;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DataIO {
	
	/**
	 * 	Pole dataFile przechowujące instancję pliku z bazą danych.
	 */
	private File dataFile;

	public File getDataFile() {
		return dataFile;
	}

	public void setDataFile(File dataFile) {
		this.dataFile = dataFile;
	}

	/**
	 * 	Konstruktor tworzy obiekt klasy DataIO, odpowiedzialnej za odczyt oraz zapis danych do bazy danych.
	 */
	public DataIO() {
	}

	/**
	 * Metoda tworzy nowy plik będący bazą danych oraz zapisuje do niego dane nowo utworzonego Eventu.                       
	 * @param event - obiekt Event, którego dane zostaną zapisane do pliku
	 * @param filename - nazwa pliku, do którego nastąpi zapis     
	 */
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

			Element startTime = doc.createElement("startTime");
			startTime.appendChild(doc.createTextNode(event.getStartTime()));
			eventHeader.appendChild(startTime);

			Element endDate = doc.createElement("endDate");
			endDate.appendChild(doc.createTextNode(event.getEndDate()));
			eventHeader.appendChild(endDate);

			Element endTime = doc.createElement("endTime");
			endTime.appendChild(doc.createTextNode(event.getEndTime()));
			eventHeader.appendChild(endTime);

			if (event.getAlarmDateTime() != null) {
				Element timerDateTime = doc.createElement("timerDateTime");
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String alarmString = simpleDateFormat.format(event.getAlarmDateTime());
				timerDateTime.appendChild(doc.createTextNode(alarmString));
				eventHeader.appendChild(timerDateTime);
			}

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			DOMSource source = new DOMSource(doc);
			dataFile = new File(filename);

			StreamResult result = new StreamResult(dataFile);

			transformer.transform(source, result);

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

	/**
	 * Metoda pobiera listę węzłów z pliku XML, w którym przechowywane są dane o Eventach                  
	 * @return NodeList - obiekt przechowujący listę węzłów zawierających dane o Eventach
	 */
	public NodeList getNodeListFromXml() {
		NodeList nList = null;

		try {
			File dir = new File("databank");
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.equalsIgnoreCase("data.xml");
				}
			};
			String[] fetchedDataFile = dir.list(filter);

			if (fetchedDataFile != null) {
				setDataFile(new File(dir + "/data.xml"));
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse("databank/" + fetchedDataFile[0]);
				doc.getDocumentElement().normalize();
				nList = doc.getElementsByTagName("Event");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nList;
	}

	/**
	 * Metoda odczytuje zawartość pliku XML z danymi o Eventach i zapisuje je do listy.                
	 * @return ArrayList - obiekt przechowujący odczytaną zawartość pliku XML
	 * @throws IOException - wyjątek zostaje rzucony, gdy nastąpi problem z otwarciem pliku
	 */
	public ArrayList<String> readXml() throws IOException {

		ArrayList<String> readFileContent = new ArrayList<String>();
		try {
			if (dataFile.exists()) {
				String path = dataFile.getPath();
				if (path != null) {
					readFileContent = (ArrayList<String>) Files.readAllLines(Paths.get(path));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return readFileContent;
	}

	/**
	 * Metoda dopisuje do istniejącego pliku XML dane o nowo utworzonym Evencie.
	 * @param event - obiekt Event, którego dane zostaną zapisane do pliku
	 * @throws SAXException - wyjątek zostaje rzucony, gdy nastąpi błąd parsowania pliku XML          
	 * @throws IOException - wyjątek zostaje rzucony, gdy nastąpi problem z otwarciem pliku
	 */
	public void appendXml(Event event) throws SAXException, IOException {

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(dataFile);
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

			Element startTime = doc.createElement("startTime");
			startTime.appendChild(doc.createTextNode(event.getStartTime()));
			eventHeader.appendChild(startTime);

			Element endDate = doc.createElement("endDate");
			endDate.appendChild(doc.createTextNode(event.getEndDate()));
			eventHeader.appendChild(endDate);

			Element endTime = doc.createElement("endTime");
			endTime.appendChild(doc.createTextNode(event.getEndTime()));
			eventHeader.appendChild(endTime);

			if (event.getAlarmDateTime() != null) {
				Element timerDateTime = doc.createElement("timerDateTime");
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String alarmString = simpleDateFormat.format(event.getAlarmDateTime());
				timerDateTime.appendChild(doc.createTextNode(alarmString));
				eventHeader.appendChild(timerDateTime);
			}

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(dataFile);
			transformer.transform(source, result);

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

}
