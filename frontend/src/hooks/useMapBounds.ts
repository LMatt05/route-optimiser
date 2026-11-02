import { useMemo } from 'react'
import L from 'leaflet'
import { useQuery } from "@tanstack/react-query";
import {fetchMapBounds} from "../api/mapClient.ts";

export function useMapBounds() {
    const query = useQuery({
        queryKey: ['map-bounds'],
        queryFn: fetchMapBounds,
        staleTime: Infinity, //loaded map doesnt change while app is open
    })

    const bounds = useMemo(
        () =>
            query.data
                ? L.latLngBounds([query.data.south, query.data.west], [query.data.north, query.data.east])
                : null,
        [query.data],
    )

    return { bounds, isLoading: query.isPending, error: query.error }
}