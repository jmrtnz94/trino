/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.plugin.iceberg.catalog.rest;

import io.airlift.log.Logger;
import io.trino.filesystem.FileIterator;
import io.trino.filesystem.Location;
import io.trino.filesystem.TrinoFileSystem;
import io.trino.filesystem.TrinoInputFile;
import io.trino.filesystem.TrinoOutputFile;
import io.trino.filesystem.encryption.EncryptionKey;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * A wrapper around TrinoFileSystem that handles AWS S3 Tables REST limitations.
 * S3 Tables REST API does not support direct file-level operations like listing
 * and deleting individual objects. This wrapper provides graceful handling
 * by logging warnings and skipping unsupported operations.
 */
public class S3TablesRestAwareFileSystem
        implements TrinoFileSystem
{
    private static final Logger LOG = Logger.get(S3TablesRestAwareFileSystem.class);

    private final TrinoFileSystem delegate;

    public S3TablesRestAwareFileSystem(TrinoFileSystem delegate)
    {
        this.delegate = requireNonNull(delegate, "delegate is null");
    }

    @Override
    public TrinoInputFile newInputFile(Location location)
    {
        return delegate.newInputFile(location);
    }

    @Override
    public TrinoInputFile newInputFile(Location location, long length)
    {
        return delegate.newInputFile(location, length);
    }

    @Override
    public TrinoInputFile newInputFile(Location location, long length, Instant lastModified)
    {
        return delegate.newInputFile(location, length, lastModified);
    }

    @Override
    public TrinoInputFile newEncryptedInputFile(Location location, EncryptionKey key)
    {
        return delegate.newEncryptedInputFile(location, key);
    }

    @Override
    public TrinoInputFile newEncryptedInputFile(Location location, long length, EncryptionKey key)
    {
        return delegate.newEncryptedInputFile(location, length, key);
    }

    @Override
    public TrinoInputFile newEncryptedInputFile(Location location, long length, Instant lastModified, EncryptionKey key)
    {
        return delegate.newEncryptedInputFile(location, length, lastModified, key);
    }

    @Override
    public TrinoOutputFile newOutputFile(Location location)
    {
        return delegate.newOutputFile(location);
    }

    @Override
    public TrinoOutputFile newEncryptedOutputFile(Location location, EncryptionKey key)
    {
        return delegate.newEncryptedOutputFile(location, key);
    }

    @Override
    public void deleteFile(Location location)
            throws IOException
    {
        LOG.warn("File-level deletion is not supported with AWS S3 Tables REST Catalog. " +
                "Skipping deletion of file: %s. " +
                "This may result in orphaned files. See documentation for S3 Tables limitations.", location);
        // Skip the deletion operation - S3 Tables REST doesn't support direct file deletion
    }

    @Override
    public void deleteDirectory(Location location)
            throws IOException
    {
        LOG.warn("Directory-level deletion requires file listing which is not supported with AWS S3 Tables REST Catalog. " +
                "Skipping deletion of directory: %s. " +
                "Use table-level operations to manage data. See documentation for S3 Tables limitations.", location);
        // Skip the deletion operation - S3 Tables REST doesn't support listing/deleting files
    }

    @Override
    public void deleteFiles(Collection<Location> locations)
            throws IOException
    {
        LOG.warn("Bulk file deletion is not supported with AWS S3 Tables REST Catalog. " +
                "Skipping deletion of %d files. " +
                "This may result in orphaned files. See documentation for S3 Tables limitations.", locations.size());
        // Skip the deletion operation - S3 Tables REST doesn't support direct file deletion
    }

    @Override
    public void renameFile(Location source, Location target)
            throws IOException
    {
        // File rename might be supported, delegate to underlying implementation
        // If it fails, the error will be propagated to the user
        delegate.renameFile(source, target);
    }

    @Override
    public FileIterator listFiles(Location location)
            throws IOException
    {
        LOG.warn("File listing is not supported with AWS S3 Tables REST Catalog. " +
                "Returning empty iterator for location: %s. " +
                "Use table-level operations to access data. See documentation for S3 Tables limitations.", location);
        // Return empty iterator - S3 Tables REST doesn't support file listing
        return FileIterator.empty();
    }

    @Override
    public boolean directoryExists(Location location)
            throws IOException
    {
        LOG.warn("Directory existence checking requires file listing which is not supported with AWS S3 Tables REST Catalog. " +
                "Returning false for location: %s. " +
                "Use table-level operations to check data existence. See documentation for S3 Tables limitations.", location);
        // Return false - S3 Tables REST doesn't support directory checking
        return false;
    }

    @Override
    public Set<Location> listDirectories(Location location)
            throws IOException
    {
        LOG.warn("Directory listing is not supported with AWS S3 Tables REST Catalog. " +
                "Returning empty set for location: %s. " +
                "Use table-level operations to access data. See documentation for S3 Tables limitations.", location);
        // Return empty set - S3 Tables REST doesn't support directory listing
        return Set.of();
    }
}