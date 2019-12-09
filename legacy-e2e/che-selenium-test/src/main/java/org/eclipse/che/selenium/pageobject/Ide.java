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
package org.eclipse.che.selenium.pageobject;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Profile.PROFILE_MENU;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.APPLICATION_START_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.PREPARING_WS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.net.URL;
import javax.annotation.PreDestroy;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.entrance.Entrance;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceUrlResolver;

/**
 * @author Vitaliy Gulyy
 * @author Dmytro Nochevnov
 */
@Singleton
public class Ide {
  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final TestWorkspaceUrlResolver testWorkspaceUrlResolver;
  private final Entrance entrance;
  private final ProjectExplorer projectExplorer;
  private final ToastLoader toastLoader;
  private final CheTerminal terminal;
  private final Menu menu;

  @Inject
  public Ide(
      SeleniumWebDriver seleniumWebDriver,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      TestWorkspaceUrlResolver testWorkspaceUrlResolver,
      Entrance entrance,
      ProjectExplorer projectExplorer,
      ToastLoader toastLoader,
      CheTerminal terminal,
      Menu menu) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.testWorkspaceUrlResolver = testWorkspaceUrlResolver;
    this.entrance = entrance;
    this.projectExplorer = projectExplorer;
    this.toastLoader = toastLoader;
    this.terminal = terminal;
    this.menu = menu;
  }

  public void open(TestWorkspace testWorkspace) throws Exception {
    URL workspaceUrl = testWorkspaceUrlResolver.resolve(testWorkspace);
    seleniumWebDriver.get(workspaceUrl.toString());
    entrance.login(testWorkspace.getOwner());
  }

  public void waitOpenedWorkspaceIsReadyToUse() {
    waitOpenedWorkspaceIsReadyToUse(PREPARING_WS_TIMEOUT_SEC);
  }

  public void waitOpenedWorkspaceIsReadyToUse(int timeout) {
    projectExplorer.waitProjectExplorer(timeout);
    terminal.waitFirstTerminalTab(timeout);
    menu.waitMenuItemIsEnabled(PROFILE_MENU, timeout);
  }

  public String switchToIdeAndWaitWorkspaceIsReadyToUse() {
    return switchToIdeAndWaitWorkspaceIsReadyToUse(APPLICATION_START_TIMEOUT_SEC);
  }

  public String switchToIdeAndWaitWorkspaceIsReadyToUse(int timeoutInSec) {
    String currentWindow = seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
    toastLoader.waitToastLoaderAndClickStartButton();
    waitOpenedWorkspaceIsReadyToUse(timeoutInSec);

    return currentWindow;
  }

  @PreDestroy
  public void close() {
    seleniumWebDriver.quit();
  }
}
