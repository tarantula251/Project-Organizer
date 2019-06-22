package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
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
	/**
	 * 	Pole dataBaseConnecion będące instancją połączenia z bazą danych
	 */
	Connection dataBaseConnecion = null;
	
	/**
	 * 	Metoda odczytuje plik konfiguracyjny zawierający dane niezbędne do skonfigurowania połączenia z bazą danych
	 * @return config - mapa zawierające odczytane dane konfiguracyjne
	 * @throws IOException - wyjątek zostanie rzucony, gdy wystąpi bład przy odczytywaniu zawartości pliku
	 */
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
	
	/**
	 * 	Metoda tworzy oraz zapisuje dane do pliku konfiguracyjnego, wykorzystywanego przy inicjowaniu połączenia z bazą danych
	 * @param config - mapa przechowująca dane konfiguracyjne
	 * @throws IOException - wyjątek zostanie rzucony, gdy wystąpi bład przy zapisie danych do pliku
	 */
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
	
	/**
	 * 	Metoda inicjuje połączenie aplikacji z zewnętrzną bazą danych
	 * @param server - nazwa serwera przechowującego bazę danych
	 * @param port - nazwa portu, poprzez który realizowane jest połączenie z bazą danych
	 * @param database - nazwa bazy danych
	 * @param user - nazwa użytkownika inicjującego połączenie
	 * @param password - hasło przypisane do użytkownika
	 * @throws SQLException - wyjątek zostanie rzucony, gdy wystąpi bład przy inicjowaniu połączenia z bazą danych
	 */
	public void connectToDatabase(String server, String port, String database, String user, String password) throws SQLException
	{
		String url = "jdbc:mysql://"+server+":"+port+"/"+database;
		dataBaseConnecion = DriverManager.getConnection(url, user, password);
	}
	
	/**
	 * 	Pole dataFile przechowujące instancję pliku z bazą danych
	 */
	private File dataFile;

	private void setDataFile(File dataFile) {
		this.dataFile = dataFile;
	}

	/**
	 * 	Konstruktor tworzy obiekt klasy DataIO, odpowiedzialnej za odczyt oraz zapis danych do bazy danych oraz zewnętrznych plików
	 */
	public DataIO() {}

	/**
	 * Metoda tworzy nowy plik XML oraz zapisuje do niego dane wybranego Eventu                       
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

	/**
	 * Metoda pobiera z bazy dane dotyczące Eventów oraz tworzy na ich podstawie obiekty typu Event, które zapisuje do kolekcji
	 * @return events - kolekcja wypełniona Eventami
	 * @throws SQLException - wyjątek zostanie rzucony, gdy wystąpi błąd związany z dostępem do bazy danych
	 */
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
	
	/**
	 * Metoda tworzy strukturę tabeli w bazie danych przechowującej dane o Eventach
	 * @param event - obiekt typu Event, którego dane zostaną umieszczone w tabeli
	 * @param statement - polecenie dla bazy danych
	 * @throws SQLException - wyjątek zostanie rzucony, gdy wystąpi błąd związany z dostępem do bazy danych
	 */
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
	
	/**
	 * Metoda wstawia do bazy danych informacje o konkretnym Evencie
	 * @param event - obiekt typu Event, którego dane zostaną umieszczone w bazie
	 * @return int - wartość typu int reprezentująca ID Eventu w bazie danych
	 * @throws SQLException - wyjątek zostanie rzucony, gdy wystąpi błąd związany z dostępem do bazy danych
	 */
	public int insertEventToDatabase(Event event) throws SQLException
	{		
		if(dataBaseConnecion == null) throw new SQLException("No database connected");
		PreparedStatement statement = dataBaseConnecion.prepareStatement("INSERT INTO events VALUES (null, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
		prepareEventStatement(event, statement);
		statement.executeUpdate();
		ResultSet generatedKey = statement.getGeneratedKeys();
		if(generatedKey.next()) return generatedKey.getInt(1);
		return 0;
	}
	
	/**
	 * Metoda aktualizuje dane o konkretnym Evencie w bazie danych
	 * @param event - obiekt typu Event, którego dane zostaną zaktualizowane w bazie
	 * @throws SQLException - wyjątek zostanie rzucony, gdy wystąpi błąd związany z dostępem do bazy danych
	 */
	public void updateEventInDatabase(Event event) throws SQLException
	{
		if(dataBaseConnecion == null) throw new SQLException("No database connected");
		PreparedStatement statement = dataBaseConnecion.prepareStatement("UPDATE events SET title = ?, location = ?, description = ?, start = ?, end = ?, notification = ? WHERE id = ?");
		prepareEventStatement(event, statement);
		statement.setInt(7, event.getIndex());
		statement.executeUpdate();
	}
	
	/**
	 * Metoda usuwa dane o konkretnym Evencie z bazy danych
	 * @param event - obiekt typu Event, którego dane zostaną usunięte z bazy
	 * @throws SQLException - wyjątek zostanie rzucony, gdy wystąpi błąd związany z dostępem do bazy danych
	 */
	public void deleteEventFromDatabase(Event event) throws SQLException
	{
		if(dataBaseConnecion == null) throw new SQLException("No database connected");
		PreparedStatement statement = dataBaseConnecion.prepareStatement("DELETE FROM events WHERE id = ?");
		statement.setInt(1, event.getIndex());
		statement.executeUpdate();
	}
	
	/**
	 * Metoda pobiera listę węzłów z pliku XML, w którym przechowywane są dane o Eventach       
	 * @param filename - nazwa pliku XML, z którego pobierana jest lista węzłów           
	 * @return nList - obiekt typu NodeList przechowujący listę węzłów zawierających dane o Eventach w pliku XML
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
	 * Metoda testuje poprawność połączenia z bazą danych
	 * @param server - nazwa serwera przechowującego bazę danych
	 * @param port - nazwa portu, poprzez który realizowane jest połączenie z bazą danych
	 * @param database - nazwa bazy danych
	 * @param user - nazwa użytkownika inicjującego połączenie
	 * @param password - hasło przypisane do użytkownika
	 * @return Connection - obiekt będący instancją połączenia z bazą danych
	 * @throws SQLException - wyjątek zostanie rzucony, gdy wystąpi błąd związany z dostępem do bazy danych
	 */
	public static Connection testConnectionToDatabase(String server, String port, String database, String user, String password) throws SQLException
	{
		String url = "jdbc:mysql://"+server+":"+port+"/"+database;
		return DriverManager.getConnection(url, user, password);
	}

	/**
	 * Metoda zapisuje dane o konkretnym Evencie do pliku w formacie iCalendar
	 * @param event - obiekt typu Event, którego dane zostaną zapisane do pliku
	 * @param filename - nazwa pliku, do którego zostaną zapisane dane
	 * @throws IOException - wyjątek zostanie rzucony, gdy wystąpi błąd związany z utworzeniem pliku
	 * @throws ValidationException - wyjątek zostanie rzucony, gdy wystąpi błąd związany z walidacją danych
	 * @throws ParseException - wyjątek zostanie rzucony, gdy wystąpi błąd związany z parsowaniem daty
	 */
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
