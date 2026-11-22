package com.ucp.biomass.ar.core

import com.ucp.biomass.ar.core.math.CylindricalPoint
import com.ucp.biomass.ar.core.math.Matrix3x3
import com.ucp.biomass.ar.core.math.SphericalPoint
import com.ucp.biomass.ar.core.math.Vector3D
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

class Vector3DTest {

    private val epsilon = 1e-6

    @Test
    fun testVectorOperations() {
        val v1 = Vector3D(1.0, 2.0, 3.0)
        val v2 = Vector3D(4.0, -1.0, 2.0)

        val sum = v1 + v2
        assertEquals(5.0, sum.x, epsilon)
        assertEquals(1.0, sum.y, epsilon)
        assertEquals(5.0, sum.z, epsilon)

        val diff = v1 - v2
        assertEquals(-3.0, diff.x, epsilon)
        assertEquals(3.0, diff.y, epsilon)
        assertEquals(1.0, diff.z, epsilon)

        val dot = v1 dot v2
        // 1*4 + 2*(-1) + 3*2 = 4 - 2 + 6 = 8
        assertEquals(8.0, dot, epsilon)

        val cross = v1 cross v2
        // y1*z2 - z1*y2 = 2*2 - 3*(-1) = 7
        // z1*x2 - x1*z2 = 3*4 - 1*2 = 10
        // x1*y2 - y1*x2 = 1*(-1) - 2*4 = -9
        assertEquals(7.0, cross.x, epsilon)
        assertEquals(10.0, cross.y, epsilon)
        assertEquals(-9.0, cross.z, epsilon)

        // Vector ortogonal a los dos originales
        assertEquals(0.0, cross dot v1, epsilon)
        assertEquals(0.0, cross dot v2, epsilon)
    }

    @Test
    fun testMatrixDeterminantAndRotation() {
        val identity = Matrix3x3.IDENTITY
        assertEquals(1.0, identity.determinant(), epsilon)

        // Rotación de 90 grados (PI/2) alrededor de Z
        val rotZ = Matrix3x3.rotationAroundAxis(Vector3D.UNIT_Z, PI / 2.0)
        assertEquals(1.0, rotZ.determinant(), epsilon)

        val v = Vector3D(1.0, 0.0, 0.0)
        val rotated = rotZ * v
        assertEquals(0.0, rotated.x, epsilon)
        assertEquals(1.0, rotated.y, epsilon)
        assertEquals(0.0, rotated.z, epsilon)
    }

    @Test
    fun testCylindricalCoordinateConversionAndJacobian() {
        val cartesian = Vector3D(0.0, 2.0, 5.0)
        val cylindrical = CylindricalPoint.fromCartesian(cartesian)

        assertEquals(2.0, cylindrical.r, epsilon)
        assertEquals(PI / 2.0, cylindrical.theta, epsilon)
        assertEquals(5.0, cylindrical.z, epsilon)

        // Jacobiano J = r
        assertEquals(2.0, cylindrical.jacobian(), epsilon)

        val backToCartesian = cylindrical.toCartesian()
        assertEquals(cartesian.x, backToCartesian.x, epsilon)
        assertEquals(cartesian.y, backToCartesian.y, epsilon)
        assertEquals(cartesian.z, backToCartesian.z, epsilon)
    }

    @Test
    fun testSphericalCoordinateConversionAndJacobian() {
        val cartesian = Vector3D(0.0, 0.0, 3.0)
        val spherical = SphericalPoint.fromCartesian(cartesian)

        assertEquals(3.0, spherical.rho, epsilon)
        assertEquals(0.0, spherical.phi, epsilon) // En el eje Z positivo, phi = 0

        val pointXY = Vector3D(2.0, 0.0, 0.0)
        val sphXY = SphericalPoint.fromCartesian(pointXY)
        assertEquals(2.0, sphXY.rho, epsilon)
        assertEquals(PI / 2.0, sphXY.phi, epsilon)
        assertEquals(0.0, sphXY.theta, epsilon)

        // Jacobiano en el ecuador: J = rho^2 * sin(PI/2) = 2^2 * 1 = 4
        assertEquals(4.0, sphXY.jacobian(), epsilon)
    }
}
