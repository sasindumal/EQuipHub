import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, FlatList, StyleSheet } from 'react-native';
import { penaltyAPI } from '../../lib/api';
import { Screen, StatCard, Badge, EmptyState, Card, InfoRow, BottomModal, Button } from '../../components/UI';
import { COLORS, FONT, RADIUS, SHADOWS } from '../../lib/theme';
import { TouchableOpacity, TextInput, Alert } from 'react-native';

export default function PenaltiesScreen() {
  const [penalties, setPenalties] = useState([]);
  const [summary, setSummary]     = useState(null);
  const [loading, setLoading]     = useState(true);
  const [selected, setSelected]   = useState(null);
  const [appealModal, setAppealModal] = useState(null);
  const [appealReason, setAppealReason] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [pRes, sRes] = await Promise.allSettled([penaltyAPI.getMyPenalties(), penaltyAPI.getMySummary()]);
      const pens = pRes.status === 'fulfilled' ? (pRes.value.data?.data?.penalties || pRes.value.data?.data || []) : [];
      const sum  = sRes.status === 'fulfilled' ? (sRes.value.data?.data || sRes.value.data) : null;
      setPenalties(Array.isArray(pens) ? pens : []);
      setSummary(sum);
    } catch { }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, []);

  const handleAppeal = async () => {
    if (!appealReason.trim()) { Alert.alert('Error', 'Please provide a reason'); return; }
    setSubmitting(true);
    try {
      await penaltyAPI.submitAppeal({ penaltyId: appealModal.penaltyId || appealModal.id, reason: appealReason });
      Alert.alert('Success', 'Appeal submitted');
      setAppealModal(null); setAppealReason(''); load();
    } catch (e) { Alert.alert('Error', e.response?.data?.message || 'Failed'); }
    finally { setSubmitting(false); }
  };

  const activeCount = penalties.filter(p => p.status === 'ACTIVE' || p.status === 'PENDING').length;
  const totalPoints = summary?.totalPoints ?? penalties.reduce((s, p) => s + (p.points || 0), 0);

  const renderItem = ({ item }) => (
    <TouchableOpacity style={[styles.row, SHADOWS.sm]} onPress={() => setSelected(item)} activeOpacity={0.7}>
      <View style={{ flex: 1 }}>
        <Text style={styles.penType}>{item.penaltyType || item.type || '—'}</Text>
        <Text style={styles.penReason} numberOfLines={1}>{item.reason || '—'}</Text>
        <Text style={styles.penDate}>{item.createdAt ? new Date(item.createdAt).toLocaleDateString() : '—'}</Text>
      </View>
      <View style={{ alignItems: 'flex-end' }}>
        <Text style={styles.penPoints}>{item.points || 0} pts</Text>
        <Badge status={item.status} style={{ marginTop: 4 }} />
      </View>
    </TouchableOpacity>
  );

  return (
    <View style={{ flex: 1, backgroundColor: COLORS.background }}>
      <View style={{ padding: 16, paddingBottom: 8 }}>
        <View style={{ flexDirection: 'row', gap: 10 }}>
          <View style={{ flex: 1 }}><StatCard label="Active Penalties" value={activeCount} icon="alert-circle-outline" color={activeCount > 0 ? COLORS.danger : COLORS.success} /></View>
          <View style={{ flex: 1 }}><StatCard label="Total Points" value={totalPoints} icon="analytics-outline" color={COLORS.primary} /></View>
        </View>
      </View>
      <FlatList
        data={penalties}
        keyExtractor={item => String(item.penaltyId || item.id)}
        renderItem={renderItem}
        contentContainerStyle={{ paddingHorizontal: 16, paddingBottom: 40, paddingTop: 8 }}
        refreshing={loading}
        onRefresh={load}
        ListEmptyComponent={<EmptyState icon="checkmark-circle-outline" title="No penalties" message="You have a clean record!" />}
      />

      {/* Detail Modal */}
      <BottomModal visible={!!selected} onClose={() => setSelected(null)} title="Penalty Details">
        {selected && (
          <View>
            <InfoRow label="Type" value={selected.penaltyType || selected.type} />
            <InfoRow label="Points" value={selected.points} />
            <InfoRow label="Status" value={selected.status} />
            <InfoRow label="Reason" value={selected.reason} />
            <InfoRow label="Date" value={selected.createdAt ? new Date(selected.createdAt).toLocaleString() : '—'} />
            {selected.status === 'ACTIVE' && (
              <Button title="Submit Appeal" icon="chatbubble-outline" variant="outline"
                onPress={() => { setSelected(null); setAppealModal(selected); }} style={{ marginTop: 16 }} />
            )}
          </View>
        )}
      </BottomModal>

      {/* Appeal Modal */}
      <BottomModal visible={!!appealModal} onClose={() => setAppealModal(null)} title="Submit Appeal">
        <Text style={{ fontSize: FONT.sm, color: COLORS.textSecondary, marginBottom: 12 }}>
          Explain why this penalty should be reviewed.
        </Text>
        <TextInput
          style={styles.textarea}
          multiline numberOfLines={4}
          placeholder="Your appeal reason…"
          placeholderTextColor={COLORS.muted}
          value={appealReason}
          onChangeText={setAppealReason}
        />
        <Button title="Submit Appeal" loading={submitting} onPress={handleAppeal} style={{ marginTop: 12 }} />
      </BottomModal>
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    backgroundColor: COLORS.white, borderRadius: RADIUS.lg,
    padding: 16, marginBottom: 10, borderWidth: 1, borderColor: COLORS.cardBorder,
    flexDirection: 'row', alignItems: 'center',
  },
  penType: { fontSize: FONT.sm, fontWeight: '700', color: COLORS.text },
  penReason: { fontSize: FONT.xs, color: COLORS.textSecondary, marginTop: 2 },
  penDate: { fontSize: FONT.xs, color: COLORS.muted, marginTop: 4 },
  penPoints: { fontSize: FONT.lg, fontWeight: '800', color: COLORS.danger },
  textarea: {
    backgroundColor: COLORS.inputBg, borderRadius: RADIUS.md,
    borderWidth: 1, borderColor: COLORS.border,
    padding: 14, fontSize: FONT.base, color: COLORS.text,
    minHeight: 100, textAlignVertical: 'top',
  },
});
