package model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import model.exception.EventEmptyFieldException;
import model.exception.EventInvalidDateException;
import model.exception.EventInvalidTimeException;

public class Event {
	private static int counter = 0; 
	private String title;
	private String description;
	private String location;
	private String startDate;
	private String endDate;
	private String startTime;
	private String endTime;
	private Integer index;

	public Event(String title, String description, String location, String startDate, String endDate, String startTime, String endTime) throws EventEmptyFieldException, EventInvalidDateException, EventInvalidTimeException {
		setTitle(title);
		setDescription(description);
		setLocation(location);
		setStartDate(startDate);
		setEndDate(endDate);	
		setStartTime(startTime);
		setEndTime(endTime);
		index = ++counter;		
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

	public Event() {}

	@Override
	public String toString() {
		return "Event [title=" + title + ", description=" + description + ", location=" + location + ", startDate="
				+ startDate + ", endDate=" + endDate + ", index=" + index + "]";
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
}
