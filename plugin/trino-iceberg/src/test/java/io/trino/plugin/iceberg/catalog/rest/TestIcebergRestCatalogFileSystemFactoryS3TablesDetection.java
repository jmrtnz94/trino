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

import io.trino.filesystem.TrinoFileSystem;
import io.trino.filesystem.TrinoFileSystemFactory;
import io.trino.spi.security.ConnectorIdentity;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class TestIcebergRestCatalogFileSystemFactoryS3TablesDetection
{
    @Mock
    private TrinoFileSystemFactory mockFileSystemFactory;

    @Mock
    private TrinoFileSystem mockFileSystem;

    @Test
    public void testS3TablesRestDetectionWrapsFileSystem()
    {
        MockitoAnnotations.openMocks(this);

        // Create config for S3 Tables REST
        IcebergRestCatalogConfig config = new IcebergRestCatalogConfig();
        config.setBaseUri("https://s3tables.us-east-1.amazonaws.com");
        config.setVendedCredentialsEnabled(false);

        when(mockFileSystemFactory.create(ConnectorIdentity.ofUser("test")))
                .thenReturn(mockFileSystem);

        IcebergRestCatalogFileSystemFactory factory = new IcebergRestCatalogFileSystemFactory(mockFileSystemFactory, config);

        TrinoFileSystem result = factory.create(ConnectorIdentity.ofUser("test"), Map.of());

        // Should return wrapped file system for S3 Tables REST
        assertThat(result).isInstanceOf(S3TablesRestAwareFileSystem.class);
    }

    @Test
    public void testGlueRestDetectionWrapsFileSystem()
    {
        MockitoAnnotations.openMocks(this);

        // Create config for Glue REST
        IcebergRestCatalogConfig config = new IcebergRestCatalogConfig();
        config.setBaseUri("https://glue.us-west-2.amazonaws.com");
        config.setVendedCredentialsEnabled(false);

        when(mockFileSystemFactory.create(ConnectorIdentity.ofUser("test")))
                .thenReturn(mockFileSystem);

        IcebergRestCatalogFileSystemFactory factory = new IcebergRestCatalogFileSystemFactory(mockFileSystemFactory, config);

        TrinoFileSystem result = factory.create(ConnectorIdentity.ofUser("test"), Map.of());

        // Should return wrapped file system for Glue REST
        assertThat(result).isInstanceOf(S3TablesRestAwareFileSystem.class);
    }

    @Test
    public void testNonS3TablesRestDoesNotWrapFileSystem()
    {
        MockitoAnnotations.openMocks(this);

        // Create config for non-S3 Tables REST
        IcebergRestCatalogConfig config = new IcebergRestCatalogConfig();
        config.setBaseUri("https://catalog.example.com");
        config.setVendedCredentialsEnabled(false);

        when(mockFileSystemFactory.create(ConnectorIdentity.ofUser("test")))
                .thenReturn(mockFileSystem);

        IcebergRestCatalogFileSystemFactory factory = new IcebergRestCatalogFileSystemFactory(mockFileSystemFactory, config);

        TrinoFileSystem result = factory.create(ConnectorIdentity.ofUser("test"), Map.of());

        // Should return original file system for non-S3 Tables REST
        assertThat(result).isSameAs(mockFileSystem);
    }
}