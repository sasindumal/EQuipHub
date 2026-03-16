import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, KeyboardAvoidingView, Platform, ScrollView, ActivityIndicator } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useAuth } from '../../context/AuthContext';
import { COLORS, RADIUS, FONT, SHADOWS } from '../../lib/theme';

export default function RegisterScreen({ navigation }) {
  const { register } = useAuth();
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', indexNumber: '', semesterYear: '', password: '', confirmPassword: '' });
  const [showPwd, setShowPwd] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState('');

  const set = (k, v) => setForm(p => ({ ...p, [k]: v }));

  const validate = () => {
    if (!form.firstName || !form.lastName || !form.email || !form.indexNumber || !form.semesterYear || !form.password) return 'All fields are required';
    if (form.password.length < 8) return 'Password must be at least 8 characters';
    if (form.password !== form.confirmPassword) return 'Passwords do not match';
    return null;
  };

  const handleRegister = async () => {
    const err = validate();
    if (err) { setError(err); return; }
    setError(''); setLoading(true);
    try {
      await register({
        email: form.email, password: form.password,
        firstName: form.firstName, lastName: form.lastName,
        role: 'STUDENT', semesterYear: parseInt(form.semesterYear),
        indexNumber: form.indexNumber.toUpperCase(),
      });
      navigation.navigate('VerifyEmail', { email: form.email });
    } catch (e) {
      setError(e.response?.data?.message || 'Registration failed');
    } finally { setLoading(false); }
  };

  return (
    <KeyboardAvoidingView style={styles.page} behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
      <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled">
        <View style={[styles.card, SHADOWS.md]}>
          <Text style={styles.title}>Create Account</Text>
          <Text style={styles.subtitle}>Register as a student</Text>

          {error ? <View style={styles.alert}><Text style={styles.alertText}>{error}</Text></View> : null}

          <View style={styles.row}>
            <View style={[styles.inputGroup, { flex: 1, marginRight: 6 }]}>
              <TextInput style={styles.input} placeholder="First Name" placeholderTextColor={COLORS.muted}
                value={form.firstName} onChangeText={v => set('firstName', v)} />
            </View>
            <View style={[styles.inputGroup, { flex: 1, marginLeft: 6 }]}>
              <TextInput style={styles.input} placeholder="Last Name" placeholderTextColor={COLORS.muted}
                value={form.lastName} onChangeText={v => set('lastName', v)} />
            </View>
          </View>

          <View style={styles.inputGroup}>
            <Ionicons name="mail-outline" size={18} color={COLORS.secondary} style={{ marginRight: 10 }} />
            <TextInput style={styles.input} placeholder="University Email" placeholderTextColor={COLORS.muted}
              value={form.email} onChangeText={v => set('email', v)} keyboardType="email-address" autoCapitalize="none" />
          </View>

          <View style={styles.row}>
            <View style={[styles.inputGroup, { flex: 1, marginRight: 6 }]}>
              <TextInput style={styles.input} placeholder="Index (21E001)" placeholderTextColor={COLORS.muted}
                value={form.indexNumber} onChangeText={v => set('indexNumber', v)} autoCapitalize="characters" />
            </View>
            <View style={[styles.inputGroup, { flex: 1, marginLeft: 6 }]}>
              <TextInput style={styles.input} placeholder="Semester (1-8)" placeholderTextColor={COLORS.muted}
                value={form.semesterYear} onChangeText={v => set('semesterYear', v)} keyboardType="number-pad" />
            </View>
          </View>

          <View style={styles.inputGroup}>
            <Ionicons name="lock-closed-outline" size={18} color={COLORS.secondary} style={{ marginRight: 10 }} />
            <TextInput style={styles.input} placeholder="Password (min 8)" placeholderTextColor={COLORS.muted}
              value={form.password} onChangeText={v => set('password', v)} secureTextEntry={!showPwd} />
            <TouchableOpacity onPress={() => setShowPwd(!showPwd)}>
              <Ionicons name={showPwd ? 'eye-off-outline' : 'eye-outline'} size={20} color={COLORS.secondary} />
            </TouchableOpacity>
          </View>

          <View style={styles.inputGroup}>
            <Ionicons name="lock-closed-outline" size={18} color={COLORS.secondary} style={{ marginRight: 10 }} />
            <TextInput style={styles.input} placeholder="Confirm Password" placeholderTextColor={COLORS.muted}
              value={form.confirmPassword} onChangeText={v => set('confirmPassword', v)} secureTextEntry />
          </View>

          <TouchableOpacity style={styles.btn} onPress={handleRegister} disabled={loading} activeOpacity={0.7}>
            {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.btnText}>Create Account</Text>}
          </TouchableOpacity>

          <View style={styles.footer}>
            <Text style={styles.footerText}>Already have an account? </Text>
            <TouchableOpacity onPress={() => navigation.navigate('Login')}>
              <Text style={styles.link}>Sign in</Text>
            </TouchableOpacity>
          </View>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  page: { flex: 1, backgroundColor: COLORS.background },
  scroll: { flexGrow: 1, justifyContent: 'center', padding: 24 },
  card: {
    backgroundColor: COLORS.white, borderRadius: RADIUS.xl, padding: 24,
    borderWidth: 1, borderColor: COLORS.cardBorder,
  },
  title: { fontSize: FONT.xl, fontWeight: '700', color: COLORS.text, textAlign: 'center' },
  subtitle: { fontSize: FONT.sm, color: COLORS.textSecondary, textAlign: 'center', marginTop: 4, marginBottom: 20 },
  alert: { backgroundColor: COLORS.dangerLight, padding: 12, borderRadius: RADIUS.sm, marginBottom: 14, borderWidth: 1, borderColor: COLORS.danger + '22' },
  alertText: { color: COLORS.danger, fontSize: FONT.sm, textAlign: 'center' },
  row: { flexDirection: 'row' },
  inputGroup: {
    flexDirection: 'row', alignItems: 'center', backgroundColor: COLORS.inputBg,
    borderRadius: RADIUS.md, borderWidth: 1, borderColor: COLORS.border,
    paddingHorizontal: 14, marginBottom: 12, height: 48,
  },
  input: { flex: 1, fontSize: FONT.base, color: COLORS.text },
  btn: {
    backgroundColor: COLORS.primary, borderRadius: RADIUS.md,
    height: 50, justifyContent: 'center', alignItems: 'center', marginTop: 6,
  },
  btnText: { color: '#fff', fontWeight: '600', fontSize: FONT.base },
  footer: { flexDirection: 'row', justifyContent: 'center', marginTop: 18 },
  footerText: { color: COLORS.textSecondary, fontSize: FONT.sm },
  link: { color: COLORS.primary, fontWeight: '600', fontSize: FONT.sm },
});
