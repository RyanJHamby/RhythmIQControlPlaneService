$version: "2"

namespace com.example.profiles

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
    @http(label: "username")
    username: string

    @required
    firstName: string

    @required
    lastName: string

    @required
    email: string

    @required
    phoneNumber: string

    @required
    password: string
}

structure CreateProfileResponse {
    message: string
    profileId: string
}
// @httpError(code: 400)
// structure ValidationError {
//    message: string
//    // errors: map<string, string>
// }
// @httpError(code: 409)
// structure ConflictError {
//    message: string
// }
