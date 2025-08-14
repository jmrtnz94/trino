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

import java.net.URI;
import java.util.Optional;

import static io.trino.plugin.iceberg.CatalogType.REST;
import static java.util.Objects.requireNonNull;

/**
 * Utility class for detecting AWS S3 Tables REST Catalog usage.
 * S3 Tables REST catalogs have specific limitations that require different handling.
 */
public final class S3TablesRestCatalogUtil
{
    private S3TablesRestCatalogUtil() {}

    /**
     * Determines if the catalog configuration represents an AWS S3 Tables REST catalog.
     * S3 Tables REST catalogs are identified by:
     * - Catalog type is REST
     * - REST URI contains AWS Glue service endpoint patterns
     *
     * @param catalogType the catalog type from configuration
     * @param restUri the REST catalog URI (only checked if catalogType is REST)
     * @return true if this is an S3 Tables REST catalog
     */
    public static boolean isS3TablesRestCatalog(CatalogType catalogType, Optional<URI> restUri)
    {
        requireNonNull(catalogType, "catalogType is null");
        requireNonNull(restUri, "restUri is null");

        if (catalogType != REST) {
            return false;
        }

        if (restUri.isEmpty()) {
            return false;
        }

        return isS3TablesRestUri(restUri.get());
    }

    /**
     * Determines if a URI represents an AWS S3 Tables REST endpoint.
     * AWS S3 Tables REST endpoints typically follow patterns like:
     * - https://s3tables.us-east-1.amazonaws.com/...
     * - https://glue.us-east-1.amazonaws.com/...
     * - Any URI containing "s3tables" or "glue" in the hostname
     *
     * @param uri the REST catalog URI
     * @return true if this URI represents an S3 Tables REST endpoint
     */
    public static boolean isS3TablesRestUri(URI uri)
    {
        requireNonNull(uri, "uri is null");

        String host = uri.getHost();
        if (host == null) {
            return false;
        }

        String lowerHost = host.toLowerCase();
        return lowerHost.contains("s3tables") || 
               (lowerHost.contains("glue") && lowerHost.contains("amazonaws.com"));
    }

    /**
     * Creates a user-friendly error message for unsupported S3 Tables REST operations.
     *
     * @param operation the operation that is not supported
     * @return formatted error message with documentation reference
     */
    public static String createUnsupportedOperationMessage(String operation)
    {
        return String.format(
                "The '%s' operation is not supported with AWS S3 Tables REST Catalog. " +
                "AWS S3 Tables REST API provides table-centric access and does not support direct file operations. " +
                "Please refer to the Trino documentation for supported operations with S3 Tables REST catalogs.",
                operation);
    }
}