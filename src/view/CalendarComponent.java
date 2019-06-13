package view;

import javax.swing.JPanel;
import com.toedter.calendar.JCalendar;

public class CalendarComponent extends JPanel {
	private static final long serialVersionUID = 1L;
	private JPanel calendarPanel = new JPanel();
	private JCalendar calendar = new JCalendar();

	public CalendarComponent() {
		super();
		calendarPanel.add(calendar);
	}

	public JPanel getCalendarPanel() {
		return calendarPanel;
	}

	public JCalendar getCalendar() {
		return calendar;
	}

}
