package org.domainrobot.json.textcomparator;

import static org.junit.Assert.*;

import org.junit.Test;


public class MessageHandlerImplTest {

	@Test
	public void testGetMessage() {
		MessageHandler mh = new MessageHandlerImpl("D1", "D2");
		mh.addDiff("/data/val", "v1", "v2");
		mh.addMissingInSrc1("/id");
		mh.addMissingInSrc2("/updated");
		mh.addWrongType("/user_id");
		assertEquals("/data/val:DIFF:D1[v1];D2[v2],/id:NOT_IN_D1,/updated:NOT_IN_D2,/user_id:WRONGTYPE", mh.getMessage());
	}

}
