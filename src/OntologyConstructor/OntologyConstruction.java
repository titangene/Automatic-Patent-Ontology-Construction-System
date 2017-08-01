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
	OWLNamedClass patentID_OWLClass;			// 專利編號
	OWLNamedClass patentCategory_OWLClass;		// 專利分類
	OWLNamedClass patentCategory_IPC_OWLClass;	// 國際分類號_IPC
	OWLNamedClass patentCategory_LOC_OWLClass;	// 設計分類號_LOC
	OWLNamedClass patentType_OWLClass;			// 專利類型
	OWLNamedClass patentRelationshipPerson_OWLClass;	// 專利關係人
	OWLNamedClass inventor_OWLClass;			// 發明人
	OWLNamedClass applicant_OWLClass;			// 申請人
	OWLNamedClass applicant_Personal_OWLClass;	// 個人
	OWLNamedClass applicant_Company_OWLClass;	// 企業
	OWLNamedClass applicant_School_OWLClass;	// 學校
	OWLNamedClass country_OWLClass;				// 國家
	OWLNamedClass country_Asia_OWLClass;		// 亞洲
	OWLNamedClass country_Europe_OWLClass;		// 歐洲
	OWLNamedClass country_Africa_OWLClass;		// 非洲
	OWLNamedClass country_NorthAmerica_OWLClass;// 北美洲
	OWLNamedClass country_SouthAmerica_OWLClass;// 南美洲
	OWLNamedClass country_Oceania_OWLClass;		// 大洋洲
	OWLNamedClass country_Other_OWLClass;		// 其他
	
	// DataProperty (資料屬性)
	OWLDatatypeProperty patentName_OWLDataProperty;			// 專利名稱
	OWLDatatypeProperty applicationDate_OWLDataProperty;	// 申請日
	OWLDatatypeProperty reference_OWLDataProperty;			// 參考文獻
	//OWLDatatypeProperty inventor_OWLDataProperty;			// 發明人
	//OWLDatatypeProperty applicant_OWLDataProperty;		// 申請人
	
	// ObjectProperty (物件屬性)
	OWLObjectProperty isReferencedBy_OWLObjectProperty;	// is_referenced_by (被參考)
	OWLObjectProperty patentType_OWLObjectProperty;		// 專利類型為
	
	// Individual (實例)
	OWLIndividual patentID_OWLIndividual;		// 專利編號
	OWLIndividual patentID_IsReferencedBy_OWLIndividual;	// 專利編號 (被參考)
	OWLIndividual patentType_Invention_OWLIndividual;		// 發明 (Invention)
	OWLIndividual patentType_Model_OWLIndividual;			// 新型 (Model)
	OWLIndividual patentType_Design_OWLIndividual;			// 新式樣/設計 (Design)
	
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
			CreateOWLIndividual();
			CreateOWLObjectProperty();
			
			ContentAnalysis();
			
			SaveOntology_to_OWL();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Class:
	 * - 專利編號
	 * - 專利類型
	 * - 專利分類
	 * 	 - 國際分類號_IPC
	 * 	 - 設計分類號_LOC
	 * - 專利關係人
	 *   - 發明人
	 *   - 申請人
	 *     - 個人
	 *     - 企業
	 *     - 學校
	 * - 國家
	 *   - 亞洲
	 *   - 歐洲
	 *   - 非洲
	 *   - 北美洲
	 *   - 南美洲
	 *   - 大洋洲
	 *   - 其他
	 */
	private void CreateOWLClass() throws Exception {
		// 建立 "專利資訊" Class
		patentID_OWLClass = owlModel.createOWLNamedClass("專利編號");	
		// 在 "專利分類" class 中新增兩個 child class "國際分類號_IPC"、"設計分類號_LOC"
		patentCategory_OWLClass = owlModel.createOWLNamedClass("專利分類");
		patentCategory_IPC_OWLClass = owlModel.createOWLNamedSubclass("國際分類號_IPC", patentCategory_OWLClass);	
		patentCategory_LOC_OWLClass = owlModel.createOWLNamedSubclass("設計分類號_LOC", patentCategory_OWLClass);
		// 建立 "專利類型" Class
		patentType_OWLClass = owlModel.createOWLNamedClass("專利類型");
		// 在 "國家" class 中新增多個 child class "亞洲"、"歐洲"、"非洲"、"北美洲"、"南美洲"、"大洋洲"、"其他"
		country_OWLClass = owlModel.createOWLNamedClass("國家");
		country_Asia_OWLClass = owlModel.createOWLNamedSubclass("亞洲", country_OWLClass);
		country_Europe_OWLClass = owlModel.createOWLNamedSubclass("歐洲", country_OWLClass);
		country_Africa_OWLClass = owlModel.createOWLNamedSubclass("非洲", country_OWLClass);
		country_NorthAmerica_OWLClass = owlModel.createOWLNamedSubclass("北美洲", country_OWLClass);
		country_SouthAmerica_OWLClass = owlModel.createOWLNamedSubclass("南美洲", country_OWLClass);
		country_Oceania_OWLClass = owlModel.createOWLNamedSubclass("大洋洲", country_OWLClass);
		country_Other_OWLClass = owlModel.createOWLNamedSubclass("其他", country_OWLClass);
		// 在 "專利關係人" class 中新增兩個 child class "發明人"、"申請人"
		patentRelationshipPerson_OWLClass = owlModel.createOWLNamedClass("專利關係人");
		inventor_OWLClass = owlModel.createOWLNamedSubclass("發明人", patentRelationshipPerson_OWLClass);
		// 在 "申請人" class 中新增三個 child class "個人"、"企業"、"學校"
		applicant_OWLClass = owlModel.createOWLNamedSubclass("申請人", patentRelationshipPerson_OWLClass);
		applicant_Personal_OWLClass = owlModel.createOWLNamedSubclass("個人", applicant_OWLClass);
		applicant_Company_OWLClass = owlModel.createOWLNamedSubclass("企業", applicant_OWLClass);
		applicant_School_OWLClass = owlModel.createOWLNamedSubclass("學校", applicant_OWLClass);
	}
	
	/**
	 * DataProperty:
	 * - 專利名稱
	 * - 申請日
	 * - 參考文獻
	 */
	private void CreateOWLDataProperty() throws Exception {
		patentName_OWLDataProperty = owlModel.createOWLDatatypeProperty("專利名稱");
		applicationDate_OWLDataProperty = owlModel.createOWLDatatypeProperty("申請日");
		reference_OWLDataProperty = owlModel.createOWLDatatypeProperty("參考文獻");
		//inventor_OWLDataProperty = owlModel.createOWLDatatypeProperty("發明人");
		//applicant_OWLDataProperty = owlModel.createOWLDatatypeProperty("申請人");
	}
	
	/**
	 * ObjectProperty:
	 * - is_referenced_by(被參考)："專利編號" is_referenced_by "專利編號"
	 * - 專利類型："專利編號" 專利類型為 "專利類型"
	 */
	private void CreateOWLObjectProperty() throws Exception {
		// 建立 "is_referenced_by(被參考)" 物件屬性，Domain 和 Range 都設為 "專利編號"
		// "專利編號" is_referenced_by "專利編號"
		isReferencedBy_OWLObjectProperty = owlModel.createOWLObjectProperty("is_referenced_by(被參考)");
		isReferencedBy_OWLObjectProperty.setDomain(patentID_OWLClass);
		isReferencedBy_OWLObjectProperty.setRange(patentID_OWLClass);
		// 建立 "專利類型為" 物件屬性，Domain 設為 "專利編號"，Range 設為 "專利類型"
		// "專利編號" 專利類型為 "專利類型"
		patentType_OWLObjectProperty = owlModel.createOWLObjectProperty("專利類型為");
		patentType_OWLObjectProperty.setDomain(patentID_OWLClass);
		patentType_OWLObjectProperty.setRange(patentType_OWLClass);
	}
	
	/**
	 * Individual
	 * - 發明(Invention)
	 * - 新型(Model)
	 * - 新式樣/設計(Design)
	 */
	private void CreateOWLIndividual() throws Exception {
		patentType_Invention_OWLIndividual = getOWLIndividual(patentType_OWLClass, "發明(Invention)");
		patentType_Model_OWLIndividual = getOWLIndividual(patentType_OWLClass, "新型(Model)"); 
		patentType_Design_OWLIndividual = getOWLIndividual(patentType_OWLClass, "新式樣/設計(Design)");
	}
	
	/**
	 * 在 "專利編號" 的實例中設定 "資料屬性"：專利名稱、申請日
	 */
	private void PatentID_OWLIndividual_AddDataPropertyValue() throws Exception {
		patentID_OWLIndividual.addPropertyValue(patentName_OWLDataProperty, patentName);
		patentID_OWLIndividual.addPropertyValue(applicationDate_OWLDataProperty, patentApplicationDate);
		//patentID_OWLIndividual.addPropertyValue(inventor_OWLDataProperty, patentInventor);
		//patentID_OWLIndividual.addPropertyValue(applicant_OWLDataProperty, patentApplicant);
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
	
	/**
	 * 如果 該筆專利沒有 "參考文獻"，就不會建立 "is_referenced_by(被參考)" 物件屬性 關聯
	 */
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
		    	
		    	// 如果是台灣專利才建立 "專利類型為" 物件屬性 關聯："專利編號" 專利類型為 "專利類型"
		    	if (IsTW_Patent(patent_reference_patentID))
		    		BuildRelationships_PatentType(patentID_IsReferencedBy_OWLIndividual, patent_reference_patentID);
	    	}
		}
	}
	
	/**
	 * 建立 "專利類型為" 物件屬性 關聯："專利編號" 專利類型為 "專利類型"
	 */
	private void BuildRelationships_PatentType(OWLIndividual _OWLIndividual, String _patentID) throws Exception {
		char patentType = _patentID.charAt(2);
		switch (patentType) {
			case 'I':	// 發明(Invention)
				_OWLIndividual.addPropertyValue(patentType_OWLObjectProperty, patentType_Invention_OWLIndividual);
				break;
			case 'M':	// 新型(Model)
				_OWLIndividual.addPropertyValue(patentType_OWLObjectProperty, patentType_Model_OWLIndividual);
				break;
			case 'D':	// 新式樣/設計(Design)
				_OWLIndividual.addPropertyValue(patentType_OWLObjectProperty, patentType_Design_OWLIndividual);
				break;
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
	
	private boolean IsTW_Patent(String _patentID) {
		return _patentID.contains("TW");
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
						// 建立 "專利類型為" 物件屬性 關聯："專利編號" 專利類型為 "專利類型"
						BuildRelationships_PatentType(patentID_OWLIndividual, patentID);
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
