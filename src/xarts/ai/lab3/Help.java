/**
 * 
 */
package xarts.ai.lab3;

/**
 * @author xarts
 *
 */
import java.awt.Dimension;
import java.io.InputStreamReader;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.miginfocom.swing.MigLayout;

/** Help window
 * @author Xarts
 *
 */
@SuppressWarnings("serial")
public class Help extends JFrame implements Runnable {
	
	private static final int minframeX = 500;
	private static final int minframeY = 500;

	public void run() {
		setTitle("Help");
		Dimension screenSize = getToolkit().getScreenSize();
		setPreferredSize(new Dimension(minframeX,minframeY));
		setLocation(((int)screenSize.getWidth() - minframeX) / 2,
				((int)screenSize.getHeight() - minframeY) / 2);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
		
		setLayout(new MigLayout());
		JTextPane text = new JTextPane();
		text.setEditable(false);
		text.setOpaque(false);
		try {
			EditorKit kit = new HTMLEditorKit();
			HTMLDocument doc = (HTMLDocument)kit.createDefaultDocument();
			doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
			text.setContentType("text/plain");
			text.read(new InputStreamReader(GameInterface.class.getResourceAsStream("resources/alemungula.txt")), null);
		} catch (Exception e) {
			System.out.println("Failed to load file");
			e.printStackTrace();
			text.setText("Failed to load file " + "alemungula.txt");
		}
		JScrollPane scroll = new JScrollPane(text);
		add(scroll, "dock center, grow");
	}
}

