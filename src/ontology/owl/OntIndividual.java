package ontology.owl;

import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;

/** 專利的本體實例 */
public class OntIndividual {
	/** 專利編號 */
	public OWLIndividual patentID;
	/** 專利編號 (建立關聯用：被參考) */
	public OWLIndividual patentID_IsReferencedBy;
	/** 發明人 */
	public OWLIndividual inventor;
	/** 申請人 */
	public OWLIndividual applicant;
	/** 代理人 */
	public OWLIndividual agent;
	/** 發明 (Invention) */
	public OWLIndividual patentType_Invention;
	/** 新型 (Model) */
	public OWLIndividual patentType_Model;
	/** 新式樣/設計 (Design) */
	public OWLIndividual patentType_Design;
	/** 台灣 */
	public OWLIndividual country_Taiwan;
	/** 其他國家 */
	public OWLIndividual country_Other;
	/** 設計分類號_LOC */
	public OWLIndividual categoryLOC;
	/** 設計分類號_LOC */
	public OWLIndividual subCategoryLOC;

	/** Get OntIndividual instance (Singleton) */
	public OWLIndividual getInstance(OWLNamedClass OWLClass, String OWLIndividualName) {
		OWLIndividual instance = OntModel.getInstance().getOWLModel().getOWLIndividual(OWLIndividualName);
		try {
			if (instance == null) {
				instance = OWLClass.createOWLIndividual(OWLIndividualName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return instance;
	}
}