package view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.NotDirectoryException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import controller.EventManager;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.BoxLayout;
import java.awt.FlowLayout;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;

import model.Event;
import model.exception.EventEmptyFieldException;
import model.exception.EventInvalidDateException;
import model.exception.EventInvalidTimeException;
import model.exception.TimerDateTimeException;

import javax.swing.table.DefaultTableModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import java.awt.GridLayout;
import com.toedter.calendar.JCalendar;
import java.awt.Component;
import javax.swing.Box;

public class MainWindow implements MenuListener, ActionListener, KeyListener {

	private static final long serialVersionUID = 1L;
	private JFrame window = new JFrame("Organizer App");
	private Container contentPane = window.getContentPane();

	private JMenu menu, databaseSubmenu, eventsSubmenu;
	private JMenuBar menuBar;
	private JMenuItem eSave, eOpen, dImport, dExport, gPreferences, gAbout, gExit;
	private JPanel calendarPanel;
	private JTextField filename = new JTextField(), dir = new JTextField();
	private EventManager eventManager;
	private String eventDate;
	private JButton createEventBtn;
	private JLabel label;
	private JTextField textField;
	private JLabel labelEvents;
	private JScrollPane scrollPane;
	private JTable table;
	private JCalendar calendarWidget;
	private JPanel panel;

	public MainWindow() throws LineUnavailableException, IOException, UnsupportedAudioFileException, ParseException, EventEmptyFieldException, EventInvalidDateException, EventInvalidTimeException, TimerDateTimeException {		
		eventManager = new EventManager(this);
		initialize();
	}

	private void initialize() throws ParseException {
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addCalendar();
		addMenuBar();
		window.setBounds(0, 0, 577, 326);
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		refreshEventsTable();
	}
	
	public void refreshEventsTable()
	{
		var model = (DefaultTableModel)table.getModel();
		model.setRowCount(0);
		for(Event event : eventManager.getEventCollection()) model.addRow(new Object[] {event.getTitle(), event.getStartDate().toString(), event.getStartTime().toString()});
	}
	
	public JFrame getWindow() {
		return window;
	}

	public EventManager getEventManager() {
		return eventManager;
	}

	public String getEventDate() {
		return eventDate;
	}

	private void setEventDate(String eventDate) {
		this.eventDate = eventDate;
	}

	private void addCalendar() {
		window.getContentPane().setLayout(new BoxLayout(window.getContentPane(), BoxLayout.X_AXIS));

		calendarWidget = new JCalendar();
		window.getContentPane().add(calendarWidget);
		showSelectedDate(calendarWidget);

		label = new JLabel("Selected date: ");
		calendarWidget.getMonthChooser().add(label, BorderLayout.NORTH);
		label.setBounds(692, 291, 80, 15);
		label.setFont(new Font("Tahoma", Font.PLAIN, 12));

		textField = new JTextField();
		calendarWidget.getYearChooser().add(textField, BorderLayout.NORTH);
		textField.setBounds(772, 288, 46, 21);
		textField.setText("2019-06-15");
		textField.setFont(new Font("Tahoma", Font.PLAIN, 12));

		createEventBtn = new JButton("Create event");
		calendarWidget.getDayChooser().add(createEventBtn, BorderLayout.SOUTH);
		createEventBtn.setBounds(208, 287, 99, 23);
		createEventBtn.setFont(new Font("Tahoma", Font.PLAIN, 12));
		createEventBtn.addActionListener(this);

		panel = new JPanel();
		window.getContentPane().add(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 363 };
		gbl_panel.rowHeights = new int[] { 21, 233 };
		gbl_panel.columnWeights = new double[] { 1.0 };
		gbl_panel.rowWeights = new double[] { 0.0, 1.0 };
		panel.setLayout(gbl_panel);

		labelEvents = new JLabel("View your events: ");
		labelEvents.setHorizontalAlignment(SwingConstants.LEFT);
		labelEvents.setLabelFor(panel);
		labelEvents.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_labelEvents = new GridBagConstraints();
		gbc_labelEvents.fill = GridBagConstraints.BOTH;
		gbc_labelEvents.insets = new Insets(0, 0, 5, 0);
		gbc_labelEvents.gridx = 0;
		gbc_labelEvents.gridy = 0;
		panel.add(labelEvents, gbc_labelEvents);

		scrollPane = new JScrollPane();

		table = new JTable() {

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
			
		};
		table.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "Title", "Start date", "Start time" }));
		scrollPane.setViewportView(table);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		panel.add(scrollPane, gbc_scrollPane);
	}

	private void showSelectedDate(JCalendar calendar) {
		// initial value in date label
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date today = new Date();
		setEventDate(dateFormat.format(today));

		// updating value in date label
		calendar.getDayChooser().setMinSelectableDate(today);
		calendar.getDayChooser().addPropertyChangeListener("day", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				Date currentDate = calendar.getDate();
				setEventDate(dateFormat.format(currentDate));
				textField.setText(getEventDate());
			}
		});
	}

	private void addMenuBar() {
		this.addKeyListener(this);

		menu = new JMenu("Menu");
		menu.setMnemonic(KeyEvent.VK_M); // press alt + m
		menu.addMenuListener(this);

		menuBar = new JMenuBar();
		menuBar.add(menu);

		databaseSubmenu = new JMenu("Database Action");
		databaseSubmenu.setMnemonic(KeyEvent.VK_D);
		databaseSubmenu.addMenuListener(this);
		menu.add(databaseSubmenu);

		eventsSubmenu = new JMenu("Event Action");
		eventsSubmenu.setMnemonic(KeyEvent.VK_E);
		eventsSubmenu.addMenuListener(this);
		menu.add(eventsSubmenu);

		eSave = new JMenuItem("Save event");
		eSave.setMnemonic(KeyEvent.VK_S);
		eSave.addActionListener(this);
		eventsSubmenu.add(eSave);

		eOpen = new JMenuItem("Open event");
		eOpen.setMnemonic(KeyEvent.VK_O);
		eOpen.addActionListener(this);
		eventsSubmenu.add(eOpen);

		dImport = new JMenuItem("Import data");
		dImport.setMnemonic(KeyEvent.VK_I);
		dImport.addActionListener(this);
		databaseSubmenu.add(dImport);

		dExport = new JMenuItem("Export data");
		dExport.setMnemonic(KeyEvent.VK_E);
		dExport.addActionListener(this);
		databaseSubmenu.add(dExport);

		gPreferences = new JMenuItem("Preferences");
		gPreferences.setMnemonic(KeyEvent.VK_P);
		gPreferences.addActionListener(this);
		menu.add(gPreferences);

		gAbout = new JMenuItem("About program");
		gAbout.setMnemonic(KeyEvent.VK_A);
		gAbout.addActionListener(this);
		menu.add(gAbout);

		gExit = new JMenuItem("Exit");
		gExit.setMnemonic(KeyEvent.VK_X);
		gExit.addActionListener(this);
		menu.add(gExit);

		window.setJMenuBar(menuBar);
	}

	private void addKeyListener(MainWindow mainWindow) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyChar() == 'x') {
			System.exit(0);
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void menuSelected(MenuEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void menuDeselected(MenuEvent e) {
		if (e.getSource().equals(gExit)) {
			System.exit(0);
		}
	}

	@Override
	public void menuCanceled(MenuEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(dImport) || e.getSource().equals(eOpen)) {
			JFileChooser chooser = new JFileChooser();
			int status = chooser.showOpenDialog(null);
			if (status == JFileChooser.APPROVE_OPTION) {
				try {
					filename.setText(chooser.getSelectedFile().getName());
					dir.setText(chooser.getCurrentDirectory().toString());
					if (chooser.getSelectedFile() == null)
						throw new FileNotFoundException("No such file in file system");
					if (chooser.getCurrentDirectory() == null)
						throw new NotDirectoryException("No such directory in file system");
				} catch (FileNotFoundException e1) {
					return;
				} catch (NotDirectoryException e1) {
					return;
				}
			}

			if (status == JFileChooser.CANCEL_OPTION) {
				filename.setText("You pressed cancel");
				dir.setText("");
			}
		}

		if (e.getSource().equals(dExport) || e.getSource().equals(eSave)) {
			JFileChooser chooser = new JFileChooser();
			int status = chooser.showSaveDialog(null);
			if (status == JFileChooser.APPROVE_OPTION) {
				try {
					filename.setText(chooser.getSelectedFile().getName());
					dir.setText(chooser.getCurrentDirectory().toString());
					if (chooser.getSelectedFile() == null)
						throw new FileNotFoundException("No such file in file system");
					if (chooser.getCurrentDirectory() == null)
						throw new NotDirectoryException("No such directory in file system");
				} catch (FileNotFoundException e1) {
					return;
				} catch (NotDirectoryException e1) {
					return;
				}
			}

			if (status == JFileChooser.CANCEL_OPTION) {
				filename.setText("You pressed cancel");
				dir.setText("");
			}
		}

		if (e.getSource().equals(gPreferences)) {
			// TODO go to preferences
		}

		if (e.getSource().equals(gAbout)) {
			JOptionPane.showMessageDialog(window, new AboutProgramWindow().getAboutProgramMessage(), "About Program",
					JOptionPane.PLAIN_MESSAGE);
		}

		if (e.getSource().equals(gExit)) {
			System.exit(0);
		}

		if (e.getSource().equals(createEventBtn)) {
			EventWindow eventWindow = new EventWindow(this);
			eventManager.fillStartDateField(eventWindow);
			eventWindow.setVisible(true);
			if(eventWindow.getDialogResult() == 1) refreshEventsTable();
		}

	}

	public void showPane(String title, String infoMessage) {
		JOptionPane.showMessageDialog(null, infoMessage, title, JOptionPane.INFORMATION_MESSAGE);

	}
}
