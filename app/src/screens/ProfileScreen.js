import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Alert } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useAuth } from '../context/AuthContext';
import { userAPI, getBaseURL } from '../lib/api';
import { Screen, Card, InfoRow, Button } from '../components/UI';
import { COLORS, FONT, RADIUS, ROLE_LABELS } from '../lib/theme';

export default function ProfileScreen() {
  const { user, logout } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading]  = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const res = await userAPI.getMyProfile();
        setProfile(res.data?.data || res.data);
      } catch { setProfile(user); }
      finally { setLoading(false); }
    })();
  }, []);

  const p = profile || user || {};

  const handleLogout = () => {
    Alert.alert('Sign Out', 'Are you sure you want to sign out?', [
      { text: 'Cancel' },
      { text: 'Sign Out', style: 'destructive', onPress: logout },
    ]);
  };

  return (
    <Screen>
      {/* Avatar Header */}
      <View style={styles.header}>
        <View style={styles.avatar}>
          <Text style={styles.avatarText}>{(p.firstName || '?')[0]}{(p.lastName || '?')[0]}</Text>
        </View>
        <Text style={styles.name}>{p.firstName} {p.lastName}</Text>
        <Text style={styles.email}>{p.email}</Text>
        <View style={styles.roleBadge}>
          <Text style={styles.roleText}>{ROLE_LABELS[p.role] || p.role}</Text>
        </View>
      </View>

      {/* Profile Info */}
      <Card title="Account Details" style={{ marginBottom: 16 }}>
        <InfoRow label="User ID" value={p.userId} />
        <InfoRow label="Email" value={p.email} />
        <InfoRow label="Role" value={ROLE_LABELS[p.role] || p.role} />
        <InfoRow label="Status" value={p.status} />
        {p.indexNumber && <InfoRow label="Index Number" value={p.indexNumber} />}
        {p.semesterYear && <InfoRow label="Semester" value={p.semesterYear} />}
        {p.departmentId && <InfoRow label="Department ID" value={p.departmentId} />}
      </Card>

      {/* Server Info */}
      <Card title="Connection" style={{ marginBottom: 16 }}>
        <InfoRow label="API URL" value={getBaseURL()} />
      </Card>

      {/* Logout */}
      <Button title="Sign Out" variant="danger" icon="log-out-outline" onPress={handleLogout} />
    </Screen>
  );
}

const styles = StyleSheet.create({
  header: { alignItems: 'center', marginBottom: 24 },
  avatar: {
    width: 80, height: 80, borderRadius: 40,
    backgroundColor: COLORS.primary,
    justifyContent: 'center', alignItems: 'center', marginBottom: 12,
  },
  avatarText: { fontSize: FONT.xxl, fontWeight: '800', color: '#fff' },
  name: { fontSize: FONT.xl, fontWeight: '700', color: COLORS.text },
  email: { fontSize: FONT.sm, color: COLORS.textSecondary, marginTop: 2 },
  roleBadge: {
    backgroundColor: COLORS.primary + '15',
    paddingHorizontal: 14, paddingVertical: 4, borderRadius: RADIUS.full,
    marginTop: 8,
  },
  roleText: { color: COLORS.primary, fontSize: FONT.xs, fontWeight: '600' },
});
