export const postcodeAreas = [
  { code: 'CF', name: 'Cardiff & the Vale' },
  { code: 'CH', name: 'North East Wales' },
  { code: 'HR', name: 'Herefordshire & Mid Wales' },
  { code: 'LD', name: 'Llandrindod Wells' },
  { code: 'LL', name: 'North Wales' },
  { code: 'NP', name: 'Newport & Gwent' },
  { code: 'SA', name: 'Swansea & West Wales' },
  { code: 'SY', name: 'Shropshire & Mid Wales' },
] as const

/** HM Land Registry property type codes. */
export const propertyTypes = [
  { code: 'D', name: 'Detached' },
  { code: 'S', name: 'Semi-detached' },
  { code: 'T', name: 'Terraced' },
  { code: 'F', name: 'Flats & maisonettes' },
  { code: 'O', name: 'Other' },
] as const

export const mortgageTermOptions = [15, 20, 25, 30, 35, 40] as const

export function propertyTypeLabel(code: string) {
  return propertyTypes.find((type) => type.code === code)?.name ?? code
}
