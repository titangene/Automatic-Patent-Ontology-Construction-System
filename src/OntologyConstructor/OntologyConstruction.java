package OntologyConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
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

import jdbc.DBOperations;
import jdbc.Mysql_Select;

public class OntologyConstruction {
	DBOperations dbOperations = new DBOperations();
	
	OWLModel owlModel;
	
	// Class (類別)
	OWLNamedClass patentInformation_OWLClass;	// 專利資訊
	OWLNamedClass patentID_OWLClass;			// 專利編號
	OWLNamedClass patentCategory_OWLClass;		// 專利分類
	OWLNamedClass IPC_OWLClass;					// 國際分類號_IPC
	OWLNamedClass LOC_OWLClass;					// 設計分類號_LOC
//	OWLNamedClass patent_relationship_person_OWLClass;	// 專利關係人
//	OWLNamedClass inventor_OWLClass;			// 發明人
//	OWLNamedClass applicant_OWLClass;			// 申請人
	
	// DataProperty (資料屬性)
	OWLDatatypeProperty patentName_OWLDataProperty;		// 專利名稱
	OWLDatatypeProperty applicationDate_OWLDataProperty;	// 申請日
	OWLDatatypeProperty reference_OWLDataProperty;			// 參考文獻
	OWLDatatypeProperty inventor_OWLDataProperty;			// 發明人
	OWLDatatypeProperty applicant_OWLDataProperty;			// 申請人
	
	// ObjectProperty (物件屬性)
	OWLObjectProperty isReferencedBy_OWLObjectProperty;	// is_referenced_by (被參考)
	
	// Individual (實例)
	OWLIndividual patentID_OWLIndividual;		// 專利編號
	OWLIndividual patentID_IsReferencedBy_OWLIndividual;	// 專利編號 (被參考)
	
	String patentID;
	String patentName;
	String patentApplicationDate;
	String patentInventor;
	String patentApplicant;
	String patentReferences;
	
	// 正規表達式：找出 參考文獻中 所有的 專利編號 (任何國家、單位的專利編號)
	final String regex_Reference_patentID = "(\\W?)([A-Z]{2,4}[0-9]{1,5}[A-Z-\\/／]?[0-9]{2,}[-(]?[A-Z]?[0-9]+[)]?[A-Z]?[A-Z]?)";
	Pattern pattern;
	Matcher matcher;
	
	final String selectSQL = "select * from crawler";
	
	public void CreateOntology() {
		try {
			owlModel = ProtegeOWL.createJenaOWLModel();
			CreateOWLClass();
			CreateOWLDataProperty();
			CreateOWLObjectProperty();
			
			ContentAnalysis();
			
			SaveOntology_to_OWL();
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
		patentInformation_OWLClass = owlModel.createOWLNamedClass("專利資訊");
		patentID_OWLClass = owlModel.createOWLNamedSubclass("專利編號", patentInformation_OWLClass);	
		// 在 "專利分類" class 中新增兩個 child class "國際分類號_IPC"、"設計分類號_LOC"
		patentCategory_OWLClass = owlModel.createOWLNamedClass("專利分類");
		IPC_OWLClass = owlModel.createOWLNamedSubclass("國際分類號_IPC", patentCategory_OWLClass);	
		LOC_OWLClass = owlModel.createOWLNamedSubclass("設計分類號_LOC", patentCategory_OWLClass);
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
		patentName_OWLDataProperty = owlModel.createOWLDatatypeProperty("專利名稱");
		applicationDate_OWLDataProperty = owlModel.createOWLDatatypeProperty("申請日");
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
		isReferencedBy_OWLObjectProperty = owlModel.createOWLObjectProperty("is_referenced_by(被參考)");
		isReferencedBy_OWLObjectProperty.setDomain(patentID_OWLClass);
		isReferencedBy_OWLObjectProperty.setRange(patentID_OWLClass);
	}
	
	/**
	 * 在 "專利編號" 的實例中設定 "資料屬性"：專利名稱、申請日、發明人、申請人
	 */
	private void PatentID_OWLIndividual_AddDataPropertyValue() throws Exception {
		patentID_OWLIndividual.addPropertyValue(patentName_OWLDataProperty, patentName);
		patentID_OWLIndividual.addPropertyValue(applicationDate_OWLDataProperty, patentApplicationDate);
		patentID_OWLIndividual.addPropertyValue(inventor_OWLDataProperty, patentInventor);
		patentID_OWLIndividual.addPropertyValue(applicant_OWLDataProperty, patentApplicant);
	}
	
	/**
	 * 新增 被參考的 "專利編號" 實例 與 參考該  "專利編號" 實例之間的 "is_referenced_by(被參考)" 物件屬性關聯
	 */
	private void PatentID_IsReferencedBy_PatentID_OWLIndividual_AddObjectPropertyValue() throws Exception {
		patentID_IsReferencedBy_OWLIndividual.addPropertyValue(isReferencedBy_OWLObjectProperty, patentID_OWLIndividual);
	}
	
	/**
	 * 如果已有名為 OWLIndividual_name 的實例，就直接取得 OWLIndividual，反之，在 OWLClass 類別中建立新的 OWLIndividual
	 */
	private OWLIndividual getOWLIndividual(OWLNamedClass OWLClass, String OWLIndividual_name) throws Exception {
		OWLIndividual _OWLIndividual = owlModel.getOWLIndividual(OWLIndividual_name);
		if (_OWLIndividual == null) return OWLClass.createOWLIndividual(OWLIndividual_name);
		else return _OWLIndividual;
	}
	
	private void BuildRelationships_PatentsAreReferencedByPatents() throws Exception {
		//String[] patent_reference_ary = patent_references.split("; ");
		// 正規表達式：找出 參考文獻中 所有的 專利編號 (任何國家、單位的專利編號)
		matcher = SetMatcher(regex_Reference_patentID, patentReferences);
		// 取出符合的項目
		while (matcher.find()) {
		    if (IsPatentID_Regex()) {
		    	String patent_reference_patentID = matcher.group(2);
		    	// 如果已有名為 patent_reference_patentID 的實例，就直接取得 "被參考的專利編號" 實例
		    	// 反之，在 "專利編號" 類別中建立新的 "被參考的專利編號" 實例
		    	patentID_IsReferencedBy_OWLIndividual = getOWLIndividual(patentID_OWLClass, patent_reference_patentID);
		    	// 新增 被參考的 "專利編號" 實例 與 參考該  "專利編號" 實例之間的 "is_referenced_by(被參考)" 物件屬性關聯
		    	PatentID_IsReferencedBy_PatentID_OWLIndividual_AddObjectPropertyValue();
	    	}
		}
	}
	
	private Matcher SetMatcher(String _regex, String matcher_value) throws Exception {
		pattern = Pattern.compile(_regex);
		return pattern.matcher(matcher_value);
	}
	
	private boolean IsPatentID_Regex() {
		// TODO Array PatentID Filter
		return !matcher.group(1).contains(".");
	}
	
	private boolean hasPatentReferences() {
		return patentReferences != null;
	}
	
	private void ContentAnalysis() throws Exception {
		dbOperations.SelectTable(selectSQL, new Mysql_Select() {
			@Override
			public void select(ResultSet rs) throws SQLException {
				int count = 0;
				while (rs.next()) {
					// DB 內都是台灣專利，所以自動加上國碼
					patentID = "TW" + rs.getString("id");
					patentName = rs.getString("name");
					patentInventor = rs.getString("inventor");
					patentApplicant = rs.getString("applicant");
					patentReferences = rs.getString("reference");
					patentApplicationDate = rs.getString("application_date");
					//System.out.println(patent_id + "\t" + patent_name);
					
					try {
						// 如果已有名為 patent_id 的實例，就直接取得 "專利編號" 實例，反之，在 "專利編號" 類別中建立新的 "專利編號" 實例
						patentID_OWLIndividual = getOWLIndividual(patentID_OWLClass, patentID);
						// 在 "專利編號" 的實例中設定 "資料屬性"：專利名稱、申請日、發明人、申請人
						PatentID_OWLIndividual_AddDataPropertyValue();
						// 如果 該筆專利沒有 "參考文獻"，就不會建立 "is_referenced_by(被參考)" 物件屬性 關聯
						if (hasPatentReferences()) BuildRelationships_PatentsAreReferencedByPatents();
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					count++;
					if (count == 1000) break;
				}
			}
		});
	}
	
	/**
	 * 將 Ontology 存成 OWL 檔
	 */
	private void SaveOntology_to_OWL() throws Exception {
		FileOutputStream fileOutputStream = new FileOutputStream(new File("patent_ontology.owl"));
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
		OWLModelWriter owlModelWriter = new OWLModelWriter(owlModel, 
				owlModel.getTripleStoreModel().getActiveTripleStore(), outputStreamWriter);
		owlModelWriter.write();
		fileOutputStream.flush();
		outputStreamWriter.flush();
		fileOutputStream.close();
		outputStreamWriter.close();
		System.out.println("Create Ontology Successfully");
	}
}
