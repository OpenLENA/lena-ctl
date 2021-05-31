package io.openlena.ctl.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.openlena.ctl.util.testtools.FileBasedTestCase;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class EnvUtilTest  {

	private String javaHome;
	private String userJavaHome;
	private String lenaHome;
	private String hostname;
	private String runUser;
	private String userHome;
	private String logHome;
	private String resultFormat;
	private boolean rootUserAllowed;


	@Before
	public void setUp() throws Exception {
		System.setProperty("lena.home", FileBasedTestCase.getTestDirectory().getCanonicalPath());
		System.setProperty("hostname", "hostname_unit_test");
		System.setProperty("run_user", System.getProperty("user.name"));

		javaHome = System.getProperty("java.home");
		userJavaHome = System.getProperty("user_java.home", javaHome);
		lenaHome = System.getProperty("lena.home");
		hostname = System.getProperty("hostname");
		runUser = System.getProperty("run_user");
		userHome = System.getProperty("user.home");
		logHome = System.getProperty("log.home", FileUtil.getConcatPath(lenaHome, "logs", "lena-installer"));
		resultFormat = System.getProperty("result.format", "text");
		rootUserAllowed = (System.getProperty("root_user.allowed", "false").equals("true"));
	}



	@Test
	public void getJavahome() {
		assertEquals(javaHome, EnvUtil.getJavahome());
	}

	@Test
	public void getUserJavahome() {
		assertEquals(userJavaHome, EnvUtil.getUserJavahome());
	}

	@Test
	public void getLenaHome() throws IOException {
		assertEquals(lenaHome, new File(EnvUtil.getLenaHome()).getCanonicalPath());
	}

	@Test
	public void getHostname() {
		assertEquals(hostname, EnvUtil.getHostname());
	}

	@Test
	public void getRunuser() {
		assertEquals(runUser, EnvUtil.getRunuser());
	}

	@Test
	public void getUserhome() {
		assertEquals(userHome, EnvUtil.getUserhome());
	}

	@Test
	public void getLogHome() throws IOException {
		assertEquals(logHome, new File(EnvUtil.getLogHome()).getCanonicalPath());
	}

	@Test
	public void getResultFormat() {
		assertEquals(resultFormat, EnvUtil.getResultFormat());
	}

	@Test
	public void isRootUserAllowed() {
		assertEquals(rootUserAllowed, EnvUtil.isRootUserAllowed());
	}

	@Test
	public void getSystemProperty() {
		assertEquals(javaHome, EnvUtil.getSystemProperty("java.home"));
	}

	@Test
	public void getSystemPropertyWithDefault() {
		String defualt = "java_home_path";
		assertEquals(javaHome, EnvUtil.getSystemProperty("java.home", defualt));
		assertEquals(defualt, EnvUtil.getSystemProperty("java.home_not_exist", defualt));
	}


}