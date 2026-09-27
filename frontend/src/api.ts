export type Dose = {
  id: number;
  medicineId: number;
  takenAt: string;
};

export type Medicine = {
  id: number;
  name: string;
  purchasedOn: string;
  expiresOn: string;
  intakeSchedule: string;
  homeLocation: string;
  lastTakenAt: string | null;
  doseCount: number;
  doses: Dose[];
};

export type AuthUser = {
  givenName: string;
  familyName: string;
  email: string;
};

export const backendUrl = import.meta.env.VITE_BACKEND_URL ?? "http://localhost:8080";

export async function getCurrentUser(): Promise<AuthUser | null> {
  const response = await request("/api/me");
  if (response.status === 401) {
    return null;
  }
  if (!response.ok) {
    throw new Error(await parseError(response));
  }
  return response.json();
}

export function googleLoginUrl(): string {
  return `${backendUrl}/oauth2/authorization/google`;
}

export function logoutUrl(): string {
  return `${backendUrl}/logout`;
}

export type MedicinePayload = {
  name: string;
  purchasedOn: string;
  expiresOn: string;
  intakeSchedule: string;
  homeLocation: string;
};

async function request(url: string, init: RequestInit = {}): Promise<Response> {
  return fetch(url, { ...init, credentials: "include" });
}

async function parseError(response: Response): Promise<string> {
  try {
    const body = await response.json();
    return body.detail || body.title || "İstek başarısız oldu";
  } catch {
    return "İstek başarısız oldu";
  }
}

export async function listMedicines(): Promise<Medicine[]> {
  const response = await request("/api/medicines");
  if (!response.ok) {
    throw new Error(await parseError(response));
  }
  return response.json();
}

export async function createMedicine(payload: MedicinePayload): Promise<Medicine> {
  const response = await request("/api/medicines", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    throw new Error(await parseError(response));
  }
  return response.json();
}

export async function updateMedicine(id: number, payload: MedicinePayload): Promise<Medicine> {
  const response = await request(`/api/medicines/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    throw new Error(await parseError(response));
  }
  return response.json();
}

export async function deleteMedicine(id: number): Promise<void> {
  const response = await request(`/api/medicines/${id}`, { method: "DELETE" });
  if (!response.ok) {
    throw new Error(await parseError(response));
  }
}

export async function addDose(medicineId: number, takenAt?: string): Promise<Dose> {
  const response = await request(`/api/medicines/${medicineId}/doses`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(takenAt ? { takenAt } : {}),
  });
  if (!response.ok) {
    throw new Error(await parseError(response));
  }
  return response.json();
}

export async function deleteDose(id: number): Promise<void> {
  const response = await request(`/api/doses/${id}`, { method: "DELETE" });
  if (!response.ok) {
    throw new Error(await parseError(response));
  }
}
