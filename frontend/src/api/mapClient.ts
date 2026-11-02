import type { MapBounds } from '../types/map'
import { ApiRequestError } from "./routeClient.ts"

export async function fetchMapBounds(): Promise<MapBounds> {
    const res = await fetch('/api/v1/map/bounds')
    if(!res.ok) throw new ApiRequestError(res.status, res.statusText)
    return res.json()
}