import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import tester.Tester;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// Represents a cell
interface ICell {
  boolean equalColor(Color color);
}

// Represents an empty cell
class MtCell implements ICell {
  MtCell() {
  }

  // To determine if the color of the cell is equal to the given color
  public boolean equalColor(Color color) {
    return false;
  }
}

// Represents a single square of the game area
class Cell implements ICell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;
  Color color;
  boolean flooded;
  // The four adjacent cells to this one
  ICell left;
  ICell top;
  ICell right;
  ICell bottom;

  Cell(int x, int y, Color color, boolean flooded) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
    this.left = new MtCell();
    this.top = new MtCell();
    this.right = new MtCell();
    this.bottom = new MtCell();
  }

  // To mutate the left field
  void addLeft(Cell cell) {
    this.left = cell;
  }

  // To mutate the top field
  void addTop(Cell cell) {
    this.top = cell;

  }

  // To mutate the right field
  void addRight(Cell cell) {
    this.right = cell;

  }

  // To mutate the bottom field
  void addBottom(Cell cell) {
    this.bottom = cell;
  }

  // To draw the cell
  RectangleImage drawCell(int cellSize) {
    return new RectangleImage(cellSize, cellSize, OutlineMode.SOLID, this.color);
  }

  // To determine if the color of the cell is equal to the given color
  public boolean equalColor(Color color) {
    return this.color.equals(color);
  }
}

// Represents a game of flood it
class FloodItWorld extends World {
  // Size of the board (Represents both the number of rows and columns)
  int boardSize;
  // Number of colors
  int numColor;
  // Number of user clicks
  int numClicks;
  // Maximum number of permitted user clicks
  int maxClicks;
  // The amount of time passed
  int clock;
  // Random variable
  Random r;
  // All the cells of the game
  ArrayList<ArrayList<Cell>> board;
  // If the user needs the game documentation
  Boolean needHelp;

  // Constructor used for testing purposes
  FloodItWorld(int boardSize, int numColor, Random r) {
    this.boardSize = boardSize;
    if (numColor > 8) {
      this.numColor = 8;
    }
    else {
      this.numColor = numColor;
    }
    this.numClicks = 0;
    this.maxClicks = (int) (this.boardSize * 2.5);
    this.clock = 0;
    this.r = r;
    // Calling the initBoard method to generate the game board
    this.board = this.initBoard();
    // Mutate the left, top, right, and bottom fields of each cell in the board
    this.addNeighbors();
    this.needHelp = false;
  }

  FloodItWorld(int boardSize, int numColor) {
    this.boardSize = boardSize;
    if (numColor > 8) {
      this.numColor = 8;
    }
    else {
      this.numColor = numColor;
    }
    this.numClicks = 0;
    this.maxClicks = (int) ((this.boardSize * 2.5) + (this.numColor / 2));
    this.clock = 0;
    this.r = new Random();
    // Calling the initBoard method to generate the game board
    this.board = this.initBoard();
    // Mutate the left, top, right, and bottom fields of each cell in the board
    this.addNeighbors();
    this.needHelp = false;
  }

  // To generate the game board
  ArrayList<ArrayList<Cell>> initBoard() {
    // The game board
    ArrayList<ArrayList<Cell>> board = new ArrayList<ArrayList<Cell>>();
    // Iterate until the board is correct size
    for (int y = 0; y < this.boardSize; y++) {
      // A row in the game board
      ArrayList<Cell> row = new ArrayList<Cell>();
      // Iterate until the column is the correct size
      for (int x = 0; x < this.boardSize; x++) {
        // Add a cell of a random color to the column
        // The cell is automatically flooded if it is in the top corner
        if (x == 0 && y == 0) {
          row.add(new Cell(x, y, this.getRandomColor(), true));
        }
        else {
          row.add(new Cell(x, y, this.getRandomColor(), false));
        }
      }
      // Add the row to the board
      board.add(row);
    }
    // Return the final game board
    return board;
  }

  // To get a random color for the cell
  Color getRandomColor() {
    // All of the eight possible colors
    ArrayList<Color> colors = new ArrayList<Color>(Arrays.asList(Color.RED, Color.ORANGE,
        Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.GRAY));
    // Generate a random index
    int index = r.nextInt(this.numColor);
    // Return the random color
    return colors.get(index);
  }

  // To add the left, top, right, and bottom values to each cell
  // EFFECT: Mutates the left, top, right, and bottom fields for each cell in the
  // board
  void addNeighbors() {
    // For each row in the board
    for (ArrayList<Cell> row : this.board) {
      // For each cell in the row
      for (Cell cell : row) {
        // The left adjacent cell
        // If the cell is at the begining of the row, there is no cell to the left
        if (row.indexOf(cell) > 0) {
          cell.addLeft(row.get(row.indexOf(cell) - 1));
        }
        // The top adjacent cell
        // If the row is at the top of the board, there is no cell above
        if (this.board.indexOf(row) > 0) {
          cell.addTop(this.board.get(this.board.indexOf(row) - 1).get(row.indexOf(cell)));
        }
        // The right adjacent cell
        // If the cell is at the end of the row, there is no cell to the right
        if (row.indexOf(cell) < this.boardSize - 1) {
          cell.addRight(row.get(row.indexOf(cell) + 1));
        }
        // The bottom adjacent cell
        // If the row is at the bottom of the board, there is no cell below
        if (this.board.indexOf(row) < this.boardSize - 1) {
          cell.addBottom(this.board.get(this.board.indexOf(row) + 1).get(row.indexOf(cell)));
        }
      }
    }
  }

  // To visualize the flood it game
  public WorldScene makeScene() {
    // Mutate the flooded field of the cells
    this.mutateFlooded();

    if (this.needHelp) {
      return this.help();
    }
    // If the user has already won the game, return the winning screen
    if (this.isWinner()) {
      return this.winner();
    }

    WorldScene background = new WorldScene(700, 700);
    // For every column in the board
    for (int column = 0; column < this.boardSize; column++) {
      // For every row in the board
      for (int row = 0; row < this.boardSize; row++) {
        // Place the cell at the desired coordinates
        background.placeImageXY(this.board.get(column).get(row).drawCell(500 / this.boardSize),
            ((500 / this.boardSize) / 2) + ((500 / this.boardSize) * row) + 100,
            ((500 / this.boardSize) / 2) + ((500 / this.boardSize) * column) + 100);
      }
    }

    TextImage helpMessage1 = new TextImage("Enter \"r\" to reset the board", 20, FontStyle.BOLD,
        Color.BLACK);
    TextImage helpMessage2 =  new TextImage("Enter \"h\" to view "
        + "game documentation", 20, FontStyle.BOLD, Color.BLACK);
    // Display the number of current user clicks
    TextImage clickCounter = new TextImage(
        Integer.toString(this.numClicks) + "/" + Integer.toString(this.maxClicks), 25,
        FontStyle.BOLD, Color.BLACK);
    // Display the clock timer
    TextImage clockTimer = new TextImage("Time in seconds: " + Integer.toString(this.clock), 25,
        FontStyle.BOLD, Color.BLACK);
    background.placeImageXY(helpMessage1, 350, 20);
    background.placeImageXY(helpMessage2, 350, 40);
    background.placeImageXY(clockTimer, 350, 75);
    background.placeImageXY(clickCounter, 350, 650);

    // If the number of user clicks is equal to the maximum number of allowed
    // clicks, return the final screen
    if (this.numClicks == this.maxClicks) {
      // Check if the player has won
      if (this.isWinner()) {
        return this.winner();
      }
      else {
        return this.loser();
      }
    }
    return background;
  }

  // To determine if the player has won the game
  boolean isWinner() {
    // The number of cells that are not flooded
    int numFalse = 0;
    // Loop through each row in the board
    for (ArrayList<Cell> row : this.board) {
      // Loop through each cell in the row
      for (Cell cell : row) {
        // If the cell is not flooded, increment numFalse
        if (!(cell.flooded)) {
          numFalse++;
        }
      }
    }
    // If the number of cells that are not flooded is 0, the player is a winner
    return (numFalse == 0);
  }

  // To display the losing screen
  WorldScene loser() {
    TextImage text = new TextImage("Loser!", 150, FontStyle.BOLD, Color.BLACK);
    TextImage finalScore = new TextImage("Clicks to solution: " + Integer.toString(this.numClicks),
        25, FontStyle.BOLD, Color.BLACK);
    TextImage helpMessage = new TextImage("Enter \"r\" to reset the board", 25, FontStyle.BOLD,
        Color.BLACK);
    WorldScene background = new WorldScene(700, 700);
    background.placeImageXY(text, 350, 350);
    background.placeImageXY(finalScore, 350, 450);
    background.placeImageXY(helpMessage, 350, 500);
    return background;
  }

  // To display the winning screen
  WorldScene winner() {
    TextImage text = new TextImage("Winner!", 150, FontStyle.BOLD, Color.BLACK);
    TextImage finalScore = new TextImage("Clicks to solution: " + Integer.toString(this.numClicks),
        25, FontStyle.BOLD, Color.BLACK);
    TextImage helpMessage = new TextImage("Enter \"r\" to reset the board", 25, FontStyle.BOLD,
        Color.BLACK);
    WorldScene background = new WorldScene(700, 700);
    background.placeImageXY(text, 350, 350);
    background.placeImageXY(finalScore, 350, 450);
    background.placeImageXY(helpMessage, 350, 500);
    return background;
  }

  // To generate the documentation of the game functionality 
  WorldScene help() {
    WorldScene background = new WorldScene(700, 700);
    
    String title = "FloodIt";
    String helpText1 = "The player must manipulate the grid of colors so that all of the cells";
    String helpText2 = "are of the same color. Starting in the upper left corner, by clicking";
    String helpText3 = "various colors they can change the color of that corner cell to increase";
    String helpText4 = "the size of the area of their control.";
    String helpText5 = "Game functionality:";
    String helpText6 = "Enter \"r\" to reset the game and create a new board.";
    String helpText7 = "Enter \"w\" to increment the size of the game board";
    String helpText8 = "Enter \"s\" to decrement the size of the game board (minimum size = 2)";
    String helpText9 = "Enter \"a\" to increment the number of colors (maximum number = 8)";
    String helpText10 = "Enter \"d\" to decrement the number of colors (minimum number = 2)";
    String helpText11 = "Enter \"h\" to display this screen.";
    String helpText12 = "Enter \"h\" to exit this screen.";
    
    TextImage titleImage = new TextImage(title, 40, Color.BLACK);
    TextImage text1 = new TextImage(helpText1, 20, Color.BLACK);
    TextImage text2 = new TextImage(helpText2, 20, Color.BLACK);
    TextImage text3 = new TextImage(helpText3, 20, Color.BLACK);
    TextImage text4 = new TextImage(helpText4, 20, Color.BLACK);
    TextImage text5 = new TextImage(helpText5, 20, Color.BLACK);
    TextImage text6 = new TextImage(helpText6, 20, Color.BLACK);
    TextImage text7 = new TextImage(helpText7, 20, Color.BLACK);
    TextImage text8 = new TextImage(helpText8, 20, Color.BLACK);
    TextImage text9 = new TextImage(helpText9, 20, Color.BLACK);
    TextImage text10 = new TextImage(helpText10, 20, Color.BLACK);
    TextImage text11 = new TextImage(helpText11, 20, Color.BLACK);
    TextImage text12 = new TextImage(helpText12, 20, Color.BLACK);
    
    background.placeImageXY(titleImage, 350, 40);
    background.placeImageXY(text1, 350, 80);
    background.placeImageXY(text2, 350, 100);
    background.placeImageXY(text3, 350, 120);
    background.placeImageXY(text4, 350, 140);
    background.placeImageXY(text5, 350, 180);
    background.placeImageXY(text6, 350, 200);
    background.placeImageXY(text7, 350, 220);
    background.placeImageXY(text8, 350, 240);
    background.placeImageXY(text9, 350, 260);
    background.placeImageXY(text10, 350, 280);
    background.placeImageXY(text11, 350, 300);
    background.placeImageXY(text12, 350, 320);
    
    
    
    return background;
  }

  // To handle key input and is given the key that has been pressed
  // EFFECT: The board is reset if the "r" key is pressed
  public void onKeyEvent(String key) {
    // If "r" key is pressed, the board will reset
    if (key.equals("r")) {
      this.clock = 0;
      this.numClicks = 0;
      this.r = new Random();
      this.board = this.initBoard();
      this.addNeighbors();
    }
    // If the "w" key is pressed, the board size will increase
    if (key.equals("w")) {
      this.clock = 0;
      this.numClicks = 0;
      this.maxClicks = (int) ((this.boardSize * 2.5) + (this.numColor / 2));
      this.boardSize++;
      this.r = new Random();
      this.board = this.initBoard();
      this.addNeighbors();
    }
    // If the "s" key is pressed, the board size will decrease
    if (key.equals("s")) {
      if (this.boardSize != 2) {
        this.clock = 0;
        this.numClicks = 0;
        this.maxClicks = (int) ((this.boardSize * 2.5) + (this.numColor / 2));
        this.boardSize--;
        this.r = new Random();
        this.board = this.initBoard();
        this.addNeighbors();
      }
    }
    // If the "a" key is pressed, the number of colors will increase (maximum 8
    // colors)
    if (key.equals("a")) {
      if (this.numColor != 8) {
        this.clock = 0;
        this.numClicks = 0;
        this.maxClicks = (int) ((this.boardSize * 2.5) + (this.numColor / 2));
        this.numColor++;
        this.r = new Random();
        this.board = this.initBoard();
        this.addNeighbors();
      }
    }
    // If the "d" key is pressed, the number of colors will decrease (minimum 2
    // colors)
    if (key.equals("d")) {
      if (this.numColor != 2) {
        this.clock = 0;
        this.numClicks = 0;
        this.maxClicks = (int) ((this.boardSize * 2.5) + (this.numColor / 2));
        this.numColor--;
        this.r = new Random();
        this.board = this.initBoard();
        this.addNeighbors();
      }
    }
    // If the "h" key is pressed, documentation of detailed game functionality will
    // be displayed
    if (key.equals("h")) {
      if (! this.needHelp) {
        this.needHelp = true;
      }
      else {
        this.needHelp = false;
      }
    }
  }

  // To get the cell from the board at the given position
  Cell getCell(Posn pos) {
    Cell cell = this.board.get(Math.floorDiv(pos.y - 100, (500 / this.boardSize)))
        .get(Math.floorDiv(pos.x - 100, (500 / this.boardSize)));
    return cell;
  }

  // To handle if a mouse button is pressed
  // EFFECT: Update the game based on where the mouse is clicked
  public void onMousePressed(Posn pos) {
    // Check if the x and y coordinates are within the coordinates of the board
    // boundaries
    if (pos.x >= 100 && pos.x <= 600 && pos.y >= 100 && pos.y <= 600) {
      if (this.numClicks < maxClicks) {
        // Increment the number of user clicks
        this.numClicks++;
        // Get the clicked cell
        Cell cell = this.getCell(pos);
        // Change color of flooded cells
        this.board.get(0).get(0).color = cell.color;
      }
    }
  }

  // To change the flooded field
  // EFFECT: Mutate the field from false to true
  void mutateFlooded() {
    // Loop through each row in the board
    for (ArrayList<Cell> row : this.board) {
      // Loop through each cell in the row
      for (Cell cell : row) {
        // If the cell is flooded, check each of its adjacent cells
        if (cell.flooded) {
          // If the left cell is not an empty cell, and the left cell is the same color,
          // set its flooded field to true using an alias
          if (cell.left != new MtCell() && cell.left.equalColor(cell.color)) {
            Cell cellToMutate = (Cell) cell.left;
            cellToMutate.flooded = true;
          }
          // If the top cell is not an empty cell, and the top cell is the same color,
          // set its flooded field to true using an alias
          if (cell.top != new MtCell() && cell.top.equalColor(cell.color)) {
            Cell cellToMutate = (Cell) cell.top;
            cellToMutate.flooded = true;
          }
          // If the right cell is not an empty cell, and the right cell is the same color,
          // set its flooded field to true using an alias
          if (cell.right != new MtCell() && cell.right.equalColor(cell.color)) {
            Cell cellToMutate = (Cell) cell.right;
            cellToMutate.flooded = true;
          }
          // If the bottom cell is not an empty cell, and the bottom cell is the same
          // color,
          // set its flooded field to true using an alias
          if (cell.bottom != new MtCell() && cell.bottom.equalColor(cell.color)) {
            Cell cellToMutate = (Cell) cell.bottom;
            cellToMutate.flooded = true;
          }
        }
      }
    }
  }

  // To change the color of all flooded cells to the given color
  // EFFECT: Mutates the color field of a cell to the given color
  void changeFloodedCellColor(Color color) {
    for (ArrayList<Cell> row : this.board) {
      for (Cell cell : row) {
        // If the cell is flooded, change its color to the given color
        if (cell.flooded) {
          cell.color = color;
        }
      }
    }
  }

  // To handle clock ticking
  // EFFECT: Mutates the clock field by incrementing the time and changes the
  // colors of the cells
  public void onTick() {
    // Create a waterfall effect
    this.changeFloodedCellColor(this.board.get(0).get(0).color);
    // Update the clock
    this.clock++;
  }
}

// Represents examples a flood it world
class ExamplesFloodItWorld {
  ExamplesFloodItWorld() {
  }

  Cell cell1;
  Cell cell2;
  Cell cell3;
  Cell cell4;
  Cell cell5;
  Cell cell6;
  Cell cell7;
  Cell cell8;
  Cell cell9;

  Cell cell10;
  Cell cell11;
  Cell cell12;
  Cell cell13;

  ArrayList<Cell> row1;
  ArrayList<Cell> row2;
  ArrayList<Cell> row3;

  ArrayList<Cell> row4;
  ArrayList<Cell> row5;

  ArrayList<ArrayList<Cell>> board1;
  ArrayList<ArrayList<Cell>> board2;

  void InitCell() {
    this.r = new Random(1);
    this.floodItWorld = new FloodItWorld(3, 4, new Random(1));
    this.cell1 = new Cell(0, 0, Color.YELLOW, true);
    this.cell2 = new Cell(1, 0, Color.RED, false);
    this.cell3 = new Cell(2, 0, Color.ORANGE, false);
    this.cell4 = new Cell(0, 1, Color.ORANGE, false);
    this.cell5 = new Cell(1, 1, Color.RED, false);
    this.cell6 = new Cell(2, 1, Color.RED, false);
    this.cell7 = new Cell(0, 2, Color.ORANGE, false);
    this.cell8 = new Cell(1, 2, Color.YELLOW, false);
    this.cell9 = new Cell(2, 2, Color.GREEN, false);

    this.cell10 = new Cell(0, 0, Color.ORANGE, true);
    this.cell11 = new Cell(1, 0, Color.RED, false);
    this.cell12 = new Cell(0, 1, Color.ORANGE, false);
    this.cell13 = new Cell(1, 1, Color.ORANGE, false);

    this.row1 = new ArrayList<Cell>(Arrays.asList(this.cell1, this.cell2, this.cell3));
    this.row2 = new ArrayList<Cell>(Arrays.asList(this.cell4, this.cell5, this.cell6));
    this.row3 = new ArrayList<Cell>(Arrays.asList(this.cell7, this.cell8, this.cell9));

    this.row4 = new ArrayList<Cell>(Arrays.asList(this.cell10, this.cell11));
    this.row5 = new ArrayList<Cell>(Arrays.asList(this.cell12, this.cell13));

    this.board1 = new ArrayList<ArrayList<Cell>>(Arrays.asList(this.row1, this.row2, this.row3));
    this.board2 = new ArrayList<ArrayList<Cell>>(Arrays.asList(this.row4, this.row5));
  }

  void InitNeighbors() {
    this.cell2.addLeft(this.cell1);
    this.cell3.addLeft(this.cell2);
    this.cell5.addLeft(this.cell4);
    this.cell6.addLeft(this.cell5);
    this.cell8.addLeft(this.cell7);
    this.cell9.addLeft(this.cell8);

    this.cell4.addTop(this.cell1);
    this.cell5.addTop(this.cell2);
    this.cell6.addTop(this.cell3);
    this.cell7.addTop(this.cell4);
    this.cell8.addTop(this.cell5);
    this.cell9.addTop(this.cell6);

    this.cell1.addRight(this.cell2);
    this.cell2.addRight(this.cell3);
    this.cell4.addRight(this.cell5);
    this.cell5.addRight(this.cell6);
    this.cell7.addRight(this.cell8);
    this.cell8.addRight(this.cell9);

    this.cell1.addBottom(this.cell4);
    this.cell2.addBottom(this.cell5);
    this.cell3.addBottom(this.cell6);
    this.cell4.addBottom(this.cell7);
    this.cell5.addBottom(this.cell8);
    this.cell6.addBottom(this.cell9);

    this.cell10.addRight(this.cell11);
    this.cell10.addBottom(this.cell12);

    this.cell11.addLeft(this.cell10);
    this.cell11.addBottom(this.cell13);

    this.cell12.addTop(this.cell10);
    this.cell12.addRight(this.cell13);

    this.cell13.addTop(this.cell11);
    this.cell13.addLeft(this.cell12);
  }

  Random r = new Random(1);

  ArrayList<Color> colors = new ArrayList<Color>(Arrays.asList(Color.RED, Color.ORANGE,
      Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.BLACK));

  RectangleImage cell1Image = new RectangleImage(166, 166, OutlineMode.SOLID, Color.YELLOW);
  RectangleImage cell2Image = new RectangleImage(166, 166, OutlineMode.SOLID, Color.RED);
  RectangleImage cell3Image = new RectangleImage(166, 166, OutlineMode.SOLID, Color.ORANGE);
  RectangleImage cell4Image = new RectangleImage(166, 166, OutlineMode.SOLID, Color.ORANGE);
  RectangleImage cell5Image = new RectangleImage(166, 166, OutlineMode.SOLID, Color.RED);
  RectangleImage cell6Image = new RectangleImage(166, 166, OutlineMode.SOLID, Color.RED);
  RectangleImage cell7Image = new RectangleImage(166, 166, OutlineMode.SOLID, Color.ORANGE);
  RectangleImage cell8Image = new RectangleImage(166, 166, OutlineMode.SOLID, Color.YELLOW);
  RectangleImage cell9Image = new RectangleImage(166, 166, OutlineMode.SOLID, Color.GREEN);

  FloodItWorld floodItWorld = new FloodItWorld(3, 4, new Random(1));

  // To test the addLeft method
  void testAddLeft(Tester t) {
    this.InitCell();
    this.InitNeighbors();

    t.checkExpect(this.cell2.left, cell1);
    t.checkExpect(this.cell3.left, cell2);
    t.checkExpect(this.cell5.left, cell4);
    t.checkExpect(this.cell6.left, cell5);
    t.checkExpect(this.cell8.left, cell7);
    t.checkExpect(this.cell9.left, cell8);
  }

  // To test the addTop method
  void testAddTop(Tester t) {
    this.InitCell();
    this.InitNeighbors();

    t.checkExpect(this.cell4.top, cell1);
    t.checkExpect(this.cell5.top, cell2);
    t.checkExpect(this.cell6.top, cell3);
    t.checkExpect(this.cell7.top, cell4);
    t.checkExpect(this.cell8.top, cell5);
    t.checkExpect(this.cell9.top, cell6);
  }

  // To test the addRight method
  void testAddRight(Tester t) {
    this.InitCell();
    this.InitNeighbors();

    t.checkExpect(this.cell1.right, cell2);
    t.checkExpect(this.cell2.right, cell3);
    t.checkExpect(this.cell4.right, cell5);
    t.checkExpect(this.cell5.right, cell6);
    t.checkExpect(this.cell7.right, cell8);
    t.checkExpect(this.cell8.right, cell9);
  }

  // To test the addBottom method
  void testAddBottom(Tester t) {
    this.InitCell();
    this.InitNeighbors();

    t.checkExpect(this.cell1.bottom, cell4);
    t.checkExpect(this.cell2.bottom, cell5);
    t.checkExpect(this.cell3.bottom, cell6);
    t.checkExpect(this.cell4.bottom, cell7);
    t.checkExpect(this.cell5.bottom, cell8);
    t.checkExpect(this.cell6.bottom, cell9);
  }

  // To test the drawCell method
  boolean testDrawCell(Tester t) {
    this.InitCell();
    this.InitNeighbors();

    return t.checkExpect(this.cell1.drawCell(166), cell1Image)
        && t.checkExpect(this.cell2.drawCell(166), cell2Image)
        && t.checkExpect(this.cell4.drawCell(166), cell3Image)
        && t.checkExpect(this.cell1.drawCell(166), cell8Image);
  }

  // To test the equalColor method
  boolean testEqualColor(Tester t) {
    this.InitCell();
    this.InitNeighbors();
    return t.checkExpect(this.cell1.equalColor(Color.YELLOW), true)
        && t.checkExpect(this.cell1.equalColor(Color.RED), false)
        && t.checkExpect(this.cell2.equalColor(Color.RED), true)
        && t.checkExpect(this.cell2.equalColor(Color.GREEN), false)
        && t.checkExpect(this.cell3.equalColor(Color.ORANGE), true)
        && t.checkExpect(this.cell4.equalColor(Color.ORANGE), true);
  }

  // To test the InitBoard method
  boolean testInitBoard(Tester t) {
    this.InitCell();
    this.InitNeighbors();

    return t.checkExpect(this.floodItWorld.board, board1);
  }

  // To test the getRandomColor method
  boolean testGetRandomColor(Tester t) {
    this.InitCell();
    this.InitNeighbors();
    return t.checkExpect(this.floodItWorld.getRandomColor(), Color.YELLOW)
        && t.checkExpect(new FloodItWorld(3, 4, new Random(2)).getRandomColor(), Color.GREEN)
        && t.checkExpect(new FloodItWorld(10, 2, new Random(4)).getRandomColor(), Color.ORANGE)
        && t.checkExpect(new FloodItWorld(11, 9, new Random(6)).getRandomColor(), Color.RED)
        && t.checkExpect(new FloodItWorld(3, 8, new Random(9)).getRandomColor(), Color.ORANGE);

  }

  // To test the addNeighbors method
  void testAddNeighbors(Tester t) {
    this.InitCell();
    this.InitNeighbors();

    t.checkExpect(this.floodItWorld.board.get(0).get(0).left, new MtCell());
    t.checkExpect(this.floodItWorld.board.get(0).get(0).top, new MtCell());
    t.checkExpect(this.floodItWorld.board.get(0).get(0).right, this.cell2);
    t.checkExpect(this.floodItWorld.board.get(0).get(0).bottom, this.cell4);
  }

  // To test the makeScene method
  boolean testMakeScene(Tester t) {
    this.InitCell();
    this.InitNeighbors();
    WorldScene background = new WorldScene(700, 700);
    // For every column in the board
    for (int column = 0; column < this.floodItWorld.boardSize; column++) {
      // For every row in the board
      for (int row = 0; row < this.floodItWorld.boardSize; row++) {
        // Place the cell at the desired coordinates
        background.placeImageXY(
            this.floodItWorld.board.get(column).get(row)
                .drawCell(500 / this.floodItWorld.boardSize),
            ((500 / this.floodItWorld.boardSize) / 2) + ((500 / this.floodItWorld.boardSize) * row)
                + 100,
            ((500 / this.floodItWorld.boardSize) / 2)
                + ((500 / this.floodItWorld.boardSize) * column) + 100);
      }
    }

    TextImage helpMessage = new TextImage("Enter \"r\" to reset the board", 20, FontStyle.BOLD,
        Color.BLACK);
    TextImage clickCounter = new TextImage("Time in seconds: 0", 25,
        FontStyle.BOLD, Color.BLACK);
    TextImage clockTimer = new TextImage("Enter \"h\" to view game documentation", 20,
        FontStyle.BOLD, Color.BLACK);
    TextImage clickCounter1 = new TextImage(
        Integer.toString(0) + "/" + Integer.toString(7), 25,
        FontStyle.BOLD, Color.BLACK);
    background.placeImageXY(helpMessage, 350, 20);
    background.placeImageXY(clockTimer, 350, 40);
    background.placeImageXY(clickCounter, 350, 75);
    background.placeImageXY(clickCounter1, 350, 650);

    return t.checkExpect(this.floodItWorld.makeScene(), background);
  }

  // To test the isWinner method
  void testIsWinner(Tester t) {
    this.InitCell();
    this.InitNeighbors();
    t.checkExpect(this.floodItWorld.isWinner(), false);
  }

  // tests the loser method
  void testLoser(Tester t) {
    this.InitCell();
    this.InitNeighbors();
    TextImage text = new TextImage("Loser!", 150, FontStyle.BOLD, Color.BLACK);
    TextImage finalScore = new TextImage("Clicks to solution: " + Integer.toString(0), 25,
        FontStyle.BOLD, Color.BLACK);
    TextImage helpMessage = new TextImage("Enter \"r\" to reset the board", 25, FontStyle.BOLD,
        Color.BLACK);
    WorldScene background = new WorldScene(700, 700);
    background.placeImageXY(text, 350, 350);
    background.placeImageXY(finalScore, 350, 450);
    background.placeImageXY(helpMessage, 350, 500);
    t.checkExpect(this.floodItWorld.loser(), background);
  }

  // tests the winner method
  void testWinner(Tester t) {
    this.InitCell();
    this.InitNeighbors();
    TextImage text = new TextImage("Winner!", 150, FontStyle.BOLD, Color.BLACK);
    TextImage finalScore = new TextImage("Clicks to solution: " + Integer.toString(0), 25,
        FontStyle.BOLD, Color.BLACK);
    TextImage helpMessage = new TextImage("Enter \"r\" to reset the board", 25, FontStyle.BOLD,
        Color.BLACK);
    WorldScene background = new WorldScene(700, 700);
    background.placeImageXY(text, 350, 350);
    background.placeImageXY(finalScore, 350, 450);
    background.placeImageXY(helpMessage, 350, 500);
    t.checkExpect(this.floodItWorld.winner(), background);
  }
  
  // To test the help method
  void testHelp(Tester t) {
    this.InitCell();
    this.InitNeighbors();
    WorldScene background = new WorldScene(700, 700);
    
    String title = "FloodIt";
    String helpText1 = "The player must manipulate the grid of colors so that all of the cells";
    String helpText2 = "are of the same color. Starting in the upper left corner, by clicking";
    String helpText3 = "various colors they can change the color of that corner cell to increase";
    String helpText4 = "the size of the area of their control.";
    String helpText5 = "Game functionality:";
    String helpText6 = "Enter \"r\" to reset the game and create a new board.";
    String helpText7 = "Enter \"w\" to increment the size of the game board";
    String helpText8 = "Enter \"s\" to decrement the size of the game board (minimum size = 2)";
    String helpText9 = "Enter \"a\" to increment the number of colors (maximum number = 8)";
    String helpText10 = "Enter \"d\" to decrement the number of colors (minimum number = 2)";
    String helpText11 = "Enter \"h\" to display this screen.";
    String helpText12 = "Enter \"h\" to exit this screen.";
    
    TextImage titleImage = new TextImage(title, 40, Color.BLACK);
    TextImage text1 = new TextImage(helpText1, 20, Color.BLACK);
    TextImage text2 = new TextImage(helpText2, 20, Color.BLACK);
    TextImage text3 = new TextImage(helpText3, 20, Color.BLACK);
    TextImage text4 = new TextImage(helpText4, 20, Color.BLACK);
    TextImage text5 = new TextImage(helpText5, 20, Color.BLACK);
    TextImage text6 = new TextImage(helpText6, 20, Color.BLACK);
    TextImage text7 = new TextImage(helpText7, 20, Color.BLACK);
    TextImage text8 = new TextImage(helpText8, 20, Color.BLACK);
    TextImage text9 = new TextImage(helpText9, 20, Color.BLACK);
    TextImage text10 = new TextImage(helpText10, 20, Color.BLACK);
    TextImage text11 = new TextImage(helpText11, 20, Color.BLACK);
    TextImage text12 = new TextImage(helpText12, 20, Color.BLACK);
    
    background.placeImageXY(titleImage, 350, 40);
    background.placeImageXY(text1, 350, 80);
    background.placeImageXY(text2, 350, 100);
    background.placeImageXY(text3, 350, 120);
    background.placeImageXY(text4, 350, 140);
    background.placeImageXY(text5, 350, 180);
    background.placeImageXY(text6, 350, 200);
    background.placeImageXY(text7, 350, 220);
    background.placeImageXY(text8, 350, 240);
    background.placeImageXY(text9, 350, 260);
    background.placeImageXY(text10, 350, 280);
    background.placeImageXY(text11, 350, 300);
    background.placeImageXY(text12, 350, 320);
    
    t.checkExpect(this.floodItWorld.help(), background);
  }
  
  
  // To test the onKeyEvent method
  void testOnKeyEvent(Tester t) {
    this.InitCell();
    this.InitNeighbors();
    this.floodItWorld.onKeyEvent("r");
    t.checkExpect(this.floodItWorld.board, this.floodItWorld.board);
    t.checkExpect(this.floodItWorld, this.floodItWorld);
    this.floodItWorld.onKeyEvent("w");
    t.checkExpect(this.floodItWorld.boardSize, 4);
    this.floodItWorld.onKeyEvent("s");
    this.floodItWorld.onKeyEvent("s");
    t.checkExpect(this.floodItWorld.boardSize, 2);
    this.floodItWorld.onKeyEvent("a");
    t.checkExpect(this.floodItWorld, this.floodItWorld);
    t.checkExpect(this.floodItWorld.numColor, 5);
    this.floodItWorld.onKeyEvent("a");
    this.floodItWorld.onKeyEvent("a");
    t.checkExpect(this.floodItWorld.numColor, 7);
    this.floodItWorld.onKeyEvent("d");
    t.checkExpect(this.floodItWorld, this.floodItWorld);
    t.checkExpect(this.floodItWorld.numColor, 6);
    this.floodItWorld.needHelp.equals(false);
    this.floodItWorld.onKeyEvent("h");
    t.checkExpect(this.floodItWorld, this.floodItWorld);
    this.floodItWorld.needHelp.equals(true);
  }

  // tests the getCell method
  void testGetCell(Tester t) {
    this.InitCell();
    this.InitNeighbors();
    t.checkExpect(this.floodItWorld.getCell(new Posn(150, 150)), cell1);
    t.checkExpect(this.floodItWorld.getCell(new Posn(400, 150)), cell2);
    t.checkExpect(this.floodItWorld.getCell(new Posn(500, 150)), cell3);
    t.checkExpect(this.floodItWorld.getCell(new Posn(120, 310)), cell4);
    t.checkExpect(this.floodItWorld.getCell(new Posn(300, 310)), cell5);
    t.checkExpect(this.floodItWorld.getCell(new Posn(450, 310)), cell6);
    t.checkExpect(this.floodItWorld.getCell(new Posn(200, 440)), cell7);
    t.checkExpect(this.floodItWorld.getCell(new Posn(310, 440)), cell8);
    t.checkExpect(this.floodItWorld.getCell(new Posn(440, 440)), cell9);
  }

  // To Test the onMousePressed method
  void testOnMouesePressed(Tester t) {
    this.InitCell();
    this.InitNeighbors();

    this.floodItWorld.onMousePressed(new Posn(150, 150));
    t.checkExpect(this.floodItWorld.numClicks, 1);
    this.floodItWorld.onMousePressed(new Posn(200, 300));
    t.checkExpect(this.floodItWorld.numClicks, 2);
    this.floodItWorld.onMousePressed(new Posn(200, 10000));
    t.checkExpect(this.floodItWorld.numClicks, 2);
    this.floodItWorld.onMousePressed(new Posn(200, 1));
    t.checkExpect(this.floodItWorld.numClicks, 2);
    this.floodItWorld.onMousePressed(new Posn(300, 400));
    t.checkExpect(this.floodItWorld.numClicks, 3);

    this.floodItWorld.onMousePressed(new Posn(400, 150));
    t.checkExpect(this.floodItWorld.board.get(0).get(0).color, Color.RED);
    this.floodItWorld.onMousePressed(new Posn(277, 277));
    t.checkExpect(this.floodItWorld.board.get(0).get(0).color, Color.RED);
    this.floodItWorld.onMousePressed(new Posn(500, 500));
    t.checkExpect(this.floodItWorld.board.get(0).get(0).color, Color.GREEN);
    this.floodItWorld.onMousePressed(new Posn(300, 500));
    t.checkExpect(this.floodItWorld.board.get(1).get(1).color, Color.RED);
    this.floodItWorld.onMousePressed(new Posn(300, 200));
    t.checkExpect(this.floodItWorld.board.get(2).get(2).color, Color.GREEN);


  }
  
  // To Test the mutateFlooded method
  void testMutateFlooded(Tester t) {
    this.InitCell();
    this.InitNeighbors();
    this.floodItWorld.mutateFlooded();
    t.checkExpect(this.floodItWorld.board.get(0).get(0).flooded, true);
    t.checkExpect(this.floodItWorld.board.get(0).get(1).flooded, false);
    t.checkExpect(this.floodItWorld.board.get(0).get(2).flooded, false);
    t.checkExpect(this.floodItWorld.board.get(1).get(0).flooded, false);
    t.checkExpect(this.floodItWorld.board.get(1).get(1).flooded, false);
    t.checkExpect(this.floodItWorld.board.get(1).get(2).flooded, false);
    t.checkExpect(this.floodItWorld.board.get(2).get(0).flooded, false);
    t.checkExpect(this.floodItWorld.board.get(2).get(1).flooded, false);
    t.checkExpect(this.floodItWorld.board.get(2).get(2).flooded, false);
    
    this.cell1 = new Cell(0, 0, Color.YELLOW, true);
    this.cell2 = new Cell(1, 0, Color.RED, true);
    this.cell3 = new Cell(2, 0, Color.ORANGE, true);
    this.cell4 = new Cell(0, 1, Color.ORANGE, true);
    this.cell5 = new Cell(1, 1, Color.RED, true);
    this.cell6 = new Cell(2, 1, Color.RED, true);
    this.cell7 = new Cell(0, 2, Color.ORANGE, false);
    this.cell8 = new Cell(1, 2, Color.YELLOW, true);
    this.cell9 = new Cell(2, 2, Color.GREEN, true);

    this.row1 = new ArrayList<Cell>(Arrays.asList(this.cell1, this.cell2, this.cell3));
    this.row2 = new ArrayList<Cell>(Arrays.asList(this.cell4, this.cell5, this.cell6));
    this.row3 = new ArrayList<Cell>(Arrays.asList(this.cell7, this.cell8, this.cell9));

    this.board1 = new ArrayList<ArrayList<Cell>>(Arrays.asList(this.row1, this.row2, this.row3));
    t.checkExpect(this.board1.get(0).get(0).flooded, true);
    t.checkExpect(this.board1.get(1).get(0).flooded, true);
    t.checkExpect(this.board1.get(2).get(0).flooded, false);
    t.checkExpect(this.board1.get(0).get(1).flooded, true);
    t.checkExpect(this.board1.get(1).get(1).flooded, true);
    t.checkExpect(this.board1.get(2).get(1).flooded, true);
    t.checkExpect(this.board1.get(0).get(2).flooded, true);
    t.checkExpect(this.board1.get(1).get(2).flooded, true);
    t.checkExpect(this.board1.get(2).get(2).flooded, true);
  }
  
  // To test the changeFloodedCellColor method
  void testChangeFloodedCellColor(Tester t) {
    this.InitCell();
    this.InitNeighbors();

    this.floodItWorld.changeFloodedCellColor(Color.BLUE);
    t.checkExpect(this.floodItWorld.board.get(0).get(0).color, Color.BLUE);

    this.floodItWorld.changeFloodedCellColor(Color.YELLOW);
    t.checkExpect(this.floodItWorld.board.get(0).get(0).color, Color.YELLOW);

    this.floodItWorld.changeFloodedCellColor(Color.GREEN);
    t.checkExpect(this.floodItWorld.board.get(0).get(0).color, Color.GREEN);

    this.floodItWorld.changeFloodedCellColor(Color.RED);
    t.checkExpect(this.floodItWorld.board.get(0).get(0).color, Color.RED);
    
    this.floodItWorld.changeFloodedCellColor(Color.BLACK);
    t.checkExpect(this.floodItWorld.board.get(0).get(0).color, Color.BLACK);
    
    this.floodItWorld.changeFloodedCellColor(Color.RED);
    t.checkExpect(this.floodItWorld.board.get(1).get(1).color, Color.RED);
    
    this.floodItWorld.changeFloodedCellColor(Color.YELLOW);
    t.checkExpect(this.floodItWorld.board.get(1).get(1).color, Color.RED);
    t.checkExpect(this.floodItWorld.board.get(2).get(2).color, Color.GREEN);

  }
  
  // To test the onTick
  void testOnTick(Tester t) {
    this.InitCell();
    this.InitNeighbors();
    this.floodItWorld.onTick();
    t.checkExpect(this.floodItWorld.clock, 1);
    t.checkExpect(this.floodItWorld.board.get(0).get(0).color, Color.YELLOW);
    this.floodItWorld.onTick();
    t.checkExpect(this.floodItWorld.clock, 2);
    t.checkExpect(this.floodItWorld.board.get(2).get(2).color, Color.GREEN);
    this.floodItWorld.onTick();
    this.floodItWorld.onTick();
    this.floodItWorld.onTick();
    t.checkExpect(this.floodItWorld.clock, 5);
    t.checkExpect(this.floodItWorld.board.get(1).get(1).color, Color.RED);
  }

  // runs the game by creating a world and calling bigBang
  void testFloodIt(Tester t) {
    this.InitCell();
    this.InitNeighbors();
    FloodItWorld floodItWorld = new FloodItWorld(5, 6);
    floodItWorld.bigBang(700, 700, 1);
  }
}