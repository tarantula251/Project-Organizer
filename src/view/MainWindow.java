package view;

import java.awt.BorderLayout;
import java.awt.Container;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import javax.swing.table.DefaultTableModel;

public class MainWindow implements MenuListener, ActionListener, KeyListener {

	private static final long serialVersionUID = 1L;
	private	JFrame window = new JFrame("Organizer App");
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
	private JLabel label_1;
	private DefaultTableModel tableModel;
	private JScrollPane scrollPane;
	private JTable table;
	
	public MainWindow() throws LineUnavailableException, IOException, UnsupportedAudioFileException {		
		eventManager = new EventManager(this);
		initialize();	    
	}

	private void initialize() {
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    addCalendar();
	    addMenuBar();
	    window.setBounds(0, 0, 835, 659);
	    window.setLocationRelativeTo(null);
		window.setVisible(true);
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
	    CalendarComponent calendar = new CalendarComponent();
	    calendarPanel = calendar.getCalendarPanel();
	    showSelectedDate(calendar);
	    
	    GridBagConstraints gbc_calendar = new GridBagConstraints();
	    gbc_calendar.insets = new Insets(0, 0, 5, 5);
	    gbc_calendar.gridx = 1;
	    gbc_calendar.gridy = 1;
	    contentPane.add(calendarPanel, gbc_calendar);
	    
	    label_1 = new JLabel("View your events: ");
	    label_1.setFont(new Font("Tahoma", Font.PLAIN, 12));
	    GridBagConstraints gbc_label_1 = new GridBagConstraints();
	    gbc_label_1.anchor = GridBagConstraints.SOUTH;
	    gbc_label_1.insets = new Insets(0, 0, 5, 5);
	    gbc_label_1.gridx = 3;
	    gbc_label_1.gridy = 0;
	    window.getContentPane().add(label_1, gbc_label_1);
	    	    	    	 	     
	    scrollPane = new JScrollPane();
	    GridBagConstraints gbc_scrollPane = new GridBagConstraints();
	    gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
	    gbc_scrollPane.fill = GridBagConstraints.VERTICAL;
	    gbc_scrollPane.gridx = 3;
	    gbc_scrollPane.gridy = 1;
	    window.getContentPane().add(scrollPane, gbc_scrollPane);
	    
	    table = new JTable();
	    table.setModel(new DefaultTableModel(
	    	new Object[][] {
	    		{"101","Amit","670000"}
	    	},   
	    	new String[] {
	    		"Title", "Start date", "Start time"
	    	}
	    ));
	    scrollPane.setViewportView(table);
	    addRowToTable();
	    
	    label = new JLabel("Selected date: ");
	    label.setFont(new Font("Tahoma", Font.PLAIN, 12));
	    GridBagConstraints gbc_label = new GridBagConstraints();
	    gbc_label.insets = new Insets(0, 0, 5, 5);
	    gbc_label.gridx = 1;
	    gbc_label.gridy = 2;
	    window.getContentPane().add(label, gbc_label);
	    
	    textField = new JTextField();
	    textField.setText("2019-06-15");
	    textField.setFont(new Font("Tahoma", Font.PLAIN, 12));
	    GridBagConstraints gbc_textField = new GridBagConstraints();
	    gbc_textField.insets = new Insets(0, 0, 5, 5);
	    gbc_textField.gridx = 2;
	    gbc_textField.gridy = 2;
	    window.getContentPane().add(textField, gbc_textField);

	    createEventBtn = new JButton("Create event");
	    createEventBtn.setFont(new Font("Tahoma", Font.PLAIN, 12));
	    GridBagConstraints gbc_createEventBtn = new GridBagConstraints();
	    gbc_createEventBtn.insets = new Insets(0, 0, 0, 5);
	    gbc_createEventBtn.gridx = 1;
	    gbc_createEventBtn.gridy = 3;
	    window.getContentPane().add(createEventBtn, gbc_createEventBtn);
	}
	
	private void addRowToTable() {
		ArrayList<Event> eventsArray = eventManager.getEventCollection();
		DefaultTableModel model = (DefaultTableModel) table.getModel();

//		for (Event event : eventsArray) {
			for (int i = 0; i < 10; i++) {

			model.addRow(new Object[] {
				"101","Amit","670000"                   
                } 
			);						
		}
		
	}
		
	private void showSelectedDate(CalendarComponent calendar) {				
		//initial value in date label
    	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date today = new Date();
		setEventDate(dateFormat.format(today));
		
		//updating value in date label
		calendar.getCalendar().getDayChooser().setMinSelectableDate(today);
		calendar.getCalendar().getDayChooser().addPropertyChangeListener("day", new PropertyChangeListener() {
		    @Override
		    public void propertyChange(PropertyChangeEvent e) {
		    	Date currentDate = calendar.getCalendar().getDate();
		    	setEventDate(dateFormat.format(currentDate));		    	
		    	textField.setText(getEventDate());	        		    
		    }
	    });
    	GridBagLayout gridBagLayout = new GridBagLayout();
    	gridBagLayout.columnWidths = new int[]{0, 30, 35, 80, 25, 0};
    	gridBagLayout.rowHeights = new int[]{35, 150, 50, 35, 0};
    	gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
    	gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
    	window.getContentPane().setLayout(gridBagLayout);
	}	

	private void addMenuBar() {		
		this.addKeyListener(this);
				
		menu = new JMenu("Menu");
		menu.setMnemonic(KeyEvent.VK_M); //press alt + m
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
			//TODO go to preferences
        }
		
		if (e.getSource().equals(gAbout)) {
			JOptionPane.showMessageDialog(window, new AboutProgramWindow().getAboutProgramMessage(), "About Program", JOptionPane.PLAIN_MESSAGE);
        }
				
		if (e.getSource().equals(gExit)) {
			System.exit(0);		
        }
		
		if (e.getSource().equals(createEventBtn)) {
			EventWindow eventWindow = new EventWindow(this);
			eventManager.fillStartDateField(eventWindow);				
        }
		
	}
	
	public void showPane(String title, String infoMessage) {
		JOptionPane.showMessageDialog(null, infoMessage, title, JOptionPane.INFORMATION_MESSAGE);

	}
}
