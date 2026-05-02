import { getApiBaseUrl } from '@/lib/apiBaseUrl';

export class ApiError extends Error {
  readonly status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

async function parseErrorMessage(res: Response): Promise<string> {
  const text = await res.text();
  if (!text) {
    return res.statusText || `HTTP ${res.status}`;
  }
  try {
    const json = JSON.parse(text) as { error?: string; message?: string };
    return json.error ?? json.message ?? text;
  } catch {
    return text;
  }
}

export async function apiGet<T>(path: string): Promise<T> {
  const base = await getApiBaseUrl();
  const res = await fetch(`${base}${path}`);
  if (!res.ok) {
    throw new ApiError(await parseErrorMessage(res), res.status);
  }
  return (await res.json()) as T;
}

export async function apiPost(path: string, body?: unknown): Promise<void> {
  const base = await getApiBaseUrl();
  const res = await fetch(`${base}${path}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (!res.ok) {
    throw new ApiError(await parseErrorMessage(res), res.status);
  }
}

export async function apiPostJson<T>(path: string, body?: unknown): Promise<T> {
  const base = await getApiBaseUrl();
  const res = await fetch(`${base}${path}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (!res.ok) {
    throw new ApiError(await parseErrorMessage(res), res.status);
  }
  const text = await res.text();
  if (!text) {
    return undefined as T;
  }
  return JSON.parse(text) as T;
}

export type InventoryPayload = {
  skuId: number;
  availableQty: number;
  reservedQty: number;
  safetyStock: number;
  sellableQty: number;
};

export type OrderCreatePayload = {
  externalOrderId: string;
  channelId: number;
  totalAmount: number;
  items: { skuId: number; quantity: number; price: number }[];
};

export type OrderCreatedPayload = {
  id: number;
  externalOrderId: string;
  channelId: number;
  status: string;
  totalAmount: number;
};

export type HealthPayload = { status?: string };

export type ChannelStatusPayload = {
  channelId: number;
  channelName: string;
  adapterRegistered: boolean;
  enabled: boolean;
  configured: boolean;
};

export type ChannelPollResultPayload = {
  channelId: number | null;
  channelName: string;
  fetchedOrders: number;
  status: 'SUCCESS' | 'FAILED' | 'NO_ADAPTER' | 'NOT_FOUND' | string;
  message: string;
};
