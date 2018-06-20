package ontology;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

/** 專利 */
public class Patent {
	/** 專利編號 */
	String id;
	/** 專利名稱 */
	String name;
	/** 公開日 */
	String announcementDate;
	/** 設計分類號_LOC */
	String categoryLOC;
	/** 發明人 (字串) */
	String inventor;
	/** 發明人 (陣列) */
	String[] inventorAry;
	/** 發明人 (物件陣列) */
	List<PatentInventor> inventors = new ArrayList<PatentInventor>();
	/** 申請人 */
	String applicant;
	/** 參考文獻 (字串) */
	String reference;
	/** 參考文獻 (陣列) */
	String[] referenceAry;

	public Patent(ResultSet resultSet) throws SQLException {
		id = "TW" + resultSet.getString("id");
		name = resultSet.getString("name");
		announcementDate = resultSet.getString("announcement_date");
		categoryLOC = resultSet.getString("LOC");
		inventor = resultSet.getString("inventor");
		applicant = resultSet.getString("applicant");
		reference = resultSet.getString("reference");

		setReference();
		setInventor();
		setApplicant();
	}
	
	public void setInventor() {
		if (hasInventor()) {
			if (hasMoreInventors()) {
				inventorAry = this.inventor.split(", ");

				for (String patentInventor: inventorAry) {
					PatentInventor _patentInventor = new PatentInventor(patentInventor);
					inventors.add(_patentInventor);
				}
			} else {
				PatentInventor _patentInventor = new PatentInventor(this.inventor);
				inventors.add(_patentInventor);
			}
		}
	}
	
	public void setApplicant() {
		// TODO: PatentApplicant：發明人名字、公司、國籍
		this.applicant = this.applicant.replace(" ", "_");
	}
	
	private void setReference() {
		if (hasReference() && hasMoreReferences()) {
			referenceAry = reference.split("; ");
		}
	}

	public String getId() {
		return id;
	}

	public List<PatentInventor> getInventorList() {
		return inventors;
	}

	public String getInventor() {
		return inventor;
	}

	/** 專利是否有發明人 */
	public boolean hasInventor() {
		return inventor != null;
	}

	/** 是否有多個專利發明人 */
	public boolean hasMoreInventors() {
		return inventor.contains(",");
	}

	public String getReference() {
		return reference;
	}

	public String[] getReferenceAry() {
		return referenceAry;
	}

	public Integer getReferenceLength() {
		return referenceAry.length; 
	}

	/** 專利是否有參考文獻 */
	private boolean hasReference() {
		return reference != null;
	}

	/** 是否有多個專利參考文獻 */
	public boolean hasMoreReferences() {
		return reference.contains(";");
	}

	public static boolean isTWApplicant(String patentApplicant) {
		return patentApplicant.endsWith("TW");
	}

	/** 是否為台灣專利 */
	public static boolean isTW_Patent(String patentID) {
		return patentID.contains("TW");
	}
}