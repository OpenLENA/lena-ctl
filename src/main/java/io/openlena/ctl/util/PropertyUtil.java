package io.openlena.ctl.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import io.openlena.ctl.exception.LenaException;

public class PropertyUtil {
	/**
	 * property파일에서 key에 해당하는 값을 조회한다.
	 * @param filePath property파일 경로
	 * @param key 조회할key
	 * @return
	 */
	public static String getProperty(String filePath, String key){
		Properties properties = new Properties();
		
        FileInputStream fis = null;
        try {
    		fis = new FileInputStream(filePath);
    		properties.load(fis);
        } catch (IOException e) {
        	throw new LenaException("Cannot load property file : '" + filePath + "'", e);
        } finally {
        	FileUtil.close(fis);
        }
        
        return properties.getProperty(key);
	}
	
	/**
	 * property를 변경한다.
	 * @param propertyFilePath
	 * @param name
	 * @param value
	 */
	public static void setProperty(String propertyFilePath, String name, String value){
		String variableString = getPropertyVariableString(propertyFilePath, name);
		
		if(StringUtil.isBlank(variableString)){
			throw new LenaException("Fail to set variable '" + name + "' : '"+ propertyFilePath +"'");
		}
		else{
			FileUtil.replaceText(propertyFilePath, variableString, name+"="+value);
		}
	}
	
	/**
	 * property를 추가한다.
	 * @param propertyFilePath
	 * @param name
	 * @param value
	 */
	public static void addProperty(String propertyFilePath, String name, String value){
		String appendStr = name+"="+value;
		
		FileUtil.appendStringToFile(propertyFilePath, appendStr);
	}
	
	/**
	 * property를 삭제한다.
	 * @param propertyFilePath
	 * @param name
	 */
	public static void removeProperty(String propertyFilePath, String name){
		String variableString = getPropertyVariableString(propertyFilePath, name);
		
		if(StringUtil.isBlank(variableString)){
			throw new LenaException("Fail to set variable '" + name + "' : '"+ propertyFilePath +"'");
		}
		else{
			FileUtil.replaceText(propertyFilePath, variableString, "");
		}
	}
	
	/**
	 * 프로퍼티 파일에서 변수가 있는 라인의 name=value의 스트링을 가져온다.
	 * @param propertyFilePath 프로퍼티파일 경로
	 * @param name 변수명
	 * @return line스트링
	 */
	private static String getPropertyVariableString(String propertyFilePath, String name){
		BufferedReader br = null;
		String variableString = null;
		
		try{
			br = new BufferedReader(new FileReader(propertyFilePath));
			
			do{
				String line = br.readLine();
				if(line == null){
					break;
				}
				
				String nameAndValue = line.trim();
				if(nameAndValue.startsWith(name)){
					variableString = nameAndValue;
					break;
				}
			}
			while(true);
		}
		catch(Throwable e){
			throw new LenaException("Fail to read file : '"+ propertyFilePath +"'", e);
		}
		finally{
			FileUtil.close(br);
		}
		
		return variableString;
	}
}
