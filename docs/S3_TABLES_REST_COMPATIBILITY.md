# S3 Tables REST Catalog Compatibility

This document describes the changes made to handle AWS S3 Tables REST Catalog limitations in Trino.

## Background

AWS S3 Tables provides a REST API for table management but does not support direct file-level operations like listing, deleting, or renaming individual files. This causes issues in Trino when:

1. Using the `sorted_by` table property (requires file merging/cleanup operations)
2. Performing fault-tolerant execution cleanup (requires listing/deleting orphaned files)
3. Other operations that need direct S3 object access

## Changes Made

### 1. File System Wrapper (`S3TablesRestAwareFileSystem`)

- **Location**: `plugin/trino-iceberg/src/main/java/io/trino/plugin/iceberg/catalog/rest/S3TablesRestAwareFileSystem.java`
- **Purpose**: Wraps the underlying file system to handle unsupported operations gracefully
- **Behavior**:
  - Skips file deletion operations (logs warnings instead of failing)
  - Returns empty results for file listing operations
  - Delegates supported operations (read, write) to underlying implementation
  - Logs appropriate warnings so operators understand what's happening

### 2. Automatic Detection and Wrapping

- **Location**: `plugin/trino-iceberg/src/main/java/io/trino/plugin/iceberg/catalog/rest/IcebergRestCatalogFileSystemFactory.java`
- **Purpose**: Automatically detects S3 Tables REST catalogs and wraps file systems
- **Detection Logic**: Checks if REST URI matches patterns:
  - `https://s3tables.*.amazonaws.com`
  - `https://glue.*.amazonaws.com`

### 3. Sorted Table Validation

- **Location**: `plugin/trino-iceberg/src/main/java/io/trino/plugin/iceberg/IcebergMetadata.java`
- **Purpose**: Blocks `sorted_by` table property for REST catalogs
- **Behavior**: Throws clear error message explaining the limitation

### 4. Detection Utilities

- **Location**: `plugin/trino-iceberg/src/main/java/io/trino/plugin/iceberg/S3TablesRestCatalogUtil.java`
- **Purpose**: Utility methods for detecting S3 Tables REST catalogs

## User Impact

### Positive Changes
- Eliminates "Failed to delete file" errors during table operations
- Provides clear error messages for unsupported operations
- Graceful handling prevents hard failures

### Breaking Changes
- `sorted_by` table property is now blocked for REST catalogs
- File cleanup operations are skipped (may leave orphaned files in some scenarios)

### Error Messages
Users will see clear error messages like:
```
The 'sorted_by' table property is not supported with REST catalogs when using AWS S3 Tables. 
AWS S3 Tables REST API provides table-centric access and does not support direct file operations 
required for sorting. If you are using a different REST catalog implementation that supports 
file-level operations, please contact your administrator or consider using a different catalog type.
```

## Configuration

No additional configuration is required. The changes are automatically applied when:
- Using `iceberg.catalog.type=rest`
- REST URI matches S3 Tables patterns

## Limitations

### Current Approach
- The validation is conservative and blocks ALL REST catalogs from using `sorted_by`
- Some non-S3 Tables REST implementations might support file operations but are still blocked

### Future Enhancements
- Add configuration property to override validation for non-S3 Tables REST catalogs
- More precise detection of S3 Tables vs other REST catalog implementations
- Integration with Iceberg's native S3 Tables support when available

## Testing

### Unit Tests
- `TestS3TablesRestCatalogUtil`: Tests detection logic
- `TestS3TablesRestAwareFileSystem`: Tests file system wrapper behavior
- `TestIcebergRestCatalogFileSystemFactoryS3TablesDetection`: Tests automatic wrapping

### Integration Testing
The changes are designed to be backward compatible. Existing tables and operations continue to work, with improved error handling for unsupported scenarios.

## Troubleshooting

### "sorted_by not supported" Error
**Cause**: Attempting to use `sorted_by` with a REST catalog
**Solution**: Remove the `sorted_by` property or use a different catalog type (glue, hms, etc.)

### Files Not Being Cleaned Up
**Cause**: File cleanup operations are skipped for S3 Tables REST
**Solution**: This is expected behavior. Use table-level operations for data management

### Unexpected "File operation skipped" Warnings
**Cause**: Operations that normally delete/list files are being skipped
**Solution**: This is expected for S3 Tables REST. The warnings are informational.

## Documentation References

- [Iceberg Connector Documentation](https://trino.io/docs/current/connector/iceberg.html)
- [AWS S3 Tables Documentation](https://docs.aws.amazon.com/s3tables/)
- [Iceberg REST Catalog Specification](https://iceberg.apache.org/docs/latest/rest-catalog/)