package ontology.owl;

import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.model.OWLModel;

public class OntModel {
	private static OntModel instance;

	private OWLModel owlModel;

	private OntModel() {
		setOWLModel();
	}
	
	public static OntModel getInstance() {
		if (instance == null) {
			instance = new OntModel();
		}
		return instance;
	}

	private void setOWLModel() {
		try {
			owlModel = ProtegeOWL.createJenaOWLModel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public OWLModel getOWLModel() {
		return owlModel;
	}
}