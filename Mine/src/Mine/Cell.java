package Mine;

public class Cell {
  public boolean isHidden;
  public boolean isMine;
  public boolean isFlagged;
  public int adjacentMines;
  public int x;
  public int y;
  public int pixelSize = 25;
  
  public Cell(int x, int y) {
    this.isHidden = true;
    this.isMine = false;
    this.isFlagged = false;
    this.adjacentMines = 0;
    this.x = x;
    this.y = y;
  }
  
  @Override
  public String toString() {
    return "x: " + this.x + ", y: " + this.y + ", isMine = " + this.isMine;
  }
}
