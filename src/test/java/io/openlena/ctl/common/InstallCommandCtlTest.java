package io.openlena.ctl.common;

import static org.junit.Assert.*;

import org.junit.Test;

public class InstallCommandCtlTest {

	@Test
	public void testContainsCommand() {
		InstallCommandCtl installCommandCtl = new InstallCommandCtl();
		
		String installCommand = "create";
		String notInstallCommand = "error";
		
		assertTrue(installCommandCtl.containsCommand(installCommand));
		assertTrue(!installCommandCtl.containsCommand(notInstallCommand));
	}

}
