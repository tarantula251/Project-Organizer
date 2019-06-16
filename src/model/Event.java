package model;

import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import model.exception.EventEmptyFieldException;
import model.exception.EventInvalidDateException;
import model.exception.EventInvalidTimeException;
import model.exception.TimerDateTimeException;

public class Event implements Comparable<Event> {
	/**
	 * 	Pole counter autoinkrementujące wartość pola index
	 */
	private static int counter = 0; 
	/**
	 * 	Pole title przechowujące tytuł Eventu
	 */
	private String title;
	/**
	 * 	Pole description przechowujące opis Eventu
	 */
	private String description;
	/**
	 * 	Pole location przechowujące lokalizację Eventu
	 */
	private String location;
	/**
	 * 	Pole startDate przechowujące datę początku Eventu
	 */
	private String startDate;
	/**
	 * 	Pole endDate przechowujące datę końca Eventu
	 */
	private String endDate;
	/**
	 * 	Pole startTime przechowujące czas początku Eventu
	 */
	private String startTime;
	/**
	 * 	Pole endTime przechowujące czas końca Eventu
	 */
	private String endTime;
	/**
	 * 	Pole index przechowujące ID Eventu
	 */
	private Integer index;
	/**
	 * 	Pole alarmDateTime przechowujące datę oraz czas, w którym uruchomi się alarm dla Eventu
	 */
	private Date alarmDateTime;
	
	/**
	 * 	Konstruktor tworzy obiekt klasy Event, przechowujący wszystkie informacje o nim, wymagane parametry są niezbędne do stworzenia Eventu
	 * @param title - tytuł Eventu
	 * @param description - opis Eventu
	 * @param location - lokalizacja Eventu
	 * @param startDate - data początkowa Eventu
	 * @param endDate - data końcowa Eventu
	 * @param startTime - czas początkowy Eventu
	 * @param endTime - czas końcowy Eventu
	 * @param alarmDateTimeValue - czas uruchomienia alarmu
	 * @throws EventEmptyFieldException - wyjątek zostaje rzucony, gdy podany zostanie pusty parametr
	 * @throws EventInvalidDateException - wyjątek zostaje rzucony, gdy podany zostanie parametr z nieprawidłową datą
	 * @throws EventInvalidTimeException - wyjątek zostaje rzucony, gdy podany zostanie parametr z nieprawidłowym czasem
	 * @throws ParseException - wyjątek zostaje rzucony, gdy nastąpi błąd związany z formatem daty
	 * @throws TimerDateTimeException - wyjątek zostaje rzucony, gdy podany zostanie parametr z nieprawidłowym czasem
	 */
	public Event(String title, String description, String location, String startDate, String endDate, String startTime, String endTime, Date alarmDateTimeValue) throws EventEmptyFieldException, EventInvalidDateException, EventInvalidTimeException, ParseException, TimerDateTimeException {
		setTitle(title);
		setDescription(description);
		setLocation(location);
		setStartDate(startDate);
		setEndDate(endDate);	
		setStartTime(startTime);
		setEndTime(endTime);
		setAlarmDateTime(alarmDateTimeValue);		
		index = ++counter;	
	}

	public void setIndex(Integer index) {
		this.index = index;
	}
	
	public Date getAlarmDateTime() {
		return alarmDateTime;
	}

	public void setAlarmDateTime(Date alarmDateTime) throws ParseException, TimerDateTimeException {
		if (alarmDateTime != null) {			
			int comparedToStart = alarmDateTime.compareTo(parseStringToDate(startDate, startTime));
			if (comparedToStart != -1) {
				throw new TimerDateTimeException("Timer time cannot be after event's start date");				
			}			
		}
		this.alarmDateTime = alarmDateTime;
	}
	
	private Date parseStringToDate(String date, String time) throws ParseException {		
		DateFormat resultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date result = (Date) resultFormat.parse(date + " " + time);		
		return result;
	}

	public void validateTime(String time) throws EventInvalidTimeException {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");			
	        LocalTime.parse(time, formatter);
	    } catch (DateTimeParseException e) {
	        throw new EventInvalidTimeException("Invalid time value");
	    }						
	}
	
	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) throws EventInvalidTimeException {
		if (!startTime.isEmpty() && startTime != null) {
			validateTime(startTime);
			this.startTime = startTime;
		}
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) throws EventInvalidTimeException {
		if (!endTime.isEmpty() && endTime != null) {
			validateTime(endTime);
			this.endTime = endTime;
		}
	}

	public Integer getIndex() {
		return index;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) throws model.exception.EventEmptyFieldException {
		if (title == null || title.isEmpty())
			throw new model.exception.EventEmptyFieldException("Title cannot be empty");
		else
			this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) throws model.exception.EventEmptyFieldException {
		if (description == null || description.isEmpty())
			throw new model.exception.EventEmptyFieldException("Description cannot be empty");
		else
			this.description = description;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) throws model.exception.EventEmptyFieldException {
		if (location == null || location.isEmpty())
			throw new model.exception.EventEmptyFieldException("Location cannot be empty");
		else
			this.location = location;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) throws model.exception.EventEmptyFieldException {
		if (startDate == null || startDate.isEmpty())
			throw new model.exception.EventEmptyFieldException("Start date cannot be empty");
		else
			this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) throws model.exception.EventInvalidDateException, EventEmptyFieldException {
		if (endDate == null || endDate.isEmpty())
			throw new model.exception.EventInvalidDateException("End date cannot be empty");
		else if (endDate.compareTo(startDate) < 0)
			throw new model.exception.EventEmptyFieldException("End date cannot be after end date");
		else
			this.endDate = endDate;
	}

	@Override
	public int compareTo(Event o) {
		return new String(startDate + " " + startTime).compareTo(o.startDate + " " + o.startTime);
	}	
}
