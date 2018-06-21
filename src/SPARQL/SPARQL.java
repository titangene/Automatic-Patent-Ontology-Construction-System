package SPARQL;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

public class SPARQL {
	final static String OWL_File = "./owl/patent_ontology_r.owl";
	final static String OWL_Base = "http://www.owl-ontologies.com/Ontology1529459183.owl#";

	public static void main(String[] args) {
		OntModel ontModel = loadOntology(OWL_File);
		
		String queryString = "prefix base: <" + OWL_Base + ">"
			+ "SELECT *"
			+ "WHERE {"
			+ "?發明人 base:發明 ?專利編號 ."
			+ "FILTER (?發明人 = base:Titan)"
	//				+ "?專利編號 base:專利類型為 ?專利類型."
			+ "}";
	
		Query query = QueryFactory.create(queryString);
		QueryExecution queryExec = QueryExecutionFactory.create(query, ontModel);

		try {
			ResultSet resultSet = queryExec.execSelect();
			ResultSetFormatter.out(System.out, resultSet, query);
			
//			int i = 0;
//			while (resultSet.hasNext()) {
////				QuerySolution solution = resultSet.nextSolution();
////				Literal literal = solution.getLiteral("");
////				System.out.println(literal);
//				
////				System.out.println(resultSet.next().toString());
//				
//				String patentID = resultSet.next().get("專利編號").toString().replace(OWL_Base, "");
//				String inventor = resultSet.next().get("專利類型").toString().replace(OWL_Base, "");
//				System.out.println(patentID + ": " + inventor);
////				i++;
////				if (i >= 40) break;
//			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			queryExec.close();
		}
	}

	public static OntModel loadOntology(String fileName) {
		// Create an empty model
		OntModel ontoModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
		try {
			FileInputStream fileInputStream = new FileInputStream(fileName);
			InputStream inputStream = fileInputStream;
			ontoModel.read(inputStream, "RDF/XML");
			inputStream.close();
			fileInputStream.close();
			System.out.println("Ontology " + fileName + " loaded.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ontoModel;
	}
}