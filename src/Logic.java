import java.util.List;
import java.util.ArrayList;

public class Logic {
	BoardNode cell;
	ArrayList<BoardNode> unknownNbrs; //a list containing nbrs that haven't been flagged as mine or clear
	int minesLeft; //number of neighboring mines left to be determined
//	int isMine = 0; //initially set to 0. If isMine, set to 1
//	int numMineNbrsDisplayed; //initially -1, then numMineNeighbors once uncovered
//	int numMineNbrFlags; // number of neighbors agent marked as mines, initially 0
	
	public Logic (BoardNode cell, ArrayList<BoardNode> nbrs) {
		this.cell = cell;
		this.unknownNbrs = nbrs;
		this.minesLeft = cell.numMineNbrsDisplayed;
		
		for (int i = unknownNbrs.size() - 1; i >= 0; i--) {
			BoardNode nbr = unknownNbrs.get(i);
			if (nbr.isCleared || nbr.isMineFlagged) {
				unknownNbrs.remove(nbr);
				if (nbr.isMineFlagged) {
					minesLeft--;
				}
			}
		}
	}
	
	public void updateConstraint() {
		for (int i = unknownNbrs.size() - 1; i >= 0; i--) {
			BoardNode nbr = unknownNbrs.get(i);
			if (nbr.isCleared || nbr.isMineFlagged) {
				unknownNbrs.remove(nbr);
				if (nbr.isMineFlagged) {
					minesLeft--;
				}
			}
		}
	}

	public Logic(BoardNode cell, ArrayList<BoardNode> unknowns, int minesLeft) {
		this.cell = cell;
		this.unknownNbrs = unknowns;
		this.minesLeft = minesLeft;
	}
	
	public ArrayList<BoardNode> flagNbrs() {
		boolean allMines = false;
		ArrayList<BoardNode> newlyFlaggedNbrs = new ArrayList<BoardNode>();
		if (unknownNbrs.size() == minesLeft) allMines = true;
		for (int i = unknownNbrs.size() - 1; i >= 0; i--) {
			BoardNode unknownNbr = unknownNbrs.get(i);
			if (allMines) {
				unknownNbr.isMineFlagged = true;
				unknownNbr.isCleared = false;
				minesLeft--;
			} else {
				unknownNbr.isCleared = true;
				unknownNbr.isMineFlagged = false;
			}
			newlyFlaggedNbrs.add(unknownNbr);
			unknownNbrs.remove(i);
		}
		return newlyFlaggedNbrs;
	}
	
	public boolean simplify(BoardNode recentlyFlaggedNode) {
		for (int i = unknownNbrs.size() - 1; i >= 0; i--) {
			BoardNode unknownNbr = unknownNbrs.get(i);
			if (unknownNbr.row == recentlyFlaggedNode.row &&
					unknownNbr.col == recentlyFlaggedNode.col) {
				if (recentlyFlaggedNode.isMineFlagged && !recentlyFlaggedNode.isCleared) {
					minesLeft--;
				}
				unknownNbrs.remove(i);
				return true;
			}
		}
		return false;
	}
	
	// This method returns true if it is possible to solve the Logic instance based on the current
	// state of the equations that represent its value.
	public boolean isSolvable() {
		if (unknownNbrs.size() == minesLeft || minesLeft == 0) return true;
		return false;
	}
	
	// This method returns true if the current Logic instance has already been solved.
	public boolean isSolved() {
		if (unknownNbrs.size() == 0) return true;
		return false;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Logic) {
			Logic toCompare = (Logic) o;
			return (this.cell.row == toCompare.cell.row) && (this.cell.col == toCompare.cell.col);
		}
		return false;
	}
	
}

