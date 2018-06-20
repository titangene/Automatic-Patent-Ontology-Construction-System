package ontology;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CategoryLocProcess {
	private static Map<String, CategoryLOC> categoryLOCs = new HashMap<String, CategoryLOC>();
	private static String jsonData;

	public static String readFile(String filename) {
	    String source = "";
	    try {
	        FileReader fileReader = new FileReader(filename);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StringBuilder stringBuilder = new StringBuilder();
			
			String readLine;
			
			while ((readLine = bufferedReader.readLine()) != null) {
				stringBuilder.append(readLine);
			}

			source = stringBuilder.toString();

			bufferedReader.close();
			fileReader.close();
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	    return source;
	}

	public static void loadJson() {
		jsonData = readFile("./data/LOC_Category.json");
	}

	public static void parseJSON() {
		try {
			JSONArray jsonArray = new JSONArray(jsonData);

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = new JSONObject(jsonArray.getString(i));

				String categoryID = jsonObject.get("id").toString();
				String categoryName = jsonObject.get("name").toString();

				CategoryLOC categoryLOC = createCategoryLOC(categoryID, categoryName);

				// System.out.println(categoryID + "_" + categoryName);
				// System.out.println("------");
				
				JSONArray subCategoryAry = new JSONArray(jsonObject.getJSONArray("subCategory").toString());
				
				for (int j = 0; j < subCategoryAry.length(); j++) {
					JSONObject subCategoryObj = new JSONObject(subCategoryAry.getString(j));

					String subCategoryID = subCategoryObj.get("id").toString();
					String subCategoryName = subCategoryObj.get("name").toString();

					createSubCategoryLOC(categoryLOC, subCategoryID, subCategoryName);
					// System.out.println(categoryID + "-" + subCategoryID + "_" + subCategoryName);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private static CategoryLOC createCategoryLOC(String id, String name) {
		CategoryLOC categoryLOC = new CategoryLOC(id, name);
		categoryLOCs.put(id, categoryLOC);
		return categoryLOC;
	}

	private static void createSubCategoryLOC(CategoryLOC categoryLOC, String id, String name) {
		categoryLOC.setSubCategory(id, name);
	}

	public static Map<String, CategoryLOC> getMaps() {
		return categoryLOCs;
	}

	public static CategoryLOC getMaps(String categoryID) {
		return categoryLOCs.get(categoryID);
	}
}