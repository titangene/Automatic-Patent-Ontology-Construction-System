package ontology.owl;

import edu.stanford.smi.protegex.owl.model.OWLNamedClass;

/** 專利的本體類別 */
public class OntClass {
	/** 專利編號 */
	public OWLNamedClass patentID;
	/** 專利分類 */
	public OWLNamedClass category;
	/** 設計分類號_LOC */
	public OWLNamedClass category_LOC;
	/** 設計分類號_LOC 的子分類 */
	public OWLNamedClass subCategory_LOC;
	/** 專利類型 */
	public OWLNamedClass patentType; 
	/** 專利關係人 */
	public OWLNamedClass relationPerson;
	/** 發明人 */
	public OWLNamedClass inventor;
	/** 申請人 */
	public OWLNamedClass applicant;
	/** 代理人 */
	public OWLNamedClass agent;
	/** 國家 */
	public OWLNamedClass country;

	/** Get OWLNamedClass class (Singleton) */
	public OWLNamedClass getInstance(String OWLClassName) {
		OWLNamedClass instance = OntModel.getInstance().getOWLModel().getOWLNamedClass(OWLClassName);
		if (instance == null) {
			instance = OntModel.getInstance().getOWLModel().createOWLNamedClass(OWLClassName);
		}

		return instance;
	}

	/** Get OWLNamedClass sub class (Singleton) */
	public OWLNamedClass getSubInstance(OWLNamedClass OWLClass, String OWLClassName) {
		OWLNamedClass instance = OntModel.getInstance().getOWLModel().getOWLNamedClass(OWLClassName);
		if (instance == null) {
			instance = OntModel.getInstance().getOWLModel().createOWLNamedSubclass(OWLClassName, OWLClass);
		}

		return instance;
	}
}