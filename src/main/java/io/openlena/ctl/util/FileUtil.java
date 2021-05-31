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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.channels.FileChannel;

import io.openlena.ctl.exception.LenaException;

/**
 * General file manipulation utilities.
 *
 * @author Pinepond
 */
public class FileUtil {
	/**
	 * The values entered as parameters are converted into a single path value.
	 *
	 * @param paths path or directory or filename
	 * @return single path combined with paths
	 */
	public static String getConcatPath(String... paths) {
		if (paths.length == 0) {
			return "";
		}

		if (paths.length == 1) {
			return paths[0];
		}

		if (paths[0] == null) {
			return "";
		}

		StringBuilder concatPath = new StringBuilder(paths[0]);

		for (int i = 1; i < paths.length; i++) {
			String path = paths[i];
			if (path.startsWith(File.separator)) {
				concatPath.append(path);
			}
			else {
				concatPath.append(File.separator).append(path);
			}
		}
		return concatPath.toString();
	}

	/**
	 * change value of variable
	 *
	 * @param shellFilePath shell file path
	 * @param name shell variable name
	 * @param value shel variable value
	 */
	public static void setShellVariable(String shellFilePath, String name, String value) {
		String variableString = getShellVariableString(shellFilePath, name);

		if (StringUtil.isBlank(variableString)) {
			throw new LenaException("Fail to set variable '" + name + "' : '" + shellFilePath + "'");
		}
		else {
			replaceText(shellFilePath, variableString, name + "=" + value);
		}
	}

	/**
	 * change permissions of file or directory
	 * file : 600
	 * directory : 700
	 *
	 * @param file target file or directory
	 */
	public static void chmodF600OD700(File file) {
		if (!file.exists()) {
			return;
		}

		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				chmodF600OD700(child);
			}
		}

		// remove all permissions
		file.setReadable(false, false);
		file.setWritable(false, false);
		file.setExecutable(false, false);

		// Grant read & write to owner
		file.setReadable(true, true);
		file.setWritable(true, true);

		if(file.isDirectory()){
			file.setExecutable(true, true);
		}

	}

	/**
	 * delete file or directory
	 *
	 * @param path target file or directory path
	 */
	public static void delete(String path) {
		delete(new File(path));
	}

	/**
	 * delete file or directory
	 *
	 * @param file target File object
	 */
	public static void delete(File file) {
		try {
			if (file.isDirectory()) {
				CustomFileUtils.deleteDirectory(file);
			}
			else {
				CustomFileUtils.deleteQuietly(file);
			}
		}
		catch (IOException e) {
			throw new LenaException("Failed to delete file '" + file.getAbsolutePath() + "'", e);
		}
	}

	/**
	 * check the path exist or not
	 *
	 * @param path target path
	 * @return <code>true</code> if exist the file, otherwise <code>false</code>
	 */
	public static boolean exists(String path) {
		return new File(path).exists();
	}

	/**
	 * Find the variable name that matches the parameter and return the line
	 * exclude set (.bat) , export (.sh)
	 *
	 * @param shellFilePath shell file path
	 * @param name variable name
	 * @return variable name & value
	 */
	private static String getShellVariableString(String shellFilePath, String name) {
		BufferedReader br = null;
		String variableString = null;

		String declare = "set";
		if (shellFilePath.endsWith(".sh")) {
			declare = "export";
		}

		try {
			br = new BufferedReader(new FileReader(shellFilePath));

			do {
				String line = br.readLine();
				if (line == null) {
					break;
				}

				if (line.startsWith(declare)) {
					String nameAndValue = line.substring(declare.length(), line.length());
					nameAndValue = nameAndValue.trim();
					if (nameAndValue.startsWith(name)) {
						variableString = nameAndValue;
						break;
					}
				}
			} while (true);
		}
		catch (Throwable e) {
			throw new LenaException("Fail to read file : '" + shellFilePath + "'", e);
		}
		finally {
			close(br);
		}

		return variableString;
	}

	/**
	 * The String corresponding to the target from a single file or all files in a directory is converted as a replacement.
	 *
	 * @param srcPath File or directory path to replace
	 * @param target String before replace
	 * @param replacement String after replace
	 */
	public static void replaceText(String srcPath, CharSequence target, CharSequence replacement) {
		replaceText(new File(srcPath), target, replacement);
	}

	/**
	 * The String corresponding to the target from a single file or all files in a directory is converted as a replacement.
	 *
	 * @param src File or directory to replace
	 * @param target String before replace
	 * @param replacement String after replace
	 */
	public static void replaceText(File src, CharSequence target, CharSequence replacement) {
		if (src.isDirectory()) {
			for (File file : src.listFiles()) {
				replaceText(file, target, replacement);
			}
		}
		else {
			replaceTextInFile(src, target, replacement);
		}
	}

	/**
	 * The String corresponding to the target from a single file is converted as a replacement.
	 *
	 * @param file File to replace
	 * @param target String before replace
	 * @param replacement String after replace
	 */
	private static void replaceTextInFile(File file, CharSequence target, CharSequence replacement) {
		try {
			String frFileStr = readFileToString(file);
			writeStringToFile(file, frFileStr.replace(target, replacement));
		}
		catch (Throwable e) {
			throw new LenaException("Failed to replace text '" + file.getAbsolutePath() + "'", e);
		}
	}

	/**
	 * Close the InputStream.
	 *
	 * @param obj InputStream object to close
	 */
	public static void close(InputStream obj) {
		try {
			if (obj != null)
				obj.close();
		}
		catch (Throwable e) {
		}
	}

	/**
	 * Close OutputStream.
	 *
	 * @param obj OutputStream object to close
	 */
	public static void close(OutputStream obj) {
		try {
			if (obj != null)
				obj.close();
		}
		catch (Throwable e) {
		}
	}

	/**
	 * Close FileChannel.
	 *
	 * @param obj FileChannel object to close
	 */
	public static void close(FileChannel obj) {
		try {
			if (obj != null)
				obj.close();
		}
		catch (Throwable e) {
		}
	}

	/**
	 * Close the Reader.
	 *
	 * @param obj Reader object to close
	 */
	public static void close(Reader obj) {
		try {
			if (obj != null)
				obj.close();
		}
		catch (Throwable e) {
		}
	}

	/**
	 * return file contents
	 *
	 * @param path file path to read
	 * @return string
	 */
	public static String readFileToString(String path) {
		return readFileToString(path, "UTF-8");
	}

	/**
	 * return file contents
	 *
	 * @param path file path to read
	 * @param encoding the encoding
	 * @return string
	 */
	public static String readFileToString(String path, String encoding) {
		return readFileToString(new File(path), encoding);
	}

	/**
	 * return file contents
	 *
	 * @param file File object to read
	 * @return string
	 */
	public static String readFileToString(File file) {
		return readFileToString(file, "UTF-8");
	}

	/**
	 * return file contents
	 *
	 * @param file File object to read
	 * @param encoding encoding
	 * @return string
	 */
	public static String readFileToString(File file, String encoding) {
		try {
			String result = CustomFileUtils.readFileToString(file, encoding);
			if (result == null) {
				result = "";
			}
			return result;
		}
		catch (IOException e) {
			throw new LenaException("Failed to read file '" + file.getAbsolutePath() + "'", e);
		}
	}

	/**
	 * Writes the parameter String received to a file.
	 *
	 * @param file File object to write
	 * @param data String data to write
	 */
	public static void writeStringToFile(File file, String data) {
		writeStringToFile(file, data, "UTF-8");
	}

	/**
	 * Writes the parameter String received to a file.
	 *
	 * @param file File object to write
	 * @param data String data to write
	 * @param encoding the encoding
	 */
	public static void writeStringToFile(File file, String data, String encoding) {
		try {
			CustomFileUtils.write(file, data, encoding);
		}
		catch (IOException e) {
			throw new LenaException("Failed to write file '" + file.getAbsolutePath() + "'", e);
		}
	}

	/**
	 * Writes the parameter String received to a file.
	 *
	 * @param path File path object to write
	 * @param data String data to write
	 */
	public static void writeStringToFile(String path, String data) {
		writeStringToFile(path, data, "UTF-8");
	}

	/**
	 * Writes the parameter String received to a file.
	 *
	 * @param path File path object to write
	 * @param data String data to write
	 * @param encoding the encoding
	 */
	public static void writeStringToFile(String path, String data, String encoding) {
		writeStringToFile(new File(path), data, encoding);
	}

	/**
	 * Copy the files in the srcPath to destPath.
	 *
	 * @param srcPath source directory path
	 * @param destPath target directory path
	 */
	public static void copyDirectory(String srcPath, String destPath) {
		copyDirectory(srcPath, destPath, null);
	}

	/**
	 * Copy the files in the srcPath to destPath.
	 *
	 * @param srcPath source directory path
	 * @param destPath target directory path
	 * @param filter the filter
	 */
	public static void copyDirectory(String srcPath, String destPath, FileFilter filter) {
		try {
			if (filter == null) {
				CustomFileUtils.copyDirectory(new File(srcPath), new File(destPath), false);
			}
			else {
				CustomFileUtils.copyDirectory(new File(srcPath), new File(destPath), filter, false);
			}
			chmod755(destPath);
		}
		catch (IOException e) {
			throw new LenaException("Failed to copy directory '" + srcPath + "', '" + destPath + "'", e);
		}
	}

	/**
	 * Change the permissions of path and all files under path to 755.
	 *
	 * @param path target path
	 */
	public static void chmod755(String path) {
		chmod755(new File(path));
	}

	/**
	 * Change the permissions of path and all files under path to 755.
	 *
	 * @param file target File Object
	 */
	public static void chmod755(File file) {
		if (!file.exists()) {
			return;
		}

		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				chmod755(child);
			}
		}

		file.setReadable(true, false);
		file.setWritable(false, false);
		file.setWritable(true, true);
		file.setExecutable(true, false);
	}
}
