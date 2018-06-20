package ontology;

public class OntologyConstructor {
	public static void main(String[] args) {
		CategoryLocProcess.loadJson();
		CategoryLocProcess.parseJSON();

		PatentOWL patentOWL = new PatentOWL();
		patentOWL.initOWL();
		patentOWL.saveOWL();
	}
}
