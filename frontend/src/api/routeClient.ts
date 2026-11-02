import type { OptimiseRequest, OptimiseResponse} from '../types/route'

export class ApiRequestError extends Error{
    status: number
    details: string[]

    constructor(status: number, message: string, details: string[]=[]) {
        super(message);
        this.status = status
        this.details = details
    }
}

export async function optimiseRoute(req: OptimiseRequest): Promise<OptimiseResponse>{
    const res = await fetch('/api/v1/routes/optimise', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(req),
    })

    if(!res.ok){
        let body: { message?: string; details?: string[] } = {}
        try{
            body = await res.json()
        } catch{
            /* non-JSON error body */
        }
        throw new ApiRequestError(res.status, body.message ?? res.statusText, body.details ?? [])
    }
    return res.json()
}