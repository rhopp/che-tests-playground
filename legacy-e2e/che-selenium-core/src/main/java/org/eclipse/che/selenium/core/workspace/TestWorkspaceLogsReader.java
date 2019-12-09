/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.workspace;

import static org.eclipse.che.selenium.core.utils.FileUtil.removeDirectoryIfItIsEmpty;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.eclipse.che.api.core.util.AbstractLineConsumer;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.utils.process.ProcessAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads and stores the workspace logs by using command line operations. It ignores absent or empty
 * logs directory.
 *
 * @author Dmytro Nochevnov
 */
public abstract class TestWorkspaceLogsReader {

  private static final String READ_LOGS_ERROR_MESSAGE_TEMPLATE =
      "Can't obtain '{}' logs from workspace with id='{}' from directory '{}'.";

  @VisibleForTesting Logger log = LoggerFactory.getLogger(this.getClass());

  @Inject @VisibleForTesting TestWorkspaceServiceClient workspaceServiceClient;

  @Inject @VisibleForTesting ProcessAgent processAgent;

  /**
   * Read logs from workspace. It ignores absent or empty logs directory.
   *
   * @param workspace workspace which logs should be read.
   * @param pathToStore location of directory where logs should be stored.
   * @param suppressWarnings do not log warnings if there is a problem with getting workspace logs
   */
  public void store(TestWorkspace workspace, Path pathToStore, boolean suppressWarnings) {
    if (!canWorkspaceLogsBeRead()) {
      return;
    }

    final String workspaceId;
    try {
      workspaceId = workspace.getId();
    } catch (ExecutionException | InterruptedException e) {
      log.warn("It's impossible to get id of test workspace.", e);
      return;
    }

    store(workspaceId, pathToStore, suppressWarnings);
  }

  /**
   * Store logs from workspace. It ignores absent or empty logs directory.
   *
   * @param workspaceId id of workspace which logs should be read.
   * @param suppressWarnings do not log warnings if there is a problem with getting workspace logs
   */
  public void store(String workspaceId, Path pathToStore, boolean suppressWarnings) {
    // check if workspace exists
    if (workspaceId == null) {
      return;
    }

    if (!canWorkspaceLogsBeRead()) {
      return;
    }

    getLogInfos()
        .forEach(
            logInfo ->
                storeLog(logInfo, workspaceId, pathToStore.resolve(workspaceId), suppressWarnings));

    try {
      removeDirectoryIfItIsEmpty(pathToStore.resolve(workspaceId));
      removeDirectoryIfItIsEmpty(pathToStore);
    } catch (IOException e) {
      log.debug("Error of removal of empty log directory {}.", pathToStore.resolve(workspaceId), e);
    }
  }

  private void storeLog(
      LogInfo logInfo, String workspaceId, Path pathToStore, boolean suppressWarnings) {
    Path testLogsDirectory = pathToStore.resolve(logInfo.getName());

    try {
      Files.createDirectories(testLogsDirectory.getParent());

      // process command to copy logs from workspace container to the workspaceLogsDir
      processAgent.process(
          getReadLogsCommand(workspaceId, testLogsDirectory, logInfo.getLocationInsideWorkspace()));
    } catch (Exception e) {
      if (!suppressWarnings) {
        log.warn(
            READ_LOGS_ERROR_MESSAGE_TEMPLATE,
            logInfo.getName(),
            workspaceId,
            logInfo.getLocationInsideWorkspace(),
            e);
      }
    } finally {
      try {
        removeDirectoryIfItIsEmpty(testLogsDirectory);
      } catch (IOException e) {
        log.debug("Error of removal of empty log directory {}.", testLogsDirectory, e);
      }
    }
  }

  /**
   * Returns bash command to read logs from workspace by path to them inside workspace.
   *
   * @param workspaceId ID of workspace
   * @param testLogsDirectory location of directory to save the logs
   * @param logLocationInsideWorkspace location of logs inside workspace
   * @return command to read logs from workspace
   */
  abstract String getReadLogsCommand(
      String workspaceId, Path testLogsDirectory, Path logLocationInsideWorkspace);

  /**
   * Gets list of available workspace log providers which are dedicated to read certain logs.
   *
   * @return list of log providers
   */
  abstract List<LogInfo> getLogInfos();

  /**
   * Checks if it is possible to read logs from workspace.
   *
   * @return <b>true</b> if it is possible to read logs from workspace, or <b>false</b> otherwise.
   */
  abstract boolean canWorkspaceLogsBeRead();

  @VisibleForTesting
  LineConsumer getStdoutConsumer() {
    return new AbstractLineConsumer() {};
  }

  @VisibleForTesting
  ListLineConsumer getStderrConsumer() {
    return new ListLineConsumer();
  }

  /** Holds information about log to read. */
  static class LogInfo {
    private final String name;
    private final Path locationInsideWorkspace;

    private LogInfo(String name, Path locationInsideWorkspace) {
      this.name = name;
      this.locationInsideWorkspace = locationInsideWorkspace;
    }

    String getName() {
      return name;
    }

    Path getLocationInsideWorkspace() {
      return locationInsideWorkspace;
    }

    static LogInfo create(String name, Path locationInsideWorkspace) {
      return new LogInfo(name, locationInsideWorkspace);
    }
  }
}
