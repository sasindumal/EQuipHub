import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, Alert, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { adminAPI, userAPI } from '../../lib/api';
import { Screen, StatCard, Card, Badge, EmptyState, SearchBar, BottomModal, InfoRow, Button } from '../../components/UI';
import { COLORS, FONT, RADIUS, SHADOWS, ROLE_LABELS } from '../../lib/theme';

export default function AdminDashboard({ navigation }) {
  const [stats, setStats] = useState({ depts: 0, users: 0, staff: 0, students: 0 });
  const [recentUsers, setRecent] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [dR, uR, sR, stR] = await Promise.allSettled([
        adminAPI.getAllDepartments(), userAPI.getAllUsers(), userAPI.getAllStaff(), userAPI.getAllStudents(),
      ]);
      const deps = dR.status === 'fulfilled' ? (dR.value.data?.data?.departments || dR.value.data?.data || []) : [];
      const usr  = uR.status === 'fulfilled' ? (uR.value.data?.data?.users || uR.value.data?.data || []) : [];
      const stf  = sR.status === 'fulfilled' ? (sR.value.data?.data?.staff || sR.value.data?.data || []) : [];
      const stu  = stR.status === 'fulfilled' ? (stR.value.data?.data?.students || stR.value.data?.data || []) : [];
      setStats({
        depts: Array.isArray(deps) ? deps.length : 0,
        users: Array.isArray(usr)  ? usr.length  : 0,
        staff: Array.isArray(stf)  ? stf.length  : 0,
        students: Array.isArray(stu) ? stu.length : 0,
      });
      setRecent(Array.isArray(usr) ? usr.slice(0, 6) : []);
    } catch { }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, []);

  return (
    <Screen refreshing={loading} onRefresh={load}>
      <Text style={styles.title}>System Dashboard</Text>
      <Text style={styles.sub}>System overview and quick stats</Text>

      <View style={styles.stGrid}>
        <View style={styles.stHalf}><StatCard label="Departments" value={stats.depts} icon="business-outline" color={COLORS.primary} /></View>
        <View style={styles.stHalf}><StatCard label="Total Users" value={stats.users} icon="people-outline" color={COLORS.primaryLight} /></View>
        <View style={styles.stHalf}><StatCard label="Staff" value={stats.staff} icon="person-outline" color={COLORS.success} /></View>
        <View style={styles.stHalf}><StatCard label="Students" value={stats.students} icon="school-outline" color={COLORS.warning} /></View>
      </View>

      <Card title="Recent Users" subtitle={`${recentUsers.length} shown`}>
        {recentUsers.length > 0 ? recentUsers.map(u => (
          <View key={u.userId || u.id} style={styles.userRow}>
            <View style={styles.avatar}>
              <Text style={styles.avatarText}>{(u.firstName || '?')[0]}{(u.lastName || '?')[0]}</Text>
            </View>
            <View style={{ flex: 1, marginLeft: 10 }}>
              <Text style={styles.userName}>{u.firstName} {u.lastName}</Text>
              <Text style={styles.userEmail}>{u.email}</Text>
            </View>
            <View style={{ alignItems: 'flex-end' }}>
              <Badge status={u.status} />
              <Text style={{ fontSize: FONT.xs, color: COLORS.secondary, marginTop: 2 }}>{ROLE_LABELS[u.role] || u.role}</Text>
            </View>
          </View>
        )) : <EmptyState icon="people-outline" title="No users" />}
      </Card>
    </Screen>
  );
}

const styles = StyleSheet.create({
  title: { fontSize: FONT.xxl, fontWeight: '800', color: COLORS.text },
  sub: { fontSize: FONT.sm, color: COLORS.textSecondary, marginBottom: 20 },
  stGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10, marginBottom: 20 },
  stHalf: { width: '48%' },
  userRow: {
    flexDirection: 'row', alignItems: 'center', paddingVertical: 10,
    borderBottomWidth: 1, borderBottomColor: COLORS.border,
  },
  avatar: {
    width: 36, height: 36, borderRadius: 18,
    backgroundColor: COLORS.primary + '15',
    justifyContent: 'center', alignItems: 'center',
  },
  avatarText: { fontSize: FONT.xs, fontWeight: '700', color: COLORS.primary },
  userName: { fontSize: FONT.sm, fontWeight: '600', color: COLORS.text },
  userEmail: { fontSize: FONT.xs, color: COLORS.textSecondary },
});
