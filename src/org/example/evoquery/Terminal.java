package org.example.evoquery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class Terminal {

	private static BufferedReader br = new BufferedReader(
			new InputStreamReader(System.in));
	private static BufferedWriter bw, log, bw3;

	private static String line = "";
	private static Main main = new Main(100);

	public static void main(String[] args) throws IOException {

		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
				"homogeneity.txt"), "utf-8"));
		log = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
				"log.txt"), "utf-8"));

		boolean running = true;
		while (running) {
			line = br.readLine();

			if (line.equals("newpop")) {
				System.out.print("Enter population size: ");
				int size = Integer.parseInt(br.readLine());
				main.initializePopulation(size);
				System.out.println("Generated new population.");
			} else if (line.equals("run")) {
				System.out.print("Enter number of rounds: ");
				int rounds = Integer.parseInt(br.readLine());
				System.out.print("Enter number of steps: ");
				int steps = Integer.parseInt(br.readLine());
				main.evolutionRounds(rounds, steps);
				System.out.println("Finished running rounds.");
			}
			if (line.equals("exit")) {
				running = false;
				log.close();
				bw.close();
				System.out.println("Exiting...");
				break;
			} else if (line.equals("metrics")) {
				ArrayList<Integer[]> graphs = Main.homogeneityGraphs;
				for (int i = 0; i < graphs.size(); i++) {
					Integer[] temp = graphs.get(i);
					String t = "";
					for (int j = 0; j < temp.length; j++)
						t = t + "." + temp[j];
					t += "X";
					bw.write(t);
					System.out.println("Wrote out metrics.");
				}
			}
		}
		System.out.println("Program finished.");
	}

	public static void writeLog(String s) {
		try {
			log.write(s + "\n");
		} catch (IOException e) {
			System.out.println("Failed to log.");
		}
	}
}
