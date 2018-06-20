package ontology;

import java.util.HashMap;
import java.util.Map;

public class CategoryLOC {
	private String id;
	private String name;
	private Map<String, String> subCategorys = new HashMap<String, String>();
	
	public CategoryLOC(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setSubCategory(String id, String name) {
		subCategorys.put(id, name);
	}
	
	public Map<String, String> getSubCategorys() {
		return subCategorys;
	}

	public String getSubCategoryName(String id) {
		return subCategorys.get(id);
	}
}