import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Random;


/*//////////////////
 * USER CONTROLS  
 *
 *  b - begin breadth first search
 *  d - begin depth first search
 *  m - begin manual traversal using the arrow keys
 *  r - restart the board with a new maze at any time
 */




//Constants used in the program
interface Cnst {

  ///////////////////////////////////
  // Constants that can be editted //
  ///////////////////////////////////

  // playing with this gives the appropriate board size for the screen
  int scale = 4;

  //set how many blocks there are in each row
  int blocksWide = 100;

  //set how many blocks there are in each column
  int blocksHigh = 60;

  /////////////////////////////////////
  // Constants that can't be editted //
  /////////////////////////////////////
  
  //sets the size of each block
  int blockDimensions = scale * 2;

  //the width of the maze
  int boardWD = blocksWide * blockDimensions;

  //the height of the maze
  int boardHT = blocksHigh * blockDimensions;
}

//represents the maze to play on
class Maze extends World {
  //the number of blocks wide
  int width;
  
  //the number of blocks high
  int height;
  
  //random used for random numbers
  Random rand;
  
  //the list of Cells that the board is
  ArrayList<Cell> board;
  
  //the lise of Edges that the board is
  ArrayList<Edge> edges;
  
  //the worklist for processing both drawing and traversing
  ArrayDeque<Cell> worklist;
  
  //the current cell that we are at in the maze during manual travel
  Cell currCell;
  
  //Hash map that helps the program build up the edges to draw back easily
  HashMap<String, Edge> map;
  
  //represents what mode the searching is in
  String mode;
  /* b - breadth search
   * d - depth search
   * m - manual search
   * f - the end has been reached
   * end - the end has been reached and the path has been drawn back, can only reset board
   *       from this state
   */

  
  //constructor that takes in every field including the worklist
  public Maze(int width, int height, Random rand, ArrayList<Cell> board, ArrayList<Edge> edges,
      ArrayDeque<Cell> worklist, String mode, Cell currCell, HashMap<String, Edge> map) {
    this.width = width;
    this.height = height;
    this.rand = rand;
    this.board = board;
    this.edges = edges;
    this.worklist = worklist;
    this.mode = "";
    this.currCell = currCell;
    this.map = map;
  }

  //constructor that takes in every field from part 1
  Maze(int width, int height, Random rand, ArrayList<Cell> board,
      ArrayList<Edge> edges) {
    this.width = width;
    this.height = height;
    this.rand = rand;
    this.board = board;
    this.edges = edges;
    this.worklist = new ArrayDeque<Cell>();
    this.mode = "";
    this.currCell = this.boardIndex(0, 0);
    this.map = new HashMap<String, Edge>();
  }
  
  //constructor to test with a random seed input
  Maze(int width, int height, Random rand) {
    this.width = width;
    this.height = height;
    this.rand = rand;
    this.board = this.unconnectedCells();
    this.edges = new ArrayList<Edge>();
    this.worklist = new ArrayDeque<Cell>();
    this.mode = "";
    this.currCell = this.boardIndex(0, 0);
    this.map = new HashMap<String, Edge>();
  }
  
  //convinience constructor that only needs the width and height
  Maze(int width, int height) {
    this.width = width;
    this.height = height;
    this.rand = new Random();
    this.board = this.unconnectedCells();
    this.edges = new ArrayList<Edge>();
    this.worklist = new ArrayDeque<Cell>();
    this.mode = "";
    this.currCell = this.boardIndex(0, 0);
    this.map = new HashMap<String, Edge>();
  }



  //gets the cell at a place in the board, top left is 0,0
  public Cell boardIndex(int x, int y) {
    return this.board.get(x + y * this.width);
  }

  //produce a list of cells that are connected to end cells on all sides
  public ArrayList<Cell> unconnectedCells() {
    ArrayList<Cell> dest = new ArrayList<Cell>();
    for (int y = 0; y < this.height; y++) {
      for (int x = 0; x < this.width; x++) {
        dest.add(new Cell(x, y));
      }
    }
    return dest;
  }
  
  //reset the maze, even when worklists and edges and board is full
  public void resetMaze() {
    this.rand = new Random();
    this.board = this.unconnectedCells();
    this.edges = new ArrayList<Edge>();
    this.worklist = new ArrayDeque<Cell>();
    this.mode = "";
    this.map.clear();
    this.initializeMaze();
    this.currCell = this.boardIndex(0, 0);
  }

  //setup the maze with the edges and cell connections
  public void initializeMaze() {
    this.initializeEdges();
    this.connectCells();
    this.boardIndex(0, 0).setColor(Color.GREEN);
    this.boardIndex(this.width - 1, this.height - 1).setColor(Color.RED);
  }

  //populate the edges with every cell possible, only edges going left or down
  public void genAllEdges() {
    this.edges = new ArrayList<Edge>();
    for (Cell c : this.board) {
      c.genAllEdges(this);
    }
    this.edges.sort(new EdgeComparator());
  }

  //Uses Kruskal's algorithm to initialize the board with only a number of edges needed
  public void initializeEdges() {
    this.genAllEdges();
    HashMap<String, String> reps = new HashMap<String, String>();
    ArrayList<Edge> dest = new ArrayList<Edge>();
    ArrayList<String> cellNames = new ArrayListUtils().getCellNames(this.board);

    ArrayList<Edge> worklist = new ArrayList<Edge>();
    for (Edge e : this.edges) {
      worklist.add(e);
    }

    for (String s : cellNames) {
      reps.put(s, s);
    }

    while (dest.size() < this.board.size() - 1) {
      Edge currEdge = worklist.remove(0);
      if (!currEdge.sameReps(reps)) {
        dest.add(currEdge);
        currEdge.union(reps);
      }
    }
    this.edges = dest;
  }

  //Connects cells to each other, only for cells that have edges connecting them though
  public void connectCells() {
    for (Edge e : this.edges) {
      e.connectCells();
    }
  }

  //draws the maze
  public WorldImage drawWorld() {
    WorldImage dest = new EmptyImage();
    for (int i = 0; i < this.height; i++) {
      dest = new AboveImage(dest, this.drawRow(i));
    }
    return dest;
  }

  //draws one row in the maze
  public WorldImage drawRow(int y) {
    WorldImage dest = new EmptyImage();
    for (int i = 0; i < this.width; i++) {
      dest = new BesideImage(dest, this.boardIndex(i, y).drawCell());
    }
    return dest;
  }

  //override for the big bang to run
  @Override
  public WorldScene makeScene() {
      WorldScene scene = this.getEmptyScene();
      //make the image to draw \/  \/
      WorldImage cell = this.drawWorld();
      scene.placeImageXY(cell, 600, 500);
      return scene;
  }
  
  //overriding onTick which does things with the worklist in searching or
  //  drawing the path back at the end
  public void onTick() {
    if ((this.mode.equals("b") || this.mode.equals("d")) && this.worklist.size() > 0) {
      this.collector();
    }
    
    if (this.mode.equals("f") && !this.map.isEmpty()) {
      if (this.currCell.equals(this.boardIndex(0, 0))) {
        this.mode = "end";
      } else {
        this.drawBack();
      }
    }
  }
  
  //draw back the path from the end of the maze to the beginning
  public void drawBack() {
    this.currCell.drawBack(this);
  }
  
  public void collector() {
    Cell currCell = this.worklist.remove();
    currCell.collector(this);
  }
  
  //key event override that calls the corresponding key method
  public void onKeyEvent(String key) {
    if (key.equals("b") && this.mode.equals("")) {
      this.mode = "b";
      this.worklist.add(this.boardIndex(0, 0));
    }
    else if (key.equals("d") && this.mode.equals("")) {
      this.mode = "d";
      this.worklist.add(this.boardIndex(0, 0));
    }
    else if (key.equals("r")) {
      this.resetMaze();
    }
    else if (key.equals("m") && this.mode.equals("")) {
      this.mode = "m";
    }
    else if ("up down left right".contains(key) && this.mode.equals("m")) {
      currCell.arrowKey(key, this);
    }
  }
  
}

//represents either an actual cell or an unconnected cell ref
abstract class ACell {
  //make an edge from the previous cell to this one in the maze
  public abstract void genEdge(Maze m, Cell prev);

  //helper to draw walls horizontally
  public abstract WorldImage drawCellHelpH();

  //helper to draw wall vertically
  public abstract WorldImage drawCellHelpV();
  
  //helper to traverse in breadth case
  public void collectorHelper(Maze m, Cell prev) {
    return;
  }
  
  //traverses to a neighboring cell during manual traversal
  public void arrowKeyHelp(Maze m, Cell prev) {
    return;
  }
}

//represents an actual visible cell
class Cell extends ACell {
  //represents the x position of the cell in the maze
  int x;

  //represents the y position of the cell in the maze
  int y;

  //represents the cell's unique string identifier
  String name;

  //represents whether the cell has been visited or not
  boolean visited;

  //represents what color the cell is
  Color color;

  //represents the cell to the right
  ACell right;

  //cell to the left
  ACell left;

  //ref up
  ACell up;

  //ref down
  ACell down;

  //convinience constructor that only sets all the cell's references to End Cells
  Cell(int x, int y) {
    this.x = x;
    this.y = y;
    this.name = Integer.toString(x) + "," + Integer.toString(y);
    this.visited = false;
    this.color = Color.GRAY;
    this.right = new EndCell();
    this.left = new EndCell();
    this.up = new EndCell();
    this.down = new EndCell();
  }

  //constructor that lets you set everything but the string
  public Cell(int x, int y, Color color, ACell right, ACell left,
      ACell up, ACell down) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.visited = false;
    this.name = Integer.toString(x) + "," + Integer.toString(y);
    this.right = right;
    this.left = left;
    this.up = up;
    this.down = down;
  }

  //constructor made in part 2 for testing involving visited field
  public Cell(int x, int y, String name, boolean visited, Color color, ACell right, ACell left,
      ACell up, ACell down) {
    this.x = x;
    this.y = y;
    this.name = name;
    this.visited = visited;
    this.color = color;
    this.right = right;
    this.left = left;
    this.up = up;
    this.down = down;
  }
  
  //find the edge that the cell has connected to be able to draw back
  public void drawBack(Maze m) {
    Edge e = m.map.get(this.name);
    this.color = Color.BLUE;
    e.drawBack(m, this.name);
  }

  //arrow key traversal in the maze
  public void arrowKey(String dir, Maze m) {
    switch (dir) {
    case "up":
      this.up.arrowKeyHelp(m, this);
      break;
    case "left":
      this.left.arrowKeyHelp(m, this);
      break;
    case "right":
      this.right.arrowKeyHelp(m, this);
      break;
    case "down":
      this.down.arrowKeyHelp(m, this);
      break;
    default:
      return;
    }
  }

  //Traversing to neighboring cells that the user inputs
  public void arrowKeyHelp(Maze m, Cell prev) {
    prev.visited = true;
    if (!this.visited) {
      m.map.put(this.name, new Edge(prev, this, 0));
    }
    prev.color = Color.CYAN;
    if (this.color.equals(Color.RED)) {
      m.mode = "f";
    }
    this.color = Color.YELLOW;
    m.currCell = this;
  }
  
  //breadth collector that populates the worklist with new cells
  public void collector(Maze m) {
    if (this.color.equals(Color.RED)) {
      m.worklist.clear();
      m.mode = "f";
      m.currCell = this;
    }
    else {
      this.visited = true;
      this.up.collectorHelper(m, this);
      this.left.collectorHelper(m, this);
      this.down.collectorHelper(m, this);
      this.right.collectorHelper(m, this);
    }
  }
  
  //at one of the neighboring cells, add to worklist only if it is both a cell and not visited
  public void collectorHelper(Maze m, Cell prev) {
    if (!this.visited) {
      m.map.put(this.name, new Edge(prev, this, 0));
      if (m.mode.equals("b")) {
        m.worklist.addLast(this);
      }
      else if (m.mode.equals("d")) {
        m.worklist.addFirst(this);
      }
    }
  }
  
  //sets the cell's color
  public void setColor(Color c) {
    this.color = c;
  }


  //adds all of this cell's edges to the maze
  public void genAllEdges(Maze m) {
    if (this.x != m.width - 1) {
      m.boardIndex(this.x + 1, this.y).genEdge(m, this);
    }
    if (this.y != m.height - 1) {
      m.boardIndex(this.x, this.y + 1).genEdge(m, this);
    }
  }

  //generate an edge between this and the last cell into the maze
  @Override
  public void genEdge(Maze m, Cell prev) {
    int cost = m.rand.nextInt(100);
    m.edges.add(new Edge(prev, this, cost));
  }

  //adds this cell's name to a list of cell names
  public void addNames(ArrayList<String> dest) {
    dest.add(this.name);
  }

  //checks if this and the other cell have the same reps in the hash map
  public boolean sameReps(Cell other, HashMap<String, String> m) {
    HashMapUtilities util = new HashMapUtilities();
    return util.findRep(this.name, m).equals(util.findRep(other.name, m));
  }

  //connect the two cells/trees in the hashmap
  public void union(Cell other, HashMap<String,String> m) {
    HashMapUtilities util = new HashMapUtilities();
    m.replace(util.findRep(this.name, m),
        util.findRep(other.name, m));
  }

  //connects this cell with the other one, so their refs point to each other
  public void connectCells(Cell other) {
    if (this.x == other.x && this.y - 1 == other.y) {
      this.up = other;
      other.down = this;
    }
    if (this.x == other.x && this.y + 1 == other.y) {
      this.down = other;
      other.up = this;
    }
    if (this.y == other.y && this.x + 1 == other.x) {
      this.right = other;
      other.left = this;
    }
    if (this.y == other.y && this.x - 1 == other.x) {
      this.left = other;
      other.right = this;
    }
  }

  //////////////////
  // Draw Methods //
  //////////////////

  //draws a cell with walls where they don't have a cell connection
  public WorldImage drawCell() {
    if (this.visited && (this.color.equals(Color.GRAY) || this.color.equals(Color.RED))) {
      this.setColor(Color.CYAN);
    }
    
    return
        new AboveImage(
            new AboveImage(this.up.drawCellHelpH(),
                new BesideImage(
                    new BesideImage(this.left.drawCellHelpV(),
                        new RectangleImage(Cnst.blockDimensions,
                            Cnst.blockDimensions, OutlineMode.SOLID, this.color)),
                    this.right.drawCellHelpV())),
            this.down.drawCellHelpH());
  }

  //return no image because no wall here
  @Override
  public WorldImage drawCellHelpH() {
    return new EmptyImage();
  }

  //return no image bc no wall here
  @Override
  public WorldImage drawCellHelpV() {
    return new EmptyImage();
  }
}

//what a cell connects to if it isn't another visible cell, useful for dispatch
//  methods and drawing
class EndCell extends ACell {
  //do nothing because this can't make an edge
  @Override
  public void genEdge(Maze m, Cell prev) {
    return;
  }

  //draw a horizontal wall the length of the cell
  @Override
  public WorldImage drawCellHelpH() {
    return new LineImage(new Posn(Cnst.blockDimensions,0), Color.BLACK);
  }

  //draw a vertical wall the length of the cell
  @Override
  public WorldImage drawCellHelpV() {
    return new LineImage(new Posn(0, Cnst.blockDimensions), Color.BLACK);
  }
}

//represents a connection between two visible Cells
class Edge {
  //the first cell
  Cell c1;
  
  //the second cell
  Cell c2;
  
  //the "cost" of making/traversing this edge, used for Kruskal's algorithm
  int cost;
  
  //represents the unique string id for this edge
  String name;

  //constructor with all but name fields
  Edge(Cell c1, Cell c2, int cost) {
    this.c1 = c1;
    this.c2 =  c2;
    this.cost = cost;
    this.name = c1.name + "-" + c2.name;
  }

  //goes to the next location that must be drawn back
  public void drawBack(Maze m, String prevName) {
    m.map.remove(prevName, this);
    m.currCell = this.c1;
  }

  //checks if both connected cells have the same rep in the hash map
  public boolean sameReps(HashMap<String, String> m) {
    return this.c1.sameReps(this.c2, m);
  }

  //sets the two cells to the same rep in the hash map
  public void union(HashMap<String,String> m) {
    this.c1.union(this.c2, m);
  }

  //connect the references of both of these cells
  public void connectCells() {
    this.c1.connectCells(this.c2);
  }
}

//function object that sorts an ArrayList<Edge> by cost in ascending order
class EdgeComparator implements Comparator<Edge> {
  //compare function that sorts edges by cost in ascending order
  @Override
  public int compare(Edge e1, Edge e2) {
    return e1.cost - e2.cost;
  }
}

//function object that gets the list of names from a list of cells
class ArrayListUtils {
  //get a list of the cell names from a list of cells
  public ArrayList<String> getCellNames(ArrayList<Cell> cells) {
    ArrayList<String> dest = new ArrayList<String>();
    for (Cell c : cells) {
      c.addNames(dest);
    }
    return dest;
  }
}

//function object that finds the rep of a key in the hashmap
class HashMapUtilities {
  //find the rep of a key in the hashmap
  public String findRep(String key, HashMap<String, String> m) {
    if (key.equals(m.get(key))) {
      return key;
    }
    else {
      return this.findRep(m.get(key), m);
    }
  }


}

//examples class
class ExamplesMaze {
  Maze runMaze = new Maze(Cnst.blocksWide, Cnst.blocksHigh);

  Maze m1 = new Maze(3,3);
  Maze m2 = new Maze(3,3,new Random(1));
  Maze m3 = new Maze(2,2,new Random(2));
  Maze m4 = new Maze(3,3,new Random(42));

  Maze mEdgesNotInit = new Maze(2,2, new Random(20));

  HashMap<String, String> hm1 = new HashMap<String, String>();
  HashMap<String,String> hm2 = new HashMap<String,String>();

  HashMap<String, String> m1Map = new HashMap<String, String>();
  HashMap<String,String> m1Map2 = new HashMap<String, String>();
  HashMap<String,String> m1Map3 = new HashMap<String, String>();
  HashMap<String,String> m1Map4 = new HashMap<String, String>();
  ArrayList<String> m1Names = new ArrayList<String>(Arrays.asList(
      "0,0", "1,0", "0,1", "1,1"));

  Cell c1 = new Cell(0, 0, Color.PINK, this.c2, new EndCell(),
      new EndCell(), new EndCell());
  Cell c2 = new Cell(1,0, Color.PINK, new EndCell(), this.c1,
      new EndCell(), this.c4);
  Cell c3 = new Cell(0,1, Color.PINK, this.c4, new EndCell(),
      new EndCell(), new EndCell());
  Cell c4 = new Cell(1,1, Color.PINK, new EndCell(), this.c3,
      this.c2, new EndCell());

  Edge c1_c2 = new Edge(this.c1, this.c2, 1);
  Edge c2_c4 = new Edge(this.c2, this.c4, 2);
  Edge c3_c4 = new Edge(this.c3, this.c4, 3);

  HashMap<String,String> mapConnected = new HashMap<String,String>();
  HashMap<String,String> mapUnconnected = new HashMap<String,String>();

  Maze m = new Maze(2,2, new Random(3), new ArrayList<Cell>(
      Arrays.asList(this.c1, this.c2, this.c3, this.c4)),
      new ArrayList<Edge>(Arrays.asList(this.c1_c2, this.c2_c4,
          this.c3_c4)));

  void initData() {
    this.runMaze = new Maze(Cnst.blocksWide, Cnst.blocksHigh);
    this.runMaze.initializeMaze();

    this.m1 = new Maze(3,3);
    this.m1.initializeMaze();

    this.m2 = new Maze(3,3,new Random(1));
    this.m2.initializeMaze();
    
    this.m4 = new Maze(3,3,new Random(42));
    this.m4.initializeMaze();
    this.m4.boardIndex(1, 0).visited = true;

    this.hm2 = new HashMap<String,String>();
    this.hm2.put("a", "a");
    this.hm2.put("b", "a");
    this.hm2.put("c", "a");
    this.hm2.put("d", "b");
    this.hm2.put("e", "d");
    this.hm2.put("f", "d");
    this.hm2.put("g", "c");
    this.hm2.put("i", "c");
    this.hm2.put("h", "c");

    this.m3 = new Maze(2,2,new Random(2));
    this.m3.initializeMaze();

    this.hm1 = new HashMap<String, String>();
    this.hm1.put("a", "a");
    this.hm1.put("b", "a");
    this.hm1.put("c", "a");
    this.hm1.put("d", "b");
    this.hm1.put("e", "e");

    this.m1Map = new HashMap<String, String>();
    this.m1Map.put("0,0", "0,0");
    this.m1Map.put("1,0", "1,0");
    this.m1Map.put("0,1", "0,1");
    this.m1Map.put("1,1", "1,1");

    this.m1Map2 = new HashMap<String, String>();
    this.m1Map2.put("0,0", "0,0");
    this.m1Map2.put("1,0", "0,0");
    this.m1Map2.put("1,1", "0,0");
    this.m1Map2.put("0,1", "1,1");

    this.m1Map3 = new HashMap<String, String>();
    this.m1Map3.put("1,0", "1,0");
    this.m1Map3.put("0,0", "1,0");
    this.m1Map3.put("1,1", "1,0");
    this.m1Map3.put("0,1", "1,1");

    this.m1Names = new ArrayList<String>(Arrays.asList(
        "0,0", "1,0", "0,1", "1,1"));

    this.c1 = new Cell(0, 0, Color.PINK, this.c2, new EndCell(),
        new EndCell(), new EndCell());
    this.c2 = new Cell(1,0, Color.PINK, new EndCell(), this.c1,
        new EndCell(), this.c4);
    this.c3 = new Cell(0,1, Color.PINK, this.c4, new EndCell(),
        new EndCell(), new EndCell());
    this.c4 = new Cell(1,1, Color.PINK, new EndCell(), this.c3,
        this.c2, new EndCell());

    this.c1_c2 = new Edge(this.c1, this.c2, 1);
    this.c2_c4 = new Edge(this.c2, this.c4, 2);
    this.c3_c4 = new Edge(this.c3, this.c4, 3);

    this.mapConnected = new HashMap<String,String>();
    this.mapConnected.put("0,0", "1,0");
    this.mapConnected.put("1,0", "1,0");
    this.mapConnected.put("0,1", "1,1");
    this.mapConnected.put("1,1", "1,0");

    this.mapUnconnected = new HashMap<String,String>();
    this.mapUnconnected.put("0,0", "1,0");
    this.mapUnconnected.put("1,0", "1,0");
    this.mapUnconnected.put("0,1", "1,1");
    this.mapUnconnected.put("1,1", "1,1");

    this.m = new Maze(2,2, new Random(3), new ArrayList<Cell>(
        Arrays.asList(this.c1, this.c2, this.c3, this.c4)),
        new ArrayList<Edge>(Arrays.asList(this.c1_c2, this.c2_c4,
            this.c3_c4)));

    this.mEdgesNotInit = new Maze(2,2, new Random(20));
  }

  void disconnectCells() {
    this.c1.right = new EndCell();
    this.c1.left = new EndCell();
    this.c1.up = new EndCell();
    this.c1.down = new EndCell();

    this.c2.right = new EndCell();
    this.c2.left = new EndCell();
    this.c2.up = new EndCell();
    this.c2.down = new EndCell();

    this.c3.right = new EndCell();
    this.c3.left = new EndCell();
    this.c3.up = new EndCell();
    this.c3.down = new EndCell();

    this.c4.right = new EndCell();
    this.c4.left = new EndCell();
    this.c4.up = new EndCell();
    this.c4.down = new EndCell();
  }


  HashMapUtilities mUtil = new HashMapUtilities();
  
  //test finding reps
  void testFindRep(Tester t) {
    initData();
    t.checkExpect(this.mUtil.findRep("a", this.hm1), "a");
    t.checkExpect(this.mUtil.findRep("b", this.hm1), "a");
    t.checkExpect(this.mUtil.findRep("c", this.hm1), "a");
    t.checkExpect(this.mUtil.findRep("d", this.hm1), "a");
    t.checkExpect(this.mUtil.findRep("e", this.hm1), "e");

    t.checkExpect(this.mUtil.findRep("a", this.hm2), "a");
    t.checkExpect(this.mUtil.findRep("b", this.hm2), "a");
    t.checkExpect(this.mUtil.findRep("c", this.hm2), "a");
    t.checkExpect(this.mUtil.findRep("d", this.hm2), "a");
    t.checkExpect(this.mUtil.findRep("e", this.hm2), "a");
    t.checkExpect(this.mUtil.findRep("f", this.hm2), "a");
    t.checkExpect(this.mUtil.findRep("g", this.hm2), "a");
    t.checkExpect(this.mUtil.findRep("h", this.hm2), "a");
    t.checkExpect(this.mUtil.findRep("i", this.hm2), "a");
  }


  ArrayListUtils lUtils = new ArrayListUtils();
  
  //test getting a list of cell names from cells
  void testCellNames(Tester t) {
    initData();
    t.checkExpect(lUtils.getCellNames(this.m1.board),
        new ArrayList<String>(Arrays.asList("0,0", "1,0", "2,0",
            "0,1", "1,1", "2,1", "0,2", "1,2", "2,2")));
    t.checkExpect(lUtils.getCellNames(this.m3.board),
        new ArrayList<String>(Arrays.asList("0,0", "1,0",
            "0,1", "1,1")));
    t.checkExpect(lUtils.getCellNames(new ArrayList<Cell>()),
        new ArrayList<String>());
  }

  EdgeComparator eComp = new EdgeComparator();
  
  //compare edges by cost
  void testEdgeComparator(Tester t) {
    initData();
    t.checkExpect(eComp.compare(this.c1_c2, this.c2_c4), -1);
    t.checkExpect(eComp.compare(this.c2_c4, this.c1_c2), 1);
    t.checkExpect(eComp.compare(this.c3_c4, this.c1_c2), 2);
  }

  //add names of cells to a list
  void testAddNames(Tester t) {
    initData();
    ArrayList<String> a = new ArrayList<String>();
    t.checkExpect(a, new ArrayList<String>());
    this.c1.addNames(a);
    t.checkExpect(a.get(0), "0,0");
    this.c2.addNames(a);
    t.checkExpect(a.get(1), "1,0");
  }

  void testSameReps(Tester t) {
    initData();

    //Testing the method on Edges
    t.checkExpect(this.c2_c4.sameReps(this.mapUnconnected), false);
    t.checkExpect(this.c2_c4.sameReps(this.mapConnected), true);
    t.checkExpect(this.c1_c2.sameReps(this.mapUnconnected), true);

    //Testing the method on Cells
    t.checkExpect(this.c1.sameReps(this.c3, this.mapConnected), true);
    t.checkExpect(this.c4.sameReps(this.c2, this.mapConnected), true);
    t.checkExpect(this.c1.sameReps(this.c3, this.mapUnconnected), false);
    t.checkExpect(this.c2.sameReps(this.c4, this.mapUnconnected), false);
  }

  void testUnion(Tester t) {
    initData();

    //Testing on an edge, connecting two smaller trees into one in the HashMap
    t.checkExpect(this.mUtil.findRep("0,1", this.mapUnconnected), "1,1");
    t.checkExpect(this.mUtil.findRep("0,0", this.mapUnconnected), "1,0");
    this.c2_c4.union(this.mapUnconnected);
    t.checkExpect(this.mUtil.findRep("0,1", this.mapUnconnected), "1,1");
    t.checkExpect(this.mUtil.findRep("0,0", this.mapUnconnected), "1,1");

    initData();

    //Testing on a cell, again connecting two smaller trees and checking the Hashmap
    t.checkExpect(this.mUtil.findRep("0,1", this.mapUnconnected), "1,1");
    t.checkExpect(this.mUtil.findRep("0,0", this.mapUnconnected), "1,0");
    this.c1.union(this.c4, this.mapUnconnected);
    t.checkExpect(this.mUtil.findRep("0,1", this.mapUnconnected), "1,1");
    t.checkExpect(this.mUtil.findRep("0,0", this.mapUnconnected), "1,1");
  }

  //get cell from maze board
  void testBoardIndex(Tester t) {
    t.checkExpect(this.m1.boardIndex(0, 0), this.m1.board.get(0));
    t.checkExpect(this.m1.boardIndex(1, 0), this.m1.board.get(1));
    t.checkExpect(this.m1.boardIndex(0, 1), this.m1.board.get(3));
    t.checkExpect(this.m1.boardIndex(2, 2), this.m1.board.get(8));
  }

  //producing cells with refs to end cells
  void testUnconnectedCells(Tester t) {
    ArrayList<Cell> output = this.m1.unconnectedCells();
    EndCell e = new EndCell();
    t.checkExpect(output.get(0).name, "0,0");
    t.checkExpect(output.get(0).left, e);
    t.checkExpect(output.get(0).right, e);
    t.checkExpect(output.get(0).up, e);
    t.checkExpect(output.get(0).down, e);

    t.checkExpect(output.get(8).name, "2,2");
    t.checkExpect(output.get(8).left, e);
    t.checkExpect(output.get(8).right, e);
    t.checkExpect(output.get(8).up, e);
    t.checkExpect(output.get(8).down, e);
  }

  void testConnectCells(Tester t) {
    initData();
    //Unconnecting all of the cells in the maze using a previously testted method
    disconnectCells();

    //Testing on a maze
    t.checkExpect(this.m.boardIndex(0, 0).right, new EndCell());
    t.checkExpect(this.m.boardIndex(1, 0).left, new EndCell());
    t.checkExpect(this.m.boardIndex(1, 1).up, new EndCell());
    this.m.connectCells();
    t.checkExpect(this.m.boardIndex(0, 0).right, this.m.boardIndex(1, 0));
    t.checkExpect(this.m.boardIndex(1, 0).left, this.m.boardIndex(0, 0));
    t.checkExpect(this.m.boardIndex(1, 1).up, this.m.boardIndex(1, 0));

    initData();
    disconnectCells();
    
    //Testing of edges
    t.checkExpect(this.m.boardIndex(0, 0).right, new EndCell());
    t.checkExpect(this.m.boardIndex(1, 0).left, new EndCell());
    t.checkExpect(this.m.boardIndex(1, 1).up, new EndCell());
    this.c1_c2.connectCells();
    this.c2_c4.connectCells();
    t.checkExpect(this.m.boardIndex(0, 0).right, this.m.boardIndex(1, 0));
    t.checkExpect(this.m.boardIndex(1, 0).left, this.m.boardIndex(0, 0));
    t.checkExpect(this.m.boardIndex(1, 1).up, this.m.boardIndex(1, 0));

    initData();
    disconnectCells();
    
    //Testing on cells
    t.checkExpect(this.m.boardIndex(0, 0).right, new EndCell());
    t.checkExpect(this.m.boardIndex(1, 0).left, new EndCell());
    t.checkExpect(this.m.boardIndex(1, 1).up, new EndCell());
    this.c1.connectCells(this.c2);
    this.c2.connectCells(this.c4);
    t.checkExpect(this.m.boardIndex(0, 0).right, this.m.boardIndex(1, 0));
    t.checkExpect(this.m.boardIndex(1, 0).left, this.m.boardIndex(0, 0));
    t.checkExpect(this.m.boardIndex(1, 1).up, this.m.boardIndex(1, 0));
  }

  //put all the edges in a maze
  void testGenAllEdges(Tester t) {
    initData();

    //Testing on a maze
    this.m2.edges = new ArrayList<Edge>();

    t.checkExpect(this.m2.edges, new ArrayList<Edge>());
    this.m2.genAllEdges();
    t.checkExpect(this.m2.edges.size(), 12);
    t.checkExpect(this.m2.edges.get(0).name,
        "0,2-1,2");
    t.checkExpect(this.m2.edges.get(0).cost,
        10);
    t.checkExpect(this.m2.edges.get(11).name,
        "1,2-2,2");
    t.checkExpect(this.m2.edges.get(11).cost,
        99);

    initData();
    //Testing on a Cell
    this.m2.edges = new ArrayList<Edge>();
    t.checkExpect(this.m2.edges, new ArrayList<Edge>());
    this.m2.boardIndex(0, 0).genAllEdges(this.m2);
    t.checkExpect(this.m2.edges.size(), 2);
    t.checkExpect(this.m2.edges.get(0).name, "0,0-1,0");
    t.checkExpect(this.m2.edges.get(0).cost, 17);
    t.checkExpect(this.m2.edges.get(1).name, "0,0-0,1");
    t.checkExpect(this.m2.edges.get(1).cost, 63);
  }

  //put one edge in maze
  void testGenEdge(Tester t) {
    initData();
    //Testing on an EndCell
    this.m2.edges = new ArrayList<Edge>();
    t.checkExpect(this.m2.edges, new ArrayList<Edge>());
    new EndCell().genEdge(this.m2, this.c1);
    t.checkExpect(this.m2.edges, new ArrayList<Edge>());

    //Testing on a Cell
    initData();
    this.m2.edges = new ArrayList<Edge>();
    t.checkExpect(this.m2.edges, new ArrayList<Edge>());
    this.m2.boardIndex(0, 0).genEdge(this.m2, this.m2.boardIndex(1, 0));
    t.checkExpect(this.m2.edges.get(0).c2, this.m2.boardIndex(0, 0));
    t.checkExpect(this.m2.edges.get(0).c1, this.m2.boardIndex(1, 0));
    t.checkExpect(this.m2.edges.get(0).cost, 17);
  }

  //initialize edges
  void testInitializeEdges(Tester t) {
    initData();
    t.checkExpect(this.mEdgesNotInit.edges.size(), 0);
    this.mEdgesNotInit.initializeEdges();
    t.checkExpect(this.mEdgesNotInit.edges.size(), 3);
    t.checkExpect(this.mEdgesNotInit.edges.get(0).name,
        "1,0-1,1");
    t.checkExpect(this.mEdgesNotInit.edges.get(0).cost, 1);
    t.checkExpect(this.mEdgesNotInit.edges.get(1).name,
        "0,0-0,1");
    t.checkExpect(this.mEdgesNotInit.edges.get(1).cost, 36);
    t.checkExpect(this.mEdgesNotInit.edges.get(2).name,
        "0,0-1,0");
    t.checkExpect(this.mEdgesNotInit.edges.get(2).cost, 53);
  }

  //connect all cells without cycles
  void testInitializeMaze(Tester t) {
    initData();
    t.checkExpect(this.mEdgesNotInit.edges.size(), 0);
    this.mEdgesNotInit.initializeMaze();
    t.checkExpect(this.mEdgesNotInit.edges.size(), 3);
    t.checkExpect(this.mEdgesNotInit.boardIndex(0, 0).right,
        this.mEdgesNotInit.boardIndex(1, 0));
    t.checkExpect(this.mEdgesNotInit.boardIndex(0, 0).down,
        this.mEdgesNotInit.boardIndex(0, 1));
    t.checkExpect(this.mEdgesNotInit.boardIndex(1, 0).left,
        this.mEdgesNotInit.boardIndex(0, 0));
    t.checkExpect(this.mEdgesNotInit.boardIndex(1, 0).down,
        this.mEdgesNotInit.boardIndex(1, 1));
    t.checkExpect(this.mEdgesNotInit.boardIndex(0, 1).right,
        new EndCell());
    t.checkExpect(this.mEdgesNotInit.boardIndex(0, 1).up,
        this.mEdgesNotInit.boardIndex(0, 0));
    t.checkExpect(this.mEdgesNotInit.boardIndex(0, 1).right,
        new EndCell());
  }

  //change cell color
  void testSetColor(Tester t) {
    initData();
    t.checkExpect(this.c1.color, Color.PINK);
    this.c1.setColor(Color.RED);
    t.checkExpect(this.c1.color, Color.RED);


    t.checkExpect(this.c4.color, Color.PINK);
    this.c4.setColor(Color.GREEN);
    t.checkExpect(this.c4.color, Color.GREEN);
  }

  //drawing cell
  void testDrawCell(Tester t) {
    initData();
    t.checkExpect(this.c1.drawCell(),
        new AboveImage(
            new AboveImage(this.c1.up.drawCellHelpH(),
                new BesideImage(
                    new BesideImage(this.c1.left.drawCellHelpV(),
                        new RectangleImage(Cnst.blockDimensions,
                            Cnst.blockDimensions, OutlineMode.SOLID, Color.PINK)),
                    this.c1.right.drawCellHelpV())),
            this.c1.down.drawCellHelpH()));
    t.checkExpect(this.c2.drawCell(),
        new AboveImage(
            new AboveImage(this.c2.up.drawCellHelpH(),
                new BesideImage(
                    new BesideImage(this.c2.left.drawCellHelpV(),
                        new RectangleImage(Cnst.blockDimensions,
                            Cnst.blockDimensions, OutlineMode.SOLID, Color.PINK)),
                    this.c2.right.drawCellHelpV())),
            this.c2.down.drawCellHelpH()));
    t.checkExpect(this.c3.drawCell(),
        new AboveImage(
            new AboveImage(this.c3.up.drawCellHelpH(),
                new BesideImage(
                    new BesideImage(this.c3.left.drawCellHelpV(),
                        new RectangleImage(Cnst.blockDimensions,
                            Cnst.blockDimensions, OutlineMode.SOLID, Color.PINK)),
                    this.c3.right.drawCellHelpV())),
            this.c3.down.drawCellHelpH()));
  }

  void testDrawCellHelpH(Tester t) {
    initData();
    EndCell e = new EndCell();
    //Testing on a Cell
    t.checkExpect(this.c1.drawCellHelpH(), new EmptyImage());

    //Testing on an EndCell
    t.checkExpect(e.drawCellHelpH(),
        new LineImage(new Posn(Cnst.blockDimensions,0), Color.BLACK));
    t.checkExpect(e.drawCellHelpH(),
        new LineImage(new Posn(Cnst.blockDimensions,0), Color.BLACK));
  }

  void testDrawCellHelpV(Tester t) {
    initData();
    EndCell e = new EndCell();
    //Testing on a Cell
    t.checkExpect(this.c1.drawCellHelpV(), new EmptyImage());

    //Testing on an EndCell
    t.checkExpect(e.drawCellHelpV(),
        new LineImage(new Posn(0, Cnst.blockDimensions), Color.BLACK));
    t.checkExpect(e.drawCellHelpV(),
        new LineImage(new Posn(0, Cnst.blockDimensions), Color.BLACK));
  }

  //drawing rows
  void testDrawRow(Tester t) {
    initData();
    WorldImage m = new EmptyImage();
    for (int i = 0; i < 2; i++) {
      m = new BesideImage(m, this.m.boardIndex(i, 0).drawCell());
    }
    t.checkExpect(this.m.drawRow(0), m);

    WorldImage m2 = new EmptyImage();
    for (int i = 0; i < 3; i++) {
      m2 = new BesideImage(m2, this.m2.boardIndex(i, 0).drawCell());
    }
    t.checkExpect(this.m2.drawRow(0), m2);

    WorldImage m3 = new EmptyImage();
    for (int i = 0; i < 2; i++) {
      m3 = new BesideImage(m3, this.m3.boardIndex(i, 0).drawCell());
    }
    t.checkExpect(this.m3.drawRow(0), m3);
  }

  //drawing mazes
  void testDrawWorld(Tester t) {
    initData();
    WorldImage m = new EmptyImage();
    for (int i = 0; i < 2; i++) {
      m = new AboveImage(m, this.m.drawRow(i));
    }
    t.checkExpect(this.m.drawWorld(), m);

    WorldImage m2 = new EmptyImage();
    for (int i = 0; i < 3; i++) {
      m2 = new AboveImage(m2, this.m2.drawRow(i));
    }
    t.checkExpect(this.m2.drawWorld(), m2);

    WorldImage m3 = new EmptyImage();
    for (int i = 0; i < 2; i++) {
      m3 = new AboveImage(m3, this.m3.drawRow(i));
    }
    t.checkExpect(this.m3.drawWorld(), m3);
  }

  //make a scene
  void testMakeScene(Tester t) {
    initData();
    WorldScene s = this.m.getEmptyScene();
    //make the image to draw \/  \/
    WorldImage m = this.m.drawWorld();
    WorldImage m2 = this.m2.drawWorld();
    WorldImage m3 = this.m3.drawWorld();
    s.placeImageXY(m, 600, 500);

    t.checkExpect(this.m.makeScene(), s);
    s = this.m.getEmptyScene();
    s.placeImageXY(m2, 600, 500);
    t.checkExpect(this.m2.makeScene(), s);
    s = this.m.getEmptyScene();
    s.placeImageXY(m3, 600, 500);
    t.checkExpect(this.m3.makeScene(), s);

  }

  //big band invoker
  void testBigBang(Tester t) {
    initData();
    Maze m = this.runMaze;
    int worldWidth = 1200;
    int worldHeight = 1000;
    double tickRate = .015;
    m.bigBang(worldWidth, worldHeight, tickRate);
  }




}