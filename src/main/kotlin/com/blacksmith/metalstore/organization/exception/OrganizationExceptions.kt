package com.blacksmith.metalstore.organization.exception

import com.blacksmith.metalstore.shared.exception.ApiException
import com.blacksmith.metalstore.shared.exception.ErrorCode

class MissingOrganizationHeaderException :
    ApiException(ErrorCode.MISSING_ORGANIZATION, "Falta el header X-Organization-Id")

class InvalidOrganizationIdException(headerValue: String) :
    ApiException(ErrorCode.INVALID_ORGANIZATION, "X-Organization-Id debe ser un UUID válido, se recibió: '$headerValue'")

class NotOrganizationMemberException :
    ApiException(ErrorCode.UNAUTHORIZED, "No eres miembro de esta organización")

class OrganizationNotFoundException :
    ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Organización no encontrada")

class MembershipNotFoundException :
    ApiException(ErrorCode.RESOURCE_NOT_FOUND, "El usuario no es miembro de esta organización")

class RoleRequiredException(role: String) :
    ApiException(ErrorCode.FORBIDDEN, "Se requiere rol $role para realizar esta acción")

class InvitationNotFoundException :
    ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Invitación no encontrada")

class InvitationAlreadyAcceptedException :
    ApiException(ErrorCode.RESOURCE_CONFLICT, "La invitación ya fue aceptada")

class DuplicateInvitationException :
    ApiException(ErrorCode.RESOURCE_CONFLICT, "Ya existe una invitación pendiente para este email")

class CannotRemoveLastOwnerException :
    ApiException(ErrorCode.RESOURCE_CONFLICT, "No se puede eliminar el único propietario de la organización")
