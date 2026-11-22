package io.github.scott2100.pacman;

import javax.swing.JFrame;

public class PacManFrame {

	public PacManFrame() {
		JFrame frame = new JFrame();
		PacManPanel pacmanPanel = new PacManPanel();
		frame.add(pacmanPanel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		new PacManFrame();
	}
}
