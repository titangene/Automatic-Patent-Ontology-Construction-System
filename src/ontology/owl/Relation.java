package ontology.owl;

import ontology.CategoryLOC;
import ontology.CategoryLocProcess;
import ontology.Patent;
import ontology.PatentInventor;
import ontology.PatentOWL;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;

/** 建立本體關聯 */
public class Relation {
	/** 專利的本體類別 */
	private static OntClass _ontClass = PatentOWL.ontClass;
	/** 專利的本體實例 */
	private static OntIndividual _ontIndividual = PatentOWL.ontIndividual;
	/** 專利的本體物件屬性 */
	private static OntObjectProperty _ontObjectProperty = PatentOWL.ontObjectProperty;

	/** 新增專利參考關聯：參考、被參考 */
	public static void addPatentReference(Patent patent) {
		if (patent.hasMoreReferences()) {
			for (String patentReference : patent.getReferenceAry()) {
				_ontIndividual.patentID_IsReferencedBy = _ontIndividual.getInstance(_ontClass.patentID, patentReference);

				addPatentIsReferenceByRelated(patent, patentReference);
			}
		} else {
			String patentReference = patent.getReference();
			_ontIndividual.patentID_IsReferencedBy = _ontIndividual.getInstance(_ontClass.patentID, patentReference);

			addPatentIsReferenceByRelated(patent, patentReference);
		}
	}

	/** 新增被參考的專利相關的關聯：參考、被參考、專利類行為、專利申請國 */
	private static void addPatentIsReferenceByRelated(Patent patent, String patentReference) {
		addReference();
		addIsReferenceBy(patent, patentReference);
		addType(_ontIndividual.patentID_IsReferencedBy, patentReference);
		addApplicationCountry(_ontIndividual.patentID_IsReferencedBy, patentReference);
	}

	/** 新增 "參考" 物件屬性 關聯："專利編號" 參考 "專利編號 (參考文獻)" */
    private static void addReference() {
		_ontIndividual.patentID.addPropertyValue(_ontObjectProperty.reference, _ontIndividual.patentID_IsReferencedBy);
	}

	/** 新增 "被參考" 物件屬性 關聯："專利編號 (參考文獻)" 被 "專利編號" 參考 */
	private static void addIsReferenceBy(Patent patent, String patentReference) {
		_ontIndividual.patentID_IsReferencedBy.addPropertyValue(_ontObjectProperty.isReferencedBy, _ontIndividual.patentID);
	}

	/** 新增專利參考關聯：專利分類為 */
	public static void addPatentCategory(OWLIndividual ontIndividual, String _patentCategoryLOC) {
		String[] patentCategoryLOC = _patentCategoryLOC.split("-");
		String categoryLocID = patentCategoryLOC[0];
		String subCategoryLocID = patentCategoryLOC[1];
		
		CategoryLOC categoryLoc = CategoryLocProcess.getMaps(categoryLocID);
		String categoryLocName = categoryLoc.getName();
		String subCategoryLocName = categoryLoc.getSubCategoryName(subCategoryLocID);

		String categoryLocNameID = categoryLocID + "：" + categoryLocName;
		String subCategoryLocNameID = categoryLocID + "-" + subCategoryLocID + "：" + subCategoryLocName;

		OWLNamedClass ontClass_CategoryLoc = _ontClass.getSubInstance(_ontClass.category_LOC, categoryLocNameID);
		_ontIndividual.subCategoryLOC = _ontIndividual.getInstance(ontClass_CategoryLoc, subCategoryLocNameID);
		addCategory(ontIndividual);
	}
	
	/** 新增 "專利分類為" 物件屬性 關聯："專利編號" 專利分類為 "專利分類" */
	public static void addCategory(OWLIndividual ontIndividual) {
		ontIndividual.addPropertyValue(_ontObjectProperty.patentCategory, _ontIndividual.subCategoryLOC);
	}

	/** 如果是 台灣專利 才新增 "專利類型為" 物件屬性 關聯："專利編號" 專利類型為 "專利類型" */
	public static void addType(OWLIndividual ontIndividual, String patentID) {
		if (Patent.isTW_Patent(patentID)) {
			char patentType = patentID.charAt(2);
			switch (patentType) {
				case 'I':	// 發明(Invention)
					ontIndividual.addPropertyValue(_ontObjectProperty.patentType, _ontIndividual.patentType_Invention);
					break;
				case 'M':	// 新型(Model)
					ontIndividual.addPropertyValue(_ontObjectProperty.patentType, _ontIndividual.patentType_Model);
					break;
				case 'D':	// 新式樣/設計(Design)
					ontIndividual.addPropertyValue(_ontObjectProperty.patentType, _ontIndividual.patentType_Design);
					break;
			}
		}
	}

	/** 新增 "專利申請國" 物件屬性 關聯："專利編號" 專利申請國為 "國家" */
	public static void addApplicationCountry(OWLIndividual OWLIndividual, String patentID) {
		// TODO: 其他國家實例
		if (Patent.isTW_Patent(patentID)) {
			OWLIndividual.addPropertyValue(_ontObjectProperty.applicationCountry, _ontIndividual.country_Taiwan);
		} else {
			OWLIndividual.addPropertyValue(_ontObjectProperty.applicationCountry, _ontIndividual.country_Other);
		}
	}

	/** 新增專利參考關聯：專利發明人、發明 */
	public static void adddPatentInventor(Patent patent) {
		if (patent.hasInventor()) {
			for (PatentInventor _patentInventor: patent.getInventorList()) {
				String patentInventor = _patentInventor.getName();
				addInventor(patentInventor);
				addInvention(patentInventor);
			}
		}
	}

	/** 新增 "專利發明人" 物件屬性 關聯："專利編號" 的 專利發明人 是 "發明人" */
	public static void addInventor(String patentInventor) {
		_ontIndividual.inventor = _ontIndividual.getInstance(_ontClass.inventor, patentInventor);
		_ontIndividual.patentID.addPropertyValue(_ontObjectProperty.inventor, _ontIndividual.inventor);
	}

	/** 新增 "發明" 物件屬性 關聯："發明人" 發明 "專利編號" */
	public static void addInvention(String patentInventor) {
		_ontIndividual.inventor.addPropertyValue(_ontObjectProperty.invention, _ontIndividual.patentID);
	}
	
	/** 新增 "專利申請人" 物件屬性 關聯："專利編號" 的 專利申請人 是 "申請人" */
	public static void addApplicant(String patentApplicant) {
		_ontIndividual.applicant = _ontIndividual.getInstance(_ontClass.applicant, patentApplicant);
		_ontIndividual.patentID.addPropertyValue(_ontObjectProperty.applicant, _ontIndividual.applicant);
	}

	/** 新增 "申請人國籍" 物件屬性 關聯："申請人" 的 申請人國籍 是 "國家" */
	public static void addNationalityOfApplicant(String patentApplicant) {
		if (Patent.isTWApplicant(patentApplicant)) {
			_ontIndividual.applicant.addPropertyValue(_ontObjectProperty.applicationCountry, _ontIndividual.country_Taiwan);
		} else {
			_ontIndividual.applicant.addPropertyValue(_ontObjectProperty.applicationCountry, _ontIndividual.country_Other);
		}
	}
}