package controller;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JTextField;

import org.xml.sax.SAXException;

import com.toedter.calendar.JDateChooser;

import controller.exception.EventManagerException;
import model.AlarmSoundTimerTask;
import model.DataIO;
import model.Event;
import model.exception.EventEmptyFieldException;
import model.exception.EventInvalidDateException;
import model.exception.TimerDateTimeException;
import view.EventWindow;
import view.MainWindow;

public class EventManager {

	ArrayList<Event> eventCollection = new ArrayList<Event>();
	MainWindow mainWindow;
	DataIO dataIo = new DataIO();
	Clip clip;
	private volatile Boolean alarmDismissed = false;

	public EventManager(MainWindow mainWindow)
			throws LineUnavailableException, IOException, UnsupportedAudioFileException {
		this.mainWindow = mainWindow;
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
								}
							}
						}
						
						Thread.sleep(1000);
						
					} catch (InterruptedException | ParseException | TimerDateTimeException e) {
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

	private void sendDataToXml(Event event, String filename) throws SAXException, IOException {
		if (event.getIndex() == 1)
			dataIo.writeToXml(event, filename);
		else {
			ArrayList<String> content = dataIo.readXml(filename);
			dataIo.appendXml(event, filename);
		}
	}

	private void getDataFromXml(String filename) {
		dataIo.parseXml(filename);
	}

	public void addEvent(String titleValue, String descriptionValue, String locationValue, String startDateValue,
			String endDateValue, String startTimeValue, String endTimeValue, Date alarmDateTimeValue)
			throws EventManagerException, EventInvalidDateException, EventEmptyFieldException {
		try {
			Event event = new Event(titleValue, descriptionValue, locationValue, startDateValue, endDateValue,
					startTimeValue, endTimeValue, alarmDateTimeValue);
			eventCollection.add(event);
			sendDataToXml(event, "data.xml");
		} catch (EventEmptyFieldException eventEmptyFieldException) {
			throw new controller.exception.EventManagerException("Invalid values in fields, please correct");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
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

	public Date setAlarmGoOffDate(Date timerValue, Date startTimeValue, String startDateValue) {
		DateFormat timerFormat = new SimpleDateFormat("HH:mm:ss");
		DateFormat startFormat = new SimpleDateFormat("HH:mm:ss");
		String timerTime = timerFormat.format(timerValue);
		String eventStart = startFormat.format(startTimeValue);

		int timerHour, timerMinute, startHour, startMinute, alarmClockHour, alarmClockMinute, year, month, day;
		timerHour = Integer.parseInt(timerTime.substring(0, 2));
		timerMinute = Integer.parseInt(timerTime.substring(3, 5));
		startHour = Integer.parseInt(eventStart.substring(0, 2));
		startMinute = Integer.parseInt(eventStart.substring(3, 5));
		year = Integer.parseInt(startDateValue.substring(0, 4));
		month = Integer.parseInt(startDateValue.substring(5, 7)) - 1;
		day = Integer.parseInt(startDateValue.substring(8, 10));

		GregorianCalendar calendarStart = new GregorianCalendar(year, month, day, startHour, startMinute, 0);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date start = calendarStart.getTime();

		GregorianCalendar calendarStop = new GregorianCalendar(year, month, day, startHour - timerHour,
				startMinute - timerMinute, 0);
		Date goOff = calendarStop.getTime();

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

}
