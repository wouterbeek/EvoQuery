package org.example.evoquery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Terminal {

	
	private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	private static String line = "";
	private static Main main = new Main(1000);
	public static void main(String[] args) throws IOException {
		boolean running = true;
		while(running) {
			line = br.readLine();
			switch(line) {
				case "newpop": {
					System.out.print("Enter population size: ");
					int size = Integer.parseInt(br.readLine());
					main.initializePopulation(size);
					System.out.println("Generated new population.");
				}
				case "run": {
					System.out.print("Enter number of rounds: ");
					int rounds = Integer.parseInt(br.readLine());
					System.out.print("Enter number of steps: ");
					int steps = Integer.parseInt(br.readLine());
					main.evolutionRounds(rounds, steps);
					System.out.println("Finished running rounds.");
				}
				case "quit": {
					running = false;
					System.out.println("Exiting...");
					break;
				}
				case "homogeneity": {
					ArrayList<Integer[]> graphs = Main.homogeneityGraphs;
					for(int i =0; i<graphs.size(); i++) {
						Integer[] temp = graphs.get(i);
						String t = "";
						for(int j=0;j<temp.length;j++) 
							t = t+"."+temp[j];
						t += "X";
						System.out.println(t);
					}
					
				}
			}
			
			System.out.println("Program finished.");
		}		
	}

}
