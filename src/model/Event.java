package model;

import java.util.Date;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import model.exception.EventEmptyFieldException;
import model.exception.EventInvalidDateException;
import model.exception.EventInvalidTimeException;
import model.exception.TimerDateTimeException;

public class Event implements Comparable<Event> {
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
	 * 	Pole startDate przechowujące datę i godzinę początku Eventu
	 */
	private Date startDate;
	/**
	 * 	Pole endDate przechowujące datę i godzinę końca Eventu
	 */
	private Date endDate;
	/**
	 * 	Pole index przechowujące ID Eventu
	 */
	private Integer index;
	/**
	 * 	Pole alarmDateTime przechowujące datę i godzinę, w którym uruchomi się alarm dla Eventu
	 */
	private Date alarmDateTime;

	/**
	 * 	Konstruktor tworzy obiekt klasy Event, przechowujący wszystkie informacje o nim, wymagane parametry są niezbędne do stworzenia Eventu
	 * @param title - tytuł Eventu
	 * @param description - opis Eventu
	 * @param location - lokalizacja Eventu
	 * @param startDate - data początkowa Eventu
	 * @param endDate - data końcowa Eventu
	 * @param alarmDateTimeValue - czas uruchomienia alarmu
	 * @throws EventEmptyFieldException - wyjątek zostaje rzucony, gdy podany zostanie pusty parametr
	 * @throws EventInvalidDateException - wyjątek zostaje rzucony, gdy podany zostanie parametr z nieprawidłową datą
	 * @throws EventInvalidTimeException - wyjątek zostaje rzucony, gdy podany zostanie parametr z nieprawidłowym czasem
	 * @throws TimerDateTimeException - wyjątek zostaje rzucony, gdy podany zostanie parametr z nieprawidłowym czasem
	 */
	public Event(String title, String description, String location, Date startDate, Date endDate, Date alarmDateTimeValue) throws EventEmptyFieldException, EventInvalidDateException, EventInvalidTimeException, TimerDateTimeException {
		setTitle(title);
		setDescription(description);
		setLocation(location);
		setStartDate(startDate);
		setEndDate(endDate);
		setAlarmDateTime(alarmDateTimeValue);		
		index = 0;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}
	
	public Date getAlarmDateTime() {
		return alarmDateTime;
	}

	public void setAlarmDateTime(Date alarmDateTime) throws EventInvalidTimeException, TimerDateTimeException {
		if (alarmDateTime != null) {
			validateTime(alarmDateTime);
			int comparedToStart = alarmDateTime.compareTo(startDate);
			if (comparedToStart != -1) {
				throw new TimerDateTimeException("Timer time cannot be after event's start date");				
			}			
		}
		this.alarmDateTime = alarmDateTime;
	}

	/**
	 * 	Metoda sprawdza, czy format podanego parametru jest poprawny
	 * @param time - zmienna typu Date, której format jest sprawdzany
	 * @throws EventInvalidTimeException - wyjątek zostaje rzucony, gdy format podanego parametru jest niezgodny ze wzorcem
	 */
	private void validateTime(Date time) throws EventInvalidTimeException {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
			String timeToValidate = DataIO.parseDateToStringTimeOnly(time);
	        LocalTime.parse(timeToValidate, formatter);
	    } catch (DateTimeParseException e) {
	        throw new EventInvalidTimeException("Invalid time value");
	    }						
	}

	public Integer getIndex() {
		return index;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) throws EventEmptyFieldException {
		if (title == null || title.isEmpty())
			throw new model.exception.EventEmptyFieldException("Title cannot be empty");
		else
			this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) throws EventEmptyFieldException {
		if (description == null || description.isEmpty())
			throw new model.exception.EventEmptyFieldException("Description cannot be empty");
		else
			this.description = description;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) throws EventEmptyFieldException {
		if (location == null || location.isEmpty())
			throw new model.exception.EventEmptyFieldException("Location cannot be empty");
		else
			this.location = location;
	}

	public Date getStartDate() { return startDate; }

	public void setStartDate(Date startDate) throws EventEmptyFieldException, EventInvalidTimeException {
		if (startDate == null || startDate.toString().isEmpty())
			throw new model.exception.EventEmptyFieldException("Start date cannot be empty");
		else {
			validateTime(startDate);
			this.startDate = startDate;
		}
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) throws EventInvalidDateException, EventEmptyFieldException, EventInvalidTimeException {
		if (endDate == null || endDate.toString().isEmpty())
			throw new model.exception.EventInvalidDateException("End date cannot be empty");
		else if (endDate.compareTo(startDate) < 0)
			throw new model.exception.EventEmptyFieldException("End date cannot be after end date");
		else {
			validateTime(endDate);
			this.endDate = endDate;
		}
	}

	@Override
	public int compareTo(Event o) {
		return (startDate).compareTo(o.startDate);
	}	
}
