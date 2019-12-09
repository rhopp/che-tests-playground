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

import static java.util.Objects.requireNonNull;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import java.io.IOException;
import java.net.URL;
import javax.inject.Named;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.selenium.core.constant.Infrastructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates workspace config from JSON stored in resources. Takes into account current infrastructure
 * implementation set by property.
 *
 * @author Mihail Kuznyetsov
 * @author Alexander Garagatyi
 */
public class WorkspaceDtoDeserializer {

  private static final Logger LOG = LoggerFactory.getLogger(WorkspaceDtoDeserializer.class);

  @Inject
  @Named("che.infrastructure")
  private Infrastructure infrastructure;

  public WorkspaceConfigDto deserializeWorkspaceTemplate(String workspaceTemplateName) {
    requireNonNull(workspaceTemplateName);

    try {

      URL url =
          Resources.getResource(
              WorkspaceDtoDeserializer.class, getTemplateDirectory(workspaceTemplateName));
      return DtoFactory.getInstance()
          .createDtoFromJson(Resources.toString(url, Charsets.UTF_8), WorkspaceConfigDto.class);
    } catch (IOException | IllegalArgumentException | JsonSyntaxException e) {
      LOG.error(
          "Fail to read workspace template {} for infrastructure {} because {} ",
          workspaceTemplateName,
          getTemplateDirectory(workspaceTemplateName),
          e.getMessage());
      throw new RuntimeException(e.getLocalizedMessage(), e);
    }
  }

  private String getTemplateDirectory(String workspaceTemplateName) {
    String templateDirectoryName;
    switch (infrastructure) {
      case OSIO:
      case K8S:
        templateDirectoryName = Infrastructure.OPENSHIFT.toString().toLowerCase();
        break;

      default:
        templateDirectoryName = infrastructure.toString().toLowerCase();
    }

    return String.format(
        "/templates/workspace/%s/%s", templateDirectoryName, workspaceTemplateName);
  }
}
