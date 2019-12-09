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
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Template.CONSOLE_JAVA_SIMPLE;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.WorkspaceDetailsTab.PROJECTS;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Devfile;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.eclipse.che.selenium.pageobject.theia.TheiaIde;
import org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CreateAndDeleteProjectsTest {

  private static final String WORKSPACE = generate("workspace", 4);
  private static final String SECOND_CONSOLE_JAVA_SIMPLE_PROJECT_NAME = CONSOLE_JAVA_SIMPLE + "-1";

  private String dashboardWindow;

  @Inject private Dashboard dashboard;
  @Inject private WorkspaceProjects workspaceProjects;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private NewWorkspace newWorkspace;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private Workspaces workspaces;
  @Inject private TestWorkspaceProvider testWorkspaceProvider;
  @Inject private TheiaIde theiaIde;
  @Inject private TheiaProjectTree theiaProjectTree;
  @Inject private NavigationBar navigationBar;

  // it is used to read workspace logs on test failure
  private TestWorkspace testWorkspace;

  @BeforeClass
  public void setUp() {
    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE, defaultTestUser.getName());
  }

  @Test
  public void createProjectTest() {
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();
    dashboardWindow = seleniumWebDriver.getWindowHandle();

    // we are selecting 'Java' stack from the 'All Devfile' tab for compatibility with OSIO
    newWorkspace.typeWorkspaceName(WORKSPACE);
    newWorkspace.selectDevfile(Devfile.JAVA_MAVEN);
    newWorkspace.waitDevfileSelected(Devfile.JAVA_MAVEN);
    projectSourcePage.waitCreatedProjectButton(CONSOLE_JAVA_SIMPLE);

    // create 'console-java-simple-1' project
    projectSourcePage.clickOnAddOrImportProjectButton();
    projectSourcePage.selectSample(CONSOLE_JAVA_SIMPLE);
    projectSourcePage.clickOnAddProjectButton();
    projectSourcePage.waitCreatedProjectButton(SECOND_CONSOLE_JAVA_SIMPLE_PROJECT_NAME);

    newWorkspace.clickOnCreateButtonAndOpenInIDE();
    // store info about created workspace to make SeleniumTestHandler.captureTestWorkspaceLogs()
    // possible to read logs in case of test failure
    testWorkspace = testWorkspaceProvider.getWorkspace(WORKSPACE, defaultTestUser);

    // switch to the IDE and wait for workspace is ready to use
    theiaIde.switchToIdeFrame();
    theiaIde.waitTheiaIde();
    theiaIde.waitLoaderInvisibility();
    theiaIde.waitTheiaIdeTopPanel();
    theiaProjectTree.waitFilesTab();
    theiaProjectTree.clickOnFilesTab();

    // wait for projects in the tree
    theiaProjectTree.waitProjectAreaOpened();
    theiaProjectTree.waitItem(CONSOLE_JAVA_SIMPLE);
    theiaProjectTree.waitItem(SECOND_CONSOLE_JAVA_SIMPLE_PROJECT_NAME);
  }

  @Test(priority = 1)
  public void deleteProjectsFromDashboardTest() {
    theiaIde.openNavbarMenu();
    seleniumWebDriver.switchTo().window(dashboardWindow);
    navigationBar.waitNavigationBar();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitAddWorkspaceButton();
    workspaces.selectWorkspaceItemName(WORKSPACE);
    workspaceDetails.selectTabInWorkspaceMenu(PROJECTS);

    deleteProject(CONSOLE_JAVA_SIMPLE);

    workspaceProjects.waitProjectIsPresent(SECOND_CONSOLE_JAVA_SIMPLE_PROJECT_NAME);
  }

  private void deleteProject(String projectName) {
    workspaceProjects.waitProjectIsPresent(projectName);
    workspaceProjects.clickOnCheckbox(projectName);
    workspaceProjects.clickOnDeleteButton();
    workspaceDetails.clickOnDeleteButtonInDialogWindow();
    workspaceProjects.waitProjectIsNotPresent(projectName);
  }
}
