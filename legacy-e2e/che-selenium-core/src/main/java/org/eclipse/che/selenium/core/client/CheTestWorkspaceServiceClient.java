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
package org.eclipse.che.selenium.core.client;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.requestfactory.TestUserHttpJsonRequestFactoryCreator;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.MemoryMeasure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheTestWorkspaceServiceClient extends AbstractTestWorkspaceServiceClient {

  private static final Logger LOG = LoggerFactory.getLogger(CheTestWorkspaceServiceClient.class);

  @Inject
  public CheTestWorkspaceServiceClient(
      TestApiEndpointUrlProvider apiEndpointProvider, HttpJsonRequestFactory requestFactory) {
    super(apiEndpointProvider, requestFactory);
  }

  @AssistedInject
  public CheTestWorkspaceServiceClient(
      TestApiEndpointUrlProvider apiEndpointProvider,
      TestUserHttpJsonRequestFactoryCreator userHttpJsonRequestFactoryCreator,
      @Assisted TestUser testUser) {
    super(apiEndpointProvider, userHttpJsonRequestFactoryCreator, testUser);
  }

  @Override
  public Workspace createWorkspace(
      String workspaceName, int memory, MemoryMeasure memoryUnit, WorkspaceConfigDto workspace)
      throws Exception {
    workspace.setName(workspaceName);
    workspace.setDefaultEnv(workspaceName);
    WorkspaceDto workspaceDto =
        requestFactory
            .fromUrl(getBaseUrl())
            .usePostMethod()
            .setBody(workspace)
            .request()
            .asDto(WorkspaceDto.class);

    LOG.info("Workspace name='{}' and id='{}' created", workspaceName, workspaceDto.getId());

    return workspaceDto;
  }

  @Override
  public void start(String workspaceId, String workspaceName, TestUser workspaceOwner)
      throws Exception {
    sendStartRequest(workspaceId, workspaceName);
    waitWorkspaceStart(workspaceName, workspaceOwner.getName());
  }
}
