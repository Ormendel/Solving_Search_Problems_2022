/* =========Ex1============    
 *      
 *----------Submitted by----------
 *|     Id: ---------            |
 *|    Name: Or Mendel           |
 *|    Version: 2022-03 (4.16.0) |
 *----------------------
 *
 *=========================
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map.Entry;


import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;



public class Ex1 
{

	/*Declaring variables*/

	private static ArrayList<String> game = new ArrayList<>(); //For reading starting state
	private static ArrayList<String> goal = new ArrayList<>(); //For reading goal state
	private static String[][] board; //This variable is public on purpose, because class Node uses it
	private static String[][] boardTarget;
	private static String size; // can be "small" or "big"
	private static int converted_size; /* can be 3 or 5*/ 
	private static String algo=""; // variable for the algorithm: BFS | DFID* | A* | IDA* | DFBnB

	private static boolean with_open = true;//default value (will change if given no open)
	//set to count how many nodes we are creating
	public static HashSet<Node> set = new HashSet<>();

	//* measure time of algorithm */
	private static long startTime;
	private static double seconds = 0;
	
	/*Output file variables*/
	private static String path_toOutput= "";
	private static int cost_toOutput = 0;
	private static int created_states = 0;

	/*DFID important variables*/
	private static HashMap<String, Node> open_list_DFID = new HashMap<>();
	private static HashMap<String, Node> closed_list_DFID = new HashMap<>();
	private static int iterationDFID = 1;
	
	/*A* important variables*/
	private static HashMap<String, Node> open_list_astar = new HashMap<>();
	private static HashMap<String, Node> closed_list_astar = new HashMap<>();
	private static int iterationAstar = 1;
	
	/*Reading from input file function and initialize the given board (start) and target board (goal)*/
	private static void read_and_initialize_from_inputFile(String path) throws FileNotFoundException
	{
		File in = new File(path);
		Scanner reader = new Scanner(in);
		//while there is line to read
		algo= reader.nextLine();
		String wopen = reader.nextLine();
		if(wopen.equals("no open"))
			with_open = false;
		size = reader.nextLine();
		if(size.equals("small"))
			converted_size = 3;
		else
			converted_size = 5;

		for(int i=0;i<converted_size;++i)
		{
			game.add(reader.nextLine());
		}

		reader.nextLine();//In this line it's written "Goal state:"
		while (reader.hasNextLine())
		{
			goal.add(reader.nextLine()); //Here we are reading exactly #converted_size lines
		}
		reader.close();
	}

	/*Initializing boards*/
	private static void initializeBoards() 
	{
		board = new String[converted_size][converted_size];
		boardTarget = new String[converted_size][converted_size];		
		String row;
		for (int i = 0; i < board.length; ++i) 
		{
			row = game.get(i);
			board[i] = row.split(",");
		}
		
		for (int i = 0; i < boardTarget.length; ++i) 
		{
			row = goal.get(i);
			boardTarget[i] = row.split(",");
		}	
	}

	/*Comparing between our board to our goal board*/
	private static boolean are_identicalBoards(String[][]b1, String[][]b2)
	{
		for(int i=0;i<converted_size;++i)
		{
			for(int j=0;j<converted_size;++j)
			{
				if(!b1[i][j].equals(b2[i][j]))
					return false;
			}
		}
		return true;
	}
	private static boolean isValid(String[][]state, int x, int y)
	{
		if(x<0||x>=state.length||y<0||y>=state[x].length)
			return false;
		return true;
	}

	private static void swap(String[][]board,int x1, int y1, int x2, int y2)
	{
		String temp = board[x1][y1];
		board[x1][y1] = board[x2][y2];
		board[x2][y2] = temp;
	}


	public static String[][] deepCopy(String[][]board)
	{
		String[][] ret = new String[board.length][board[0].length];
		for (int i = 0; i < board.length; i++) 
			for (int j = 0; j < board[0].length; j++) 
				ret[i][j] = board[i][j];
		return ret;
	}
	/*Searching algorithms:
	 * 1. BFS Breadth-First-Search
	 * 2. DFID
	 * 3. A* (A_star)
	 * 4. IDA* (IDA_star)
	 * 5. DFBnB
	 */

	public static int defineCost(String[][]board, int i, int j)
	{
		int returned = 0;
		switch(board[i][j])
		{
		case "R":
		case "Y":
			returned = 1;
			break;
		case "B":
			returned = 2;
			break;
		case "G":
			returned = 10;
			break;
		default:
			break;
		}
		return returned;
	}
	private static Queue<Node> operator(Node n)	/*Creating all possible states from Node n (duplicates free) */
	{
		Queue<Node> ret = new LinkedList<Node>();
		String[][]curr = n.getBoardState();
		for(int i=0;i<converted_size;++i)
		{
			for(int j=0;j<converted_size;++j)
			{
				if(!curr[i][j].equals("_"))
				{
					//We found a marble. now for possible states from this marble
					if(isValid(curr, i, j-1) && curr[i][j-1].equals("_")) // 1. Check if we can move this marble to left side
					{
						String[][]insert = deepCopy(curr);
						swap(insert, i, j, i, j-1);
						int x_i = i+1; //increasing by 1
						int x_j = j+1; // increasing by 1
						int y_i = i+1; //increasing by 1
						int y_j = j ; //j-1+1
						String check = "("+x_i+","+x_j+"):"+curr[i][j]+":("+y_i+","+y_j+")--"; //for comparing opposite states
						
						String insert_path = "(" + x_i + "," + x_j + "):" + curr[i][j] + ":(" + y_i + "," + y_j + ")--";
						int cost = defineCost(curr, i, j);
						
						boolean oppsiteState = false;
						Node g2 = n;
						if(!g2.getPath().isEmpty())
						{
							String p = g2.getPath();
							if(p.charAt(1) == check.charAt(9) && p.charAt(3) == check.charAt(11))
								if(p.charAt(9) == check.charAt(1) && p.charAt(11) == check.charAt(3))
									if(p.charAt(6) == check.charAt(6))
										oppsiteState = true;
						}
						if(!oppsiteState)
						{
							Node g = new Node(insert, insert_path, n, cost);
							if(!set.contains(g))
							{
								set.add(g);
								++created_states;
								ret.add(g);
							}
						}
					}
					if(isValid(curr, i, j+1) && curr[i][j+1].equals("_")) // 2. Check if we can move this marble to right side
					{
						String[][]insert = deepCopy(curr);
						swap(insert, i, j, i, j+1);
						int x_i = i+1; //increasing by 1
						int x_j = j+1; // increasing by 1
						int y_i = i+1; //increasing by 1
						int y_j = j+2 ; //j+1+1
						String check = "("+x_i+","+x_j+"):"+curr[i][j]+":("+y_i+","+y_j+")--"; //for comparing opposite states
						
						String insert_path = "(" + x_i + "," + x_j + "):" + curr[i][j] + ":(" + y_i + "," + y_j + ")--";
						int cost = defineCost(curr, i, j);
						
						boolean oppsiteState = false;
						Node g2 = n;
						if(!g2.getPath().isEmpty())
						{
							String p = g2.getPath();
							if(p.charAt(1) == check.charAt(9) && p.charAt(3) == check.charAt(11))
								if(p.charAt(9) == check.charAt(1) && p.charAt(11) == check.charAt(3))
									if(p.charAt(6) == check.charAt(6))
										oppsiteState = true;
						}
						if(!oppsiteState)
						{
							Node g = new Node(insert, insert_path, n, cost);
							if(!set.contains(g))
							{
								set.add(g);
								++created_states;
								ret.add(g);
							}
						}
					}
					if(isValid(curr, i-1, j) && curr[i-1][j].equals("_")) // 3. Check if we can move this marble up
					{
						String[][]insert = deepCopy(curr);
						swap(insert, i, j, i-1, j);
						int x_i = i+1; //increasing by 1
						int x_j = j+1; // increasing by 1
						int y_i = i; //i-1+1
						int y_j = j+1 ; //increasing by 1
						String check = "("+x_i+","+x_j+"):"+curr[i][j]+":("+y_i+","+y_j+")--"; //for comparing opposite states
						
						String insert_path = "(" + x_i + "," + x_j + "):" + curr[i][j] + ":(" + y_i + "," + y_j + ")--";
						int cost = defineCost(curr, i, j);
						
						boolean oppsiteState = false;
						Node g2 = n;
						if(!g2.getPath().isEmpty())
						{
							String p = g2.getPath();
							if(p.charAt(1) == check.charAt(9) && p.charAt(3) == check.charAt(11))
								if(p.charAt(9) == check.charAt(1) && p.charAt(11) == check.charAt(3))
									if(p.charAt(6) == check.charAt(6))
										oppsiteState = true;
						}
						if(!oppsiteState)
						{
							Node g = new Node(insert, insert_path, n, cost);
							if(!set.contains(g))
							{
								set.add(g);
								++created_states;
								ret.add(g);
							}
						}
					}
					if(isValid(curr, i+1, j) && curr[i+1][j].equals("_")) // 4. Check if we can move this marble down
					{
						String[][]insert = deepCopy(curr);
						swap(insert, i, j, i+1, j);
						int x_i = i+1; //increasing by 1
						int x_j = j+1; // increasing by 1
						int y_i = i+2; //i+1+1
						int y_j = j+1 ; //increasing by 1
						String check = "("+x_i+","+x_j+"):"+curr[i][j]+":("+y_i+","+y_j+")--"; //for comparing opposite states
						
						String insert_path = "(" + x_i + "," + x_j + "):" + curr[i][j] + ":(" + y_i + "," + y_j + ")--";
						int cost = defineCost(curr, i, j);
						
						boolean oppsiteState = false;
						Node g2 = n;
						if(!g2.getPath().isEmpty())
						{
							String p = g2.getPath();
							if(p.charAt(1) == check.charAt(9) && p.charAt(3) == check.charAt(11))
								if(p.charAt(9) == check.charAt(1) && p.charAt(11) == check.charAt(3))
									if(p.charAt(6) == check.charAt(6))
										oppsiteState = true;
						}
						if(!oppsiteState)
						{
							Node g = new Node(insert, insert_path, n, cost);
							if(!set.contains(g))
							{
								set.add(g);
								++created_states;
								ret.add(g);
							}
						}
					}

				}
			}
		}
		/*return all possible operators*/
		//System.out.println("queue size = "+ret.size());
		return ret;
	}
	private static String unfixed_path_to_goal(Node n)	/*Getting the correct path from start to goal */
	{
		/* also get the cost */
		String unfixed="";
		while(n.getParent()!=null)
		{
			unfixed = unfixed.concat(n.getPath());
			cost_toOutput += n.getCost(); /* getting also the total cost from start to goal */
			n = n.getParent();
		}
		return unfixed;
	}
	private static String correct_path_to_goal(String s)	/*Getting the correct path from start to goal */
	{
		String ans = "";
		String[] arrOfPath = s.split("--");
		for(int i=arrOfPath.length-1;i>=0;--i)
		{
			ans = ans.concat(arrOfPath[i]);
			if(i!=0)//I don't want to add extra "--"
				ans = ans.concat("--");
		}
		return ans;
	}
	private static void print_results()
	{
		if(path_toOutput.isEmpty())
		{
			System.out.println("no path");
			System.out.println("Nums: "+created_states);
			System.out.println("Cost: inf");
			System.out.println(seconds+" seconds");
		}
		else
		{
			System.out.println(path_toOutput);
			System.out.println("Nums: "+created_states);
			System.out.println("Cost: "+cost_toOutput);
			System.out.println(seconds+" seconds");
		}
	}
	
	private static String uniqueMat(Node n) 
	{
		String[][] temp = n.getBoardState();
		String unique = "";
		for (int i = 0; i < temp.length; i++) 
		{
			for (int j = 0; j < temp[i].length; j++) 
			{
				unique = unique.concat(temp[i][j]);
			}
		}
		return unique;
	}
	
	private static boolean BFS(Node start) /*improved BFS with open list and closed list*/
	{
		Queue<Node> q = new LinkedList<Node>();
		Hashtable<Integer, Node> open_list = new Hashtable<>();
		Hashtable<Integer, Node> closed_list = new Hashtable<>();
		q.add(start);
		int index_open_list = 1, index_closed_list = 1;
		int iteration = 1;
		open_list.put(index_open_list++, start);
		while(!q.isEmpty())
		{
			if(with_open)
			{
				System.out.println("\n==========ITERATION #"+(iteration++)+"==========");
				System.out.println(open_list);
			}
			Node n = q.remove();
			if(open_list.containsValue(n))
			{
				/*Searching for the key*/
				for(Entry<Integer, Node> entry: open_list.entrySet())
				{
					if(are_identicalBoards(entry.getValue().getBoardState(), n.getBoardState()))
					{
						open_list.remove(entry.getKey());
					}
				}
			}
			closed_list.put(index_closed_list++, n);
			Queue<Node> check_operator = operator(n);
			for(Node g: check_operator)
			{
				if(!closed_list.containsValue(g) && !q.contains(g))
				{
					if(are_identicalBoards(g.getBoardState(), boardTarget))
					{
						open_list.put(index_open_list++, g);
						String path = unfixed_path_to_goal(g);
						path_toOutput = correct_path_to_goal(path);
						long end = System.currentTimeMillis() - startTime;
						seconds = end / 1000.0;
						if(with_open)
						{
							System.out.println("\n==========LAST ITERATION : #"+(iteration++)+"==========");
							System.out.println(open_list);
							System.out.println("\n Goal was found =]\n");
						}
						return true;
					}
					q.add(g);
					open_list.put(index_open_list++, g); //all children are being inserted into open_list
				}	
			}
		}
		long end = System.currentTimeMillis() - startTime;
		seconds = end / 1000.0;
		return false;
	}
	
	private static boolean DFID(Node start)
	{
		// iterate until we will find goal or no where to go
		for (int depth = 1; depth < Integer.MAX_VALUE; depth++) 
		{
			HashMap<String, Node> h = new HashMap<>(); /* Hash table to hold integer (unique id state) and node */
			set.clear();
			open_list_DFID.clear();
			String result = limited_DFS(start, depth, h); /* return string that explain status of search depth */
			if (!result.equals("cutoff")) /* if we found a solution to problem return the path to goal, otherwise return no path */
			{
				long end = System.currentTimeMillis() - startTime;
				seconds = end / 1000.0;
				return true;
				
			}
			
		}
		long end = System.currentTimeMillis() - startTime;
		seconds = end / 1000.0;
		return false;
	}
	private static String limited_DFS(Node n, int limit, HashMap<String, Node> h) 
	{
		
		open_list_DFID.put(uniqueMat(n), n);
		if(with_open)
		{
			System.out.println("\n==========ITERATION #"+(iterationDFID++)+": LIMIT #"+(limit)+" ==========");
			System.out.println(open_list_DFID);
		}
		//check if we found solution
		if (are_identicalBoards(n.getBoardState(), boardTarget)) 
		{
			String path = unfixed_path_to_goal(n);
			path_toOutput = correct_path_to_goal(path);
			if(with_open)
				System.out.println("\n Goal was found =] \n");
			return path_toOutput;
		}
		// else if limit reached zero we will go back and look deeper next round
		else if (limit == 0)
			return "cutoff";
		else
		{ 
			h.put(uniqueMat(n), n); /* insert Hash table current node */

			boolean isCutoff = false;
			
			Queue<Node> queueOp = operator(n);
			//for each node in queueOp check if answer question
			for (Node g : queueOp) 
			{
				if (h.containsKey(uniqueMat(g))) /* if hash table contains that path continue to next operator */
					continue;
				String result = limited_DFS(g, limit - 1, h); /* recurse to deeper layer with the current node */
				
				if (result.equals("cutoff")) /* if the string equals to cutoff then isCutoff will also will set to true */
				{
					isCutoff = true;
				}
				
				else if (!result.equals("fail")) /* if the result doesn't equal to fail return the result */
					return result;
			}
			closed_list_DFID.put(uniqueMat(n), n);
			//if we reached here means we couldn't find solution with n node so will remove it from hash table
			h.remove(uniqueMat(n),n);
			open_list_DFID.remove(uniqueMat(n),n);
			//if cutoff true -> return cutoff
			if (isCutoff)
				return "cutoff";
			// else return fail
			return "fail";
		}

	}
	
		//Manhattan function as my heuristic function
		private static int f(Node start) 
		{
			//if the node is null return 0
			if(start==null)
				return  0;
			//init distance to 0
			int distance = 0;
			//from the start board to the target board
			String[][]board = start.getBoardState();
			for (int i = 0; i < board.length; i++) 
			{
				for (int j = 0; j < board[0].length; j++) 
				{
					//as long as the current location is different enter to check distance from target
					if (!board[i][j].equals(boardTarget[i][j])) 
					{
						for (int k = 0; k < boardTarget.length; k++) 
						{
							for (int l = 0; l < boardTarget[0].length; l++)
							{
								//when we reached the right spot
								if (board[i][j].equals(boardTarget[k][l]))
								{
									//update distance to be according to the distance between start to target
									distance += Math.abs(i - k) + Math.abs(j - l);
								}
							}
						}
					}
				}
			}
			//update the return value by multiplying by 3 because of the distance according to the directions we can go
			//usually we can go 4 directions but we can't go back to previous state
			return 3 * distance;
		}
		
		public static boolean A_star_new(Node start) /*Finds the goal, but creates a lot of states - please consider!!!*/
		{
			// Creating empty priority queue
			PriorityQueue<Node> pQueue_openList = new PriorityQueue<>(new Comparator<Node>() 
			{
				public int compare(Node a1, Node a2)
				{
					//where will order according to the Cost of the nodes
					int a = a1.getCost();
					int b = a2.getCost();
					return a - b;
				}
			});
			
			//Add the start node
			open_list_astar.put(uniqueMat(start), start);
			pQueue_openList.add(start);
			int g=0;
			int h = f(start);
			int f = g + h;
			while (!pQueue_openList.isEmpty()) // Loop until you find the end
			{
				Node m = pQueue_openList.poll();
				if (are_identicalBoards(m.getBoardState(), boardTarget))
				{
					//because we return before the end we didn't include the last new Node
					//we decrement 1 because of root that isn't the number of moves...
					long end = System.currentTimeMillis() - startTime;
					seconds = end / 1000.0;
					String path = unfixed_path_to_goal(m);
					path_toOutput = correct_path_to_goal(path);
					if(with_open)
					{
						System.out.println("\n==========ITERATION #"+(iterationAstar++)+"==========");
						System.out.println(open_list_astar);
						System.out.println("\n Goal was found =]\n");
					}
					return true;
				}
				//add to set
				if(!set.contains(m))
				{
					set.add(m);
					++created_states;
				}
				if(with_open)
				{
					System.out.println("\n==========ITERATION #"+(iterationAstar++)+"==========");
					System.out.println(open_list_astar);
				}
				open_list_astar.remove(uniqueMat(m), m);
				pQueue_openList.remove(m);
				closed_list_astar.put(uniqueMat(m), m);
				//collect available operators
				Queue<Node> children = operator(m);
				for(Node n: children)
				{
					// Child is on the closedList
					if(closed_list_astar.containsKey(uniqueMat(n)))
						continue;
					int cost = f(m) + n.getCost();
					if(open_list_astar.containsKey(uniqueMat(n)) && cost < f(n))
					{
						if(with_open)
						{
							System.out.println("\n==========ITERATION #"+(iterationAstar++)+"==========");
							System.out.println(open_list_astar);
						}
						open_list_astar.remove(uniqueMat(n));
						pQueue_openList.remove(n);
					}
					if(closed_list_astar.containsKey(uniqueMat(n)) && cost < f(n))
						closed_list_astar.remove(uniqueMat(n));
					
					if(!open_list_astar.containsKey(uniqueMat(n)) && !closed_list_astar.containsKey(uniqueMat(n)))
					{
						open_list_astar.put(uniqueMat(n),n);
						pQueue_openList.add(n);
						
						g = cost;
						h = f(n);
						int f2 = g+h;
						f = Math.min(f, f2);
					}
					
				}
				
				
			}	
			
			long end = System.currentTimeMillis() - startTime;
			seconds = end / 1000.0;
			return false;
		}
		
		//first version of a*, without closed list
		public static boolean A_star(Node start) 
		{

			// Creating empty priority queue
			PriorityQueue<Node> pQueue = new PriorityQueue<>(new Comparator<Node>() 
			{
				public int compare(Node a1, Node a2)
				{
					//where will order according to the Cost of the nodes
					int a = a1.getCost();
					int b = a2.getCost();
					return a - b;
				}
			});

			// Creating empty Hashtable
			Hashtable<String, Node> ht = new Hashtable<>();
			//add to priority queue start state
			pQueue.add(start);
			open_list_astar.put(uniqueMat(start), start);
			//while priority queue isn't empty

			while (!pQueue.isEmpty())
			{
				//retrieve last node from queue
				if(with_open)
				{
					System.out.println("\n==========ITERATION #"+(iterationAstar++)+"==========");
					System.out.println(open_list_astar);
				}
				Node temp = pQueue.poll();
				//check if temp equal to target node if true return results
				if (are_identicalBoards(temp.getBoardState(), boardTarget))
				{
					//because we return before the end we didn't include the last new Node
					//we decrement 1 because of root that isn't the number of moves...
					long end = System.currentTimeMillis() - startTime;
					seconds = end / 1000.0;
					String path = unfixed_path_to_goal(temp);
					path_toOutput = correct_path_to_goal(path);
					if(with_open)
					{
						System.out.println("\n==========ITERATION #"+(iterationAstar++)+"==========");
						System.out.println(open_list_astar);
						System.out.println("\n Goal was found =]\n");
					}
					return true;
				}
				//insert hashtable unique string and node
				ht.put(uniqueMat(temp), temp);
				//add to set
				if(!set.contains(temp))
					set.add(temp);
				//collect available operators
				Queue<Node> qu = operator(temp);
				//for each node from qu
				for (Node tempNW : qu) 
				{
					//				if (oList) openList(tempNW);
					// if hashtable doesn't contain new temp as well as priority queue
					if (!ht.containsKey(uniqueMat(tempNW)) && !pQueue.contains(tempNW))
					{
						//add to priority queue the new temp node
						pQueue.add(tempNW);
						open_list_astar.put(uniqueMat(tempNW), tempNW);
						
						//else if priority queue contains new temp node
					} 
					else if (pQueue.contains(tempNW)) 
					{
						int a, b;
						Node found = null;
						//search in priority queue for the same node with different path
						for (Node a1 : pQueue) {
							//if we found a match
							if (uniqueMat(tempNW).equals(uniqueMat(a1))) 
							{
								//keep reference to that node
								found = a1;
								break;
							}
						}
						//get Manhattan distance value for each node
						a = f(found);
						b = f(tempNW);
						//if the new one is better
						if (b < a) 
						{
							//erase old one
							if(with_open)
							{
								System.out.println("\n==========ITERATION #"+(iterationAstar++)+"==========");
								System.out.println(open_list_astar);
							}
							pQueue.remove(found);
							open_list_astar.remove(uniqueMat(tempNW), tempNW);
							//add new one
							pQueue.add(tempNW);
							open_list_astar.put(uniqueMat(tempNW), tempNW);
						}
					}
					//end run for operators
				}
				

			}
			long end = System.currentTimeMillis() - startTime;
			seconds = end / 1000.0;
			if(with_open)
				print_results();
			return false;
		}
	
	private static boolean IDA_star_new(Node start)
	{
		Hashtable<String,Node> open_list = new Hashtable<>();
		int iteration = 1;
		Stack<Node> stack = new Stack<>();
		Hashtable<String,Node> hash = new Hashtable<>();
		HashSet<Node> out = new HashSet<>(); //we can also rename it as visited
		int t = f(start);
		while (t != Integer.MAX_VALUE)
		{
			int minF = Integer.MAX_VALUE;
			stack.add(start);
			open_list.put(uniqueMat(start), start);
			hash.put(uniqueMat(start), start);
			
			out.clear();
			set.clear();
			while (!stack.isEmpty())
			{
				
				if(with_open)
				{
					System.out.println("\n==========ITERATION #"+(iteration++)+ "==========");
					System.out.println(open_list);
				}
				
				Node node = stack.pop();

				if (out.contains(node))
					hash.remove(uniqueMat(node));
				else
				{
					out.add(node);
					stack.add(node);
					open_list.put(uniqueMat(node), node);
					for (Node temp: operator(node))
					{
						open_list.put(uniqueMat(temp), temp);
						int f = temp.getCost() + f(temp); //h(temp) + f(temp) while h is the cost of temp
						if (f > t)
						{
							minF = Math.min(minF,f);
							continue;
						}
						if (hash.containsKey(uniqueMat(temp)) && out.contains(temp))
							continue;
						if (hash.containsKey(uniqueMat(temp)) && !out.contains(temp))
						{
							Node g =  hash.get(uniqueMat(temp));
							if (f >= g.getCost() + f(g))
							{
								stack.remove(g);
								open_list.remove(uniqueMat(g), g);
								hash.remove(uniqueMat(g));
							}
							else{
								continue;
							}
						}
						if (are_identicalBoards(temp.getBoardState(), boardTarget))
						{
							long end = System.currentTimeMillis() - startTime;
							seconds = end / 1000.0;
							String path = unfixed_path_to_goal(temp);
							path_toOutput = correct_path_to_goal(path);
							if(with_open)
							{
								System.out.println("\n==========ITERATION #"+(iteration++)+ "==========");
								System.out.println(open_list);
								System.out.println("\n Goal was found =]\n");
							}
							return true;
						}
						stack.add(temp);
						hash.put(uniqueMat(temp), temp);
					}
				}
			}
			t = minF; //update t
		}

		long end = System.currentTimeMillis() - startTime;
		seconds = end / 1000.0;
		return false;
	}
	
	/* ORIGINAL IDA*
	private static boolean IDA_star(Node start)
	{
		
		//init stack,hashMap,hashSet
		Stack<Node> st = new Stack<>();
		HashMap<String, Node> h = new HashMap<>();
		HashSet<Node> out = new HashSet<>();
		//the Manhattan cost o the start node
		int t = f(start);
		//while t is smaller than maximum value
		while (t <= Integer.MAX_VALUE) 
		{
			h.clear();
			//start minF to be max value
			int minF = Integer.MAX_VALUE;
			//push in stack start node
			st.push(start);
			//insert to hashtable start unique string and node
			h.put(uniqueMat(start), start);
			//while stack isn't empty
			while (!st.isEmpty()) 
			{
				//pop first stack node
				Node n = st.pop();
				//if node is marked -> remove from hashtable node
				if (out.contains(n))
					h.remove(uniqueMat(n));
				else 
				{
					//if not than mark node with adding it to out group
					out.add(n);
					//fetch node operators
					Queue<Node> qu = operator(n);
					//for each node from operators
					for (Node g : qu) 
					{
						//if Manhattan cost of g node is higher than minF
						if (f(g) > t) 
						{
							//update minF to be lowest between minF and f(g) and continue to next operator
							minF = Math.min(minF, f(g));
							continue;
						}
						//get the same node from hashtable with the same unique order
						Node same = h.get(uniqueMat(g));
						//if hashtable contains g node and g is marked as out continue
						if (h.containsKey(uniqueMat(g)) && out.contains(g))
							continue;
						//if hashtable contains g node and g isnt marked as out
						if (h.containsKey(uniqueMat(g)) && !out.contains(g)) 
						{
							//if the Manhattan value of same is higher than g node
							if (f(same) > f(g))
							{
								//remove same from hashtable
								h.remove(uniqueMat(same));
								//also remove g from stack
								st.remove(g);
								//else continue
							}
							else
								continue;
						}
						//if g equals to target return answers
						if (are_identicalBoards(g.getBoardState(), boardTarget)) 
						{
							long end = System.currentTimeMillis() - startTime;
							seconds = end / 1000.0;
							String path = unfixed_path_to_goal(g);
							path_toOutput = correct_path_to_goal(path);
							if(with_open)
								print_results();
							return true;
						}
						//insert to hashtable g
						h.put(uniqueMat(g), g);
						//insert g to stack
						st.push(g);

					}

				}
			}
			//update t to be minF
			t = minF;

		}
		long end = System.currentTimeMillis() - startTime;
		seconds = end / 1000.0;
		if(with_open)
			print_results();
		return false;
	}
	
	*/
	private static boolean DFBnB(Node start)
	{
		//init stack,hashMap,hashSet
		Stack<Node> st = new Stack<>();
		HashMap<String, Node> open_list = new HashMap<>();
		int iteration = 1;
		HashMap<String, Node> ht = new HashMap<>();
		HashSet<String> out = new HashSet<>();
		//push to stack and hash table start position
		st.push(start);
		open_list.put(uniqueMat(start), start);
		ht.put(uniqueMat(start), start);
		//init t to be max size
		int t = Integer.MAX_VALUE;
		//while stack isn't empty
		while (!st.isEmpty()) 
		{
			if(with_open)
			{
				System.out.println("\n==========ITERATION #"+(iteration++)+ "==========");
				System.out.println(open_list);
			}
			//n will be the first node from stack
			Node n = st.pop();
			//we need to check if we've been here -> loop avoidance
			//if we've been remove node from hashTable
			if (out.contains(uniqueMat(n))) ht.remove(uniqueMat(n));
			else 
			{
				//mark n node as out -> add to out group
				out.add(uniqueMat(n));
				//push n to stack
				st.push(n);
				open_list.put(uniqueMat(n), n);
				//get all operators nodes to queue
				Queue<Node> regularQueue = operator(n);
				//crate priority queue according to cost of Manhattan function
				Queue<Node> priorityQueue = new PriorityQueue<>(new Comparator<Node>() 
				{
					public int compare(Node a1, Node a2) 
					{
						int a = f(a1);
						int b = f(a2);
						return a - b;
					}
				});
				//add to priority queue all of nodes from the operator queue whilst sorting them
				priorityQueue.addAll(regularQueue);
				//add to open list
				for(Node add : priorityQueue)
				{
					open_list.put(uniqueMat(add), add);
				}
				//for all of the nodes from priority queue
				ArrayList<Node> list = new ArrayList<>(priorityQueue);
				for (int i = 0; i < list.size(); i++) 
				{
					//get node from i index
					Node g = list.get(i);
					//if Manhattan cost higher than t
					if (f(g) >= t) 
					{
						//remove all from hashtable
						ht.clear();
						//if hashtable contains g node and that node is marked as out
					} 
					else if (ht.containsKey(uniqueMat(g)) && out.contains(ht.get(uniqueMat(g)))) 
					{
						//remove the node from hashtable and go back by one
						list.remove(i--);
						//if hashtable contains g node and that node isn't marked as out
					} 
					else if (ht.containsKey(uniqueMat(g)) && !out.contains(ht.get(uniqueMat(g))))
					{
						//check if the Manhattan cost is better in the old one if yes remove current node from list
						if (f(ht.get(uniqueMat(g))) <= f(g)) 
						{
							list.remove(i--);
						
						} 
						else 
						{
							if(with_open)
							{
								System.out.println("\n==========ITERATION #"+(iteration++)+ "==========");
								System.out.println(open_list);
							}
							
							//if non of the conditions were correct
							//remove from stack and from hashTable old node
							st.remove(ht.get(uniqueMat(g)));
							ht.remove(ht.get(uniqueMat(g)));
							
							open_list.remove(uniqueMat(g));
						}
						//if new node equals to target stop return answer
					} 
					else if (are_identicalBoards(g.getBoardState(), boardTarget))
					{
						t=f(g);
						list.clear();
						long end = System.currentTimeMillis() - startTime;
						seconds = end / 1000.0;
						String path = unfixed_path_to_goal(g);
						path_toOutput = correct_path_to_goal(path);
						if(with_open)
						{
							System.out.println("\n==========ITERATION #"+(iteration++)+ "==========");
							System.out.println(open_list);
							System.out.println("\n Goal was found =]\n");
						}
						return true;
					}

				}
				//reverse list
				Collections.reverse(list);
				for (Node temp : list) 
				{
					//insert to stack and hashtable all nodes that were left
					st.push(temp);
					ht.put(uniqueMat(temp), temp);
					open_list.put(uniqueMat(temp), temp);
				}
			}
		}

		long end = System.currentTimeMillis() - startTime;
		seconds = end / 1000.0;
		return false;
	}

	/*Main function*/
	public static void main(String[] args) throws IOException 
	{
		/*  Create an output file, writing to it (with append flag) and close it:
			https://www.youtube.com/watch?v=k3K9KHPYZFc&ab_channel=PaulMiskew */

		try 
		{
			read_and_initialize_from_inputFile("src/input.txt");
		}
		catch (FileNotFoundException e) 
		{
			System.out.println("An error occured while trying opening the file =[");
			e.printStackTrace();
		}
		initializeBoards();

		Node start = new Node(board, "", null, 0); /*starting state has been created successfully*/
		set.add(start); //I could have done created_states++ after this line, but it doesn't really count for the starting state.

		//measure time of algorithm
		startTime = System.currentTimeMillis();
		
		switch(algo)
		{
			case "BFS": BFS(start);
				break;
			case "DFID": DFID(start);
				break;
			case "A*": A_star_new(start);
				break;
			case "IDA*": IDA_star_new(start);
				break;
			case "DFBnB": DFBnB(start);
				break;
			default:
				break;
					
		}

		/*Writing to output and by that ending assignment*/
		
		File file = new File("src/my_output.txt");
		FileWriter fw = new FileWriter(file,true);
		PrintWriter pw = new PrintWriter(fw);
		
		if(path_toOutput.isEmpty())
		{
			pw.println("no path");
			pw.println("Num: "+created_states);
			pw.println("Cost: inf");
			
		}
		else
		{
			pw.println(path_toOutput);
			pw.println("Num: "+created_states);
			pw.println("Cost: "+cost_toOutput);
		}
		pw.print(seconds+" seconds");
		pw.close();

	}

}
