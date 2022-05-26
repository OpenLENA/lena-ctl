package io.openlena.ctl.type;

import static org.junit.Assert.*;

import org.junit.Test;

import io.lat.ctl.type.InstallerCommandType;

public class InstallerCommandTypeTest {

	@Test
	public void testGetCommand() {
		String command = "CREATE";
		assertNotNull(InstallerCommandType.valueOf(command));
	}

}
