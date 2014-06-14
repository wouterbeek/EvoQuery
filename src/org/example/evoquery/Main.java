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
	ArrayList<BindingSet> memory;
	public static RepositoryHandler rh;
	public static HashSet<String> globalEntailments = new HashSet<String>();
	public static ArrayList<Integer[]> homogeneityGraphs;
	
	public Main(int popsize) {
		
		initializeRepository();
		initializePopulation(popsize);
	}

	public Main() {
		initializeRepository();
	}
	private void initializeRepository() {
		rh = new RepositoryHandler("repo");
		initialMemory = rh
				.queryRepo("SELECT ?x ?p ?y WHERE { ?x ?p ?y } LIMIT 600000");
		System.out.println("Initialized repository.");
		memory = randomMemory();
	}

	private ArrayList<BindingSet> randomMemory() {
		ArrayList<BindingSet> result = new ArrayList<BindingSet>();
		Random ra = new Random();
		for (int i = 0; i < 100; i++)
			result.add(initialMemory.get(ra.nextInt(600000)));
		return result;
	}

	@SuppressWarnings("unused")
	private void testAgent() {
		Agent2 test = new Agent2(200, initialMemory);
		for (int i = 0; i < initialMemory.size(); i++)
			System.out.println(initialMemory.get(i).getValue("x") + " | "
					+ initialMemory.get(i).getValue("p") + " | "
					+ initialMemory.get(i).getValue("y"));
		test.buildQueries();
		test.printQueries();
	}

	public void initializePopulation(int popsize) {
		homogeneityGraphs = new ArrayList<Integer[]>();
		population = new Agent2[popsize];
		ArrayList<BindingSet> rm = randomMemory();
		for (int i = 0; i < population.length; i++) {
			population[i] = new Agent2(200, rm);
		}
		System.out.println("Initialized population.");

	}

	private void evolvePopulation(int n) {
		System.out.println("Evolving...");
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
		for (int i = 0; i < population.length; i++) {
			population[i].resetRound();
		}
	}

	public static void main(String[] args) {
		Main m = new Main(1000);
		m.evolutionRounds(500, 5);
	}

	public void evolutionRounds(int rounds, int steps) {
		for (int i = 0; i < rounds; i++) {
			takeSteps(steps);
			evolvePopulation(i);
			homogeneityGraphs.add(getHomogeneity());
			//population[0].printQueries();
		}
		
	}

	public void testCode() {
		ArrayList<BindingSet> ress = rh
				.queryRepo("SELECT ?x ?p ?y WHERE { <http://dbpedia.org/resource/Connecticut> ?p ?y } LIMIT 30");
		for (int i = 0; i < ress.size(); i++)
			System.out.println(ress.get(i).getValue("x") + " | "
					+ ress.get(i).getValue("p") + " | "
					+ ress.get(i).getValue("y"));
	}
	
	public Integer[] getHomogeneity() {
		int[] queryNgraph = new int[population.length];
		int[] paramGraph = new int[population.length];
		int[] termParamGraph = new int[population.length];
		Integer[] finalGraph = new Integer[population.length];
		
		for(int i=0; i< population.length; i++) {
			queryNgraph[i] = population[i].getQueryN();
			paramGraph[i] = population[i].getMostFrequentParam();
			termParamGraph[i] = population[i].getMostFrequentTermParam();
		}
		
		for(int i=0; i< population.length; i++) 
			finalGraph[i] = queryNgraph[i] * 9 * 9 + paramGraph[i] * 9 + termParamGraph[i];
		
		return finalGraph;	
	}

	private void takeSteps(int n) {
		for (int j = 0; j < n; j++) {
			ArrayList<BindingSet> nm = randomMemory();
			for (int i = 0; i < population.length; i++)
				population[i].takeStep(nm);
			
			//for (int i = 0; i < population.length; i++)
				//population[i].startEntail();
			
			for (int i = 0; i < population.length; i++)
				population[i].waitEntail();
			
			// System.out.println("Step taken.");
		}

	}

	private class AgentTotalComparator implements Comparator<Object> {

		public int agentCompare(Agent2 o1, Agent2 o2) {
			return (o1.getTotalScore()>o2.getTotalScore() ? -1 : (o1.getTotalScore()==o2.getTotalScore() ? 0 : 1));
		}

		@Override
		public int compare(Object o1, Object o2) {
			return agentCompare((Agent2)o1, (Agent2)o2);
		}
	}
}
