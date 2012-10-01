/** In this file the CPUAgent class is implemented
 * It uses Alpha-beta pruning algorithm for selecting
 * the move to make in the game.
 * Our general assumption is that our opponent plays perfectly.
 */
package xarts.ai.lab3;

import java.util.ArrayList;
import java.util.Random;

import xarts.ai.lab3.GameInterface.Difficulty;

/** Represents artificial intelligence for the game of Alemungula
 * @author xarts
 */
class CPUAgent extends NotifyingRunnable {
	
	static enum Turn {
		AGENT, OPPONENT
	}
	/** link to the only instance of this class */
	private static CPUAgent agent = null;
	
	/** depth of alpha-beta search that depends on difficulty */
	private static int maxDepth;
	
	/** How smart agent should be */
	private GameInterface.Difficulty difficulty;
	private GameState game;
	
	/** Creates new agent of given difficulty
	 * @param difficulty
	 * @param game link to game state
	 */
	public CPUAgent(GameInterface.Difficulty difficulty, GameState game) {
		this.difficulty = difficulty;
		this.game = game;
		setMaxDepth();
	}
	
	public static CPUAgent getAgent(GameInterface.Difficulty difficulty, GameState game) {
		if (agent != null) {
			agent.difficulty = difficulty;
			agent.game = game;
			agent.setMaxDepth();
			return agent;
		}
		return new CPUAgent(difficulty, game);
	}
	
	private void setMaxDepth() {
		if (difficulty == Difficulty.EASY)
			maxDepth = 2;
		else if (difficulty == Difficulty.MEDIUM)
			maxDepth = 6;
		else if (difficulty == Difficulty.HARD)
			maxDepth = 10;
	}
	
	public static int getMaxDepth() {
		return maxDepth;
	}
	
	/**
	 * 
	 */
	private void findBestMove() {
		GameStateSimple.Action action = alphaBetaSearch();
		String direction;
		if (action.isLeftDirection()) 
			direction = "left";
		else 
			direction = "right";
		game.setReadyToMove(action.getHole(), direction);
	}
	
	private GameStateSimple.Action alphaBetaSearch() {
		GameStateSimple state = new GameStateSimple(game);
		int best = Integer.MIN_VALUE;
		int numOfBest = 0;
		int alpha = Integer.MIN_VALUE;
		int beta =  Integer.MAX_VALUE;
		ArrayList<GameStateSimple> successors = getSuccessorsArray(state, Turn.AGENT);
		for (int i = 0; i < successors.size(); i++) {
			int min = minValue(successors.get(i), alpha, beta);
			if (min > best) {
				best = min;
				numOfBest = i;
			}
			alpha = max(alpha, best);
		}
		return successors.get(numOfBest).getAction();
	}
	
	/** Returns the action with maximal value among its descendants.
	 * This is a function that chooses an action for our agent (MAX player).
	 * Our goal is to maximize the result for this player. 
	 * @param state game state for which we seek the action with maximum value
	 * @param depth depth of current state in our search tree
	 * @param alpha best(maximal) value among all found values for player MAX (our agent)
	 * @param beta best(minimal) value among all found values for player MIN (agent's opponent)
	 * @return best value for agent (maximal value of utility)
	 */
	private int maxValue(GameStateSimple state, int alpha, int beta) {
		if (state.isTerminal())
			return state.getUtility();
		int best = Integer.MIN_VALUE;
		ArrayList<GameStateSimple> successors = getSuccessorsArray(state, Turn.AGENT);
		if (successors.isEmpty()) 
			return state.getUtility();
		for (int i = 0; i < successors.size(); i++) {
			best = max(best, minValue(successors.get(i), alpha, beta));
			if (best >= beta)
				return best;
			alpha = max(alpha, best);
		}
		return best;
	}
	
	/** Returns the action with minimal value among its descendants.
	 * This is a function that chooses an action for agent's opponent (MIN player).
	 * Opponents tries to minimize our result here.
	 * We make assumption that opponent plays perfectly. 
	 * @param state game state for which we seek the action with maximum value
	 * @param depth depth of current state in our search tree
	 * @param alpha best(maximal) value among all found values for player MAX (our agent)
	 * @param beta best(minimal) value among all found values for player MIN (agent's opponent)
	 * @return best value for agent (maximal value of utility)
	 */
	private int minValue(GameStateSimple state, int alpha, int beta) {
		if (state.isTerminal())
			return state.getUtility();
		int best = Integer.MAX_VALUE;
		ArrayList<GameStateSimple> successors = getSuccessorsArray(state, Turn.OPPONENT);
		if (successors.isEmpty()) 
			return state.getUtility();
		for (int i = 0; i < successors.size(); i++) {
			best = min(best, maxValue(successors.get(i), alpha, beta));
			if (best <= alpha)
				return best;
			beta = min(beta, best);
		}
		return best;
	}
	
	/** Finds all possible moves from this state and creates list of
	 * successors according to this actions.
	 * @param state state to generate successors from
	 * @return list of all possible successors
	 */
	private ArrayList<GameStateSimple> getSuccessorsArray(GameStateSimple state, Turn turn) {
		ArrayList<GameStateSimple> successors = new ArrayList<GameStateSimple>();
		for (GameStateSimple.Action action : GameStateSimple.Action.values()) {
			if (((turn == Turn.AGENT && action.getHole() >= 5)
					|| (turn == Turn.OPPONENT && action.getHole() < 5))
					&& state.isAllowedAction(action))
				successors.add(new GameStateSimple(state, action));
		}
		return successors;
	}

	private int max(int a, int b) {
		if (a > b) return a;
		return b;
	}
	
	private int min(int a, int b) {
		if (a < b) return a;
		return b;
	}
	
	private void makeMove() {
		if (difficulty == GameInterface.Difficulty.ZERO) {
			Random rnd = new Random();
			while (true) {
				int hole = rnd.nextInt(5) + 5;
				String direction = "";
				if (hole < 7) direction = "left";
				else if (hole > 7) direction = "right";
				else if (hole == 7) {
					if (rnd.nextBoolean()) direction = "left";
					else direction = "right";
				}
				if (game.setReadyToMove(hole, direction)) break;
			}
		//	game.makeMove();
		} else {
			findBestMove();
		}
	}

	/* (non-Javadoc)
	 * @see xarts.ai.lab3.NotifyingRunnable#doRun()
	 */
	@Override
	public void doRun() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}
		makeMove();
	}
	
}
