package org.example.evoquery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import org.openrdf.query.BindingSet;

// mutatie van alle ints/bits met een % kans
public class Agent2 {
	private static Entailment ent = new Entailment();
	private int returns, score, totalScore;
	private int memorySize;
	private static int initialQueryTries = 2;
	private ArrayList<Integer> params;
	ArrayList<Integer> termParams;
	private ArrayList<BindingSet> memory;
	private ArrayList<String> candidatesS, candidatesP, candidatesO, queries;
	static String[] syntax = {
			" <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ",
			" <http://www.w3.org/2000/01/rdf-schema#domain> ",
			" <http://www.w3.org/2000/01/rdf-schema#range> ",
			" <http://www.w3.org/2000/01/rdf-schema#subClassOf> " };

	public Agent2(int memorySize, ArrayList<BindingSet> initialMemory) {
		this(memorySize, initialMemory, getRandomParams(9), getRandomParams(4));
	}

	public Agent2(int memorySize, ArrayList<BindingSet> initialMemory,
			ArrayList<Integer> params, ArrayList<Integer> termParams) {
		returns = 0;
		score = 0;
		totalScore = 0;
		
		this.memorySize = memorySize;
		this.params = params;
		this.termParams = termParams;

		memory = initialMemory;
	}

	public ArrayList<Integer> getParams() {
		return params;
	}

	public ArrayList<Integer> getTermParams() {
		return termParams;
	}

	public int getQueryN() {
		return params.size();
	}
	public int getMostFrequentParam() {
		int[] frequencies = new int[9];
		for(int i=0;i<params.size();i++) 
			frequencies[params.get(i)]++;
		int most = -1, result = -1;
		for(int i=0;i<frequencies.length;i++)
			if(frequencies[i] > most) {
				most = frequencies[i];
				result = i;
			}
		return result;
	}
	
	public int getMostFrequentTermParam() {
		int[] frequencies = new int[9];
		for(int i=0;i<termParams.size();i++) 
			frequencies[termParams.get(i)]++;
		int most = -1, result = -1;
		for(int i=0;i<frequencies.length;i++)
			if(frequencies[i] > most) {
				most = frequencies[i];
				result = i;
			}
		return result;
	}
	
	public void mutate() { // change one digit in the params
		Random rand = new Random();
		if (rand.nextInt(10) < 7) {
			if (rand.nextBoolean()) {
				params.add(rand.nextInt(9));
				termParams.add(rand.nextInt(4));
			} else if(params.size() > 1) {
				params.remove(rand.nextInt(params.size()));
				termParams.remove(rand.nextInt(termParams.size()));
			}
		}
		int nn = rand.nextInt(params.size());
		if (rand.nextBoolean())
			termParams.set(rand.nextInt(params.size()), rand.nextInt(4));
		else
			params.set(nn, rand.nextInt(9));
	}

	public int getScore() {
		return score;
	}

	public int getTotalScore() {
		return totalScore;
	}

	public int getReturnsN() {
		return returns;
	}

	public void resetRound() {
		score = 0;
		totalScore = 0;
	}

	public void takeStep(ArrayList<BindingSet> nm) {
		memory = nm;
		buildQueries();
		for (int i = 0; i < queries.size(); i++) {
			try {
				ArrayList<BindingSet> result = Main.rh
						.queryRepo(queries.get(i));
				returns += result.size();
				int n = 0;
				for (int j = 0; j < result.size(); j++) {
					if (n >= (100 / params.size())) // amount of results to process
						break;
					if (memory.contains(result.get(j)))
						continue;
					if (memory.size() >= memorySize)
						memory.remove(0);
					memory.add(result.get(j));
					n++;
				}
			} catch (Exception e) {
			}
		}
		entail();
	}

	public void loadCandidates() {
		candidatesS = new ArrayList<String>();
		candidatesP = new ArrayList<String>();
		candidatesO = new ArrayList<String>();

		for (int i = 0; i < memory.size(); i++) {
			String x = memory.get(i).getValue("x").stringValue();
			String p = memory.get(i).getValue("p").stringValue();
			String y = memory.get(i).getValue("y").stringValue();
			if (!candidatesS.contains(x))
				candidatesS.add(x);
			if (!candidatesP.contains(p))
				candidatesP.add(p);
			if (!candidatesO.contains(y))
				candidatesO.add(y);
		}
	}

	public static ArrayList<Integer> getRandomParams(int n) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		Random r = new Random();
		for (int i = 0; i < initialQueryTries; i++)
			result.add(r.nextInt(n));
		return result;
	}

	public void printQueries() {
		for (int i = 0; i < queries.size(); i++)
			System.out.println(queries.get(i));
	}

	public void buildQueries() {

		loadCandidates();
		queries = new ArrayList<String>();
		String start = "SELECT ?x ?p ?y WHERE { ?x ?p ?y . ";
		String end = ") } LIMIT 205";

		for (int i = 0; i < params.size(); i++) {

			String[] clause = { "?x ", " ?p ", " ?y" };
			int mp = (params.get(i) % 3);
			int qp = (params.get(i) - mp) / 3;

			if (qp != 1)
				clause[1] = insertTerm(i); // insert a Term according to param

			String filter = "";
			switch (qp) {
			case 0:
				filter = " . FILTER (?x = ";
				break;
			case 1:
				filter = " . FILTER (?p = ";
				break;
			case 2:
				filter = " . FILTER (?y = ";
				break;
			}
			switch (mp) {
			case 0:
				if (!candidatesS.isEmpty()) {
					clause[qp] = candidatesS.remove(0);
					if (clause[qp].startsWith("http://"))
						clause[qp] = "<" + clause[qp] + ">";
					else
						clause[qp] = '"' + clause[qp] + '"';
					String query = start + clause[0] + clause[1] + clause[2]
							+ filter + clause[qp] + end;
					queries.add(query);
				}
				break;
			case 1:
				if (!candidatesP.isEmpty()) {
					clause[qp] = candidatesP.remove(0);
					if (clause[qp].startsWith("http://"))
						clause[qp] = "<" + clause[qp] + ">";
					else {
						clause[qp] = '"' + clause[qp] + '"';
						if (qp == 0)
							continue;
					}
					String query = start + clause[0] + clause[1] + clause[2]
							+ filter + clause[qp] + end;
					queries.add(query);
				}
				break;
			case 2:
				if (!candidatesO.isEmpty()) {
					if (qp < 2 && !candidatesO.get(0).startsWith("http://")) // we
																				// want
																				// no
																				// literals
																				// as
																				// subject
																				// or
																				// predicate
						continue;
					clause[qp] = candidatesO.remove(0);
					if (clause[qp].startsWith("http://"))
						clause[qp] = "<" + clause[qp] + ">";
					else
						clause[qp] = '"' + clause[qp] + '"';
					String query = start + clause[0] + clause[1] + clause[2]
							+ filter + clause[qp] + end;
					queries.add(query);
				}
				break;
			}

		}
	}

	public String insertTerm(int n) {
		if (termParams.get(n) == 0)
			return " ?p ";
		else
			return syntax[termParams.get(n) - 1];
	}

	public void entail() {
		totalScore += (ent.entail(ent.createModel(memory)));
	}

	public boolean checkIfEntailmentInDataset(String entailment) {
		return (Main.rh.queryRepo(
				"SELECT ?x ?p ?y WHERE { " + entailment + " }").size() > 0);
	}

}
