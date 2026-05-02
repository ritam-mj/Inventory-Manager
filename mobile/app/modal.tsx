import { StatusBar } from 'expo-status-bar';
import { Platform, StyleSheet } from 'react-native';

import { Text, View } from '@/components/Themed';

export default function ModalScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Inventory Manager</Text>
      <Text style={styles.body}>
        Companion app for the Spring Boot inventory API. Configure the API base URL on the Home tab.
        Source lives under <Text style={styles.mono}>mobile/</Text> in the repo.
      </Text>

      <StatusBar style={Platform.OS === 'ios' ? 'light' : 'auto'} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'stretch',
    justifyContent: 'center',
    paddingHorizontal: 24,
  },
  title: {
    fontSize: 22,
    fontWeight: 'bold',
    marginBottom: 12,
    textAlign: 'center',
  },
  body: {
    fontSize: 16,
    lineHeight: 22,
    textAlign: 'center',
    opacity: 0.85,
  },
  mono: {
    fontFamily: 'monospace',
    fontSize: 14,
  },
});
