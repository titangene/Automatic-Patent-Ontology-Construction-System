package OntologyConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.writer.rdfxml.rdfwriter.OWLModelWriter;

public class OntologyConstruction {
	jdbcmysql mysql = new jdbcmysql();
	
	OWLModel owlModel;
	
	OWLNamedClass patent_information_OWLClass;	// Class：專利資訊
	OWLNamedClass patent_category_OWLClass;		// Class：專利分類
	OWLNamedClass IPC_OWLClass;					// Class：國際分類號_IPC
	OWLNamedClass LOC_OWLClass;					// Class：設計分類號_LOC
//	OWLNamedClass patent_relationship_person_OWLClass;	// Class：專利關係人
//	OWLNamedClass inventor_OWLClass;			// Class：發明人
//	OWLNamedClass applicant_OWLClass;			// Class：申請人
	
	OWLDatatypeProperty patent_name_OWLDataProperty;
	OWLDatatypeProperty application_date_OWLDataProperty;
	OWLDatatypeProperty reference_OWLDataProperty;
	OWLDatatypeProperty inventor_OWLDataProperty;
	OWLDatatypeProperty applicant_OWLDataProperty;
	
	public void CreateOntology() {
		try {
			owlModel = ProtegeOWL.createJenaOWLModel();
			CreateOWLClass(owlModel);
			CreateOWLDataProperty(owlModel);
			
			ContentAnalysis(owlModel);
			
			SaveOntology();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void CreateOWLClass(OWLModel owlModel) throws Exception {
		// 建立 "專利資訊" Class
		patent_information_OWLClass = owlModel.createOWLNamedClass("專利資訊");
		// 在 "專利分類" class 中新增兩個 child class "國際分類號_IPC"、"設計分類號_LOC"
		patent_category_OWLClass = owlModel.createOWLNamedClass("專利分類");
		IPC_OWLClass = owlModel.createOWLNamedSubclass("國際分類號_IPC", patent_category_OWLClass);	
		LOC_OWLClass = owlModel.createOWLNamedSubclass("設計分類號_LOC", patent_category_OWLClass);
		// 在 "專利關係人" class 中新增兩個 child class "發明人"、"申請人"
//		patent_relationship_person_OWLClass = owlModel.createOWLNamedClass("專利關係人");
//		inventor_OWLClass = owlModel.createOWLNamedSubclass("發明人", patent_relationship_person_OWLClass);
//		applicant_OWLClass = owlModel.createOWLNamedSubclass("申請人", patent_relationship_person_OWLClass);
	}
	
	private void CreateOWLDataProperty(OWLModel owlModel) throws Exception {
		patent_name_OWLDataProperty = owlModel.createOWLDatatypeProperty("專利名稱");
		application_date_OWLDataProperty = owlModel.createOWLDatatypeProperty("申請日");
		reference_OWLDataProperty = owlModel.createOWLDatatypeProperty("參考文獻");
		inventor_OWLDataProperty = owlModel.createOWLDatatypeProperty("發明人");
		applicant_OWLDataProperty = owlModel.createOWLDatatypeProperty("申請人");
	}
	
	private void CreateOWLIndividual(OWLModel owlModel) throws Exception {
		
	}
	
	
	private void ContentAnalysis(OWLModel owlModel) throws Exception {
		
	}
	
	private void SaveOntology() throws Exception {
		FileOutputStream fileOutputStream = new FileOutputStream(new File("patent_ontology.owl"));
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
		OWLModelWriter owlModelWriter = 
				new OWLModelWriter(owlModel, owlModel.getTripleStoreModel().getActiveTripleStore(), outputStreamWriter);
		owlModelWriter.write();
		fileOutputStream.flush();
		outputStreamWriter.flush();
		fileOutputStream.close();
		outputStreamWriter.close();
		System.out.println("Create Ontology Successfully");
	}
}
