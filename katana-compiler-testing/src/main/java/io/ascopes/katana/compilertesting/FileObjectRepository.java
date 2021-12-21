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

import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.tools.JavaFileManager.Location;


/**
 * Repository to hold compilation file objects.
 *
 * @param <F> the file descriptor type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class FileObjectRepository<F extends CompilerTestingFileObject> {

  private final ReadWriteLock readWriteLock;
  private final Map<URI, F> urisToFiles;
  private final Map<Location, Map<String, F>> locationsToFiles;

  /**
   * Initialize this repository.
   */
  public FileObjectRepository() {
    this.readWriteLock = new ReentrantReadWriteLock();
    this.urisToFiles = new HashMap<>();
    this.locationsToFiles = new HashMap<>();
  }

  /**
   * Get a file by its URI, if it exists.
   *
   * @param uri the URI of the file to get.
   * @return the file object in an optional, or an empty optional if not found.
   */
  public Optional<F> get(URI uri) {
    Lock mutex = this.readWriteLock.readLock();
    mutex.lock();

    URI normalizedUri = this.normalizeUri(uri);

    try {
      return Optional.ofNullable(this.urisToFiles.get(normalizedUri));
    } finally {
      mutex.unlock();
    }
  }

  /**
   * Get a file by its location and file name, if it exists.
   *
   * @param location the location of the file.
   * @param name     the name of the file.
   * @return the file object in an optional, or an empty optional if not found.
   */
  public Optional<F> get(Location location, String name) {
    Lock mutex = this.readWriteLock.readLock();
    mutex.lock();

    String normalizedName = this.normalizeName(name);

    try {
      return Optional
          .ofNullable(this.locationsToFiles.get(location))
          .map(locationMap -> locationMap.get(normalizedName));
    } finally {
      mutex.unlock();
    }
  }

  /**
   * Get all files in this repository.
   *
   * @return a stream of the files in this repository. This is a snapshot.
   */
  public Stream<F> getAll() {
    Lock mutex = this.readWriteLock.readLock();
    mutex.lock();
    try {
      return new ArrayList<>(this.urisToFiles.values()).stream();
    } finally {
      mutex.unlock();
    }
  }

  /**
   * Get all files for a given location.
   *
   * @param location the location to get all files for.
   * @return a stream of each file found in the given location. This is a snapshot.
   */
  public Stream<F> getAll(Location location) {
    Lock mutex = this.readWriteLock.readLock();
    mutex.lock();
    try {
      Collection<F> values = Optional.ofNullable(this.locationsToFiles.get(location))
          .map(Map::values)
          .orElseGet(Collections::emptySet);

      return new ArrayList<>(values).stream();
    } finally {
      mutex.unlock();
    }
  }

  /**
   * Insert a new file object, replacing anything that was already registered under the same names.
   *
   * @param fileObject the file object to add.
   */
  public void put(F fileObject) {
    Lock mutex = this.readWriteLock.writeLock();
    mutex.lock();

    String normalizedName = this.normalizeName(fileObject.getName());
    URI normalizedUri = this.normalizeUri(fileObject.toUri());

    try {
      this.urisToFiles.put(normalizedUri, fileObject);
      this.locationsToFiles
          .computeIfAbsent(fileObject.getLocation(), unused -> new HashMap<>())
          .put(normalizedName, fileObject);
    } finally {
      mutex.unlock();
    }
  }

  /**
   * Get the file object at the given location with the given name.
   *
   * <p>If no file descriptor exists, initialize one using the given initializer.
   *
   * @param location the location of the file.
   * @param name the name of the file.
   * @param initializer the initializer to use if the file is not present in the mapping.
   * @return the final file at the location
   */
  public F getOrPut(Location location, String name, Supplier<F> initializer) {
    // We can downgrade a write-lock to a read-lock, but not upgrade a read-lock to a write-lock.
    Lock mutex = this.readWriteLock.writeLock();
    mutex.lock();

    try {
      return this.locationsToFiles
          .computeIfAbsent(location, unused -> new HashMap<>())
          .computeIfAbsent(this.normalizeName(name), unused -> {
            F newFileObject = initializer.get();
            this.urisToFiles.put(this.normalizeUri(newFileObject.toUri()), newFileObject);
            return newFileObject;
          });
    } finally {
      mutex.unlock();
    }
  }

  private String normalizeName(String name) {
    // Normalize the name.
    return Paths.get(name).normalize().toString();
  }

  private URI normalizeUri(URI uri) {
    return uri.normalize();
  }
}
