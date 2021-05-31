package io.openlena.ctl.util;


import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StringUtilTest {

	@Test
	public void testIsBlank() {
		StringUtil strUtil = new StringUtil();
		String strNull = null;
		String str = "TEST";
		String str2 = "";
		String strWhiteSpace= " ABC DEF";
		assertTrue(strUtil.isBlank(strNull));
		assertTrue(!strUtil.isBlank(str));
		assertTrue(strUtil.isBlank(str2));
		assertTrue(!strUtil.isBlank(strWhiteSpace));
	}
}
