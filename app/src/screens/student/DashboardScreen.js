import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Dimensions } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';
import { requestAPI, penaltyAPI } from '../../lib/api';
import { useAuth } from '../../context/AuthContext';
import { Screen, StatCard, Card, EmptyState, Badge } from '../../components/UI';
import { COLORS, FONT, RADIUS, SPACING, SHADOWS } from '../../lib/theme';

const { width } = Dimensions.get('window');
const isSmallScreen = width < 375;

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
    <View style={styles.container}>
      <LinearGradient
        colors={[COLORS.primary, COLORS.primaryLight]}
        start={{ x: 0, y: 0 }}
        end={{ x: 1, y: 1 }}
        style={styles.headerGradient}
      >
        <View style={styles.headerContent}>
          <Text style={styles.greeting}>Hello, {user?.firstName || 'Student'} 👋</Text>
          <Text style={styles.subGreeting}>Here's your equipment overview</Text>
        </View>
      </LinearGradient>

      <Screen refreshing={loading} onRefresh={load} style={styles.content}>
        <View style={styles.statsContainer}>
          <View style={styles.statsRow}>
            <View style={{ flex: 1, marginRight: isSmallScreen ? 6 : 8 }}>
              <StatCard 
                label="Active Borrows" 
                value={stats.active} 
                icon="swap-horizontal-outline" 
                color={COLORS.primary} 
              />
            </View>
            <View style={{ flex: 1, marginLeft: isSmallScreen ? 6 : 8 }}>
              <StatCard 
                label="Pending" 
                value={stats.pending} 
                icon="time-outline" 
                color={COLORS.warning} 
              />
            </View>
          </View>
          <View style={{ marginTop: isSmallScreen ? 10 : 12 }}>
            <StatCard 
              label="Active Penalties" 
              value={stats.penalties}
              icon={stats.penalties > 0 ? 'alert-circle-outline' : 'checkmark-circle-outline'}
              color={stats.penalties > 0 ? COLORS.danger : COLORS.success} 
            />
          </View>
        </View>

        <View style={styles.quickActions}>
          <Text style={styles.sectionTitle}>Quick Actions</Text>
          <View style={styles.quickRow}>
            <TouchableOpacity style={styles.quickBtn} onPress={() => navigation.navigate('StudentEquipment')} activeOpacity={0.7}>
              <View style={[styles.quickIcon, { backgroundColor: COLORS.primary + '20' }]}>
                <Ionicons name="search-outline" size={22} color={COLORS.primary} />
              </View>
              <Text style={styles.quickLabel}>Browse</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.quickBtn} onPress={() => navigation.navigate('StudentNewRequest')} activeOpacity={0.7}>
              <View style={[styles.quickIcon, { backgroundColor: COLORS.success + '20' }]}>
                <Ionicons name="add-circle-outline" size={22} color={COLORS.success} />
              </View>
              <Text style={styles.quickLabel}>New Request</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.quickBtn} onPress={() => navigation.navigate('StudentRequests')} activeOpacity={0.7}>
              <View style={[styles.quickIcon, { backgroundColor: COLORS.warning + '20' }]}>
                <Ionicons name="document-text-outline" size={22} color={COLORS.warning} />
              </View>
              <Text style={styles.quickLabel}>My Requests</Text>
            </TouchableOpacity>
          </View>
        </View>

        <Card 
          title="Recent Requests" 
          headerRight={
            <TouchableOpacity onPress={() => navigation.navigate('StudentRequests')}>
              <Text style={{ color: COLORS.primary, fontSize: FONT.sm, fontWeight: '600' }}>View All →</Text>
            </TouchableOpacity>
          }
        >
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
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: COLORS.background },
  headerGradient: {
    paddingTop: SPACING.xxl + 20,
    paddingBottom: SPACING.xxl,
    borderBottomLeftRadius: 30,
    borderBottomRightRadius: 30,
  },
  headerContent: { paddingHorizontal: SPACING.lg },
  greeting: { 
    fontSize: isSmallScreen ? FONT.xxl : 28, 
    fontWeight: '800', 
    color: COLORS.white, 
    marginBottom: 4 
  },
  subGreeting: { 
    fontSize: FONT.sm, 
    color: 'rgba(255,255,255,0.8)', 
    marginBottom: 0 
  },
  content: { marginTop: -SPACING.lg },
  statsContainer: { paddingHorizontal: SPACING.lg, marginBottom: SPACING.lg },
  statsRow: { flexDirection: 'row' },
  quickActions: { paddingHorizontal: SPACING.lg, marginBottom: SPACING.lg },
  sectionTitle: {
    fontSize: FONT.lg,
    fontWeight: '700',
    color: COLORS.text,
    marginBottom: SPACING.md,
  },
  quickRow: { 
    flexDirection: 'row', 
    justifyContent: 'space-between', 
    gap: isSmallScreen ? 8 : 10 
  },
  quickBtn: { 
    flex: 1, 
    backgroundColor: COLORS.glass.heavy, 
    borderRadius: RADIUS.xl, 
    padding: SPACING.md, 
    alignItems: 'center', 
    borderWidth: 1, 
    borderColor: COLORS.borderLight,
    ...SHADOWS.sm,
  },
  quickIcon: { 
    width: 48, 
    height: 48, 
    borderRadius: RADIUS.lg, 
    justifyContent: 'center', 
    alignItems: 'center', 
    marginBottom: SPACING.sm 
  },
  quickLabel: { 
    fontSize: FONT.xs, 
    fontWeight: '600', 
    color: COLORS.text, 
    textAlign: 'center' 
  },
  reqRow: { 
    flexDirection: 'row', 
    alignItems: 'center', 
    paddingVertical: SPACING.md, 
    borderBottomWidth: 1, 
    borderBottomColor: COLORS.border 
  },
  reqId: { fontSize: FONT.sm, fontWeight: '600', color: COLORS.text },
  reqPurpose: { fontSize: FONT.xs, color: COLORS.textSecondary, marginTop: 2 },
});
