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

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Optional;

import static io.trino.plugin.iceberg.CatalogType.GLUE;
import static io.trino.plugin.iceberg.CatalogType.HIVE_METASTORE;
import static io.trino.plugin.iceberg.CatalogType.REST;
import static io.trino.plugin.iceberg.S3TablesRestCatalogUtil.isS3TablesRestCatalog;
import static io.trino.plugin.iceberg.S3TablesRestCatalogUtil.isS3TablesRestUri;
import static org.assertj.core.api.Assertions.assertThat;

public class TestS3TablesRestCatalogUtil
{
    @Test
    public void testIsS3TablesRestCatalog()
    {
        // Not REST catalog types should return false
        assertThat(isS3TablesRestCatalog(HIVE_METASTORE, Optional.empty())).isFalse();
        assertThat(isS3TablesRestCatalog(GLUE, Optional.empty())).isFalse();
        assertThat(isS3TablesRestCatalog(HIVE_METASTORE, Optional.of(URI.create("https://s3tables.us-east-1.amazonaws.com")))).isFalse();

        // REST catalog without URI should return false
        assertThat(isS3TablesRestCatalog(REST, Optional.empty())).isFalse();

        // REST catalog with non-S3 Tables URI should return false
        assertThat(isS3TablesRestCatalog(REST, Optional.of(URI.create("https://example.com/rest")))).isFalse();
        assertThat(isS3TablesRestCatalog(REST, Optional.of(URI.create("https://catalog.example.com")))).isFalse();

        // REST catalog with S3 Tables URI should return true
        assertThat(isS3TablesRestCatalog(REST, Optional.of(URI.create("https://s3tables.us-east-1.amazonaws.com")))).isTrue();
        assertThat(isS3TablesRestCatalog(REST, Optional.of(URI.create("https://glue.us-west-2.amazonaws.com")))).isTrue();
        assertThat(isS3TablesRestCatalog(REST, Optional.of(URI.create("https://s3tables.eu-west-1.amazonaws.com/v1/catalog")))).isTrue();
    }

    @Test
    public void testIsS3TablesRestUri()
    {
        // S3 Tables endpoints
        assertThat(isS3TablesRestUri(URI.create("https://s3tables.us-east-1.amazonaws.com"))).isTrue();
        assertThat(isS3TablesRestUri(URI.create("https://s3tables.us-west-2.amazonaws.com"))).isTrue();
        assertThat(isS3TablesRestUri(URI.create("https://s3tables.eu-west-1.amazonaws.com"))).isTrue();
        assertThat(isS3TablesRestUri(URI.create("https://s3tables.ap-southeast-1.amazonaws.com/v1"))).isTrue();

        // Glue endpoints (case insensitive)
        assertThat(isS3TablesRestUri(URI.create("https://glue.us-east-1.amazonaws.com"))).isTrue();
        assertThat(isS3TablesRestUri(URI.create("https://GLUE.us-west-2.amazonaws.com"))).isTrue();
        assertThat(isS3TablesRestUri(URI.create("https://glue.eu-west-1.amazonaws.com/catalog"))).isTrue();

        // Non-S3 Tables endpoints
        assertThat(isS3TablesRestUri(URI.create("https://example.com"))).isFalse();
        assertThat(isS3TablesRestUri(URI.create("https://catalog.example.com"))).isFalse();
        assertThat(isS3TablesRestUri(URI.create("https://rest-catalog.company.com"))).isFalse();
        assertThat(isS3TablesRestUri(URI.create("https://amazonaws.com"))).isFalse();

        // Edge cases
        assertThat(isS3TablesRestUri(URI.create("https://not-glue.amazonaws.com"))).isFalse();
        assertThat(isS3TablesRestUri(URI.create("https://glue.example.com"))).isFalse(); // Not amazonaws.com
        assertThat(isS3TablesRestUri(URI.create("file:///local/path"))).isFalse(); // No host
    }
}