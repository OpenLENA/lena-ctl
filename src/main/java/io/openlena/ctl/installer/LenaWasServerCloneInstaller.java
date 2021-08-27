package io.openlena.ctl.installer;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;

import io.openlena.ctl.common.vo.Server;
import io.openlena.ctl.exception.LenaException;
import io.openlena.ctl.type.InstallerCommandType;
import io.openlena.ctl.type.InstallerServerType;
import io.openlena.ctl.util.CipherUtil;
import io.openlena.ctl.util.FileUtil;
import io.openlena.ctl.util.InstallConfigUtil;
import io.openlena.ctl.util.InstallInfoUtil;
import io.openlena.ctl.util.StringUtil;
import io.openlena.ctl.util.SystemUtil;

/**
 * lena-server(lena-se/lena-ee)를 복제하기 공통 로직을 담고 있는 Installer class
 */
public class LenaWasServerCloneInstaller extends LenaInstaller{
	/**
	 * 기본생성자
	 * @param installCommandType
	 * @param installServerType
	 */
	public LenaWasServerCloneInstaller(InstallerCommandType installCommandType, InstallerServerType installServerType) {
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
		
		String cloneInstallRootPath = InstallConfigUtil.getProperty(getServerType() + ".install-root-path.default", FileUtil.getParentPath(srcInstallPath));
		
		String targetPath = FileUtil.getConcatPath(cloneInstallRootPath, getTargetDirName(cloneServerId, cloneServicePort));
		
		// server exists check
		if(InstallInfoUtil.existsServer(cloneServerId)){
			throw new LenaException(cloneServerId + " already exists.");
		}

		// server exists check
		if(FileUtil.exists(targetPath)){
			throw new LenaException(targetPath + " already exists.");
		}
		
		// server type check
		if(!srcServer.getType().equals(getServerType())) {
			throw new LenaException("Server Type matching error");
		}

		try{
			// copy install files
			FileUtil.copyDirectory(srcInstallPath, targetPath);
			FileUtil.deleteFilesByWildcard(targetPath, "*.pid");

			String jvmRoute = SystemUtil.getDefaultJvmRoute(cloneServicePort);

			// serverId로부터 ajpSecret 생성
			String ajpSecret = CipherUtil.md5(cloneServerId);
			
			// replace text with user inputs.
			FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "SERVER_ID", cloneServerId);
			FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "SERVICE_PORT", cloneServicePort);
			FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "INSTALL_PATH", targetPath);
			FileUtil.setShellVariableWithoutException(FileUtil.getConcatPath(targetPath, "env.sh"), "JVM_ROUTE", jvmRoute);
			FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "AJP_SECRET", ajpSecret);
				
			// conf 디렉토리 하위 파일 권한을 600, 디렉토리 권한을 700으로 변경
			FileUtil.chmodF600OD700(new File(FileUtil.getConcatPath(targetPath, "conf")));
			
			// update install-info.xml
			addInstallInfo(cloneServerId, cloneServicePort, targetPath, srcServer.getVersion(), srcServer.getHotfix());

			// create temp directory
			FileUtil.mkdirs(FileUtil.getConcatPath(targetPath, "temp"));
		} catch(Throwable e){
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
		System.out.println("|    ex : lena_was-8080                                                               ");
		System.out.print("|: ");
		commandMap.put("SERVER_ID", scan.nextLine());
		System.out.println("| 2. CLONE_SERVER_ID is clone target server's id                                      ");
		System.out.println("|    ex : lena_was-8090                                                               ");
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
