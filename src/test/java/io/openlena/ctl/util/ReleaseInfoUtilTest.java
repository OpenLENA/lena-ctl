package io.openlena.ctl.util;

import java.io.File;
import java.io.IOException;

import io.openlena.ctl.util.testtools.FileBasedTestCase;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReleaseInfoUtilTest {

	private String lenaHome = FileBasedTestCase.getTestDirectory().getCanonicalPath();

	public ReleaseInfoUtilTest() throws IOException {
	}

	@Test
	public void getDepotPath() throws IOException {
		File releaseInfoFile = new File(FileUtil.getConcatPath(lenaHome, "etc", "info", "release-info.xml"));
		String defaultXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
				+ "<release>\n"
				+ "    <date>${build.timestamp}</date>\n"
				+ "    <version>1.0.0</version>\n"
				+ "    <type>lena</type>\n"
				+ "    <depot>\n"
				+ "        <modules>\n"
				+ "            <module>\n"
				+ "                <id>lena-was</id>\n"
				+ "                <version>1.0.0</version>\n"
				+ "            </module>\n"
				+ "        </modules>\n"
				+ "    </depot>\n"
				+ "</release>";

		FileUtil.writeStringToFile(releaseInfoFile, defaultXml);

		String deoptPath = ReleaseInfoUtil.getDepotPath("lena-was");
		assertTrue(new File(deoptPath).getCanonicalPath().startsWith(lenaHome));
	}

}


