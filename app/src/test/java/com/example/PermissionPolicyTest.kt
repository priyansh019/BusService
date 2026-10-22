package com.example

import com.example.domain.policy.Action
import com.example.domain.model.Session
import com.example.domain.model.User
import com.example.domain.model.UserRole
import com.example.domain.policy.PermissionPolicy
import com.example.domain.policy.PermissionResult
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionPolicyTest {

    @Test
    fun test_passenger_permissions() {
        val session = Session(User("u_pass", "Passenger", UserRole.PASSENGER))
        assertTrue(PermissionPolicy.check(session, Action.SEARCH_JOURNEYS) is PermissionResult.Allowed)
        assertTrue(PermissionPolicy.check(session, Action.SAVE_JOURNEY) is PermissionResult.Allowed)
        assertTrue(PermissionPolicy.check(session, Action.SUBMIT_CONTRIBUTION) is PermissionResult.Denied)
        assertTrue(PermissionPolicy.check(session, Action.PUBLISH_DATA) is PermissionResult.Denied)
        assertTrue(PermissionPolicy.check(session, Action.MANAGE_SETTINGS) is PermissionResult.Denied)
    }

    @Test
    fun test_contributor_cannot_publish_directly() {
        val session = Session(User("u_contrib", "Contributor", UserRole.CONTRIBUTOR))
        assertTrue(PermissionPolicy.check(session, Action.SUBMIT_CONTRIBUTION) is PermissionResult.Allowed)
        // Non-negotiable honesty rule: Contributor cannot publish without moderation
        val pubCheck = PermissionPolicy.check(session, Action.PUBLISH_DATA)
        assertTrue(pubCheck is PermissionResult.Denied)
        assertTrue((pubCheck as PermissionResult.Denied).isSecurityViolation)
    }

    @Test
    fun test_operator_cross_tenant_prohibited() {
        val opSession = Session(User("u_op", "Operator City", UserRole.OPERATOR, operatorId = "op_city"))

        // Own tenant: Allowed
        val ownCheck = PermissionPolicy.check(opSession, Action.MANAGE_OWN_OPERATOR_DATA, targetOperatorId = "op_city")
        assertTrue(ownCheck is PermissionResult.Allowed)

        // Other tenant: Denied and flagged as security violation
        val crossCheck = PermissionPolicy.check(opSession, Action.MANAGE_OWN_OPERATOR_DATA, targetOperatorId = "op_state")
        assertTrue(crossCheck is PermissionResult.Denied)
        assertTrue((crossCheck as PermissionResult.Denied).isSecurityViolation)
    }

    @Test
    fun test_driver_assigned_trip_only() {
        val driverSession = Session(User("u_driver", "Driver", UserRole.DRIVER, assignedTripId = "TRIP_123"))

        // Assigned trip: Allowed
        val assignedCheck = PermissionPolicy.check(driverSession, Action.START_DRIVER_TRACKING, targetTripId = "TRIP_123")
        assertTrue(assignedCheck is PermissionResult.Allowed)

        // Unassigned trip: Denied
        val unassignedCheck = PermissionPolicy.check(driverSession, Action.START_DRIVER_TRACKING, targetTripId = "TRIP_999")
        assertTrue(unassignedCheck is PermissionResult.Denied)
    }

    @Test
    fun test_moderator_cannot_change_system_settings() {
        val modSession = Session(User("u_mod", "Moderator", UserRole.MODERATOR))
        assertTrue(PermissionPolicy.check(modSession, Action.REVIEW_SUBMISSIONS) is PermissionResult.Allowed)
        assertTrue(PermissionPolicy.check(modSession, Action.MANAGE_SETTINGS) is PermissionResult.Denied)
        assertTrue(PermissionPolicy.check(modSession, Action.MANAGE_USERS) is PermissionResult.Denied)
    }

    @Test
    fun test_admin_has_full_management_access() {
        val adminSession = Session(User("u_admin", "Admin", UserRole.ADMIN))
        assertTrue(PermissionPolicy.check(adminSession, Action.MANAGE_SETTINGS) is PermissionResult.Allowed)
        assertTrue(PermissionPolicy.check(adminSession, Action.MANAGE_USERS) is PermissionResult.Allowed)
        assertTrue(PermissionPolicy.check(adminSession, Action.VIEW_AUDIT_LOG) is PermissionResult.Allowed)
    }
}
