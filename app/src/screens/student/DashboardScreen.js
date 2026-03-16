import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Alert } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { requestAPI, penaltyAPI } from '../../lib/api';
import { useAuth } from '../../context/AuthContext';
import { Screen, StatCard, Card, EmptyState, Badge, Button } from '../../components/UI';
import { COLORS, FONT, RADIUS } from '../../lib/theme';

export default function StudentDashboard({ navigation }) {
  const { user } = useAuth();
  const [stats, setStats] = useState({ active: 0, pending: 0, penalties: 0 });
  const [recentReqs, setRecentReqs] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [reqRes, penRes] = await Promise.allSettled([
        requestAPI.getMyRequests(),
        penaltyAPI.getMyPenalties(),
      ]);
      const reqs = reqRes.status === 'fulfilled' ? (reqRes.value.data?.data?.requests || reqRes.value.data?.requests || reqRes.value.data || []) : [];
      const pens = penRes.status === 'fulfilled' ? (penRes.value.data?.data?.penalties || penRes.value.data?.data || []) : [];
      const list = Array.isArray(reqs) ? reqs : [];
      setRecentReqs(list.slice(0, 5));
      setStats({
        active:    list.filter(r => ['INUSE', 'IN_USE', 'APPROVED'].includes(r.status)).length,
        pending:   list.filter(r => ['PENDINGAPPROVAL', 'PENDING', 'DRAFT'].includes(r.status)).length,
        penalties: Array.isArray(pens) ? pens.filter(p => p.status === 'ACTIVE' || p.status === 'PENDING').length : 0,
      });
    } catch { }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, []);

  return (
    <Screen refreshing={loading} onRefresh={load}>
      <Text style={styles.greeting}>Hello, {user?.firstName || 'Student'} 👋</Text>
      <Text style={styles.subGreeting}>Here's your equipment overview</Text>

      <View style={styles.stRow}>
        <View style={{ flex: 1, marginRight: 8 }}>
          <StatCard label="Active Borrows" value={stats.active} icon="swap-horizontal-outline" color={COLORS.primary} />
        </View>
        <View style={{ flex: 1, marginLeft: 8 }}>
          <StatCard label="Pending" value={stats.pending} icon="time-outline" color={COLORS.warning} />
        </View>
      </View>
      <View style={{ marginTop: 12 }}>
        <StatCard label="Active Penalties" value={stats.penalties}
          icon={stats.penalties > 0 ? 'alert-circle-outline' : 'checkmark-circle-outline'}
          color={stats.penalties > 0 ? COLORS.danger : COLORS.success} />
      </View>

      {/* Quick Actions */}
      <View style={styles.quickRow}>
        <TouchableOpacity style={styles.quickBtn} onPress={() => navigation.navigate('StudentEquipment')} activeOpacity={0.7}>
          <View style={[styles.quickIcon, { backgroundColor: COLORS.primary + '15' }]}>
            <Ionicons name="search-outline" size={22} color={COLORS.primary} />
          </View>
          <Text style={styles.quickLabel}>Browse Equipment</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.quickBtn} onPress={() => navigation.navigate('StudentNewRequest')} activeOpacity={0.7}>
          <View style={[styles.quickIcon, { backgroundColor: COLORS.success + '15' }]}>
            <Ionicons name="add-circle-outline" size={22} color={COLORS.success} />
          </View>
          <Text style={styles.quickLabel}>New Request</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.quickBtn} onPress={() => navigation.navigate('StudentRequests')} activeOpacity={0.7}>
          <View style={[styles.quickIcon, { backgroundColor: COLORS.warning + '15' }]}>
            <Ionicons name="document-text-outline" size={22} color={COLORS.warning} />
          </View>
          <Text style={styles.quickLabel}>My Requests</Text>
        </TouchableOpacity>
      </View>

      {/* Recent Requests */}
      <Card title="Recent Requests" headerRight={
        <TouchableOpacity onPress={() => navigation.navigate('StudentRequests')}>
          <Text style={{ color: COLORS.primary, fontSize: FONT.sm, fontWeight: '600' }}>View All →</Text>
        </TouchableOpacity>
      }>
        {recentReqs.length > 0 ? recentReqs.map(r => (
          <View key={r.requestId || r.id} style={styles.reqRow}>
            <View style={{ flex: 1 }}>
              <Text style={styles.reqId}>{r.requestId || r.id}</Text>
              <Text style={styles.reqPurpose} numberOfLines={1}>{r.purpose || r.equipmentName || '—'}</Text>
            </View>
            <Badge status={r.status} />
          </View>
        )) : (
          <EmptyState icon="document-text-outline" title="No requests yet" message="Create your first equipment request" />
        )}
      </Card>
    </Screen>
  );
}

const styles = StyleSheet.create({
  greeting: { fontSize: FONT.xxl, fontWeight: '800', color: COLORS.text, marginBottom: 4 },
  subGreeting: { fontSize: FONT.sm, color: COLORS.textSecondary, marginBottom: 20 },
  stRow: { flexDirection: 'row', marginBottom: 0 },
  quickRow: { flexDirection: 'row', justifyContent: 'space-between', marginVertical: 20, gap: 10 },
  quickBtn: { flex: 1, backgroundColor: COLORS.white, borderRadius: RADIUS.lg, padding: 14, alignItems: 'center', borderWidth: 1, borderColor: COLORS.cardBorder },
  quickIcon: { width: 44, height: 44, borderRadius: RADIUS.md, justifyContent: 'center', alignItems: 'center', marginBottom: 8 },
  quickLabel: { fontSize: FONT.xs, fontWeight: '600', color: COLORS.text, textAlign: 'center' },
  reqRow: { flexDirection: 'row', alignItems: 'center', paddingVertical: 10, borderBottomWidth: 1, borderBottomColor: COLORS.border },
  reqId: { fontSize: FONT.sm, fontWeight: '600', color: COLORS.text },
  reqPurpose: { fontSize: FONT.xs, color: COLORS.textSecondary, marginTop: 2 },
});
