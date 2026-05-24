package com.eferraz.usecases.entities

public sealed interface B3IdentifierStatus {

    public data class Informed(val value: String) : B3IdentifierStatus

    public data object NotInformed : B3IdentifierStatus

    public data object NotApplicable : B3IdentifierStatus
}
