import { PrimaryButton, LabeledInput } from '@/components/FormControls';
import { Text, View } from '@/components/Themed';
import { ApiError, apiPostJson, type OrderCreatedPayload, type OrderCreatePayload } from '@/lib/api';
import { useState } from 'react';
import { Pressable, ScrollView, StyleSheet, useWindowDimensions } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

type LineForm = { skuId: string; quantity: string; price: string };

const emptyLine = (): LineForm => ({ skuId: '', quantity: '1', price: '0' });

export default function OrdersScreen() {
  const { width } = useWindowDimensions();
  const pad = width > 480 ? 24 : 16;

  const [externalOrderId, setExternalOrderId] = useState('');
  const [channelId, setChannelId] = useState('1');
  const [totalAmount, setTotalAmount] = useState('0');
  const [lines, setLines] = useState<LineForm[]>([emptyLine()]);
  const [loading, setLoading] = useState(false);
  const [created, setCreated] = useState<OrderCreatedPayload | null>(null);
  const [error, setError] = useState<string | null>(null);

  const updateLine = (index: number, patch: Partial<LineForm>) => {
    setLines((prev) => prev.map((row, i) => (i === index ? { ...row, ...patch } : row)));
  };

  const addLine = () => setLines((prev) => [...prev, emptyLine()]);
  const removeLine = (index: number) =>
    setLines((prev) => (prev.length <= 1 ? prev : prev.filter((_, i) => i !== index)));

  const submit = async () => {
    const ch = Number(channelId);
    const total = Number(totalAmount);
    if (!externalOrderId.trim()) {
      setError('External order id is required.');
      setCreated(null);
      return;
    }
    if (!Number.isFinite(ch) || ch <= 0) {
      setError('Channel id must be a positive number.');
      setCreated(null);
      return;
    }
    if (!Number.isFinite(total) || total < 0) {
      setError('Total amount must be a number ≥ 0.');
      setCreated(null);
      return;
    }

    const items: OrderCreatePayload['items'] = [];
    for (const row of lines) {
      const sku = Number(row.skuId);
      const qty = Number(row.quantity);
      const price = Number(row.price);
      if (!Number.isFinite(sku) || sku <= 0) {
        setError('Each line needs a valid SKU id.');
        setCreated(null);
        return;
      }
      if (!Number.isFinite(qty) || qty < 1 || !Number.isInteger(qty)) {
        setError('Each line needs quantity ≥ 1 (whole number).');
        setCreated(null);
        return;
      }
      if (!Number.isFinite(price) || price < 0) {
        setError('Each line needs price ≥ 0.');
        setCreated(null);
        return;
      }
      items.push({ skuId: sku, quantity: qty, price });
    }

    const body: OrderCreatePayload = {
      externalOrderId: externalOrderId.trim(),
      channelId: ch,
      totalAmount: total,
      items,
    };

    setLoading(true);
    setError(null);
    setCreated(null);
    try {
      const order = await apiPostJson<OrderCreatedPayload>('/api/orders', body);
      setCreated(order);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Could not create order');
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={{ flex: 1 }} edges={['bottom']}>
      <ScrollView contentContainerStyle={[styles.scroll, { paddingHorizontal: pad }]}>
        <Text style={styles.title}>New order</Text>
        <Text style={styles.sub}>POST /api/orders — reserves stock per line. Duplicate external id + channel returns existing.</Text>

        <LabeledInput
          label="External order id"
          autoCapitalize="none"
          value={externalOrderId}
          onChangeText={setExternalOrderId}
          placeholder="CHANNEL-12345"
        />
        <LabeledInput
          label="Channel id"
          keyboardType="number-pad"
          value={channelId}
          onChangeText={setChannelId}
        />
        <LabeledInput
          label="Total amount"
          keyboardType="decimal-pad"
          value={totalAmount}
          onChangeText={setTotalAmount}
        />

        <Text style={styles.linesTitle}>Line items</Text>
        {lines.map((row, index) => (
          <View key={index} style={styles.lineCard}>
            <Text style={styles.lineHeading}>Item {index + 1}</Text>
            <LabeledInput
              label="SKU id"
              keyboardType="number-pad"
              value={row.skuId}
              onChangeText={(t) => updateLine(index, { skuId: t })}
            />
            <LabeledInput
              label="Quantity"
              keyboardType="number-pad"
              value={row.quantity}
              onChangeText={(t) => updateLine(index, { quantity: t })}
            />
            <LabeledInput
              label="Price"
              keyboardType="decimal-pad"
              value={row.price}
              onChangeText={(t) => updateLine(index, { price: t })}
            />
            {lines.length > 1 ? (
              <Pressable onPress={() => removeLine(index)} style={styles.removeBtn}>
                <Text style={styles.removeText}>Remove line</Text>
              </Pressable>
            ) : null}
          </View>
        ))}
        <Pressable onPress={addLine} style={styles.addBtn}>
          <Text style={styles.addText}>+ Add line</Text>
        </Pressable>

        <PrimaryButton title="Submit order" onPress={() => void submit()} loading={loading} />

        {created ? (
          <View style={styles.result}>
            <Text style={styles.resultTitle}>Order saved</Text>
            <Text style={styles.mono}>id: {created.id}</Text>
            <Text style={styles.mono}>external: {created.externalOrderId}</Text>
            <Text style={styles.mono}>channelId: {created.channelId}</Text>
            <Text style={styles.mono}>status: {created.status}</Text>
            <Text style={styles.mono}>total: {created.totalAmount}</Text>
          </View>
        ) : null}

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
  linesTitle: {
    fontSize: 17,
    fontWeight: '600',
    marginBottom: 10,
    marginTop: 6,
  },
  lineCard: {
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: '#ccc',
    borderRadius: 12,
    padding: 12,
    marginBottom: 12,
    gap: 2,
  },
  lineHeading: {
    fontWeight: '600',
    marginBottom: 6,
  },
  removeBtn: {
    alignSelf: 'flex-start',
    marginTop: 4,
  },
  removeText: {
    color: '#dc2626',
    fontWeight: '600',
  },
  addBtn: {
    marginBottom: 16,
  },
  addText: {
    color: '#0d9488',
    fontWeight: '700',
    fontSize: 15,
  },
  result: {
    marginTop: 18,
    padding: 14,
    borderRadius: 12,
    backgroundColor: 'rgba(13,148,136,0.08)',
    gap: 4,
  },
  resultTitle: {
    fontWeight: '700',
    marginBottom: 6,
  },
  mono: {
    fontFamily: 'monospace',
    fontSize: 14,
  },
  err: {
    marginTop: 16,
    color: '#dc2626',
    fontWeight: '500',
  },
});
