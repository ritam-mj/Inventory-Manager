import AsyncStorage from '@react-native-async-storage/async-storage';
import Constants from 'expo-constants';

const STORAGE_KEY = 'inventory_api_base_url';

function stripTrailingSlash(url: string): string {
  return url.replace(/\/+$/, '');
}

function defaultFromEnv(): string | undefined {
  if (typeof process.env.EXPO_PUBLIC_API_URL === 'string' && process.env.EXPO_PUBLIC_API_URL.trim()) {
    return stripTrailingSlash(process.env.EXPO_PUBLIC_API_URL.trim());
  }
  return undefined;
}

function defaultFromExpoExtra(): string | undefined {
  const extra = Constants.expoConfig?.extra as { apiBaseUrl?: string } | undefined;
  const v = extra?.apiBaseUrl;
  if (typeof v === 'string' && v.trim()) {
    return stripTrailingSlash(v.trim());
  }
  return undefined;
}

export async function getApiBaseUrl(): Promise<string> {
  const stored = await AsyncStorage.getItem(STORAGE_KEY);
  if (stored?.trim()) {
    return stripTrailingSlash(stored.trim());
  }
  return defaultFromEnv() ?? defaultFromExpoExtra() ?? 'http://localhost:8080';
}

export async function setApiBaseUrl(url: string): Promise<void> {
  await AsyncStorage.setItem(STORAGE_KEY, stripTrailingSlash(url.trim()));
}
