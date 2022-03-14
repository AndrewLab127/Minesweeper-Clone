package Mine;

import Mine.Cell;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Board extends JPanel {
  
  private final int BOARD_WIDTH = 30;
  private final int BOARD_HEIGHT = 18;
  private final double DENSITY_FACTOR = 0.1;
  private final int MAX_MINES = 120;
  
  private Cell board[][];
  public Timer timer;
  private JLabel statusbar;
  private final int PERIOD_INTERVAL = 300;
  private int numMinesLeft;
  private List<Cell> checked = new ArrayList<Cell>();
  private long tStart;
  
  public Timer getTimer() {
    return this.timer;
  }
  
  public Board(Minesweeper parent) {
    initBoard(parent);
  }
  
  private void initBoard(Minesweeper parent) {
    setFocusable(true);
    statusbar = parent.getStatusBar();
    addMouseListener(new MAdapter());
    //addKeyListener(new MAdapter());
  }
  
  //need a size for each cell
  private int squareWidth() {
    return (int)getSize().getWidth() / BOARD_WIDTH;
  }
  
  private int squareHeight() {
    return (int)getSize().getHeight() / BOARD_HEIGHT;
  }
  
  void start() {
    board = new Cell[BOARD_WIDTH][BOARD_HEIGHT];
    this.checked = new ArrayList<Cell>();
    
    clearBoard();
    
    timer = new Timer(PERIOD_INTERVAL, new GameCycle());
    timer.start();
  }
  
  private void clearBoard() {
    
    checked.clear();
    this.numMinesLeft = 0;
    
    for(int i = 0; i < BOARD_WIDTH; i++) {
      for(int k = 0; k < BOARD_HEIGHT; k++) {
        board[i][k] = new Cell(i, k);
      }
    }
  }
  
  private void placeMines() {
    clearBoard();
    
    //placing mines
    while(numMinesLeft < MAX_MINES) {
      for(int i = 0; i < BOARD_WIDTH; i++) {
        for(int k = 0; k < BOARD_HEIGHT; k++) {
          if(!board[i][k].isMine) {
            if(Math.random() > DENSITY_FACTOR) {
              board[i][k].isMine = false;
            } else {
              board[i][k].isMine = true;
              if(++this.numMinesLeft >= MAX_MINES) {
                return;
              }
            }
          }
        }
      }
    }
  }
  
  private void placeNumbers() {
    //placing numbers
    for(int i = 0; i < board.length; i++) {
      for(int k = 0; k < board[i].length; k++) {
        setNumAdjacentMines(board[i][k], i, k);
      }
    }
  }

  private void boardSetUp() {
    placeMines();
    placeNumbers();
  }
  
  private void gameOver() {
    revealBoard();
    statusbar.setText("You Lose!");
  }
  
  private boolean boundsOkay(int x, int y) {
    if(x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {
      return false;
    }
    return true;
  }
  
  private int[][] getAdjacentCells(int x, int y) {
    return new int[][] {
      {x-1, y-1},
      {x, y-1},
      {x+1, y-1},
      {x-1, y},
      {x+1, y},
      {x-1, y+1},
      {x, y+1},
      {x+1, y+1}
    };
  }
  
  private void setNumAdjacentMines(Cell c, int x, int y) {
    int numMines = 0;
    
    int[][] adjacentCoords = getAdjacentCells(x, y);
    
    for(int i = 0; i < adjacentCoords.length; i++) {
      
      int cellX = adjacentCoords[i][0]; int cellY = adjacentCoords[i][1];
      
      if(boundsOkay(cellX, cellY)) {
        if(board[cellX][cellY].isMine) {
          numMines++;
        }
      }
    }
    
    c.adjacentMines = numMines;
  }
  
  private void revealCellHelper(Cell c, List<Cell> checked) {
    checked.add(c);
    
    if(!c.isHidden || c.isMine) {
      return;
    }
    
    c.isHidden = false;
    
    if(c.adjacentMines > 0) {
      return;
    }
    
    if(c.adjacentMines == 0) {
      int[][] adjacentCoords = getAdjacentCells(c.x, c.y);

      //trying to go through the valid adjacent mines and reveal them with recursion
      for(int i = 0; i < adjacentCoords.length; i++) {
        
        int cellX = adjacentCoords[i][0]; int cellY = adjacentCoords[i][1];
        
        if(boundsOkay(cellX, cellY)) {
          if(!checked.contains(board[cellX][cellY])) {
            revealCellHelper(board[cellX][cellY], checked);
          }
        }
      }
    }
  }
  
  private void revealCell(Cell c, List<Cell> checked) {
    checked.add(c);
    
    //if cell is already revealed do nothing
    if(!c.isHidden) {
      return;
    }
    
    //if cell is mine game is over
    if(c.isMine) {
      //run game over method
      gameOver();
      return;
    }
    
    c.isHidden = false;
    
    //if cell has mines next to it, cell becomes revealed
    if(c.adjacentMines > 0) {
      return;
    }
    
    //if cell has no mines next to it, we have to reveal all the cells
    //that are around it, and then keep going until we find a cells that are
    //touching mines. Stupid af idk wtf to do
    if(c.adjacentMines == 0) {
      //write recursive method
      
      int[][] adjacentCoords = getAdjacentCells(c.x, c.y);

      //trying to go through the valid adjacent mines and reveal them with recursion
      for(int i = 0; i < adjacentCoords.length; i++) {
        
        int cellX = adjacentCoords[i][0]; int cellY = adjacentCoords[i][1];
        
        if(boundsOkay(cellX, cellY)) {
          if(!checked.contains(board[cellX][cellY])) {
            revealCellHelper(board[cellX][cellY], checked);
          }
        }
      }
    }
  }
  
  private void revealBoard() {
    for(int i = 0; i < board.length; i++) {
      for(int k = 0; k < board[i].length; k++) {
        board[i][k].isHidden = false;
      }
    }
  }
 
  /**
   * Reveals cells like with mouse 1 + 3 click
   * @param c Initial cell to reveal
   * @param checked List of checked cells
   */
  private void revealAllAdjacentCells(Cell c, List<Cell> checked) {
    if(c.isHidden) return;
    int[][] adjacentCoords = getAdjacentCells(c.x, c.y);
    
    int numAdjacentFlags = 0;
    
    for(int i = 0; i < adjacentCoords.length; i++) {
      
      int cellX = adjacentCoords[i][0]; int cellY = adjacentCoords[i][1];
      
      if(boundsOkay(cellX, cellY)) {
        if(board[cellX][cellY].isFlagged) numAdjacentFlags++;
      }
    }
    
    if(numAdjacentFlags == c.adjacentMines) {
      for(int i = 0; i < adjacentCoords.length; i++) {
        int cellX = adjacentCoords[i][0]; int cellY = adjacentCoords[i][1];
        
        if(boundsOkay(cellX, cellY) && !board[cellX][cellY].isFlagged) {
          revealCell(board[cellX][cellY], checked);
        }
      }
    }
  }
  
  private void flagCell(Cell c) {
    if(!c.isHidden) {
      return;
    }
    
    c.isFlagged = !c.isFlagged;
  }
  
  private int getNumMinesLeft() {
    int numFlags = 0;
    for(int i = 0; i < board.length; i++) {
      for(int k = 0; k < board[i].length; k++) {
        if(board[i][k].isFlagged) numFlags++;
      }
    }
    
    return MAX_MINES - numFlags;
  }
  
  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    doDrawing(g);
  }
  
  private void doDrawing(Graphics g) {
    //offest the top of the board so it doesn't cut off ui
    var size = getSize();
    int boardTop = (int) size.getHeight() - BOARD_HEIGHT * squareHeight();
    
    //draw the board and hidden/revealed cells
    for(int i = 0; i < BOARD_HEIGHT; i++) {
      for(int k = 0; k < BOARD_WIDTH; k++) {
        Cell cell = board[k][i];
        
        if(cell.isHidden) {
          drawHiddenSquare(g, k * squareWidth(), boardTop + i * squareHeight(), cell);
        } else {
          drawRevealedSquare(g, k * squareWidth(), boardTop + i * squareHeight(), cell);
        }
      }
    }
  }
  
  private void drawHiddenSquare(Graphics g, int x, int y, Cell cell) {
    int value = 200; //color value for gray
    int offset = 20; //offset for lighter and darker shades of gray
    Color gray = new Color(value, value, value);
    Color lightGray = new Color(value+offset, value+offset, value+offset);
    Color darkGray = new Color(value-offset, value-offset, value-offset);
    
    g.setColor(gray);
    g.fillRect(x + 1, y + 1, squareWidth()-2, squareHeight()-2);
    
    //drawing bezels
    g.setColor(lightGray);
    g.drawLine(x, y + squareHeight() - 1, x, y);
    g.drawLine(x+1, y + squareHeight() - 1, x+1, y);
    g.drawLine(x, y, x + squareWidth() - 1, y);
    g.drawLine(x, y-1, x + squareWidth() - 1, y-1);
    
    g.setColor(darkGray);
    g.drawLine(x + 1, y + squareHeight() - 1, x + squareWidth() - 1, y + squareHeight() - 1);
    g.drawLine(x + 1, y + squareHeight() - 2, x + squareWidth() - 1, y + squareHeight() - 2);
    g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1, x + squareWidth() - 1, y + 1);
    g.drawLine(x + squareWidth() - 2, y + squareHeight() - 1, x + squareWidth() - 2, y + 1);
    
    if(cell.isFlagged) { //drawing flags
      g.setColor(new Color(255, 0 , 0));
      g.fillRect(x + 3, y + 3, squareWidth() - 6, squareHeight() - 6);
    }
    
  }
  
  private void drawRevealedSquare(Graphics g, int x, int y, Cell cell) {
    int value = 200;
    Color gray = new Color(value, value, value);
    Color black = new Color(0, 0, 0);
    
    g.setColor(gray);
    g.fillRect(x + 1, y + 1, squareWidth()-2, squareHeight()-2);
    
    g.setColor(black);
    g.drawLine(x, y + squareHeight() - 1, x, y);
    g.drawLine(x, y, x + squareWidth() - 1, y);
    g.drawLine(x + 1, y + squareHeight() - 1, x + squareWidth() - 1, y + squareHeight() - 1);
    g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1, x + squareWidth() - 1, y + 1);
    
    if(cell.adjacentMines > 0 && !cell.isMine) {
      Color num;
      switch(cell.adjacentMines) {
        case 1:
          num = Color.blue;
          break;
        case 2:
          num = Color.green.darker().darker();
          break;
        case 3:
          num = Color.red;
          break;
        case 4:
          num = Color.magenta.darker().darker();
          break;
        case 5:
          num = Color.pink.darker().darker();
          break;
        case 6:
          num = Color.cyan.darker().darker();
          break;
        case 7:
          num = Color.black;
          break;
        case 8:
          num = Color.gray;
          break;
        default:
          num = Color.darkGray;
          break;
      }
      g.setColor(num);
      g.drawChars(String.valueOf(cell.adjacentMines).toCharArray(), 0, 1,  (x + squareWidth() / 2) - 3, (y + squareHeight() / 2) + 4);
    }
    if(cell.isMine) {
      g.setColor(black);
      g.fillRect(x + 3, y + 3, squareWidth() - 6, squareHeight() - 6);
    }
  }
  
  private class GameCycle implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      doGameCycle();
    }
  }
  
  private void doGameCycle() {
    update();
    repaint();
  }
  
  private void update() {
    int currTime = (int) ((System.nanoTime() - tStart)/1000000000);
    this.numMinesLeft = getNumMinesLeft();
    statusbar.setText("Time: " + currTime + "       MinesLeft: " + this.numMinesLeft);
  }

  class MAdapter extends MouseAdapter {
    
    int boardTop = (int) getSize().getHeight() - BOARD_HEIGHT * squareHeight();
    
    //Hash function for converting between pixels and game board coords
    private Cell mouseToBoardHash(Point p) {
      return board[p.x/squareWidth()][p.y/squareHeight() + boardTop];
    }
    
    @Override //Called when mouse button is released
    public void mouseReleased(MouseEvent e) {
      
      Point point = e.getPoint();
      Cell c = mouseToBoardHash(point);
      
      int button = e.getButton();
      System.out.println("x: " + point.x + " Board x: " + c.x);
      System.out.println("y: " + point.y + " Board y: " + c.y);
      
      //if checked is empty the game has just begun, and we don't want the first cell
      //the player clicks to be a mine
      if(checked.size() == 0) {
        do {
          boardSetUp();
          c = mouseToBoardHash(point);
        } while(c.isMine || c.adjacentMines != 0);
        tStart = System.nanoTime();
      }
      
      System.out.println("Button: " + String.valueOf(button));
      
      //if mouse 1, reveal cell, else if mouse 2 flag cell
      if(button == 1) {
        revealCell(c, checked);
        System.out.println(c.toString());
      } else if(button == 3) {
        flagCell(c);
      } else if(button == 2) {
        revealAllAdjacentCells(c, checked);
      }
    }
    
  }
  
  
}
