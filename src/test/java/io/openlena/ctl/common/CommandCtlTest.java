package io.openlena.ctl.common;

import static org.junit.Assert.*;

import org.junit.Test;

public class CommandCtlTest {
	
	public static final String INSTALLER = "INSTALLER";
	public static final String CONFIGURATOR = "CONFIGURATOR";

	@Test
	public void testCommandChecker() {
		CommandCtl commandCtl = new CommandCtl();
		String createCommand = "create";
		String exepctionCommand = "error";
		
		// check create command
		assertEquals(CommandCtl.INSTALLER, commandCtl.commandChecker(createCommand));
		
		// not contain command
		assertEquals("", commandCtl.commandChecker(exepctionCommand));
	}

}
