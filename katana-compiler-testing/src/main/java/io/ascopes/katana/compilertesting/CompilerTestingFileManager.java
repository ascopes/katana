/*
 * Copyright (C) 2021 Ashley Scopes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ascopes.katana.compilertesting;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;


/**
 * File manager for compilation testing.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class CompilerTestingFileManager implements JavaFileManager {

  private final FileObjectRepository<CompilerTestingFileObject> inputFiles;
  private final FileObjectRepository<InMemoryTestingFileObject> outputFiles;

  /**
   * Initialize this file manager.
   */
  public CompilerTestingFileManager(StandardJavaFileManager fileManager) {
    this.inputFiles = new FileObjectRepository<>();
    this.outputFiles = new FileObjectRepository<>();
  }

  /**
   * Add a given input file to the input file repository.
   *
   * @param fileObject the input file to add.
   */
  public void addInputFile(CompilerTestingFileObject fileObject) {
    this.inputFiles.put(fileObject);
  }

  /**
   * Add a given output file to the output file repository.
   *
   * @param fileObject the output file to add.
   */
  public void addOutputFile(InMemoryTestingFileObject fileObject) {
    this.outputFiles.put(fileObject);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CompilerTestingFileObject getFileForInput(
      Location location,
      String packageName,
      String relativeName
  ) {
    String path = packageAndFile(packageNameToPath(packageName), relativeName);
    if (location.isOutputLocation()) {
      return this.getOrPutForOutput(location, path);
    } else {
      return this.getOrPutForInput(location, path);
    }
  }

  @Override
  public ClassLoader getClassLoader(Location location) {
    return null;
  }

  @Override
  public Iterable<JavaFileObject> list(
      Location location,
      String packageName,
      Set<Kind> kinds,
      boolean recurse
  ) {
    String packagePath = packageNameToPath(packageName) + '/';

    FileObjectRepository<? extends CompilerTestingFileObject> repo = location.isOutputLocation()
        ? this.outputFiles
        : this.inputFiles;

    return repo
        .getAll(location)
        // If we recurse, we only care that the path matches the start. If we do not recurse,
        // we expect the last '/' to be in the package part of the name and not the file name,
        // since that would imply the directory was further nested if not.
        // This is shoddy, but we don't have a good way of working this out currently, as the
        // in memory files are not held in a hierarchy.
        .filter(file -> file.getName().startsWith(packageName)
            && (recurse || file.getName().lastIndexOf('/') < packagePath.length()))
        .filter(file -> kinds.contains(file.getKind()))
        .collect(Collectors.toList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String inferBinaryName(Location location, JavaFileObject file) {
    // TODO(ascopes): is this right?
    return file.getName();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSameFile(FileObject a, FileObject b) {
    return a.toUri().normalize().equals(b.toUri().normalize());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean handleOption(String current, Iterator<String> remaining) {
    // TODO(ascopes): is this right?
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasLocation(Location location) {
    // TODO(ascopes): is this right?
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CompilerTestingFileObject getJavaFileForInput(
      Location location,
      String className,
      Kind kind
  ) {
    String path = classNameToPath(className, kind);
    if (location.isOutputLocation()) {
      return this.getOrPutForOutput(location, path);
    } else {
      return this.getOrPutForInput(location, path);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InMemoryTestingFileObject getFileForOutput(
      Location location,
      String packageName,
      String relativeName,
      FileObject sibling
  ) {
    String path = packageAndFile(packageNameToPath(packageName), relativeName);
    return this.getOrPutForOutput(location, path);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JavaFileObject getJavaFileForOutput(
      Location location,
      String className,
      Kind kind,
      FileObject sibling
  ) {
    String path = classNameToPath(className, kind);
    return this.getOrPutForOutput(location, path);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void flush() {
    // Do nothing.
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() {
    // Do nothing.
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int isSupportedOption(String option) {
    // TODO(ascopes): is this right?
    return 0;
  }

  private CompilerTestingFileObject getOrPutForInput(Location location, String path) {
    return this.inputFiles
        .getOrPut(location, path, () -> InMemoryTestingFileObject.forPath(location, path));
  }

  private InMemoryTestingFileObject getOrPutForOutput(Location location, String path) {
    return this.outputFiles
        .getOrPut(location, path, () -> InMemoryTestingFileObject.forPath(location, path));
  }

  private static String packageNameToPath(String packageName) {
    return packageName.replace('.', '/');
  }

  private static String packageAndFile(String packagePath, String filePath) {
    return packagePath.isEmpty()
        ? filePath
        : packagePath + '/' + filePath;
  }

  private static String classNameToPath(String className, Kind kind) {
    return packageNameToPath(className) + kind.extension;
  }
}
