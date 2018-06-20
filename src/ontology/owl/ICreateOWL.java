package ontology.owl;

public interface ICreateOWL {
    void initOWL();
    void createOntClass();
    void createOntIndividual();
    void createOntDataProperty();
    void createOntObjectProperty();
    void saveOWL() throws Exception;
}