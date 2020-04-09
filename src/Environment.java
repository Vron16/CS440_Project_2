
public class Environment {
	private EnvironmentNode[][] board;
	
	public Environment(int dim, int numMines) {
		board = new EnvironmentNode[dim][dim];
		int minesPlaced = 0;
		while (minesPlaced < numMines) {
			minesPlaced = 0;
			for (int i = 0; i < dim; i++) {
				for (int j = 0; j < dim; j++) {
					int neighbors = getNeighborCount(dim, i, j);
					boolean isMine = false;
					if (minesPlaced < numMines) { //cell is eligible to be a mine
						double minePrb = (double)numMines/(double)(dim*dim);
						double random = Math.random();
						if (random < minePrb) { //some random number is less than the probability any given cell is a mine
							isMine = true;
							minesPlaced++;
						}
					}
					board[i][j] = new EnvironmentNode(i, j, isMine);
				}
			}
		}
		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) { //set number of mine neighbors
				if (board[i][j].getIsMine()) board[i][j].setClue(-1);
				else board[i][j].setClue(getMineNeighborCount(dim, i, j));
			}
		}
	}
	
	private int getNeighborCount(int dim, int row, int col) {
		int nCount = 0;
		if (row+1 < dim) nCount++;
		if (col+1 < dim) nCount++;
		if (col-1 > -1) nCount++;
		if (row-1 > -1) nCount++;
		if (row-1 > -1 && col-1 > -1) nCount++; 
		if (row-1 > -1 && col+1 < dim) nCount++;
		if (row+1 < dim && col-1 > -1) nCount++;
		if (row+1 < dim && col+1 < dim) nCount++;
		return nCount;
	}
	
	private int getMineNeighborCount(int dim, int row, int col) {
		int nCount = 0;
		if (row+1 < dim && board[row+1][col].getIsMine()) nCount++;
		if (col+1 < dim && board[row][col+1].getIsMine()) nCount++;
		if (col-1 > -1 && board[row][col-1].getIsMine()) nCount++;
		if (row-1 > -1 && board[row-1][col].getIsMine()) nCount++;
		if (row-1 > -1 && col-1 > -1 && board[row-1][col-1].getIsMine()) nCount++; 
		if (row-1 > -1 && col+1 < dim && board[row-1][col+1].getIsMine()) nCount++;
		if (row+1 < dim && col-1 > -1 && board[row+1][col-1].getIsMine()) nCount++;
		if (row+1 < dim && col+1 < dim && board[row+1][col+1].getIsMine()) nCount++;
		return nCount;
	}
	
	public String textGUI(int row, int col) {
		if (board[row][col].getIsMine()) return "exploded";
		else return String.valueOf(board[row][col].getClue());
	}
	
	public int getDim() {
		return board.length;
	}
	
	public int query(int row, int col) {
		return board[row][col].getClue();
	}
}
