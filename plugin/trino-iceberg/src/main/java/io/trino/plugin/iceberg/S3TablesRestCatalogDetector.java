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
package io.trino.plugin.iceberg;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.trino.plugin.iceberg.catalog.rest.IcebergRestCatalogConfig;

import java.net.URI;
import java.util.Optional;

import static io.trino.plugin.iceberg.S3TablesRestCatalogUtil.isS3TablesRestCatalog;
import static java.util.Objects.requireNonNull;

/**
 * Service that detects whether the current Iceberg catalog is configured
 * as an AWS S3 Tables REST catalog. This detection is used to block
 * unsupported operations for S3 Tables REST catalogs.
 */
public class S3TablesRestCatalogDetector
{
    private final CatalogType catalogType;
    private final Provider<IcebergRestCatalogConfig> restCatalogConfigProvider;

    @Inject
    public S3TablesRestCatalogDetector(IcebergConfig icebergConfig, Provider<IcebergRestCatalogConfig> restCatalogConfigProvider)
    {
        this.catalogType = requireNonNull(icebergConfig, "icebergConfig is null").getCatalogType();
        this.restCatalogConfigProvider = requireNonNull(restCatalogConfigProvider, "restCatalogConfigProvider is null");
    }

    /**
     * Returns true if the current catalog is configured as an AWS S3 Tables REST catalog.
     */
    public boolean isS3TablesRestCatalog()
    {
        Optional<URI> restUri = getRestUri();
        return isS3TablesRestCatalog(catalogType, restUri);
    }

    /**
     * Returns the catalog type for debugging/logging purposes.
     */
    public CatalogType getCatalogType()
    {
        return catalogType;
    }

    /**
     * Returns the REST URI if available, for debugging/logging purposes.
     */
    public Optional<URI> getRestUri()
    {
        if (catalogType != CatalogType.REST) {
            return Optional.empty();
        }
        
        try {
            IcebergRestCatalogConfig config = restCatalogConfigProvider.get();
            return Optional.of(config.getBaseUri());
        }
        catch (Exception e) {
            // REST config not available (shouldn't happen if catalog type is REST, but handle gracefully)
            return Optional.empty();
        }
    }
}