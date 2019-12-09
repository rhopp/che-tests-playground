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
package org.eclipse.che.selenium.pageobject.ocp;

import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.pageobject.ocp.OpenShiftLoginPage.Locators.LOGIN_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.ocp.OpenShiftLoginPage.Locators.PASSWORD_INPUT_NAME;
import static org.eclipse.che.selenium.pageobject.ocp.OpenShiftLoginPage.Locators.USERNAME_INPUT_NAME;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

@Singleton
public class OpenShiftLoginPage {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  protected interface Locators {
    String USERNAME_INPUT_NAME = "username";
    String PASSWORD_INPUT_NAME = "password";
    String LOGIN_BUTTON_XPATH = "//button[contains(text(),'Log In')]";
  }

  @FindBy(name = USERNAME_INPUT_NAME)
  private WebElement usernameInput;

  @FindBy(name = PASSWORD_INPUT_NAME)
  private WebElement passwordInput;

  @FindBy(xpath = LOGIN_BUTTON_XPATH)
  private WebElement loginButton;

  @Inject
  public OpenShiftLoginPage(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;

    PageFactory.initElements(seleniumWebDriver, this);
  }

  public void login(String username, String password) {
    waitOnOpen();

    seleniumWebDriverHelper.setValue(usernameInput, username);
    seleniumWebDriverHelper.setValue(passwordInput, password);
    seleniumWebDriverHelper.waitAndClick(loginButton);

    waitOnClose();
  }

  public void waitOnOpen() {
    seleniumWebDriverHelper.waitAllVisibility(asList(usernameInput, passwordInput, loginButton));
  }

  private void waitOnClose() {
    seleniumWebDriverHelper.waitAllInvisibility(asList(usernameInput, passwordInput, loginButton));
  }
}
