package io.openlena.ctl.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @see CipherUtil
 * @author Pinepond
 */
public class CipherUtilTest {

	/**
	 * test CipherUtil.md5
	 * encryt string
	 */
	@Test public void md5() {
		String plainText = "plainText";
		String md5Text = "e3b3d28c2389af60776cf20de998f1a4";
		assertEquals(md5Text, CipherUtil.md5("plainText"));
	}
}