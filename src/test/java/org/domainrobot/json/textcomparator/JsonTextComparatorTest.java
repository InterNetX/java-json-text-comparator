package org.domainrobot.json.textcomparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

public class JsonTextComparatorTest {

	private JsonTextComparator jtc;

	@Before
	public void init() {
		jtc = new JsonTextComparator(new MessageHandlerImpl("J1", "J2"));
	}

	private String read(InputStream in) {
		return new BufferedReader(new InputStreamReader(in)).lines().parallel().collect(Collectors.joining("\n"));
	}

	@Test
	public void testEquals() throws Exception {
		String actual = jtc.setJson(
			read(this.getClass().getResourceAsStream("obj1.json")),
			read(this.getClass().getResourceAsStream("obj1.json"))).compare();
		assertNull(actual);
	}

	@Test
	public void testDiff() throws Exception {
		String actual = jtc.setJson(
			read(this.getClass().getResourceAsStream("obj1.json")),
			read(this.getClass().getResourceAsStream("obj2.json"))).compare();
		assertEquals(
			"/data/val:DIFF:J1[\"v1.1\"];J2[\"v2\"],/val:DIFF:J1[\"v1\"];J2[\"v2\"],/list:DIFF:J1[[1,2,3]];J2[[1,4,3]]",
			actual);
	}

	@Test
	public void testIgnore1() throws Exception {
		String actual = jtc.addKeyToIgnore("val")
				.setJson(
					read(this.getClass().getResourceAsStream("obj1.json")),
					read(this.getClass().getResourceAsStream("obj2.json")))
				.compare();
		assertEquals("/list:DIFF:J1[[1,2,3]];J2[[1,4,3]]", actual);
	}

	@Test
	public void testIgnore2() throws Exception {
		String actual = jtc.addPathToIgnore("/data/val").addKeyToIgnore("list")
				.setJson(
					read(this.getClass().getResourceAsStream("obj1.json")),
					read(this.getClass().getResourceAsStream("obj2.json")))
				.compare();
		assertEquals("/val:DIFF:J1[\"v1\"];J2[\"v2\"]", actual);
	}

	@Test
	public void testNullIsEmpty() throws Exception {
		String actual = jtc.enableNullIsUnavailable()
				.setJson(
					read(this.getClass().getResourceAsStream("nullcheck1.json")),
					read(this.getClass().getResourceAsStream("nullcheck2.json")))
				.compare();
		assertNull(actual);
	}

	@Test
	public void testArray01() throws Exception {
		String actual = jtc.setJson(
			read(this.getClass().getResourceAsStream("array1.json")),
			read(this.getClass().getResourceAsStream("array2.json"))).compare();
		assertEquals("/array/obj[1]/name:DIFF:J1[\"obj2\"];J2[\"data2\"]", actual);
	}

	@Test
	public void testArray02() throws Exception {
		String actual = jtc.setJson(
			read(this.getClass().getResourceAsStream("array1.json")),
			read(this.getClass().getResourceAsStream("array3.json"))).compare();
		assertEquals("/array/obj[2]:NOT_IN_J2", actual);
	}

	@Test
	public void testArray03() throws Exception {
		String actual = jtc.setJson(
			read(this.getClass().getResourceAsStream("array3.json")),
			read(this.getClass().getResourceAsStream("array1.json"))).compare();
		assertEquals("/array/obj[2]:NOT_IN_J1", actual);
	}

}
