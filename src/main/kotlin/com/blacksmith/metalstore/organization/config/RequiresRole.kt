package com.blacksmith.metalstore.organization.config

import com.blacksmith.metalstore.organization.domain.entity.OrganizationRole

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresRole(
    val value: OrganizationRole,
    val orgIdFromArg: Int = -1,
)
