package org.domainrobot.json.textcomparator;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Object to which the {@link JsonTextComparator} forwards two {@link JsonNode}s for comparing. It's useful, if you have
 * to compare 'special' nodes.
 */
public interface NamedNodeComparator {

	/**
	 * Processes the comparison for two json nodes. 
	 * 
	 * @param node1
	 * @param node2
	 * 
	 * @return
	 */
	public JsonDiff process(JsonNode node1, JsonNode node2);

	/**
	 * Defines the name of the node, which will be skipped by {@link JsonTextComparator} and forwarded to the given NamedNodeComparator implementation.
	 * 
	 * @return
	 */
	public String getName();

}
