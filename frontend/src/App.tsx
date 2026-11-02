import { useState } from 'react'
import type {StopInput} from "./types/route.ts";
import {ApiRequestError, optimiseRoute} from "./api/routeClient.ts";
import {useMutation} from "@tanstack/react-query";
import MapView from "./components/MapView.tsx";
import { useMapBounds } from './hooks/useMapBounds.ts'




const MAX_STOPS = 50 // matches route.max-stops, includes start.

const round = (n: number) => Math.round(n*1e5) / 1e5

export default function App() {
  const [start, setStart] = useState<StopInput | null>(null)
  const [destinations, setDestinations] = useState<StopInput[]>([])
  const mutation = useMutation({ mutationFn: optimiseRoute})
  const { bounds, isLoading: boundsLoading, error: boundsError } = useMapBounds()
  const [notice, setNotice] = useState<string | null>(null)

  function handleMapClick(lat: number, lng: number){
    if(!bounds?.contains([lat, lng])) {
      setNotice('That point is outside the loaded map area.')
      return
    }
    setNotice(null)
    mutation.reset() // a new point invalidates the old result
    const point = { latitude: round(lat), longitude: round(lng)}
    if(!start) setStart({ ...point, label: 'Start' })
    else if(destinations.length + 1 < MAX_STOPS)
      setDestinations([...destinations, { ...point, label: `Stop ${destinations.length + 1}` }])
  }

  function removeDestination(index: number){
    mutation.reset()
    setDestinations(destinations.filter((_, i) => i !== index))
  }

  function clearAll(){
    mutation.reset()
    setStart(null)
    setDestinations([])
  }

  const canSubmit = start !== null && destinations.length > 0 && !mutation.isPending
  const result = mutation.data

  return (
      <div className="layout">
        <aside className="sidebar">
          <h1>Route Optimiser</h1>
          <p className="hint">
            {!start ? 'Click the map to set the start point.' : 'Click the mpa to add stops.'}
          </p>

          {boundsLoading && <p className="hint"> Loading map area...</p>}
          {boundsError && <ErrorBox error={boundsError}/>}
          {notice && <p className="error" role="status">{notice}</p>}

          <ul className="stop-list">
            {start && <li><strong>S</strong>{start.label}</li>}
            {destinations.map((d, i)=> (
                <li key={i}>
                  {d.label}
                  <button onClick={() => removeDestination(i)} aria-label={`Remove ${d.label}`}>X</button>
                </li>
            ))}
          </ul>

          <button disabled={!canSubmit} onClick={() => start && mutation.mutate({ start, destinations })}>
            {mutation.isPending ? 'Optimising...' : 'Find quickest route'}
          </button>
          <button onClick={clearAll} disabled = {!start}>Clear</button>

          {mutation.error && <ErrorBox error={mutation.error}/>}

          {result && (
              <section>
                <h2>Result</h2>
                <p>Total: {Math.round(result.totalDurationSeconds / 60)} min </p>
                <ol>
                  {result.legs.map((leg) => (
                      <li key = {leg.fromIndex}>
                        {result.stops[leg.fromIndex].label ?? leg.fromIndex} → {' '}
                        {result.stops[leg.toIndex].label ?? leg.toIndex}:{' '}
                        {Math.round(leg.durationSeconds / 60)} min
                      </li>
                  ))}
                </ol>
              </section>
          )}
        </aside>

        {bounds && (
            <MapView bounds={bounds} start={start} destinations={destinations} result={result} onMapClick={handleMapClick}/>
        )}
      </div>
  )
}

function ErrorBox({ error }: { error: Error }) {
  const message =
      error instanceof ApiRequestError && error.status === 422
        ? "Those points can't be connected by road."
        : error instanceof  ApiRequestError && error.status === 502
          ? 'The routing service is inavailable. Is OSRM running?'
          : error.message
  return <div role="alert" className="error">{message}</div>
}


