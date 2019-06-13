package view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import controller.EventManager;

public class MainWindow extends JFrame implements MenuListener, ActionListener, KeyListener {
	private static final long serialVersionUID = 1L;
	private	JFrame window = new JFrame("Organizer App");
	private Container contentPane = window.getContentPane();
	
	private JMenu menu, databaseSubmenu, eventsSubmenu;
	private JMenuBar menuBar;
	private JMenuItem eSave, eOpen, dImport, dExport, gPreferences, gAbout, gExit;
	private JPanel calendarPanel, datePanel;	
	private JTextField filename = new JTextField(), dir = new JTextField(), selectedDate = new JTextField();
	private JButton createEventBtn;
	private EventManager eventManager = new EventManager(this);

	private String eventDate;

	public MainWindow() {
	    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    addCalendar();
	    addMenuBar();
	    addCreateEventButton();

	    window.setBounds(0, 0, 500, 300);
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
	    contentPane.add(calendarPanel, BorderLayout.CENTER);
	}
		
	private void showSelectedDate(CalendarComponent calendar) {		
		datePanel = new JPanel();
		datePanel.add(new JLabel("Selected date:"));
		
		//initial value in date label
    	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date today = new Date();
		setEventDate(dateFormat.format(today));
		selectedDate.setText(eventDate);
		
		//updating value in date label
		calendar.getCalendar().getDayChooser().setMinSelectableDate(today);
		calendar.getCalendar().getDayChooser().addPropertyChangeListener("day", new PropertyChangeListener() {
		    @Override
		    public void propertyChange(PropertyChangeEvent e) {
		    	Date currentDate = calendar.getCalendar().getDate();
		    	setEventDate(dateFormat.format(currentDate));
		    	selectedDate.setText(getEventDate());	        		    
		    }
	    });		
    	datePanel.add(selectedDate);		
		contentPane.add(datePanel, BorderLayout.BEFORE_FIRST_LINE);		
	}	

	private void addCreateEventButton() {
		createEventBtn = new JButton("Create event");
		createEventBtn.addActionListener(this);
        contentPane.add(createEventBtn, BorderLayout.SOUTH);

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
}


