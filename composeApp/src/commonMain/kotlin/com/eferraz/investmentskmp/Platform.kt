package com.eferraz.investmentskmp

internal class JVMPlatform {
    val name: String = "Java ${System.getProperty("java.version")}"
}

internal fun getPlatform() = JVMPlatform()