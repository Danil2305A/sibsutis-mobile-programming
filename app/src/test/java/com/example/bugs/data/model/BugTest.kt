package com.example.bugs.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BugTest {

    private fun bug(
        x: Float = 100f, y: Float = 100f,
        vx: Float = 0f, vy: Float = 0f,
        type: BugType = BugType.NORMAL
    ) = Bug(0, x, y, vx, vy, type)

    @Test
    fun moveIntegratesVelocityByDt() {
        val b = bug(vx = 2f, vy = -3f)
        b.move(dt = 5f)
        assertEquals(110f, b.x, 1e-4f)
        assertEquals(85f, b.y, 1e-4f)
    }

    @Test
    fun sizeAndScoreDelegateToType() {
        assertEquals(BugType.NORMAL.size, bug(type = BugType.NORMAL).size, 0f)
        assertEquals(BugType.RARE.score, bug(type = BugType.RARE).scoreValue)
    }

    @Test
    fun anglePointsUp()  { assertEquals(0f,   bug(vy = -1f).angle, 1e-3f) }
    @Test
    fun anglePointsRight() { assertEquals(90f,  bug(vx = 1f).angle, 1e-3f) }
    @Test
    fun anglePointsDown()  { assertEquals(180f, bug(vy = 1f).angle, 1e-3f) }

    @Test
    fun leftEdgeReversesVelocity() {
        val b = bug(x = -5f, vx = -1f)
        b.handleBoundsCollision(1000f, 1000f)
        assertEquals(1f, b.velocityX, 1e-4f)
        assertEquals(0f, b.x, 1e-4f)
    }

    @Test
    fun rightEdgeReversesVelocity() {
        val b = bug(x = 950f, vx = 1f)
        b.handleBoundsCollision(1000f, 1000f)
        assertEquals(-1f, b.velocityX, 1e-4f)
        assertEquals(880f, b.x, 1e-4f)
    }

    @Test
    fun topEdgeReversesVelocity() {
        val b = bug(y = -5f, vy = -1f)
        b.handleBoundsCollision(1000f, 1000f)
        assertEquals(1f, b.velocityY, 1e-4f)
        assertEquals(0f, b.y, 1e-4f)
    }

    @Test
    fun bottomEdgeReversesVelocity() {
        val b = bug(y = 950f, vy = 1f)
        b.handleBoundsCollision(1000f, 1000f)
        assertEquals(-1f, b.velocityY, 1e-4f)
        assertEquals(880f, b.y, 1e-4f)
    }

    @Test
    fun collisionSafeWhenFieldSmallerThanBug() {
        val b = bug(x = 50f, y = 50f, vx = 1f, vy = 1f, type = BugType.GOLD)
        b.handleBoundsCollision(10f, 10f)
        assertTrue(b.x >= 0f && b.y >= 0f)
        assertTrue(b.x.isFinite() && b.y.isFinite())
    }
}