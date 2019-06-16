package controller;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
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

	/**
	 * 	Pole eventCollection przechowujące kolekcję wszystkich Eventów w liście
	 */
	ArrayList<Event> eventCollection = new ArrayList<Event>();
	/**
	 * 	Pole mainWindow łączące wartstwę logiki z warstwą widoku
	 */
	MainWindow mainWindow;
	/**
	 * 	Pole dataIo łączące wartstwę logiki z warstwą danych
	 */
	DataIO dataIo = new DataIO();
	/**
	 * 	Pole directory określające folder zawierający bazę danych
	 */
	String directory = "databank";
	/**
	 * 	Pole eventsFilename określające nazwę pliku XML z danymi
	 */
	String eventsFilename = "data.xml";
	/**
	 * 	Pole clip niezbędne do uruchomienia pliku dźwiękowego dla alarmu
	 */
	Clip clip;

	/**
	 * 	Konstruktor tworzy obiekt klasy EventManager, który odpowiedzialny jest za zarządzanie Eventami oraz  przekazywanie danych o nich do innych warstw aplikacji
	 * @param mainWindow - obiekt reprezentujący główne okno aplikacji
	 * @throws LineUnavailableException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z uruchomieniem pliku dźwiękowego
	 * @throws IOException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z otwarciem pliku dźwiękowego
	 * @throws UnsupportedAudioFileException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z otwarciem pliku dźwiękowego
	 * @throws ParseException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z zaimportowaniem Eventów z bazy XML
	 * @throws EventEmptyFieldException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z utworzeniem Eventów z zaimportowanego pliku XML
	 * @throws EventInvalidDateException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z utworzeniem Eventów z zaimportowanego pliku XML
	 * @throws EventInvalidTimeException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z utworzeniem Eventów z zaimportowanego pliku XML
	 * @throws TimerDateTimeException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z utworzeniem Eventów z zaimportowanego pliku XML
	 */
	public EventManager(MainWindow mainWindow)
			throws LineUnavailableException, IOException, UnsupportedAudioFileException, ParseException,
			EventEmptyFieldException, EventInvalidDateException, EventInvalidTimeException, TimerDateTimeException {
		this.mainWindow = mainWindow;

		importEventsFromXml();

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
													+ event.getStartTime() + "\nEnd: " + event.getEndDate() + " "
													+ event.getEndTime() + "\n");
									clip.stop();
									event.setAlarmDateTime(null);
									createDirectory(directory);
									sendDataToXml(event, directory + "/" + eventsFilename);
								}
							}
						}

						Thread.sleep(1000);

					} catch (InterruptedException | ParseException | TimerDateTimeException | SAXException
							| IOException e) {
						e.printStackTrace();
					} catch (EventEmptyFieldException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (EventInvalidDateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (EventInvalidTimeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});

		alarmCheckThread.start();
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
	 * @throws ParseException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z zaimportowaniem Eventów z bazy XML
	 * @throws EventEmptyFieldException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z utworzeniem Eventów z zaimportowanego pliku XML
	 * @throws EventInvalidDateException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z utworzeniem Eventów z zaimportowanego pliku XML
	 * @throws EventInvalidTimeException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z utworzeniem Eventów z zaimportowanego pliku XML
	 * @throws TimerDateTimeException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z utworzeniem Eventów z zaimportowanego pliku XML
	 */
	private void sendDataToXml(Event event, String filename) throws SAXException, IOException, ParseException,
			EventEmptyFieldException, EventInvalidDateException, EventInvalidTimeException, TimerDateTimeException {
		dataIo.writeToXml(event, filename);
		if (eventCollection.size() > 1) {
			for (Event existingEvent : eventCollection) {
				if (existingEvent != event)
					dataIo.appendXml(existingEvent);
			}
		}
	}

	void createDirectory(String directory) throws IOException {
		var path = Paths.get(directory);
		if (!Files.exists(path))
			Files.createDirectory(path);
	}

	/**
	 * 	Metoda tworzy Event i dodaje go do kolekcji. Jako parametry przyjmuje dane niezbędne do stworzenia Eventu
	 * @param titleValue
	 * @param descriptionValue
	 * @param locationValue
	 * @param startDateValue
	 * @param endDateValue
	 * @param startTimeValue
	 * @param endTimeValue
	 * @param alarmDateTimeValue
	 * @throws EventManagerException - wyjątek zostaje rzucony, gdy podane zostaną błędne parametry
	 * @throws EventInvalidDateException - wyjątek zostaje rzucony, gdy podane zostaną błędne parametry
	 * @throws EventEmptyFieldException - wyjątek zostaje rzucony, gdy podane zostaną błędne parametry
	 */
	public void addEvent(String titleValue, String descriptionValue, String locationValue, String startDateValue,
			String endDateValue, String startTimeValue, String endTimeValue, Date alarmDateTimeValue)
			throws EventManagerException, EventInvalidDateException, EventEmptyFieldException {
		try {
			Event event = new Event(titleValue, descriptionValue, locationValue, startDateValue, endDateValue,
					startTimeValue, endTimeValue, alarmDateTimeValue);
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
	 * @throws ParseException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z zaimportowaniem Eventów z bazy XML
	 * @throws EventEmptyFieldException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z pobraniem usuwanego Eventu z kolekcji
	 * @throws EventInvalidDateException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z pobraniem usuwanego Eventu z kolekcji
	 * @throws EventInvalidTimeException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z pobraniem usuwanego Eventu z kolekcji
	 * @throws TimerDateTimeException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z pobraniem usuwanego Eventu z kolekcji
	 */
	public void removeEvent(int eventId) throws EventManagerException, SAXException, IOException, ParseException,
			EventEmptyFieldException, EventInvalidDateException, EventInvalidTimeException, TimerDateTimeException {
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

	public String fetchSelectedDate() {
		return mainWindow.getEventDate();
	}

	/**
	 * 	Metoda pobiera wartość wybranej daty z ekranu głównego i przekazuje ją do stworzonego okna kreatora Eventów
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
	 * @param startTimeValue - wartość określająca czas początkowy Eventu
	 * @param startDateValue - data określająca datę początkową Eventu
	 * @return goOff - data określająca datę i godzinę uruchomienia alarmu
	 * @throws ParseException - wyjątek zostaje rzucony, gdy wystąpi błąd formatów dat
	 */
	public Date setAlarmGoOffDate(Date timerValue, Date startTimeValue, String startDateValue) throws ParseException {
		DateFormat timerFormat = new SimpleDateFormat("HH:mm:ss");
		DateFormat startFormat = new SimpleDateFormat("HH:mm:ss");
		String timerTime = timerFormat.format(timerValue);
		String eventStart = startFormat.format(startTimeValue);

		int timerHour, timerMinute, startHour, startMinute, year, month, day;
		timerHour = Integer.parseInt(timerTime.substring(0, 2));
		timerMinute = Integer.parseInt(timerTime.substring(3, 5));
		startHour = Integer.parseInt(eventStart.substring(0, 2));
		startMinute = Integer.parseInt(eventStart.substring(3, 5));
		year = Integer.parseInt(startDateValue.substring(0, 4));
		month = Integer.parseInt(startDateValue.substring(5, 7)) - 1;
		day = Integer.parseInt(startDateValue.substring(8, 10));

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		GregorianCalendar calendarStop = new GregorianCalendar(year, month, day, startHour - timerHour,
				startMinute - timerMinute, 0);
		String resultString = sdf.format(calendarStop.getTime());
		Date goOff = sdf.parse(resultString);

		return goOff;
	}

	public void addRowToTable(JTable table) throws ParseException {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		Date currentDate = new Date();

		for (Event event : getEventCollection()) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date eventDate = simpleDateFormat.parse(event.getStartDate());

			int comparedTime = eventDate.compareTo(currentDate);
			if (comparedTime > 0) {
				model.addRow(new Object[] { event.getTitle(), event.getStartDate(), event.getStartTime() });
			}
		}
	}

	/**
	 * 	Metoda importuje istniejące Eventy z bazy XML przy uruchomieniu programu oraz tworzy na tej podstawie kolekcję Eventów
	 * @throws ParseException - wyjątek zostaje rzucony, gdy wystąpi błąd formatów dat
	 * @throws EventEmptyFieldException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z pobraniem Eventu z bazy
	 * @throws EventInvalidDateException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z pobraniem Eventu z bazy
	 * @throws EventInvalidTimeException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z pobraniem Eventu z bazy
	 * @throws TimerDateTimeException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z pobraniem Eventu z bazy
	 */
	public void importEventsFromXml() throws ParseException, EventEmptyFieldException, EventInvalidDateException,
			EventInvalidTimeException, TimerDateTimeException {
		NodeList nList = dataIo.getNodeListFromXml();
		if (nList != null) {
			for (int item = 0; item < nList.getLength(); item++) {

				Node nNode = nList.item(item);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String title, description, location, startDate, startTime, endDate, endTime, timerDateTime = "";
					Integer index = null;
					title = eElement.getElementsByTagName("title").item(0).getTextContent();
					description = eElement.getElementsByTagName("description").item(0).getTextContent();
					location = eElement.getElementsByTagName("location").item(0).getTextContent();
					startDate = eElement.getElementsByTagName("startDate").item(0).getTextContent();
					startTime = eElement.getElementsByTagName("startTime").item(0).getTextContent();
					endDate = eElement.getElementsByTagName("endDate").item(0).getTextContent();
					endTime = eElement.getElementsByTagName("endTime").item(0).getTextContent();
					NodeList timerNl = eElement.getElementsByTagName("timerDateTime");
					if (timerNl.getLength() > 0)
						timerDateTime = eElement.getElementsByTagName("timerDateTime").item(0).getTextContent();
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date timerDate = null;
					if (!timerDateTime.isEmpty()) {
						timerDate = simpleDateFormat.parse(timerDateTime);
					}
					Event fetchedEvent = new Event(title, description, location, startDate, endDate, startTime, endTime,
							timerDate);
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
		TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<DefaultTableModel>(model);
		table.setRowSorter(sorter);
		sorter.setRowFilter(RowFilter.regexFilter(field));
	}

}
