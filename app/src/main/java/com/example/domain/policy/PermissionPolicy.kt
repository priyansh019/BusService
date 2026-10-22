package com.example.domain.policy

import com.example.domain.model.Session
import com.example.domain.model.UserRole

sealed class PermissionResult {
    object Allowed : PermissionResult()
    data class Denied(val reason: String, val isSecurityViolation: Boolean = false) : PermissionResult()
}

enum class Action {
    SEARCH_JOURNEYS,
    SAVE_JOURNEY,
    SUBMIT_ISSUE_REPORT,
    SUBMIT_CONTRIBUTION,
    PUBLISH_DATA,
    REVIEW_SUBMISSIONS,
    MANAGE_OWN_OPERATOR_DATA,
    MANAGE_OTHER_OPERATOR_DATA,
    START_DRIVER_TRACKING,
    MANAGE_USERS,
    MANAGE_SETTINGS,
    VIEW_AUDIT_LOG
}

object PermissionPolicy {
    fun check(session: Session?, action: Action, targetOperatorId: String? = null, targetTripId: String? = null): PermissionResult {
        val user = session?.user
        val role = user?.role ?: UserRole.PASSENGER

        return when (action) {
            Action.SEARCH_JOURNEYS,
            Action.SAVE_JOURNEY,
            Action.SUBMIT_ISSUE_REPORT -> PermissionResult.Allowed

            Action.SUBMIT_CONTRIBUTION -> {
                if (role == UserRole.CONTRIBUTOR || role == UserRole.MODERATOR || role == UserRole.ADMIN) {
                    PermissionResult.Allowed
                } else {
                    PermissionResult.Denied("Role $role cannot submit community data directly. Switch to Contributor.")
                }
            }

            Action.PUBLISH_DATA -> {
                // Direct publishing without moderation is forbidden for contributors
                if (role == UserRole.ADMIN || role == UserRole.MODERATOR) {
                    PermissionResult.Allowed
                } else {
                    PermissionResult.Denied("Role $role cannot publish data directly without moderation approval.", isSecurityViolation = true)
                }
            }

            Action.REVIEW_SUBMISSIONS -> {
                if (role == UserRole.MODERATOR || role == UserRole.ADMIN) {
                    PermissionResult.Allowed
                } else {
                    PermissionResult.Denied("Role $role is not permitted to review submissions.")
                }
            }

            Action.MANAGE_OWN_OPERATOR_DATA -> {
                if (role == UserRole.ADMIN) {
                    PermissionResult.Allowed
                } else if (role == UserRole.OPERATOR) {
                    if (targetOperatorId != null && user?.operatorId != targetOperatorId) {
                        PermissionResult.Denied("Cross-tenant modification prohibited: Operator ${user?.operatorId} attempted to access operator $targetOperatorId", isSecurityViolation = true)
                    } else {
                        PermissionResult.Allowed
                    }
                } else {
                    PermissionResult.Denied("Role $role cannot manage operator data.", isSecurityViolation = true)
                }
            }

            Action.MANAGE_OTHER_OPERATOR_DATA -> {
                if (role == UserRole.ADMIN) {
                    PermissionResult.Allowed
                } else {
                    PermissionResult.Denied("Cross-tenant modification strictly prohibited for role $role.", isSecurityViolation = true)
                }
            }

            Action.START_DRIVER_TRACKING -> {
                if (role == UserRole.DRIVER || role == UserRole.ADMIN) {
                    if (targetTripId != null && role == UserRole.DRIVER && user?.assignedTripId != targetTripId) {
                        PermissionResult.Denied("Driver is only permitted to share location for assigned trip (${user?.assignedTripId}). Attempted: $targetTripId", isSecurityViolation = true)
                    } else {
                        PermissionResult.Allowed
                    }
                } else {
                    PermissionResult.Denied("Role $role cannot initiate driver tracking.", isSecurityViolation = true)
                }
            }

            Action.MANAGE_USERS,
            Action.MANAGE_SETTINGS,
            Action.VIEW_AUDIT_LOG -> {
                if (role == UserRole.ADMIN) {
                    PermissionResult.Allowed
                } else {
                    PermissionResult.Denied("Role $role cannot access administrator functions.")
                }
            }
        }
    }
}
