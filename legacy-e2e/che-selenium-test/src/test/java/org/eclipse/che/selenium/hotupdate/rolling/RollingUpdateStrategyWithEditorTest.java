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
package org.eclipse.che.selenium.hotupdate.rolling;

import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Template.CONSOLE_JAVA_SIMPLE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import org.eclipse.che.api.system.shared.SystemStatus;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.CheTestSystemClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.executor.hotupdate.HotUpdateUtil;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Devfile;
import org.eclipse.che.selenium.pageobject.theia.TheiaIde;
import org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Ihor Okhrimenko */
public class RollingUpdateStrategyWithEditorTest {
  @Inject private CheTestSystemClient cheTestSystemClient;
  @Inject private TestWorkspaceServiceClient testWorkspaceServiceClient;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private HotUpdateUtil hotUpdateUtil;
  @Inject private Dashboard dashboard;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private TheiaIde theiaIde;
  @Inject private TheiaProjectTree theiaProjectTree;

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
  public void shouldUpdateMasterByRollingStrategyWithAccessibleEditorInProcess() throws Exception {
    theiaProjectTree.waitFilesTab();
    theiaProjectTree.waitProjectAreaOpened();
    theiaProjectTree.waitItem(CONSOLE_JAVA_SIMPLE);
    theiaIde.waitAllNotificationsClosed();

    // check that master is running
    assertEquals(cheTestSystemClient.getStatus(), SystemStatus.RUNNING);

    hotUpdateUtil.executeMasterPodUpdateCommand();

    checkIdeAvailability();

    // check that che is updated
    assertTrue(
        hotUpdateUtil.getRolloutStatus().contains("deployment \"che\" successfully rolled out"));
    WaitUtils.sleepQuietly(60);

    // check that workspace is successfully migrated to the new master
    assertTrue(testWorkspaceServiceClient.exists(workspaceName, defaultTestUser.getName()));

    checkIdeAvailability();
  }

  private void checkIdeAvailability() {
    hotUpdateUtil.checkMasterPodAvailabilityByPreferencesRequest();
    seleniumWebDriver.navigate().refresh();
    try {
      theiaIde.switchToIdeFrame();
      theiaIde.waitTheiaIde();
    } catch (TimeoutException ex) {
      seleniumWebDriver.navigate().refresh();
      theiaIde.switchToIdeFrame();
      theiaIde.waitTheiaIde();
    }
  }
}
