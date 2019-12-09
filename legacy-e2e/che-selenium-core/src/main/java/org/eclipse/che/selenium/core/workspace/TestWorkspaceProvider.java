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

import org.eclipse.che.selenium.core.user.TestUser;

/**
 * Workspace provider.
 *
 * @author Anatolii Bazko
 */
public interface TestWorkspaceProvider {

  /**
   * Creates a new workspace.
   *
   * @param owner the workspace owner
   * @param memoryGB the workspace memory size in GB
   * @param templateFileName the workspace template file name {@link WorkspaceTemplate}
   * @param startAfterCreation start workspace just after creation, if <bold>true</bold>
   */
  TestWorkspace createWorkspace(
      TestUser owner, int memoryGB, String templateFileName, boolean startAfterCreation)
      throws Exception;

  /**
   * Get existed workspace.
   *
   * @param workspaceName name of workspace
   * @param owner the workspace owner
   */
  TestWorkspace getWorkspace(String workspaceName, TestUser owner);

  /** Release all allocated resources. */
  void shutdown();
}
