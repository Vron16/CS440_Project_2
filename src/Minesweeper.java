import javax.swing.*;
import java.awt.*;
//import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
 
public class Minesweeper {
 
    public static BoardCellPanel[][] buildBoard(int dim) {
        JFrame board = new JFrame("Minesweeper Board with Dim = " + dim);
        board.setSize(500, 500);
        board.setLayout(new GridLayout(dim, dim));
        BoardCellPanel cells[][] = new BoardCellPanel[dim][dim];
       
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                cells[i][j] = new BoardCellPanel();
                cells[i][j].setBorder(BorderFactory.createLineBorder(Color.black));
                cells[i][j].setBackground(Color.LIGHT_GRAY);
                JLabel innerText = cells[i][j].getTextLabel();
                innerText.setText("?");
                board.add(cells[i][j]);
            }
        }
        board.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        board.setVisible(true);
        return cells;
    }
   
    public static void changeCell(BoardCellPanel cell, String newText) {
        JLabel innerText = cell.getTextLabel();
        if (newText.equals("M")) {
            innerText.setText(newText);
            cell.setBackground(Color.ORANGE);
        } else if (newText.equals("C")) {
            innerText.setText(newText);
            cell.setBackground(Color.GREEN);
        } else if (newText.equals("exploded")) {
            ImageIcon img = new ImageIcon("resources/mine.png");
            innerText.setIcon(img);
            cell.setBackground(Color.RED);
        }
        else {
            innerText.setText(newText);
            cell.setBackground(Color.WHITE);
        }
    }
       
    public static double playGame(Environment board, double totalMines) {
        // Will be incremented each time we query a mine and used to track score.
        double numErrors = 0;
        // BoardNode now is part of the agent's knowledge base. We create a replica
        // of the environment using BoardNodes as they will contain all fields to
        // track number of flagged neighbors and flag cells themselves.
        BoardNode[][] knowledgeBaseBoard = new BoardNode[board.getDim()][board.getDim()];
        for (int i = 0; i < board.getDim(); i++) {
            for (int j = 0; j < board.getDim(); j++) {
                knowledgeBaseBoard[i][j] = new BoardNode(i, j);
            }
        }
        // Will be used to come up with a play-by-play GUI
        //BoardCellPanel[][] knowledgeBaseGUI = buildBoard(board.getDim());
        ArrayList<BoardNode> availableCells = new ArrayList<BoardNode>(); // contains all available cells
        ArrayList<BoardNode> clearedCells = new ArrayList<BoardNode>(); // initially empty
       
        // initially empty, but essentially stores all previously queried safe cells for
        // whom not all neighbors are yet known
        ArrayList<Logic> constraints = new ArrayList<Logic>();
        // Populate availableCells with the entire knowledge base board initially.
        for (int i = 0; i < knowledgeBaseBoard.length; i++) {
            for (int j = 0; j < knowledgeBaseBoard.length; j++) {
                availableCells.add(knowledgeBaseBoard[i][j]);
            }
        }
        while (!availableCells.isEmpty()) {
            while (!clearedCells.isEmpty()) {
//                System.out.println("Press c to continue:");
//                Scanner reader = new Scanner(System.in);
//                String keepGoing = reader.nextLine();
                // Knowledge base cell of the cell we're about to query from the environment
                BoardNode kbQuery = clearedCells.remove(0);
                availableCells.remove(kbQuery);
                // Query the cell from the environment and get the clue
                int clue = board.query(kbQuery.row, kbQuery.col);
                numErrors += updateQuery(knowledgeBaseBoard, null, kbQuery, clue, constraints, false);
                // Queried cell cannot be a mine in here (already flagged as clear). If it has some neighbors that are
                // still unknown, we should represent it in our knowledge base.
                Logic constraint = new Logic(kbQuery, getNbrs(knowledgeBaseBoard, kbQuery));
                if (!constraint.isSolved()) {
                    constraints.add(constraint);
                    // We know it's not solved...so try to see if we can solve it
                    // and accordingly flag its neighbors!
                    updateKnowledgeBase(knowledgeBaseBoard, null, kbQuery, availableCells, clearedCells, constraint, constraints);
                }
            }
            if (!availableCells.isEmpty()) { // get random cell
//                System.out.println("Press c to continue:");
//                Scanner reader = new Scanner(System.in);
//                String keepGoing = reader.nextLine();
                Random randGenerator = new Random();
                BoardNode kbQuery = availableCells.remove(randGenerator.nextInt(availableCells.size()));
                int clue = board.query(kbQuery.row, kbQuery.col);
                //System.out.println("Clue: " + clue);
                numErrors += updateQuery(knowledgeBaseBoard, null, kbQuery, clue, constraints, true);
                // We only update the knowledge base if the clue is not -1 (not a mine) and
                // it still has some neighbors that are unknown.
                if (clue != -1) {
                    Logic constraint = new Logic(kbQuery, getNbrs(knowledgeBaseBoard, kbQuery));
                    if (!constraint.isSolved()) {
                        constraints.add(constraint);
                        // We know it's not solved...so try to see if we can solve it
                        // and accordingly flag its neighbors!
                        updateKnowledgeBase(knowledgeBaseBoard, null, kbQuery, availableCells, clearedCells, constraint, constraints);
                    }
                }
            }
        }
       
        return (totalMines - numErrors)/totalMines;
    }
   
    public static double playInferenceGame(Environment board, double totalMines) {
        double numErrors = 0;
        BoardNode[][] knowledgeBaseBoard = new BoardNode[board.getDim()][board.getDim()];
        for (int i = 0; i < board.getDim(); i++) {
            for (int j = 0; j < board.getDim(); j++) {
                knowledgeBaseBoard[i][j] = new BoardNode(i, j);
            }
        }
        BoardCellPanel[][] knowledgeBaseGUI = buildBoard(board.getDim());
        ArrayList <BoardNode> availableCells = new ArrayList <BoardNode>();
        ArrayList <BoardNode> clearedCells = new ArrayList<BoardNode>(); // initially empty
        ArrayList <Logic> constraints = new ArrayList<Logic>();
       
       
        //initially adds all the cells in the board to availableCells
        for (int i = 0; i < knowledgeBaseBoard.length; i++) {
            for (int j = 0; j < knowledgeBaseBoard[i].length; j++) {
                availableCells.add(knowledgeBaseBoard[i][j]);
            }
        }
        while (!availableCells.isEmpty()) {
        	//System.out.println("Inside and we have " + availableCells.size());
            if (!clearedCells.isEmpty()) {
                while (!clearedCells.isEmpty()) {
                    System.out.println("Press c to continue:");
                    Scanner reader = new Scanner(System.in);
                    String keepGoing = reader.nextLine();
                    // Knowledge base cell of the cell we're about to query from the environment
                    BoardNode kbQuery = clearedCells.remove(0);
                    availableCells.remove(kbQuery);
                    int clue = board.query(kbQuery.row, kbQuery.col);
                    //System.out.println("Formerly cleared clue for cell: (" + kbQuery.row + ", " + kbQuery.col + ") is " + clue);
                    numErrors += updateQuery(knowledgeBaseBoard, knowledgeBaseGUI, kbQuery, clue, constraints, false);
                    directInference(knowledgeBaseBoard, knowledgeBaseGUI, kbQuery, availableCells, clearedCells, constraints, false);
                }
                //constraintSatisfy
                // First, create a deep copy of the knowledgeBaseBoard
                //BoardNode[][] knowledgeBaseCopy = new BoardNode[knowledgeBaseBoard.length][knowledgeBaseBoard.length];
                //copyKnowledgeBase(knowledgeBaseCopy, knowledgeBaseBoard);
                // Then create deep copy of constraints. Each constraint's variables should
                // reference the BoardNode in constraintSatisfactionKB that corresponds to 
                // the BoardNode in knowledgeBaseBoard.
                //ArrayList<Logic> constraintsCopy = new ArrayList<Logic>();
                //copyConstraints(constraintsCopy, constraints, knowledgeBaseCopy);
                //ArrayList<BoardNode> variables = new ArrayList<BoardNode>();
                //getVariables(variables, constraintsCopy);
                //while (!variables.isEmpty()) {
                	// First, try flagging the variable as a mine and playing one turn with that.
                	// This involves calling updateQuery and directInference and then seeing whether
                	// any of the constraints are now equal to a number greater than the number of 
                	// variables (minesLeft > unknownNbrs.size() or minesLeft < 0).
                	// If that occurs, we have encountered a contradiction. We return that particular
                	// cell back, flag in the actual knowledge base, and loop to call direct inference
                //}
//                System.out.println("entering constraint satisfy");
//                ArrayList <BoardNode> variablesToTry = copyAvailable(availableCells); // consider deep copy
//                while (!variablesToTry.isEmpty()) {
//                    BoardNode flagNext = variablesToTry.get(0);
//                    variablesToTry.remove(0);
//                    BoardNode nextToClear = constraintSatisfaction(knowledgeBaseBoard, knowledgeBaseGUI, flagNext, constraints);
//                    if (nextToClear != null) {
//                    	if (nextToClear != flagNext) { //returned cell other than the original--board is solved
//                    		return (totalMines - numErrors)/(totalMines);
//                    	}
//                    	clearedCells.add(nextToClear);
//                        System.out.println("nextToClear: [" + nextToClear.row + "][" + nextToClear.col);
//                    } else {
//                    	System.out.println("null");
//                    }
//                }
            }
            else {
            	//System.out.println("querying random cell");
                System.out.println("Press c to continue:");
                Scanner reader = new Scanner(System.in);
                String keepGoing = reader.nextLine();
                Random randGenerator = new Random();
                BoardNode kbQuery = availableCells.remove(randGenerator.nextInt(availableCells.size()));
                int clue = board.query(kbQuery.row, kbQuery.col);
              //  System.out.println("Random clue for cell: (" + kbQuery.row + ", " + kbQuery.col + ") is " + clue);
                numErrors += updateQuery(knowledgeBaseBoard, knowledgeBaseGUI, kbQuery, clue, constraints, true);
                directInference(knowledgeBaseBoard, knowledgeBaseGUI, kbQuery, availableCells, clearedCells, constraints, true);
            }
        }
        return (totalMines - numErrors)/(totalMines);
    }
   
    public static void copyKnowledgeBase(BoardNode[][] knowledgeBaseCopy, BoardNode[][] knowledgeBaseBoard) {
    	for (int i = 0; i < knowledgeBaseBoard.length; i++) {
    		for (int j = 0; j < knowledgeBaseBoard.length; j++) {
    			BoardNode currentNode = knowledgeBaseBoard[i][j];
    			knowledgeBaseCopy[i][j] = new BoardNode(currentNode.row, currentNode.col);
    			knowledgeBaseCopy[i][j].isMineFlagged = currentNode.isMineFlagged;
    			knowledgeBaseCopy[i][j].isCleared = currentNode.isCleared;
    			knowledgeBaseCopy[i][j].numMineNbrsDisplayed = currentNode.numMineNbrsDisplayed;
    			// We aren't using the other two fields to my knowledge but I'm copying them anyway.
    			knowledgeBaseCopy[i][j].numMineNbrFlags = currentNode.numMineNbrFlags;
    			knowledgeBaseCopy[i][j].numClearedNbr = currentNode.numClearedNbr;
    		}
    	}
    }
    
    public static void getVariables(ArrayList<BoardNode> variables, ArrayList<Logic> constraints) {
    	for (Logic constraint : constraints) {
    		for (BoardNode variable : constraint.unknownNbrs) {
    			if (!variables.contains(variable)) variables.add(variable);
    		}
    	}
    }
    
    public static ArrayList<Logic> copyConstraints(ArrayList<Logic> list){
        ArrayList<Logic> copy = new ArrayList<Logic>();
        for (int i = 0; i < list.size(); i++) {
            BoardNode cellCpy = list.get(i).cell;
            ArrayList<BoardNode> unknownCpy = new ArrayList<BoardNode>();
            for (int j = 0; j < list.get(i).unknownNbrs.size(); j++) { //copy unknown nbrs array
                unknownCpy.add(list.get(i).unknownNbrs.get(j)); //cells should have same mem address, array that holds them should not
            }
            copy.add(new Logic(cellCpy, unknownCpy, list.get(i).minesLeft));
        }
        return copy;
    }
    
    public static void copyConstraints(ArrayList<Logic> constraintsCopy, ArrayList<Logic> constraints, BoardNode[][] knowledgeBaseCopy) {
    	for (int i = 0; i < constraints.size(); i++) {
    		Logic constraint = constraints.get(i);
    		BoardNode constraintCell = constraint.cell;
    		BoardNode constraintCellCopy = knowledgeBaseCopy[constraintCell.row][constraintCell.col];
    		ArrayList<BoardNode> unknownNbrs = constraint.unknownNbrs;
    		ArrayList<BoardNode> unknownNbrsCpy = new ArrayList<BoardNode>();
    		for (int j = 0; j < unknownNbrs.size(); j++) {
    			// Add the BoardNode in the copied knowledge base corresponding to the BoardNode
    			// in the original constraint's unknownNbrs list.
    			unknownNbrsCpy.add(knowledgeBaseCopy[unknownNbrs.get(j).row][unknownNbrs.get(j).col]);
    		}
    		constraintsCopy.add(new Logic(constraintCellCopy, unknownNbrsCpy, constraint.minesLeft));
    	}
    }
    
    public static ArrayList<BoardNode> copyAvailable(ArrayList<BoardNode>arr){
        ArrayList<BoardNode> copy = new ArrayList<BoardNode>();
        for (int i = 0; i < arr.size(); i++) {
            copy.add(arr.get(i));
        }
        return copy;
    }
   
    //before constraintSatisfaction is called: BoardNode flip = findMostFrequentBN (constraints); //mark flip as flag
    //if return null -> guess
   
    public static BoardNode constraintSatisfaction (BoardNode[][] knowledgeBaseBoard, BoardCellPanel[][] mineSweeperCells,
            BoardNode originalFlagNext, ArrayList<Logic> constraints) {
            ArrayList <BoardNode> newlyFlaggedNbrs = new ArrayList <BoardNode> ();
            ArrayList<BoardNode> modifiedCells = new ArrayList <BoardNode>();
            ArrayList<Logic> originalConstraints = copyConstraints(constraints);
            newlyFlaggedNbrs.add(originalFlagNext);
            while (!newlyFlaggedNbrs.isEmpty()) {
                BoardNode currentFlagNext = newlyFlaggedNbrs.get(0);
                newlyFlaggedNbrs.remove(0);
                for (int i = 0; i < constraints.size(); i++) {
                    Logic constraint = constraints.get(i);
                   // System.out.println("infinite logic constraints");
                    for (int j = 0; j < constraint.unknownNbrs.size(); j++) {
                        if (constraint.unknownNbrs.get(j) == currentFlagNext) {
                       // 	System.out.println("infinite constraint neighbors");
                            constraint.unknownNbrs.get(j).isMineFlagged = true;
                            constraint.unknownNbrs.remove(j);
                            constraint.minesLeft--;
                            if (constraint.unknownNbrs.size() < constraint.minesLeft) { //contradiction
                                restoreOriginalContext(knowledgeBaseBoard, modifiedCells, constraints, originalConstraints); //restores context before constraint satisfaction
                                originalFlagNext.isMineFlagged = !originalFlagNext.isMineFlagged;
                                originalFlagNext.isCleared = !originalFlagNext.isCleared;
                                return originalFlagNext;
                            }
                            else {
                                if (constraint.isSolvable()) {
                                	ArrayList<BoardNode> temp = constraint.flagNbrs();
                                    newlyFlaggedNbrs.addAll(temp);
                                    modifiedCells.addAll(temp);
                                    constraints.remove(constraint);
                                }
                                boolean solved = true;
                                if (constraints.isEmpty()) { //board might be solved
                                	for (int r = 0; r < knowledgeBaseBoard.length; r++) {
                                		for (int s = 0; s < knowledgeBaseBoard.length; s++) {
                                			if (!knowledgeBaseBoard[r][s].isMineFlagged && !knowledgeBaseBoard[r][s].isCleared) { //cell is not solved
                                				solved = false;
                                				break;
                                			}
                                		}
                                	}
                                	
                                	if (solved) { //return some other random cell
                                		int rRow = originalFlagNext.row+1;
                                		if (rRow >= knowledgeBaseBoard.length) { //row index is out of bounds
                                			rRow = originalFlagNext.row-1;
                                		}
                                		return knowledgeBaseBoard[rRow][0];
                                	}
                                }
                                break;
                            }
                        }
                    }
                }
            }
            originalFlagNext.isMineFlagged = false;
            originalFlagNext.isCleared = false;
            restoreOriginalContext(knowledgeBaseBoard, modifiedCells, constraints, originalConstraints); //restores context before constraint satisfaction
            return null;
        }
     
    public static void restoreOriginalContext(BoardNode[][] wrongKB, ArrayList<BoardNode> modified,
            ArrayList<Logic> wrongConstraints, ArrayList<Logic> originalConstraints) {
        while (!modified.isEmpty()) {
            BoardNode mod = modified.remove(0);
            mod.isMineFlagged = false;
            mod.isCleared = false;
        }
        wrongConstraints = originalConstraints;
    }
    
    public static double updateQuery(BoardNode [][] boardKnowledgeBase,
            BoardCellPanel[][] mineSweeperCells, BoardNode kbQuery, int clue,
            ArrayList<Logic> constraints, boolean isQueryRandom) {
        /*try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        double numErrors = 0;
        boolean clickedOnMine = false;
        if (clue == -1) { //agent clicked on a mine
            numErrors++;
            boardKnowledgeBase[kbQuery.row][kbQuery.col].isMineFlagged = true;
            boardKnowledgeBase[kbQuery.row][kbQuery.col].isCleared = false;
            clickedOnMine = true;
            changeCell(mineSweeperCells[kbQuery.row][kbQuery.col], "exploded");
        } else {
            boardKnowledgeBase[kbQuery.row][kbQuery.col].numMineNbrsDisplayed = clue;
            boardKnowledgeBase[kbQuery.row][kbQuery.col].isCleared = true;
            changeCell(mineSweeperCells[kbQuery.row][kbQuery.col], String.valueOf(boardKnowledgeBase[kbQuery.row][kbQuery.col].numMineNbrsDisplayed));
        }
        // Brute force impl. for now...go through all constraints and update them
        // now that this cell has been flagged
        for (int i = 0; i < constraints.size(); i++) {
            Logic currentConstraint = constraints.get(i);
            currentConstraint.updateConstraint();
        }
        //updateQueriedNbrs(boardKnowledgeBase, kbQuery, clickedOnMine, isQueryRandom);
        return numErrors;
    }
   
    //flags cells as mine or cleared based on queried cell clue
    public static ArrayList<BoardNode> updateKnowledgeBase (BoardNode [][] knowledgeBaseBoard, BoardCellPanel[][] mineSweeperCells,
            BoardNode kbQuery, ArrayList<BoardNode> availableCells, ArrayList<BoardNode> clearedCells,
            Logic constraint, ArrayList<Logic> constraints) {
        ArrayList<BoardNode> newlyFlagged = new ArrayList<BoardNode>();
        //flag hidden neighbors as mine
        if (constraint.isSolvable()) {
            newlyFlagged = flagAllNeighbors(knowledgeBaseBoard, mineSweeperCells, kbQuery, availableCells, clearedCells, constraint, constraints);
        }
        return newlyFlagged;
        //implement method to update the nbrs of query cell (decrement hidden nbrs and increment nbrs flagged)
    }
    
    public static void solveSingleConstraints(BoardNode[][] knowledgeBaseBoard, BoardCellPanel[][] mineSweeperCells,
    		ArrayList<BoardNode> availableCells, ArrayList<BoardNode> clearedCells, ArrayList<Logic> constraints) {
    	// Now, loop through all constraints and solve all solvable ones by calling
        // updateKnowledgeBase for all of them. If any call to updateKnowledgeBase
        // results in a solved constraint, it necessarily means that some other cells
        // were flagged and that constraints may have been updated, so we set steadyState
        // to false. It terminates only when we pass through all constraints and
        // none of the constraints can be solved.
        boolean steadyState = false;
        while (!steadyState) {
            steadyState = true;
            for (int i = constraints.size() - 1; i >= 0; i--) {
                Logic currentConstraint = constraints.get(i);
                BoardNode constraintCell = currentConstraint.cell;
                ArrayList<BoardNode> newlyFlagged = updateKnowledgeBase(knowledgeBaseBoard, mineSweeperCells, constraintCell, availableCells, clearedCells, currentConstraint, constraints);
                if (newlyFlagged.size() > 0) steadyState = false;
            }
        }
    }
    
    public static void updateAllConstraints(ArrayList<Logic> constraints) {
    	// Now, go through and update the rest of the constraints based on the current
        // state of the knowledge base board. The basic agent won't ever go back to
        // these but the direct inference one will.
        // Brute force impl. for now...go through all constraints and update them
        // now that this cell has been flagged
    	// After the constraints are updated, if they are in a "solved" state, remove them
    	// from the list of constraints.
        for (int i = 0; i < constraints.size(); i++) {
            Logic currentConstraint = constraints.get(i);
            currentConstraint.updateConstraint();
        }
        for (int i = constraints.size() - 1; i >= 0; i--) {
        	if (constraints.get(i).isSolved()) constraints.remove(i);
        }
    }
    
    public static boolean solveSystemConstraints(BoardCellPanel[][] mineSweeperCells, ArrayList<BoardNode> availableCells, ArrayList<BoardNode> clearedCells, ArrayList<Logic> constraints) {
    	 // Now, sort the list of constraints in descending order by the number of mines they have left.
        constraints.sort(new Comparator<Logic>() {
        	public int compare(Logic constraintOne, Logic constraintTwo) {
        		int minesLeftComp = Integer.compare(constraintTwo.minesLeft, constraintOne.minesLeft);
        		if (minesLeftComp == 0) {
        			return Integer.compare(constraintTwo.unknownNbrs.size(), constraintOne.unknownNbrs.size());
        		}
        		return minesLeftComp;
        	}
        });
        boolean isFlagged = false;
        for (int i = 0; i < constraints.size(); i++) {
        	for (int j = i + 1; j < constraints.size(); j++) {
        		// Write method to find how many variables in constraintTwo are in constraintOne
        		// Write method to find how many variables in constraintOne are in constraintTwo
        		Logic constraintOne = constraints.get(i);
        		Logic constraintTwo = constraints.get(j);
        		
        		int minesLeftDiff = constraintOne.minesLeft - constraintTwo.minesLeft;
        		ArrayList<BoardNode> varsNotInTwo = diffVariables(constraintOne, constraintTwo);
            	ArrayList<BoardNode> varsNotInOne = diffVariables(constraintTwo, constraintOne);
        		// Case 1: If there is one variable in the first constraint that is not in the
            	// second constraint, one variable in the second constraint that is not in the 
            	// first constraint, and minesLeftDiff is 1, then we can conclude that the variable
            	// in the first constraint is 1 and the variable in the second constraint is 0.
            	if (varsNotInTwo.size() == 1 && varsNotInOne.size() == 1 && minesLeftDiff == 1) {
            		isFlagged = true;
            		BoardNode mineVariable = varsNotInTwo.get(0);
            		BoardNode safeVariable = varsNotInOne.get(0);
            		mineVariable.isMineFlagged = true;
            		mineVariable.isCleared = false;
            		availableCells.remove(mineVariable);
            		safeVariable.isMineFlagged = false;
            		safeVariable.isCleared = true;
            		clearedCells.add(safeVariable);
            		changeCell(mineSweeperCells[mineVariable.row][mineVariable.col], "M");
            		changeCell(mineSweeperCells[safeVariable.row][safeVariable.col], "C");
            		break;
            	} else if (varsNotInOne.size() == 0) {
            		// Cases 2 and 3 occur if the variables of the second constraint equation
            		// are a strict subset of the variables of the first constraint equation.
            		// If there is only one variable in the first constraint equation that's
            		// not in the second, then that variable is a mine if minesLeftDiff is 1
            		// and clear if minesLeftDiff is 0. If there is more than 1 variable, then
            		// we cannot solve it, but we can simplify the first constraint to only include
            		// the the contents of varNotInTwo and set its minesLeft to minesLeftDiff
            		if (varsNotInTwo.size() == 1) {
            			BoardNode flagVariable = varsNotInTwo.get(0);
            			if (minesLeftDiff == 1) {
            				isFlagged = true;
            				flagVariable.isMineFlagged = true;
            				flagVariable.isCleared = false;
            				availableCells.remove(flagVariable);
            				changeCell(mineSweeperCells[flagVariable.row][flagVariable.col], "M");
            				break;
            			} else if (minesLeftDiff == 0) {
            				isFlagged = true;
            				flagVariable.isMineFlagged = false;
            				flagVariable.isCleared = true;
            				clearedCells.add(flagVariable);
            				changeCell(mineSweeperCells[flagVariable.row][flagVariable.col], "C");
            				break;
            			} else {
            				System.out.println("Contradiction in direct inference system of equations.");
            			}
            		} else {
            			// Manually update constraintOne's unknownNbrs to have varsNotInTwo
            			// and minesLeft to have minesLeftDiff
            			constraintOne.unknownNbrs = new ArrayList<BoardNode>(varsNotInTwo);
            			constraintOne.minesLeft = minesLeftDiff;
            			// Since this constraint might now be out of order in the list, resort it
            			// and restart the loops
            			constraints.sort(new Comparator<Logic>() {
            	        	public int compare(Logic constraintOne, Logic constraintTwo) {
            	        		int minesLeftComp = Integer.compare(constraintTwo.minesLeft, constraintOne.minesLeft);
            	        		if (minesLeftComp == 0) {
            	        			return Integer.compare(constraintTwo.unknownNbrs.size(), constraintOne.unknownNbrs.size());
            	        		}
            	        		return minesLeftComp;
            	        	}
            	        });
            			//System.out.println("Restarting system of equations solver...");
            			//i = -1;
            			isFlagged = true;
            			break;
            		}
            	}
        	}
        	if (isFlagged) break;
        }
        return isFlagged;
    }
    
    public static void removeDuplicates(ArrayList<Logic> constraints) {
    	boolean[] flagForDeletion = new boolean[constraints.size()];
    	
    	for (int i = 0; i < constraints.size(); i++) {
    		for (int j = i + 1; j < constraints.size(); j++) {
    			Logic constraintOne = constraints.get(i);
        		Logic constraintTwo = constraints.get(j);
        		
        		int minesLeftDiff = constraintOne.minesLeft - constraintTwo.minesLeft;
        		ArrayList<BoardNode> varsNotInTwo = diffVariables(constraintOne, constraintTwo);
            	ArrayList<BoardNode> varsNotInOne = diffVariables(constraintTwo, constraintOne);
            	if (varsNotInTwo.size() == 0 && varsNotInOne.size() == 0) {
            		// There are no variables in constraint one that aren't in constraint two 
            		// and vice versa.
            		if (minesLeftDiff == 0) {
            			// This is what should happen, we have two identical constraints.
            			// Flag first one for deletion.
            			flagForDeletion[i] = true;
            		} else {
            			System.out.println("Something went wrong: Two identical constraints are equal to different values.");
            		}
            	}
    			
    		}
    	}
    	// Now, remove any constraints that were flagged for deletion.
    	for (int i = constraints.size() - 1; i >= 0; i--) {
    		if (flagForDeletion[i]) constraints.remove(i);
    	}
    }
    
    // This method simulates subtraction by identifying all variables that are in the
    // first constraint that are not in the second equation. Assuming that all variables
    // in the equations have coefficient one, this represents the variables in the first
    // constraint that won't be cancelled out via subtraction.
    public static ArrayList<BoardNode> diffVariables (Logic constraintOne, Logic constraintTwo) {
    	// Determine variables in constraint one NOT in constraint two.
    	ArrayList<BoardNode> constraintOneVariables = constraintOne.unknownNbrs;
    	ArrayList<BoardNode> constraintTwoVariables = constraintTwo.unknownNbrs;
    	
    	ArrayList<BoardNode> diffVariables = new ArrayList<BoardNode>();
    	for (BoardNode variable : constraintOneVariables) {
    		if (!constraintTwoVariables.contains(variable)) {
    			diffVariables.add(variable);
    		}
    	}
    	return diffVariables;
    }
    
    public static void printConstraints (ArrayList<Logic> constraints) {
    	for (Logic constraint : constraints) {
    		StringBuilder equation = new StringBuilder();
    		for (BoardNode variable : constraint.unknownNbrs) {
    			equation.append("(" + variable.row + ", " + variable.col + ")" + " + ");
    		}
    		equation.delete(equation.length() - 3, equation.length());
    		equation.append(" = " + constraint.minesLeft);
    		System.out.println(equation);
    	}
    }
    
    public static void directInference (BoardNode[][] knowledgeBaseBoard, BoardCellPanel[][] mineSweeperCells,
            BoardNode kbQuery, ArrayList<BoardNode> availableCells, ArrayList<BoardNode> clearedCells,
            ArrayList<Logic> constraints, boolean isQueryRandom) {
        // while logic is still solvable in knowledge base, solve
        // when we get something solvable, solve it, then restart through knowledge base
        // Stores recently flagged BoardNodes (initially queried node goes in here
        // if it was random)
        // First, add a new constraint for the queried cell if it's not a mine and
        // cannot be solved. UpdateQuery already called in playInferenceGame, so all
        // constraints and knowledgeBaseBoard already reflect kbQuery as a known cell.
        // Attempt to solve the constraint for the current cell, flagging its neighbors
        // and updating the constraints as necessary via updateKnowledgeBase.
        if (kbQuery.numMineNbrsDisplayed != -1) {
            Logic constraint = new Logic(kbQuery, getNbrs(knowledgeBaseBoard, kbQuery));
            if (!constraint.isSolved()) {
                constraints.add(constraint);
                // We know it's not solved...so try to see if we can solve it
                // and accordingly flag its neighbors!
                updateKnowledgeBase(knowledgeBaseBoard, mineSweeperCells, kbQuery, availableCells, clearedCells, constraint, constraints);
            }
        }
        // Here, we first infer by solving cells based on each logic constraint independently
        // and then in combinations. The first method call solves as many cells as it can through
        // independently solving constraints. The second method solves as many cells as it can
        // by using pairs of clues at a time. If that results in some cell/variables being solved,
        // all the constraints are updated again to reflect this. As a result, some constraints
        // may once again be solvable, causing the loop to repeat. Eventually, we will hit a 
        // state when we cannot solve anything through the system of equations, which will let us
        // leave the loop having inferred all that we possibly can.
        boolean areVariablesSolved = true;
        while (areVariablesSolved) {
        	// Following method loops through all constraints and solves as many as it can
            // one at a time (i.e., equations that can be solved without using a system; cells
            // that can be flagged by using info from just one clue).
            solveSingleConstraints(knowledgeBaseBoard, mineSweeperCells, availableCells, clearedCells, constraints);
            // checkForContradictions()
            removeDuplicates(constraints);
            System.out.println("After inferring based on single constraints: ");
            printConstraints(constraints);
            System.out.println("Finished inferring based on single constraints. Press c to infer based on systems of constraints: ");
            Scanner reader = new Scanner(System.in);
            String keepGoing = reader.nextLine();
            // Now that everything has been inferred using all uncovered clues independently, we
            // use the following method to sort all the constraints based on the value of the 
            // right-hand side of the equation in descending order (minesLeft). Then, we take
            // all possible pairs of constraint equations and attempt to solve for some variables or simplify the
            // equation. When that is done, we have to update all the constraints IF some variables
            // were solved by the system of equations solver. 
            areVariablesSolved = solveSystemConstraints(mineSweeperCells, availableCells, clearedCells, constraints);
            // checkforContradiction()
            
            if (areVariablesSolved) {
            	// If we were able to simplify the constraints somewhat by the system solver 
            	// (we either flagged a variable or rewrote an equation with fewer variables)
            	// we should pass through all constraints and remove any that are now duplicates.
            	// Then, we should update all constraints and remove any that are now in a solved
            	// state (i.e., have no variables left).
            	removeDuplicates(constraints);
            	updateAllConstraints(constraints);
            	// Remove any duplicate constraints that may have arisen by updating constraints.
            	removeDuplicates(constraints);
            	
            }
            System.out.println("After inferring based on system of contraints.");
            printConstraints(constraints);
        }
    }
 
    public static ArrayList<BoardNode> flagAllNeighbors (BoardNode[][] boardKnowledgeBase, BoardCellPanel[][] mineSweeperCells,
            BoardNode kbQuery, ArrayList<BoardNode> availableCells, ArrayList<BoardNode> clearedCells,
            Logic constraint, ArrayList<Logic> constraints) {
//      try {
//          TimeUnit.SECONDS.sleep(1);
//      } catch (InterruptedException e) {
//          e.printStackTrace();
//      }
        ArrayList<BoardNode> newlyFlaggedNbrs = constraint.flagNbrs();
        for (int i = 0; i < newlyFlaggedNbrs.size(); i++) {
            BoardNode newlyFlaggedNbr = newlyFlaggedNbrs.get(i);
            boolean flag = false;
            if (newlyFlaggedNbr.isMineFlagged) {
                flag = true;
                changeCell(mineSweeperCells[newlyFlaggedNbr.row][newlyFlaggedNbr.col], "M");
                availableCells.remove(newlyFlaggedNbr);
            } else {
                changeCell(mineSweeperCells[newlyFlaggedNbr.row][newlyFlaggedNbr.col], "C");
                clearedCells.add(newlyFlaggedNbr);
            }
            //updateFlaggedNbrs(boardKnowledgeBase, newlyFlaggedNbr, flag);
        }
        // We have solved the constrained cell (flagged all neighbors around it), so we can
        // remove it from our ArrayList of constraints.
        constraints.remove(constraint);
        // Now, go through and update the rest of the constraints based on the current
        // state of the knowledge base board. The basic agent won't ever go back to
        // these but the direct inference one will.
        // Brute force impl. for now...go through all constraints and update them
        // now that this cell has been flagged
        for (int i = 0; i < constraints.size(); i++) {
            Logic currentConstraint = constraints.get(i);
            currentConstraint.updateConstraint();
        }
        //removeDuplicates(constraints);
        /*ArrayList<BoardNode> nbrs = getNbrs(board, cell);
        ArrayList<BoardNode> newlyFlaggedNbrs = new ArrayList <BoardNode> ();
        while (!nbrs.isEmpty()) {
            BoardNode nbr = nbrs.remove(0);
            if (nbr.numMineNbrsDisplayed == -1 && !nbr.isMineFlagged && !nbr.isCleared) {
                System.out.println("cell about to be flagged: [" + nbr.row +"][" + nbr.col +"]");
                if (flag) {
                    board[nbr.row][nbr.col].isMineFlagged = true;
                    changeCell(mineSweeperCells[nbr.row][nbr.col], "M");
                    availableCells.remove(board[nbr.row][nbr.col]);
                    newlyFlaggedNbrs.add(board[nbr.row][nbr.col]);
                }
                else {
                    board[nbr.row][nbr.col].isCleared = true;
                    changeCell(mineSweeperCells[nbr.row][nbr.col], "C");
                    clearedCells.add(board[nbr.row][nbr.col]);
                    newlyFlaggedNbrs.add(board[nbr.row][nbr.col]);
                }
                updateFlaggedNbrs (board, nbr, flag);
            }
        }*/
//      System.out.print("newlyFlaggedNbrs list for [" + cell.row + "][" + cell.col + "]: ");
//      for (int i = 0; i < newlyFlaggedNbrs.size(); i++) {
//          BoardNode nbr = newlyFlaggedNbrs.get(i);
//          System.out.print("[" + nbr.row + "][" + nbr.col + "] ");
//      }
//      System.out.println();
        return newlyFlaggedNbrs;
    }
   
    //updating #flagged or #cleared nbrs for the neighbors of queried cell
    public static void updateQueriedNbrs (BoardNode[][] knowledgeBaseBoard, BoardNode kbQuery,
            boolean isMineClicked, boolean isQueryRandom) {
       
        List<BoardNode> nbrs = getNbrs(knowledgeBaseBoard, kbQuery);
        while (!nbrs.isEmpty()) {
            BoardNode nbr = nbrs.remove(0);
            if (isMineClicked) knowledgeBaseBoard[nbr.row][nbr.col].numMineNbrFlags++;
            else if (isQueryRandom) knowledgeBaseBoard[nbr.row][nbr.col].numClearedNbr++;
        }
       
    }
   
    //helper method to return all the nbrs of a cell in the form of an ArrayList
    public static ArrayList<BoardNode> getNbrs (BoardNode[][] board, BoardNode cell) {
        ArrayList <BoardNode> nbrs = new ArrayList<BoardNode> ();
        int row = cell.row;
        int col = cell.col;
        int dim = board.length;
        if (row+1 < dim) nbrs.add(board[row+1][col]);
        if (col+1 < dim) nbrs.add(board[row][col+1]);
        if (col-1 > -1) nbrs.add(board[row][col-1]);
        if (row-1 > -1) nbrs.add(board[row-1][col]);
        if (row-1 > -1 && col-1 > -1) nbrs.add(board[row-1][col-1]);
        if (row-1 > -1 && col+1 < dim) nbrs.add(board[row-1][col+1]);
        if (row+1 < dim && col-1 > -1) nbrs.add(board[row+1][col-1]);
        if (row+1 < dim && col+1 < dim) nbrs.add(board[row+1][col+1]);
        return nbrs;
    }
    //update the #clear or #flag of neighbors of the neighbors of the queried cell
    //update nbrs of nbrs
    public static void updateFlaggedNbrs (BoardNode[][] knowledgeBaseBoard, BoardNode neighbor, boolean isFlagged) {
        List<BoardNode> nbrs = getNbrs(knowledgeBaseBoard, neighbor);
        while (!nbrs.isEmpty()) {
            BoardNode nbr = nbrs.remove(0);
            if (isFlagged) knowledgeBaseBoard[nbr.row][nbr.col].numMineNbrFlags++;
            else knowledgeBaseBoard[nbr.row][nbr.col].numClearedNbr++;
//          System.out.println("For ["+ nbr.row + "][" + nbr.col +"]: "
//          + "numMineNbrFlags=" + nbr.numMineNbrFlags + ", " + "numClearedNbr=" + nbr.numClearedNbr);
        }
    }
           
//  public static void printBoard(BoardNode[][]board) {
//      for (int i = 0; i < board.length; i++) {
//          for (int j = 0; j < board.length; j++) {
//              if (board[i][j].getIsMine()) {
//                  System.out.print(1);
//              }else {
//                  System.out.print(0);
//              }
//          }
//          System.out.println();
//      }
//  }
   
    public static DefaultXYDataset agentPerformance() {
        DefaultXYDataset scoreData = new DefaultXYDataset();
        double [][] basicScores = new double[2][100];
        double [][] directInfScores = new double[2][100];
        for (int numMines = 1; numMines <= 100; numMines++) {
            double totalBasicScore = 0.0;
            double totalDirInfScore = 0.0;
            for (int trial = 0; trial < 1000; trial++) {
                Environment myBoard = new Environment(10, numMines);
                totalBasicScore += playGame(myBoard, numMines);
                totalDirInfScore += playInferenceGame(myBoard, numMines);
            }
            double avgBasicScore = totalBasicScore/1000;
            double avgDirInfScore = totalDirInfScore/1000;
            System.out.println("Mine Density: " + numMines*0.01);
            System.out.println("Avg Basic Score: " + avgBasicScore);
            System.out.println("Avg Direct Inf Score: " + avgDirInfScore);
            basicScores[0][numMines-1] = numMines*0.01;
            basicScores[1][numMines-1] = avgBasicScore;
            directInfScores[0][numMines-1] = numMines*0.01;
            directInfScores[1][numMines-1] = avgDirInfScore;
        }
       
        scoreData.addSeries("Basic Agent Score vs. Mine Density", basicScores);
        scoreData.addSeries("Direct Inference Agent Score vs. Mine Density", directInfScores);
        return scoreData;
       
    }
   
    public static void plotAgentPerformance() {
        ApplicationFrame agentPerformanceApp = new ApplicationFrame("Average Score vs. Mine Density");
        JFreeChart agentPerformancePlot = ChartFactory.createXYLineChart("Avg Score of Agent against Mine Density", "Mine Density", "Avg Score", agentPerformance(), PlotOrientation.VERTICAL, true, true, false);
        ChartPanel chartPanel = new ChartPanel(agentPerformancePlot);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
        agentPerformanceApp.setContentPane(chartPanel);
        agentPerformanceApp.pack();
        RefineryUtilities.centerFrameOnScreen(agentPerformanceApp);
        agentPerformanceApp.setVisible(true);
    }
   
    public static void main(String[] args) {
    	
    	Environment gameBoard = new Environment(5, 5);
     
	    BoardCellPanel[][] mineSweeperCells = buildBoard(5);
	    for (int i = 0; i < mineSweeperCells.length; i++) {
	        for (int j = 0; j < mineSweeperCells.length; j++) {
	            changeCell(mineSweeperCells[i][j], gameBoard.textGUI(i, j));
	        }
	    }
//      double score = playGame(gameBoard, 27);
//      System.out.println("Basic Agent Score: " + score);
      	double score = playInferenceGame(gameBoard, 5);
//      System.out.println("Inference game score: " + score);
//        plotAgentPerformance();
       
        // TODO Auto-generated method stub
        /*int numTotalMines = 27;
        BoardNode[][] myBoard = generateBoard(9, numTotalMines);
        ArrayList<BoardNode> availableCells = new ArrayList<BoardNode>();
        System.out.println("Original Board's numClearedNbrs: ");
        for (int i = 0; i < myBoard.length; i++) {
            for (int j = 0; j < myBoard.length; j++) {
                System.out.print(myBoard[i][j].numClearedNbr + " ");
            }
            System.out.println();
        }
        availableCells.add(myBoard[2][3]);
        availableCells.add(myBoard[5][6]);
        while (availableCells.size() != 0) {
            BoardNode queryNext = availableCells.remove(0);
            Logic constraint = new Logic(queryNext, getNbrs(myBoard, queryNext));
            System.out.println("Unknown Neighbors: ");
            List<BoardNode> unknownNbrs = constraint.unknownNbrs;
            for (int i = 0; i < unknownNbrs.size(); i++) {
                System.out.println("(" + unknownNbrs.get(i).row + ", " + unknownNbrs.get(i).col + ")");
            }
            System.out.println("All Neighbors: ");
            List<BoardNode> allNbrs = constraint.allNbrs;
            for (int i = 0; i < allNbrs.size(); i++) {
                System.out.println("(" + allNbrs.get(i).row + ", " + allNbrs.get(i).col + ")");
            }
           
            // Query cell
            if (queryNext.getIsMine()) {
                queryNext.isCleared = false;
                queryNext.isMineFlagged = true;
            }
        }*/
       
       
        /*for (int i = 0; i < mineSweeperCells.length; i++) {
            for (int j = 0; j < mineSweeperCells[i].length; j++) {
                if (!myBoard[i][j].getIsMine()) {
                    changeCell(mineSweeperCells[i][j], String.valueOf(myBoard[i][j].getNumMineNbrs()));
                }
                if (myBoard[i][j].getIsMine()) {
                    changeCell(mineSweeperCells[i][j], "exploded");
                }
            }
        }*/
//      playGame(myBoard);
        //playInferenceGame(myBoard);
       
        /*try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < mineSweeperCells.length; i++) {
            for (int j = 0; j < mineSweeperCells[i].length; j++) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
               
                if (myBoard[i][j].getIsMine()) {
                    changeCell(mineSweeperCells[i][j], "M");
                } else {
                    changeCell(mineSweeperCells[i][j], "C");
                }
            }
        } */
    }
}