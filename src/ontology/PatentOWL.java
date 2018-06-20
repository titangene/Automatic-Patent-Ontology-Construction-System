package ontology;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import ontology.owl.*;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.writer.rdfxml.rdfwriter.OWLModelWriter;
import jdbc.DBOperations;
import jdbc.Mysql_Select;

public class PatentOWL implements ICreateOWL {
	public OWLModel owlModel;
	/** 專利的本體類別 */
	public static OntClass ontClass = new OntClass();
	/** 專利的本體資料屬性 */
	public static OntDataProperty ontDataProperty = new OntDataProperty();
	/** 專利的本體實例 */
	public static OntIndividual ontIndividual = new OntIndividual();
	/** 專利的本體物件屬性 */
	public static OntObjectProperty ontObjectProperty = new OntObjectProperty();

	private DBOperations dbOperations = new DBOperations();
	private final String selectSQL = "select * from crawler";
	
	private String OWLFileName = "./OWL/patent_ontology.owl";

	@Override
	public void initOWL() {
		try {
			owlModel = OntModel.getInstance().getOWLModel();

			createOntClass();
			createOntIndividual();
			createOntDataProperty();
			createOntObjectProperty();

			createRelation();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createOntClass() {
		// 建立 "專利編號" Class
		ontClass.patentID = owlModel.createOWLNamedClass("專利編號");
		
		// 在 "專利分類" class 中新增 child class "設計分類號_LOC"
		ontClass.category = owlModel.createOWLNamedClass("專利分類");	
		ontClass.category_LOC = owlModel.createOWLNamedSubclass("設計分類號_LOC", ontClass.category);
		
		// 建立 "專利類型" Class
		ontClass.patentType = owlModel.createOWLNamedClass("專利類型");
		
		// 在 "專利關係人" class 中新增 3 個 child class "發明人"、"申請人"、"代理人"
		ontClass.relationPerson = owlModel.createOWLNamedClass("專利關係人");
		ontClass.inventor = owlModel.createOWLNamedSubclass("發明人", ontClass.relationPerson);
		ontClass.applicant = owlModel.createOWLNamedSubclass("申請人", ontClass.relationPerson);
		
		// 建立 "國家" class
		ontClass.country = owlModel.createOWLNamedClass("國家");
	}

	@Override
	public void createOntIndividual() {
		// 專利類型
		ontIndividual.patentType_Invention = ontIndividual.getInstance(ontClass.patentType, "發明(Invention)");
		ontIndividual.patentType_Model = ontIndividual.getInstance(ontClass.patentType, "新型(Model)"); 
		ontIndividual.patentType_Design = ontIndividual.getInstance(ontClass.patentType, "新式樣/設計(Design)");
		
		// 國家
		ontIndividual.country_Taiwan = ontIndividual.getInstance(ontClass.country, "台灣");
		ontIndividual.country_Other = ontIndividual.getInstance(ontClass.country, "其他國家");
	}

	@Override
	public void createOntDataProperty() {
		ontDataProperty.patentName = owlModel.createOWLDatatypeProperty("專利名稱");
		ontDataProperty.announcementDate = owlModel.createOWLDatatypeProperty("公開日");
	}

	@Override
	public void createOntObjectProperty() {
		// 建立 "參考" 物件屬性，Domain 和 Range 都設為 "專利編號"
		// "專利編號" --參考--> "專利編號"
		ontObjectProperty.reference = owlModel.createOWLObjectProperty("參考");
		ontObjectProperty.reference.setDomain(ontClass.patentID);
		ontObjectProperty.reference.setRange(ontClass.patentID);
		
		// 建立 "被參考" 物件屬性，Domain 和 Range 都設為 "專利編號"
		// "專利編號" --被參考--> "專利編號"
		ontObjectProperty.isReferencedBy = owlModel.createOWLObjectProperty("被參考");
		ontObjectProperty.isReferencedBy.setDomain(ontClass.patentID);
		ontObjectProperty.isReferencedBy.setRange(ontClass.patentID);
		
		// 建立 "專利分類為" 物件屬性，Domain 設為 "專利編號"，Range 設為 "專利分類"
		// "專利編號" --專利分類為--> "專利分類"
		ontObjectProperty.patentCategory = owlModel.createOWLObjectProperty("專利分類為");
		ontObjectProperty.patentCategory.setDomain(ontClass.patentID);
		ontObjectProperty.patentCategory.setRange(ontClass.category);
		
		// 建立 "專利類型為" 物件屬性，Domain 設為 "專利編號"，Range 設為 "專利類型"
		// "專利編號" --專利類型為--> "專利類型"
		ontObjectProperty.patentType = owlModel.createOWLObjectProperty("專利類型為");
		ontObjectProperty.patentType.setDomain(ontClass.patentID);
		ontObjectProperty.patentType.setRange(ontClass.patentType);
		
		// 建立 "專利申請國" 物件屬性，Domain 設為 "專利編號"，Range 設為 "國家"
		// "專利編號" --專利申請國--> "國家"
		ontObjectProperty.applicationCountry = owlModel.createOWLObjectProperty("專利申請國");
		ontObjectProperty.applicationCountry.setDomain(ontClass.patentID);
		ontObjectProperty.applicationCountry.setRange(ontClass.country);
		
		// 建立 "專利發明人" 物件屬性，Domain 設為 "專利編號"，Range 設為 "發明人"
		// "專利編號" --專利發明人--> "發明人"
		ontObjectProperty.inventor = owlModel.createOWLObjectProperty("專利發明人");
		ontObjectProperty.inventor.setDomain(ontClass.patentID);
		ontObjectProperty.inventor.setRange(ontClass.inventor);
		
		// 建立 "專利申請人" 物件屬性，Domain 設為 "專利編號"，Range 設為 "申請人"
		// "專利編號" --專利申請人--> "申請人"
		ontObjectProperty.applicant = owlModel.createOWLObjectProperty("專利申請人");
		ontObjectProperty.applicant.setDomain(ontClass.patentID);
		ontObjectProperty.applicant.setRange(ontClass.applicant);

		// 建立 "發明" 物件屬性，Domain 設為 "專利編號"，Range 設為 "代理人"
		// "發明人" --發明--> "專利編號"
		ontObjectProperty.invention = owlModel.createOWLObjectProperty("發明");
		ontObjectProperty.invention.setDomain(ontClass.inventor);
		ontObjectProperty.invention.setRange(ontClass.patentID);
		
		// 建立 "申請人國籍" 物件屬性，Domain 設為 "申請人"，Range 設為 "國家"
		// "申請人" --申請人國籍--> "國家"
		ontObjectProperty.nationalityOfApplicant = owlModel.createOWLObjectProperty("申請人國籍");
		ontObjectProperty.nationalityOfApplicant.setDomain(ontClass.applicant);
		ontObjectProperty.nationalityOfApplicant.setRange(ontClass.country);
	}

	public void createRelation() {
		dbOperations.SelectTable(selectSQL, new Mysql_Select() {
			@Override
			public void select(ResultSet resultSet) throws SQLException {
				while (resultSet.next()) {
					Patent patent = new Patent(resultSet);
					System.out.println(patent.id + "\t" + patent.name);
					
					try {
						ontIndividual.patentID = ontIndividual.getInstance(ontClass.patentID, patent.id);

						addDataPropertyValue(patent);
						Relation.addType(ontIndividual.patentID, patent.id);
						Relation.addPatentReference(patent);
						Relation.adddPatentInventor(patent);
						Relation.addApplicationCountry(ontIndividual.patentID, patent.id);
						Relation.addApplicant(patent.applicant);
						Relation.addNationalityOfApplicant(patent.applicant);
						Relation.addPatentCategory(ontIndividual.patentID, patent.categoryLOC);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	/** 在 "專利編號" 的實例中設定 "資料屬性"：專利名稱、公開日 */
	public void addDataPropertyValue(Patent patent) {
		ontIndividual.patentID.addPropertyValue(ontDataProperty.patentName, patent.name);
		ontIndividual.patentID.addPropertyValue(ontDataProperty.announcementDate, patent.announcementDate);
	}

	/** 將 Ontology 存成 OWL 檔 */
	@Override
	public void saveOWL() {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(new File(OWLFileName));
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
			OWLModelWriter owlModelWriter = new OWLModelWriter(owlModel, 
					owlModel.getTripleStoreModel().getActiveTripleStore(), outputStreamWriter);
			owlModelWriter.write();
			fileOutputStream.flush();
			outputStreamWriter.flush();
			fileOutputStream.close();
			outputStreamWriter.close();
			System.out.println("Create Ontology Successfully");
		} catch (IOException e) {
			e.printStackTrace();
		}
		// FileInputStream is = new FileInputStream("./OWL/patent_ontology.owl");
		// JenaOWLModel owlModel = ProtegeOWL.createJenaOWLModelFromInputStream(is);
		
		// String filePath = "./OWL/patent_ontology_r.owl";
		// File file = new File(filePath);
		// Jena.saveOntModel(owlModel, file, owlModel.getOntModel(), "file saved at "+ filePath);

		// System.out.println("Owl model get.");
		// is.close();

		// String fileName = "./OWL/patent_ontology_r.owl";
		// Collection errors = new ArrayList();
		// owlModel.save(new File(fileName).toURI(), FileUtils.langXMLAbbrev, errors);
		// System.out.println("File saved with " + errors.size() + " errors.");
		// owlModel.close();
	}
}