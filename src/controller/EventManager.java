package controller;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.toedter.calendar.JDateChooser;

import controller.exception.EventManagerException;
import model.DataIO;
import model.Event;
import model.exception.EventEmptyFieldException;
import model.exception.EventInvalidDateException;
import model.exception.EventInvalidTimeException;
import model.exception.TimerDateTimeException;
import net.fortuna.ical4j.model.ValidationException;
import view.EventWindow;
import view.MainWindow;

public class EventManager {
	/**
	 * 	Pole databaseConnected będące flagą połączenia z bazą danych
	 */
	private boolean databaseConnected = false;
	/**
	 * 	Pole config przechowujące dane konfiguracyjne połączenia z bazą danych
	 */
	HashMap<String, String> config = null;
	/**
	 * 	Pole eventCollection przechowujące kolekcję wszystkich Eventów w liście
	 */
	private ArrayList<Event> eventCollection = new ArrayList<Event>();
	/**
	 * 	Pole mainWindow łączące warstwę logiki z warstwą widoku
	 */
	private MainWindow mainWindow;
	/**
	 * 	Pole dataIo łączące warstwę logiki z warstwą danych
	 */
	private DataIO dataIo = new DataIO();
	/**
	 * 	Pole clip niezbędne do uruchomienia pliku dźwiękowego dla alarmu
	 */
	private Clip clip;

	/**
	 * 	Konstruktor tworzy obiekt klasy EventManager, który jest odpowiedzialny za zarządzanie Eventami oraz przekazywanie danych o nich do innych warstw aplikacji
	 * @param mainWindow - obiekt reprezentujący główne okno aplikacji
	 * @throws LineUnavailableException - wyjątek zostaje rzucony, gdy wystąpi błąd związany z uruchomieniem pliku dźwiękowego
	 * @throws IOException - wyjątek zostaje rzucony, gdy wystąpi błąd związany z otwarciem pliku dźwiękowego
	 * @throws UnsupportedAudioFileException - wyjątek zostaje rzucony, gdy wystąpi błąd związany z otwarciem pliku dźwiękowego
	 * @throws EventManagerException - wyjątek zostaje rzucony, gdy nastąpi błąd ogólny modułu zarządzającego (np. bład odczytu konfiguracji)
	 */
	public EventManager(MainWindow mainWindow)
			throws LineUnavailableException, IOException, UnsupportedAudioFileException, EventManagerException {
		this.mainWindow = mainWindow;

		loadConfig();
		if(connectToDatabase()) importEventsFromDatabase();

		clip = AudioSystem.getClip();
		AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File("resources/alarm.wav"));
		clip.open(inputStream);
		Thread alarmCheckThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						for (Event event : eventCollection) {
							if (event.getAlarmDateTime() != null) {
								Date date = new Date();

								int comparedTime = date.compareTo(event.getAlarmDateTime());
								if (comparedTime >= 0) {
									clip.start();
									mainWindow.showInformationMessageDialog("Alarm",
											"Title: " + event.getTitle() + "\nStart: " + event.getStartDate() + " "
													+ "\nEnd: " + event.getEndDate() + " "
													+ "\n");
									clip.stop();
									event.setAlarmDateTime(null);
									try {
										dataIo.updateEventInDatabase(event);
									} catch (SQLException e) {}
								}
							}
						}

						Thread.sleep(1000);

					} catch (InterruptedException | TimerDateTimeException | EventInvalidTimeException e) {
					} 
				}
			}
		});

		alarmCheckThread.start();
	}

	/**
	 * Metoda ładuje dane konfiguracyjne dotyczące połączenia z bazą danych do aplikacji
	 * @throws EventManagerException - wyjątek zostaje rzucony, gdy nastąpi błąd odczytu konfiguracji
	 */
	public void loadConfig() throws EventManagerException
	{
		try {
			config = dataIo.loadConfig();
		} catch (IOException e) {
			throw new EventManagerException("Couldn't load config from file.\nPleae, check for config file in application directory.");
		}
		
	}
	
	/**
	 * Metoda zapisuje dane konfiguracyjne dotyczące połączenia z bazą danych do pliku
	 * @throws EventManagerException - wyjątek zostaje rzucony, gdy nastąpi błąd dostępu do pliku konfiguracyjnego
	 */
	public void saveConfig() throws EventManagerException
	{
		try {
			dataIo.saveConfig(config);
		} catch (IOException e) {
			throw new EventManagerException("Couldn't open config from file.\nPleae, check for config file in application directory.");
		}	
	}
	
	/**
	 * Metoda realizuje połączenie aplikacji z bazą danych
	 * @return databaseConnected - flaga wskazująca, czy połączenie zostało ustanowione
	 */
	public boolean connectToDatabase()
	{
		String server = config.get("hostname");
		String port = config.get("port");
		String database = config.get("database");
		String user = config.get("username");
		String password = config.get("password");
		if(server == null || database == null || user == null || password == null) return false;
		
		try {
			dataIo.connectToDatabase(server, port, database, user, password);
			databaseConnected = true;
		} catch (SQLException e) {
		    databaseConnected = false;
		}
		
		return databaseConnected;
	}
	
	public boolean isDatabaseConnected() {
		return databaseConnected;
	}

	public HashMap<String, String> getConfig() {
		return config;
	}

	public ArrayList<Event> getEventCollection() {
		return eventCollection;
	}

	@Override
	public String toString() {
		return "EventManager [eventCollection=" + eventCollection.toString() + "]";
	}

	/**
	 * 	Metoda przekazuje dane o konkretnym Evencie do warstwy danych, gdzie następuje zapis informacji do pliku XML
	 * @param eventId - ID wybranego Eventu
	 * @param filename - nazwa pliku, do którego zapisane zostaną dane
	 */
	public void sendDataToXml(int eventId, String filename) {		
		for (Event event :  eventCollection) {
			if (event.getIndex() == eventId) {				
				dataIo.writeToXml(event, filename);
				return;
			}
		}				
	}
	
	/**
	 * 	Metoda tworzy Event, dodaje go do kolekcji oraz zapisuje w bazie danych. Jako parametry przyjmuje dane niezbędne do stworzenia Eventu
	 * @param titleValue - tytuł Eventu
	 * @param descriptionValue - opis Eventu
	 * @param locationValue - lokalizacja Eventu
	 * @param startDateValue - data początkowa Eventu
	 * @param endDateValue - data końcowa Eventu
	 * @param alarmDateTimeValue - czas uruchomienia alarmu
	 * @throws EventManagerException - wyjątek zostaje rzucony, gdy podane zostaną błędne parametry
	 */
	public void addEvent(String titleValue, String descriptionValue, String locationValue, Date startDateValue, Date endDateValue, Date alarmDateTimeValue)
			throws EventManagerException {
		try {
			Event event = new Event(titleValue, descriptionValue, locationValue, startDateValue, endDateValue, alarmDateTimeValue);
			int id = dataIo.insertEventToDatabase(event);
			if(id == 0) throw new EventManagerException("Invalid ID returned from database.");
			eventCollection.add(event);					
			eventCollection.sort(null);
			event.setIndex(id);

		} catch (EventEmptyFieldException eventEmptyFieldException) {
			throw new controller.exception.EventManagerException("Invalid values in fields, please correct");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 	Metoda usuwa Event z kolekcji oraz z bazy danych
	 * @param eventId - numer indeksu Eventu do usunięcia
	 * @throws EventManagerException - wyjątek zostaje rzucony, gdy podany zostanie błędny parametr
	 * @throws SQLException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z dostępem do bazy danych
	 */
	public void removeEvent(int eventId) throws EventManagerException, SQLException {
		for (Event event : eventCollection) {
			if (event.getIndex() == eventId) {
				dataIo.deleteEventFromDatabase(event);
				eventCollection.remove(event);
				return;
			}
		}
		throw new controller.exception.EventManagerException("Provided event ID doesn't exist");
	}

	private String fetchSelectedDate() {
		return mainWindow.getEventDate();
	}

	/**
	 * 	Metoda pobiera wartość wybranej daty z okna głównego i przekazuje ją do stworzonego okna kreatora Eventów
	 * @param eventWindow - instancja głównego okna aplikacji
	 */
	public void fillStartDateField(EventWindow eventWindow) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(fetchSelectedDate());

					JDateChooser startDayField = eventWindow.getStartDateField();
					startDayField.setDate(startDate);
					startDayField.setMinSelectableDate(startDate);
					eventWindow.startDateFieldValue = fetchSelectedDate();

					JDateChooser chooser = eventWindow.getChooser();
					chooser.setDateFormatString("yyyy-MM-dd");
					chooser.getDateEditor().setDate(startDate);
					chooser.setMinSelectableDate(startDate);
					eventWindow.setChooser(chooser);

					eventWindow.frmEventCreator.setLocationRelativeTo(null);
					eventWindow.frmEventCreator.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 	Metoda oblicza datę oraz godzinę uruchomienia alarmu
	 * @param timerValue - data określająca, na jaki czas przed startem Eventu ma uruchomić się alarm
	 * @param startDateValue - data określająca datę początkową Eventu
	 * @return Date - zmienna typu Date określająca datę i godzinę uruchomienia alarmu
	 */
	public Date setAlarmGoOffDate(Date timerValue, Date startDateValue) {
		String timerTime = DataIO.parseDateToStringTimeOnly(timerValue);
		String startDate = DataIO.parseDateToString(startDateValue);

		int timerHour, timerMinute, startHour, startMinute, year, month, day;
		timerHour = Integer.parseInt(timerTime.substring(0, 2));
		timerMinute = Integer.parseInt(timerTime.substring(3, 5));
		year = Integer.parseInt(startDate.substring(0, 4));
		month = Integer.parseInt(startDate.substring(5, 7)) - 1;
		day = Integer.parseInt(startDate.substring(8, 10));
		startHour = Integer.parseInt(startDate.substring(11, 13));
		startMinute = Integer.parseInt(startDate.substring(14, 16));

		GregorianCalendar calendarStop = new GregorianCalendar(year, month, day, startHour - timerHour,
				startMinute - timerMinute, 0);
		return calendarStop.getTime();
	}
	
	/**
	 * 	Metoda pobiera Eventy z bazy danych
	 */
	public void importEventsFromDatabase()
	{
		try {
			eventCollection = dataIo.getEventsFromDatabase();
		} catch (SQLException e) {
		    System.out.println("SQLException: " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		}
	}

	/**
	 * 	Metoda importuje istniejące Eventy z pliku XML, zapisuje je do bazy danych oraz do kolekcji
	 * @param filename - nazwa pliku, z którego zostaną zaimportowane dane
	 * @throws ParseException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z parsowaniem daty
	 * @throws EventEmptyFieldException - wyjątek zostaje rzucony, gdy nastąpi błąd związany ze stworzeniem Eventu
	 * @throws EventInvalidDateException - wyjątek zostaje rzucony, gdy nastąpi błąd związany ze stworzeniem Eventu
	 * @throws EventInvalidTimeException - wyjątek zostaje rzucony, gdy nastąpi błąd związany ze stworzeniem Eventu
	 * @throws TimerDateTimeException - wyjątek zostaje rzucony, gdy nastąpi błąd związany ze stworzeniem Eventu
	 * @throws SQLException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z dostępem do bazy danych
	 */
	public void importEventsFromXml(String filename) throws ParseException, EventEmptyFieldException, EventInvalidDateException,
			EventInvalidTimeException, TimerDateTimeException, SQLException {
		NodeList nList = dataIo.getNodeListFromXml(filename);
		if (nList != null) {
			for (int item = 0; item < nList.getLength(); item++) {

				Node nNode = nList.item(item);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String title, description, location, startDateValue, endDateValue, timerDateTime = "";
					title = eElement.getElementsByTagName("title").item(0).getTextContent();
					description = eElement.getElementsByTagName("description").item(0).getTextContent();
					location = eElement.getElementsByTagName("location").item(0).getTextContent();
					startDateValue = eElement.getElementsByTagName("startDate").item(0).getTextContent();
					endDateValue = eElement.getElementsByTagName("endDate").item(0).getTextContent();
					NodeList timerNl = eElement.getElementsByTagName("timerDateTime");
					if (timerNl.getLength() > 0)
						timerDateTime = eElement.getElementsByTagName("timerDateTime").item(0).getTextContent();
					Date startDate = null, endDate = null, timerDate = null;
					if (!startDateValue.isEmpty())
						startDate = DataIO.parseStringToDate(startDateValue);
					if (!endDateValue.isEmpty())
						endDate = DataIO.parseStringToDate(endDateValue);
					if (!timerDateTime.isEmpty())
						timerDate = DataIO.parseStringToDate(timerDateTime);

					Event fetchedEvent = new Event(title, description, location, startDate, endDate, timerDate);
					int fetchedEventIndex = dataIo.insertEventToDatabase(fetchedEvent);
					fetchedEvent.setIndex(fetchedEventIndex);
				
					eventCollection.add(fetchedEvent);
				}
			}
		} else
			return;
	}

	/**
	 * Metoda filtruje dane zawarte w tabeli Eventów
	 * @param table - tabela zawierająca dane o Eventach w oknie głównym aplikacji
	 * @param field - pole, według którego filtrowane są dane
	 */
	public void filterEventsTable(JTable table, String field) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<DefaultTableModel>(model);
		table.setRowSorter(sorter);
		sorter.setRowFilter(RowFilter.regexFilter(field));
	}
	
	/**
	 * Metoda deleguje eksport danych o Evencie do iCalendara
	 * @param eventId - numer indeksu Eventu do eksportu
	 * @param filename - nazwa pliku, do którego wyeksportowane zostaną dane
	 * @throws IOException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z eksportem danych do pliku
	 * @throws ValidationException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z eksportem danych do pliku
	 * @throws ParseException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z eksportem danych do pliku
	 */
	public void exportToICalendar(int eventId, String filename) throws IOException, ValidationException, ParseException {
		for (Event event :  eventCollection) {
			if (event.getIndex() == eventId) {				
				dataIo.saveICalendar(event, filename);
				return;
			}
		}
	}

}
