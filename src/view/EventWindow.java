package view;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.text.DateFormatter;

import com.toedter.calendar.JDateChooser;

import controller.EventManager;
import controller.exception.EventManagerException;
import model.exception.EventEmptyFieldException;
import model.exception.EventInvalidDateException;

import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JButton;
import javax.swing.JDialog;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.awt.event.ActionEvent;

public class EventWindow {

	public JDialog eventDialog;
	public JFrame frmEventCreator;
	public JTextField titleField;
	public JTextField locationField;
	public JTextField startDateField;
	private JLabel lblEndDate;
	private JLabel lblLocation;
	private JLabel lblDescription;
	private JScrollPane scrollPane;
	public JTextArea descriptionArea;
	private JButton btnCancel;
	private JButton btnSubmit;
	public JPanel endDatePanel;
	public JDateChooser chooser = new JDateChooser();
	public SpinnerDateModel spinnerDateModel;
	public JSpinner spinner;
	private EventManager eventManager;
	private JButton btnSetAlarm;
	private JLabel lblStartTime;
	private JLabel lblEndTime;
	private JPanel startTimePanel;
	private JPanel endTimePanel;
	public String startDateFieldValue;
	private JLabel lblAlarmWillGo;
	private JLabel lblBeforeEventsStart;
	private JPanel timerPanel;
	private JButton btnDismiss;

	public JDateChooser getChooser() {
		return chooser;
	}

	public void setChooser(JDateChooser chooser) {
		this.chooser = chooser;
	}
	
	public JSpinner addTimeSpinner(JSpinner spinner, String timeFormat) {
		spinner.setFont(new Font("Tahoma", Font.PLAIN, 11));
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, timeFormat);
        DateFormatter formatter = (DateFormatter)editor.getTextField().getFormatter();
        formatter.setAllowsInvalid(false);
        formatter.setOverwriteMode(true);
        spinner.setEditor(editor);        
        return spinner;		
	}


	public JTextField getStartDateField() {
		return startDateField;
	}

	public void setStartDateField(JTextField startDateField) {
		this.startDateField = startDateField;
	}

	public EventWindow(MainWindow mainWindow) {
		this.eventManager = mainWindow.getEventManager();
		initialize();
	}

	private void initialize() {
		frmEventCreator = new JFrame();
		frmEventCreator.setTitle("Event Creator");
		eventDialog = new JDialog(frmEventCreator, frmEventCreator.getTitle(), true);

		frmEventCreator.setBounds(100, 100, 433, 718);
		frmEventCreator.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 126, 281, 0 };
		gridBagLayout.rowHeights = new int[] { 45, 45, 45, 45, 45, 45, 100, 45, 45, 30, 45, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		frmEventCreator.getContentPane().setLayout(gridBagLayout);

		JLabel lblTitle = new JLabel("Title: ");
		lblTitle.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_lblTitle = new GridBagConstraints();
		gbc_lblTitle.insets = new Insets(0, 0, 5, 5);
		gbc_lblTitle.gridx = 0;
		gbc_lblTitle.gridy = 0;
		frmEventCreator.getContentPane().add(lblTitle, gbc_lblTitle);

		titleField = new JTextField();
		titleField.setFont(new Font("Tahoma", Font.PLAIN, 12));
		titleField.setColumns(10);
		GridBagConstraints gbc_titleField = new GridBagConstraints();
		gbc_titleField.anchor = GridBagConstraints.WEST;
		gbc_titleField.insets = new Insets(0, 0, 5, 0);
		gbc_titleField.gridx = 1;
		gbc_titleField.gridy = 0;
		frmEventCreator.getContentPane().add(titleField, gbc_titleField);

		lblLocation = new JLabel("Location: ");
		lblLocation.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_lblLocation = new GridBagConstraints();
		gbc_lblLocation.insets = new Insets(0, 0, 5, 5);
		gbc_lblLocation.gridx = 0;
		gbc_lblLocation.gridy = 1;
		frmEventCreator.getContentPane().add(lblLocation, gbc_lblLocation);

		locationField = new JTextField();
		locationField.setFont(new Font("Tahoma", Font.PLAIN, 12));
		locationField.setColumns(10);
		GridBagConstraints gbc_locationField = new GridBagConstraints();
		gbc_locationField.anchor = GridBagConstraints.WEST;
		gbc_locationField.insets = new Insets(0, 0, 5, 0);
		gbc_locationField.gridx = 1;
		gbc_locationField.gridy = 1;
		frmEventCreator.getContentPane().add(locationField, gbc_locationField);

		JLabel lblStartDate = new JLabel("Start date: ");
		lblStartDate.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_lblStartDate = new GridBagConstraints();
		gbc_lblStartDate.insets = new Insets(0, 0, 5, 5);
		gbc_lblStartDate.gridx = 0;
		gbc_lblStartDate.gridy = 2;
		frmEventCreator.getContentPane().add(lblStartDate, gbc_lblStartDate);

		startDateField = new JTextField();
		startDateField.setFont(new Font("Tahoma", Font.PLAIN, 12));
		startDateField.setColumns(10);
		GridBagConstraints gbc_startDateField = new GridBagConstraints();
		gbc_startDateField.anchor = GridBagConstraints.WEST;
		gbc_startDateField.insets = new Insets(0, 0, 5, 0);
		gbc_startDateField.gridx = 1;
		gbc_startDateField.gridy = 2;
		frmEventCreator.getContentPane().add(startDateField, gbc_startDateField);
		
		lblStartTime = new JLabel("Start time: ");
		lblStartTime.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_lblStartTime = new GridBagConstraints();
		gbc_lblStartTime.insets = new Insets(0, 0, 5, 5);
		gbc_lblStartTime.gridx = 0;
		gbc_lblStartTime.gridy = 3;
		frmEventCreator.getContentPane().add(lblStartTime, gbc_lblStartTime);
		
		startTimePanel = new JPanel();
		GridBagConstraints gbc_startTimePanel = new GridBagConstraints();
		gbc_startTimePanel.anchor = GridBagConstraints.WEST;
		gbc_startTimePanel.insets = new Insets(0, 0, 5, 0);
		gbc_startTimePanel.gridx = 1;
		gbc_startTimePanel.gridy = 3;
		JSpinner spinnerStart = new JSpinner(new SpinnerDateModel());
	//	startTimePanel.add(addTimeSpinner(spinnerStart, "HH:mm:ss"));
		frmEventCreator.getContentPane().add(startTimePanel, gbc_startTimePanel);

		lblEndDate = new JLabel("End date: ");
		lblEndDate.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_lblEndDate = new GridBagConstraints();
		gbc_lblEndDate.insets = new Insets(0, 0, 5, 5);
		gbc_lblEndDate.gridx = 0;
		gbc_lblEndDate.gridy = 4;
		frmEventCreator.getContentPane().add(lblEndDate, gbc_lblEndDate);

		endDatePanel = new JPanel();
		GridBagConstraints gbc_endDatePanel = new GridBagConstraints();
		gbc_endDatePanel.anchor = GridBagConstraints.WEST;
		gbc_endDatePanel.insets = new Insets(0, 0, 5, 0);
		gbc_endDatePanel.gridx = 1;
		gbc_endDatePanel.gridy = 4;
		endDatePanel.add(chooser);
		frmEventCreator.getContentPane().add(endDatePanel, gbc_endDatePanel);
		
		lblEndTime = new JLabel("End time: ");
		lblEndTime.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_lblEndTime = new GridBagConstraints();
		gbc_lblEndTime.insets = new Insets(0, 0, 5, 5);
		gbc_lblEndTime.gridx = 0;
		gbc_lblEndTime.gridy = 5;
		frmEventCreator.getContentPane().add(lblEndTime, gbc_lblEndTime);
		
		endTimePanel = new JPanel();
		GridBagConstraints gbc_endTimePanel = new GridBagConstraints();
		gbc_endTimePanel.anchor = GridBagConstraints.WEST;
		gbc_endTimePanel.insets = new Insets(0, 0, 5, 0);
		gbc_endTimePanel.gridx = 1;
		gbc_endTimePanel.gridy = 5;
		JSpinner spinnerEnd = new JSpinner(new SpinnerDateModel());
	//	endTimePanel.add(addTimeSpinner(spinnerEnd, "HH:mm:ss"));
		frmEventCreator.getContentPane().add(endTimePanel, gbc_endTimePanel);

		lblDescription = new JLabel("Description: ");
		lblDescription.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.BASELINE;
		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 6;
		frmEventCreator.getContentPane().add(lblDescription, gbc_lblDescription);

		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.anchor = GridBagConstraints.BASELINE;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 10);
		gbc_scrollPane.fill = GridBagConstraints.HORIZONTAL;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 6;
		frmEventCreator.getContentPane().add(scrollPane, gbc_scrollPane);

		descriptionArea = new JTextArea();
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		descriptionArea.setRows(8);
		descriptionArea.setColumns(8);
		scrollPane.setViewportView(descriptionArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmEventCreator.dispose();
			}
		});
		
		btnSetAlarm = new JButton("Set alarm");
		btnSetAlarm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {     
                Date timerValue = (Date) ((JSpinner) timerPanel.getComponent(0)).getValue();     
                Date startTimeValue = (Date) ((JSpinner) startTimePanel.getComponent(0)).getValue();
				String startDateValue = startDateField.getText();	
				eventManager.setAlarmGoOffDate(timerValue, startTimeValue, startDateValue);								
			}
		});
		btnSetAlarm.setForeground(Color.BLACK);
		btnSetAlarm.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnSetAlarm.setBackground(new Color(153, 204, 255));
		GridBagConstraints gbc_btnSetAlarm = new GridBagConstraints();
		gbc_btnSetAlarm.insets = new Insets(0, 0, 5, 5);
		gbc_btnSetAlarm.gridx = 0;
		gbc_btnSetAlarm.gridy = 7;
		frmEventCreator.getContentPane().add(btnSetAlarm, gbc_btnSetAlarm);
		
		lblAlarmWillGo = new JLabel("Alarm will go off ");
		lblAlarmWillGo.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_lblAlarmWillGo = new GridBagConstraints();
		gbc_lblAlarmWillGo.insets = new Insets(0, 0, 5, 0);
		gbc_lblAlarmWillGo.gridx = 1;
		gbc_lblAlarmWillGo.gridy = 7;
		frmEventCreator.getContentPane().add(lblAlarmWillGo, gbc_lblAlarmWillGo);
		
		timerPanel = new JPanel();
		GridBagConstraints gbc_timerPanel = new GridBagConstraints();
		gbc_timerPanel.insets = new Insets(0, 0, 5, 0);
		gbc_timerPanel.fill = GridBagConstraints.BOTH;
		gbc_timerPanel.gridx = 1;
		gbc_timerPanel.gridy = 8;
		JSpinner spinnerTimer = new JSpinner(new SpinnerDateModel());
		//set default value
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		Date date = cal.getTime();
		
		btnDismiss = new JButton("Dismiss");
		btnDismiss.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		btnDismiss.setBackground(new Color(221, 160, 221));
		btnDismiss.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_btnDismiss = new GridBagConstraints();
		gbc_btnDismiss.insets = new Insets(0, 0, 5, 5);
		gbc_btnDismiss.gridx = 0;
		gbc_btnDismiss.gridy = 8;
		frmEventCreator.getContentPane().add(btnDismiss, gbc_btnDismiss);
		spinnerTimer.setValue(date);
		timerPanel.add(addTimeSpinner(spinnerTimer, "HH:mm"));
		frmEventCreator.getContentPane().add(timerPanel, gbc_timerPanel);
		
		lblBeforeEventsStart = new JLabel("before event's start time.");
		lblBeforeEventsStart.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_lblBeforeEventsStart = new GridBagConstraints();
		gbc_lblBeforeEventsStart.insets = new Insets(0, 0, 5, 0);
		gbc_lblBeforeEventsStart.gridx = 1;
		gbc_lblBeforeEventsStart.gridy = 9;
		frmEventCreator.getContentPane().add(lblBeforeEventsStart, gbc_lblBeforeEventsStart);
		btnCancel.setForeground(Color.BLACK);
		btnCancel.setBackground(new Color(255, 51, 102));
		btnCancel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.anchor = GridBagConstraints.SOUTH;
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 0;
		gbc_btnCancel.gridy = 10;
		frmEventCreator.getContentPane().add(btnCancel, gbc_btnCancel);

		btnSubmit = new JButton("Submit");
		btnSubmit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String titleValue, descriptionValue, locationValue, startDateValue, endDateValue, startTimeValue, endTimeValue;
				titleValue = titleField.getText();
				descriptionValue = descriptionArea.getText();
				locationValue = locationField.getText();
				startDateValue = startDateField.getText();
				Date fetchedDate = ((JDateChooser) endDatePanel.getComponent(0)).getDate();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				endDateValue = dateFormat.format(fetchedDate);			
				Date valueStart = (Date) ((JSpinner) startTimePanel.getComponent(0)).getValue();
                SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                startTimeValue = format.format(valueStart);                 
				Date valueEnd = (Date) ((JSpinner) endTimePanel.getComponent(0)).getValue();
                endTimeValue = format.format(valueEnd);                   

				try {
					eventManager.addEvent(titleValue, descriptionValue, locationValue, startDateValue, endDateValue, startTimeValue, endTimeValue);
					showSuccessPane("Event created successfully");
					frmEventCreator.dispose();
				} catch (EventManagerException ex) {
					showErrorPane(ex.getMessage());
					System.out.println(ex.getMessage());
				} catch (EventInvalidDateException e1) {
					showErrorPane(e1.getMessage());
				} catch (EventEmptyFieldException e1) {
					showErrorPane(e1.getMessage());
				}

			}
		});
		btnSubmit.setForeground(new Color(0, 0, 0));
		btnSubmit.setBackground(new Color(0, 204, 102));
		btnSubmit.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_btnSubmit = new GridBagConstraints();
		gbc_btnSubmit.anchor = GridBagConstraints.SOUTH;
		gbc_btnSubmit.gridx = 1;
		gbc_btnSubmit.gridy = 10;
		frmEventCreator.getContentPane().add(btnSubmit, gbc_btnSubmit);

	}
	
	private void showErrorPane(String infoMessage) {
		JOptionPane.showMessageDialog(null, infoMessage, "Error", JOptionPane.ERROR_MESSAGE);

	}

	private void showSuccessPane(String infoMessage) {
		JOptionPane.showMessageDialog(null, infoMessage, "Success", JOptionPane.INFORMATION_MESSAGE);

	}
}
