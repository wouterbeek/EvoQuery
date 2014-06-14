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
