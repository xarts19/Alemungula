/**
 * 
 */
package xarts.ai.lab3;

/** Data that is relevant to the board state and operations on it, 
 * such as moves (state changing) and its correctness, game over checks. 
 * @author xarts
 */
class GameState {
	/** Game state */
	private int ballsInPlayerStorage;
	private int ballsInCPUStorage;
	private int[] ballsInHole = {5,5,5,5,5,5,5,5,5,5};	//first 5 - player; rest - CPU
	
	/** Backup for current state */
	private int backupBallsInPlayerStorage;
	private int backupBallsInCPUStorage;
	private int[] backupBallsInHole = {5,5,5,5,5,5,5,5,5,5};
	
	/** String representation of a state*/
	private String strBallsInPlayerStorage = "0";
	private String strBallsInCPUStorage = "0";
	private String[] strBallsInHole = {"5","5","5","5","5","5","1","3","5","5"};
	
	/** Who's turn it is (true - player; false - CPU) */
	private boolean isPlayerTurn = true;
	
	private boolean isGameOver = false;
	
	public GameState() {
		ballsInPlayerStorage = backupBallsInPlayerStorage = 0;
		strBallsInPlayerStorage = "0";
		ballsInCPUStorage = backupBallsInCPUStorage = 0;
		strBallsInCPUStorage = "0";
		for (int i = 0; i < 10; i++) {
			ballsInHole[i] = backupBallsInHole[i] = 5;
			strBallsInHole[i] = "5";
		}
	/*	ballsInHole[7] = backupBallsInHole[7] = 3;
		strBallsInHole[7] = "3";
		ballsInHole[8] = backupBallsInHole[7] = 1;
		strBallsInHole[8] = "1";
	*/	
	}
	
	public int getValueInPlayerStorage() {
		return ballsInPlayerStorage;
	}
	
	public int getValueInCPUStorage() {
		return ballsInCPUStorage;
	}
	
	public int getValueInHole(int hole) {
		if (hole >= 0 && hole < 10)
			return ballsInHole[hole];
		return -1;
	}
	
	public boolean isGameOver() {
		return isGameOver;
	}
	
	public String getPlayerStorage() {
		return strBallsInPlayerStorage;
	}
	
	public String getCPUStorage() {
		return strBallsInCPUStorage;
	}
	
	public String getBallsInHole(int hole) {
		if (hole >= 0 && hole < 10)
			return strBallsInHole[hole];
		return "error";
	}
	
	/** Makes all arrangements for move and make visible for drawing function
	 * what will happen if move will be committed.
	 * @param hole hole to take balls from
	 * @param direction direction of balls spreading 
	 * (clockwise if left, counterclockwise if right)
	 * @return true if move is allowed
	 */
	public boolean setReadyToMove(int hole, String direction) {
		if (!isAllowedAction(hole, direction))
			return false;
		saveBackup();
		int currentHole = hole;
		int numSteps = ballsInHole[hole];
		ballsInHole[hole] = 0;
		strBallsInHole[hole] = "0";
		currentHole = spreadBalls(currentHole, numSteps, direction);
		checkForTakenBalls(hole, currentHole, numSteps, direction);
		return true;
	}
	
	/** Returns whether this action is allowed to do.
	 * @param hole
	 * @param direction
	 * @return
	 */
	private boolean isAllowedAction(int hole, String direction) {
		if (ballsInHole[hole] == 0) {
			return false;
		}
		if (direction != "left" && direction != "right") {
			throw new RuntimeException("direction != left and != right in GameState class");
		}
		if (checkForSingleton(hole)) {
			return false;
		}
		return true;
	}

	/** Checks for a singleton which is played into an empty hole of the opponent's side,
	 *  that is either the last or the first hole of the opponent's row). 
	 * @param hole 
	 * @return true if move is not allowed
	 */
	private boolean checkForSingleton(int hole) {
		if (hole == 0 && ballsInHole[0] == 1 && ballsInHole[9] == 0) return true;
		if (hole == 9 && ballsInHole[9] == 1 && ballsInHole[0] == 0) return true;
		if (hole == 4 && ballsInHole[4] == 1 && ballsInHole[5] == 0) return true;
		if (hole == 5 && ballsInHole[5] == 1 && ballsInHole[4] == 0) return true;
		return false;
	}

	/** Walks around the board in given direction and places one ball in each hole.
	 * @param currentHole hole to start from
	 * @param numSteps number of balls (and number of steps to take naturally)
	 * @param direction clockwise if left, counterclockwise if right
	 * @return number of hole in which last ball was placed
	 */
	private int spreadBalls(int currentHole, int numSteps, String direction) {
		int hole = currentHole;
		for (int i = 0; i < numSteps; i++) {
			if (direction == "left") hole--;
			else if (direction == "right") hole++;
			if (hole == -1) hole = 9;
			if (hole == 10) hole = 0;
			strBallsInHole[hole] = strBallsInHole[hole] + "+1";
			ballsInHole[hole]++;
		}
		return hole;
	}
	
	/** Walks back and check if some balls can be gather according to rules.
	 * @param startingHole hole the spreading started from
	 * @param currentHole hole to start from
	 * @param numSteps maximal number of steps to take
	 * @param direction the round will be taken inversely 
	 * to spreadBalls function if given same direction (as it should be)
	 */
	private void checkForTakenBalls(int startingHole, int currentHole, int numSteps, String direction) {
		int hole = currentHole;
		for (int i = 0; i < numSteps; i++) {
			if ((startingHole < 5 && hole < 5)
					|| (startingHole >= 5 && hole >= 5))
				break;
			if (ballsInHole[hole] == 2 || ballsInHole[hole] == 4) {
				strBallsInHole[hole] = strBallsInHole[hole] + "(*0)";
				if (startingHole >= 5) {
					ballsInCPUStorage += ballsInHole[hole];
					strBallsInCPUStorage = strBallsInCPUStorage + "(+" + ballsInHole[hole] + ")";
				} else {
					ballsInPlayerStorage += ballsInHole[hole];
					strBallsInPlayerStorage = strBallsInPlayerStorage + "(+" + ballsInHole[hole] + ")";
				}
				ballsInHole[hole] = 0;
			} else {
				break;
			}
			if (direction == "left") hole++;
			else if (direction == "right") hole--;
			if (hole == 10 || hole == -1) break;
		}
	}

	public void resetReadyToMove() {
		restoreFromBackup();
	}
	
	/** Commits move and make it visible for drawing function */
	public void makeMove() {
		for (int i = 0; i < 10; i++) {
			strBallsInHole[i] = String.valueOf(ballsInHole[i]);
		}
		strBallsInPlayerStorage = String.valueOf(ballsInPlayerStorage);
		strBallsInCPUStorage = String.valueOf(ballsInCPUStorage);
		isPlayerTurn = !isPlayerTurn;
		if (checkForGameOver()) {
			isGameOver = true;
		}
	}
	
	public boolean checkForGameOver() {
		if (allHolesEmpty(isPlayerTurn))
			return true;
		if (isPlayerTurn) {
			if ( !((ballsInHole[0] == 1 && ballsInHole[9] == 0) || ballsInHole[0] == 0))
				return false;
			for (int i = 1; i < 4; i++)
				if (ballsInHole[i] != 0)
					return false;
			if ( !((ballsInHole[4] == 1 && ballsInHole[5] == 0) || ballsInHole[4] == 0))
				return false;
		} else {
			if ( !((ballsInHole[5] == 1 && ballsInHole[4] == 0) || ballsInHole[5] == 0))
				return false;
			for (int i = 6; i < 9; i++)
				if (ballsInHole[i] != 0)
					return false;
			if ( !((ballsInHole[9] == 1 && ballsInHole[0] == 0) || ballsInHole[9] == 0))
				return false;
		}
		return true;
	}
	
	/** Checks if all holes of contestant is empty
	 * @param contestant true for player and false for CPU
	 * @return true if all holes of contestant is empty
	 */
	private boolean allHolesEmpty(boolean contestant) {
		if (contestant) {
			for (int i = 0; i < 5; i++)
				if (ballsInHole[i] != 0)
					return false;
		} else {
			for (int i = 5; i < 10; i++)
				if (ballsInHole[i] != 0)
					return false;
		}
		return true;
	}

	private void saveBackup() {
		backupBallsInPlayerStorage = ballsInPlayerStorage;
		backupBallsInCPUStorage = ballsInCPUStorage;
		for (int i = 0; i < 10; i++) {
			backupBallsInHole[i] = ballsInHole[i];
		}
	}
	
	private void restoreFromBackup() {
		ballsInPlayerStorage = backupBallsInPlayerStorage;
		ballsInCPUStorage = backupBallsInCPUStorage;
		for (int i = 0; i < 10; i++) {
			ballsInHole[i] = backupBallsInHole[i];
			strBallsInHole[i] = String.valueOf(ballsInHole[i]);
		}
		strBallsInPlayerStorage = String.valueOf(ballsInPlayerStorage);
		strBallsInCPUStorage = String.valueOf(ballsInCPUStorage);
	}

	/** Moves all balls to corresponding contestant's storage */
	public void transferEverythingToStorage() {
		for (int i = 0; i < 5; i++) {
			ballsInPlayerStorage += ballsInHole[i];
			ballsInHole[i] = 0;
			strBallsInHole[i] = "0";
			strBallsInPlayerStorage = String.valueOf(ballsInPlayerStorage);
		}
		for (int i = 5; i < 10; i++) {
			ballsInCPUStorage += ballsInHole[i];
			ballsInHole[i] = 0;
			strBallsInHole[i] = "0";
			strBallsInCPUStorage = String.valueOf(ballsInCPUStorage);
		}
	}

	/**
	 * @return winner of the game
	 */
	public String getWinner() {
		if (ballsInCPUStorage == ballsInPlayerStorage) 
			return "The game has ended in a draw. :|";
		else if (ballsInCPUStorage > ballsInPlayerStorage) 
			return "CPU has won the game. :(";
		else if (ballsInCPUStorage < ballsInPlayerStorage) 
			return "You won the game! :)";
		return null;
	}
	
}
