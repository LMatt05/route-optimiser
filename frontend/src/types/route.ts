export interface StopInput{
    label?: string
    latitude: number
    longitude: number
}

export interface OptimiseRequest {
    start: StopInput
    destinations: StopInput[]
}

export interface Stop{
    label?: string | null
    latitude: number
    longitude: number
}

export interface Leg{
    fromIndex: number
    toIndex: number
    durationSeconds: number
}

export interface OptimiseResponse{
    stops: Stop[]
    legs: Leg[]
    totalDurationSeconds: number
    geometry?: [number, number] [] // [longitude, latitude] pairs along the roads
}