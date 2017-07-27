package OntologyConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.writer.rdfxml.rdfwriter.OWLModelWriter;

public class OntologyConstruction {
	jdbcmysql mysql = new jdbcmysql();
	public void getOntology() {
		try {
			OWLModel owlModel = ProtegeOWL.createJenaOWLModel();
			ContentAnalysis(owlModel);
			
			FileOutputStream fileOutputStream = new FileOutputStream(new File("patent_ontology.owl"));
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
			OWLModelWriter owlModelWriter = new OWLModelWriter(owlModel, owlModel.getTripleStoreModel().getActiveTripleStore(), outputStreamWriter);
			owlModelWriter.write();
			fileOutputStream.flush();
			outputStreamWriter.flush();
			fileOutputStream.close();
			outputStreamWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void ContentAnalysis(OWLModel owlModel) throws Exception {
		// 建立 "專利資訊" Class
		OWLNamedClass patent_information_OWLClass = owlModel.createOWLNamedClass("專利資訊");
		// 在 "專利分類" class 中新增兩個 child class "國際分類號_IPC"、"設計分類號_LOC"
		OWLNamedClass patent_category_OWLClass = owlModel.createOWLNamedClass("專利分類");
		OWLNamedClass IPC_OWLClass = owlModel.createOWLNamedSubclass("國際分類號_IPC", patent_category_OWLClass);	
		OWLNamedClass LOC_OWLClass = owlModel.createOWLNamedSubclass("設計分類號_LOC", patent_category_OWLClass);
		// 在 "專利關係人" class 中新增兩個 child class "發明人"、"申請人"
		OWLNamedClass patent_relationship_person_OWLClass = owlModel.createOWLNamedClass("專利關係人");
		OWLNamedClass inventor_OWLClass = owlModel.createOWLNamedSubclass("發明人", patent_relationship_person_OWLClass);
		OWLNamedClass applicant_OWLClass = owlModel.createOWLNamedSubclass("申請人", patent_relationship_person_OWLClass);
		
		System.out.println("Create Ontology Successfully");
	}
}
