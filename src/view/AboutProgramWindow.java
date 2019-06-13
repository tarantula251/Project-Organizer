package view;

public class AboutProgramWindow {
	private String aboutProgramMessage = "";
	
	public AboutProgramWindow() {
		String pageTitle = "<html><h1 align='center' style='color:#666699;font-size:12px;font-family:verdana;'>Organizer Application</h1>";
		String pageHeader = "<h2 align='center' style='font-size:8px;font-family:verdana;font-weight:bold;'>Created by Franciszek Demski & Justyna Gasior</h2>";
		String pageContent= "<div align='center' style='font-size:8px;font-family:verdana;font-weight:normal;text-align:justify;padding: 3px 10px;'><p >&#9;&#9;&#9;This program is a simple organizer that allows you to use calendar, save your events and set reminders.</p><p>You can use it to manage your plans and check previous events.</p></div>";
		String pageFooter = "<h4 align='center' style='font-size:6px;font-family:verdana;font-weight:lighter;'>Version: 1.0 2019-06</h3></html>";		
		aboutProgramMessage = pageTitle + pageHeader + pageContent + pageFooter;
	}

	public String getAboutProgramMessage() {
		return aboutProgramMessage;
	}
}
