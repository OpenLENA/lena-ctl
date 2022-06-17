package io.lat.ctl.installer;

import java.util.HashMap;
import java.util.Scanner;

import io.lat.ctl.common.vo.Server;
import io.lat.ctl.exception.LatException;
import io.lat.ctl.type.InstallerCommandType;
import io.lat.ctl.type.InstallerServerType;
import io.lat.ctl.util.FileUtil;
import io.lat.ctl.util.InstallInfoUtil;
import io.lat.ctl.util.StringUtil;

/**
 * lat 인스턴스를 삭제하는 공통 Installer
 */
public class LatInstanceDeleteInstaller extends LatInstaller {

	public LatInstanceDeleteInstaller(InstallerCommandType installerCommandType,
			InstallerServerType installerServerType) {
		super(installerCommandType, installerServerType);
	}

	public void execute() {

		HashMap<String, String> commandMap = getServerInfoFromUser();

		String serverId = commandMap.get("SERVER_ID");
//		String logHomeDeleteFlag = getParameterValue(commandMap.get("LOG_HOME_DELETE_FLAG"), "D");

		String targetPath = InstallInfoUtil.getServerInstallPath(serverId);
		
		if (StringUtil.isBlank(targetPath)) {
			throw new LatException(serverId + " doesn't exist.");
		}

		// 서버가 기동중인 경우 삭제할 수 없음
		if (isRunning(targetPath, "ps")) {
			throw new LatException(serverId + " is running.");
		}

		Server srcServer = InstallInfoUtil.getServer(serverId);
		if (srcServer.getType().equals(getServerType())) {
			try {
				//String loghome = EnvUtil.getLogHome();
//				String loghome = FileUtil.getShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "LOG_HOME");
				
				// 'D' 일경우 기본 모드로 동작
				// log home 이 server home 내부에 위치한경우 삭제되며
				// log home 이 server home 외부에 위치한경우 삭제하지 않는다.
//				if (!("Y".equals(logHomeDeleteFlag)) && !("N".equals(logHomeDeleteFlag)) && !("D".equals(logHomeDeleteFlag))){
//					// TODO
//					throw new LatException("must select Y, N or D");
//				} else if ("D".equals(logHomeDeleteFlag)) {
//					if (loghome.contains(targetPath)) {
//						logHomeDeleteFlag = "Y";
//					} else {
//						logHomeDeleteFlag = "N";
//					}
//				} 

				// 로그홈 삭제 && 내부
				// 1. server home 삭제
				// 로그홈 유지 && 외부
				// 1. server home 삭제
				// 로그홈 삭제 && 외부
				// 1. server home 삭제
				// 2. log home 삭제
				// 로그홈 유지 && 내부
				// 3. server home 디렉토리중 , log 디렉토리를 제외한 디렉토리 삭제

//				if ("N".equalsIgnoreCase(logHomeDeleteFlag) && loghome.contains(targetPath)) {
//					// 3. server home 디렉토리중 , log 디렉토리를 제외한 디렉토리 삭제
//					FileUtil.deleteDirWithExceptDir(new File(targetPath), new File(loghome));
//					
//					System.out.println("Delete Target Home : " + targetPath);
//					System.out.println("Delete Log Home : " + loghome);
//					
//				} else {
					// 1. server home 삭제
					FileUtil.delete(targetPath);
					System.out.println("The instance is deleted : " + targetPath);
					
//					if ("Y".equalsIgnoreCase(logHomeDeleteFlag)) {
//						// 2. log home 삭제
//						FileUtil.delete(loghome);
//						System.out.println("Delete Log : " + loghome);
//					} else {
//						System.out.println("Don't delete Log Home : " + loghome);
//					}
//				}

				// update install-info.xml
				InstallInfoUtil.removeInstallInfo(serverId);
			} catch (Exception e) {
				throw new LatException(e);
			}

		} else {
			throw new LatException("Server Type matching error");
		}
	}

	/**
	 * @return Server information to be created
	 */
	public HashMap<String, String> getServerInfoFromUser() {
		HashMap<String, String> commandMap = new HashMap<String, String>();
		Scanner scan = new Scanner(System.in);

		System.out.println("+-------------------------------------------------------------------------------------");
		System.out.println("| 1. SERVER_ID : Server ID to delete                                                  ");
		System.out.print("|: ");
		commandMap.put("SERVER_ID", scan.nextLine());
//		System.out.println("| 2. LOG_HOME_DELETE_FLAG : whether to delete LOG Home ['Y','N','D'] ('D' is default) ");
//		System.out.print("|: ");
//		commandMap.put("LOG_HOME_DELETE_FLAG", scan.nextLine());
		System.out.println("+-------------------------------------------------------------------------------------");

		return commandMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see argo.install.installer.ArgoInstaller#getRequiredArgumentNames()
	 */
	protected String[] getRequiredArgumentNames() {
		return new String[] { "SERVER_ID" };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see argo.install.installer.ArgoInstaller#getOptionalArgumentNames()
	 */
	protected String[] getOptionalArgumentNames() {
		return new String[] { "LOG_HOME_DELETE_FLAG" };
	}
}
