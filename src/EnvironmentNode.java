
public class EnvironmentNode {
	private int row;
	private int col;
	private boolean isMine;
	private int clue; // -1 if it's a mine; initially -2; then properly set by the setClue method

	public EnvironmentNode(int row, int col, boolean isMine) {
		this.row = row;
		this.col = col;
		this.isMine = isMine;
		this.clue = -2;
	}
	
	public boolean getIsMine() {
		return isMine;
	}
	
	public void setClue(int clue) {
		this.clue = clue;
	}
	
	public int getClue() {
		return clue;
	}
	
	
}
