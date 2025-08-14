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

import io.trino.filesystem.FileIterator;
import io.trino.filesystem.Location;
import io.trino.filesystem.TrinoFileSystem;
import io.trino.testing.AbstractTestQueryFramework;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class TestS3TablesRestAwareFileSystem
{
    @Mock
    private TrinoFileSystem mockDelegate;

    @Test
    public void testDeleteFileSkipsOperation()
            throws IOException
    {
        MockitoAnnotations.openMocks(this);
        S3TablesRestAwareFileSystem fileSystem = new S3TablesRestAwareFileSystem(mockDelegate);
        Location location = Location.of("s3://bucket/path/file.txt");

        // Should not throw exception and should not call delegate
        fileSystem.deleteFile(location);

        verifyNoInteractions(mockDelegate);
    }

    @Test
    public void testDeleteDirectorySkipsOperation()
            throws IOException
    {
        MockitoAnnotations.openMocks(this);
        S3TablesRestAwareFileSystem fileSystem = new S3TablesRestAwareFileSystem(mockDelegate);
        Location location = Location.of("s3://bucket/path/");

        // Should not throw exception and should not call delegate
        fileSystem.deleteDirectory(location);

        verifyNoInteractions(mockDelegate);
    }

    @Test
    public void testDeleteFilesSkipsOperation()
            throws IOException
    {
        MockitoAnnotations.openMocks(this);
        S3TablesRestAwareFileSystem fileSystem = new S3TablesRestAwareFileSystem(mockDelegate);
        List<Location> locations = List.of(
                Location.of("s3://bucket/path/file1.txt"),
                Location.of("s3://bucket/path/file2.txt"));

        // Should not throw exception and should not call delegate
        fileSystem.deleteFiles(locations);

        verifyNoInteractions(mockDelegate);
    }

    @Test
    public void testListFilesReturnsEmpty()
            throws IOException
    {
        MockitoAnnotations.openMocks(this);
        S3TablesRestAwareFileSystem fileSystem = new S3TablesRestAwareFileSystem(mockDelegate);
        Location location = Location.of("s3://bucket/path/");

        FileIterator result = fileSystem.listFiles(location);

        assertThat(result.hasNext()).isFalse();
        verifyNoInteractions(mockDelegate);
    }

    @Test
    public void testDirectoryExistsReturnsFalse()
            throws IOException
    {
        MockitoAnnotations.openMocks(this);
        S3TablesRestAwareFileSystem fileSystem = new S3TablesRestAwareFileSystem(mockDelegate);
        Location location = Location.of("s3://bucket/path/");

        boolean result = fileSystem.directoryExists(location);

        assertThat(result).isFalse();
        verifyNoInteractions(mockDelegate);
    }

    @Test
    public void testListDirectoriesReturnsEmpty()
            throws IOException
    {
        MockitoAnnotations.openMocks(this);
        S3TablesRestAwareFileSystem fileSystem = new S3TablesRestAwareFileSystem(mockDelegate);
        Location location = Location.of("s3://bucket/path/");

        Set<Location> result = fileSystem.listDirectories(location);

        assertThat(result).isEmpty();
        verifyNoInteractions(mockDelegate);
    }

    @Test
    public void testRenameFileDelegatesToUnderlying()
            throws IOException
    {
        MockitoAnnotations.openMocks(this);
        S3TablesRestAwareFileSystem fileSystem = new S3TablesRestAwareFileSystem(mockDelegate);
        Location source = Location.of("s3://bucket/path/source.txt");
        Location target = Location.of("s3://bucket/path/target.txt");

        fileSystem.renameFile(source, target);

        verify(mockDelegate).renameFile(source, target);
    }

    @Test
    public void testNewInputFileDelegatesToUnderlying()
    {
        MockitoAnnotations.openMocks(this);
        S3TablesRestAwareFileSystem fileSystem = new S3TablesRestAwareFileSystem(mockDelegate);
        Location location = Location.of("s3://bucket/path/file.txt");

        fileSystem.newInputFile(location);

        verify(mockDelegate).newInputFile(location);
    }

    @Test
    public void testNewOutputFileDelegatesToUnderlying()
    {
        MockitoAnnotations.openMocks(this);
        S3TablesRestAwareFileSystem fileSystem = new S3TablesRestAwareFileSystem(mockDelegate);
        Location location = Location.of("s3://bucket/path/file.txt");

        fileSystem.newOutputFile(location);

        verify(mockDelegate).newOutputFile(location);
    }
}