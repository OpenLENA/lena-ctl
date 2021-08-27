package io.openlena.ctl.installer;

import java.util.HashMap;
import java.util.Scanner;

import io.openlena.ctl.common.vo.Server;
import io.openlena.ctl.exception.LenaException;
import io.openlena.ctl.type.InstallerCommandType;
import io.openlena.ctl.type.InstallerServerType;
import io.openlena.ctl.util.FileUtil;
import io.openlena.ctl.util.InstallConfigUtil;
import io.openlena.ctl.util.InstallInfoUtil;
import io.openlena.ctl.util.StringUtil;

/**
 * Apache서버를 복제하기 위한 Installer class
 */
public class LenaWebServerCloneInstaller extends LenaInstaller{
	/**
	 * 기본생성자
	 * @param installCommandType
	 * @param installServerType
	 */
	public LenaWebServerCloneInstaller(InstallerCommandType installCommandType, InstallerServerType installServerType) {
		super(installCommandType, installServerType);
	}
	
	/*
	 * (non-Javadoc)
	 * @see argo.install.installer.ArgoInstaller#execute()
	 */
	public void execute() {
		// get options from user
		HashMap<String, String> commandMap = getServerInfoFromUser();
		
		String srcServerId = commandMap.get("SERVER_ID");
		String srcServicePort = InstallInfoUtil.getServicePort(srcServerId);
		String cloneServerId = commandMap.get("CLONE_SERVER_ID");
		String cloneServicePort = commandMap.get("CLONE_SERVICE_PORT");
		
		Server srcServer = InstallInfoUtil.getServer(srcServerId);
		
		String srcInstallPath = srcServer.getPath();
		
		// server exists check
		if(!InstallInfoUtil.existsServer(srcServerId)){
			throw new LenaException(srcServerId + " doesn't exist.");
		}

		// validate options
		if(!StringUtil.isNumeric(srcServicePort, cloneServicePort)){
			throw new LenaException("Service Port should be numeric");
		}

		// server exists check
		if(!FileUtil.exists(srcInstallPath)){
			throw new LenaException(srcInstallPath + " doesn't exist.");
		}
		
		// server exists check
		if(InstallInfoUtil.existsServer(cloneServerId)){
			throw new LenaException(cloneServerId + " already exists.");
		}

		String cloneInstallRootPath = InstallConfigUtil.getProperty(getServerType() + ".install-root-path.default", FileUtil.getParentPath(srcInstallPath));
		
		String targetPath = FileUtil.getConcatPath(cloneInstallRootPath, getTargetDirName(cloneServerId, cloneServicePort));
		String cloneDocumentRootPath = FileUtil.getConcatPath(targetPath, "htdocs");
		//String cloneRootHomePath = FileUtil.getConcatPath(targetPath, "logs");
		
		// server exists check
		if(FileUtil.exists(targetPath)){
			throw new LenaException(targetPath + " already exists.");
		}
		
		try{
			// copy install files
			FileUtil.copyDirectory(srcInstallPath, targetPath);
			FileUtil.deleteFilesByWildcard(targetPath, "*.pid");
			
			FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "SERVER_ID", cloneServerId);
			FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "SERVICE_PORT", cloneServicePort);
			
			// document경로가 서버 하위에 있는 경우는 default doc_root로 설정하고, 서버 외부에 존재하는 경우에는 기존 경로를 그대로 유지한다.
			String orgInstallPath = FileUtil.getShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "INSTALL_PATH");
			String orgDocumentRootPath = FileUtil.getShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "DOC_ROOT");
			
			FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "INSTALL_PATH", targetPath);
			if(FileUtil.isSubDirectory(orgInstallPath, orgDocumentRootPath)){
				FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "DOC_ROOT", cloneDocumentRootPath);
			}
			// update install-info.xml
			addInstallInfo(cloneServerId, cloneServicePort, targetPath, srcServer.getVersion(), srcServer.getHotfix());
		}
		catch(Throwable e){
			throw new LenaException(e);
		}
	}
	
	/**
	 * @return Server information to be created
	 */
	protected HashMap<String, String> getServerInfoFromUser() {
		HashMap<String, String> commandMap = new HashMap<String, String>();
		Scanner scan = new Scanner(System.in);

		System.out.println("+-------------------------------------------------------------------------------------");
		System.out.println("| 1. SERVER_ID means business code of system and its number of letter is from 3 to 5. ");
		System.out.println("|    ex : web-80                                                                      ");
		System.out.print("|: ");
		commandMap.put("SERVER_ID", scan.nextLine());
		System.out.println("| 2. CLONE_SERVER_ID is clone target server's id                                      ");
		System.out.println("|    ex : web-8090                                                                    ");
		System.out.print("|: ");
		commandMap.put("CLONE_SERVER_ID", scan.nextLine());
		System.out.println("| 3. CLONE_SERVICE_PORT is the target server's port number used by HTTP Connector.    ");
		System.out.println("|    ex : 8090                                                                  ");
		System.out.print("|: ");
		commandMap.put("CLONE_SERVICE_PORT", scan.nextLine());
		System.out.println("+-------------------------------------------------------------------------------------");

		return commandMap;
	}
	
}
