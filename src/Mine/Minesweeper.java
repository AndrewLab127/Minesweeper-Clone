package Mine;

import java.awt.BorderLayout;

import java.awt.EventQueue;
import javax.swing.Timer;

import javax.swing.JFrame;
import javax.swing.JLabel;

import Mine.Board;

public class Minesweeper extends JFrame {
  
  private JLabel statusbar;
  
  public Minesweeper() {
    initUI();
  }
  
  private void initUI() {
    statusbar = new JLabel("Time: ");
    add(statusbar, BorderLayout.NORTH);
    
    var board = new Board(this);
    add(board);
    board.start();
    
    setTitle("Minesweeper");
    setSize(750, 435);
    setResizable(false);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
  }

  public JLabel getStatusBar() {
    return statusbar;
  }
  
  public static void main(String[] args) {
    EventQueue.invokeLater(() -> {
      var game = new Minesweeper();
      game.setVisible(true);
    });
    
    
  }

}
