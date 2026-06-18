package com.blacksmith.metalstore.shared.exception

import java.util.UUID

class ResourceNotFoundException(
    resourceType: String,
    id: UUID
) : ApiException(ErrorCode.RESOURCE_NOT_FOUND, "$resourceType with id $id not found")

class ResourceNotFoundExceptionByField(
    resourceType: String,
    field: String,
    value: String
) : ApiException(ErrorCode.RESOURCE_NOT_FOUND, "$resourceType with $field '$value' not found")
