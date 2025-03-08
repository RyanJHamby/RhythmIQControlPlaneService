$version: "2"

namespace com.rhythmiq.controlplaneservice

use aws.auth#sigv4
use aws.protocols#restJson1
use smithy.api#String
use smithy.api#http
use smithy.api#readonly
use smithy.api#required

@restJson1
@sigv4(name: "rhythmiqcontrolplaneservice")
service RhythmIqControlPlaneService {
    version: "2024-02-25"
    operations: [
        CreateProfile
        GetProfile
        UpdateProfile
        DeleteProfile
        ListProfiles
    ]
}

@http(method: "POST", uri: "/profiles", code: 201)
operation CreateProfile {
    input: CreateProfileRequest
    output: CreateProfileResponse
    errors: [
        ValidationException
        ConflictException
    ]
}

@readonly
@http(method: "GET", uri: "/profiles/{profileId}", code: 200)
operation GetProfile {
    input: GetProfileRequest
    output: GetProfileResponse
    errors: [
        NotFoundException
    ]
}

@idempotent
@http(method: "PUT", uri: "/profiles/{profileId}", code: 200)
operation UpdateProfile {
    input: UpdateProfileRequest
    output: UpdateProfileResponse
    errors: [
        ValidationException
        NotFoundException
    ]
}

@idempotent
@http(method: "DELETE", uri: "/profiles/{profileId}", code: 204)
operation DeleteProfile {
    input: DeleteProfileRequest
    errors: [
        NotFoundException
    ]
}

@readonly
@http(method: "GET", uri: "/profiles", code: 200)
operation ListProfiles {
    input: ListProfilesRequest
    output: ListProfilesResponse
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

structure GetProfileRequest {
    @required
    @httpLabel
    profileId: String
}

structure GetProfileResponse {
    profileId: String
    username: String
    firstName: String
    lastName: String
    email: String
    phoneNumber: String
}

structure UpdateProfileRequest {
    @required
    @httpLabel
    profileId: String

    username: String

    firstName: String

    lastName: String

    email: String

    phoneNumber: String

    password: String
}

structure UpdateProfileResponse {
    message: String
}

structure DeleteProfileRequest {
    @required
    @httpLabel
    profileId: String
}

structure ListProfilesRequest {}

structure ListProfilesResponse {
    profiles: ProfileSummaryList
}

list ProfileSummaryList {
    member: ProfileSummary
}

structure ProfileSummary {
    profileId: String
    username: String
    firstName: String
    lastName: String
    email: String
}

@error("client")
@httpError(400)
structure ValidationException {
    message: String
    errors: ValidationExceptionMap
}

@error("client")
@httpError(404)
structure NotFoundException {
    message: String
}

@error("client")
@httpError(409)
structure ConflictException {
    message: String
}

map ValidationExceptionMap {
    key: String
    value: Integer
}
