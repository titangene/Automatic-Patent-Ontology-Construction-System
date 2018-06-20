package ontology.owl;

import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;

/** 專利的本體物件屬性 */
public class OntObjectProperty {
	/** 參考 */
	public OWLObjectProperty reference;
	/** 被參考 */
	public OWLObjectProperty isReferencedBy;
	/** 專利分類為 */
	public OWLObjectProperty patentCategory;
	/** 專利類型為 */
	public OWLObjectProperty patentType;
	/** 專利申請國 */
	public OWLObjectProperty applicationCountry;
	/** 專利發明人 */
	public OWLObjectProperty inventor;
	/** 專利申請人 */
	public OWLObjectProperty applicant;
	/** 發明 */
	public OWLObjectProperty invention;
	/** 申請人國籍 */
	public OWLObjectProperty nationalityOfApplicant;
}