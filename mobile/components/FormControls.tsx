import Colors from '@/constants/Colors';
import { useColorScheme } from '@/components/useColorScheme';
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  TextInputProps,
  View,
} from 'react-native';
import { Text as ThemedText } from '@/components/Themed';

type LabeledInputProps = TextInputProps & {
  label: string;
};

export function LabeledInput({ label, style, ...props }: LabeledInputProps) {
  const scheme = useColorScheme() ?? 'light';
  const colors = Colors[scheme];

  return (
    <View style={styles.field}>
      <ThemedText style={styles.label}>{label}</ThemedText>
      <TextInput
        placeholderTextColor={scheme === 'dark' ? '#888' : '#999'}
        style={[
          styles.input,
          {
            color: colors.text,
            borderColor: scheme === 'dark' ? '#444' : '#ccc',
            backgroundColor: scheme === 'dark' ? '#111' : '#fafafa',
          },
          style,
        ]}
        {...props}
      />
    </View>
  );
}

type PrimaryButtonProps = {
  title: string;
  onPress: () => void;
  disabled?: boolean;
  loading?: boolean;
};

export function PrimaryButton({ title, onPress, disabled, loading }: PrimaryButtonProps) {
  const scheme = useColorScheme() ?? 'light';
  const tint = Colors[scheme].tint;
  const inactive = disabled || loading;

  return (
    <Pressable
      onPress={onPress}
      disabled={inactive}
      style={({ pressed }) => [
        styles.button,
        { backgroundColor: tint, opacity: inactive ? 0.5 : pressed ? 0.85 : 1 },
      ]}>
      {loading ? (
        <ActivityIndicator color="#fff" />
      ) : (
        <Text style={styles.buttonText}>{title}</Text>
      )}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  field: {
    marginBottom: 14,
  },
  label: {
    fontSize: 13,
    fontWeight: '600',
    marginBottom: 6,
    opacity: 0.85,
  },
  input: {
    borderWidth: 1,
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontSize: 16,
  },
  button: {
    borderRadius: 10,
    paddingVertical: 14,
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: 48,
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
});
