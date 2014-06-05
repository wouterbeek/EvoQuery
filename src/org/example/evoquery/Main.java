package org.example.evoquery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;

import org.openrdf.query.BindingSet;

public class Main {
	Agent2[] population;
	ArrayList<BindingSet> initialMemory;
	public static RepositoryHandler rh;
	public static HashSet<String> globalEntailments = new HashSet<String>();

	public Main() {
		initializeRepository();
	}

	private void initializeRepository() {
		rh = new RepositoryHandler("repo");
		initialMemory = rh
				.queryRepo("SELECT ?x ?p ?y WHERE { ?x ?p ?y } LIMIT 300000");
		System.out.println("Initialized repository.");
	}

	private ArrayList<BindingSet> randomMemory() {
		ArrayList<BindingSet> result = new ArrayList<BindingSet>();
		Random ra = new Random();
		for (int i = 0; i < 100; i++)
			result.add(initialMemory.get(ra.nextInt(300000)));
		return result;
	}

	@SuppressWarnings("unused")
	private void testAgent() {
		Agent2 test = new Agent2(300, initialMemory);
		for (int i = 0; i < initialMemory.size(); i++)
			System.out.println(initialMemory.get(i).getValue("x") + " | "
					+ initialMemory.get(i).getValue("p") + " | "
					+ initialMemory.get(i).getValue("y"));
		test.buildQueries();
		test.printQueries();
	}

	private void initializePopulation() {
		population = new Agent2[1000];
		ArrayList<BindingSet> rm = randomMemory();
		for (int i = 0; i < population.length; i++) {
			population[i] = new Agent2(200, rm);
		}
		System.out.println("Initialized population.");

	}

	private void evolvePopulation(int n) {
		Arrays.sort(population, new AgentTotalComparator());

		int top10score = 0;
		int popscore = 0;
		for (int i = 0; i < 10; i++)
			top10score += population[i].getTotalScore();
		for (int i = 0; i < population.length; i++)
			popscore += population[i].getTotalScore();
		System.out.println(n + ". Topscorer: " + population[0].getTotalScore()
				+ "   Top 10: " + top10score + "   Population: " + popscore);

		for (int i = 0; i < population.length / 10; i++) {
			population[population.length - i - 1] = new Agent2(300,
					initialMemory, population[i].getParams(),
					population[i].getTermParams());
			population[population.length - i - 1].mutate();
		}
		ArrayList<BindingSet> nm = randomMemory();
		for (int i = 0; i < population.length; i++) {
			population[i].resetRound(nm);
		}
		// System.out.println("Evolved population");
	}

	public static void main(String[] args) {
		Main m = new Main();
		m.initializePopulation();
		m.evolutionRounds(500, 5);

	}

	public void evolutionRounds(int rounds, int steps) {
		for (int i = 0; i < rounds; i++) {
			takeSteps(steps);
			evolvePopulation(i);
		}
		population[0].printQueries();
	}

	public void testCode() {
		ArrayList<BindingSet> ress = rh
				.queryRepo("SELECT ?x ?p ?y WHERE { <http://dbpedia.org/resource/Connecticut> ?p ?y } LIMIT 30");
		for (int i = 0; i < ress.size(); i++)
			System.out.println(ress.get(i).getValue("x") + " | "
					+ ress.get(i).getValue("p") + " | "
					+ ress.get(i).getValue("y"));
	}

	private void takeSteps(int n) {
		for (int j = 0; j < n; j++) {
			ArrayList<BindingSet> nm = randomMemory();
			for (int i = 0; i < population.length; i++) {
				population[i].takeStep(nm);
			}
			// System.out.println("Step taken.");
		}

	}

	private class AgentTotalComparator implements Comparator<Agent2> {

		public int compare(Agent2 o1, Agent2 o2) {
			return (o1.getTotalScore() > o2.getTotalScore() ? -1 : (o1
					.getTotalScore() == o2.getTotalScore() ? 0 : 1));
		}
	}
}
