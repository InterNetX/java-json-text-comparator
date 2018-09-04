package org.domainrobot.json.textcomparator;

/**
 * Produces a 'diff output' (message) for a full comparison between two json strings, for several issues. The implementation has
 * to respect all differences and shouldn't stop at the first. That means a message can be build up with 0 or more items.
 * <p>
 * Methods for all types of differences are provided.
 */
public interface MessageHandler {

	/**
	 * Add message for wrong node type.
	 * 
	 * @param path e.g. '/data/id'
	 */
	void addWrongType(String path);

	/**
	 * Add message for different node values.
	 * 
	 * @param path e.g. '/data/id'
	 * @param v1
	 * @param v2
	 */
	void addDiff(String path, String v1, String v2);

	/**
	 * Add message, if the  node of the desired path is missing in the first source.
	 * 
	 * @param path e.g. '/data/id'
	 */
	void addMissingInSrc1(String path);

	/**
	 * Add message, if the  node of the desired path is missing in the second source.
	 * 
	 * @param path e.g. '/data/id'
	 */
	void addMissingInSrc2(String path);

	/**
	 * @return the 'diff output' or null, if there is no differences
	 */
	String getMessage();
}