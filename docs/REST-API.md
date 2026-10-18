# REST API

Base URL in local setups: `http://localhost:8080`. The interactive OpenAPI documentation is served by Swagger UI at
`/swagger-ui/index.html` - the root path `/` redirects there.

All endpoints consume and produce `application/json`. Errors are returned as Problem Details, see
[Errors](#errors).

## Coupons

### Create coupon

`POST /api/v1/coupons`

```json
{
  "code": "SUMMER2026",
  "usageLimit": 5,
  "countryCode": "PL"
}
```

- `code` - 1-255 alphanumeric characters; trimmed and uppercased
- `usageLimit` - integer, at least 1
- `countryCode` - 1-8 letters; trimmed and uppercased

Responses:

- `201 Created` - `{"id": "0199a3c4-..."}`
- `400 Bad Request` - validation failed
- `409 Conflict` - `COUPON_ALREADY_EXISTS`, a coupon with the same code exists

### List coupons

`GET /api/v1/coupons?page=0&size=20&code=SUMMER2026`

- `page` - zero-based page number, default `0`, at least 0
- `size` - page size, default `20`, between 1 and 500
- `code` - optional filter, 1-255 alphanumeric characters; exact match, case-insensitive

Coupons are ordered by creation time, oldest first. List items do not include `countryCode`.

```json
{
  "content": [
    {
      "id": "0199a3c4-...",
      "code": "SUMMER2026",
      "usageLimit": 5,
      "usageCount": 2,
      "createdAt": "2026-06-26T12:33:42Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 37
}
```

### Get coupon

`GET /api/v1/coupons/{id}`

- `200 OK` - the coupon, including `countryCode`:
  ```json
  {
    "id": "0199a3c4-...",
    "code": "SUMMER2026",
    "usageLimit": 5,
    "usageCount": 2,
    "countryCode": "PL",
    "createdAt": "2026-06-26T12:33:42Z"
  }
  ```
- `404 Not Found` - `COUPON_BY_ID_NOT_EXISTS`

## Coupon redemptions

### Redeem coupon

`POST /api/v1/coupon-redemptions`

```json
{
  "code": "summer2026",
  "userId": "8c6d0f1e-4b8a-4c57-9a3e-2f1f5d6b7a10"
}
```

- `code` - 1-255 alphanumeric characters; trimmed and uppercased
- `userId` - UUID of the user redeeming the coupon

The client's country is resolved from the `X-Forwarded-For` header (first address) or the connection address. See
[Business Rules](./Business-Rules.md#redemption) for the full decision flow.

Responses:

- `201 Created` - redeemed, empty body
- `400 Bad Request` - validation failed, or one of `COUPON_BY_CODE_NOT_EXISTS`, `COUPON_UNAVAILABLE_IN_COUNTRY`,
  `COUPON_USAGE_LIMIT_REACHED`
- `500 Internal Server Error` - `FAILED_TO_LOCALIZE_CLIENT`, the geolocation service failed

## Errors

Errors follow [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) Problem Details, produced by
[Problem4J](https://problem4j.github.io).

Business errors carry a machine-readable `code`:

```json
{
  "title": "Bad Request",
  "status": 400,
  "detail": "coupon usage limit has been reached",
  "code": "COUPON_USAGE_LIMIT_REACHED",
  "instance": "6a1f0c2b9e4d4f7a8b3c2d1e0f9a8b7c"
}
```

Validation errors list each failing field:

```json
{
  "title": "Bad Request",
  "status": 400,
  "detail": "validation failed",
  "errors": [
    { "field": "usageLimit", "error": "must be greater than or equal to 1" }
  ],
  "instance": "6a1f0c2b9e4d4f7a8b3c2d1e0f9a8b7c"
}
```

`instance` holds the request's trace id, which is also attached to every log line of the request - quote it when
reporting a problem.

| Code                            | Status | Meaning                                         |
|---------------------------------|--------|-------------------------------------------------|
| `COUPON_ALREADY_EXISTS`         | 409    | a coupon with the same code already exists      |
| `COUPON_BY_ID_NOT_EXISTS`       | 404    | no coupon with the given id                     |
| `COUPON_BY_CODE_NOT_EXISTS`     | 400    | no coupon with the given code                   |
| `COUPON_UNAVAILABLE_IN_COUNTRY` | 400    | client's country differs from coupon's country  |
| `COUPON_USAGE_LIMIT_REACHED`    | 400    | coupon exhausted, or already redeemed by user   |
| `FAILED_TO_LOCALIZE_CLIENT`     | 500    | client's country could not be resolved          |
