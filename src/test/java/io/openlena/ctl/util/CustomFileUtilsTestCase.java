/*
 * Copyright 2021 LENA Development Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.openlena.ctl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.NameFileFilter;

import io.openlena.ctl.util.testtools.FileBasedTestCase;

/**
 * This is used to test CustomFileUtils for correctness.
 *
 * @version $Id$
 * @see CustomFileUtils
 */
public class CustomFileUtilsTestCase extends FileBasedTestCase {

    // Test data

    /**
     * Size of test directory.
     */
    private static final int TEST_DIRECTORY_SIZE = 0;
    
    /**
     * List files recursively
     */
    private static final ListDirectoryWalker LIST_WALKER = new ListDirectoryWalker();

    /** Delay in milliseconds to make sure test for "last modified date" are accurate */
    //private static final int LAST_MODIFIED_DELAY = 600;

    private File testFile1;
    private File testFile2;

    private int testFile1Size;
    private int testFile2Size;

    public CustomFileUtilsTestCase(String name) {
        super(name);

        testFile1 = new File(getTestDirectory(), "file1-test.txt");
        testFile2 = new File(getTestDirectory(), "file1a-test.txt");

        testFile1Size = (int)testFile1.length();
        testFile2Size = (int)testFile2.length();
    }

    /** @see junit.framework.TestCase#setUp() */
    @Override
    protected void setUp() throws Exception {
        getTestDirectory().mkdirs();
        createFile(testFile1, testFile1Size);
        createFile(testFile2, testFile2Size);
        CustomFileUtils.deleteDirectory(getTestDirectory());
        getTestDirectory().mkdirs();
        createFile(testFile1, testFile1Size);
        createFile(testFile2, testFile2Size);
    }

    /** @see junit.framework.TestCase#tearDown() */
    @Override
    protected void tearDown() throws Exception {
        CustomFileUtils.deleteDirectory(getTestDirectory());
    }

    //-----------------------------------------------------------------------
    public void test_openInputStream_exists() throws Exception {
        File file = new File(getTestDirectory(), "test.txt");
        createLineBasedFile(file, new String[] {"Hello"});
        FileInputStream in = null;
        try {
            in = CustomFileUtils.openInputStream(file);
            assertEquals('H', in.read());
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public void test_openInputStream_existsButIsDirectory() throws Exception {
        File directory = new File(getTestDirectory(), "subdir");
        directory.mkdirs();
        FileInputStream in = null;
        try {
            in = CustomFileUtils.openInputStream(directory);
            fail();
        } catch (IOException ioe) {
            // expected
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public void test_openInputStream_notExists() throws Exception {
        File directory = new File(getTestDirectory(), "test.txt");
        FileInputStream in = null;
        try {
            in = CustomFileUtils.openInputStream(directory);
            fail();
        } catch (IOException ioe) {
            // expected
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    //-----------------------------------------------------------------------
    void openOutputStream_noParent(boolean createFile) throws Exception {
        File file = new File("test.txt");
        assertNull(file.getParentFile());
        try {
            if (createFile) {
            createLineBasedFile(file, new String[]{"Hello"});}
            FileOutputStream out = null;
            try {
                out = CustomFileUtils.openOutputStream(file);
                out.write(0);
            } finally {
                IOUtils.closeQuietly(out);
            }
            assertTrue(file.exists());
        } finally {
            if (file.delete() == false) {
                file.deleteOnExit();
            }
        }
    }

    public void test_openOutputStream_noParentCreateFile() throws Exception {
        openOutputStream_noParent(true);
    }

    public void test_openOutputStream_noParentNoFile() throws Exception {
        openOutputStream_noParent(false);
    }


    public void test_openOutputStream_exists() throws Exception {
        File file = new File(getTestDirectory(), "test.txt");
        createLineBasedFile(file, new String[] {"Hello"});
        FileOutputStream out = null;
        try {
            out = CustomFileUtils.openOutputStream(file);
            out.write(0);
        } finally {
            IOUtils.closeQuietly(out);
        }
        assertTrue(file.exists());
    }

    public void test_openOutputStream_existsButIsDirectory() throws Exception {
        File directory = new File(getTestDirectory(), "subdir");
        directory.mkdirs();
        FileOutputStream out = null;
        try {
            out = CustomFileUtils.openOutputStream(directory);
            fail();
        } catch (IOException ioe) {
            // expected
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    public void test_openOutputStream_notExists() throws Exception {
        File file = new File(getTestDirectory(), "a/test.txt");
        FileOutputStream out = null;
        try {
            out = CustomFileUtils.openOutputStream(file);
            out.write(0);
        } finally {
            IOUtils.closeQuietly(out);
        }
        assertTrue(file.exists());
    }

    public void test_openOutputStream_notExistsCannotCreate() throws Exception {
        // according to Wikipedia, most filing systems have a 256 limit on filename
        String longStr =
            "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
            "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
            "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
            "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
            "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz" +
            "abcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyzabcdevwxyz";  // 300 chars
        File file = new File(getTestDirectory(), "a/" + longStr + "/test.txt");
        FileOutputStream out = null;
        try {
            out = CustomFileUtils.openOutputStream(file);
            fail();
        } catch (IOException ioe) {
            // expected
        } finally {
            IOUtils.closeQuietly(out);
        }
    }


    // copyFile

    public void testCopyDirectoryFiltered() throws Exception {
        File grandParentDir = new File(getTestDirectory(), "grandparent");
        File parentDir      = new File(grandParentDir, "parent");
        File childDir       = new File(parentDir, "child");
        createFilesForTestCopyDirectory(grandParentDir, parentDir, childDir);

        NameFileFilter filter = new NameFileFilter(new String[] {"parent", "child", "file3.txt"});
        File destDir       = new File(getTestDirectory(), "copydest");

        CustomFileUtils.copyDirectory(grandParentDir, destDir, filter);
        List<File> files  = LIST_WALKER.list(destDir);
        assertEquals(3, files.size());
        assertEquals("parent", files.get(0).getName());
        assertEquals("child", files.get(1).getName());
        assertEquals("file3.txt", files.get(2).getName());
   }

    public void testCopyDirectoryPreserveDates() throws Exception {
        File source = new File(getTestDirectory(), "source");
        File sourceDirectory = new File(source, "directory");
        File sourceFile = new File(sourceDirectory, "hello.txt");

        // Prepare source data
        source.mkdirs();
        sourceDirectory.mkdir();
        CustomFileUtils.writeStringToFile(sourceFile, "HELLO WORLD", "UTF8");
        // Set dates in reverse order to avoid overwriting previous values
        // Also, use full seconds (arguments are in ms) close to today
        // but still highly unlikely to occur in the real world
        sourceFile.setLastModified(1000000002000L);
        sourceDirectory.setLastModified(1000000001000L);
        source.setLastModified(1000000000000L);

        File target = new File(getTestDirectory(), "target");
        File targetDirectory = new File(target, "directory");
        File targetFile = new File(targetDirectory, "hello.txt");

        // Test with preserveFileDate disabled
        CustomFileUtils.copyDirectory(source, target, false);
        assertTrue(1000000000000L != target.lastModified());
        assertTrue(1000000001000L != targetDirectory.lastModified());
        assertTrue(1000000002000L != targetFile.lastModified());
        CustomFileUtils.deleteDirectory(target);

        // Test with preserveFileDate enabled
        CustomFileUtils.copyDirectory(source, target, true);
        assertEquals(1000000000000L, target.lastModified());
        assertEquals(1000000001000L, targetDirectory.lastModified());
        assertEquals(1000000002000L, targetFile.lastModified());
        CustomFileUtils.deleteDirectory(target);

        // also if the target directory already exists (IO-190)
        target.mkdirs();
        CustomFileUtils.copyDirectory(source, target, true);
        assertEquals(1000000000000L, target.lastModified());
        assertEquals(1000000001000L, targetDirectory.lastModified());
        assertEquals(1000000002000L, targetFile.lastModified());
        CustomFileUtils.deleteDirectory(target);

        // also if the target subdirectory already exists (IO-190)
        targetDirectory.mkdirs();
        CustomFileUtils.copyDirectory(source, target, true);
        assertEquals(1000000000000L, target.lastModified());
        assertEquals(1000000001000L, targetDirectory.lastModified());
        assertEquals(1000000002000L, targetFile.lastModified());
        CustomFileUtils.deleteDirectory(target);
    }

    private void createFilesForTestCopyDirectory(File grandParentDir, File parentDir, File childDir) throws Exception {
        File childDir2 = new File(parentDir, "child2");
        File grandChildDir = new File(childDir, "grandChild");
        File grandChild2Dir = new File(childDir2, "grandChild2");
        File file1 = new File(grandParentDir, "file1.txt");
        File file2 = new File(parentDir, "file2.txt");
        File file3 = new File(childDir, "file3.txt");
        File file4 = new File(childDir2, "file4.txt");
        File file5 = new File(grandChildDir, "file5.txt");
        File file6 = new File(grandChild2Dir, "file6.txt");
        CustomFileUtils.deleteDirectory(grandParentDir);
        grandChildDir.mkdirs();
        grandChild2Dir.mkdirs();
        CustomFileUtils.writeStringToFile(file1, "File 1 in grandparent", "UTF8");
        CustomFileUtils.writeStringToFile(file2, "File 2 in parent", "UTF8");
        CustomFileUtils.writeStringToFile(file3, "File 3 in child", "UTF8");
        CustomFileUtils.writeStringToFile(file4, "File 4 in child2", "UTF8");
        CustomFileUtils.writeStringToFile(file5, "File 5 in grandChild", "UTF8");
        CustomFileUtils.writeStringToFile(file6, "File 6 in grandChild2", "UTF8");
    }

    public void testCopyDirectoryErrors() throws Exception {
        try {
            CustomFileUtils.copyDirectory(null, null);
            fail();
        } catch (NullPointerException ex) {}
        try {
            CustomFileUtils.copyDirectory(new File("a"), null);
            fail();
        } catch (NullPointerException ex) {}
        try {
            CustomFileUtils.copyDirectory(null, new File("a"));
            fail();
        } catch (NullPointerException ex) {}
        try {
            CustomFileUtils.copyDirectory(new File("doesnt-exist"), new File("a"));
            fail();
        } catch (IOException ex) {}
        try {
            CustomFileUtils.copyDirectory(testFile1, new File("a"));
            fail();
        } catch (IOException ex) {}
        try {
            CustomFileUtils.copyDirectory(getTestDirectory(), testFile1);
            fail();
        } catch (IOException ex) {}
        try {
            CustomFileUtils.copyDirectory(getTestDirectory(), getTestDirectory());
            fail();
        } catch (IOException ex) {}
    }

    public void testForceDeleteAFile1() throws Exception {
        File destination = new File(getTestDirectory(), "copy1.txt");
        destination.createNewFile();
        assertTrue("Copy1.txt doesn't exist to delete", destination.exists());
        CustomFileUtils.forceDelete(destination);
        assertTrue("Check No Exist", !destination.exists());
    }

    public void testForceDeleteAFile2() throws Exception {
        File destination = new File(getTestDirectory(), "copy2.txt");
        destination.createNewFile();
        assertTrue("Copy2.txt doesn't exist to delete", destination.exists());
        CustomFileUtils.forceDelete(destination);
        assertTrue("Check No Exist", !destination.exists());
    }

    public void testForceDeleteAFile3() throws Exception {
        File destination = new File(getTestDirectory(), "no_such_file");
        assertTrue("Check No Exist", !destination.exists());
        try {
            CustomFileUtils.forceDelete(destination);
            fail("Should generate FileNotFoundException");
        } catch (FileNotFoundException ignored){
        }
    }

    public void testForceDeleteDir() throws Exception {
        File testDirectory = getTestDirectory();
        CustomFileUtils.forceDelete(testDirectory.getParentFile());
        assertTrue(
            "Check No Exist",
            !testDirectory.getParentFile().exists());
    }

    /**
     *  Test the CustomFileUtils implementation.
     */
    public void testCustomFileUtils() throws Exception {
        // Loads file from classpath
        File file1 = new File(getTestDirectory(), "test.txt");
        String filename = file1.getAbsolutePath();
        
        //Create test file on-the-fly (used to be in CVS)
        OutputStream out = new java.io.FileOutputStream(file1);
        try {
            out.write("This is a test".getBytes("UTF-8"));
        } finally {
            out.close();
        }
        
        File file2 = new File(getTestDirectory(), "test2.txt");

        CustomFileUtils.writeStringToFile(file2, filename, "UTF-8");
        assertTrue(file2.exists());
        assertTrue(file2.length() > 0);

        String file2contents = CustomFileUtils.readFileToString(file2, "UTF-8");
        assertTrue(
            "Second file's contents correct",
            filename.equals(file2contents));

        assertTrue(file2.delete());
        
        String contents = CustomFileUtils.readFileToString(new File(filename), "UTF-8");
        assertEquals("CustomFileUtils.fileRead()", "This is a test", contents);

    }

    public void testReadFileToStringWithDefaultEncoding() throws Exception {
        File file = new File(getTestDirectory(), "read.obj");
        FileOutputStream out = new FileOutputStream(file);
        byte[] text = "Hello /u1234".getBytes();
        out.write(text);
        out.close();
        
        String data = CustomFileUtils.readFileToString(file);
        assertEquals("Hello /u1234", data);
    }

    public void testReadFileToStringWithEncoding() throws Exception {
        File file = new File(getTestDirectory(), "read.obj");
        FileOutputStream out = new FileOutputStream(file);
        byte[] text = "Hello /u1234".getBytes("UTF8");
        out.write(text);
        out.close();
        
        String data = CustomFileUtils.readFileToString(file, "UTF8");
        assertEquals("Hello /u1234", data);
    }

    public void testReadFileToByteArray() throws Exception {
        File file = new File(getTestDirectory(), "read.txt");
        FileOutputStream out = new FileOutputStream(file);
        out.write(11);
        out.write(21);
        out.write(31);
        out.close();
        
        byte[] data = CustomFileUtils.readFileToByteArray(file);
        assertEquals(3, data.length);
        assertEquals(11, data[0]);
        assertEquals(21, data[1]);
        assertEquals(31, data[2]);
    }

    public void testWriteStringToFile1() throws Exception {
        File file = new File(getTestDirectory(), "write.txt");
        CustomFileUtils.writeStringToFile(file, "Hello /u1234", "UTF8");
        byte[] text = "Hello /u1234".getBytes("UTF8");
        assertEqualContent(text, file);
    }

    public void testWriteStringToFile2() throws Exception {
        File file = new File(getTestDirectory(), "write.txt");
        CustomFileUtils.writeStringToFile(file, "Hello /u1234", null);
        byte[] text = "Hello /u1234".getBytes();
        assertEqualContent(text, file);
    }

    public void testWriteCharSequence1() throws Exception {
        File file = new File(getTestDirectory(), "write.txt");
        CustomFileUtils.write(file, "Hello /u1234", "UTF8");
        byte[] text = "Hello /u1234".getBytes("UTF8");
        assertEqualContent(text, file);
    }

    public void testWriteCharSequence2() throws Exception {
        File file = new File(getTestDirectory(), "write.txt");
        CustomFileUtils.write(file, "Hello /u1234", null);
        byte[] text = "Hello /u1234".getBytes();
        assertEqualContent(text, file);
    }

    public void testWriteStringToFileWithEncoding_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        File file = newFile("lines.txt");
        CustomFileUtils.writeStringToFile(file, "This line was there before you...");
        
        CustomFileUtils.writeStringToFile(file, "this is brand new data", null, true);

        String expected = "This line was there before you..."
                + "this is brand new data";
        String actual = CustomFileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    public void testWriteStringToFileWithEncoding_WithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        File file = newFile("lines.txt");
        CustomFileUtils.writeStringToFile(file, "This line was there before you...");
        
        CustomFileUtils.writeStringToFile(file, "this is brand new data", null, false);

        String expected = "this is brand new data";
        String actual = CustomFileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    public void testWriteStringToFile_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        File file = newFile("lines.txt");
        CustomFileUtils.writeStringToFile(file, "This line was there before you...");
        
        CustomFileUtils.writeStringToFile(file, "this is brand new data", true);

        String expected = "This line was there before you..."
                + "this is brand new data";
        String actual = CustomFileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    public void testWriteStringToFile_WithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        File file = newFile("lines.txt");
        CustomFileUtils.writeStringToFile(file, "This line was there before you...");
        
        CustomFileUtils.writeStringToFile(file, "this is brand new data", false);

        String expected = "this is brand new data";
        String actual = CustomFileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }

    public void testWriteWithEncoding_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        File file = newFile("lines.txt");
        CustomFileUtils.writeStringToFile(file, "This line was there before you...");
        
        CustomFileUtils.write(file, "this is brand new data", null, true);

        String expected = "This line was there before you..."
            + "this is brand new data";
        String actual = CustomFileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }
    
    public void testWriteWithEncoding_WithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        File file = newFile("lines.txt");
        CustomFileUtils.writeStringToFile(file, "This line was there before you...");
        
        CustomFileUtils.write(file, "this is brand new data", null, false);

        String expected = "this is brand new data";
        String actual = CustomFileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }
    
    public void testWrite_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines() throws Exception {
        File file = newFile("lines.txt");
        CustomFileUtils.writeStringToFile(file, "This line was there before you...");
        
        CustomFileUtils.write(file, "this is brand new data", true);

        String expected = "This line was there before you..."
            + "this is brand new data";
        String actual = CustomFileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }
    
    public void testWrite_WithAppendOptionFalse_ShouldDeletePreviousFileLines() throws Exception {
        File file = newFile("lines.txt");
        CustomFileUtils.writeStringToFile(file, "This line was there before you...");
        
        CustomFileUtils.write(file, "this is brand new data", false);

        String expected = "this is brand new data";
        String actual = CustomFileUtils.readFileToString(file);
        assertEquals(expected, actual);
    }
    
    public void testDeleteQuietlyForNull() {
        try {
            CustomFileUtils.deleteQuietly(null);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    public void testDeleteQuietlyDir() throws IOException {
        File testDirectory = new File(getTestDirectory(), "testDeleteQuietlyDir");
        File testFile= new File(testDirectory, "testDeleteQuietlyFile");
        testDirectory.mkdirs();
        createFile(testFile, 0);

        assertTrue(testDirectory.exists());
        assertTrue(testFile.exists());
        CustomFileUtils.deleteQuietly(testDirectory);
        assertFalse("Check No Exist", testDirectory.exists());
        assertFalse("Check No Exist", testFile.exists());
    }

    public void testDeleteQuietlyFile() throws IOException {
        File testFile= new File(getTestDirectory(), "testDeleteQuietlyFile");
        createFile(testFile, 0);

        assertTrue(testFile.exists());
        CustomFileUtils.deleteQuietly(testFile);
        assertFalse("Check No Exist", testFile.exists());
    }

    public void testDeleteQuietlyNonExistent() {
        File testFile = new File("testDeleteQuietlyNonExistent");
        assertFalse(testFile.exists());
        
        try {
            CustomFileUtils.deleteQuietly(testFile);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * DirectoryWalker implementation that recursively lists all files and directories.
     */
    static class ListDirectoryWalker extends DirectoryWalker<File> {
        ListDirectoryWalker() {
            super();
        }
        List<File> list(File startDirectory) throws IOException {
            ArrayList<File> files = new ArrayList<File>();
            walk(startDirectory, files);
            return files;
        }

        @Override
        protected void handleDirectoryStart(File directory, int depth, Collection<File> results) throws IOException {
            // Add all directories except the starting directory
            if (depth > 0) {
                results.add(directory);
            }
        }

        @Override
        protected void handleFile(File file, int depth, Collection<File> results) throws IOException {
            results.add(file);
        }
    }

}