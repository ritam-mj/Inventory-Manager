import { PrimaryButton, LabeledInput } from '@/components/FormControls';
import { Text, View } from '@/components/Themed';
import { ApiError, apiGet, type InventoryPayload } from '@/lib/api';
import { useState } from 'react';
import { ScrollView, StyleSheet, useWindowDimensions } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

export default function InventoryScreen() {
  const { width } = useWindowDimensions();
  const pad = width > 480 ? 24 : 16;

  const [skuId, setSkuId] = useState('');
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<InventoryPayload | null>(null);
  const [error, setError] = useState<string | null>(null);

  const fetchSku = async () => {
    const id = Number(skuId);
    if (!Number.isFinite(id) || id <= 0) {
      setError('Enter a valid SKU id (positive number).');
      setData(null);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const inv = await apiGet<InventoryPayload>(`/api/inventory/sku/${id}`);
      setData(inv);
    } catch (e) {
      setData(null);
      setError(e instanceof ApiError ? e.message : 'Request failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={{ flex: 1 }} edges={['bottom']}>
      <ScrollView contentContainerStyle={[styles.scroll, { paddingHorizontal: pad }]}>
        <Text style={styles.title}>Inventory</Text>
        <Text style={styles.sub}>Look up stock levels by internal SKU id.</Text>

        <LabeledInput
          label="SKU id"
          keyboardType="number-pad"
          value={skuId}
          onChangeText={setSkuId}
          placeholder="e.g. 1"
        />
        <PrimaryButton title="Fetch inventory" onPress={() => void fetchSku()} loading={loading} />

        {data ? (
          <View style={styles.card}>
            <Text style={styles.cardTitle}>SKU {data.skuId}</Text>
            <Row label="Available" value={String(data.availableQty)} />
            <Row label="Reserved" value={String(data.reservedQty)} />
            <Row label="Safety stock" value={String(data.safetyStock)} />
            <Row label="Sellable" value={String(data.sellableQty)} accent />
          </View>
        ) : null}

        {error ? <Text style={styles.err}>{error}</Text> : null}
      </ScrollView>
    </SafeAreaView>
  );
}

function Row({ label, value, accent }: { label: string; value: string; accent?: boolean }) {
  return (
    <View style={styles.row}>
      <Text style={[styles.rowLabel, accent && styles.accent]}>{label}</Text>
      <Text style={[styles.rowValue, accent && styles.accent]}>{value}</Text>
    </View>
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
    marginBottom: 6,
  },
  sub: {
    opacity: 0.7,
    marginBottom: 18,
  },
  card: {
    marginTop: 20,
    padding: 16,
    borderRadius: 12,
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: '#ccc',
    gap: 8,
  },
  cardTitle: {
    fontSize: 17,
    fontWeight: '600',
    marginBottom: 4,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  rowLabel: {
    fontSize: 15,
    opacity: 0.85,
  },
  rowValue: {
    fontSize: 15,
    fontVariant: ['tabular-nums'],
  },
  accent: {
    fontWeight: '700',
  },
  err: {
    marginTop: 16,
    color: '#dc2626',
    fontWeight: '500',
  },
});
