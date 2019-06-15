import java.io.IOException;
import java.text.ParseException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import model.DataIO;
import model.exception.EventEmptyFieldException;
import model.exception.EventInvalidDateException;
import model.exception.EventInvalidTimeException;
import model.exception.TimerDateTimeException;
import view.*;

public class Program {

	public static void main(String[] args) throws LineUnavailableException, IOException, UnsupportedAudioFileException, ParseException, EventEmptyFieldException, EventInvalidDateException, EventInvalidTimeException, TimerDateTimeException {
		MainWindow window = new MainWindow();
	}

}
