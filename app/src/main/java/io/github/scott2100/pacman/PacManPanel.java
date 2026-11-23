package io.github.scott2100.pacman;

import java.awt.Image;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.*;
import java.awt.Dimension;
import javax.swing.JPanel;

class PacManPanel extends JPanel implements Runnable, KeyListener {

	public static int height = 400;
	public static int width = 400;

	private static int[][] grid = {
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
			{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
			{ 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1 },
			{ 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1 },
			{ 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1 },
			{ 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1 },
			{ 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1 },
			{ 1, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 1 },
			{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
			{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
			{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
			{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
			{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
			{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
			{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
			{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
			{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
			{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
			{ 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
	};

	Maze pacMaze = new Maze(grid);
	PacMan pacMan = new PacMan(20, 20, 20, 3, pacMaze);

	public PacManPanel() {
		this.setPreferredSize(new Dimension(width, height));
		this.setFocusable(true);
		setDoubleBuffered(true);
		addKeyListener(this);
		new Thread(this).start();
	}

	protected void paintComponent(Graphics g) {
		System.out.println("paint component. " + Thread.currentThread().getName());
		Image image = createImage(height, width);
		Graphics g2 = image.getGraphics();

		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, height, width);

		for (int row = 0; row < pacMaze.height(); row++) {
			for (int col = 0; col < pacMaze.width(); col++) {
				if (pacMaze.isWall(row, col)) {
					g2.setColor(Color.BLUE);
					g2.drawRect(col * pacMan.getSize(), row * pacMan.getSize(), pacMan.getSize(), pacMan.getSize());
				} else if (pacMaze.isPill(row, col)) {
					g2.setColor(Color.ORANGE);
					g2.fillOval(((col * pacMan.getSize()) + 7), ((row * pacMan.getSize()) + 7), 5, 5);
				}
			}
		}

		g2.setColor(Color.YELLOW);
		g2.fillRect(pacMan.getPacManX(), pacMan.getPacManY(), pacMan.getSize(), pacMan.getSize());
		g2.dispose();
		g.drawImage(image, 0, 0, null);
	}

	public void run() {
		while (true) {
			repaint();
			pacMan.move();
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// Restore the interrupted flag and exit the loop cleanly
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		System.out.println("key pressed. " + Thread.currentThread().getName());

		int key = e.getKeyCode();

		if (key == KeyEvent.VK_LEFT) {
			pacMan.setQueuedDirection(Direction.LEFT);
		}

		if (key == KeyEvent.VK_RIGHT) {
			pacMan.setQueuedDirection(Direction.RIGHT);
		}

		if (key == KeyEvent.VK_UP) {
			pacMan.setQueuedDirection(Direction.UP);
		}

		if (key == KeyEvent.VK_DOWN) {
			pacMan.setQueuedDirection(Direction.DOWN);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		//
	}

	@Override
	public void keyTyped(KeyEvent e) {
		//
	}

}
