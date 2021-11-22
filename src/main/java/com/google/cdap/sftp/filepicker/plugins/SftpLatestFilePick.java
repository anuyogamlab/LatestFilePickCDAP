/*
 * Copyright © 2017 Cask Data,nc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cdap.sftp.filepicker.plugins;

import com.google.common.base.Strings;
import com.jcraft.jsch.*;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.api.plugin.PluginConfig;
import io.cdap.cdap.etl.api.FailureCollector;
import io.cdap.cdap.etl.api.PipelineConfigurer;
import io.cdap.cdap.etl.api.action.Action;
import io.cdap.cdap.etl.api.action.ActionContext;
import io.cdap.cdap.etl.api.action.SettableArguments;
import com.google.cdap.sftp.filepicker.plugins.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static sun.management.jmxremote.ConnectorBootstrap.PropertyNames.HOST;

/**
 * SFTP Latest File Pick Action Plugin  - This provides a good starting point for building your own Transform Plugin
 * For full documentation, check out: https://docs.cask.co/cdap/current/en/developer-manual/pipelines/developing-plugins/index.html
 */
@Plugin(type = Action.PLUGIN_TYPE)
@Name("SftpLatestFilePick")
@Description("This picks up latest file in SFTP")
public class SftpLatestFilePick extends Action {
  // If you want to log things, you will need this line
  private static final Logger LOG = LoggerFactory.getLogger(SftpLatestFilePick.class);

  // Usually, you will need a private variable to store the config that was passed to your class
  private final SftpLatestDatePickConfig config;

  /**
   * Config properties for the plugin.
   */
  public static class SftpLatestDatePickConfig extends PluginConfig {
    @Name("hostName")
    @Macro
    //Macro enabled, so the values can be passed at the runtime
    @Nonnull
    private final String hostName;
    @Name("port")
    @Macro
    @Nonnull
    private final String port;
    @Name("userName")
    @Macro
    @Nonnull
    private final String userName;
    @Name("password")
    @Macro
    @Nonnull
    private final String password;
    @Name("directory")
    @Macro
    @Nonnull
    private final String directory;
    // set defaults for properties in a no-argument constructor.
    public SftpLatestDatePickConfig(String hostName, String port, String userName, String password, String directory) {
      this.hostName=hostName;
      this.port=port;
      this.userName=userName;
      this.password=password;
      this.directory=directory;
    }
  }
  public SftpLatestFilePick(SftpLatestDatePickConfig config) {
    this.config = config;
  }

  @Override
  public void configurePipeline(PipelineConfigurer pipelineConfigurer) {
  }


  @Override
  public void run(ActionContext context) throws Exception {

    String SftpWorkingDirectory = config.directory;
    SftpConnector sftpConnector = null;

    try {
      sftpConnector = new SftpConnector(config.hostName, config.port,
              config.userName, config.password);
      findLatestFiles(sftpConnector, SftpWorkingDirectory, context);
    } catch (Exception e) {
      throw new RuntimeException(String.format("Error occurred while accessing the folder: %s", e.getMessage()), e);
    } finally {
      if (sftpConnector != null) {
        sftpConnector.close();
      }
    }
  }

  private void findLatestFiles(SftpConnector connector,String SftpWorkingDirectory,
                             ActionContext context) throws SftpException, IOException {
    HashMap<String, Long> map = new HashMap<>();
    long maxCheck=0;
    ArrayList<String> parsedFolders = new ArrayList<>();
    long dateValue=0;
    String latestFolder = "";
    ChannelSftp channel = connector.getSftpChannel();
    channel.cd(SftpWorkingDirectory);
    Vector folderList = channel.ls(SftpWorkingDirectory);
    Pattern datePattern = Pattern.compile("(\\d\\d\\d\\d)(\\d\\d)(\\d\\d)");
    Matcher patternMatchCheck;
    for (int i = 0; i < folderList.size(); i++) {
      LOG.info(folderList.get(i).toString().split("\\s+")[8]);
      //Extract the folder name which has the date parameter embedded
      parsedFolders.add(folderList.get(i).toString().split("\\s+")[8]);
      patternMatchCheck = datePattern.matcher(parsedFolders.get(i));
      if (patternMatchCheck.matches()) {
        dateValue = Long.parseLong(patternMatchCheck.group(1) + patternMatchCheck.group(2) + patternMatchCheck.group(3));
        if(dateValue>maxCheck) {
          map.put(parsedFolders.get(i),dateValue);
          maxCheck=dateValue;
          latestFolder=parsedFolders.get(i);
        }
      }
    }
    SettableArguments datafusionSourcePluginOutput = context.getArguments();
    datafusionSourcePluginOutput.set("latest", latestFolder);
    LOG.info("Latest Folder/File is: "+latestFolder);
  }
}

