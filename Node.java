import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;



/*
 * Color of a marble:
 * R = Red, B = Blue, G = Green, Y = Yellow
 */

/*This class represents all possible states of matrix, hence each node is a matrix itself*/
public class Node 
{
	private  String[][]boardState;
	private  String path = ""; //building path for our answer
	private  int cost;//taking all marbles we moved and summed them
	private  Node parent; //which state brought us here


	//Constructor
	/*color, cost and adj of a given node are initialized in main function of Ex1*/
	public Node(String[][]tempBoard, String path, Node parent, int cost)
	{
		this.boardState = tempBoard;
		this.path = path;
		this.parent = parent;
		this.cost = cost;
	}

	public String[][] getBoardState() {
		return boardState;
	}

	public void setBoardState(String[][] boardState) {
		this.boardState = boardState;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}
	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(boardState);
		return result;
	}


	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (!Arrays.deepEquals(boardState, other.boardState))
			return false;
		return true;
	}

	private static String printBoard(String[][]mat)
	{
		String convertedBoard = "";
		char c;
		for(int i=0;i<mat.length;++i)
		{
			for(int j=0;j<mat[0].length;++j)
			{
				convertedBoard = convertedBoard.concat(mat[i][j]+" ");
			}
			if(i!=mat.length-1)
				convertedBoard = convertedBoard.concat(",");
		}
		return convertedBoard;
	}
	@Override
	public String toString() 
	{
		return "Node [boardState=" + printBoard(this.boardState) + "]";
	}
	





}
