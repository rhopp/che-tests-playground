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
package org.eclipse.che.selenium.dashboard;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.WorkspaceDetailsTab.OVERVIEW;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Devfile;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.ActionButton;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceOverview;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class RenameWorkspaceTest {

  private static final int MIN_WORKSPACE_NAME_SIZE = 3;
  private static final int MAX_WORKSPACE_NAME_SIZE = 100;
  private static final String MIN_WORKSPACE_NAME = generate("", MIN_WORKSPACE_NAME_SIZE);
  private static final String MAX_WORKSPACE_NAME = generate("", MAX_WORKSPACE_NAME_SIZE);
  private static final String WORKSPACE_NAME =
      generate(RenameWorkspaceTest.class.getSimpleName(), 5);
  private static final String WS_NAME_TOO_SHORT =
      ("The name has to be more than 3 characters long.");
  private static final String WS_NAME_TOO_LONG =
      ("The name has to be less than 101 characters long.");

  @Inject private Dashboard dashboard;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private DefaultTestUser user;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private Workspaces workspaces;
  @Inject private WorkspaceOverview workspaceOverview;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;

  @BeforeClass
  public void setUp() throws Exception {
    dashboard.open();
    createWorkspaceHelper.createAndEditWorkspaceFromStack(
        Devfile.JAVA_MAVEN, WORKSPACE_NAME, Collections.emptyList(), null);
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, user.getName());
    workspaceServiceClient.delete(MIN_WORKSPACE_NAME, user.getName());
    workspaceServiceClient.delete(MAX_WORKSPACE_NAME, user.getName());
  }

  @Test
  public void renameNameWorkspaceTest() throws IOException {
    dashboard.selectWorkspacesItemOnDashboard();
    dashboard.waitToolbarTitleName("Workspaces");
    workspaces.selectWorkspaceItemName(WORKSPACE_NAME);
    workspaceDetails.waitToolbarTitleName(WORKSPACE_NAME);
    workspaceDetails.selectTabInWorkspaceMenu(OVERVIEW);

    // type name with 1 characters and check error message that this name is too short
    workspaceOverview.enterNameWorkspace("w");
    workspaceDetails.waitAllDisabled(ActionButton.SAVE_BUTTON, ActionButton.APPLY_BUTTON);
    assertTrue(workspaceOverview.isWorkspaceNameErrorMessageEquals(WS_NAME_TOO_SHORT));
    workspaceDetails.clickOnCancelChangesBtn();
    workspaceOverview.checkNameWorkspace(WORKSPACE_NAME);

    // type name with 101 characters and check error message that this name is too long
    workspaceOverview.enterNameWorkspace(MAX_WORKSPACE_NAME + "a");
    workspaceDetails.waitAllDisabled(ActionButton.SAVE_BUTTON, ActionButton.APPLY_BUTTON);
    assertTrue(workspaceOverview.isWorkspaceNameErrorMessageEquals(WS_NAME_TOO_LONG));
    workspaceDetails.clickOnCancelChangesBtn();
    workspaceOverview.checkNameWorkspace(WORKSPACE_NAME);

    // type a name with min possible size and check that the workspace renamed
    renameWorkspace(MIN_WORKSPACE_NAME);

    // type a name with max possible size and check that the workspace renamed
    renameWorkspace(MAX_WORKSPACE_NAME);
  }

  private void renameWorkspace(String name) {
    workspaceOverview.enterNameWorkspace(name);
    workspaceOverview.checkOnWorkspaceNameErrorAbsence();
    assertFalse(workspaceOverview.isWorkspaceNameErrorMessageEquals(WS_NAME_TOO_SHORT));
    assertFalse(workspaceOverview.isWorkspaceNameErrorMessageEquals(WS_NAME_TOO_LONG));
    saveAndWaitWorkspaceRestarted();
    workspaceOverview.checkNameWorkspace(name);
  }

  private void saveAndWaitWorkspaceRestarted() {
    workspaceDetails.clickOnSaveChangesBtn();
    dashboard.waitNotificationMessage("Workspace updated");
    dashboard.waitNotificationIsClosed();
  }
}
