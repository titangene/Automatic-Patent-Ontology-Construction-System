package ontology;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 專利發明人 */
public class PatentInventor {
	/** 發明人名字 */
	private String name;
	/** 發明人名字 (Map)，包含：chinese, englishLast, englishFirst */
	private Map<String, String> nameMap = new HashMap<>();

	/** Regex 中英文名字 */
	private final String regexChineseEnglishName = "(\\p{sc=Han}+)?(\\w+)? (\\w+)?";
	/** Regex 中文名字 */
	private final String regexChineseName = "\\p{sc=Han}+";
	private static Pattern pattern;
	private static Matcher matcher;
	
	public PatentInventor(String name) {
		if (isChineseName(name)) {
			matcher = setMatcher(regexChineseName, name);
			while (matcher.find()) {
				nameMap.put("chinese", matcher.group(0));
				this.name = getName("chinese");
			}
		} else {
			matcher = setMatcher(regexChineseEnglishName, name);
			while (matcher.find()) {
				setNameMap();
				setName();
			}
		}
	}

	private void setNameMap() {
		nameMap.put("chinese", matcher.group(1));
		nameMap.put("englishLast", matcher.group(2));
		nameMap.put("englishFirst", matcher.group(3));
	}

	private void setName() {
		if (hasntEnglishName()) {
			this.name = getName("chinese");
		} else if (hasntChineseName()) {
			this.name = getName("englishLast") + "_" + getName("englishFirst");
		} else if (hasntEnglishLastName()) {
			this.name = getName("chinese") + "___" + getName("englishFirst");
		}
	}

	public Map<String, String> getNameMap() {
		return nameMap;
	}

	public String getName() {
		return this.name;
	}

	public String getName(String name) {
		return nameMap.get(name);
	}

	private boolean hasntEnglishLastName() {
		return nameMap.get("englishLast") == null;
	}

	private boolean hasntEnglishFirstName() {
		return nameMap.get("englishFirst") == null;
	}

	private boolean hasntEnglishName() {
		return hasntEnglishLastName() && hasntEnglishFirstName();
	}

	private boolean hasntChineseName() {
		return nameMap.get("chinese") == null;
	}

	private boolean isChineseName(String name) {
		return !name.contains(" ");
	}

	private Matcher setMatcher(String regex, String matcherValue) {
		pattern = Pattern.compile(regex);
		return pattern.matcher(matcherValue);
	}
}