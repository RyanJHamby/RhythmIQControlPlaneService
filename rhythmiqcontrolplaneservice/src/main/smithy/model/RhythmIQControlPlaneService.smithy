$version: "2"

namespace com.rhythmiq.controlplaneservice

use smithy.api#String
use smithy.api#http
use smithy.api#required

@http(method: "POST", uri: "/profiles", code: 201)
operation CreateProfile {
    input: CreateProfileRequest
    output: CreateProfileResponse
    errors: [
        ValidationError
        ConflictError
    ]
}

structure CreateProfileRequest {
    @required
    username: String

    @required
    firstName: String

    @required
    lastName: String

    @required
    email: String

    @required
    phoneNumber: String

    @required
    password: String
}

structure CreateProfileResponse {
    message: String
    profileId: String
}

@error("client")
@httpError(400)
structure ValidationError {
    message: String
    errors: ValidationErrorMap
}

@error("client")
@httpError(409)
structure ConflictError {
    message: String
}

map ValidationErrorMap {
    key: String
    value: Integer
}
