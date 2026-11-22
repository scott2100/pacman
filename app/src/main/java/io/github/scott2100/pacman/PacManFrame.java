package io.github.scott2100.pacman;

import javax.swing.JFrame;
import java.awt.Color;

class PacManFrame {

	public PacManFrame() {
		JFrame frame = new JFrame();
		PacManPanel pacPanel = new PacManPanel();
		frame.add(pacPanel);
		frame.pack();
		frame.setVisible(true);
		frame.getContentPane().setBackground(Color.BLACK);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void main(String[] args) {
		new PacManFrame();
	}
}
