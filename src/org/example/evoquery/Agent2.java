package org.example.evoquery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import org.openrdf.query.BindingSet;

//termparams en params worden arraylists querytries dus size van die params return value ook param. Abstraheer constructeer met een parameterArrayList, mutatie van alle ints/bits met een % kans
public class Agent2 {
	private int returns, score, totalScore;
	private int queryTries;
	private int memorySize;
	private ArrayList<Integer> params;
	ArrayList<Integer> termParams;
	private ArrayList<BindingSet> memory;
	private ArrayList<String> candidatesS, candidatesP, candidatesO, queries;
	private HashSet<String> entailments;
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
		queryTries = 10;
		this.memorySize = memorySize;
		this.params = params;
		this.termParams = termParams;

		memory = initialMemory;
		entailments = new HashSet<String>();
	}

	public ArrayList<Integer> getParams() {
		return params;
	}

	public ArrayList<Integer> getTermParams() {
		return termParams;
	}

	public void mutate() { // change one digit in the params
		Random rand = new Random();

		int nn = rand.nextInt(params.size() + 1);
		if (nn == params.size()) {
			params.add(rand.nextInt(9));
			termParams.add(rand.nextInt(4));
		} else {
			if (rand.nextBoolean())
				termParams.set(rand.nextInt(params.size()), rand.nextInt(4));
			else
				params.set(nn, rand.nextInt(9));
		}
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

	public void resetRound(ArrayList<BindingSet> nm) {
		score = 0;
		totalScore = 0;
		entailments = new HashSet<String>();
		memory = nm;
	}

	public void takeStep(ArrayList<BindingSet> nm) {
		entailments = new HashSet<String>();
		memory = nm;
		buildQueries();
		for (int i = 0; i < queries.size(); i++) {
			try {
				ArrayList<BindingSet> result = Main.rh
						.queryRepo(queries.get(i));
				returns += result.size();
				int n = 0;
				for (int j = 0; j < result.size(); j++) {
					if (n >= (100 / queryTries)) // amount of results to process
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
		for (int i = 0; i < 10; i++)
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

		for (int i = 0; i < queryTries; i++) {
			if (queries.size() >= 10)
				break;

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

	public String insertTerm(int n) { // n should be queryTries
		if (termParams.get(n) == 0)
			return " ?p ";
		else
			return syntax[termParams.get(n) - 1];
	}

	public void entail() { /*
		// Entail using rules RDFS2 and RDFS3
		for (int i = 0; i < memory.size(); i++) {
			if (memory.get(i).getValue("p").toString().contains("domain")) { // entailment
																				// pattern
																				// RDFS2
				for (int j = 0; j < memory.size(); j++)
					if (memory.get(j).getValue("p").toString()
							.equals(memory.get(i).getValue("x").toString())) {
						String a = memory.get(j).getValue("x").toString();
						String b = memory.get(i).getValue("y").toString();
						if (a.substring(0, 4).equals("http"))
							a = "<" + a + ">";
						if (b.substring(0, 4).equals("http"))
							b = "<" + b + ">";
						String etm = a
								+ "  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  "
								+ b;
						if (!entailments.contains(etm)) {
							score++;
							totalScore++;
							entailments.add(etm);
							if (!Main.globalEntailments.contains(etm)
									&& !checkIfEntailmentInDataset(etm)) {
								Main.globalEntailments.add(etm);
								// System.out.println("(RDFS2) "+etm);
							}
						}
					}
			}

			if (memory.get(i).getValue("p").toString()
					.equals("http://www.w3.org/2000/01/rdf-schema#range")) { // entailment
																				// pattern
																				// RDFS3
				for (int j = 0; j < memory.size(); j++)
					if (memory.get(j).getValue("p").toString()
							.equals(memory.get(i).getValue("x").toString())) {
						String a = memory.get(j).getValue("y").toString();
						String b = memory.get(i).getValue("y").toString();
						if (a.substring(0, 4).equals("http"))
							a = "<" + a + ">";
						if (b.substring(0, 4).equals("http"))
							b = "<" + b + ">";
						String etm = a
								+ "  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  "
								+ b;
						if (!entailments.contains(etm)
								&& !checkIfEntailmentInDataset(etm)) {
							score++;
							totalScore++;
							entailments.add(etm);
							if (!Main.globalEntailments.contains(etm)) {
								Main.globalEntailments.add(etm);
								// System.out.println("(RDFS3) "+etm);
							}
						}
					}
			}
			if (memory.get(i).getValue("p").toString()
					.equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")) { // entailment
																					// pattern
																					// RDFS9
				for (int j = 0; j < memory.size(); j++)
					if (memory
							.get(j)
							.getValue("p")
							.toString()
							.equals("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>")
							&& memory.get(j).getValue("y")
									.equals(memory.get(i).getValue("x"))) {
						String a = memory.get(i).getValue("x").toString();
						String b = memory.get(j).getValue("y").toString();
						if (a.substring(0, 4).equals("http"))
							a = "<" + a + ">";
						if (b.substring(0, 4).equals("http"))
							b = "<" + b + ">";
						String etm = a
								+ "  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  "
								+ b;
						if (!entailments.contains(etm)
								&& !checkIfEntailmentInDataset(etm)) {
							score++;
							totalScore++;
							entailments.add(etm);
							if (!Main.globalEntailments.contains(etm)) {
								Main.globalEntailments.add(etm);
								// System.out.println("(RDFS3) "+etm);
							}
						}
					}

			}

		} */
	}

	public boolean checkIfEntailmentInDataset(String entailment) {
		return (Main.rh.queryRepo(
				"SELECT ?x ?p ?y WHERE { " + entailment + " }").size() > 0);
	}
}
