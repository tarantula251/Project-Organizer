package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import model.DataIO;

import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JTextField;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.HashMap;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.awt.event.ActionEvent;

public class SettingsDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JPasswordField passwordField;
	private JTextField textFieldHostname;
	private JTextField textFieldDatabase;
	private JTextField textFieldUsername;
	private JSpinner spinnerPort;
	private HashMap<String, String> config = null;
	private int dialogResult = 0;

	/**
	 * Konstruktor klasy SettingsDialog tworzy okno dialogowe, służące do podania danych konfiguracyjnych
	 * @param window - obiekt łączący SettingsDialog z MainWindow, zapewniający dostęp do warstwy logiki
	 */
	public SettingsDialog(MainWindow window) {
		super(window.getWindow(), "Settings", true);
		config = window.getEventManager().getConfig();
		setBounds(100, 100, 450, 300);
		setLocationRelativeTo(null);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			JPanel databasePanel = new JPanel();
			databasePanel.setBorder(new EmptyBorder(15, 15, 15, 15));
			tabbedPane.add("Database", databasePanel);
			databasePanel.setLayout(new BorderLayout(0, 0));
			
			JPanel databaseContentPanel = new JPanel();
			databasePanel.add(databaseContentPanel);
			GridBagLayout gbl_databaseContentPanel = new GridBagLayout();
			gbl_databaseContentPanel.columnWidths = new int[]{57, 221, 26, 95, 0};
			gbl_databaseContentPanel.rowHeights = new int[]{20, 20, 20, 20, 23, 0};
			gbl_databaseContentPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
			gbl_databaseContentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			databaseContentPanel.setLayout(gbl_databaseContentPanel);
			{
				JLabel lblHostname = new JLabel("Hostname:");
				GridBagConstraints gbc_lblHostname = new GridBagConstraints();
				gbc_lblHostname.anchor = GridBagConstraints.WEST;
				gbc_lblHostname.insets = new Insets(0, 0, 5, 5);
				gbc_lblHostname.gridx = 0;
				gbc_lblHostname.gridy = 0;
				databaseContentPanel.add(lblHostname, gbc_lblHostname);
				lblHostname.setFont(new Font("Tahoma", Font.PLAIN, 12));
			}
			
			textFieldHostname = new JTextField();
			GridBagConstraints gbc_textFieldHostname = new GridBagConstraints();
			gbc_textFieldHostname.anchor = GridBagConstraints.NORTH;
			gbc_textFieldHostname.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldHostname.insets = new Insets(0, 0, 5, 5);
			gbc_textFieldHostname.gridx = 1;
			gbc_textFieldHostname.gridy = 0;
			databaseContentPanel.add(textFieldHostname, gbc_textFieldHostname);
			textFieldHostname.setColumns(10);
			textFieldHostname.setText(config.get("hostname"));
			
			JLabel lblPort = new JLabel("Port:");
			GridBagConstraints gbc_lblPort = new GridBagConstraints();
			gbc_lblPort.anchor = GridBagConstraints.WEST;
			gbc_lblPort.insets = new Insets(0, 0, 5, 5);
			gbc_lblPort.gridx = 2;
			gbc_lblPort.gridy = 0;
			databaseContentPanel.add(lblPort, gbc_lblPort);
			lblPort.setFont(new Font("Tahoma", Font.PLAIN, 12));
			
			spinnerPort = new JSpinner();
			GridBagConstraints gbc_spinnerPort = new GridBagConstraints();
			gbc_spinnerPort.anchor = GridBagConstraints.NORTH;
			gbc_spinnerPort.fill = GridBagConstraints.HORIZONTAL;
			gbc_spinnerPort.insets = new Insets(0, 0, 5, 0);
			gbc_spinnerPort.gridx = 3;
			gbc_spinnerPort.gridy = 0;
			databaseContentPanel.add(spinnerPort, gbc_spinnerPort);
			spinnerPort.setModel(new SpinnerNumberModel(3306, 0, 65535, 1));
			spinnerPort.setEditor(new JSpinner.NumberEditor(spinnerPort, "#"));
			spinnerPort.setValue(Integer.parseInt(config.get("port")));
			
			JLabel lblDatabase = new JLabel("Database:");
			GridBagConstraints gbc_lblDatabase = new GridBagConstraints();
			gbc_lblDatabase.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblDatabase.insets = new Insets(0, 0, 5, 5);
			gbc_lblDatabase.gridx = 0;
			gbc_lblDatabase.gridy = 1;
			databaseContentPanel.add(lblDatabase, gbc_lblDatabase);
			lblDatabase.setFont(new Font("Tahoma", Font.PLAIN, 12));
			
			textFieldDatabase = new JTextField();
			GridBagConstraints gbc_textFieldDatabase = new GridBagConstraints();
			gbc_textFieldDatabase.anchor = GridBagConstraints.NORTH;
			gbc_textFieldDatabase.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldDatabase.insets = new Insets(0, 0, 5, 0);
			gbc_textFieldDatabase.gridwidth = 3;
			gbc_textFieldDatabase.gridx = 1;
			gbc_textFieldDatabase.gridy = 1;
			databaseContentPanel.add(textFieldDatabase, gbc_textFieldDatabase);
			textFieldDatabase.setColumns(10);
			textFieldDatabase.setText(config.get("database"));
			
			JLabel lblUsername = new JLabel("Username:");
			GridBagConstraints gbc_lblUsername = new GridBagConstraints();
			gbc_lblUsername.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblUsername.insets = new Insets(0, 0, 5, 5);
			gbc_lblUsername.gridx = 0;
			gbc_lblUsername.gridy = 2;
			databaseContentPanel.add(lblUsername, gbc_lblUsername);
			lblUsername.setFont(new Font("Tahoma", Font.PLAIN, 12));
			
			textFieldUsername = new JTextField();
			GridBagConstraints gbc_textFieldUsername = new GridBagConstraints();
			gbc_textFieldUsername.anchor = GridBagConstraints.NORTH;
			gbc_textFieldUsername.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldUsername.insets = new Insets(0, 0, 5, 0);
			gbc_textFieldUsername.gridwidth = 3;
			gbc_textFieldUsername.gridx = 1;
			gbc_textFieldUsername.gridy = 2;
			databaseContentPanel.add(textFieldUsername, gbc_textFieldUsername);
			textFieldUsername.setColumns(10);
			textFieldUsername.setText(config.get("username"));
			
			JLabel lblPassword = new JLabel("Password:");
			GridBagConstraints gbc_lblPassword = new GridBagConstraints();
			gbc_lblPassword.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
			gbc_lblPassword.gridx = 0;
			gbc_lblPassword.gridy = 3;
			databaseContentPanel.add(lblPassword, gbc_lblPassword);
			lblPassword.setFont(new Font("Tahoma", Font.PLAIN, 12));
			
			passwordField = new JPasswordField();
			GridBagConstraints gbc_passwordField = new GridBagConstraints();
			gbc_passwordField.anchor = GridBagConstraints.NORTH;
			gbc_passwordField.fill = GridBagConstraints.HORIZONTAL;
			gbc_passwordField.insets = new Insets(0, 0, 5, 0);
			gbc_passwordField.gridwidth = 3;
			gbc_passwordField.gridx = 1;
			gbc_passwordField.gridy = 3;
			databaseContentPanel.add(passwordField, gbc_passwordField);
			passwordField.setText(config.get("password"));
			
			JButton btnTestConnection = new JButton("Test Connection");
			btnTestConnection.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						DataIO.testConnectionToDatabase(textFieldHostname.getText(), spinnerPort.getValue().toString(), textFieldDatabase.getText(), textFieldUsername.getText(), new String(passwordField.getPassword()));
						JOptionPane.showMessageDialog(null, "Connected to database successfully.", "Connection Test", JOptionPane.INFORMATION_MESSAGE);
					} catch (SQLException e1) {
						JOptionPane.showMessageDialog(null, "Failed to connect to database.\n\nDetails:\nError " + Integer.toString(e1.getErrorCode()) + " - " + e1.getMessage(), "Connection Test", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			GridBagConstraints gbc_btnTestConnection = new GridBagConstraints();
			gbc_btnTestConnection.anchor = GridBagConstraints.NORTHEAST;
			gbc_btnTestConnection.gridwidth = 3;
			gbc_btnTestConnection.gridx = 1;
			gbc_btnTestConnection.gridy = 4;
			databaseContentPanel.add(btnTestConnection, gbc_btnTestConnection);
			contentPanel.add(tabbedPane);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dialogResult = 1;
						updateConfig();
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dialogResult = 0;
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	
	public int getDialogResult() {
		return dialogResult;
	}

	private void updateConfig()
	{
		config.put("hostname", textFieldHostname.getText());
		config.put("port", spinnerPort.getValue().toString());
		config.put("database", textFieldDatabase.getText());
		config.put("username", textFieldUsername.getText());
		config.put("passowrd", new String(passwordField.getPassword()));
	}
}
