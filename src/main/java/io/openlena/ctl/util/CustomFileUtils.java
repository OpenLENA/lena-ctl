/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Modifications copyright 2021 LENA Development Team.
 */

package io.openlena.ctl.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 * General file manipulation utilities.
 * <p>
 * Facilities are provided in the following areas:
 * <ul>
 * <li>writing to a file
 * <li>reading from a file
 * <li>make a directory including parent directories
 * <li>copying files and directories
 * <li>deleting files and directories
 * <li>converting to and from a URL
 * <li>listing files and directories by filter and extension
 * <li>comparing file content
 * <li>file last changed date
 * <li>calculating a checksum
 * </ul>
 * <p>
 * Origin of code: Excalibur, Alexandria, Commons-Utils
 *
 * @version $Id: FileUtils.java 1304052 2012-03-22 20:55:29Z ggregory $
 */
public class CustomFileUtils {

	/**
	 * The number of bytes in a kilobyte.
	 */
	public static final long ONE_KB = 1024;

	/**
	 * The number of bytes in a megabyte.
	 */
	public static final long ONE_MB = ONE_KB * ONE_KB;

	/**
	 * The file copy buffer size (30 MB)
	 */
	private static final long FILE_COPY_BUFFER_SIZE = ONE_MB * 30;

	/**
	 * The number of bytes in a gigabyte.
	 */
	public static final long ONE_GB = ONE_KB * ONE_MB;

	/**
	 * The number of bytes in a terabyte.
	 */
	public static final long ONE_TB = ONE_KB * ONE_GB;

	/**
	 * The number of bytes in a petabyte.
	 */
	public static final long ONE_PB = ONE_KB * ONE_TB;

	/**
	 * The number of bytes in an exabyte.
	 */
	public static final long ONE_EB = ONE_KB * ONE_PB;

	/**
	 * The number of bytes in a zettabyte.
	 */
	public static final BigInteger ONE_ZB = BigInteger.valueOf(ONE_KB).multiply(BigInteger.valueOf(ONE_EB));

	/**
	 * The number of bytes in a yottabyte.
	 */
	public static final BigInteger ONE_YB = ONE_ZB.multiply(BigInteger.valueOf(ONE_EB));

	private static final boolean USE_NIO = Boolean.parseBoolean(InstallConfigUtil.getProperty("filecopy.use.nio", "true"));

	// -----------------------------------------------------------------------
	/**
	 * Deletes a directory recursively.
	 *
	 * @param directory directory to delete
	 * @throws IOException in case deletion is unsuccessful
	 */
	public static void deleteDirectory(File directory) throws IOException {
		if (!directory.exists()) {
			return;
		}

		if (!isSymlink(directory)) {
			cleanDirectory(directory);
		}

		if (!directory.delete()) {
			String message = "Unable to delete directory " + directory + ".";
			throw new IOException(message);
		}
	}

	/**
	 * Deletes a file, never throwing an exception. If file is a directory, delete it and
	 * all sub-directories.
	 * <p>
	 * The difference between File.delete() and this method are:
	 * <ul>
	 * <li>A directory to be deleted does not have to be empty.</li>
	 * <li>No exceptions are thrown when a file or directory cannot be deleted.</li>
	 * </ul>
	 *
	 * @param file file or directory to delete, can be <code>null</code>
	 * @return <code>true</code> if the file or directory was deleted, otherwise
	 * <code>false</code>
	 *
	 * @since 1.4
	 */
	public static boolean deleteQuietly(File file) {
		if (file == null) {
			return false;
		}
		try {
			if (file.isDirectory()) {
				cleanDirectory(file);
			}
		}
		catch (Exception ignored) {
		}

		try {
			return file.delete();
		}
		catch (Exception ignored) {
			return false;
		}
	}

	// -----------------------------------------------------------------------
	/**
	 * Reads the contents of a file into a String. The file is always closed.
	 *
	 * @param file the file to read, must not be <code>null</code>
	 * @param encoding the encoding to use, <code>null</code> means platform default
	 * @return the file contents, never <code>null</code>
	 * @throws IOException in case of an I/O error
	 * @throws java.io.UnsupportedEncodingException if the encoding is not supported by
	 * the VM
	 */
	public static String readFileToString(File file, String encoding) throws IOException {
		InputStream in = null;
		try {
			in = openInputStream(file);
			return IOUtils.toString(in, encoding);
		}
		finally {
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * Reads the contents of a file into a String using the default encoding for the VM.
	 * The file is always closed.
	 *
	 * @param file the file to read, must not be <code>null</code>
	 * @return the file contents, never <code>null</code>
	 * @throws IOException in case of an I/O error
	 * @since 1.3.1
	 */
	public static String readFileToString(File file) throws IOException {
		return readFileToString(file, null);
	}

	/**
	 * Reads the contents of a file into a byte array. The file is always closed.
	 *
	 * @param file the file to read, must not be <code>null</code>
	 * @return the file contents, never <code>null</code>
	 * @throws IOException in case of an I/O error
	 * @since 1.1
	 */
	public static byte[] readFileToByteArray(File file) throws IOException {
		InputStream in = null;
		try {
			in = openInputStream(file);
			return IOUtils.toByteArray(in, file.length());
		}
		finally {
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * Writes a CharSequence to a file creating the file if it does not exist using the
	 * default encoding for the VM.
	 *
	 * @param file the file to write
	 * @param data the content to write to the file
	 * @throws IOException in case of an I/O error
	 * @since 2.0
	 */
	public static void write(File file, CharSequence data) throws IOException {
		write(file, data, null, false);
	}

	/**
	 * Writes a CharSequence to a file creating the file if it does not exist using the
	 * default encoding for the VM.
	 *
	 * @param file the file to write
	 * @param data the content to write to the file
	 * @param append if <code>true</code>, then the data will be added to the end of the
	 * file rather than overwriting
	 * @throws IOException in case of an I/O error
	 * @since 2.1
	 */
	public static void write(File file, CharSequence data, boolean append) throws IOException {
		write(file, data, null, append);
	}

	/**
	 * Writes a CharSequence to a file creating the file if it does not exist.
	 *
	 * @param file the file to write
	 * @param data the content to write to the file
	 * @param encoding the encoding to use, <code>null</code> means platform default
	 * @throws IOException in case of an I/O error
	 * @throws java.io.UnsupportedEncodingException if the encoding is not supported by
	 * the VM
	 * @since 2.0
	 */
	public static void write(File file, CharSequence data, String encoding) throws IOException {
		write(file, data, encoding, false);
	}

	/**
	 * Writes a CharSequence to a file creating the file if it does not exist.
	 *
	 * @param file the file to write
	 * @param data the content to write to the file
	 * @param encoding the encoding to use, <code>null</code> means platform default
	 * @param append if <code>true</code>, then the data will be added to the end of the
	 * file rather than overwriting
	 * @throws IOException in case of an I/O error
	 * @throws java.io.UnsupportedEncodingException if the encoding is not supported by
	 * the VM
	 * @since IO 2.1
	 */
	public static void write(File file, CharSequence data, String encoding, boolean append) throws IOException {
		String str = data == null ? null : data.toString();
		writeStringToFile(file, str, encoding, append);
	}

	// -----------------------------------------------------------------------
	/**
	 * Writes a String to a file creating the file if it does not exist.
	 *
	 * NOTE: As from v1.3, the parent directories of the file will be created if they do
	 * not exist.
	 *
	 * @param file the file to write
	 * @param data the content to write to the file
	 * @param encoding the encoding to use, <code>null</code> means platform default
	 * @throws IOException in case of an I/O error
	 * @throws java.io.UnsupportedEncodingException if the encoding is not supported by
	 * the VM
	 */
	public static void writeStringToFile(File file, String data, String encoding) throws IOException {
		writeStringToFile(file, data, encoding, false);
	}

	/**
	 * Writes a String to a file creating the file if it does not exist.
	 *
	 * @param file the file to write
	 * @param data the content to write to the file
	 * @param encoding the encoding to use, <code>null</code> means platform default
	 * @param append if <code>true</code>, then the String will be added to the end of the
	 * file rather than overwriting
	 * @throws IOException in case of an I/O error
	 * @throws java.io.UnsupportedEncodingException if the encoding is not supported by
	 * the VM
	 * @since 2.1
	 */
	public static void writeStringToFile(File file, String data, String encoding, boolean append) throws IOException {
		OutputStream out = null;
		try {
			out = openOutputStream(file, append);
			IOUtils.write(data, out, encoding);
			out.close(); // don't swallow close Exception if copy completes normally
		}
		finally {
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * Writes a String to a file creating the file if it does not exist using the default
	 * encoding for the VM.
	 *
	 * @param file the file to write
	 * @param data the content to write to the file
	 * @throws IOException in case of an I/O error
	 */
	public static void writeStringToFile(File file, String data) throws IOException {
		writeStringToFile(file, data, null, false);
	}

	/**
	 * Writes a String to a file creating the file if it does not exist using the default
	 * encoding for the VM.
	 *
	 * @param file the file to write
	 * @param data the content to write to the file
	 * @param append if <code>true</code>, then the String will be added to the end of the
	 * file rather than overwriting
	 * @throws IOException in case of an I/O error
	 * @since 2.1
	 */
	public static void writeStringToFile(File file, String data, boolean append) throws IOException {
		writeStringToFile(file, data, null, append);
	}

	/**
	 * Determines whether the specified file is a Symbolic Link rather than an actual
	 * file.
	 * <p>
	 * Will not return true if there is a Symbolic Link anywhere in the path, only if the
	 * specific file is.
	 * <p>
	 * <b>Note:</b> the current implementation always returns {@code false} if the system
	 * is detected as Windows using {@link FilenameUtils#isSystemWindows()}
	 *
	 * @param file the file to check
	 * @return true if the file is a Symbolic Link
	 * @throws IOException if an IO error occurs while checking the file
	 * @since 2.0
	 */
	public static boolean isSymlink(File file) throws IOException {
		if (file == null) {
			throw new NullPointerException("File must not be null");
		}
		// isWindows 대체
		// 기존소스는 FileNamesUtils.isWindows
		if (File.separatorChar == '\\') {
			return false;
		}
		File fileInCanonicalDir = null;
		if (file.getParent() == null) {
			fileInCanonicalDir = file;
		}
		else {
			File canonicalDir = file.getParentFile().getCanonicalFile();
			fileInCanonicalDir = new File(canonicalDir, file.getName());
		}

		if (fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile())) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Cleans a directory without deleting it.
	 *
	 * @param directory directory to clean
	 * @throws IOException in case cleaning is unsuccessful
	 */
	public static void cleanDirectory(File directory) throws IOException {
		if (!directory.exists()) {
			String message = directory + " does not exist";
			throw new IllegalArgumentException(message);
		}

		if (!directory.isDirectory()) {
			String message = directory + " is not a directory";
			throw new IllegalArgumentException(message);
		}

		File[] files = directory.listFiles();
		if (files == null) { // null if security restricted
			throw new IOException("Failed to list contents of " + directory);
		}

		IOException exception = null;
		for (File file : files) {
			try {
				forceDelete(file);
			}
			catch (IOException ioe) {
				exception = ioe;
			}
		}

		if (null != exception) {
			throw exception;
		}
	}

	// -----------------------------------------------------------------------
	/**
	 * Opens a {@link FileInputStream} for the specified file, providing better error
	 * messages than simply calling <code>new FileInputStream(file)</code>.
	 * <p>
	 * At the end of the method either the stream will be successfully opened, or an
	 * exception will have been thrown.
	 * <p>
	 * An exception is thrown if the file does not exist. An exception is thrown if the
	 * file object exists but is a directory. An exception is thrown if the file exists
	 * but cannot be read.
	 *
	 * @param file the file to open for input, must not be <code>null</code>
	 * @return a new {@link FileInputStream} for the specified file
	 * @throws FileNotFoundException if the file does not exist
	 * @throws IOException if the file object is a directory
	 * @throws IOException if the file cannot be read
	 * @since 1.3
	 */
	public static FileInputStream openInputStream(File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if (!file.canRead()) {
				throw new IOException("File '" + file + "' cannot be read");
			}
		}
		else {
			throw new FileNotFoundException("File '" + file + "' does not exist");
		}
		return new FileInputStream(file);
	}

	// -----------------------------------------------------------------------
	/**
	 * Opens a {@link FileOutputStream} for the specified file, checking and creating the
	 * parent directory if it does not exist.
	 * <p>
	 * At the end of the method either the stream will be successfully opened, or an
	 * exception will have been thrown.
	 * <p>
	 * The parent directory will be created if it does not exist. The file will be created
	 * if it does not exist. An exception is thrown if the file object exists but is a
	 * directory. An exception is thrown if the file exists but cannot be written to. An
	 * exception is thrown if the parent directory cannot be created.
	 *
	 * @param file the file to open for output, must not be <code>null</code>
	 * @return a new {@link FileOutputStream} for the specified file
	 * @throws IOException if the file object is a directory
	 * @throws IOException if the file cannot be written to
	 * @throws IOException if a parent directory needs creating but that fails
	 * @since 1.3
	 */
	public static FileOutputStream openOutputStream(File file) throws IOException {
		return openOutputStream(file, false);
	}

	/**
	 * Opens a {@link FileOutputStream} for the specified file, checking and creating the
	 * parent directory if it does not exist.
	 * <p>
	 * At the end of the method either the stream will be successfully opened, or an
	 * exception will have been thrown.
	 * <p>
	 * The parent directory will be created if it does not exist. The file will be created
	 * if it does not exist. An exception is thrown if the file object exists but is a
	 * directory. An exception is thrown if the file exists but cannot be written to. An
	 * exception is thrown if the parent directory cannot be created.
	 *
	 * @param file the file to open for output, must not be <code>null</code>
	 * @param append if <code>true</code>, then bytes will be added to the end of the file
	 * rather than overwriting
	 * @return a new {@link FileOutputStream} for the specified file
	 * @throws IOException if the file object is a directory
	 * @throws IOException if the file cannot be written to
	 * @throws IOException if a parent directory needs creating but that fails
	 * @since 2.1
	 */
	public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if (!file.canWrite()) {
				throw new IOException("File '" + file + "' cannot be written to");
			}
		}
		else {
			File parent = file.getParentFile();
			if (parent != null) {
				if (!parent.mkdirs() && !parent.isDirectory()) {
					throw new IOException("Directory '" + parent + "' could not be created");
				}
			}
		}
		return new FileOutputStream(file, append);
	}

	// -----------------------------------------------------------------------
	/**
	 * Deletes a file. If file is a directory, delete it and all sub-directories.
	 * <p>
	 * The difference between File.delete() and this method are:
	 * <ul>
	 * <li>A directory to be deleted does not have to be empty.</li>
	 * <li>You get exceptions when a file or directory cannot be deleted. (java.io.File
	 * methods returns a boolean)</li>
	 * </ul>
	 *
	 * @param file file or directory to delete, must not be <code>null</code>
	 * @throws NullPointerException if the directory is <code>null</code>
	 * @throws FileNotFoundException if the file was not found
	 * @throws IOException in case deletion is unsuccessful
	 */
	public static void forceDelete(File file) throws IOException {
		if (file.isDirectory()) {
			deleteDirectory(file);
		}
		else {
			boolean filePresent = file.exists();
			if (!file.delete()) {
				if (!filePresent) {
					throw new FileNotFoundException("File does not exist: " + file);
				}
				String message = "Unable to delete file: " + file;
				throw new IOException(message);
			}
		}
	}

	public static void copyDirectory(File srcDir, File destDir) throws IOException {
		copyDirectory(srcDir, destDir, true);
	}

	/**
	 * Copies a whole directory to a new location.
	 * <p>
	 * This method copies the contents of the specified source directory to within the
	 * specified destination directory.
	 * <p>
	 * The destination directory is created if it does not exist. If the destination
	 * directory did exist, then this method merges the source with the destination, with
	 * the source taking precedence.
	 * <p>
	 * <strong>Note:</strong> Setting <code>preserveFileDate</code> to <code>true</code>
	 * tries to preserve the files' last modified date/times using
	 * {@link File#setLastModified(long)}, however it is not guaranteed that those
	 * operations will succeed. If the modification operation fails, no indication is
	 * provided.
	 *
	 * @param srcDir an existing directory to copy, must not be <code>null</code>
	 * @param destDir the new directory, must not be <code>null</code>
	 * @param preserveFileDate true if the file date of the copy should be the same as the
	 * original
	 *
	 * @throws NullPointerException if source or destination is <code>null</code>
	 * @throws IOException if source or destination is invalid
	 * @throws IOException if an IO error occurs during copying
	 * @since 1.1
	 */
	public static void copyDirectory(File srcDir, File destDir, boolean preserveFileDate) throws IOException {
		copyDirectory(srcDir, destDir, null, preserveFileDate);
	}

	/**
	 * Copies a filtered directory to a new location preserving the file dates.
	 * <p>
	 * This method copies the contents of the specified source directory to within the
	 * specified destination directory.
	 * <p>
	 * The destination directory is created if it does not exist. If the destination
	 * directory did exist, then this method merges the source with the destination, with
	 * the source taking precedence.
	 * <p>
	 * <strong>Note:</strong> This method tries to preserve the files' last modified
	 * date/times using {@link File#setLastModified(long)}, however it is not guaranteed
	 * that those operations will succeed. If the modification operation fails, no
	 * indication is provided.
	 *
	 * <h4>Example: Copy directories only</h4>
	 *
	 * <pre>
	 * // only copy the directory structure
	 * FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY);
	 * </pre>
	 *
	 * <h4>Example: Copy directories and txt files</h4>
	 *
	 * <pre>
	 * // Create a filter for ".txt" files
	 * IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(".txt");
	 * IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
	 *
	 * // Create a filter for either directories or ".txt" files
	 * FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
	 *
	 * // Copy using the filter
	 * FileUtils.copyDirectory(srcDir, destDir, filter);
	 * </pre>
	 *
	 * @param srcDir an existing directory to copy, must not be <code>null</code>
	 * @param destDir the new directory, must not be <code>null</code>
	 * @param filter the filter to apply, null means copy all directories and files should
	 * be the same as the original
	 *
	 * @throws NullPointerException if source or destination is <code>null</code>
	 * @throws IOException if source or destination is invalid
	 * @throws IOException if an IO error occurs during copying
	 * @since 1.4
	 */
	public static void copyDirectory(File srcDir, File destDir, FileFilter filter) throws IOException {
		copyDirectory(srcDir, destDir, filter, true);
	}

	/**
	 * Copies a filtered directory to a new location.
	 * <p>
	 * This method copies the contents of the specified source directory to within the
	 * specified destination directory.
	 * <p>
	 * The destination directory is created if it does not exist. If the destination
	 * directory did exist, then this method merges the source with the destination, with
	 * the source taking precedence.
	 * <p>
	 * <strong>Note:</strong> Setting <code>preserveFileDate</code> to <code>true</code>
	 * tries to preserve the files' last modified date/times using
	 * {@link File#setLastModified(long)}, however it is not guaranteed that those
	 * operations will succeed. If the modification operation fails, no indication is
	 * provided.
	 *
	 * <h4>Example: Copy directories only</h4>
	 *
	 * <pre>
	 * // only copy the directory structure
	 * FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY, false);
	 * </pre>
	 *
	 * <h4>Example: Copy directories and txt files</h4>
	 *
	 * <pre>
	 * // Create a filter for ".txt" files
	 * IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(".txt");
	 * IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
	 *
	 * // Create a filter for either directories or ".txt" files
	 * FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
	 *
	 * // Copy using the filter
	 * FileUtils.copyDirectory(srcDir, destDir, filter, false);
	 * </pre>
	 *
	 * @param srcDir an existing directory to copy, must not be <code>null</code>
	 * @param destDir the new directory, must not be <code>null</code>
	 * @param filter the filter to apply, null means copy all directories and files
	 * @param preserveFileDate true if the file date of the copy should be the same as the
	 * original
	 *
	 * @throws NullPointerException if source or destination is <code>null</code>
	 * @throws IOException if source or destination is invalid
	 * @throws IOException if an IO error occurs during copying
	 * @since 1.4
	 */
	public static void copyDirectory(File srcDir, File destDir, FileFilter filter, boolean preserveFileDate) throws IOException {
		if (srcDir == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (destDir == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (!srcDir.exists()) {
			throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
		}
		if (!srcDir.isDirectory()) {
			throw new IOException("Source '" + srcDir + "' exists but is not a directory");
		}
		if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
			throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
		}

		// Cater for destination being directory within the source directory (see IO-141)
		List<String> exclusionList = null;
		if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath())) {
			File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
			if (srcFiles != null && srcFiles.length > 0) {
				exclusionList = new ArrayList<String>(srcFiles.length);
				for (File srcFile : srcFiles) {
					File copiedFile = new File(destDir, srcFile.getName());
					exclusionList.add(copiedFile.getCanonicalPath());
				}
			}
		}
		doCopyDirectory(srcDir, destDir, filter, preserveFileDate, exclusionList);
	}

	/**
	 * Internal copy directory method.
	 *
	 * @param srcDir the validated source directory, must not be <code>null</code>
	 * @param destDir the validated destination directory, must not be <code>null</code>
	 * @param filter the filter to apply, null means copy all directories and files
	 * @param preserveFileDate whether to preserve the file date
	 * @param exclusionList List of files and directories to exclude from the copy, may be null
	 * @throws IOException if an error occurs
	 * @since 1.1
	 */
	private static void doCopyDirectory(File srcDir, File destDir, FileFilter filter,
			boolean preserveFileDate, List<String> exclusionList) throws IOException {
		// recurse
		File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
		if (srcFiles == null) { // null if abstract pathname does not denote a directory,
								// or if an I/O
								// error occurs
			throw new IOException("Failed to list contents of " + srcDir);
		}
		if (destDir.exists()) {
			if (!destDir.isDirectory()) {
				throw new IOException("Destination '" + destDir + "' exists but is not a directory");
			}
		}
		else {
			if (!destDir.mkdirs() && !destDir.isDirectory()) {
				throw new IOException("Destination '" + destDir + "' directory cannot be created");
			}
		}
		if (!destDir.canWrite()) {
			throw new IOException("Destination '" + destDir + "' cannot be written to");
		}
		for (File srcFile : srcFiles) {
			File dstFile = new File(destDir, srcFile.getName());
			if (exclusionList == null || !exclusionList.contains(srcFile.getCanonicalPath())) {
				if (srcFile.isDirectory()) {
					doCopyDirectory(srcFile, dstFile, filter, preserveFileDate, exclusionList);
				}
				else {
					doCopyFile(srcFile, dstFile, preserveFileDate);
				}
			}
		}

		// Do this last, as the above has probably affected directory metadata
		if (preserveFileDate) {
			boolean result = destDir.setLastModified(srcDir.lastModified());
			if (result == false) {
				throw new IOException("Failed to set modified time of " + destDir.getName());
			}
		}
	}

	/**
	 * Internal copy file method.
	 *
	 * @param srcFile the validated source file, must not be <code>null</code>
	 * @param destFile the validated destination file, must not be <code>null</code>
	 * @param preserveFileDate whether to preserve the file date
	 * @throws IOException if an error occurs
	 */
	private static void doCopyFile(File srcFile, File destFile, boolean preserveFileDate)
			throws IOException {
		if (destFile.exists() && destFile.isDirectory()) {
			throw new IOException("Destination '" + destFile + "' exists but is a directory");
		}

		if (USE_NIO) {
			FileInputStream fis = null;
			FileOutputStream fos = null;
			FileChannel input = null;
			FileChannel output = null;
			try {
				fis = new FileInputStream(srcFile);
				fos = new FileOutputStream(destFile);
				input = fis.getChannel();
				output = fos.getChannel();
				long size = input.size();
				long pos = 0;
				long count = 0;
				while (pos < size) {
					count = size - pos > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : size - pos;
					pos += output.transferFrom(input, pos, count);
				}
			}
			finally {
				IOUtils.closeQuietly(output);
				IOUtils.closeQuietly(fos);
				IOUtils.closeQuietly(input);
				IOUtils.closeQuietly(fis);
			}
		}
		else {
			InputStream is = null;
			OutputStream os = null;
			try {
				is = new FileInputStream(srcFile);
				os = new FileOutputStream(destFile);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
			}
			finally {
				if (is != null) {
					try {
						is.close();
					}
					catch (IOException e) {
						// Do Nothing
					}
				}
				if (os != null) {
					try {
						os.close();
					}
					catch (IOException e) {
						// Do Nothing
					}
				}
			}
		}

		if (srcFile.length() != destFile.length()) {
			throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'");
		}
		if (preserveFileDate) {
			boolean result = destFile.setLastModified(srcFile.lastModified());
			if (result == false) {
				throw new IOException("Failed to set modified time of " + destFile.getName());
			}
		}
	}

	/**
     * Allows iteration over the files in given directory (and optionally
     * its subdirectories).
     * <p>
     * All files found are filtered by an IOFileFilter. This method is
     * based on {@link #listFiles(File, IOFileFilter, IOFileFilter)},
     * which supports Iterable ('foreach' loop).
     * <p>
     * @param directory  the directory to search in
     * @param fileFilter  filter to apply when finding files.
     * @param dirFilter  optional filter to apply when finding subdirectories.
     * If this parameter is <code>null</code>, subdirectories will not be included in the
     * search. Use TrueFileFilter.INSTANCE to match all directories.
     * @return an iterator of java.io.File for the matching files
     * @see org.apache.commons.io.filefilter.FileFilterUtils
     * @see org.apache.commons.io.filefilter.NameFileFilter
     * @since 1.2
     */
    public static Iterator<File> iterateFiles(
            File directory, IOFileFilter fileFilter, IOFileFilter dirFilter) {
        return listFiles(directory, fileFilter, dirFilter).iterator();
    }
    
    /**
     * Finds files within a given directory (and optionally its
     * subdirectories). All files found are filtered by an IOFileFilter.
     * <p>
     * If your search should recurse into subdirectories you can pass in
     * an IOFileFilter for directories. You don't need to bind a
     * DirectoryFileFilter (via logical AND) to this filter. This method does
     * that for you.
     * <p>
     * An example: If you want to search through all directories called
     * "temp" you pass in <code>FileFilterUtils.NameFileFilter("temp")</code>
     * <p>
     * Another common usage of this method is find files in a directory
     * tree but ignoring the directories generated CVS. You can simply pass
     * in <code>FileFilterUtils.makeCVSAware(null)</code>.
     *
     * @param directory  the directory to search in
     * @param fileFilter  filter to apply when finding files.
     * @param dirFilter  optional filter to apply when finding subdirectories.
     * If this parameter is <code>null</code>, subdirectories will not be included in the
     * search. Use TrueFileFilter.INSTANCE to match all directories.
     * @return an collection of java.io.File with the matching files
     * @see org.apache.commons.io.filefilter.FileFilterUtils
     * @see org.apache.commons.io.filefilter.NameFileFilter
     */
    public static Collection<File> listFiles(
            File directory, IOFileFilter fileFilter, IOFileFilter dirFilter) {
        validateListFilesParameters(directory, fileFilter);

        IOFileFilter effFileFilter = setUpEffectiveFileFilter(fileFilter);
        IOFileFilter effDirFilter = setUpEffectiveDirFilter(dirFilter);

        //Find files
        Collection<File> files = new java.util.LinkedList<File>();
        innerListFiles(files, directory,
            FileFilterUtils.or(effFileFilter, effDirFilter), false);
        return files;
    }
    
    /**
     * Validates the given arguments.
     * <ul>
     * <li>Throws {@link IllegalArgumentException} if {@code directory} is not a directory</li>
     * <li>Throws {@link NullPointerException} if {@code fileFilter} is null</li>
     * </ul>
     * 
     * @param directory The File to test
     * @param fileFilter The IOFileFilter to test
     */
    private static void validateListFilesParameters(File directory, IOFileFilter fileFilter) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Parameter 'directory' is not a directory");
        }
        if (fileFilter == null) {
            throw new NullPointerException("Parameter 'fileFilter' is null");
        }
    }
    
    /**
     * Returns a filter that accepts files in addition to the {@link File} objects accepted by the given filter.
     * 
     * @param fileFilter a base filter to add to
     * @return a filter that accepts files 
     */
    private static IOFileFilter setUpEffectiveFileFilter(IOFileFilter fileFilter) {
        return FileFilterUtils.and(fileFilter, FileFilterUtils.notFileFilter(DirectoryFileFilter.INSTANCE));
    }

    /**
     * Returns a filter that accepts directories in addition to the {@link File} objects accepted by the given filter.
     * 
     * @param dirFilter a base filter to add to
     * @return a filter that accepts directories 
     */
    private static IOFileFilter setUpEffectiveDirFilter(IOFileFilter dirFilter) {
        return dirFilter == null ? FalseFileFilter.INSTANCE : FileFilterUtils.and(dirFilter,
                DirectoryFileFilter.INSTANCE);
    }

    /**
     * Finds files within a given directory (and optionally its
     * subdirectories). All files found are filtered by an IOFileFilter.
     *
     * @param files the collection of files found.
     * @param directory the directory to search in.
     * @param filter the filter to apply to files and directories.
     * @param includeSubDirectories indicates if will include the subdirectories themselves
     */
    private static void innerListFiles(Collection<File> files, File directory,
            IOFileFilter filter, boolean includeSubDirectories) {
        File[] found = directory.listFiles((FileFilter) filter);
        
        if (found != null) {
            for (File file : found) {
                if (file.isDirectory()) {
                    if (includeSubDirectories) {
                        files.add(file);
                    }
                    innerListFiles(files, file, filter, includeSubDirectories);
                } else {
                    files.add(file);
                }
            }
        }
    }
}
