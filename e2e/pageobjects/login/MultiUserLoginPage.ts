/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import { ICheLoginPage } from './ICheLoginPage';
import { injectable, inject } from 'inversify';
import { DriverHelper } from '../../utils/DriverHelper';
import { CLASSES } from '../../inversify.types';
import { By } from 'selenium-webdriver';
import { TestConstants } from '../../TestConstants';
import { Logger } from '../../utils/Logger';

@injectable()
export class MultiUserLoginPage implements ICheLoginPage {

    constructor(
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    async login() {
        Logger.debug('MultiUserLoginPage.login');

        await this.waitEclipseCheLoginFormPage();
        await this.inputUserNameEclipseCheLoginPage(TestConstants.TS_SELENIUM_USERNAME);
        await this.inputPaswordEclipseCheLoginPage(TestConstants.TS_SELENIUM_PASSWORD);
        await this.clickEclipseCheLoginButton();
    }

    async waitEclipseCheLoginFormPage() {
        Logger.debug('MultiUserLoginPage.waitEclipseCheLoginFormPage');

        await this.driverHelper.waitVisibility(By.id('kc-form-login'));
    }

    async inputUserNameEclipseCheLoginPage(userName: string) {
        Logger.debug(`MultiUserLoginPage.inputUserNameEclipseCheLoginPage username: "${userName}"`);

        await this.driverHelper.enterValue(By.id('username'), userName);
    }

    async inputPaswordEclipseCheLoginPage(passw: string) {
        Logger.debug(`MultiUserLoginPage.inputPaswordEclipseCheLoginPage password: "${passw}"`);

        await this.driverHelper.enterValue(By.id('password'), passw);
    }

    async clickEclipseCheLoginButton() {
        Logger.debug('MultiUserLoginPage.clickEclipseCheLoginButton');

        await this.driverHelper.waitAndClick(By.id('kc-login'));
    }

}
