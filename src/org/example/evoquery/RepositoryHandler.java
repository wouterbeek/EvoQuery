package org.example.evoquery;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.openrdf.OpenRDFException;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

public class RepositoryHandler {
	Repository repo;
	RepositoryConnection con;

	public RepositoryHandler(String repositoryPath) {

		File dataDir = new File(repositoryPath);
		MemoryStore store = new MemoryStore(dataDir);
		ForwardChainingRDFSInferencer inf = new ForwardChainingRDFSInferencer(
				store);
		repo = new SailRepository(inf);
		ArrayList<BindingSet> ress = queryRepo("SELECT * WHERE { ?x ?p ?y } LIMIT 30");
		for (int i = 0; i < 30; i++)
			System.out.println(ress.get(i).getValue("x") + " | "
					+ ress.get(i).getValue("p") + " |  "
					+ ress.get(i).getValue("y"));

		try {
			repo.initialize();
			con = repo.getConnection();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<BindingSet> queryRepo(String query) {
		ArrayList<BindingSet> returnSet = new ArrayList<BindingSet>();
		try {
			String queryString = query;

			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
					queryString);
			// System.out.println(tupleQuery.getIncludeInferred());
			TupleQueryResult result = tupleQuery.evaluate();

			try {
				while (result.hasNext()) {
					returnSet.add(result.next());
					// System.out.println(returnSet.get(returnSet.size()-1));
				}
			} finally {
				result.close();
			}
		} catch (OpenRDFException e) {
			e.printStackTrace();
		} finally {

		}
		return returnSet;

	}

	public void addFiletoRepo(String filepath) {
		File file = new File(filepath);
		try {
			con.add(file, "http://example.org/dbpedia_ontology",
					RDFFormat.RDFXML);
		} catch (RDFParseException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
