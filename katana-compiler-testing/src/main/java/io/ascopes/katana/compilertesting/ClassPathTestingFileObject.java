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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.stream.Stream;
import javax.tools.JavaFileManager.Location;


/**
 * A class-path based file object that can be used to read sources from the classpath at runtime as
 * part of a test case.
 *
 * <p>This can consist of a Java source or a regular file. These file objects do not support write
 * operations. Attempting to perform a write-operation will result in an {@link IOException} or
 * {@link UncheckedIOException} being thrown.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class ClassPathTestingFileObject extends CompilerTestingFileObject {

  private final ClassLoader classLoader;

  private ClassPathTestingFileObject(
      ClassLoader classLoader,
      Location location,
      String name,
      URI uri,
      Kind kind) {
    super(location, name, uri, kind);
    this.classLoader = Objects.requireNonNull(classLoader, "classLoader was null");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getLastModified() {
    // Don't bother working this out.
    return 0L;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean exists() {
    return this.classLoader.getResource(this.getName()) != null;
  }

  @Override
  public InputStream openInputStream() throws IOException {
    this.requireFileExists();
    return this.classLoader.getResourceAsStream(this.getName());
  }

  @Override
  public OutputStream openOutputStream() throws IOException {
    throw this.readOnlyError();
  }

  @Override
  public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
    return new InputStreamReader(this.openInputStream(), this.getCharset());
  }

  @Override
  public Writer openWriter() throws IOException {
    throw this.readOnlyError();
  }

  @Override
  public boolean delete() {
    throw new UncheckedIOException(this.readOnlyError());
  }

  private IOException readOnlyError() {
    return new IOException("Cannot write to the classpath of the running JVM");
  }

  /**
   * Create a class path file object descriptor for the given path.
   *
   * @param classLoader the class loader to use to read the file.
   * @param location    the location to use as the root.
   * @param pathParts   the parts of the path. Must be at least one element. Each will be separated
   *                    by a path delimiter when stored.
   * @return the class path file descriptor.
   * @throws IllegalArgumentException if there are zero path parts provided along with the
   *                                  location.
   * @throws NullPointerException     if the location, classloader, or any of the path parts are
   *                                  null.
   */
  public static ClassPathTestingFileObject forPath(
      ClassLoader classLoader,
      Location location,
      String... pathParts
  ) throws IllegalArgumentException, NullPointerException {

    Objects.requireNonNull(classLoader, "classLoader was null");
    Objects.requireNonNull(location, "location was null");
    Objects.requireNonNull(pathParts, "pathParts varargs were null");

    if (pathParts.length == 0) {
      throw new IllegalArgumentException("Cannot initialize location with empty path");
    }

    if (Stream.of(pathParts).anyMatch(Objects::isNull)) {
      throw new NullPointerException("One or more parts of the path were null");
    }

    String fileName = String.join("/", pathParts);
    String rawUri = "/" + location.getName() + "/" + fileName;

    Kind kind = determineKind(fileName);

    URI uri;
    try {
      uri = new URI("classpath", null, rawUri, null);
    } catch (URISyntaxException ex) {
      throw new RuntimeException("Unexpected exception", ex);
    }

    return new ClassPathTestingFileObject(classLoader, location, fileName, uri, kind);
  }
}
