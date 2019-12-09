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
package org.eclipse.che.selenium.core.webdriver;

import static com.google.common.io.Files.createTempDir;
import static java.nio.file.Paths.get;
import static java.util.Arrays.stream;
import static org.eclipse.che.commons.lang.ZipUtils.unzip;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.DockerUtil;

/**
 * This is set of methods which operate with files which are downloaded by WebDriver which is
 * running inside the Docker container.
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class DownloadedIntoGridFileUtil implements DownloadedFileUtil {
  // default download directory in selenium chrome-node 3.10+ which doesn't depend from value of
  // "download.default_directory" preference
  private final String downloadDir = "/home/seluser/Downloads";

  private final DockerUtil dockerUtil;

  @Inject
  public DownloadedIntoGridFileUtil(DockerUtil dockerUtil) {
    this.dockerUtil = dockerUtil;
  }

  @Override
  public List<String> getPackageFileList(
      SeleniumWebDriver seleniumWebDriver, String downloadedPackageName) throws IOException {
    Path tempDirectory = Paths.get(createTempDir().toString());
    dockerUtil.copyFromContainer(
        seleniumWebDriver.getGridNodeContainerId(),
        get(downloadDir, downloadedPackageName),
        tempDirectory);

    File packageFile = Paths.get(tempDirectory.toString(), downloadedPackageName).toFile();
    unzip(packageFile, tempDirectory.toFile());
    FileUtils.deleteQuietly(packageFile);

    List<String> packageFileList =
        FileUtils.listFiles(tempDirectory.toFile(), null, true)
            .stream()
            .map(file -> file.toString().replace(tempDirectory.toString() + File.separatorChar, ""))
            .collect(Collectors.toList());
    Collections.sort(packageFileList);

    FileUtils.deleteQuietly(tempDirectory.toFile());

    return packageFileList;
  }

  @Override
  public String getDownloadedFileContent(
      SeleniumWebDriver seleniumWebDriver, String downloadedFileName) throws IOException {
    Path tempDirectory = Paths.get(createTempDir().toString());

    dockerUtil.copyFromContainer(
        seleniumWebDriver.getGridNodeContainerId(),
        get(downloadDir, downloadedFileName),
        tempDirectory);
    String content = FileUtils.readFileToString(tempDirectory.resolve(downloadedFileName).toFile());

    FileUtils.deleteQuietly(tempDirectory.toFile());

    return content;
  }

  @Override
  public void removeDownloadedFiles(SeleniumWebDriver seleniumWebDriver, String... filenames)
      throws IOException {
    String gridNodeContainerId = seleniumWebDriver.getGridNodeContainerId();
    stream(filenames)
        .forEach(
            filename -> {
              try {
                dockerUtil.delete(gridNodeContainerId, get(downloadDir, filename));
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
  }
}
