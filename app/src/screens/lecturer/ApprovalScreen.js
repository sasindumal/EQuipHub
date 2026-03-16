import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, TextInput, Alert, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { approvalAPI } from '../../lib/api';
import { Badge, EmptyState, BottomModal, InfoRow, Button } from '../../components/UI';
import { COLORS, FONT, RADIUS, SHADOWS } from '../../lib/theme';

export default function LecturerApprovalScreen() {
  const [queue, setQueue] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);
  const [comments, setComments] = useState('');
  const [actioning, setActioning] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await approvalAPI.getMyQueue();
      const d = res.data?.data?.requests || res.data?.data || res.data || [];
      setQueue(Array.isArray(d) ? d : []);
    } catch { } finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, []);

  const handleDecision = async (action) => {
    setActioning(true);
    try {
      const stage = selected.currentStage || 'LECTURERAPPROVAL';
      await approvalAPI.processDecision(selected.requestId || selected.id, stage, { action, comments });
      Alert.alert('Done', `Request ${action.toLowerCase()}d`);
      setSelected(null); setComments(''); load();
    } catch (e) { Alert.alert('Error', e.response?.data?.message || 'Failed'); }
    finally { setActioning(false); }
  };

  return (
    <View style={{ flex: 1, backgroundColor: COLORS.background }}>
      <View style={{ paddingHorizontal: 16, paddingTop: 16 }}>
        <Text style={s.title}>Approval Queue</Text>
        <Text style={s.sub}>{queue.length} request(s) pending your review</Text>
      </View>
      <FlatList
        data={queue}
        keyExtractor={item => String(item.requestId || item.id)}
        contentContainerStyle={{ paddingHorizontal: 16, paddingBottom: 40 }}
        refreshing={loading} onRefresh={load}
        renderItem={({ item }) => (
          <TouchableOpacity style={[s.row, SHADOWS.sm]} onPress={() => { setSelected(item); setComments(''); }} activeOpacity={0.7}>
            <View style={[s.reqIcon, { backgroundColor: COLORS.warning + '15' }]}>
              <Ionicons name="time-outline" size={20} color={COLORS.warning} />
            </View>
            <View style={{ flex: 1, marginLeft: 12 }}>
              <Text style={s.rowTitle}>{item.requestId || item.id}</Text>
              <Text style={s.rowSub}>{item.studentName || item.requesterName || '—'}</Text>
              <Text style={s.rowSub}>{item.purpose || '—'}</Text>
            </View>
            <Ionicons name="chevron-forward" size={18} color={COLORS.muted} />
          </TouchableOpacity>
        )}
        ListEmptyComponent={
          <EmptyState icon="checkmark-done-outline" title="All clear!" message="No pending approvals" />
        }
      />

      <BottomModal visible={!!selected} onClose={() => setSelected(null)} title="Review Request">
        {selected && (
          <View>
            <InfoRow label="Request ID" value={selected.requestId || selected.id} />
            <InfoRow label="Student" value={selected.studentName || selected.requesterName} />
            <InfoRow label="Purpose" value={selected.purpose} />
            <InfoRow label="Type" value={selected.requestType} />
            <InfoRow label="Stage" value={selected.currentStage} />
            <InfoRow label="From" value={selected.fromDateTime ? new Date(selected.fromDateTime).toLocaleDateString() : selected.borrowDate ? new Date(selected.borrowDate).toLocaleDateString() : '—'} />
            <InfoRow label="To" value={selected.toDateTime ? new Date(selected.toDateTime).toLocaleDateString() : selected.returnDate ? new Date(selected.returnDate).toLocaleDateString() : '—'} />

            <Text style={{ fontSize: FONT.sm, fontWeight: '600', color: COLORS.textSecondary, marginTop: 14, marginBottom: 6 }}>Comments</Text>
            <TextInput
              style={s.textarea}
              multiline placeholder="Add your comments (optional)…"
              placeholderTextColor={COLORS.muted}
              value={comments} onChangeText={setComments} />

            <View style={{ flexDirection: 'row', gap: 10, marginTop: 16 }}>
              <Button title="Approve" icon="checkmark-circle-outline"
                style={{ flex: 1 }} loading={actioning}
                onPress={() => handleDecision('APPROVE')} />
              <Button title="Reject" variant="danger" icon="close-circle-outline"
                style={{ flex: 1 }} loading={actioning}
                onPress={() => handleDecision('REJECT')} />
            </View>
          </View>
        )}
      </BottomModal>
    </View>
  );
}

const s = StyleSheet.create({
  title: { fontSize: FONT.xxl, fontWeight: '800', color: COLORS.text },
  sub: { fontSize: FONT.sm, color: COLORS.textSecondary, marginBottom: 16 },
  row: {
    backgroundColor: COLORS.white, borderRadius: RADIUS.lg,
    padding: 16, marginBottom: 10, borderWidth: 1, borderColor: COLORS.cardBorder,
    flexDirection: 'row', alignItems: 'center',
  },
  reqIcon: { width: 40, height: 40, borderRadius: RADIUS.md, justifyContent: 'center', alignItems: 'center' },
  rowTitle: { fontSize: FONT.sm, fontWeight: '700', color: COLORS.text },
  rowSub: { fontSize: FONT.xs, color: COLORS.textSecondary, marginTop: 1 },
  textarea: {
    backgroundColor: COLORS.inputBg, borderRadius: RADIUS.md,
    borderWidth: 1, borderColor: COLORS.border,
    padding: 14, fontSize: FONT.base, color: COLORS.text,
    minHeight: 80, textAlignVertical: 'top',
  },
});
