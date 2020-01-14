/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { injectable, inject } from 'inversify';
import { CLASSES } from '../../inversify.types';
import { DriverHelper } from '../../utils/DriverHelper';
import { By, error } from 'selenium-webdriver';
import { TestConstants } from '../../TestConstants';
import { Ide } from './Ide';
import { Logger } from '../../utils/Logger';

@injectable()
export class PreviewWidget {
    private static readonly WIDGET_TOOLBAR_XPATH: string = '//*[@id=\'theia-right-side-panel\']//div[@class=\'theia-mini-browser-toolbar\']';
    private static readonly WIDGET_URL_LOCATOR: By = By.xpath(`${PreviewWidget.WIDGET_TOOLBAR_XPATH}//input[@class='theia-input']`);

    constructor(@inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper,
        @inject(CLASSES.Ide) private readonly ide: Ide) { }

    async waitUrl(expectedUrl: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`PreviewWidget.waitUrl ${expectedUrl}`);

        await this.driverHelper.waitAttributeValue(PreviewWidget.WIDGET_URL_LOCATOR, 'value', expectedUrl, timeout);
    }

    async typeUrl(url: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`PreviewWidget.typeUrl ${url}`);

        await this.driverHelper.enterValue(PreviewWidget.WIDGET_URL_LOCATOR, url, timeout);
    }

    async typeAndApplyUrl(url: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`PreviewWidget.typeAndApplyUrl ${url}`);

        await this.typeUrl(url, timeout);
        await this.refreshPage();
    }

    async waitApplicationOpened(expectedUrl: string, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`PreviewWidget.waitApplicationOpened ${expectedUrl}`);

        await this.driverHelper.getDriver().wait(async () => {
            try {
                await this.waitUrl(expectedUrl, timeout / 5);
                return true;
            } catch (err) {
                if (!(err instanceof error.TimeoutError)) {
                    throw err;
                }

                await this.typeAndApplyUrl(expectedUrl, timeout);
            }
        }, timeout);
    }

    async waitAndSwitchToWidgetFrame() {
        Logger.debug('PreviewWidget.waitAndSwitchToWidgetFrame');

        const iframeLocator: By = By.css('div.theia-mini-browser iframe');
        await this.driverHelper.waitAndSwitchToFrame(iframeLocator);

    }

    async waitPreviewWidget(timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug('PreviewWidget.waitPreviewWidget');

        await this.driverHelper.waitVisibility(By.css('div.theia-mini-browser'), timeout);
    }

    async waitPreviewWidgetAbsence() {
        Logger.debug('PreviewWidget.waitPreviewWidgetAbsence');

        await this.driverHelper.waitDisappearance(By.css('div.theia-mini-browser'));
    }

    async waitContentAvailable(contentLocator: By,
        timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING * 5) {

        Logger.debug(`PreviewWidget.waitContentAvailable ${contentLocator}`);

        await this.waitAndSwitchToWidgetFrame();
        await this.driverHelper.getDriver().wait(async () => {
            const isApplicationTitleVisible: boolean = await this.driverHelper.isVisible(contentLocator);
            if (isApplicationTitleVisible) {
                await this.driverHelper.getDriver().switchTo().defaultContent();
                await this.ide.waitAndSwitchToIdeFrame();
                return true;
            }

            await this.switchBackToIdeFrame();
            await this.refreshPage();
            await this.waitAndSwitchToWidgetFrame();
            await this.driverHelper.wait(polling);
        }, timeout);
    }

    async waitContentAvailableInAssociatedWorkspace(contentLocator: By,
        timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT,
        polling: number = TestConstants.TS_SELENIUM_DEFAULT_POLLING * 5) {

        Logger.debug(`PreviewWidget.waitContentAvailableInAssociatedWorkspace ${contentLocator}`);

        await this.waitAndSwitchToWidgetFrame();
        await this.driverHelper.getDriver().wait(async () => {
            const isApplicationTitleVisible: boolean = await this.driverHelper.isVisible(contentLocator);
            if (isApplicationTitleVisible) {
                await this.driverHelper.getDriver().switchTo().defaultContent();
                return true;
            }

            await this.driverHelper.getDriver().switchTo().defaultContent();
            await this.refreshPage();
            await this.waitAndSwitchToWidgetFrame();
            await this.driverHelper.wait(polling);
        }, timeout);
    }

    async waitVisibility(element: By, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`PreviewWidget.waitVisibility ${element}`);

        await this.driverHelper.waitVisibility(element, timeout);
    }

    async waitAndClick(element: By, timeout: number = TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        Logger.debug(`PreviewWidget.waitAndClick ${element}`);

        await this.driverHelper.waitAndClick(element, timeout);
    }

    async refreshPage() {
        Logger.debug('PreviewWidget.refreshPage');

        const refreshButtonLocator: By = By.css('.theia-mini-browser .theia-mini-browser-refresh');
        await this.driverHelper.waitAndClick(refreshButtonLocator);
    }

    async switchBackToIdeFrame() {
        Logger.debug('PreviewWidget.switchBackToIdeFrame');

        await this.driverHelper.getDriver().switchTo().defaultContent();
        await this.ide.waitAndSwitchToIdeFrame();
    }

}
