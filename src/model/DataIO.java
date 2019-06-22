package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;

import model.exception.EventEmptyFieldException;
import model.exception.EventInvalidDateException;
import model.exception.EventInvalidTimeException;
import model.exception.TimerDateTimeException;

public class DataIO {
	
	Connection dataBaseConnecion = null;
	
	public HashMap<String, String> loadConfig() throws IOException
	{
		HashMap<String, String> config = new HashMap<String, String>();
		
		File configFile = new File("config.cfg");
		BufferedReader configFileReader = new BufferedReader(new FileReader(configFile));
		String line = configFileReader.readLine();
		while(line != null)
		{
			String[] option = line.split("=");
			if(option.length < 2) continue;
			config.put(option[0], option[1]);
			line = configFileReader.readLine();
		}
		configFileReader.close();
		return config;
	}
	
	public void saveConfig(HashMap<String, String> config) throws IOException
	{
		File configFile = new File("config.cfg");
		BufferedWriter configFileWriter = new BufferedWriter(new FileWriter(configFile));
		configFileWriter.write("hostname=" + config.get("hostname") + "\n");
		configFileWriter.write("port=" + config.get("port") + "\n");
		configFileWriter.write("database=" + config.get("database") + "\n");
		configFileWriter.write("username=" + config.get("username") + "\n");
		configFileWriter.write("password=" + config.get("password"));
		configFileWriter.close();
	}
	
	public void connectToDatabase(String server, String port, String database, String user, String password) throws SQLException
	{
		String url = "jdbc:mysql://"+server+":"+port+"/"+database;
		dataBaseConnecion = DriverManager.getConnection(url, user, password);
	}
	
	/**
	 * 	Pole dataFile przechowujące instancję pliku z bazą danych.
	 */
	private File dataFile;

	private void setDataFile(File dataFile) {
		this.dataFile = dataFile;
	}

	/**
	 * 	Konstruktor tworzy obiekt klasy DataIO, odpowiedzialnej za odczyt oraz zapis danych do bazy danych.
	 */
	public DataIO() {}

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
			startDate.appendChild(doc.createTextNode(parseDateToString(event.getStartDate())));
			eventHeader.appendChild(startDate);

			Element endDate = doc.createElement("endDate");
			endDate.appendChild(doc.createTextNode(parseDateToString(event.getEndDate())));
			eventHeader.appendChild(endDate);

			if (event.getAlarmDateTime() != null) {
				Element timerDateTime = doc.createElement("timerDateTime");
				timerDateTime.appendChild(doc.createTextNode(parseDateToString(event.getAlarmDateTime())));
				eventHeader.appendChild(timerDateTime);
			}

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			DOMSource source = new DOMSource(doc);
			dataFile = new File(filename);

			StreamResult result = new StreamResult(dataFile);

			transformer.transform(source, result);			

		} catch (ParserConfigurationException | TransformerException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Event> getEventsFromDatabase() throws SQLException
	{
		if(dataBaseConnecion == null) throw new SQLException("No database connected");
		Statement statement = dataBaseConnecion.createStatement();
		ResultSet result = statement.executeQuery("SELECT * FROM events ORDER BY start");
		
		ArrayList<Event> events = new ArrayList<Event>();
		
		while(result.next())
		{
			int id = result.getInt("id");
			String title = result.getString("title");
			String location = result.getString("location");
			String description = result.getString("description");
			Date start = new Date(result.getTimestamp("start").getTime());
			Date end = new Date(result.getTimestamp("end").getTime());
			Date notification = null;
			Timestamp notificationTimestamp = result.getTimestamp("notification");
			if(notificationTimestamp != null) notification = new Date(result.getTimestamp("notification").getTime()); 
			try {
				Event event = new Event(title, description, location, start, end, notification);
				event.setIndex(id);
				events.add(event);
			} catch (EventEmptyFieldException | EventInvalidDateException | EventInvalidTimeException | TimerDateTimeException e) {
				System.out.println(e.getMessage());
				continue;
			}
		}
		
		result.close();
		statement.close();
		return events;
	}
	
	private void prepareEventStatement(Event event, PreparedStatement statement) throws SQLException
	{
		statement.setString(1, event.getTitle());
		statement.setString(2, event.getLocation());
		statement.setString(3, event.getDescription());
		statement.setTimestamp(4, new Timestamp(event.getStartDate().getTime()));
		statement.setTimestamp(5, new Timestamp(event.getEndDate().getTime()));
		
		Timestamp notification = null;
		if(event.getAlarmDateTime() != null) notification = new Timestamp(event.getAlarmDateTime().getTime());
		statement.setTimestamp(6, notification);
	}
	
	public int insertEventToDatabase(Event event) throws SQLException, IOException
	{		
		if(dataBaseConnecion == null) throw new SQLException("No database connected");
		PreparedStatement statement = dataBaseConnecion.prepareStatement("INSERT INTO events VALUES (null, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
		prepareEventStatement(event, statement);
		statement.executeUpdate();
		ResultSet generatedKey = statement.getGeneratedKeys();
		if(generatedKey.next()) return generatedKey.getInt(1);
		return 0;
	}
	
	public void updateEventInDatabase(Event event) throws SQLException
	{
		if(dataBaseConnecion == null) throw new SQLException("No database connected");
		PreparedStatement statement = dataBaseConnecion.prepareStatement("UPDATE events SET title = ?, location = ?, description = ?, start = ?, end = ?, notification = ? WHERE id = ?");
		prepareEventStatement(event, statement);
		statement.setInt(7, event.getIndex());
		statement.executeUpdate();
	}
	
	public void deleteEventFromDatabase(Event event) throws SQLException
	{
		if(dataBaseConnecion == null) throw new SQLException("No database connected");
		PreparedStatement statement = dataBaseConnecion.prepareStatement("DELETE FROM events WHERE id = ?");
		statement.setInt(1, event.getIndex());
		statement.executeUpdate();
	}
	
	/**
	 * Metoda pobiera listę węzłów z pliku XML, w którym przechowywane są dane o Eventach                  
	 * @return NodeList - obiekt przechowujący listę węzłów zawierających dane o Eventach
	 */
	public NodeList getNodeListFromXml(String filename) {
		NodeList nList = null;

		try {
			setDataFile(new File(filename));
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(filename);
			doc.getDocumentElement().normalize();
			nList = doc.getElementsByTagName("Event");			
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
				if (path != null && !path.isEmpty()) {
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
			startDate.appendChild(doc.createTextNode(parseDateToString(event.getStartDate())));
			eventHeader.appendChild(startDate);

			Element endDate = doc.createElement("endDate");
			endDate.appendChild(doc.createTextNode(parseDateToString(event.getEndDate())));
			eventHeader.appendChild(endDate);

			if (event.getAlarmDateTime() != null) {
				Element timerDateTime = doc.createElement("timerDateTime");
				timerDateTime.appendChild(doc.createTextNode(parseDateToString(event.getAlarmDateTime())));
				eventHeader.appendChild(timerDateTime);
			}

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(dataFile);
			transformer.transform(source, result);

		} catch (ParserConfigurationException | TransformerException pce) {
		}
	}
	
	public static Connection testConnectionToDatabase(String server, String port, String database, String user, String password) throws SQLException
	{
		String url = "jdbc:mysql://"+server+":"+port+"/"+database;
		return DriverManager.getConnection(url, user, password);
	}

	public void saveICalendar(Event event, String filename) throws IOException, ValidationException, ParseException {

		Calendar iCal = new Calendar();
		iCal.getProperties().add(new ProdId("-//Project-Orgaznier//ICalendar 1.0//EN"));
		iCal.getProperties().add(Version.VERSION_2_0);
		iCal.getProperties().add(CalScale.GREGORIAN);
				
		VEvent eventICal = new VEvent();		
		eventICal.getProperties().add(new Uid(event.getIndex().toString()));				
		final DtStart dtStart = new DtStart(new net.fortuna.ical4j.model.DateTime(event.getStartDate()));
		eventICal.getProperties().add(dtStart);		
		final DtEnd dtEnd = new DtEnd(new net.fortuna.ical4j.model.DateTime(event.getEndDate()));
		eventICal.getProperties().add(dtEnd);					
		eventICal.getProperties().add(new Summary(event.getTitle()));
		eventICal.getProperties().add(new Location(event.getLocation()));			
		eventICal.getProperties().add(new Description(event.getDescription()));				
		
		if (event.getAlarmDateTime() != null) { 			
			DateTime alarmDateTime = new DateTime(parseDateToString(event.getAlarmDateTime()), "yyyy-MM-dd HH:mm:ss", null);
			VAlarm alarmICal = new VAlarm(alarmDateTime);			
			alarmICal.getProperties().add(Action.AUDIO);
			eventICal.getAlarms().add(alarmICal);	
		}
		
		iCal.getComponents().add(eventICal);		
		File exportIcsFile = new File(filename);
		
		if (!exportIcsFile.exists()) {
			exportIcsFile.createNewFile();
        }
		
		FileOutputStream iCalendarOut = new FileOutputStream(exportIcsFile);
		CalendarOutputter outputter = new CalendarOutputter();
		outputter.output(iCal, iCalendarOut);
		iCalendarOut.close();
	}

	public static String parseDateToString(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strDate = dateFormat.format(date);
		return strDate;
	}

	public static String parseDateToStringDateOnly(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = dateFormat.format(date);
		return strDate;
	}

	public static String parseDateToStringTimeOnly(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		String strDate = dateFormat.format(date);
		return strDate;
	}

	public static Date parseStringToDate(String date) throws ParseException {
		DateFormat resultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date result = (Date) resultFormat.parse(date);
		return result;
	}
}
