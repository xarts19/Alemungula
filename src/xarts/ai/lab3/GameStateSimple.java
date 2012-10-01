/**
 * 
 */
package xarts.ai.lab3;

/** Data that is relevant to the board state and operations on it, 
 * such as moves (state changing) and its correctness, game over checks. 
 * @author xarts
 */
class GameStateSimple {
	
	static enum Action {
		
		SPREAD_0_LEFT (0, 'l'),
		SPREAD_1_LEFT (1, 'l'),
		SPREAD_2_LEFT (2, 'l'),
		SPREAD_2_RIGHT(2, 'r'),
		SPREAD_3_RIGHT(3, 'r'),
		SPREAD_4_RIGHT(4, 'r'),
		SPREAD_5_LEFT (5, 'l'),
		SPREAD_6_LEFT (6, 'l'),
		SPREAD_7_LEFT (7, 'l'),
		SPREAD_7_RIGHT(7, 'r'),
		SPREAD_8_RIGHT(8, 'r'),
		SPREAD_9_RIGHT(9, 'r');
		
		private final int hole;
		private final char direction;		//'l' - left;   'r' - right
		
		private Action(int hole, char direction) {
			this.hole = hole;
			this.direction = direction;
		}
		
		public int getHole() {
			return hole;
		}
		
		public boolean isLeftDirection() {
			return direction == 'l';
		}
		
		public boolean isRightDirection() {
			return direction == 'r';
		}
		
		public char getDirection() {
			return direction;
		}

	}
	
	private static final int WEIGHT_OF_STORAGE = 2;
	private static final int WEIGHT_OF_HOLES = 1;
	
	/** Game state */
	private int ballsInPlayerStorage;
	private int ballsInAgentStorage;
	private int[] ballsInHole = {5,5,5,5,5,5,5,5,5,5};	//first 5 - player; rest - CPU
	private int depth;
	private Action action;
	
	/** Copy constructor from the actual game state
	 * @param state
	 */
	public GameStateSimple(GameState state) {
		ballsInPlayerStorage = state.getValueInPlayerStorage();
		ballsInAgentStorage = state.getValueInCPUStorage();
		for (int i = 0; i < 10; i++) {
			ballsInHole[i] = state.getValueInHole(i);
		}
		action = null;
		depth = 0;
	}
	
	/** Successors' constructor. It is important to check whether the action
	 * you give is allowed with isAllowedAction function.
	 * @param state
	 * @param action
	 */
	public GameStateSimple(GameStateSimple state, Action action) {
		depth += state.getDepth() + 1;
		this.action = action;
		//copy all from previous state
		ballsInPlayerStorage = state.getValueInPlayerStorage();
		ballsInAgentStorage = state.getValueInCPUStorage();
		for (int i = 0; i < 10; i++) {
			ballsInHole[i] = state.getValueInHole(i);
		}
		//add necessary changes
		int holeToTake = action.getHole();
		int numSteps = ballsInHole[holeToTake];
		ballsInHole[holeToTake] = 0;
		int currentHole = spreadBalls(numSteps, action);
		checkForTakenBalls(currentHole, numSteps, action);
	}
	
	/** Walks around the board in given direction and places one ball in each hole.
	 * @param currentHole hole to start from
	 * @param numSteps number of balls (and number of steps to take naturally)
	 * @param direction clockwise if left, counterclockwise if right
	 * @return number of hole in which last ball was placed
	 */
	private int spreadBalls(int numSteps, Action action) {
		int hole = action.getHole();
		for (int i = 0; i < numSteps; i++) {
			if (action.isLeftDirection()) 
				hole--;
			else hole++;
			if (hole == -1) hole = 9;
			if (hole == 10) hole = 0;
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
	private void checkForTakenBalls(int currentHole, int numSteps, Action action) {
		int hole = currentHole;
		for (int i = 0; i < numSteps; i++) {
			if ((action.getHole() < 5 && hole < 5)
					|| (action.getHole() >= 5 && hole >= 5))
				break;
			if (ballsInHole[hole] == 2 || ballsInHole[hole] == 4) {
				if (action.getHole() >= 5) {
					ballsInAgentStorage += ballsInHole[hole];
				} else {
					ballsInPlayerStorage += ballsInHole[hole];
				}
				ballsInHole[hole] = 0;
			} else {
				break;
			}
			if (action.isLeftDirection())
				hole++;
			else hole--;
			if (hole == 10 || hole == -1) break;
		}
	}
	
	/** Checks whether the action is allowed to do.
	 * @param action
	 * @return
	 */
	public boolean isAllowedAction(Action action) {
		if (ballsInHole[action.getHole()] == 0) {
			return false;
		}
		if (checkForSingleton(action.getHole())) {
			return false;
		}
		return true;
	}
	
	public Action getAction() {
		return action;
	}

	public int getValueInPlayerStorage() {
		return ballsInPlayerStorage;
	}
	
	public int getValueInCPUStorage() {
		return ballsInAgentStorage;
	}
	
	public int getValueInHole(int hole) {
		if (hole >= 0 && hole < 10)
			return ballsInHole[hole];
		return -1;
	}
	
	public int getDepth() {
		return depth;
	}
	
	/** Checks whether this state has to be the leaf of the tree 
	 * (have no descendants) and should not be branched.
	 * @return
	 */
	public boolean isTerminal() {
		if ((depth > CPUAgent.getMaxDepth() && !anyoneCanTakeSmth())
				|| depth > CPUAgent.getMaxDepth() * 1.5) return true;
		return false;
	}
	
	/** Check whether opponent can take some balls to his storage
	 * @return true if he can
	 */
	private boolean anyoneCanTakeSmth() {
		if ((depth & 1) == 1) {				//odd depth means player's turn
			for (int playerHole = 0; playerHole < 5; playerHole++) {
				if (playerHole <= 2) {
					int cpuHole = mod(playerHole - ballsInHole[playerHole], 10);
					if ((ballsInHole[cpuHole] == 1 || ballsInHole[cpuHole] == 3) 
							&& cpuHole >= 5)
						return true;
				}
				if (playerHole >= 2) {
					int cpuHole = mod(playerHole + ballsInHole[playerHole], 10);
					if ((ballsInHole[cpuHole] == 1 || ballsInHole[cpuHole] == 3)
							&& cpuHole >= 5)
						return true;
				}
			}
		} else {				//otherwise - agent's turn
			for (int cpuHole = 5; cpuHole < 10; cpuHole++) {
				if (cpuHole <= 7) {
					int playerHole = mod(cpuHole - ballsInHole[cpuHole], 10);
					if ((ballsInHole[playerHole] == 1 || ballsInHole[playerHole] == 3) 
							&& playerHole < 5)
						return true;
				}
				if (cpuHole >= 7) {
					int playerHole = mod(cpuHole + ballsInHole[cpuHole], 10);
					if ((ballsInHole[playerHole] == 1 || ballsInHole[playerHole] == 3)
							&& playerHole < 5)
						return true;
				}
			}
		}
		return false;
	}
	
	private int mod(int x, int y) {
		int result = x % y;
	    if (result < 0)
	        result += y;
	    return result;
	}

	/** Calculates the estimation of cpu gain if it will come to this state.
	 * (estimation of the chances for winning in this state)
	 * @return
	 */
	public int getUtility() {
		return (ballsInAgentStorage - ballsInPlayerStorage) * WEIGHT_OF_STORAGE
				+ (getTotalBallsInAllHoles(CPUAgent.Turn.AGENT)
					- getTotalBallsInAllHoles(CPUAgent.Turn.OPPONENT)) * WEIGHT_OF_HOLES;
	}
	
	/**
	 * @return
	 */
	private int getTotalBallsInAllHoles(CPUAgent.Turn turn) {
		int numBalls = 0;
		for (int i = 0; i < 10; i++) {
			if (turn == CPUAgent.Turn.AGENT && i >= 5)
				numBalls += ballsInHole[i];
			else if (turn == CPUAgent.Turn.OPPONENT && i < 5)
				numBalls += ballsInHole[i];
		}
		return numBalls;
	}

	/** Checks for a singleton which is played into an empty hole of the opponent's side,
	 *  that is either the last or the first hole of the opponent's row). 
	 * @param hole 
	 * @return true if move is not allowed
	 */
	private boolean checkForSingleton(int hole) {
		//TODO
		if (hole == 0 && ballsInHole[0] == 1 && ballsInHole[9] == 0) return true;
		if (hole == 9 && ballsInHole[9] == 1 && ballsInHole[0] == 0) return true;
		if (hole == 4 && ballsInHole[4] == 1 && ballsInHole[5] == 0) return true;
		if (hole == 5 && ballsInHole[5] == 1 && ballsInHole[4] == 0) return true;
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10; i++) {
			sb.append(" ["+i+"]:");
			sb.append(ballsInHole[i]);
		}
		sb.append(" StorPlayer:" + ballsInPlayerStorage);
		sb.append(" StorCPU:" + ballsInAgentStorage);
		sb.append("\n");
		return sb.toString();
	}

}
