package org.domainrobot.json.textcomparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A json comparator that compares two json strings following the fluent interface pattern. It's based on jackson
 * (https://github.com/FasterXML/jackson-databind). <p>
 * It's designed for simple objects and arrays. For special nodes the {@link NamedNodeComparator} should be implemented.<p>
 * 
 * E.g. for testing, it can be useful to ignore nodes. This can be done by key (name of the node) or path (hierarchical order of keys).
 * The following methods are provided:
 * <ul>
 * <li>{@link #addKeyToIgnore(String)}
 * <li>{@link #addKeysToIgnore(String...)}
 * <li>{@link #addPathToIgnore(String)}
 * <li>{@link #addPathsToIgnore(String...)}
 * </ul>
 */
public class JsonTextComparator {

	private ObjectMapper mapper = new ObjectMapper();
	
	private String indexPattern = "%s[%d]";

	private String json1;

	private String json2;

	private MessageHandler messageHandler;

	private Set<String> keysToIgnore = new HashSet<>();

	private Set<String> pathsToIgnore = new HashSet<>();

	private boolean nullIsUnavailable;

	private Map<String, NamedNodeComparator> namedNodeComparators = new HashMap<>();

	/**
	 * Constructor with the desired {@link MessageHandler} to uses.
	 * 
	 * @param messageHandler the desired {@link MessageHandler} to use
	 */
	public JsonTextComparator(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

	/**
	 * Enables that unavailable nodes are handled at the same way as the node has the value null.
	 * 
	 * @return
	 */
	public JsonTextComparator enableNullIsUnavailable() {
		nullIsUnavailable = true;
		return this;
	}

	/**
	 * Sets a {@link NamedNodeComparator}, to which the comparison is forwarded to. 
	 * The comparison of a node, which has the name equals {@link NamedNodeComparator#getName()}, will be forwarded to.
	 * 
	 * @param containerComparator
	 * @return
	 */
	public JsonTextComparator addNamedNodeComparator(NamedNodeComparator containerComparator) {
		namedNodeComparators.put(containerComparator.getName(), containerComparator);
		return this;
	}

	/**
	 * Sets the two desired json strings to compare.
	 *  
	 * @param json1
	 * @param json2
	 * @return
	 */
	public JsonTextComparator setJson(String json1, String json2) {
		if(json1 == null || "".equals(json1) || json2 == null || "".equals(json2))
			throw new IllegalArgumentException("All json strings must not be null.");
		this.json1 = json1;
		this.json2 = json2;
		return this;
	}

	/**
	 * Set an {@link ObjectMapper}, so you can use one, that fits your special needs. It's an optional setting.
	 * 
	 * @param mapper
	 * @return
	 */
	public JsonTextComparator setMapper(ObjectMapper mapper) {
		this.mapper = mapper;
		return this;
	}
	
	public JsonTextComparator setIndexPattern(String indexPattern) {
		this.indexPattern = indexPattern;
		return this;
	}

	public JsonTextComparator setKeysToIgnore(Set<String> keysToIgnore) {
		this.keysToIgnore = keysToIgnore;
		return this;
	}

	public JsonTextComparator setPathsToIgnore(Set<String> pathsToIgnore) {
		this.pathsToIgnore = pathsToIgnore;
		return this;
	}

	public JsonTextComparator addKeyToIgnore(String key) {
		keysToIgnore.add(key);
		return this;
	}

	public JsonTextComparator addKeysToIgnore(String... keys) {
		for(String key : keys) {
			addKeyToIgnore(key);
		}
		return this;
	}

	public JsonTextComparator addPathsToIgnore(String... paths) {
		for(String path : paths) {
			addPathToIgnore(path);
		}
		return this;
	}

	public JsonTextComparator addPathToIgnore(String path) {
		if(!path.startsWith("/"))
			throw new IllegalArgumentException("'path' has to start with '/'");
		pathsToIgnore.add(path);
		return this;
	}

	/**
	 * Starts the comparison of the json strings.
	 * 
	 * @return null if the json strings are equal, otherwise a string which explains the difference
	 * @throws IOException
	 */
	public String compare() throws IOException {
		JsonNode root1 = mapper.readTree(json1);
		JsonNode root2 = mapper.readTree(json2);
		if(!root1.getNodeType().equals(root2.getNodeType()))
			throw new IllegalArgumentException("Different types of the root nodes.");
		if(root1.equals(root2)) // a full deep equality
			return null;
		traverse("", root1, root2);
		String msg = messageHandler.getMessage();
		// System.out.println("*** MSG: " + msg);
		return (msg == null || msg.length() == 0) ? null : msg;
	}

	private String toString(JsonNode node) {
		return node.toString();
	}

	private void traverse(String path, JsonNode n1, JsonNode n2) throws JsonProcessingException {
		List<String> processedNodes = new ArrayList<>();
		if(n1.isArray()) {
			// we need a special handling for Arrays because of the index
			Iterator<JsonNode> aryIter1 = n1.elements();
			Iterator<JsonNode> aryIter2 = n2.elements();
			int cnt = 0;
			while(aryIter1.hasNext()) {
				JsonNode itemNode1 = aryIter1.next();
				JsonNode itemNode2 =  (aryIter2.hasNext()) ? aryIter2.next() : null;
				if(itemNode1.isObject()) {
					doCompare(cnt, processedNodes, path, (ObjectNode) itemNode1, (ObjectNode) itemNode2);
				} else if(itemNode1.isArray()) {
					traverse(path, itemNode1, itemNode2);
				}
				cnt++;
			}
			while(aryIter2.hasNext()) {
				JsonNode itemNode2 = aryIter2.next();
				Iterator<String> iter = itemNode2.fieldNames();
				String key = iter.next();
				String currentPath = path + "/" + key;
				if(cnt != 0)
					currentPath = String.format(indexPattern, currentPath, cnt);
				messageHandler.addMissingInSrc1(currentPath);
			}
		} else if(n1.isObject()) {
			doCompare(processedNodes, path, (ObjectNode) n1, (ObjectNode) n2);
		}

		Iterator<Map.Entry<String, JsonNode>> iter2 = n2.fields();
		while(iter2.hasNext()) {
			Map.Entry<String, JsonNode> entry = iter2.next();
			String key = entry.getKey();
			if(!processedNodes.contains(key)) {
				String currentPath = path + "/" + key;
				if(toIgnore(currentPath, key))
					continue;
				messageHandler.addMissingInSrc1(currentPath);
			}
		}
	}

	private void doCompare(List<String> processedNodes, String path, ObjectNode n1, ObjectNode n2) throws JsonProcessingException {
		doCompare(null, processedNodes, path, n1, n2);
	}

	/**
	 * The man compare method.
	 * 
	 * @param cnt
	 * @param processedNodes
	 * @param path
	 * @param n1
	 * @param n2
	 * @throws JsonProcessingException
	 */
	private void doCompare(Integer cnt, List<String> processedNodes, String path, ObjectNode n1, ObjectNode n2) throws JsonProcessingException {
		Iterator<String> iter = n1.fieldNames();
		while(iter.hasNext()) {
			String key = iter.next();
			String currentPath = path + "/" + key;
			if(cnt != null)
				currentPath = String.format(indexPattern, currentPath, cnt);
			// System.out.println(currentPath);
			processedNodes.add(key);
			boolean ignore = toIgnore(currentPath, key);
			if(ignore)
				continue;
			JsonNode child1 = n1.path(key);
			JsonNode child2 = fetchChildNode(n2, key);
			if(child2 == null) {
				messageHandler.addMissingInSrc2(currentPath);
			} else if(checkNullIsUnavailable(child1, child2)) {
				continue;
			} else if(!child1.getNodeType().equals(child2.getNodeType())) {
				messageHandler.addWrongType(currentPath);
			} else if(namedNodeComparators.containsKey(key)) {
				processNamedNodeCoparator(currentPath, namedNodeComparators.get(key), child1, child2);
			} else if(child1.isContainerNode() && !isSimpleArray(child1)) {
				traverse(currentPath, child1, child2);
			} else {
				String val1Str = toString(child1);
				String val2Str = toString(child2);
				if(!val1Str.equals(val2Str))
					messageHandler.addDiff(currentPath, val1Str, val2Str);
			}
		}
		
	}
	
	/**
	 * Starts the processing of the desired {@link NamedNodeComparator} and handles its diff.
	 *  
	 * @param currentPath
	 * @param namedNodeComparator
	 * @param n1
	 * @param n2
	 */
	private void processNamedNodeCoparator(String currentPath, NamedNodeComparator namedNodeComparator, JsonNode n1, JsonNode n2) {
		JsonDiff diff = namedNodeComparator.process(n1, n2);
		if(diff != null)
			messageHandler.addDiff(currentPath, diff.getVal1Str(), diff.getVal2Str());
	}

	/**
	 * Fetches a child-node with a specific name. If 'parent' is an ArrayNode, it will be scanned
	 * 
	 * @param parent
	 * @param name
	 * @return
	 */
	private JsonNode fetchChildNode(JsonNode parent, String name) {
		if(parent == null)
			return null;
		JsonNode child = parent.get(name);
		if(parent.isArray() && child == null) {
			Iterator<JsonNode> iterChildren2 = parent.elements();
			while(iterChildren2.hasNext()) {
				JsonNode subChild = iterChildren2.next();
				if(subChild.isObject()) {
					child = subChild.get(name);
					break;
				}
			}
		}
		return child;
	}

	private boolean checkNullIsUnavailable(JsonNode n1, JsonNode n2) {
		if(!nullIsUnavailable)
			return false;
		if(!n1.isObject())
			return false;

		Iterator<String> iter = n1.fieldNames();
		while(iter.hasNext()) {
			String key = iter.next();
			JsonNode child1 = n1.path(key);
			JsonNode child2 = n2.get(key);
			if(nullIsUnavailable(child1, child2))
				return true;
		}

		return false;
	}

	private boolean nullIsUnavailable(JsonNode n1, JsonNode n2) {
		if(n1 == null && n2 == null)
			return true;
		if(n1 == null) {
			return n2 == null || n2.isNull();
		}
		if(n2 == null) {
			return n1 == null || n1.isNull();
		}
		return false;
	}

	private boolean isSimpleArray(JsonNode node) {
		if(!node.isArray())
			return false;
		Iterator<JsonNode> iterChildren = node.elements();
		while(iterChildren.hasNext()) {
			JsonNode child = iterChildren.next();
			if(!child.isValueNode())
				return false;
		}
		return true;
	}

	private boolean toIgnore(String path, String key) {
		path = cleanIndex(path);
		key = cleanIndex(key);
		return pathsToIgnore.contains(path) || keysToIgnore.contains(key);
	}

	String cleanIndex(String str) {
		return (str == null) ? null : str.replaceAll("\\[\\d+\\]", "");
	}
}
