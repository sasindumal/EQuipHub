import React, { useState } from 'react';
import { View, Text, TextInput, StyleSheet, Alert, ScrollView, KeyboardAvoidingView, Platform } from 'react-native';
import { requestAPI } from '../../lib/api';
import { Button } from '../../components/UI';
import { COLORS, RADIUS, FONT, SHADOWS } from '../../lib/theme';

export default function NewRequestScreen({ navigation }) {
  const [form, setForm] = useState({
    requestType: 'NORMAL',
    purpose: '',
    fromDateTime: '',
    toDateTime: '',
    equipmentName: '',
    quantity: '1',
  });
  const [loading, setLoading] = useState(false);

  const set = (k, v) => setForm(p => ({ ...p, [k]: v }));

  const handleSubmit = async () => {
    if (!form.purpose.trim()) { Alert.alert('Error', 'Purpose is required'); return; }
    if (!form.fromDateTime.trim() || !form.toDateTime.trim()) { Alert.alert('Error', 'Please set borrow and return dates (YYYY-MM-DD)'); return; }
    setLoading(true);
    try {
      const fromDT = `${form.fromDateTime}T09:00:00`;
      const toDT   = `${form.toDateTime}T17:00:00`;
      const payload = {
        requestType: form.requestType,
        purpose: form.purpose,
        fromDateTime: fromDT,
        toDateTime: toDT,
        items: form.equipmentName ? [{ equipmentName: form.equipmentName, quantity: parseInt(form.quantity) || 1 }] : [],
      };
      const res = await requestAPI.createRequest(payload);
      const reqId = res.data?.data?.requestId || res.data?.requestId;
      if (reqId) {
        try { await requestAPI.submitRequest(reqId); } catch { }
      }
      Alert.alert('Success', 'Request submitted successfully!', [
        { text: 'OK', onPress: () => navigation.goBack() },
      ]);
    } catch (e) {
      Alert.alert('Error', e.response?.data?.message || 'Failed to create request');
    } finally { setLoading(false); }
  };

  return (
    <KeyboardAvoidingView style={{ flex: 1, backgroundColor: COLORS.background }} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
        <View style={[styles.card, SHADOWS.sm]}>
          <Text style={styles.sectionTitle}>Request Details</Text>

          <Text style={styles.label}>Purpose *</Text>
          <TextInput style={[styles.input, { minHeight: 80, textAlignVertical: 'top' }]}
            multiline placeholder="Why do you need this equipment?" placeholderTextColor={COLORS.muted}
            value={form.purpose} onChangeText={v => set('purpose', v)} />

          <Text style={styles.label}>Equipment Name</Text>
          <TextInput style={styles.input} placeholder="e.g. Arduino Uno, Oscilloscope" placeholderTextColor={COLORS.muted}
            value={form.equipmentName} onChangeText={v => set('equipmentName', v)} />

          <View style={{ flexDirection: 'row', gap: 12 }}>
            <View style={{ flex: 1 }}>
              <Text style={styles.label}>Quantity</Text>
              <TextInput style={styles.input} keyboardType="number-pad" placeholder="1"
                placeholderTextColor={COLORS.muted} value={form.quantity} onChangeText={v => set('quantity', v)} />
            </View>
            <View style={{ flex: 1 }}>
              <Text style={styles.label}>Type</Text>
              <View style={[styles.input, { justifyContent: 'center' }]}>
                <Text style={{ color: COLORS.text, fontSize: FONT.base }}>{form.requestType}</Text>
              </View>
            </View>
          </View>

          <Text style={styles.sectionTitle}>Schedule</Text>

          <Text style={styles.label}>Borrow Date * (YYYY-MM-DD)</Text>
          <TextInput style={styles.input} placeholder="2026-03-20" placeholderTextColor={COLORS.muted}
            value={form.fromDateTime} onChangeText={v => set('fromDateTime', v)} />

          <Text style={styles.label}>Return Date * (YYYY-MM-DD)</Text>
          <TextInput style={styles.input} placeholder="2026-03-25" placeholderTextColor={COLORS.muted}
            value={form.toDateTime} onChangeText={v => set('toDateTime', v)} />

          <Button title="Submit Request" loading={loading} onPress={handleSubmit} icon="checkmark-circle-outline" style={{ marginTop: 16 }} />
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  content: { padding: 16, paddingBottom: 40 },
  card: { backgroundColor: COLORS.white, borderRadius: RADIUS.xl, padding: 20, borderWidth: 1, borderColor: COLORS.cardBorder },
  sectionTitle: { fontSize: FONT.lg, fontWeight: '700', color: COLORS.text, marginTop: 8, marginBottom: 14 },
  label: { fontSize: FONT.sm, fontWeight: '600', color: COLORS.textSecondary, marginBottom: 6 },
  input: {
    backgroundColor: COLORS.inputBg, borderRadius: RADIUS.md,
    borderWidth: 1, borderColor: COLORS.border,
    paddingHorizontal: 14, paddingVertical: 12, fontSize: FONT.base, color: COLORS.text,
    marginBottom: 14,
  },
});
