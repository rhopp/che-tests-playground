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
package org.eclipse.che.selenium.core.utils;

import static java.lang.String.format;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.IOException;
import java.nio.file.Path;
import javax.inject.Singleton;
import org.eclipse.che.selenium.core.utils.process.ProcessAgent;
import org.eclipse.che.selenium.core.utils.process.ProcessAgentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is set of methods which operate with docker containers.
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class DockerUtil {
  private static final Logger LOG = LoggerFactory.getLogger(DockerUtil.class);

  @Inject
  @Named("che.host")
  private String cheHostParameter;

  @Inject private ProcessAgent processAgent;

  /**
   * Checks if IP address of host where program is running is the same as value of 'che.host'
   * parameter.
   *
   * @return {@code true} if only program is running on the host with the same IP address as Che
   */
  public boolean isCheRunLocally() {
    String command = "docker run --rm --net host eclipse/che-ip:nightly";

    try {
      String cheIpAddress = processAgent.process(command);

      if (cheIpAddress != null && cheHostParameter.contains(cheIpAddress)) {
        return true;
      }
    } catch (IOException e) {
      LOG.warn("Can't check if Eclipse Che run locally.", e);
    }

    return false;
  }

  /**
   * Copies files container to the host computer.
   *
   * @param containerId ID of container which we is being copped from
   * @param pathInsideContainer destination of files in the container
   * @param pathOnHost placement of files on the host computer
   * @throws IOException
   */
  public void copyFromContainer(String containerId, Path pathInsideContainer, Path pathOnHost)
      throws IOException {
    String copyCommand =
        format("docker cp %1$s:%2$s %3$s", containerId, pathInsideContainer, pathOnHost);

    processAgent.process(copyCommand);
  }

  /**
   * Copies files from the the host computer to container.
   *
   * @param containerId ID of container which we is being copped to
   * @param pathOnHost placement of files on the host computer
   * @param pathInsideContainer destination of files in the container
   * @throws IOException
   */
  public void copyIntoContainer(String containerId, Path pathOnHost, Path pathInsideContainer)
      throws IOException {
    String copyCommand =
        format("docker cp %1$s %2$s:%3$s", pathOnHost, containerId, pathInsideContainer);

    processAgent.process(copyCommand);
  }

  /**
   * Obtains ID of container kind of 'selenium_chromenode' with certain value of '{@code
   * .NetworkSettings.Networks.selenium_selenium_grid_internal.IPAddress}' runtime config parameter.
   *
   * @param IP internal IP address of grid node
   * @return ID of container
   * @throws ProcessAgentException
   */
  public String findGridNodeContainerByIp(String IP) throws ProcessAgentException {
    String getContainerIdCommand =
        format(
            "docker ps -q --filter='name=selenium_chromenode*' | xargs docker inspect --format '{{ .Id }} {{ .NetworkSettings.Networks.selenium_selenium_grid_internal.IPAddress }}' | grep %s | awk 'NR>0 {print $1;}'",
            IP);

    return processAgent.process(getContainerIdCommand);
  }

  /**
   * Delete files inside container .
   *
   * @param gridNodeContainerId ID of container which holds removing files
   * @param pathToDelete placement of files to remove inside container
   * @throws ProcessAgentException
   */
  public void delete(String gridNodeContainerId, Path pathToDelete) throws ProcessAgentException {
    String deleteInsideContainer =
        format("docker exec -i %s sh -c 'rm -fr %s'", gridNodeContainerId, pathToDelete);

    processAgent.process(deleteInsideContainer);
  }
}
