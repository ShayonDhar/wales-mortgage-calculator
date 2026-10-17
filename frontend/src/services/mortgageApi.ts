import type { AffordabilityRequest, AffordabilityResponse } from '../types/mortgage'

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api')
  .replace(/\/+$/, '')

export async function calculateAffordability(
  request: AffordabilityRequest,
): Promise<AffordabilityResponse> {
  let response: Response

  try {
    response = await fetch(`${apiBaseUrl}/v1/mortgages/calculate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    })
  } catch {
    throw new Error('The mortgage service is unavailable. Check that the API is running and try again.')
  }

  if (!response.ok) {
    const detail = await response.text()
    throw new Error(detail || `The mortgage service returned an error (${response.status}).`)
  }

  return response.json() as Promise<AffordabilityResponse>
}
