import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, TextInput, Alert, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { requestAPI, inspectionAPI, equipmentAPI } from '../../lib/api';
import { useAuth } from '../../context/AuthContext';
import { Screen, StatCard, Card, Badge, EmptyState, SearchBar, BottomModal, InfoRow, Button } from '../../components/UI';
import { COLORS, FONT, RADIUS, SHADOWS } from '../../lib/theme';

// ─── Dashboard ────────────────────────────────────────────
export function TODashboard({ navigation }) {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({ pending: 0, inspections: 0, unack: 0 });

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [rR, iR, uR] = await Promise.allSettled([
        requestAPI.getDepartmentRequests(),
        inspectionAPI.getMyInspections(),
        inspectionAPI.getUnacknowledged(),
      ]);
      const reqs = rR.status === 'fulfilled' ? (rR.value.data?.data?.requests || rR.value.data?.requests || []) : [];
      const insps = iR.status === 'fulfilled' ? (iR.value.data?.data?.inspections || iR.value.data?.data || []) : [];
      const unack = uR.status === 'fulfilled' ? (uR.value.data?.data?.inspections || uR.value.data?.data || []) : [];
      setStats({
        pending: Array.isArray(reqs) ? reqs.filter(r => r.status === 'APPROVED').length : 0,
        inspections: Array.isArray(insps) ? insps.length : 0,
        unack: Array.isArray(unack) ? unack.length : 0,
      });
    } catch { } finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, []);

  return (
    <Screen refreshing={loading} onRefresh={load}>
      <Text style={s.title}>Technical Officer</Text>
      <Text style={s.sub}>Equipment operations overview</Text>
      <View style={s.grid}>
        <View style={s.half}><StatCard label="Ready to Issue" value={stats.pending} icon="swap-horizontal-outline" color={COLORS.primary} /></View>
        <View style={s.half}><StatCard label="Total Inspections" value={stats.inspections} icon="clipboard-outline" color={COLORS.success} /></View>
      </View>
      {stats.unack > 0 && (
        <Card>
          <View style={{ flexDirection: 'row', alignItems: 'center', gap: 10 }}>
            <Ionicons name="alert-circle-outline" size={24} color={COLORS.warning} />
            <Text style={{ fontSize: FONT.sm, color: COLORS.warning, fontWeight: '600', flex: 1 }}>
              {stats.unack} unacknowledged damage inspection(s)
            </Text>
          </View>
        </Card>
      )}
    </Screen>
  );
}

// ─── Issue Equipment ──────────────────────────────────────
export function TOIssue() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);
  const [issuing, setIssuing] = useState(false);
  const [condition, setCondition] = useState('GOOD');
  const [notes, setNotes] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await requestAPI.getDepartmentRequests();
      const d = res.data?.data?.requests || res.data?.requests || [];
      const approved = (Array.isArray(d) ? d : []).filter(r => r.status === 'APPROVED');
      setRequests(approved);
    } catch { } finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, []);

  const handleIssue = async () => {
    setIssuing(true);
    try {
      const items = (selected.items || []).map(i => ({
        equipmentId: i.equipmentId, conditionBefore: condition, notes,
      }));
      await inspectionAPI.issueEquipment({ requestId: selected.requestId || selected.id, items: items.length ? items : [{ equipmentId: selected.equipmentId, conditionBefore: condition, notes }] });
      Alert.alert('Success', 'Equipment issued!');
      setSelected(null); load();
    } catch (e) { Alert.alert('Error', e.response?.data?.message || 'Failed'); }
    finally { setIssuing(false); }
  };

  return (
    <View style={{ flex: 1, backgroundColor: COLORS.background }}>
      <FlatList data={requests} keyExtractor={item => String(item.requestId || item.id)}
        contentContainerStyle={{ padding: 16 }} refreshing={loading} onRefresh={load}
        renderItem={({ item }) => (
          <TouchableOpacity style={[s.row, SHADOWS.sm]} onPress={() => { setSelected(item); setNotes(''); setCondition('GOOD'); }} activeOpacity={0.7}>
            <View style={{ flex: 1 }}>
              <Text style={s.rowTitle}>{item.requestId || item.id}</Text>
              <Text style={s.rowSub}>{item.studentName || item.requesterName || '—'}</Text>
            </View>
            <Badge status="APPROVED" />
          </TouchableOpacity>
        )}
        ListEmptyComponent={<EmptyState icon="swap-horizontal-outline" title="No approved requests" message="Approved requests will appear here for issuing" />}
      />
      <BottomModal visible={!!selected} onClose={() => setSelected(null)} title="Issue Equipment">
        {selected && (
          <View>
            <InfoRow label="Request" value={selected.requestId || selected.id} />
            <InfoRow label="Student" value={selected.studentName || selected.requesterName} />
            <Text style={{ fontSize: FONT.sm, fontWeight: '600', color: COLORS.textSecondary, marginTop: 12, marginBottom: 6 }}>Condition</Text>
            <View style={{ flexDirection: 'row', gap: 8, marginBottom: 14 }}>
              {['EXCELLENT', 'GOOD', 'FAIR'].map(c => (
                <TouchableOpacity key={c} onPress={() => setCondition(c)}
                  style={[s.condBtn, condition === c && { borderColor: COLORS.primary, backgroundColor: COLORS.primary + '08' }]}>
                  <Text style={[s.condText, condition === c && { color: COLORS.primary }]}>{c}</Text>
                </TouchableOpacity>
              ))}
            </View>
            <TextInput style={s.textarea} multiline placeholder="Notes (optional)…" placeholderTextColor={COLORS.muted}
              value={notes} onChangeText={setNotes} />
            <Button title="Issue Equipment" loading={issuing} onPress={handleIssue} icon="checkmark-circle-outline" style={{ marginTop: 14 }} />
          </View>
        )}
      </BottomModal>
    </View>
  );
}

// ─── Process Return ───────────────────────────────────────
export function TOReturn() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);
  const [returning, setReturning] = useState(false);
  const [condition, setCondition] = useState('GOOD');
  const [damage, setDamage] = useState('NONE');
  const [notes, setNotes] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await requestAPI.getDepartmentRequests();
      const d = res.data?.data?.requests || res.data?.requests || [];
      const inUse = (Array.isArray(d) ? d : []).filter(r => ['INUSE', 'IN_USE'].includes(r.status));
      setRequests(inUse);
    } catch { } finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, []);

  const handleReturn = async () => {
    setReturning(true);
    try {
      const items = (selected.items || []).map(i => ({
        equipmentId: i.equipmentId, conditionAfter: condition, damageLevel: damage, notes,
      }));
      await inspectionAPI.processReturn({ requestId: selected.requestId || selected.id, items: items.length ? items : [{ equipmentId: selected.equipmentId, conditionAfter: condition, damageLevel: damage, notes }] });
      Alert.alert('Success', 'Return processed!');
      setSelected(null); load();
    } catch (e) { Alert.alert('Error', e.response?.data?.message || 'Failed'); }
    finally { setReturning(false); }
  };

  return (
    <View style={{ flex: 1, backgroundColor: COLORS.background }}>
      <FlatList data={requests} keyExtractor={item => String(item.requestId || item.id)}
        contentContainerStyle={{ padding: 16 }} refreshing={loading} onRefresh={load}
        renderItem={({ item }) => (
          <TouchableOpacity style={[s.row, SHADOWS.sm]} onPress={() => { setSelected(item); setNotes(''); setCondition('GOOD'); setDamage('NONE'); }} activeOpacity={0.7}>
            <View style={{ flex: 1 }}>
              <Text style={s.rowTitle}>{item.requestId || item.id}</Text>
              <Text style={s.rowSub}>{item.studentName || item.requesterName || '—'}</Text>
            </View>
            <Badge status="INUSE" />
          </TouchableOpacity>
        )}
        ListEmptyComponent={<EmptyState icon="arrow-down-circle-outline" title="No items to return" message="In-use equipment will appear here" />}
      />
      <BottomModal visible={!!selected} onClose={() => setSelected(null)} title="Process Return">
        {selected && (
          <View>
            <InfoRow label="Request" value={selected.requestId || selected.id} />
            <InfoRow label="Student" value={selected.studentName || selected.requesterName} />
            <Text style={{ fontSize: FONT.sm, fontWeight: '600', color: COLORS.textSecondary, marginTop: 12, marginBottom: 6 }}>Condition After</Text>
            <View style={{ flexDirection: 'row', gap: 6, flexWrap: 'wrap', marginBottom: 12 }}>
              {['EXCELLENT', 'GOOD', 'FAIR', 'POOR', 'DAMAGED'].map(c => (
                <TouchableOpacity key={c} onPress={() => setCondition(c)}
                  style={[s.condBtn, condition === c && { borderColor: COLORS.primary, backgroundColor: COLORS.primary + '08' }]}>
                  <Text style={[s.condText, condition === c && { color: COLORS.primary }]}>{c}</Text>
                </TouchableOpacity>
              ))}
            </View>
            <Text style={{ fontSize: FONT.sm, fontWeight: '600', color: COLORS.textSecondary, marginBottom: 6 }}>Damage Level</Text>
            <View style={{ flexDirection: 'row', gap: 6, flexWrap: 'wrap', marginBottom: 12 }}>
              {['NONE', 'MINOR', 'MODERATE', 'SEVERE'].map(d => (
                <TouchableOpacity key={d} onPress={() => setDamage(d)}
                  style={[s.condBtn, damage === d && { borderColor: COLORS.danger, backgroundColor: COLORS.danger + '08' }]}>
                  <Text style={[s.condText, damage === d && { color: COLORS.danger }]}>{d}</Text>
                </TouchableOpacity>
              ))}
            </View>
            <TextInput style={s.textarea} multiline placeholder="Notes (optional)…" placeholderTextColor={COLORS.muted}
              value={notes} onChangeText={setNotes} />
            <Button title="Process Return" loading={returning} onPress={handleReturn} icon="checkmark-circle-outline" style={{ marginTop: 14 }} />
          </View>
        )}
      </BottomModal>
    </View>
  );
}

// ─── Styles ───────────────────────────────────────────────
const s = StyleSheet.create({
  title: { fontSize: FONT.xxl, fontWeight: '800', color: COLORS.text },
  sub: { fontSize: FONT.sm, color: COLORS.textSecondary, marginBottom: 20 },
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10, marginBottom: 20 },
  half: { width: '48%' },
  row: {
    backgroundColor: COLORS.white, borderRadius: RADIUS.lg,
    padding: 16, marginBottom: 10, borderWidth: 1, borderColor: COLORS.cardBorder,
    flexDirection: 'row', alignItems: 'center',
  },
  rowTitle: { fontSize: FONT.sm, fontWeight: '700', color: COLORS.text },
  rowSub: { fontSize: FONT.xs, color: COLORS.textSecondary, marginTop: 2 },
  condBtn: {
    paddingVertical: 6, paddingHorizontal: 12,
    borderRadius: RADIUS.sm, borderWidth: 1, borderColor: COLORS.border,
  },
  condText: { fontSize: FONT.xs, fontWeight: '600', color: COLORS.textSecondary },
  textarea: {
    backgroundColor: COLORS.inputBg, borderRadius: RADIUS.md,
    borderWidth: 1, borderColor: COLORS.border,
    padding: 14, fontSize: FONT.base, color: COLORS.text,
    minHeight: 80, textAlignVertical: 'top',
  },
});
