package io.openlena.ctl.type;

import static org.junit.Assert.*;

import org.junit.Test;

public class InstallerCommandTypeTest {

	@Test
	public void testGetCommand() {
		String command = "CREATE";
		assertNotNull(InstallerCommandType.valueOf(command));
	}

}
