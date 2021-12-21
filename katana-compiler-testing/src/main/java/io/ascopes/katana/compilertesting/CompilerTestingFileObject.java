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

import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.tools.JavaFileManager.Location;
import javax.tools.SimpleJavaFileObject;


/**
 * Abstract base for a compiler-testing file object.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public abstract class CompilerTestingFileObject extends SimpleJavaFileObject {

  private final Location location;
  private final String name;
  private Charset charset;

  /**
   * Construct an AbstractFileObject of the given kind and with the given URI.
   *
   * @param location the location of the file object for the compiler.
   * @param name     the name of the file object.
   * @param uri      the URI for this file object.
   * @param kind     the kind of this file object.
   */
  protected CompilerTestingFileObject(Location location, String name, URI uri, Kind kind) {
    super(uri, kind);
    this.location = Objects.requireNonNull(location, "location was null");
    this.name = Objects.requireNonNull(name, "name was null");
    this.charset = StandardCharsets.UTF_8;
  }

  /**
   * Determine if the file exists or not.
   *
   * @return {@code true} if it exists, {@code false} if it does not.
   */
  public abstract boolean exists();

  /**
   * Get the location.
   *
   * @return the location.
   */
  public Location getLocation() {
    return this.location;
  }

  /**
   * Get the charset to use for reader/writer operations.
   *
   * @return the charset to use.
   */
  public Charset getCharset() {
    return this.charset;
  }

  /**
   * Set the charset to use for reader/writer operations.
   *
   * @param charset the charset to use.
   */
  public void setCharset(Charset charset) {
    this.charset = Objects.requireNonNull(charset);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return this.name;
  }

  /**
   * Throw an exception if this file object does not exist.
   *
   * @throws FileNotFoundException if the file does not exist.
   */
  protected final void requireFileExists() throws FileNotFoundException {
    if (!this.exists()) {
      throw new FileNotFoundException(this.uri.toString());
    }
  }

  /**
   * Determine the kind of file from the file name.
   *
   * @param fileName the file name.
   * @return the kind of file the file name represents.
   */
  protected static Kind determineKind(String fileName) {
    for (Kind potentialKind : Kind.values()) {
      if (fileName.endsWith(potentialKind.name())) {
        return potentialKind;
      }
    }
    return Kind.OTHER;
  }
}
