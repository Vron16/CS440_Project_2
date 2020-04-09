import javax.swing.*;
import java.awt.*;

public class BoardCellPanel extends JPanel {
	private JLabel displayText = new JLabel();
	
	public BoardCellPanel() {
		setLayout(new GridBagLayout());
		add(displayText);
	}
	
	public JLabel getTextLabel() {
		return displayText;
	}
	
}
