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
package org.eclipse.che.selenium.workspaces;

import static org.eclipse.che.selenium.core.TestGroup.UNDER_REPAIR;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Template.CONSOLE_JAVA_SIMPLE;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Devfile;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces.Status;
import org.eclipse.che.selenium.pageobject.theia.TheiaEditor;
import org.eclipse.che.selenium.pageobject.theia.TheiaIde;
import org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev on 10.03.16 * */
@Test(groups = {UNDER_REPAIR})
public class ProjectStateAfterWorkspaceRestartTest {

  private static final String PATH_TO_POM_FILE = CONSOLE_JAVA_SIMPLE + "/" + "pom.xml";
  private static final String PATH_TO_README_FILE = CONSOLE_JAVA_SIMPLE + "/" + "README.md";

  @Inject private Dashboard dashboard;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private TheiaIde theiaIde;
  @Inject private TheiaProjectTree theiaProjectTree;
  @Inject private TheiaEditor theiaEditor;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private Workspaces workspaces;

  private String workspaceName;

  @BeforeClass
  public void setUp() throws Exception {
    dashboard.open();
    workspaceName = createWorkspaceHelper.createAndStartWorkspace(Devfile.JAVA_MAVEN);
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(workspaceName, defaultTestUser.getName());
  }

  @Test
  public void checkProjectAfterStopStartWs() {
    theiaProjectTree.waitFilesTab();
    theiaProjectTree.waitProjectAreaOpened();
    theiaProjectTree.waitItem(CONSOLE_JAVA_SIMPLE);
    theiaIde.waitAllNotificationsClosed();

    openFilesInEditor();
    checkFilesAreOpened();

    // stop and start workspace
    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    dashboard.waitToolbarTitleName("Workspaces");
    workspaces.clickOnWorkspaceStopStartButton(workspaceName);
    workspaces.waitWorkspaceStatus(workspaceName, Status.STOPPED);
    workspaces.clickOnWorkspaceStopStartButton(workspaceName);
    workspaces.waitWorkspaceStatus(workspaceName, Status.RUNNING);
    workspaces.selectWorkspaceItemName(workspaceName);
    workspaceDetails.waitToolbarTitleName(workspaceName);
    workspaceDetails.clickOpenInIdeWsBtn();

    theiaIde.waitOpenedWorkspaceIsReadyToUse();

    // check state of the project
    try {
      checkFilesAreOpened();
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known permanent failure https://github.com/eclipse/che/issues/14717");
    }
  }

  private void openFilesInEditor() {
    theiaProjectTree.expandItem(CONSOLE_JAVA_SIMPLE);
    theiaProjectTree.waitItem(PATH_TO_POM_FILE);
    theiaProjectTree.waitItem(PATH_TO_README_FILE);

    theiaProjectTree.openItem(PATH_TO_POM_FILE);
    theiaProjectTree.openItem(PATH_TO_README_FILE);
    theiaEditor.waitEditorTab("pom.xml");
    theiaEditor.waitEditorTab("README.md");
  }

  private void checkFilesAreOpened() {
    theiaEditor.waitEditorTab("pom.xml");
    theiaEditor.waitEditorTab("README.md");
  }
}
