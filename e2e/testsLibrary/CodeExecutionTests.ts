/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { CLASSES, Terminal, TopMenu, Ide, DialogWindow, DriverHelper } from '..';
import { e2eContainer } from '../inversify.config';
import Axios from 'axios';
import { Key } from 'selenium-webdriver';

const terminal: Terminal = e2eContainer.get(CLASSES.Terminal);
const topMenu: TopMenu = e2eContainer.get(CLASSES.TopMenu);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const dialogWindow: DialogWindow = e2eContainer.get(CLASSES.DialogWindow);
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);

export function runTask(taskName: string, timeout: number) {
    test(`Run command '${taskName}'`, async () => {
        await topMenu.runTask(taskName);
        await terminal.waitIconSuccess(taskName, timeout);
    });
}

export function runTaskInputText(taskName: string, waitedText: string, inputText: string, timeout: number) {
    test(`Run command '${taskName}' expecting dialog shell`, async () => {
        await topMenu.runTask(taskName);
        await terminal.waitText(taskName, waitedText, timeout);
        await terminal.clickOnTab(taskName);
        await terminal.type(taskName, inputText);
        await terminal.type(taskName, Key.ENTER);
        await terminal.waitIconSuccess(taskName, timeout);
    });
}

export function runTaskWithDialogShellAndOpenLink(taskName: string, expectedDialogText: string, timeout: number) {
    test(`Run command '${taskName}' expecting dialog shell`, async () => {
        await topMenu.runTask(taskName);
        await dialogWindow.waitDialogAndOpenLink(expectedDialogText, timeout);
    });
}

export function runTaskWithDialogShellDjangoWorkaround(taskName: string, expectedDialogText: string, urlSubPath: string, timeout: number) {
    test(`Run command '${taskName}' expecting dialog shell`, async () => {
        await topMenu.runTask(taskName);
        await dialogWindow.waitDialog(expectedDialogText, timeout);
        const dialogRedirectUrl: string = await dialogWindow.getApplicationUrlFromDialog(expectedDialogText);
        const augmentedPreviewUrl: string = dialogRedirectUrl + urlSubPath;
        await dialogWindow.closeDialog();
        await dialogWindow.waitDialogDissappearance();
        await driverHelper.getDriver().wait(async () => {
            try {
                const res = await Axios.get(augmentedPreviewUrl);
                if (res.status === 200) { return true; }
            } catch (error) { await driverHelper.wait(1_000); }
        }, timeout);
    });
}

export function runTaskWithDialogShellAndClose(taskName: string, expectedDialogText: string, timeout: number) {
    test(`Run command '${taskName}' expecting dialog shell`, async () => {
        await topMenu.runTask(taskName);
        await dialogWindow.waitDialog(expectedDialogText, timeout);
        await dialogWindow.closeDialog();
        await dialogWindow.waitDialogDissappearance();
    });
}

export function runTaskWithNotification(taskName: string, notificationText: string, timeout: number) {
    test(`Run command '${taskName}' expecting notification pops up`, async () => {
        await topMenu.runTask(taskName);
        await ide.waitNotification(notificationText, timeout);
    });
}

export function closeTerminal(taskName: string) {
    test('Close the terminal tasks', async () => {
        await ide.closeAllNotifications();
        await terminal.closeTerminalTab(taskName);
    });
}
