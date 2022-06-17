package io.lat.ctl.common;

import org.apache.commons.cli.Options;

public class ConfigureCommandCtl {
    final String LIST_ENGINES = "LIST-ENGINES";
    final String DOWNLOAD_ENGINE = "DOWNLOAD-ENGINE";
    final String MODIFY_ENGINE = "MODIFY-ENGINE";

    Options options = null;

    public ConfigureCommandCtl() {
        initCommandOptions();
    }
    public void initCommandOptions() {

    }

    public boolean containsCommand(String command){
        boolean result=false;
        if(LIST_ENGINES.toLowerCase().equals(command.toLowerCase())){
            result = true;
        }
        if(DOWNLOAD_ENGINE.toLowerCase().equals(command.toLowerCase())){
            result = true;
        }
        if(MODIFY_ENGINE.toLowerCase().equals(command.toLowerCase())){
            result = true;
        }
        return result;
    }
}
