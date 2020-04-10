
public class BoardNode implements Comparable <BoardNode>{

	int row;
	int col;
	private int numMineNbrs; //generated at start, private and hidden from agent
	int numMineNbrsDisplayed; //initially -1, then numMineNeighbors once uncovered
	int numMineNbrFlags; // number of neighbors agent marked as mines, initially 0
	boolean isMineFlagged; //whether or not the agent has flagged current node as mine
	boolean isCleared; //whether or not the agent has flagged the current node as clear
	int numClearedNbr; // number of neighbors marked as clear by agent 
	
	public BoardNode (int row, int col) {
		this.row = row;
		this.col = col; 
		this.numMineNbrsDisplayed = -1; 
		this.numMineNbrFlags = 0; 
		this.isMineFlagged = false; 
		this.isCleared = false; 
		this.numClearedNbr = 0;  
	}
	
	public BoardNode (int row, int col, int numMineNbrs, int numMineNbrsDisplayed, int numMineNbrFlags, 
			boolean isMineFlagged, boolean isCleared, int numClearedNbr) {
		this.row = row;
		this.col = col;
		this.numMineNbrs = numMineNbrs;
		this.numMineNbrsDisplayed = numMineNbrsDisplayed;
		this.numMineNbrFlags = numMineNbrFlags;
		this.isMineFlagged = isMineFlagged;
		this.isCleared = isCleared;
		this.numClearedNbr = numClearedNbr;
	}
	
	public void setNumMineNbrs (int numMineNbrs) {
		this.numMineNbrs = numMineNbrs;
	}
	
	public int getNumMineNbrs () {
		return numMineNbrs;
	}

	//Returns -1 if this < o, 1 if o < this, 0 otherwise
	public int compareTo(BoardNode o) {
		if (this.row < o.row) {
			return -1;
		}else if(o.row < this.row) {
			return 1;
		}else{
			if (this.col < o.col) {
				return -1;
			}else if(o.col < this.col) {
				return -1;
			}
		}
		return 0;
	}
		
}
