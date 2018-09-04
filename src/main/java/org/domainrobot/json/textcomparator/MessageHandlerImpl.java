package org.domainrobot.json.textcomparator;

/**
 * This is a default implementation of the {@link MessageHandler} inferface.
 *
 */
public class MessageHandlerImpl implements MessageHandler {

	private String separatorFields = ",";

	public String descriptor1;

	public String descriptor2;

	public StringBuilder info = new StringBuilder();

	/**
	 * 
	 * @param descriptor1 a describing name for the first source
	 * @param descriptor2 a describing name for the second source
	 */
	public MessageHandlerImpl(String descriptor1, String descriptor2) {
		this.descriptor1 = descriptor1;
		this.descriptor2 = descriptor2;
	}

	@Override
	public String getMessage() {
		String msg = info.toString();
		if(msg.endsWith(separatorFields))
			msg = msg.substring(0, msg.length() - separatorFields.length());
		info = new StringBuilder();
		return msg;
	}
	
	@Override
	public void addWrongType(String path) {
		addInfo(String.format("%s:WRONGTYPE", path));
	}

	private void addInfo(String msg) {
		info.append(msg).append(separatorFields);
	}

	@Override
	public void addDiff(String path, String v1, String v2) {
		addInfo(String.format("%s:DIFF:%s[%s];%s[%s]", path, descriptor1, v1, descriptor2, v2));
	}

	@Override
	public void addMissingInSrc1(String path) {
		info.append(path).append(":").append("NOT_IN_").append(descriptor1).append(separatorFields);
	}

	@Override
	public void addMissingInSrc2(String path) {
		info.append(path).append(":").append("NOT_IN_").append(descriptor2).append(separatorFields);		
	}
}