package controller;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.sql.SQLException;
import java.text.DateFormat;

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
import view.EventWindow;
import view.MainWindow;

public class EventManager {

	HashMap<String, String> config = null;
	/**
	 * 	Pole eventCollection przechowujÄ…ce kolekcjÄ™ wszystkich EventÃ³w w liÅ›cie
	 */
	private ArrayList<Event> eventCollection = new ArrayList<Event>();
	/**
	 * 	Pole mainWindow Å‚Ä…czÄ…ce wartstwÄ™ logiki z warstwÄ… widoku
	 */
	private MainWindow mainWindow;
	/**
	 * 	Pole dataIo Å‚Ä…czÄ…ce wartstwÄ™ logiki z warstwÄ… danych
	 */
	private DataIO dataIo = new DataIO();
	/**
	 * 	Pole directory okreÅ›lajÄ…ce folder zawierajÄ…cy bazÄ™ danych
	 */
	private String directory = "databank";
	/**
	 * 	Pole eventsFilename okreÅ›lajÄ…ce nazwÄ™ pliku XML z danymi
	 */
	private String eventsFilename = "data.xml";
	/**
	 * 	Pole clip niezbÄ™dne do uruchomienia pliku dÅºwiÄ™kowego dla alarmu
	 */
	private Clip clip;

	/**
	 * 	Konstruktor tworzy obiekt klasy EventManager, ktÃ³ry odpowiedzialny jest za zarzÄ…dzanie Eventami oraz  przekazywanie danych o nich do innych warstw aplikacji
	 * @param mainWindow - obiekt reprezentujÄ…cy gÅ‚Ã³wne okno aplikacji
	 * @throws LineUnavailableException - wyjÄ…tek zostaje rzucony, gdy nastÄ…pi bÅ‚Ä…d zwiÄ…zany z uruchomieniem pliku dÅºwiÄ™kowego
	 * @throws IOException - wyjÄ…tek zostaje rzucony, gdy nastÄ…pi bÅ‚Ä…d zwiÄ…zany z otwarciem pliku dÅºwiÄ™kowego
	 * @throws UnsupportedAudioFileException - wyjÄ…tek zostaje rzucony, gdy nastÄ…pi bÅ‚Ä…d zwiÄ…zany z otwarciem pliku dÅºwiÄ™kowego
	 * @throws ParseException - wyjÄ…tek zostaje rzucony, gdy nastÄ…pi bÅ‚Ä…d zwiÄ…zany z zaimportowaniem EventÃ³w z bazy XML
	 * @throws EventEmptyFieldException - wyjÄ…tek zostaje rzucony, gdy nastÄ…pi bÅ‚Ä…d zwiÄ…zany z utworzeniem EventÃ³w z zaimportowanego pliku XML
	 * @throws EventInvalidDateException - wyjÄ…tek zostaje rzucony, gdy nastÄ…pi bÅ‚Ä…d zwiÄ…zany z utworzeniem EventÃ³w z zaimportowanego pliku XML
	 * @throws EventInvalidTimeException - wyjÄ…tek zostaje rzucony, gdy nastÄ…pi bÅ‚Ä…d zwiÄ…zany z utworzeniem EventÃ³w z zaimportowanego pliku XML
	 * @throws TimerDateTimeException - wyjÄ…tek zostaje rzucony, gdy nastÄ…pi bÅ‚Ä…d zwiÄ…zany z utworzeniem EventÃ³w z zaimportowanego pliku XML
	 * @throws EventManagerException - wyjątek zostje rzucony, gdy nastąpi błąd ogólny modułu zarządzającego (np. bład odczytu konfiguracji)
	 */
	public EventManager(MainWindow mainWindow)
			throws LineUnavailableException, IOException, UnsupportedAudioFileException, ParseException,
			EventEmptyFieldException, EventInvalidDateException, EventInvalidTimeException, TimerDateTimeException, EventManagerException {
		this.mainWindow = mainWindow;

		loadConfig();
		if(connectToDatabase()) importEventsFromDatabase();
		else importEventsFromXml();

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
									mainWindow.showPane("Alarm",
											"Title: " + event.getTitle() + "\nStart: " + event.getStartDate() + " "
													+ "\nEnd: " + event.getEndDate() + " "
													+ "\n");
									clip.stop();
									event.setAlarmDateTime(null);
									createDirectory(directory);
									sendDataToXml(event, directory + "/" + eventsFilename);
								}
							}
						}

						Thread.sleep(1000);

					} catch (InterruptedException | TimerDateTimeException | EventInvalidTimeException | SAXException
							| IOException e) {
						e.printStackTrace();
					}
				}
			}
		});

		alarmCheckThread.start();
	}

	private void loadConfig() throws EventManagerException
	{
		try {
			config = dataIo.loadConfig();
		} catch (IOException e) {
			throw new EventManagerException("Couldn't load config from file.\nPleae, check for config file in application directory.");
		}
		
	}
	
	private boolean connectToDatabase()
	{
		String server = config.get("server");
		String database = config.get("database");
		String user = config.get("user");
		String password = config.get("password");
		if(server == null || database == null || user == null || password == null) return false;
		
		try {
			dataIo.connectToDatabase(server, database, user, password);
			return true;
		} catch (SQLException e) {
		    System.out.println("SQLException: " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
			return false;
		}
		
	}
	
	public ArrayList<Event> getEventCollection() {
		return eventCollection;
	}

	@Override
	public String toString() {
		return "EventManager [eventCollection=" + eventCollection.toString() + "]";
	}

	/**
	 * 	Metoda przekazuje dane o stworzonym Evencie do warstwy danych, gdzie następuje zapis informacji do pliku XML
	 * @param event - obiekt reprezentujący aktualnie stworzony Event
	 * @param filename - nazwa pliku, do którego zapisane zostaną dane
	 * @throws SAXException - wyjątek zostaje rzucony, gdy nastąpi błąd parsowania pliku XML   
	 * @throws IOException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z otwarciem pliku do zapisu
	 */
	private void sendDataToXml(Event event, String filename) throws SAXException, IOException {
		dataIo.writeToXml(event, filename);
		if (eventCollection.size() > 1) {
			for (Event existingEvent : eventCollection) {
				if (existingEvent != event)
					dataIo.appendXml(existingEvent);
			}
		}
	}

	private void createDirectory(String directory) throws IOException {
		var path = Paths.get(directory);
		if (!Files.exists(path))
			Files.createDirectory(path);
	}

	/**
	 * 	Metoda tworzy Event i dodaje go do kolekcji. Jako parametry przyjmuje dane niezbÄ™dne do stworzenia Eventu
	 * @param titleValue
	 * @param descriptionValue
	 * @param locationValue
	 * @param startDateValue
	 * @param endDateValue
	 * @param alarmDateTimeValue
	 * @throws EventManagerException - wyjątek zostaje rzucony, gdy podane zostaną błędne parametry
	 */
	public void addEvent(String titleValue, String descriptionValue, String locationValue, Date startDateValue, Date endDateValue, Date alarmDateTimeValue)
			throws EventManagerException {
		try {
			Event event = new Event(titleValue, descriptionValue, locationValue, startDateValue, endDateValue, alarmDateTimeValue);
			eventCollection.add(event);
			eventCollection.sort(null);
			createDirectory(directory);
			sendDataToXml(event, directory + "/" + eventsFilename);

		} catch (EventEmptyFieldException eventEmptyFieldException) {
			throw new controller.exception.EventManagerException("Invalid values in fields, please correct");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 	Metoda usuwa Event i przekazuje aktualne informacje do warstwy danych, gdzie następuje aktualizacja pliku XML z danymi
	 * @param eventId - numer indeksu Eventu do usunięcia
	 * @throws EventManagerException - wyjątek zostaje rzucony, gdy podany zostanie błędny parametr
	 * @throws SAXException - wyjątek zostaje rzucony, gdy nastąpi błąd parsowania pliku XML   
	 * @throws IOException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z otwarciem pliku do zapisu
	 */
	public void removeEvent(int eventId) throws EventManagerException, SAXException, IOException {
		for (Event event : eventCollection) {
			if (event.getIndex() == eventId) {
				eventCollection.remove(event);
				if (!eventCollection.isEmpty())
					sendDataToXml(eventCollection.get(0), directory + "/" + eventsFilename);
				else {
					Files.deleteIfExists(Paths.get(directory + "/" + eventsFilename));
					Files.deleteIfExists(Paths.get(directory));
				}

				return;
			}
		}
		throw new controller.exception.EventManagerException("Provided event ID doesn't exist");
	}

	private String fetchSelectedDate() {
		return mainWindow.getEventDate();
	}

	/**
	 * 	Metoda pobiera wartoÅ›Ä‡ wybranej daty z ekranu gÅ‚Ã³wnego i przekazuje jÄ… do stworzonego okna kreatora EventÃ³w
	 * @param eventWindow - instancja gÅ‚Ã³wnego okna aplikacji
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
	 * @return goOff - data określająca datę i godzinę uruchomienia alarmu
	 * @throws ParseException - wyjątek zostaje rzucony, gdy wystąpi błąd formatów dat
	 */
	public Date setAlarmGoOffDate(Date timerValue, Date startDateValue) throws ParseException {
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
		String resultString = DataIO.parseDateToString(calendarStop.getTime());

		return DataIO.parseStringToDate(resultString);
	}
	
	public void importEventsFromDatabase()
	{
		
	}

	/**
	 * 	Metoda importuje istniejÄ…ce Eventy z bazy XML przy uruchomieniu programu oraz tworzy na tej podstawie kolekcjÄ™ EventÃ³w
	 * @throws ParseException - wyjÄ…tek zostaje rzucony, gdy wystÄ…pi bÅ‚Ä…d formatÃ³w dat
	 * @throws EventEmptyFieldException - wyjÄ…tek zostaje rzucony, gdy nastÄ…pi bÅ‚Ä…d zwiÄ…zany z pobraniem Eventu z bazy
	 * @throws EventInvalidDateException - wyjÄ…tek zostaje rzucony, gdy nastÄ…pi bÅ‚Ä…d zwiÄ…zany z pobraniem Eventu z bazy
	 * @throws EventInvalidTimeException - wyjÄ…tek zostaje rzucony, gdy nastÄ…pi bÅ‚Ä…d zwiÄ…zany z pobraniem Eventu z bazy
	 * @throws TimerDateTimeException - wyjÄ…tek zostaje rzucony, gdy nastÄ…pi bÅ‚Ä…d zwiÄ…zany z pobraniem Eventu z bazy
	 */
	private void importEventsFromXml() throws ParseException, EventEmptyFieldException, EventInvalidDateException,
			EventInvalidTimeException, TimerDateTimeException {
		NodeList nList = dataIo.getNodeListFromXml();
		if (nList != null) {
			for (int item = 0; item < nList.getLength(); item++) {

				Node nNode = nList.item(item);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String title, description, location, startDateValue, endDateValue, timerDateTime = "";
					Integer index;
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
					index = Integer.parseInt(eElement.getAttribute("id"));
					fetchedEvent.setIndex(index);

					eventCollection.add(fetchedEvent);
				}
			}
		} else
			return;
	}

	public void filterEventsTable(JTable table, String field) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		TableRowSorter<DefaultTableModel> sorter = new TableRowSorter(model);
		table.setRowSorter(sorter);
		sorter.setRowFilter(RowFilter.regexFilter(field));
	}

}
