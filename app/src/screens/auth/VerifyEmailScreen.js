import React, { useState, useRef } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator, Alert } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useAuth } from '../../context/AuthContext';
import { COLORS, RADIUS, FONT, SHADOWS } from '../../lib/theme';

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
      <View style={[styles.card, SHADOWS.md]}>
        <View style={styles.iconWrap}>
          <Ionicons name="shield-checkmark-outline" size={40} color={COLORS.primary} />
        </View>
        <Text style={styles.title}>Verify Your Email</Text>
        <Text style={styles.subtitle}>We sent a 6-digit code to{'\n'}<Text style={{ color: COLORS.primary, fontWeight: '600' }}>{email}</Text></Text>

        {error ? <View style={styles.alert}><Text style={styles.alertText}>{error}</Text></View> : null}

        <View style={styles.codeRow}>
          {code.map((d, i) => (
            <TextInput key={i} ref={el => refs.current[i] = el}
              style={[styles.codeInput, d ? styles.codeInputFilled : null]}
              value={d} onChangeText={v => handleChange(i, v)}
              onKeyPress={e => handleKey(i, e)}
              keyboardType="number-pad" maxLength={1} textAlign="center" />
          ))}
        </View>

        <TouchableOpacity style={styles.btn} onPress={handleVerify} disabled={loading} activeOpacity={0.7}>
          {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.btnText}>Verify Email</Text>}
        </TouchableOpacity>

        <View style={styles.footer}>
          <Text style={styles.footerText}>Didn't receive the code? </Text>
          <TouchableOpacity onPress={handleResend} disabled={resending}>
            <Text style={styles.link}>{resending ? 'Sending…' : 'Resend Code'}</Text>
          </TouchableOpacity>
        </View>
        <TouchableOpacity onPress={() => navigation.navigate('Login')} style={{ marginTop: 12, alignItems: 'center' }}>
          <Text style={styles.link}>← Back to Login</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  page: { flex: 1, backgroundColor: COLORS.background, justifyContent: 'center', padding: 24 },
  card: { backgroundColor: COLORS.white, borderRadius: RADIUS.xl, padding: 28, borderWidth: 1, borderColor: COLORS.cardBorder },
  iconWrap: { alignItems: 'center', marginBottom: 16 },
  title: { fontSize: FONT.xl, fontWeight: '700', color: COLORS.text, textAlign: 'center' },
  subtitle: { fontSize: FONT.sm, color: COLORS.textSecondary, textAlign: 'center', marginTop: 6, marginBottom: 20, lineHeight: 20 },
  alert: { backgroundColor: COLORS.dangerLight, padding: 10, borderRadius: RADIUS.sm, marginBottom: 14 },
  alertText: { color: COLORS.danger, fontSize: FONT.sm, textAlign: 'center' },
  codeRow: { flexDirection: 'row', justifyContent: 'center', gap: 8, marginBottom: 20 },
  codeInput: {
    width: 44, height: 52, borderRadius: RADIUS.md,
    borderWidth: 2, borderColor: COLORS.border,
    fontSize: FONT.xl, fontWeight: '700', color: COLORS.text,
    backgroundColor: COLORS.inputBg,
  },
  codeInputFilled: { borderColor: COLORS.primary, backgroundColor: COLORS.primary + '08' },
  btn: { backgroundColor: COLORS.primary, borderRadius: RADIUS.md, height: 50, justifyContent: 'center', alignItems: 'center' },
  btnText: { color: '#fff', fontWeight: '600', fontSize: FONT.base },
  footer: { flexDirection: 'row', justifyContent: 'center', marginTop: 16 },
  footerText: { color: COLORS.textSecondary, fontSize: FONT.sm },
  link: { color: COLORS.primary, fontWeight: '600', fontSize: FONT.sm },
});
