package view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import model.DataIO;
import org.xml.sax.SAXException;

import controller.EventManager;
import controller.exception.EventManagerException;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.BoxLayout;
import java.awt.FlowLayout;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.JTable;
import model.Event;
import model.exception.EventEmptyFieldException;
import model.exception.EventInvalidDateException;
import model.exception.EventInvalidTimeException;
import model.exception.TimerDateTimeException;
import net.fortuna.ical4j.model.ValidationException;

import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JCalendar;
import javax.swing.JSplitPane;
import java.awt.event.KeyAdapter;
import javax.swing.ListSelectionModel;

public class MainWindow implements MenuListener, ActionListener, KeyListener {

	private JFrame window = new JFrame("Organizer App");
	private JMenu menu;
	private JMenuBar menuBar;
	private JMenuItem gAbout, gExit;
	private EventManager eventManager;
	private String eventDate;
	private JButton createEventBtn;
	private JLabel label;
	private JTextField textField;
	private JLabel labelFilter;
	private JScrollPane scrollPane;
	private JTable table;
	private JCalendar calendarWidget;
	private JPanel panel;
	private final JButton deleteEventBtn = new JButton("Delete event");
	private JPanel panelButtons;
	private JLabel labelEvents;
	private JSplitPane splitPaneFilter;
	private JTextField txtFieldFilter;
	private JMenuItem gSettings;
	private JMenu gExport;
	private JMenuItem gICalendar;
	private JMenuItem gXml;
	private JMenuItem gImport;

	/**
	 * Konstruktor tworzy obiekt klasy MainWindow, który jest głównym oknem aplikacji
	 * @throws Exception - wyjątek zostaje rzucony, gdy którykolwiek z użytych komponentów rzuci wyjątek
	 */
	public MainWindow() throws Exception {
		try {
			eventManager = new EventManager(this);
			if(!eventManager.isDatabaseConnected()) JOptionPane.showMessageDialog(window,
					"Connection to database could not be established. Creating new events is disabled.\nFor more details, please test your connection in Settings.",
					"Database", JOptionPane.ERROR_MESSAGE);
		} catch (LineUnavailableException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "LineUnavailableException", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			throw new Exception();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "IOException", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			throw new Exception();
		} catch (UnsupportedAudioFileException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "UnsupportedAudioFileException",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			throw new Exception();
		} catch (EventManagerException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "EventManagerException", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			throw new Exception();
		} catch (Exception e) {
			throw new Exception();
		}
	}

	/**
	 * Metoda inicjująca okno i określająca jego ogólny wygląd
	 */
	public void initialize() {
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addCalendar();
		addMenuBar();
		window.setBounds(0, 0, 577, 326);
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		refreshEventsTable();
		enableEventCreation(eventManager.isDatabaseConnected());
	}

	/**
	 * Metoda odświeża wygląd tabeli z Eventami, gdy kolekcja Eventów jest aktualizowana
	 */
	private void refreshEventsTable() {
		var model = (DefaultTableModel) table.getModel();
		model.setRowCount(0);
		for (Event event : eventManager.getEventCollection())
			model.addRow(new Object[] { event.getIndex(), event.getTitle(),
					DataIO.parseDateToString(event.getStartDate()) });
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

	/**
	 * Metoda dodaje do okna głównego komponent JCalendar
	 */
	@SuppressWarnings("serial")
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
		calendarWidget.getYearChooser().setYear(Calendar.getInstance().get(Calendar.YEAR));
		textField.setBounds(772, 288, 46, 21);
		textField.setText(DataIO.parseDateToStringDateOnly(new Date()));
		textField.setFont(new Font("Tahoma", Font.PLAIN, 12));

		panelButtons = new JPanel();
		calendarWidget.getDayChooser().add(panelButtons, BorderLayout.SOUTH);
		panelButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

		createEventBtn = new JButton("Create event");
		panelButtons.add(createEventBtn);
		createEventBtn.setFont(new Font("Tahoma", Font.PLAIN, 12));
		deleteEventBtn.addActionListener(this);
		deleteEventBtn.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panelButtons.add(deleteEventBtn);
		createEventBtn.addActionListener(this);

		panel = new JPanel();
		window.getContentPane().add(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 363 };
		gbl_panel.rowHeights = new int[] { 21, 0, 233 };
		gbl_panel.columnWeights = new double[] { 1.0 };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 1.0 };
		panel.setLayout(gbl_panel);

		splitPaneFilter = new JSplitPane();
		GridBagConstraints gbc_splitPaneFilter = new GridBagConstraints();
		gbc_splitPaneFilter.fill = GridBagConstraints.BOTH;
		gbc_splitPaneFilter.insets = new Insets(0, 0, 5, 0);
		gbc_splitPaneFilter.gridx = 0;
		gbc_splitPaneFilter.gridy = 0;
		panel.add(splitPaneFilter, gbc_splitPaneFilter);

		labelFilter = new JLabel("Filter events by:");
		splitPaneFilter.setLeftComponent(labelFilter);
		labelFilter.setHorizontalAlignment(SwingConstants.LEFT);
		labelFilter.setLabelFor(panel);
		labelFilter.setFont(new Font("Tahoma", Font.PLAIN, 12));

		txtFieldFilter = new JTextField();
		txtFieldFilter.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String filterByField = txtFieldFilter.getText();
				eventManager.filterEventsTable(table, filterByField);
			}
		});
		splitPaneFilter.setRightComponent(txtFieldFilter);
		txtFieldFilter.setColumns(10);

		labelEvents = new JLabel("View your events: ");
		labelEvents.setHorizontalAlignment(SwingConstants.LEFT);
		labelEvents.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_labelEvents = new GridBagConstraints();
		gbc_labelEvents.anchor = GridBagConstraints.WEST;
		gbc_labelEvents.insets = new Insets(0, 0, 5, 0);
		gbc_labelEvents.gridx = 0;
		gbc_labelEvents.gridy = 1;
		panel.add(labelEvents, gbc_labelEvents);

		scrollPane = new JScrollPane();

		table = new JTable() {

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

		};
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting())
					gExport.setEnabled(table.getSelectedRowCount() > 0);
			}
		});
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE)
					if(eventManager.isDatabaseConnected()) removeSelectedEvent();
			}
		});
		table.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "Id", "Title", "Start date" }) {
			@SuppressWarnings("rawtypes")
			Class[] columnTypes = new Class[] { Integer.class, Object.class, Object.class };

			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});
		table.getColumnModel().getColumn(0).setPreferredWidth(40);
		scrollPane.setViewportView(table);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 2;
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

	/**
	 * Metoda dodaje menu do paska aplikacji
	 */
	private void addMenuBar() {
		this.addKeyListener(this);

		menu = new JMenu("Menu");
		menu.setMnemonic(KeyEvent.VK_M); // press alt + m
		menu.addMenuListener(this);

		menuBar = new JMenuBar();
		menuBar.add(menu);

		gAbout = new JMenuItem("About program");
		gAbout.setMnemonic(KeyEvent.VK_A);
		gAbout.addActionListener(this);

		gSettings = new JMenuItem("Settings");
		gSettings.setMnemonic(KeyEvent.VK_S);
		gSettings.addActionListener(this);

		menu.add(gSettings);

		gExport = new JMenu("Export to");
		gExport.setEnabled(false);
		gExport.setMnemonic(KeyEvent.VK_E);
		menu.add(gExport);

		gICalendar = new JMenuItem("iCalendar");
		gICalendar.setMnemonic(KeyEvent.VK_S);
		gICalendar.addActionListener(this);
		gExport.add(gICalendar);

		gXml = new JMenuItem("XML file");
		gXml.setMnemonic(KeyEvent.VK_X);
		gXml.addActionListener(this);
		gExport.add(gXml);
		
		gImport = new JMenuItem("Import...");
		gImport.addActionListener(this);
		menu.add(gImport);
		gImport.setMnemonic(KeyEvent.VK_I);
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
		// TODO Auto-generated method stub
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

	/**
	 * Metoda odpowiedzialna za wywołanie akcji
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource().equals(gSettings)) {
			SettingsDialog settingsDialog = new SettingsDialog(this);
			settingsDialog.setVisible(true);
			if (settingsDialog.getDialogResult() == 1) {
				try {
					eventManager.saveConfig();
				} catch (EventManagerException e1) {
					JOptionPane.showMessageDialog(window, e1.getMessage(), "Configuration", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (eventManager.connectToDatabase()) {
					eventManager.importEventsFromDatabase();
					refreshEventsTable();
				} else {
					JOptionPane.showMessageDialog(window,
							"Connection to database could not be established. Creating new events is disabled.\nFor more details, please test your connection in Settings.",
							"Database", JOptionPane.ERROR_MESSAGE);
				}
				enableEventCreation(eventManager.isDatabaseConnected());
			}
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
			if (eventWindow.getDialogResult() == 1)
				refreshEventsTable();
		}

		if (e.getSource().equals(deleteEventBtn)) {
			removeSelectedEvent();
		}

		if (e.getSource().equals(gICalendar)) {
			if (table.getSelectedRowCount() > 0) {
				try {
					String exportFilename = null;
					int eventId = (Integer) table.getValueAt(table.getSelectedRow(), 0);
					if (String.valueOf(eventId) != null && !String.valueOf(eventId).isEmpty()) {
						JFileChooser saveFileDialog = new JFileChooser();
						saveFileDialog.setFileFilter(new FileNameExtensionFilter("iCalendar file", "ics"));
						int dialogResult = saveFileDialog.showSaveDialog(window);
						if (dialogResult == JFileChooser.APPROVE_OPTION) {
							String filename = saveFileDialog.getSelectedFile().getName();
							if (!filename.endsWith(".ics"))
								filename += ".ics";
							exportFilename = saveFileDialog.getCurrentDirectory().toString() + "/" + filename;
						}
						if (exportFilename == null)
							return;
						eventManager.exportToICalendar(eventId, exportFilename);
					}

				} catch (IOException | ValidationException | ParseException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		if (e.getSource().equals(gXml)) {			
			if(table.getSelectedRowCount() > 0) {
				String exportFilename = null;
				int eventId = (Integer) table.getValueAt(table.getSelectedRow(), 0);
				if (String.valueOf(eventId) != null && !String.valueOf(eventId).isEmpty()) {
					JFileChooser saveFileDialog = new JFileChooser();
					saveFileDialog.setFileFilter(new FileNameExtensionFilter("XML file", "xml"));				
					int dialogResult = saveFileDialog.showSaveDialog(window);
					if (dialogResult == JFileChooser.APPROVE_OPTION) {							
						String filename = saveFileDialog.getSelectedFile().getName();							
						if (!filename.endsWith(".xml"))
							filename += ".xml";														
						exportFilename = saveFileDialog.getCurrentDirectory().toString() + "/" + filename;
					}
					if (exportFilename == null)
						return;
					eventManager.sendDataToXml(eventId, exportFilename);										
				}
			}
		}
		
		if (e.getSource().equals(gImport)) {	
			try {
				String importFilename = null;
				JFileChooser openFileDialog = new JFileChooser();
				openFileDialog.setFileFilter(new FileNameExtensionFilter("XML file", "xml"));				
				int dialogResult = openFileDialog.showSaveDialog(window);
				if (dialogResult == JFileChooser.APPROVE_OPTION) {							
					String filename = openFileDialog.getSelectedFile().getName();																						
					importFilename = openFileDialog.getCurrentDirectory().toString() + "/" + filename;
				}
				if (importFilename == null)
					return;
				eventManager.importEventsFromXml(importFilename);
				refreshEventsTable();						
			} catch (ParseException | EventEmptyFieldException | EventInvalidDateException | EventInvalidTimeException
					| TimerDateTimeException | SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
		}
	}
	
	/**
	 * Metoda odpowiedzialna za usuwanie wybranego Eventu z tabeli
	 */
	public void removeSelectedEvent() {
		if (table.getSelectedRowCount() > 0) {
			int eventId = (Integer) table.getValueAt(table.getSelectedRow(), 0);
			if (JOptionPane.showConfirmDialog(null, "Do you really want to remove selected event?", "Delete event",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				try {
					eventManager.removeEvent(eventId);
					refreshEventsTable();
				} catch (EventManagerException | SQLException e1) {
					JOptionPane.showMessageDialog(null, e1.getMessage(), "Delete event", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			}
		}
	}
	
	public void enableEventCreation(boolean enabled)
	{
		createEventBtn.setEnabled(enabled);
		deleteEventBtn.setEnabled(enabled);
		gImport.setEnabled(enabled);
	}

	public void showInformationMessageDialog(String title, String infoMessage) {
		JOptionPane.showMessageDialog(null, infoMessage, title, JOptionPane.INFORMATION_MESSAGE);

	}
}
