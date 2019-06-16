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
import java.util.Timer;
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
import model.AlarmSoundTimerTask;
import model.DataIO;
import model.Event;
import model.exception.EventEmptyFieldException;
import model.exception.EventInvalidDateException;
import model.exception.EventInvalidTimeException;
import model.exception.TimerDateTimeException;
import view.EventWindow;
import view.MainWindow;

public class EventManager {

	ArrayList<Event> eventCollection = new ArrayList<Event>();
	MainWindow mainWindow;
	DataIO dataIo = new DataIO();
	String directory = "databank";
	String eventsFilename = "data.xml";
	Clip clip;
	private volatile Boolean alarmDismissed = false;

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

	public void addEvent(String titleValue, String descriptionValue, String locationValue, String startDateValue,
			String endDateValue, String startTimeValue, String endTimeValue, Date alarmDateTimeValue)
			throws EventManagerException, EventInvalidDateException, EventEmptyFieldException {
		try {
			Event event = new Event(titleValue, descriptionValue, locationValue, startDateValue, endDateValue,
					startTimeValue, endTimeValue, alarmDateTimeValue);
			eventCollection.add(event);

			createDirectory(directory);

			sendDataToXml(event, directory + "/" + eventsFilename);

		} catch (EventEmptyFieldException eventEmptyFieldException) {
			throw new controller.exception.EventManagerException("Invalid values in fields, please correct");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

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

	public void goOffAlarm(Date goOffDate) {
		Timer timer = new Timer();
		Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					clip.start();
					Thread.sleep(100);
					while (clip.isActive()) {
						if (alarmDismissed) {
							clip.stop();
							alarmDismissed = false;
							break;
						}
					}
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		});
		timer.schedule(new AlarmSoundTimerTask(thread), goOffDate);
	}

	public void dismissAlarm() throws InterruptedException {
		alarmDismissed = true;
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
