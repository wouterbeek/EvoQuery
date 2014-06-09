package org.example.evoquery;

import java.util.*;

import org.openrdf.query.BindingSet;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.ValidityReport.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.vocabulary.*;

public class Entailment {
	
	private static Resource config = ModelFactory.createDefaultModel().createResource()
			.addProperty(ReasonerVocabulary.PROPsetRDFSLevel, "simple");
	private static Reasoner reasoner = RDFSRuleReasonerFactory.theInstance()
			.create(config);
	
	public static void main(String[] args) {
		String NS = "urn:x-hp-jena:eg/";

		// Build a trivial example data set
		Model rdfsExample = ModelFactory.createDefaultModel();
		Property p = rdfsExample.createProperty(NS, "p");
		Property q = rdfsExample.createProperty(NS, "q");
		
		
		rdfsExample.add(p, RDFS.subPropertyOf, q);
		rdfsExample.createResource(NS + "a").addProperty(p, "foo");

		
		
		Resource config = ModelFactory.createDefaultModel().createResource()
				.addProperty(ReasonerVocabulary.PROPsetRDFSLevel, "simple");
		Reasoner reasoner = RDFSRuleReasonerFactory.theInstance()
				.create(config);
		InfModel infModel = ModelFactory.createInfModel(reasoner, rdfsExample);

		StmtIterator iter = infModel.listStatements();

		// print out the predicate, subject and object of each statement
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement(); // get next statement
			Resource subject = stmt.getSubject(); // get the subject
			Property predicate = stmt.getPredicate(); // get the predicate
			RDFNode object = stmt.getObject(); // get the object

			System.out.print(subject.toString());
			System.out.print(" " + predicate.toString() + " ");
			if (object instanceof Resource) {
				System.out.print(object.toString());
			} else {
				// object is a literal
				System.out.print(" \"" + object.toString() + "\"");
			}

			System.out.println(" .");
		}
	}
	
	public Model createModel(ArrayList<BindingSet> memory) {
		Model theModel = ModelFactory.createDefaultModel();
		for(int i=0;i<memory.size();i++) {
			BindingSet temp = memory.get(i);
			String prop = temp.getValue("p").toString();
			Property pred = theModel.createProperty(temp.getValue("p").toString());
			if(prop.contains("domain")) {
				pred = RDFS.domain;
			} else if(prop.contains("range")) {
				pred = RDFS.range;
			} else if(prop.contains("subClassOf")) {
				pred = RDFS.subClassOf;
			} else if(prop.contains("type")) {
				pred = RDF.type;
			} else if(prop.contains("subPropertyOf")) {
				pred = RDFS.subPropertyOf;
			}
			
			theModel.add(
			theModel.createResource(temp.getValue("x").toString()),
			pred,
			theModel.createResource(temp.getValue("y").toString())
			);			
		}
		return theModel;
	}
	
	public int entail(Model theModel) {
		
		InfModel infmodel = ModelFactory.createInfModel(reasoner, theModel);
		List<Statement> sts = infmodel.listStatements().toList();
		
		//for(int i=0;i<sts.size();i++)
			//System.out.println(sts.get(i).toString());
		//System.out.println(sts.size());
		
		return sts.size();
	}
}
