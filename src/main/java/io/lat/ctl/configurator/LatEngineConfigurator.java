package io.lat.ctl.configurator;

import io.lat.ctl.type.ConfiguratorCommandType;
import io.lat.ctl.type.InstallerServerType;
import io.lat.ctl.util.EngineUtil;

import java.util.Scanner;

public class LatEngineConfigurator extends LatConfigurator {
    private ConfiguratorCommandType configuratorCommandType;
    private static InstallerServerType installerServerType;



    public LatEngineConfigurator(ConfiguratorCommandType configuratorCommandType, InstallerServerType installerServerType) throws Exception {
        this.configuratorCommandType = configuratorCommandType;
        this.installerServerType = installerServerType;

        switch (configuratorCommandType){
            case LIST_ENGINES:
                EngineUtil.listEngines(getServerType());
                break;
            case DOWNLOAD_ENGINE:
                System.out.println("Available "+getServerType()+" engine versions:");
                EngineUtil.listEngines(getServerType());
                downloadEngine();
                break;
            case MODIFY_ENGINE:
                modifyEngine();
                break;
        }

    }
    protected static String getServerType() {
        return installerServerType.getServerType();
    }

   private void downloadEngine() throws Exception {


        System.out.println("=====================================================");
        System.out.println("Select ENGN_VERSION to download. Version with '*' is already downloaded.");
        System.out.println("ex : 9.0.00.A.RELEASE, 2.4.02.A.RELEASE");
        System.out.print(": ");


        Scanner scan = new Scanner(System.in);
        String version = scan.nextLine();

        EngineUtil.downloadEngine(version, getServerType());
   }

   private void modifyEngine() {

        //TODO : stopped 상태인지 검증 로직 추가
        //TODO : SERVER_ID 도 INSTANCE_ID로 바꿔야 하는 것 아닌지?
        //TODO : 설치된 instance list 보여주는 로직 추가
        Scanner scan = new Scanner(System.in);

        System.out.println("Enter SERVER_ID to modify engine");
        System.out.print(": ");
        String serverId = scan.nextLine();

        System.out.println("Enter ENGN_VERSION to run");
        System.out.println("ex : 9.0.00.A.RELEASE");
        System.out.print(": ");
        String version = scan.nextLine();

        EngineUtil.modifyEngine(serverId, version, getServerType());
   }

}
