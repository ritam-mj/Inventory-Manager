import { PrimaryButton, LabeledInput } from '@/components/FormControls';
import { Text } from '@/components/Themed';
import { ApiError, apiPost } from '@/lib/api';
import { useState } from 'react';
import { ScrollView, StyleSheet, useWindowDimensions } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

export default function ReserveScreen() {
  const { width } = useWindowDimensions();
  const pad = width > 480 ? 24 : 16;

  const [skuId, setSkuId] = useState('');
  const [quantity, setQuantity] = useState('1');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const submit = async () => {
    const id = Number(skuId);
    const qty = Number(quantity);
    if (!Number.isFinite(id) || id <= 0) {
      setError('Enter a valid SKU id.');
      setMessage(null);
      return;
    }
    if (!Number.isFinite(qty) || qty < 1 || !Number.isInteger(qty)) {
      setError('Quantity must be a whole number ≥ 1.');
      setMessage(null);
      return;
    }

    setLoading(true);
    setError(null);
    setMessage(null);
    try {
      await apiPost('/api/inventory/reserve', { skuId: id, quantity: qty });
      setMessage(`Reserved ${qty} unit(s) for SKU ${id}.`);
    } catch (e) {
      setMessage(null);
      setError(e instanceof ApiError ? e.message : 'Reservation failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={{ flex: 1 }} edges={['bottom']}>
      <ScrollView contentContainerStyle={[styles.scroll, { paddingHorizontal: pad }]}>
        <Text style={styles.title}>Reserve stock</Text>
        <Text style={styles.sub}>Creates a reservation against central inventory (not tied to an order line).</Text>

        <LabeledInput
          label="SKU id"
          keyboardType="number-pad"
          value={skuId}
          onChangeText={setSkuId}
          placeholder="e.g. 1"
        />
        <LabeledInput
          label="Quantity"
          keyboardType="number-pad"
          value={quantity}
          onChangeText={setQuantity}
          placeholder="1"
        />
        <PrimaryButton title="Reserve" onPress={() => void submit()} loading={loading} />

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
    marginBottom: 6,
  },
  sub: {
    opacity: 0.7,
    marginBottom: 18,
    lineHeight: 20,
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
