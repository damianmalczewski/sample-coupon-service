export class ApiError extends Error {
  constructor(status, problem) {
    super(problem?.detail ?? `request failed with status ${status}`)
    this.status = status
    this.problem = problem
  }
}

async function request(method, path, body) {
  const response = await fetch(path, {
    method,
    headers: body ? { 'Content-Type': 'application/json' } : undefined,
    body: body ? JSON.stringify(body) : undefined,
  })
  const data = await response.json().catch(() => null)
  if (!response.ok) {
    throw new ApiError(response.status, data)
  }
  return data
}

export function listCoupons({ page, size, code }) {
  const params = new URLSearchParams({ page, size })
  if (code) {
    params.set('code', code)
  }
  return request('GET', `/api/v1/coupons?${params}`)
}

export function createCoupon(coupon) {
  return request('POST', '/api/v1/coupons', coupon)
}

export function redeemCoupon(redemption) {
  return request('POST', '/api/v1/coupon-redemptions', redemption)
}

// problem+json to a single line, e.g. "validation failed: code must be alphanumeric"
export function describeError(error) {
  const fieldErrors = error.problem?.errors ?? []
  if (fieldErrors.length === 0) {
    return error.message
  }
  return `${error.message}: ${fieldErrors.map((e) => `${e.field} ${e.error}`).join(', ')}`
}
