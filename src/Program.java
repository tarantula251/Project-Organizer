import view.*;

public class Program {

	public static void main(String[] args){
		MainWindow window;
		try {
			window = new MainWindow();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		window.initialize();
	}
}
