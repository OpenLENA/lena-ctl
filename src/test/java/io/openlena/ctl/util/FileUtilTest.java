package io.openlena.ctl.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.openlena.ctl.exception.LenaException;
import io.openlena.ctl.util.testtools.FileBasedTestCase;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test FileUtil Class
 *
 * @see FileUtil
 * @author Pinepond
 */
public class FileUtilTest extends FileBasedTestCase {

	final File top = getLocalTestDirectory();

	public FileUtilTest(String name) {
		super(name);
	}

	private File getLocalTestDirectory() {
		return new File(getTestDirectory(), "test-file-util");
	}

	@Override
	protected void setUp() throws Exception {
		top.mkdirs();
	}

	@Override
	protected void tearDown() throws Exception {
		chmod(top, 775, true);
		CustomFileUtils.deleteDirectory(top);
	}

	@Test
	public void testGetConcatPath() {
		// set expected data
		String expectedPath = "path_1" + File.separator + "path_2" + File.separator + "path_3" + File.separator + "path_4";
		// concat path
		String resultPath = FileUtil.getConcatPath("path_1", "path_2", "path_3", "path_4");
		// check result
		assertEquals(expectedPath, resultPath);

		// set expected data
		expectedPath = File.separator + "path_1" + File.separator + "path_2" + File.separator + "path_3" + File.separator + "path_4" + File.separator;
		// concat path
		resultPath = FileUtil.getConcatPath(File.separator + "path_1", "path_2", "path_3", "path_4" + File.separator);
		// check result
		assertEquals(expectedPath, resultPath);

		// check length 0 case
		resultPath = FileUtil.getConcatPath();
		assertEquals("", resultPath);

		// check length 1 case
		resultPath = FileUtil.getConcatPath("path1");
		assertEquals("path1", resultPath);

	}

	/**
	 * test FileUtil.setShellVariable
	 */
	@Test
	public void testSetShellVariable() throws IOException {
		// set data
		String variable = "TEST_SHELL_VARIABLE";
		String defaultValue = "DEFAULT_VALUE";
		String testValue = "TEST_VALUE";

		String defualtData = "export " + variable + "=" + defaultValue + System.lineSeparator() + "set TEST_SHELL_VARIABLE_2=1234" + System.lineSeparator() + "export TEST_SHELL_VARIABLE_3=1234";
		String expectedData = "export " + variable + "=" + testValue + System.lineSeparator() + "set TEST_SHELL_VARIABLE_2=1234" + System.lineSeparator() + "export TEST_SHELL_VARIABLE_3=1234";

		// create shell file
		File testFile = new File(top, "test.sh");
		FileUtil.writeStringToFile(testFile, defualtData);


		// set shell variable - variable exist in shell file
		FileUtil.setShellVariable(testFile.getPath(), variable, testValue);

		//check result
		assertEquals(expectedData, FileUtil.readFileToString(testFile));

		// create shell file 2
		File testFile2 = new File(top, "test2.sh");
		testFile2.createNewFile();

		// set shell variable - variable doesn't exist in shell file
		LenaException exception = assertThrows(LenaException.class, () -> FileUtil.setShellVariable(testFile2.getPath(), variable, testValue));

		// check result
		assertEquals("Fail to set variable '" + variable + "' : '" + testFile2.getPath() + "'", exception.getMessage());
	}

	/**
	 * test FileUtil.setShellVariable - file not exist exception case
	 */
	@Test
	public void testSetShellVariable_fileNotExsit() throws IOException {
		String variable = "TEST_SHELL_VARIABLE";
		String testValue = "TEST_VALUE";

		// create shell file 2
		File testFile = new File(top, "test.sh");

		// set shell variable - variable doesn't exist in shell file
		LenaException exception = assertThrows(LenaException.class, () -> FileUtil.setShellVariable(testFile.getPath(), variable, testValue));

		// check result
		assertEquals("Fail to read file : '" + testFile.getAbsolutePath() + "'", exception.getMessage());
	}

	/**
	 * test file & directory permissions
	 */
	@Test
	public void testChmodF600OD700() throws IOException {
		File testFile = new File(top, "test.txt");

		//file not exist case
		//FileUtil.chmodF600OD700(testFile);

		// create file
		FileUtil.writeStringToFile(testFile, "test");
		System.out.println(testFile.exists());

		// Grant all permissions
		testFile.setReadable(true);
		testFile.setExecutable(true);
		testFile.setWritable(true);

		// change permission
		FileUtil.chmodF600OD700(testFile);

		// check file permissions
		assertTrue(testFile.canWrite());
		assertTrue(testFile.canRead());
	}

	/**
	 * test FileUtil.delete by File Object
	 * @throws IOException
	 */
	@Test
	public void testDeleteByFile() throws IOException {
		File testFile = new File(top, "test.txt");
		// create file
		testFile.createNewFile();
		// check file exit
		assertTrue(testFile.exists());
		// delete file
		FileUtil.delete(testFile);
		// check result
		assertFalse(testFile.exists());

		File testDir = new File(top, "testDir");
		// create file
		testDir.mkdir();
		// check file exit
		assertTrue(testDir.exists());
		// delete file
		FileUtil.delete(testDir);
		// check result
		assertFalse(testDir.exists());
	}

	/**
	 * test FileUtil.delete by File Path
	 * @throws IOException
	 */
	@Test
	public void testDeleteByPath() throws IOException {
		File testFile = new File(top, "test.txt");
		// create file
		testFile.createNewFile();
		// check file exit
		assertTrue(testFile.exists());
		// delete file by path
		FileUtil.delete(testFile.getPath());
		// check result
		assertFalse(testFile.exists());
	}

	/**
	 * test FileUtil.exists
	 * check the file exist or not by file path
	 */
	@Test
	public void testExists() throws IOException {
		File testFile = new File(top, "test.txt");
		// check file exit
		assertFalse(FileUtil.exists(testFile.getPath()));
		// create file
		testFile.createNewFile();
		// check file exit
		assertTrue(FileUtil.exists(testFile.getPath()));
	}

	/**
	 * test FileUtil.replaceText
	 * replace text of the file object
	 */
	@Test
	public void testReplaceTextByFile_file() {
		// set data
		String defaultValue = "DEFAULT_VALUE";
		String testValue = "TEST_VALUE";

		String defualtData = "!@#$%^&*()_" + defaultValue + "!@#$%^&*()_";
		String expectedData = "!@#$%^&*()_" + testValue + "!@#$%^&*()_";

		// create shell file
		File testFile = new File(top, "test.txt");
		FileUtil.writeStringToFile(testFile, defualtData);

		// replace txt
		FileUtil.replaceText(testFile, defaultValue, testValue);

		//check result
		assertEquals(expectedData, FileUtil.readFileToString(testFile));

	}

	/**
	 * test FileUtil.replaceText
	 * replace text of the file object
	 */
	@Test
	public void testReplaceTextByFile_directory() {
		// set data
		String defaultValue = "DEFAULT_VALUE";
		String testValue = "TEST_VALUE";

		String defualtData = "!@#$%^&*()_" + defaultValue + "!@#$%^&*()_";
		String expectedData = "!@#$%^&*()_" + testValue + "!@#$%^&*()_";

		// create file
		File testFile1 = new File(top, "test1.txt");
		FileUtil.writeStringToFile(testFile1, defualtData);

		File testFile2 = new File(top, "test2.txt");
		FileUtil.writeStringToFile(testFile2, defualtData);

		// replace txt
		FileUtil.replaceText(top, defaultValue, testValue);

		//check result
		assertEquals(expectedData, FileUtil.readFileToString(testFile1));
		assertEquals(expectedData, FileUtil.readFileToString(testFile2));

	}

	/**
	 * test FileUtil.replaceText
	 * replace text of the file path
	 */
	@Test
	public void testReplaceTextByPath() {
		// set data
		String defaultValue = "DEFAULT_VALUE";
		String testValue = "TEST_VALUE";

		String defualtData = "!@#$%^&*()_" + defaultValue + "!@#$%^&*()_";
		String expectedData = "!@#$%^&*()_" + testValue + "!@#$%^&*()_";

		// create shell file
		File testFile = new File(top, "test.txt");
		FileUtil.writeStringToFile(testFile, defualtData);

		// replace txt
		FileUtil.replaceText(testFile.getPath(), defaultValue, testValue);

		//check result
		assertEquals(expectedData, FileUtil.readFileToString(testFile));
	}

	@Test
	public void close() {
	}

	@Test
	public void testClose() {
	}

	@Test
	public void testClose1() {
	}

	@Test
	public void testClose2() {
	}

	/**
	 * test FileUtil.readFileToString
	 */
	@Test
	public void testReadFileToString() throws IOException {
		// set data
		String defualtData = "!@#$%^&*()_" + "123456789" + "!@#$%^&*()_";

		// ------------------------------------------------------------------------------
		// create file
		File testFile = new File(top, "test.txt");
		FileUtil.writeStringToFile(testFile.getPath(), defualtData);

		//check result
		assertEquals(defualtData, FileUtil.readFileToString(testFile.getPath()));

		// ------------------------------------------------------------------------------
		// create file - null case
		File testFile2 = new File(top, "test2.txt");
		testFile2.createNewFile();

		//check result
		assertEquals("", FileUtil.readFileToString(testFile2.getPath()));

		// ------------------------------------------------------------------------------
		// create file - file not exit
		File testFile3 = new File(top, "test3.txt");

		// readFileToString
		LenaException exception = assertThrows(LenaException.class, () -> FileUtil.readFileToString(testFile3.getPath()));

		// check result
		assertEquals("Failed to read file '" + testFile3.getAbsolutePath() + "'", exception.getMessage());
	}

	/**
	 * test FileUtil.writeStringToFile
	 */
	@Test
	public void testWriteStringToFile() throws IOException {
		// set data
		String defualtData = "!@#$%^&*()_" + "123456789" + "!@#$%^&*()_";

		// ------------------------------------------------------------------------------
		// create file
		File testFile = new File(top, "test.txt");
		FileUtil.writeStringToFile(testFile.getPath(), defualtData);

		//check result
		assertEquals(defualtData, FileUtil.readFileToString(testFile.getPath()));

		// ------------------------------------------------------------------------------
		// exception case
		// create file
		File testFile2 = new File(top, "test2.txt");

		// readFileToString
		LenaException exception = assertThrows(LenaException.class, () -> FileUtil.writeStringToFile(testFile2, "test", "unknown_encoding"));

		// check result
		assertEquals("Failed to write file '" + testFile2.getAbsolutePath() + "'", exception.getMessage());

	}

	@Test
	public void testCopyDirectory() {
		// set test data
		File dir1 = new File(top, "dir1");
		File dir2 = new File(top, "dir2");

		dir1.mkdir();
		dir2.mkdir();

		File file1 = new File(dir1, "file1");
		File file2 = new File(dir1, "file2");
		File copyFile1 = new File(dir2, "file1");
		File copyFile2 = new File(dir2, "file2");

		FileUtil.writeStringToFile(file1, "data1");
		FileUtil.writeStringToFile(file2, "data2");

		// copy directory
		FileUtil.copyDirectory(dir1.getPath(), dir2.getPath());

		// check result
		assertTrue(copyFile1.exists());
		assertTrue(copyFile2.exists());
		assertEquals("data1", FileUtil.readFileToString(file1));
		assertEquals("data2", FileUtil.readFileToString(file2));

	}

	@Test
	public void chmod755() {
	}

	@Test
	public void testChmod755() {
	}


	private boolean chmod(File file, int mode, boolean recurse) throws InterruptedException {
		// TODO: Refactor this to FileSystemUtils
		List<String> args = new ArrayList<String>();
		args.add("chmod");

		if (recurse) {
			args.add("-R");
		}

		args.add(Integer.toString(mode));
		args.add(file.getAbsolutePath());

		Process proc;

		try {
			proc = Runtime.getRuntime().exec(args.toArray(new String[args.size()]));
		}
		catch (IOException e) {
			return false;
		}
		int result = proc.waitFor();
		return result == 0;
	}
}