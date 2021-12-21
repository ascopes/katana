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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CodingErrorAction;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.tools.JavaFileManager.Location;


/**
 * A file object that stores contents in-memory rather than on the file system.
 *
 * <p>This can consist of a Java source or a regular file, and supports both read and write
 * operations.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class InMemoryTestingFileObject extends CompilerTestingFileObject {

  private static final long NEVER = 0L;

  private byte[] content;
  private long lastModified;

  private InMemoryTestingFileObject(Location location, String name, URI uri, Kind kind) {
    super(location, name, uri, kind);
    this.content = null;
    this.lastModified = NEVER;
  }

  /**
   * Determine if the file exists.
   *
   * <p>An in-memory file is considered to exist if it has got a non-null content array.
   *
   * <p>A newly-initialized {@link InMemoryTestingFileObject} will not have any content, and will be
   * considered to not exist, even if you have a reference to it.
   *
   * @return true if the file exists.
   */
  @Override
  public boolean exists() {
    return this.content != null;
  }

  /**
   * Get a copy of the raw content, if the file exists.
   *
   * @return the raw content, if the file exists. An empty optional otherwise.
   */
  public Optional<byte[]> getContent() {
    return Optional.ofNullable(this.content);
  }

  /**
   * Set the content.
   *
   * <p>If the content parameter is null, then the {@link #getLastModified()} will be reset, and
   * the file will be considered deleted.
   *
   * @param content the content to set. May be null.
   */
  public void setContent(byte[] content) {
    this.lastModified = content == null
        ? NEVER
        : System.currentTimeMillis();
    this.content = content;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getLastModified() {
    return this.lastModified;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
    return this.decode(ignoreEncodingErrors);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InputStream openInputStream() throws FileNotFoundException {
    this.requireFileExists();
    return new ByteArrayInputStream(this.content);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OutputStream openOutputStream() {
    return new ByteArrayOutputStream() {
      @Override
      public void close() {
        InMemoryTestingFileObject.this.content = this.toByteArray();
        InMemoryTestingFileObject.this.lastModified = System.currentTimeMillis();
      }
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
    CharBuffer buffer = this.decode(ignoreEncodingErrors);
    char[] data = new char[buffer.remaining()];
    buffer.get(data);
    return new CharArrayReader(data);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Writer openWriter() {
    return new OutputStreamWriter(this.openOutputStream(), this.getCharset());
  }

  private CharBuffer decode(boolean ignoreEncodingErrors) throws IOException {
    this.requireFileExists();

    CodingErrorAction onError = ignoreEncodingErrors
        ? CodingErrorAction.IGNORE
        : CodingErrorAction.REPORT;

    return this
        .getCharset()
        .newDecoder()
        .onMalformedInput(onError)
        .onUnmappableCharacter(onError)
        .decode(ByteBuffer.wrap(this.content));
  }

  /**
   * Create an in-memory file object descriptor for the given path.
   *
   * @param location  the location to use as the root.
   * @param pathParts the parts of the path. Must be at least one element. Each will be separated by
   *                  a path delimiter when stored.
   * @return the in-memory file object that will not yet be marked as existing until you write to
   *     it.
   * @throws IllegalArgumentException if there are zero path parts provided along with the
   *                                  location.
   * @throws NullPointerException     if the location or any of the path parts are null.
   */
  public static InMemoryTestingFileObject forPath(Location location, String... pathParts)
      throws IllegalArgumentException, NullPointerException {

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
      uri = new URI("memory", null, rawUri, null);
    } catch (URISyntaxException ex) {
      throw new RuntimeException("Unexpected exception", ex);
    }

    return new InMemoryTestingFileObject(location, fileName, uri, kind);
  }
}
