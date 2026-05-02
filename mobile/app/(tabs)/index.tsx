import { PrimaryButton, LabeledInput } from '@/components/FormControls';
import { Text, View } from '@/components/Themed';
import {
  ApiError,
  apiGet,
  apiPost,
  apiPostJson,
  type ChannelPollResultPayload,
  type ChannelStatusPayload,
  type HealthPayload,
} from '@/lib/api';
import { getApiBaseUrl, setApiBaseUrl } from '@/lib/apiBaseUrl';
import { useCallback, useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  useWindowDimensions,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

export default function DashboardScreen() {
  const { width } = useWindowDimensions();
  const pad = width > 480 ? 24 : 16;

  const [baseUrlInput, setBaseUrlInput] = useState('');
  const [loadedUrl, setLoadedUrl] = useState<string | null>(null);

  const [health, setHealth] = useState<HealthPayload | null>(null);
  const [channels, setChannels] = useState<ChannelStatusPayload[]>([]);
  const [loadingHealth, setLoadingHealth] = useState(false);
  const [loadingChannels, setLoadingChannels] = useState(false);
  const [pollAllLoading, setPollAllLoading] = useState(false);
  const [pollingChannel, setPollingChannel] = useState<string | null>(null);
  const [reconcileLoading, setReconcileLoading] = useState(false);
  const [saveLoading, setSaveLoading] = useState(false);
  const [lastPollResults, setLastPollResults] = useState<ChannelPollResultPayload[]>([]);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const refreshBaseUrl = useCallback(async () => {
    const u = await getApiBaseUrl();
    setBaseUrlInput(u);
    setLoadedUrl(u);
  }, []);

  useEffect(() => {
    void refreshBaseUrl();
  }, [refreshBaseUrl]);

  const loadHealth = useCallback(async () => {
    setLoadingHealth(true);
    setError(null);
    try {
      const h = await apiGet<HealthPayload>('/actuator/health');
      setHealth(h);
    } catch (e) {
      setHealth(null);
      setError(e instanceof ApiError ? e.message : 'Health check failed');
    } finally {
      setLoadingHealth(false);
    }
  }, []);

  const loadChannels = useCallback(async () => {
    setLoadingChannels(true);
    setError(null);
    try {
      const list = await apiGet<ChannelStatusPayload[]>('/api/channels/status');
      setChannels(list);
    } catch (e) {
      setChannels([]);
      setError(e instanceof ApiError ? e.message : 'Could not load channels');
    } finally {
      setLoadingChannels(false);
    }
  }, []);

  useEffect(() => {
    if (loadedUrl) {
      void loadHealth();
      void loadChannels();
    }
  }, [loadedUrl, loadHealth, loadChannels]);

  const saveBaseUrl = async () => {
    setSaveLoading(true);
    setMessage(null);
    setError(null);
    try {
      await setApiBaseUrl(baseUrlInput);
      setLoadedUrl(await getApiBaseUrl());
      setMessage('API base URL saved.');
      await loadHealth();
      await loadChannels();
    } catch {
      setError('Could not save URL.');
    } finally {
      setSaveLoading(false);
    }
  };

  const pollAllChannels = async () => {
    setPollAllLoading(true);
    setMessage(null);
    setError(null);
    setLastPollResults([]);
    try {
      const results = await apiPostJson<ChannelPollResultPayload[]>('/api/channels/poll-orders');
      setLastPollResults(results);
      setMessage('Poll completed for all channels.');
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Poll failed');
    } finally {
      setPollAllLoading(false);
      await loadChannels();
    }
  };

  const pollSingleChannel = async (channelName: string) => {
    setPollingChannel(channelName);
    setMessage(null);
    setError(null);
    try {
      const result = await apiPostJson<ChannelPollResultPayload>(
        `/api/channels/${encodeURIComponent(channelName)}/poll-orders`
      );
      setLastPollResults((prev) => [result, ...prev.filter((x) => x.channelName !== result.channelName)]);
      setMessage(`Poll completed for ${channelName}.`);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : `Poll failed for ${channelName}`);
    } finally {
      setPollingChannel(null);
      await loadChannels();
    }
  };

  const reconcile = async () => {
    setReconcileLoading(true);
    setMessage(null);
    setError(null);
    try {
      await apiPost('/api/admin/reconcile');
      setMessage('Reconcile triggered.');
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Reconcile failed');
    } finally {
      setReconcileLoading(false);
    }
  };

  return (
    <SafeAreaView style={{ flex: 1 }} edges={['bottom']}>
      <ScrollView contentContainerStyle={[styles.scroll, { paddingHorizontal: pad }]}>
        <Text style={styles.title}>Dashboard</Text>
        <Text style={styles.hint}>
          Physical Android device: use your computer&apos;s LAN IP (e.g. http://192.168.1.10:8080).
          Android emulator: http://10.0.2.2:8080
        </Text>

        <LabeledInput
          label="API base URL"
          autoCapitalize="none"
          autoCorrect={false}
          keyboardType="url"
          value={baseUrlInput}
          onChangeText={setBaseUrlInput}
          placeholder="http://localhost:8080"
        />
        <PrimaryButton title="Save URL" onPress={() => void saveBaseUrl()} loading={saveLoading} />

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Backend status</Text>
          <View style={styles.row}>
            {loadingHealth ? (
              <ActivityIndicator />
            ) : (
              <Text style={styles.mono}>{health?.status ?? 'unknown'}</Text>
            )}
            <PrimaryButton title="Refresh health" onPress={() => void loadHealth()} disabled={loadingHealth} />
          </View>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Channels</Text>
          {loadingChannels ? (
            <ActivityIndicator />
          ) : (
            <View style={{ gap: 10 }}>
              {channels.map((item) => (
                <View key={item.channelName} style={styles.channelCard}>
                  <Text style={styles.channelTitle}>{item.channelName}</Text>
                  <Text style={styles.channelMeta}>
                    adapter: {item.adapterRegistered ? 'yes' : 'no'} | enabled:{' '}
                    {item.enabled ? 'yes' : 'no'} | configured: {item.configured ? 'yes' : 'no'}
                  </Text>
                  <Pressable
                    onPress={() => void pollSingleChannel(item.channelName)}
                    disabled={pollingChannel === item.channelName}
                    style={({ pressed }) => [
                      styles.inlineBtn,
                      { opacity: pollingChannel === item.channelName ? 0.5 : pressed ? 0.85 : 1 },
                    ]}>
                    <Text style={styles.inlineBtnText}>
                      {pollingChannel === item.channelName ? 'Polling...' : 'Poll this channel'}
                    </Text>
                  </Pressable>
                </View>
              ))}
              {channels.length === 0 ? <Text style={styles.muted}>No channel rows found in database.</Text> : null}
            </View>
          )}
          <PrimaryButton title="Reload channels" onPress={() => void loadChannels()} disabled={loadingChannels} />
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Sync actions</Text>
          <PrimaryButton
            title="Poll all channels"
            onPress={() => void pollAllChannels()}
            loading={pollAllLoading}
          />
          <View style={{ height: 10 }} />
          <PrimaryButton title="Run reconcile" onPress={() => void reconcile()} loading={reconcileLoading} />
        </View>

        {lastPollResults.length > 0 ? (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Last poll results</Text>
            {lastPollResults.map((r) => (
              <View key={r.channelName} style={styles.resultCard}>
                <Text style={styles.resultTitle}>
                  {r.channelName} - {r.status}
                </Text>
                <Text style={styles.resultText}>fetched orders: {r.fetchedOrders}</Text>
                <Text style={styles.resultText}>{r.message}</Text>
              </View>
            ))}
          </View>
        ) : null}

        {message ? <Text style={styles.success}>{message}</Text> : null}
        {error ? <Text style={styles.err}>{error}</Text> : null}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  scroll: {
    paddingTop: 16,
    paddingBottom: 32,
  },
  title: {
    fontSize: 24,
    fontWeight: '700',
    marginBottom: 8,
  },
  hint: {
    fontSize: 13,
    opacity: 0.7,
    marginBottom: 18,
    lineHeight: 18,
  },
  section: {
    marginTop: 22,
    gap: 10,
  },
  sectionTitle: {
    fontSize: 17,
    fontWeight: '600',
  },
  row: {
    gap: 12,
  },
  mono: {
    fontFamily: 'monospace',
    fontSize: 16,
  },
  channelCard: {
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: '#ccc',
    borderRadius: 12,
    padding: 12,
    gap: 6,
  },
  channelTitle: {
    fontSize: 16,
    fontWeight: '700',
    textTransform: 'capitalize',
  },
  channelMeta: {
    fontSize: 13,
    opacity: 0.8,
  },
  inlineBtn: {
    alignSelf: 'flex-start',
    backgroundColor: '#0d9488',
    borderRadius: 8,
    paddingHorizontal: 10,
    paddingVertical: 8,
  },
  inlineBtnText: {
    color: '#fff',
    fontWeight: '600',
    fontSize: 13,
  },
  muted: {
    opacity: 0.6,
    marginBottom: 8,
  },
  resultCard: {
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: '#ddd',
    borderRadius: 10,
    padding: 10,
    gap: 2,
  },
  resultTitle: {
    fontWeight: '700',
    textTransform: 'capitalize',
  },
  resultText: {
    fontSize: 13,
    opacity: 0.85,
  },
  success: {
    marginTop: 16,
    color: '#059669',
    fontWeight: '500',
  },
  err: {
    marginTop: 16,
    color: '#dc2626',
    fontWeight: '500',
  },
});
