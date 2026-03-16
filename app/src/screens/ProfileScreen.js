import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Alert, Dimensions } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';
import { useAuth } from '../context/AuthContext';
import { userAPI, getBaseURL } from '../lib/api';
import { Screen, Card, InfoRow, Button } from '../components/UI';
import { COLORS, FONT, RADIUS, ROLE_LABELS, SPACING, SHADOWS } from '../lib/theme';

const { width } = Dimensions.get('window');
const isSmallScreen = width < 375;

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
    <View style={styles.container}>
      <LinearGradient
        colors={[COLORS.primary, COLORS.primaryLight]}
        start={{ x: 0, y: 0 }}
        end={{ x: 1, y: 1 }}
        style={styles.headerGradient}
      >
        <View style={styles.headerContent}>
          <View style={styles.avatar}>
            <Text style={styles.avatarText}>{(p.firstName || '?')[0]}{(p.lastName || '?')[0]}</Text>
          </View>
          <Text style={styles.name}>{p.firstName} {p.lastName}</Text>
          <Text style={styles.email}>{p.email}</Text>
          <View style={styles.roleBadge}>
            <Text style={styles.roleText}>{ROLE_LABELS[p.role] || p.role}</Text>
          </View>
        </View>
      </LinearGradient>

      <Screen style={styles.content}>
        <Card title="Account Details" style={{ marginBottom: SPACING.lg }}>
          <InfoRow label="User ID" value={p.userId} />
          <InfoRow label="Email" value={p.email} />
          <InfoRow label="Role" value={ROLE_LABELS[p.role] || p.role} />
          <InfoRow label="Status" value={p.status} />
          {p.indexNumber && <InfoRow label="Index Number" value={p.indexNumber} />}
          {p.semesterYear && <InfoRow label="Semester" value={p.semesterYear} />}
          {p.departmentId && <InfoRow label="Department ID" value={p.departmentId} />}
        </Card>

        <Card title="Connection" style={{ marginBottom: SPACING.lg }}>
          <InfoRow label="API URL" value={getBaseURL()} />
        </Card>

        <Button title="Sign Out" variant="danger" icon="log-out-outline" onPress={handleLogout} />
      </Screen>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: COLORS.background },
  headerGradient: {
    paddingTop: SPACING.xxl + 20,
    paddingBottom: SPACING.xxl + 10,
    borderBottomLeftRadius: 30,
    borderBottomRightRadius: 30,
  },
  headerContent: { alignItems: 'center', paddingHorizontal: SPACING.lg },
  avatar: {
    width: isSmallScreen ? 72 : 88,
    height: isSmallScreen ? 72 : 88,
    borderRadius: isSmallScreen ? 36 : 44,
    backgroundColor: 'rgba(255,255,255,0.25)',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: SPACING.md,
    borderWidth: 3,
    borderColor: 'rgba(255,255,255,0.4)',
  },
  avatarText: { 
    fontSize: isSmallScreen ? FONT.xxl : 32, 
    fontWeight: '800', 
    color: COLORS.white 
  },
  name: { 
    fontSize: isSmallScreen ? FONT.xl : FONT.xxl, 
    fontWeight: '700', 
    color: COLORS.white 
  },
  email: { 
    fontSize: FONT.sm, 
    color: 'rgba(255,255,255,0.8)', 
    marginTop: 2 
  },
  roleBadge: {
    backgroundColor: 'rgba(255,255,255,0.25)',
    paddingHorizontal: 16,
    paddingVertical: 6,
    borderRadius: RADIUS.full,
    marginTop: SPACING.sm,
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.3)',
  },
  roleText: { 
    color: COLORS.white, 
    fontSize: FONT.xs, 
    fontWeight: '700' 
  },
  content: { marginTop: -SPACING.sm },
});
