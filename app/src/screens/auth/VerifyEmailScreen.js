import React, { useState, useRef } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator, Alert, Dimensions } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';
import { useAuth } from '../../context/AuthContext';
import { COLORS, RADIUS, FONT, SHADOWS, SPACING } from '../../lib/theme';

const { width, height } = Dimensions.get('window');
const isSmallScreen = width < 375;

export default function VerifyEmailScreen({ navigation, route }) {
  const email = route.params?.email || '';
  const { verifyEmail, resendCode } = useAuth();
  const [code, setCode] = useState(['', '', '', '', '', '']);
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  const [error, setError] = useState('');
  const refs = useRef([]);

  const handleChange = (idx, val) => {
    if (!/^\d*$/.test(val)) return;
    const next = [...code];
    next[idx] = val.slice(-1);
    setCode(next);
    if (val && idx < 5) refs.current[idx + 1]?.focus();
  };

  const handleKey = (idx, e) => {
    if (e.nativeEvent.key === 'Backspace' && !code[idx] && idx > 0) refs.current[idx - 1]?.focus();
  };

  const handleVerify = async () => {
    const full = code.join('');
    if (full.length !== 6) { setError('Enter the complete 6-digit code'); return; }
    setError(''); setLoading(true);
    try {
      await verifyEmail(email, full);
      Alert.alert('Success', 'Email verified! Please sign in.', [{ text: 'OK', onPress: () => navigation.navigate('Login') }]);
    } catch (e) { setError(e.response?.data?.message || 'Verification failed'); }
    finally { setLoading(false); }
  };

  const handleResend = async () => {
    setResending(true); setError('');
    try { await resendCode(email); Alert.alert('Sent', 'A new code has been sent.'); }
    catch (e) { setError(e.response?.data?.message || 'Failed to resend'); }
    finally { setResending(false); }
  };

  return (
    <View style={styles.page}>
      <LinearGradient
        colors={[COLORS.primary, COLORS.primaryLight, COLORS.background]}
        start={{ x: 0, y: 0 }}
        end={{ x: 1, y: 1 }}
        style={styles.gradient}
      />

      <View style={styles.backgroundCircles}>
        <View style={[styles.circle, styles.circle1]} />
        <View style={[styles.circle, styles.circle2]} />
      </View>

      <View style={[styles.card, SHADOWS.glass]}>
        <View style={styles.iconWrap}>
          <View style={styles.iconCircle}>
            <Ionicons name="shield-checkmark-outline" size={36} color={COLORS.primary} />
          </View>
        </View>
        <Text style={styles.title}>Verify Your Email</Text>
        <Text style={styles.subtitle}>We sent a 6-digit code to{'\n'}<Text style={{ color: COLORS.primary, fontWeight: '700' }}>{email}</Text></Text>

        {error ? (
          <View style={styles.alert}>
            <Ionicons name="alert-circle" size={16} color={COLORS.danger} />
            <Text style={styles.alertText}>{error}</Text>
          </View>
        ) : null}

        <View style={styles.codeRow}>
          {code.map((d, i) => (
            <TextInput 
              key={i} 
              ref={el => refs.current[i] = el}
              style={[styles.codeInput, d ? styles.codeInputFilled : null]}
              value={d} 
              onChangeText={v => handleChange(i, v)}
              onKeyPress={e => handleKey(i, e)}
              keyboardType="number-pad" 
              maxLength={1} 
              textAlign="center" 
            />
          ))}
        </View>

        <TouchableOpacity style={styles.btn} onPress={handleVerify} disabled={loading} activeOpacity={0.8}>
          {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.btnText}>Verify Email</Text>}
        </TouchableOpacity>

        <View style={styles.footer}>
          <Text style={styles.footerText}>Didn't receive the code? </Text>
          <TouchableOpacity onPress={handleResend} disabled={resending}>
            <Text style={styles.link}>{resending ? 'Sending…' : 'Resend Code'}</Text>
          </TouchableOpacity>
        </View>
        <TouchableOpacity onPress={() => navigation.navigate('Login')} style={styles.backBtn}>
          <Ionicons name="arrow-back" size={18} color={COLORS.primary} />
          <Text style={styles.backText}>Back to Login</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  page: { flex: 1 },
  gradient: { ...StyleSheet.absoluteFillObject },
  backgroundCircles: {
    ...StyleSheet.absoluteFillObject,
    overflow: 'hidden',
  },
  circle: {
    position: 'absolute',
    borderRadius: 200,
    backgroundColor: 'rgba(255,255,255,0.08)',
  },
  circle1: { width: 250, height: 250, top: -80, right: -80 },
  circle2: { width: 180, height: 180, bottom: height * 0.1, left: -50 },
  card: {
    flex: 1,
    justifyContent: 'center',
    backgroundColor: COLORS.glass.heavy,
    borderRadius: RADIUS.xxl,
    padding: isSmallScreen ? 24 : 32,
    margin: SPACING.lg,
    borderWidth: 1,
    borderColor: COLORS.borderLight,
  },
  iconWrap: { alignItems: 'center', marginBottom: SPACING.lg },
  iconCircle: {
    width: 80, height: 80,
    borderRadius: 40,
    backgroundColor: COLORS.glass.medium,
    justifyContent: 'center', alignItems: 'center',
    borderWidth: 1,
    borderColor: COLORS.borderLight,
  },
  title: { 
    fontSize: isSmallScreen ? FONT.xl : FONT.xxl, 
    fontWeight: '700', 
    color: COLORS.text, 
    textAlign: 'center' 
  },
  subtitle: { 
    fontSize: FONT.sm, 
    color: COLORS.textSecondary, 
    textAlign: 'center', 
    marginTop: SPACING.sm, 
    marginBottom: SPACING.xl, 
    lineHeight: 20 
  },
  alert: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: COLORS.dangerLight,
    padding: SPACING.md,
    borderRadius: RADIUS.md,
    marginBottom: SPACING.lg,
    borderWidth: 1,
    borderColor: COLORS.danger + '33',
  },
  alertText: { 
    color: COLORS.danger, 
    fontSize: FONT.sm, 
    marginLeft: SPACING.sm 
  },
  codeRow: { 
    flexDirection: 'row', 
    justifyContent: 'center', 
    gap: isSmallScreen ? 6 : 10, 
    marginBottom: SPACING.xl 
  },
  codeInput: {
    width: isSmallScreen ? 42 : 48,
    height: isSmallScreen ? 50 : 56,
    borderRadius: RADIUS.lg,
    borderWidth: 2,
    borderColor: COLORS.border,
    fontSize: isSmallScreen ? FONT.xl : FONT.xxl,
    fontWeight: '700',
    color: COLORS.text,
    backgroundColor: COLORS.inputBg,
  },
  codeInputFilled: { 
    borderColor: COLORS.primary, 
    backgroundColor: COLORS.primary + '10' 
  },
  btn: {
    backgroundColor: COLORS.primary,
    borderRadius: RADIUS.lg,
    height: isSmallScreen ? 48 : 54,
    justifyContent: 'center',
    alignItems: 'center',
    ...SHADOWS.md,
  },
  btnText: { 
    color: '#fff', 
    fontWeight: '700', 
    fontSize: isSmallScreen ? FONT.base : FONT.lg 
  },
  footer: { 
    flexDirection: 'row', 
    justifyContent: 'center', 
    marginTop: SPACING.xl 
  },
  footerText: { 
    color: COLORS.textSecondary, 
    fontSize: FONT.sm 
  },
  link: { 
    color: COLORS.primary, 
    fontWeight: '700', 
    fontSize: FONT.sm 
  },
  backBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: SPACING.lg,
  },
  backText: {
    color: COLORS.primary,
    fontWeight: '600',
    fontSize: FONT.sm,
    marginLeft: SPACING.xs,
  },
});
