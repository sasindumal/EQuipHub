import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, Alert, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { deptAdminAPI, requestAPI, approvalAPI, equipmentAPI, penaltyAPI } from '../../lib/api';
import { useAuth } from '../../context/AuthContext';
import { Screen, StatCard, Card, Badge, EmptyState, SearchBar, BottomModal, InfoRow, Button } from '../../components/UI';
import { COLORS, FONT, RADIUS, SHADOWS } from '../../lib/theme';

// ─── Dashboard ────────────────────────────────────────────
export function DeptDashboard() {
  const { user } = useAuth();
  const [dept, setDept] = useState(null);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [dR, sR] = await Promise.allSettled([deptAdminAPI.getMyDepartment(), deptAdminAPI.getMyDepartmentStats()]);
      if (dR.status === 'fulfilled') setDept(dR.value.data?.data || dR.value.data);
      if (sR.status === 'fulfilled') setStats(sR.value.data?.data || sR.value.data);
    } catch { } finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, []);

  return (
    <Screen refreshing={loading} onRefresh={load}>
      <Text style={s.title}>{dept?.name || 'Department'}</Text>
      <Text style={s.sub}>Department overview</Text>
      <View style={s.grid}>
        <View style={s.half}><StatCard label="Staff" value={stats?.staffCount ?? '—'} icon="people-outline" color={COLORS.primary} /></View>
        <View style={s.half}><StatCard label="Students" value={stats?.studentCount ?? '—'} icon="school-outline" color={COLORS.primaryLight} /></View>
        <View style={s.half}><StatCard label="Equipment" value={stats?.equipmentCount ?? '—'} icon="hardware-chip-outline" color={COLORS.success} /></View>
        <View style={s.half}><StatCard label="Requests" value={stats?.requestCount ?? '—'} icon="document-text-outline" color={COLORS.warning} /></View>
      </View>
      {dept && (
        <Card title="Department Info">
          <InfoRow label="Code" value={dept.code || dept.departmentCode} />
          <InfoRow label="Description" value={dept.description} />
          <InfoRow label="Status" value={dept.active !== false ? 'Active' : 'Inactive'} />
        </Card>
      )}
    </Screen>
  );
}

// ─── Requests ─────────────────────────────────────────────
export function DeptRequests() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);
  const [rejectNote, setRejectNote] = useState('');
  const [actioning, setActioning] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await requestAPI.getDepartmentRequests();
      const d = res.data?.data?.requests || res.data?.requests || [];
      setRequests(Array.isArray(d) ? d : []);
    } catch { } finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, []);

  const handleAction = async (reqId, stage, action, comments = '') => {
    setActioning(true);
    try {
      await approvalAPI.processDecision(reqId, stage, { action, comments });
      Alert.alert('Done', `Request ${action.toLowerCase()}d`);
      setSelected(null); load();
    } catch (e) { Alert.alert('Error', e.response?.data?.message || 'Failed'); }
    finally { setActioning(false); }
  };

  return (
    <View style={{ flex: 1, backgroundColor: COLORS.background }}>
      <FlatList
        data={requests}
        keyExtractor={item => String(item.requestId || item.id)}
        contentContainerStyle={{ padding: 16 }}
        refreshing={loading} onRefresh={load}
        renderItem={({ item }) => (
          <TouchableOpacity style={[s.row, SHADOWS.sm]} onPress={() => setSelected(item)} activeOpacity={0.7}>
            <View style={{ flex: 1 }}>
              <Text style={s.rowTitle}>{item.requestId || item.id}</Text>
              <Text style={s.rowSub}>{item.studentName || item.requesterName || '—'}</Text>
            </View>
            <Badge status={item.status} />
          </TouchableOpacity>
        )}
        ListEmptyComponent={<EmptyState icon="document-text-outline" title="No requests" />}
      />
      <BottomModal visible={!!selected} onClose={() => setSelected(null)} title="Request Details">
        {selected && (
          <View>
            <InfoRow label="ID" value={selected.requestId || selected.id} />
            <InfoRow label="Student" value={selected.studentName || selected.requesterName} />
            <InfoRow label="Purpose" value={selected.purpose || selected.reason} />
            <InfoRow label="Status" value={selected.status} />
            <InfoRow label="Submitted" value={selected.submittedAt ? new Date(selected.submittedAt).toLocaleDateString() : '—'} />
            {['PENDINGAPPROVAL', 'PENDING'].includes(selected.status) && (
              <View style={{ flexDirection: 'row', gap: 10, marginTop: 16 }}>
                <Button title="Approve" icon="checkmark-circle-outline"
                  style={{ flex: 1 }} loading={actioning}
                  onPress={() => handleAction(selected.requestId || selected.id, selected.currentStage || 'DEPTADMINAPPROVAL', 'APPROVE')} />
                <Button title="Reject" variant="danger" icon="close-circle-outline"
                  style={{ flex: 1 }} loading={actioning}
                  onPress={() => {
                    Alert.prompt?.('Rejection Reason', '', (text) => {
                      handleAction(selected.requestId || selected.id, selected.currentStage || 'DEPTADMINAPPROVAL', 'REJECT', text || '');
                    }) || handleAction(selected.requestId || selected.id, selected.currentStage || 'DEPTADMINAPPROVAL', 'REJECT', '');
                  }} />
              </View>
            )}
          </View>
        )}
      </BottomModal>
    </View>
  );
}

// ─── Equipment ────────────────────────────────────────────
export function DeptEquipment() {
  const [equipment, setEquipment] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await equipmentAPI.getAllEquipment();
      const d = res.data?.data || res.data || [];
      setEquipment(Array.isArray(d) ? d : (d.content || []));
    } catch { } finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, []);

  const filtered = equipment.filter(e =>
    !search || [e.name, e.serialNumber, e.category].some(f => f?.toLowerCase().includes(search.toLowerCase()))
  );

  return (
    <View style={{ flex: 1, backgroundColor: COLORS.background }}>
      <View style={{ padding: 16, paddingBottom: 8 }}>
        <SearchBar value={search} onChangeText={setSearch} placeholder="Search equipment…" />
      </View>
      <FlatList
        data={filtered}
        keyExtractor={item => String(item.equipmentId || item.id)}
        contentContainerStyle={{ paddingHorizontal: 16 }}
        refreshing={loading} onRefresh={load}
        renderItem={({ item }) => (
          <View style={[s.row, SHADOWS.sm]}>
            <View style={{ flex: 1 }}>
              <Text style={s.rowTitle}>{item.name || item.equipmentName}</Text>
              <Text style={s.rowSub}>{item.serialNumber || '—'} · {item.category || '—'}</Text>
            </View>
            <Badge status={item.status} />
          </View>
        )}
        ListEmptyComponent={<EmptyState icon="cube-outline" title="No equipment" />}
      />
    </View>
  );
}

// ─── Staff ────────────────────────────────────────────────
export function DeptStaff() {
  const [staff, setStaff] = useState([]);
  const [loading, setLoading] = useState(true);
  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await deptAdminAPI.getMyDepartmentStaff();
      const d = res.data?.data?.staff || res.data?.data || [];
      setStaff(Array.isArray(d) ? d : []);
    } catch { } finally { setLoading(false); }
  }, []);
  useEffect(() => { load(); }, []);

  return (
    <View style={{ flex: 1, backgroundColor: COLORS.background }}>
      <FlatList data={staff} keyExtractor={item => String(item.userId || item.id)}
        contentContainerStyle={{ padding: 16 }} refreshing={loading} onRefresh={load}
        renderItem={({ item }) => (
          <View style={[s.row, SHADOWS.sm]}>
            <View style={s.avatar}><Text style={s.avatarText}>{(item.firstName||'?')[0]}{(item.lastName||'?')[0]}</Text></View>
            <View style={{ flex: 1, marginLeft: 10 }}>
              <Text style={s.rowTitle}>{item.firstName} {item.lastName}</Text>
              <Text style={s.rowSub}>{item.email}</Text>
            </View>
            <Badge status={item.status} />
          </View>
        )}
        ListEmptyComponent={<EmptyState icon="people-outline" title="No staff" />}
      />
    </View>
  );
}

// ─── Students ─────────────────────────────────────────────
export function DeptStudents() {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await deptAdminAPI.getMyDepartmentStudents();
      const d = res.data?.data?.students || res.data?.data || [];
      setStudents(Array.isArray(d) ? d : []);
    } catch { } finally { setLoading(false); }
  }, []);
  useEffect(() => { load(); }, []);

  return (
    <View style={{ flex: 1, backgroundColor: COLORS.background }}>
      <FlatList data={students} keyExtractor={item => String(item.userId || item.id)}
        contentContainerStyle={{ padding: 16 }} refreshing={loading} onRefresh={load}
        renderItem={({ item }) => (
          <View style={[s.row, SHADOWS.sm]}>
            <View style={{ flex: 1 }}>
              <Text style={s.rowTitle}>{item.firstName} {item.lastName}</Text>
              <Text style={s.rowSub}>{item.indexNumber || item.email}</Text>
            </View>
            <Badge status={item.status} />
          </View>
        )}
        ListEmptyComponent={<EmptyState icon="school-outline" title="No students" />}
      />
    </View>
  );
}

// ─── Shared styles ────────────────────────────────────────
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
  avatar: {
    width: 36, height: 36, borderRadius: 18,
    backgroundColor: COLORS.primary + '15',
    justifyContent: 'center', alignItems: 'center',
  },
  avatarText: { fontSize: FONT.xs, fontWeight: '700', color: COLORS.primary },
});
