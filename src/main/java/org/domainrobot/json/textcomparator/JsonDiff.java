package org.domainrobot.json.textcomparator;

/**
 * Object to hold the result of a custom NamedNodeComparator process call.
 * 
 * @see NamedNodeComparator#process(com.fasterxml.jackson.databind.JsonNode, com.fasterxml.jackson.databind.JsonNode)
 */
public class JsonDiff {

	private String val1Str;

	private String val2Str;

	
	public JsonDiff() {
	}
	
	public JsonDiff(String val1Str, String val2Str) {
		this.val1Str = val1Str;
		this.val2Str = val2Str;
	}

	public String getVal1Str() {
		return val1Str;
	}

	public void setVal1Str(String val1Str) {
		this.val1Str = val1Str;
	}

	public String getVal2Str() {
		return val2Str;
	}

	public void setVal2Str(String val2Str) {
		this.val2Str = val2Str;
	}

}
