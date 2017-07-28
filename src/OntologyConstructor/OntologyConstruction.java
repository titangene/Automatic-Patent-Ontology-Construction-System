package OntologyConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.writer.rdfxml.rdfwriter.OWLModelWriter;

public class OntologyConstruction {
	jdbcmysql mysql = new jdbcmysql();
	
	OWLModel owlModel;
	
	// Class (類別)
	OWLNamedClass patent_information_OWLClass;	// 專利資訊
	OWLNamedClass patent_id_OWLClass;			// 專利編號
	OWLNamedClass patent_category_OWLClass;		// 專利分類
	OWLNamedClass IPC_OWLClass;					// 國際分類號_IPC
	OWLNamedClass LOC_OWLClass;					// 設計分類號_LOC
//	OWLNamedClass patent_relationship_person_OWLClass;	// 專利關係人
//	OWLNamedClass inventor_OWLClass;			// 發明人
//	OWLNamedClass applicant_OWLClass;			// 申請人
	
	// DataProperty (資料屬性)
	OWLDatatypeProperty patent_name_OWLDataProperty;		// 專利名稱
	OWLDatatypeProperty application_date_OWLDataProperty;	// 申請日
	OWLDatatypeProperty reference_OWLDataProperty;			// 參考文獻
	OWLDatatypeProperty inventor_OWLDataProperty;			// 發明人
	OWLDatatypeProperty applicant_OWLDataProperty;			// 申請人
	
	// ObjectProperty (物件屬性)
	OWLObjectProperty is_referenced_by_OWLObjectProperty;	// is_referenced_by (被參考)
	
	// Individual (實例)
	OWLIndividual patentID_OWLIndividual;		// 專利編號
	OWLIndividual patentID_is_referenced_by_OWLIndividual;	// 專利編號 (被參考)
	
	String patent_id;
	String patent_name;
	String patent_applicationDate;
	String patent_inventor;
	String patent_applicant;
	String patent_references;
	
	// 正規表達式：找出 參考文獻中 所有的 專利編號 (任何國家、單位的專利編號)
	final String regex = "(\\W?)([A-Z]{2,4}[0-9]{1,5}[A-Z-\\/／]?[0-9]{2,}[-(]?[A-Z]?[0-9]+[)]?[A-Z]?[A-Z]?);?";
	final Pattern pattern = Pattern.compile(regex);
	Matcher matcher;
	
	public void CreateOntology() {
		try {
			owlModel = ProtegeOWL.createJenaOWLModel();
			CreateOWLClass();
			CreateOWLDataProperty();
			CreateOWLObjectProperty();
			
			ContentAnalysis();
			
			SaveOntology();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Class:
	 * - 專利資訊
	 * 	 - 專利編號
	 * - 專利分類
	 * 	 - 國際分類號_IPC
	 * 	 - 設計分類號_LOC
	 */
	private void CreateOWLClass() throws Exception {
		// 建立 "專利資訊" Class 中新增一個 child class "專利編號"
		patent_information_OWLClass = owlModel.createOWLNamedClass("專利資訊");
		patent_id_OWLClass = owlModel.createOWLNamedSubclass("專利編號", patent_information_OWLClass);	
		// 在 "專利分類" class 中新增兩個 child class "國際分類號_IPC"、"設計分類號_LOC"
		patent_category_OWLClass = owlModel.createOWLNamedClass("專利分類");
		IPC_OWLClass = owlModel.createOWLNamedSubclass("國際分類號_IPC", patent_category_OWLClass);	
		LOC_OWLClass = owlModel.createOWLNamedSubclass("設計分類號_LOC", patent_category_OWLClass);
		// 在 "專利關係人" class 中新增兩個 child class "發明人"、"申請人"
//		patent_relationship_person_OWLClass = owlModel.createOWLNamedClass("專利關係人");
//		inventor_OWLClass = owlModel.createOWLNamedSubclass("發明人", patent_relationship_person_OWLClass);
//		applicant_OWLClass = owlModel.createOWLNamedSubclass("申請人", patent_relationship_person_OWLClass);
	}
	
	/**
	 * DataProperty:
	 * - 專利名稱
	 * - 申請日
	 * - 參考文獻
	 * - 發明人
	 * - 申請人
	 */
	private void CreateOWLDataProperty() throws Exception {
		patent_name_OWLDataProperty = owlModel.createOWLDatatypeProperty("專利名稱");
		application_date_OWLDataProperty = owlModel.createOWLDatatypeProperty("申請日");
		reference_OWLDataProperty = owlModel.createOWLDatatypeProperty("參考文獻");
		inventor_OWLDataProperty = owlModel.createOWLDatatypeProperty("發明人");
		applicant_OWLDataProperty = owlModel.createOWLDatatypeProperty("申請人");
	}
	
	/**
	 * ObjectProperty:
	 * - is_referenced_by(被參考)："專利編號" is_referenced_by "專利編號"
	 */
	private void CreateOWLObjectProperty() throws Exception {
		// 建立 "is_referenced_by(被參考)" 物件屬性，Domain 和 Range 都設為 "專利編號"
		// "專利編號" is_referenced_by "專利編號"
		is_referenced_by_OWLObjectProperty = owlModel.createOWLObjectProperty("is_referenced_by(被參考)");
		is_referenced_by_OWLObjectProperty.setDomain(patent_id_OWLClass);
		is_referenced_by_OWLObjectProperty.setRange(patent_id_OWLClass);
	}
	
	/**
	 * 在 "專利編號" 的實例中設定 "資料屬性"：專利名稱、申請日、發明人、申請人
	 */
	private void PatentID_OWLIndividual_SetDataPropertyValue() throws Exception {
		patentID_OWLIndividual.setPropertyValue(patent_name_OWLDataProperty, patent_name);
		patentID_OWLIndividual.setPropertyValue(application_date_OWLDataProperty, patent_applicationDate);
		patentID_OWLIndividual.setPropertyValue(inventor_OWLDataProperty, patent_inventor);
		patentID_OWLIndividual.setPropertyValue(applicant_OWLDataProperty, patent_applicant);
	}
	
	private void ContentAnalysis() throws Exception {
		String selectSQL = "select * from crawler";
		try {
			mysql.stat = mysql.con.createStatement();
			mysql.rs = mysql.stat.executeQuery(selectSQL);
			int count = 0;
			while (mysql.rs.next()) {
				patent_id = "TW" + mysql.rs.getString("id");
				patent_name = mysql.rs.getString("name");
				patent_applicationDate  = mysql.rs.getString("application_date");
				patent_inventor  = mysql.rs.getString("inventor");
				patent_applicant  = mysql.rs.getString("applicant");
				patent_references = mysql.rs.getString("reference");
//				System.out.println(patent_id + "\t" + patent_name);
				
				// 取得該 "專利編號" 的實例，如果沒有就在 "專利編號" 類別中建立新的 "專利編號" 實例
				patentID_OWLIndividual = owlModel.getOWLIndividual(patent_id);
				if (patentID_OWLIndividual == null)
					patentID_OWLIndividual = patent_id_OWLClass.createOWLIndividual(patent_id);
				
				// 在 "專利編號" 的實例中設定 "資料屬性"：專利名稱、申請日、發明人、申請人
				PatentID_OWLIndividual_SetDataPropertyValue();
				
				count++;
				if (count == 1000) break;
			}
		} catch (SQLException e) {
			System.out.println("DropDB Exception :" + e.toString());
		} finally {
			mysql.Close();
		}
	}
	
	/**
	 * 將 Ontology 存成 OWL 檔
	 */
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
