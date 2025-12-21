package com.eferraz.usecases.exceptions

public class ValidateException(public val messages: Map<String, String>) : Exception()