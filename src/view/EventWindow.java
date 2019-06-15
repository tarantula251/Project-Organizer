package view;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.JSpinner.DateEditor;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import java.awt.FlowLayout;
import javax.swing.SwingConstants;
import java.awt.Dialog.ModalExclusionType;

public class EventWindow {

	public JDialog eventDialog;
	public JDialog frmEventCreator;
	public JTextField titleField;
	public JTextField locationField;
	public JDateChooser startDateField;
	private JLabel lblEndDate;
	private JLabel lblLocation;
	private JLabel lblDescription;
	private JScrollPane scrollPane;
	public JTextArea descriptionArea;
	private JButton btnCancel;
	private JButton btnSubmit;
	public JPanel startDatePanel;
	public JPanel endDatePanel;
	public JDateChooser chooserStart = new JDateChooser();
	public JDateChooser chooser = new JDateChooser();
	public SpinnerDateModel spinnerDateModel;
	public JSpinner spinner;
	private EventManager eventManager;
	private JLabel lblStartTime;
	private JLabel lblEndTime;
	private JPanel startTimePanel;
	private JPanel endTimePanel;
	public String startDateFieldValue;
	private JLabel lblAlarmWillGo;
	private JLabel lblBeforeEventsStart;
	private JPanel timerPanel;
	private JCheckBox chckbxSetAlarm;
	private JPanel panel;
	private int dialogResult = 0;

	public void setVisible(Boolean visible)
	{
		frmEventCreator.setVisible(visible);
	}
	
	public int getDialogResult() {
		return dialogResult;
	}

	public JDateChooser getChooser() {
		return chooser;
	}

	public void setChooser(JDateChooser chooser) {
		this.chooser = chooser;
	}

	public JSpinner addTimeSpinner(JSpinner spinner, String timeFormat) {
		spinner.setFont(new Font("Tahoma", Font.PLAIN, 11));
		JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, timeFormat);
		DateFormatter formatter = (DateFormatter) editor.getTextField().getFormatter();
		formatter.setAllowsInvalid(false);
		formatter.setOverwriteMode(true);
		spinner.setEditor(editor);
		return spinner;
	}

	public JDateChooser getStartDateField() {
		return startDateField;
	}

	public void setStartDateField(JDateChooser startDateField) {
		this.startDateField = startDateField;
	}

	public EventWindow(MainWindow mainWindow) {
		this.eventManager = mainWindow.getEventManager();
		initialize(mainWindow.getWindow());
	}

	private void initialize(JFrame parent) {
		frmEventCreator = new JDialog(parent, "Event creator", true);
		frmEventCreator.setModal(true);
		frmEventCreator.setResizable(false);
		frmEventCreator.setTitle("Event Creator");
		eventDialog = new JDialog(frmEventCreator, frmEventCreator.getTitle(), true);

		frmEventCreator.setBounds(100, 100, 380, 431);
		frmEventCreator.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 80, 93, 93, 88, 0 };
		gridBagLayout.rowHeights = new int[] { 21, 21, 30, 25, 150, 23, 15, 30, 15, 33, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		frmEventCreator.getContentPane().setLayout(gridBagLayout);

		JLabel lblTitle = new JLabel("Title: ");
		lblTitle.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_lblTitle = new GridBagConstraints();
		gbc_lblTitle.anchor = GridBagConstraints.WEST;
		gbc_lblTitle.insets = new Insets(0, 0, 5, 5);
		gbc_lblTitle.gridx = 0;
		gbc_lblTitle.gridy = 0;
		frmEventCreator.getContentPane().add(lblTitle, gbc_lblTitle);

		titleField = new JTextField();
		titleField.setFont(new Font("Tahoma", Font.PLAIN, 12));
		titleField.setColumns(10);
		GridBagConstraints gbc_titleField = new GridBagConstraints();
		gbc_titleField.fill = GridBagConstraints.HORIZONTAL;
		gbc_titleField.insets = new Insets(0, 0, 5, 0);
		gbc_titleField.gridwidth = 3;
		gbc_titleField.gridx = 1;
		gbc_titleField.gridy = 0;
		frmEventCreator.getContentPane().add(titleField, gbc_titleField);

		lblLocation = new JLabel("Location: ");
		lblLocation.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_lblLocation = new GridBagConstraints();
		gbc_lblLocation.anchor = GridBagConstraints.WEST;
		gbc_lblLocation.insets = new Insets(0, 0, 5, 5);
		gbc_lblLocation.gridx = 0;
		gbc_lblLocation.gridy = 1;
		frmEventCreator.getContentPane().add(lblLocation, gbc_lblLocation);

		locationField = new JTextField();
		locationField.setFont(new Font("Tahoma", Font.PLAIN, 12));
		locationField.setColumns(10);
		GridBagConstraints gbc_locationField = new GridBagConstraints();
		gbc_locationField.fill = GridBagConstraints.HORIZONTAL;
		gbc_locationField.insets = new Insets(0, 0, 5, 0);
		gbc_locationField.gridwidth = 3;
		gbc_locationField.gridx = 1;
		gbc_locationField.gridy = 1;
		frmEventCreator.getContentPane().add(locationField, gbc_locationField);

		JLabel lblStartDate = new JLabel("Start date: ");
		lblStartDate.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_lblStartDate = new GridBagConstraints();
		gbc_lblStartDate.anchor = GridBagConstraints.WEST;
		gbc_lblStartDate.insets = new Insets(0, 0, 5, 5);
		gbc_lblStartDate.gridx = 0;
		gbc_lblStartDate.gridy = 2;
		frmEventCreator.getContentPane().add(lblStartDate, gbc_lblStartDate);

		startDateField = new JDateChooser();
		startDateField.setFont(new Font("Tahoma", Font.PLAIN, 12));
		startDateField.setDateFormatString("yyyy-MM-dd");
		GridBagConstraints gbc_startDateField = new GridBagConstraints();
		gbc_startDateField.insets = new Insets(0, 0, 5, 5);
		gbc_startDateField.gridx = 1;
		gbc_startDateField.gridy = 2;
		frmEventCreator.getContentPane().add(startDateField, gbc_startDateField);

		lblStartTime = new JLabel("Start time: ");
		lblStartTime.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_lblStartTime = new GridBagConstraints();
		gbc_lblStartTime.insets = new Insets(0, 0, 5, 5);
		gbc_lblStartTime.gridx = 2;
		gbc_lblStartTime.gridy = 2;
		frmEventCreator.getContentPane().add(lblStartTime, gbc_lblStartTime);

		startTimePanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) startTimePanel.getLayout();
		flowLayout.setVgap(0);
		flowLayout.setHgap(0);
		flowLayout.setAlignment(FlowLayout.LEFT);
		JSpinner spinnerStart = new JSpinner(new SpinnerDateModel());
		spinnerStart.setFont(new Font("Tahoma", Font.PLAIN, 12));
		spinnerStart.setEditor(new JSpinner.DateEditor(spinnerStart, "HH:mm:ss"));
		startTimePanel.add(spinnerStart);
		GridBagConstraints gbc_startTimePanel = new GridBagConstraints();
		gbc_startTimePanel.insets = new Insets(0, 0, 5, 0);
		gbc_startTimePanel.gridx = 3;
		gbc_startTimePanel.gridy = 2;
		frmEventCreator.getContentPane().add(startTimePanel, gbc_startTimePanel);

		lblEndDate = new JLabel("End date: ");
		lblEndDate.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_lblEndDate = new GridBagConstraints();
		gbc_lblEndDate.anchor = GridBagConstraints.WEST;
		gbc_lblEndDate.insets = new Insets(0, 0, 5, 5);
		gbc_lblEndDate.gridx = 0;
		gbc_lblEndDate.gridy = 3;
		frmEventCreator.getContentPane().add(lblEndDate, gbc_lblEndDate);

		endDatePanel = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) endDatePanel.getLayout();
		flowLayout_1.setVgap(0);
		flowLayout_1.setHgap(0);
		endDatePanel.add(chooser);
		GridBagConstraints gbc_endDatePanel = new GridBagConstraints();
		gbc_endDatePanel.insets = new Insets(0, 0, 5, 5);
		gbc_endDatePanel.gridx = 1;
		gbc_endDatePanel.gridy = 3;
		frmEventCreator.getContentPane().add(endDatePanel, gbc_endDatePanel);

		lblEndTime = new JLabel("End time: ");
		lblEndTime.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_lblEndTime = new GridBagConstraints();
		gbc_lblEndTime.insets = new Insets(0, 0, 5, 5);
		gbc_lblEndTime.gridx = 2;
		gbc_lblEndTime.gridy = 3;
		frmEventCreator.getContentPane().add(lblEndTime, gbc_lblEndTime);

		endTimePanel = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) endTimePanel.getLayout();
		flowLayout_2.setVgap(0);
		flowLayout_2.setHgap(0);
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		JSpinner spinnerEnd = new JSpinner(new SpinnerDateModel());
		spinnerEnd.setFont(new Font("Tahoma", Font.PLAIN, 12));
		spinnerEnd.setEditor(new JSpinner.DateEditor(spinnerEnd, "HH:mm:ss"));
		endTimePanel.add(spinnerEnd);
		GridBagConstraints gbc_endTimePanel = new GridBagConstraints();
		gbc_endTimePanel.insets = new Insets(0, 0, 5, 0);
		gbc_endTimePanel.gridx = 3;
		gbc_endTimePanel.gridy = 3;
		frmEventCreator.getContentPane().add(endTimePanel, gbc_endTimePanel);

		lblDescription = new JLabel("Description: ");
		lblDescription.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDescription.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.WEST;
		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 4;
		frmEventCreator.getContentPane().add(lblDescription, gbc_lblDescription);

		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.HORIZONTAL;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 4;
		frmEventCreator.getContentPane().add(scrollPane, gbc_scrollPane);

		descriptionArea = new JTextArea();
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		descriptionArea.setRows(8);
		descriptionArea.setColumns(8);
		scrollPane.setViewportView(descriptionArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		chckbxSetAlarm = new JCheckBox("Set alarm");
		chckbxSetAlarm.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_chckbxSetAlarm = new GridBagConstraints();
		gbc_chckbxSetAlarm.gridwidth = 3;
		gbc_chckbxSetAlarm.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxSetAlarm.gridx = 1;
		gbc_chckbxSetAlarm.gridy = 5;
		frmEventCreator.getContentPane().add(chckbxSetAlarm, gbc_chckbxSetAlarm);

		lblAlarmWillGo = new JLabel("Alarm will go off ");
		lblAlarmWillGo.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_lblAlarmWillGo = new GridBagConstraints();
		gbc_lblAlarmWillGo.gridwidth = 3;
		gbc_lblAlarmWillGo.insets = new Insets(0, 0, 5, 0);
		gbc_lblAlarmWillGo.gridx = 1;
		gbc_lblAlarmWillGo.gridy = 6;
		frmEventCreator.getContentPane().add(lblAlarmWillGo, gbc_lblAlarmWillGo);

		timerPanel = new JPanel();
		JSpinner spinnerTimer = new JSpinner(new SpinnerDateModel());
		spinnerTimer.setFont(new Font("Tahoma", Font.PLAIN, 12));
		// set default value
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		Date date = cal.getTime();
		spinnerTimer.setValue(date);
		spinnerTimer.setEditor(new JSpinner.DateEditor(spinnerTimer, "HH:mm"));
		timerPanel.add(spinnerTimer);
		GridBagConstraints gbc_timerPanel = new GridBagConstraints();
		gbc_timerPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_timerPanel.insets = new Insets(0, 0, 5, 0);
		gbc_timerPanel.gridwidth = 3;
		gbc_timerPanel.gridx = 1;
		gbc_timerPanel.gridy = 7;
		frmEventCreator.getContentPane().add(timerPanel, gbc_timerPanel);

		lblBeforeEventsStart = new JLabel("before event's start time.");
		lblBeforeEventsStart.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_lblBeforeEventsStart = new GridBagConstraints();
		gbc_lblBeforeEventsStart.anchor = GridBagConstraints.NORTH;
		gbc_lblBeforeEventsStart.insets = new Insets(0, 0, 5, 0);
		gbc_lblBeforeEventsStart.gridwidth = 3;
		gbc_lblBeforeEventsStart.gridx = 1;
		gbc_lblBeforeEventsStart.gridy = 8;
		frmEventCreator.getContentPane().add(lblBeforeEventsStart, gbc_lblBeforeEventsStart);

		panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.anchor = GridBagConstraints.NORTH;
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.gridwidth = 4;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 9;
		frmEventCreator.getContentPane().add(panel, gbc_panel);
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));

		btnSubmit = new JButton("Submit");
		panel.add(btnSubmit);
		btnSubmit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String titleValue, descriptionValue, locationValue, startDateValue, endDateValue, startTimeValue,
						endTimeValue;
				titleValue = titleField.getText();
				descriptionValue = descriptionArea.getText();
				locationValue = locationField.getText();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				startDateValue = dateFormat.format(startDateField.getDate());
				Date fetchedDate = ((JDateChooser) endDatePanel.getComponent(0)).getDate();
				endDateValue = dateFormat.format(fetchedDate);
				Date valueStart = (Date) ((JSpinner) startTimePanel.getComponent(0)).getValue();
				SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
				startTimeValue = format.format(valueStart);
				Date valueEnd = (Date) ((JSpinner) endTimePanel.getComponent(0)).getValue();
				endTimeValue = format.format(valueEnd);
				Date timerValue = (Date) ((JSpinner) timerPanel.getComponent(0)).getValue();
				Date alarmDateTimeValue = null;

				if (chckbxSetAlarm.isSelected())
					try {
						alarmDateTimeValue = eventManager.setAlarmGoOffDate(timerValue, valueStart, startDateValue);
					} catch (ParseException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				try {
					eventManager.addEvent(titleValue, descriptionValue, locationValue, startDateValue, endDateValue,
							startTimeValue, endTimeValue, alarmDateTimeValue);
					showPane("Success", "Event created successfully");
					frmEventCreator.dispose();
				} catch (EventManagerException ex) {
					showErrorPane(ex.getMessage());
					System.out.println(ex.getMessage());
				} catch (EventInvalidDateException e1) {
					showErrorPane(e1.getMessage());
				} catch (EventEmptyFieldException e1) {
					showErrorPane(e1.getMessage());
				}
				dialogResult = 1;
			}
		});
		btnSubmit.setForeground(new Color(0, 0, 0));
		btnSubmit.setBackground(new Color(0, 204, 102));
		btnSubmit.setFont(new Font("Tahoma", Font.PLAIN, 12));

		btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmEventCreator.dispose();
				dialogResult = 0;
			}
		});
		btnCancel.setForeground(Color.BLACK);
		btnCancel.setBackground(new Color(255, 51, 102));
		btnCancel.setFont(new Font("Tahoma", Font.PLAIN, 12));
	}

	private void showErrorPane(String infoMessage) {
		JOptionPane.showMessageDialog(null, infoMessage, "Error", JOptionPane.ERROR_MESSAGE);

	}

	public void showPane(String title, String infoMessage) {
		JOptionPane.showMessageDialog(null, infoMessage, title, JOptionPane.INFORMATION_MESSAGE);

	}
}
