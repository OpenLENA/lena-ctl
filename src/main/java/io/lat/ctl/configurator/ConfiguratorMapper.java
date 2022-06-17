package io.lat.ctl.configurator;

import io.lat.ctl.type.ConfiguratorCommandType;
import io.lat.ctl.type.InstallerServerType;

import java.util.List;

public class ConfiguratorMapper {
    public static Configurator getConfigurator(List<String> commandList) throws Exception {
        String command = commandList.get(0);
        String serverType = commandList.get(1);

        InstallerServerType installerServerType=null;
        ConfiguratorCommandType configuratorCommandType=null;

        installerServerType = InstallerServerType.getInstallServerType(serverType);
        configuratorCommandType = ConfiguratorCommandType.valueOf(command.toUpperCase().replace('-','_'));

        switch (installerServerType){
            case APACHE:
            case TOMCAT:
                switch(configuratorCommandType){
                    case LIST_ENGINES:
                    case MODIFY_ENGINE:
                    case DOWNLOAD_ENGINE:
                        return new LatEngineConfigurator(configuratorCommandType, installerServerType);

                }
        }



        return null;
    }
}
