/**
 * 
 */
package xarts.ai.lab3;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

/** About window.
 * @author xarts
 *
 */
@SuppressWarnings("serial")
public class About extends JFrame implements Runnable {
		
	private static final int minframeX = 300;
	private static final int minframeY = 150;

	public void run() {
		setTitle("About");
		Dimension screenSize = getToolkit().getScreenSize();
		setPreferredSize(new Dimension(minframeX,minframeY));
		setLocation(((int)screenSize.getWidth() - minframeX) / 2,
				((int)screenSize.getHeight() - minframeY) / 2);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
		
		setLayout(new MigLayout("","grow","grow"));
		add(new JLabel("The famous game of Alemungula"), "wrap, grow");
		add(new JLabel("Author: Xarts"), "wrap, grow");
		add(new JLabel("Release date: 28.02.2011"));
		
	}
	
}
