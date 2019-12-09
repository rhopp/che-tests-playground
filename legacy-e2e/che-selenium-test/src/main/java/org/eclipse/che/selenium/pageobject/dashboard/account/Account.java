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
package org.eclipse.che.selenium.pageobject.dashboard.account;

import static java.lang.String.format;

import com.google.inject.Singleton;
import java.util.Objects;

/** @author Igor Ohrimenko */
@Singleton
public class Account {
  private String login;
  private String email;
  private String firstName;
  private String lastName;

  public Account withLogin(String login) {
    this.login = login;
    return this;
  }

  public Account withEmail(String email) {
    this.email = email;
    return this;
  }

  public Account withFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public Account withLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public String getLogin() {
    return this.login;
  }

  public String getEmail() {
    return this.email;
  }

  public String getFirstName() {
    return this.firstName;
  }

  public String getLastName() {
    return this.lastName;
  }

  @Override
  public boolean equals(Object accountToCompare) {
    return accountToCompare instanceof Account
        && Objects.equals(login, ((Account) accountToCompare).getLogin())
        && Objects.equals(email, ((Account) accountToCompare).getEmail())
        && Objects.equals(firstName, ((Account) accountToCompare).getFirstName())
        && Objects.equals(lastName, ((Account) accountToCompare).getLastName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(login, email, firstName, lastName);
  }

  @Override
  public String toString() {
    return format(
        "%s{login=%s, email=%s, firstName=%s, lastName=%s}",
        this.getClass().getSimpleName(),
        this.getLogin(),
        this.getEmail(),
        getFirstName(),
        getLastName());
  }
}
