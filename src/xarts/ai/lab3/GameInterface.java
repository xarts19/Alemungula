/** This program represents the game of Alemungula.
 * This is a mancala game played by the Wetawit (formerly spelled Wetaweat) 
 * in Ethiopia, around the towns of Asosa and Beni Sangul, towards the Sudan border. 
 * Wetawit is an ethnic group in Ethiopia and Sudan. 
 * They speak Berta, a Nilo-Saharan language. The population of this group is about 248,000.
 * More info at http://mancala.wikia.com/wiki/Alemungula
 */

package xarts.ai.lab3;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import net.miginfocom.layout.CC;
import net.miginfocom.swing.MigLayout;

/** Implements interface for the game.
 * @author xarts
 */
@SuppressWarnings("serial")
public class GameInterface extends JFrame implements Runnable, ActionListener, ThreadCompleteListener {
	
	static enum Difficulty {
		ZERO, EASY, MEDIUM, HARD
	}
	
	/** Preferred window size */
	private static final int FRAME_HEIGHT = 450;
	private static final int FRAME_WIDTH = 1024;
	
	private static final int WAIT_TIME = 2000;
	
	ImagePanel graphics;
	
	/** Menu and its' items */
	private JMenuBar menuBar;
	private JMenu menu;
	private JMenuItem menuItem;
	private ButtonGroup difficultyButtonGroup;
	
	/** Control panel items */
	private JButton makeMoveButton;
	
	/** Status bar */
	private JLabel statusBar;
	
	/** Holes for both players */
	private ImagePanel[] Holes;
	private ImagePanel leftButton, rightButton;
	private ImagePanel playerStorage;
	private ImagePanel CPUStorage;
	private static final String IMAGE_HOLE_FILE = "resources/Hole.gif";
	private static final String IMAGE_STORAGE_FILE = "resources/Storage.gif";
	private static final String IMAGE_BACKGROUND = "resources/BG.gif";
	private static final String IMAGE_LEFT = "resources/Left.png";
	private static final String IMAGE_RIGHT = "resources/Right.png";
/*	private static final String IMAGE_BALL_1 = "resources/Ball1.gif";
	private static final String IMAGE_BALL_2 = "resources/Ball2.gif";
	private static final String IMAGE_BALL_3 = "resources/Ball3.gif";
	private static final String IMAGE_BALL_4 = "resources/Ball4.gif";
	private static final String IMAGE_BALL_5 = "resources/Ball5.gif";
	private static final String IMAGE_BALL_MANY = "resources/BallMany.gif";*/
	
	/** Player pressed button but not accepted move yet */
	private boolean isMakingMove = false;
	
	/** State of the game */
	private GameState game = new GameState();
	private Difficulty difficulty;
	private CPUAgent cpu = null;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GameInterface view = new GameInterface();
		SwingUtilities.invokeLater(view);
	}
	
	public GameInterface() {
		setLayout(new MigLayout());
		paintWindow();
		paintMenu();
		paintHoles();
		paintControls();
	}
	
	private void paintHoles() {
		graphics = new ImagePanel(IMAGE_BACKGROUND, new MigLayout("","[grow][grow][grow]","[grow]"));
		graphics.setEnabled(false);
		Holes = new ImagePanel[10];
		//Left storage-----------------------------------------
		CPUStorage = new ImagePanel(IMAGE_STORAGE_FILE);
		CPUStorage.setEnabled(false);
		CPUStorage.setLabel(game.getCPUStorage());
		graphics.add(CPUStorage, "w 15%");
		//Creating panel with holes-----------------------------
		JPanel CPUPanel = new JPanel(new MigLayout());
		CPUPanel.setOpaque(false);
		JPanel playerPanel = new JPanel(new MigLayout());
		playerPanel.setOpaque(false);
		for (int i = 0; i < 5; i++) {
			Holes[i] = new ImagePanel(IMAGE_HOLE_FILE, new MigLayout());
			Holes[i].setContentAreaFilled(false);
			Holes[i].setActionCommand("Player hole " + i);
			Holes[i].addActionListener(this);
			Holes[i].setLabel(game.getBallsInHole(i));
			playerPanel.add(Holes[i], "dock center");
		}
		for (int i = 9; i > 4; i--) {
			Holes[i] = new ImagePanel(IMAGE_HOLE_FILE, new MigLayout());
			Holes[i].setContentAreaFilled(false);
			Holes[i].setEnabled(false);
			Holes[i].setBorderPainted(false);
			Holes[i].setLabel(game.getBallsInHole(i));
			CPUPanel.add(Holes[i], "dock center");
		}
		//Buttons at the middle hole ---------------------------
		leftButton = new ImagePanel(IMAGE_LEFT);
		rightButton = new ImagePanel(IMAGE_RIGHT);
		leftButton.setContentAreaFilled(false);
		rightButton.setContentAreaFilled(false);
		leftButton.setActionCommand("Player button left");
		rightButton.setActionCommand("Player button right");
		leftButton.addActionListener(this);
		rightButton.addActionListener(this);
		leftButton.setVisible(false);
		rightButton.setVisible(false);
		Holes[2].setLayout(new MigLayout("insets 0", "[grow][][grow]","[][grow][]"));
		Holes[2].setMargin(new Insets(0,0,0,0));
		Holes[2].add(leftButton, new CC().cell(0,1));
		Holes[2].add(rightButton, new CC().cell(2,1).alignX("right"));
		//Adding panel with holes--------------------------------------
		JPanel panel = new JPanel(new MigLayout());
		panel.setOpaque(false);
		panel.add(CPUPanel, "dock center,  wrap");
		panel.add(playerPanel, "dock center");
		graphics.add(panel, "align center, w 70%");
		//Right storage-----------------------------------------------
		playerStorage = new ImagePanel(IMAGE_STORAGE_FILE);
		playerStorage.setEnabled(false);
		playerStorage.setLabel(game.getPlayerStorage());
		graphics.add(playerStorage, "w 15%");
		add(graphics, "wrap");
	}
	
	private void paintWindow() {
		setPreferredSize(new Dimension(FRAME_WIDTH,FRAME_HEIGHT));
		Dimension screenSize = getToolkit().getScreenSize();
		setLocation(((int)screenSize.getWidth() - FRAME_WIDTH) / 2,
				((int)screenSize.getHeight() - FRAME_HEIGHT) / 2);
	}
	
	private void paintMenu() {
		//Main bar ------------------------------
		menuBar = new JMenuBar();
		//First menu entry ------------------------------
		menu = new JMenu("Menu");
		menu.setMnemonic(KeyEvent.VK_M);
		menuBar.add(menu);
		//Start new game button ------------------------------
		menuItem = new JMenuItem("Start new game", KeyEvent.VK_S);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
		menuItem.addActionListener(this);
		menu.add(menuItem);
		//Difficulty selection --------------------------
		menu.addSeparator();
		
        difficultyButtonGroup = new ButtonGroup();
        JRadioButtonMenuItem rbMenuItem;
        rbMenuItem = new JRadioButtonMenuItem("Zero difficulty (random)");
        rbMenuItem.addActionListener(this);
        difficultyButtonGroup.add(rbMenuItem);
        menu.add(rbMenuItem);
        rbMenuItem = new JRadioButtonMenuItem("Easy difficulty");
        rbMenuItem.addActionListener(this);
        difficultyButtonGroup.add(rbMenuItem);
        menu.add(rbMenuItem);
        rbMenuItem = new JRadioButtonMenuItem("Medium difficulty");
        rbMenuItem.addActionListener(this);
        rbMenuItem.setSelected(true);
        difficulty = Difficulty.MEDIUM;
        difficultyButtonGroup.add(rbMenuItem);
        menu.add(rbMenuItem);
        rbMenuItem = new JRadioButtonMenuItem("Hard difficulty");
        rbMenuItem.addActionListener(this);
        difficultyButtonGroup.add(rbMenuItem);
        menu.add(rbMenuItem);
        
        menu.addSeparator();
        //Exit button -------------------------------
        menuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        menuItem.addActionListener(this);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
        menu.add(menuItem);
        
        //Second menu entry ------------------------------
		menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(menu);
		//Help button -------------------------------
        menuItem = new JMenuItem("Help", KeyEvent.VK_E);
        menuItem.addActionListener(this);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.ALT_MASK));
        menu.add(menuItem);
        //About button -------------------------------
        menuItem = new JMenuItem("About", KeyEvent.VK_A);
        menuItem.addActionListener(this);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
        menu.add(menuItem);
		
        //Add menu bar to window
        setJMenuBar(menuBar);
	}
	
	private void paintControls() {
		JPanel panel = new JPanel(new MigLayout("","[grow][][grow]",""));
		makeMoveButton = new JButton("Make move");
		makeMoveButton.addActionListener(this);
		makeMoveButton.setEnabled(false);
		panel.add(new JLabel("Opponent's storage"), new CC().height("15::").alignX("left"));
		panel.add(makeMoveButton, new CC().grow().alignX("center"));
		panel.add(new JLabel("Your storage"), new CC().height("15::").alignX("right"));
		add(panel, new CC().grow().wrap());
		statusBar = new JLabel("Ready");
		add(statusBar, new CC().height("15::"));
	}
	
		/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		for (int i = 0; i < 10; i++) {
			Holes[i].setEnabled(false);
			Holes[i].setBorderPainted(false);
			Holes[i].setLabel("");
		}
		CPUStorage.setLabel("");
		playerStorage.setLabel("");
		statusBar.setText("Press Alt-N to start new game");
		pack();
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setVisible(true);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Exit")) {
			dispose();
			System.exit(0);
		} else if (e.getActionCommand().equals("Start new game")) {
			initNewGame();
		} else if (e.getActionCommand().equals("Zero difficulty (random)")) {
			setDifficulty(Difficulty.ZERO);
		} else if (e.getActionCommand().equals("Easy difficulty")) {
			setDifficulty(Difficulty.EASY);
		} else if (e.getActionCommand().equals("Medium difficulty")) {
			setDifficulty(Difficulty.MEDIUM);
		} else if (e.getActionCommand().equals("Hard difficulty")) {
			setDifficulty(Difficulty.HARD);
		} else if (e.getActionCommand().equals("Help")) {
			Help help = new Help();
			SwingUtilities.invokeLater(help);
		} else if (e.getActionCommand().equals("About")) {
			About about = new About();
			SwingUtilities.invokeLater(about);
		} else if (e.getActionCommand().equals("Player hole 0")) {
			holePressed(0);
		} else if (e.getActionCommand().equals("Player hole 1")) {
			holePressed(1);
		} else if (e.getActionCommand().equals("Player hole 2")) {
			holePressed(2);
		} else if (e.getActionCommand().equals("Player button left")) {
			leftButton.setVisible(false);
			rightButton.setVisible(false);
			setReadyToMove(2,"left");
		} else if (e.getActionCommand().equals("Player button right")) {
			leftButton.setVisible(false);
			rightButton.setVisible(false);
			setReadyToMove(2,"right");
		} else if (e.getActionCommand().equals("Player hole 3")) {
			holePressed(3);
		} else if (e.getActionCommand().equals("Player hole 4")) {
			holePressed(4);
		} else if (e.getActionCommand().equals("Make move")) {
			makeMove();
		}
		
	}
	
	private void holePressed(int hole) {
		if (isMakingMove) {
			enableAllButtons();
			isMakingMove = false;
			resetReadyToMove();
			if (hole == 2) {
				leftButton.setVisible(false);
				rightButton.setVisible(false);
			}
		} else {
			disableOtherButtons(hole);
			isMakingMove = true;
			if (hole < 2)
				setReadyToMove(hole,"left");
			else if (hole > 2)
				setReadyToMove(hole,"right");
			else if (hole == 2) {
				leftButton.setVisible(true);
				rightButton.setVisible(true);
			}
		}
	}
	
	private void initNewGame() {
		game = new GameState();
		updateInterface();
		statusBar.setText("Press on any hole to make a move");
		cpu = new CPUAgent(difficulty, game);
	}
	
	private void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
		if (difficulty == Difficulty.ZERO) {
			statusBar.setText("Zero difficulty set (you need to start new game for changes to take effect)");
		} else if (difficulty == Difficulty.EASY) {
			statusBar.setText("Easy difficulty set (you need to start new game for changes to take effect)");
		} else if (difficulty == Difficulty.MEDIUM) {
			statusBar.setText("Medium difficulty set (you need to start new game for changes to take effect)");
		} else if (difficulty == Difficulty.HARD) {
			statusBar.setText("Hard difficulty set (you need to start new game for changes to take effect)");
		}
	}
	
	private void updateInterface() {
		for (int i = 0; i < 10; i++) {
			Holes[i].setLabel(game.getBallsInHole(i));
		}
		for (int i = 0; i < 5; i++) {
			if (!isMakingMove) {
				Holes[i].setEnabled(true);
				Holes[i].setBorderPainted(true);
			}
		}
		CPUStorage.setLabel(game.getCPUStorage());
		playerStorage.setLabel(game.getPlayerStorage());
	}
	
	private void disableOtherButtons(int exceptButton) {
		for (int i = 0; i < 5; i++) {
			if (i != exceptButton) {
				Holes[i].setEnabled(false);
				Holes[i].setBorderPainted(false);
			}
		}
	}
	
	private void enableAllButtons() {
		for (int i = 0; i < 5; i++) {
			Holes[i].setEnabled(true);
			Holes[i].setBorderPainted(true);
		}
	}
	
	/**
	 * @param hole
	 * @param direction clockwise if left, counterclockwise if right
	 */
	private void setReadyToMove(int hole, String direction) {
		if (game.setReadyToMove(hole, direction)) {
			updateInterface();
			makeMoveButton.setEnabled(true);
			statusBar.setText("Press \"Make move\" to commit a move");
		} else {
			enableAllButtons();
			isMakingMove = false;
			statusBar.setText("Not allowed!");
		}
	}
	
	private void resetReadyToMove() {
		makeMoveButton.setEnabled(false);
		isMakingMove = false;
		game.resetReadyToMove();
		updateInterface();
		statusBar.setText("Press on any hole to make a move");
	}
	
	private void makeMove() {
		enableAllButtons();
		isMakingMove = false;
		makeMoveButton.setEnabled(false);
		game.makeMove();
		updateInterface();
		if (game.isGameOver()) { 
			finishGame();
			return;
		}
		statusBar.setText("You made your move, now CPU will move");
		//--------------------------------
		disableOtherButtons(5);
		cpu.addListener(this);
		new Thread(cpu).start();
	}
	
	/* (non-Javadoc)
	 * @see xarts.ai.lab3.ThreadCompleteListener#notifyOfThreadComplete(java.lang.Runnable)
	 */
	/** Fires when CPU agent has made a move. */
	@Override
	public void notifyOfThreadComplete(Runnable thread) {
		updateInterface();
		disableOtherButtons(5);
		cpu.removeListener(this);
		try {
			Thread.sleep(WAIT_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		game.makeMove();
		updateInterface();
		enableAllButtons();
		if (game.isGameOver()) { 
			finishGame();
			return;
		}
		statusBar.setText("CPU made his move, now you can move again");
	}
	
	private void finishGame() {
		disableOtherButtons(5);
		game.transferEverythingToStorage();
		updateInterface();
		statusBar.setText("Game over. " + game.getWinner());
		graphics.setEnabled(false);
	}
	
	private class ImagePanel extends JButton {

		private Image img;
		private String label = "";
		private int fontSize = 24;
		private Color fontColor = Color.decode("0x009900");

		public ImagePanel(String img) {
			this(new ImageIcon(GameInterface.class.getResource(img)).getImage());
		}
		
		public ImagePanel(String img, LayoutManager lm) {
			this(new ImageIcon(GameInterface.class.getResource(img)).getImage());
			setLayout(lm);
		}

		public ImagePanel(Image img) {
			this.setOpaque(false);
			this.img = img;
			Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
			setPreferredSize(size);
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), null);
			if (label != "") {
				g.setFont(new Font("Arail",Font.ITALIC, fontSize));
				g.setColor(fontColor);
				g.drawString(label, this.getWidth()/2 - label.length()*14/2, this.getHeight()/2 + 8);
			}
		}
		
		public void setLabel(String label) {
			this.label = label;
			this.repaint();
		}
	}
	
}
