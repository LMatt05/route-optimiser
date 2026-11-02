import L from 'leaflet'
import { MapContainer, Marker, Polyline, TileLayer, useMapEvents } from 'react-leaflet'
import type { OptimiseResponse, StopInput } from "../types/route.ts"

const LONDON: [number, number] = [51.5074, -0.1278] // default map region

function pin(text: string | number, isStart: boolean){
    return L.divIcon({
        className: '',
        html: `<div class ="pin ${isStart? 'pin-start' : ''}">${text}</div>`,
        iconSize: [28,28],
        iconAnchor: [14,14],
    })
}

function ClickHandler({ onClick }: { onClick: (lat: number, lng: number) => void }) {
    useMapEvents({ click: (e) => onClick(e.latlng.lat, e.latlng.lng) })
    return null
}

interface Props {
    start: StopInput | null
    destinations: StopInput[]
    result?: OptimiseResponse
    onMapClick: (lat: number, lng: number) => void
}

export default function MapView({ start, destinations, result, onMapClick }: Props){
    //after optimising, show points in visiting order; before, in the order added.
    const points = result ? result.stops : start ? [start, ...destinations] : []

    return(
        <MapContainer center={LONDON} zoom={11} className ="map">
            <TileLayer
                attribution="&copy; OpenStreetMap contributors"
                url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            <ClickHandler onClick={onMapClick}/>

            {points.map((p,i) =>(
                <Marker
                key={`${p.latitude}-${p.longitude}-${i}`}
                position={[p.latitude, p.longitude]}
                icon={pin(i === 0 ? 'S' : i, i === 0)}
                />
            ))}

            {result && (
                <Polyline positions={result.stops.map((s) => [s.latitude, s.longitude] as [number, number])} />
            )}
        </MapContainer>
    )
}