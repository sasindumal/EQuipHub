import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, KeyboardAvoidingView, Platform, ScrollView, ActivityIndicator, Dimensions } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';
import { useAuth } from '../../context/AuthContext';
import { COLORS, RADIUS, FONT, SHADOWS, SPACING } from '../../lib/theme';

const { width, height } = Dimensions.get('window');
const isSmallScreen = width < 375;

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

      <KeyboardAvoidingView style={styles.container} behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
        <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled" showsVerticalScrollIndicator={false}>
          <View style={styles.header}>
            <TouchableOpacity style={styles.backBtn} onPress={() => navigation.goBack()}>
              <Ionicons name="arrow-back" size={24} color={COLORS.white} />
            </TouchableOpacity>
          </View>

          <View style={[styles.card, SHADOWS.glass]}>
            <Text style={styles.title}>Create Account</Text>
            <Text style={styles.subtitle}>Join EQuipHub</Text>

            {error ? (
              <View style={styles.alert}>
                <Ionicons name="alert-circle" size={18} color={COLORS.danger} />
                <Text style={styles.alertText}>{error}</Text>
              </View>
            ) : null}

            <View style={styles.row}>
              <View style={[styles.inputGroup, { flex: 1, marginRight: isSmallScreen ? 4 : 6 }]}>
                <TextInput 
                  style={styles.input} 
                  placeholder="First Name" 
                  placeholderTextColor={COLORS.muted}
                  value={form.firstName} 
                  onChangeText={v => set('firstName', v)} 
                />
              </View>
              <View style={[styles.inputGroup, { flex: 1, marginLeft: isSmallScreen ? 4 : 6 }]}>
                <TextInput 
                  style={styles.input} 
                  placeholder="Last Name" 
                  placeholderTextColor={COLORS.muted}
                  value={form.lastName} 
                  onChangeText={v => set('lastName', v)} 
                />
              </View>
            </View>

            <View style={styles.inputGroup}>
              <Ionicons name="mail-outline" size={20} color={COLORS.secondary} style={styles.inputIcon} />
              <TextInput 
                style={styles.input} 
                placeholder="University Email" 
                placeholderTextColor={COLORS.muted}
                value={form.email} 
                onChangeText={v => set('email', v)} 
                keyboardType="email-address" 
                autoCapitalize="none" 
              />
            </View>

            <View style={styles.row}>
              <View style={[styles.inputGroup, { flex: 1, marginRight: isSmallScreen ? 4 : 6 }]}>
                <TextInput 
                  style={styles.input} 
                  placeholder="Index (21E001)" 
                  placeholderTextColor={COLORS.muted}
                  value={form.indexNumber} 
                  onChangeText={v => set('indexNumber', v)} 
                  autoCapitalize="characters" 
                />
              </View>
              <View style={[styles.inputGroup, { flex: 1, marginLeft: isSmallScreen ? 4 : 6 }]}>
                <TextInput 
                  style={styles.input} 
                  placeholder="Semester (1-8)" 
                  placeholderTextColor={COLORS.muted}
                  value={form.semesterYear} 
                  onChangeText={v => set('semesterYear', v)} 
                  keyboardType="number-pad" 
                />
              </View>
            </View>

            <View style={styles.inputGroup}>
              <Ionicons name="lock-closed-outline" size={20} color={COLORS.secondary} style={styles.inputIcon} />
              <TextInput 
                style={styles.input} 
                placeholder="Password (min 8)" 
                placeholderTextColor={COLORS.muted}
                value={form.password} 
                onChangeText={v => set('password', v)} 
                secureTextEntry={!showPwd} 
              />
              <TouchableOpacity onPress={() => setShowPwd(!showPwd)} style={styles.eyeBtn}>
                <Ionicons name={showPwd ? 'eye-off-outline' : 'eye-outline'} size={22} color={COLORS.secondary} />
              </TouchableOpacity>
            </View>

            <View style={styles.inputGroup}>
              <Ionicons name="lock-closed-outline" size={20} color={COLORS.secondary} style={styles.inputIcon} />
              <TextInput 
                style={styles.input} 
                placeholder="Confirm Password" 
                placeholderTextColor={COLORS.muted}
                value={form.confirmPassword} 
                onChangeText={v => set('confirmPassword', v)} 
                secureTextEntry 
              />
            </View>

            <TouchableOpacity style={styles.btn} onPress={handleRegister} disabled={loading} activeOpacity={0.8}>
              {loading ? (
                <ActivityIndicator color="#fff" />
              ) : (
                <Text style={styles.btnText}>Create Account</Text>
              )}
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
  circle1: { width: 280, height: 280, top: -100, left: -80 },
  circle2: { width: 180, height: 180, bottom: height * 0.15, right: -50 },
  container: { flex: 1 },
  header: { paddingHorizontal: SPACING.lg, paddingTop: SPACING.xxl },
  backBtn: {
    width: 40, height: 40,
    borderRadius: 20,
    backgroundColor: 'rgba(255,255,255,0.2)',
    justifyContent: 'center', alignItems: 'center',
  },
  scroll: {
    flexGrow: 1,
    justifyContent: 'center',
    paddingHorizontal: SPACING.lg,
    paddingVertical: SPACING.lg,
  },
  card: {
    backgroundColor: COLORS.glass.heavy,
    borderRadius: RADIUS.xxl,
    padding: isSmallScreen ? 18 : 24,
    borderWidth: 1,
    borderColor: COLORS.borderLight,
  },
  title: {
    fontSize: isSmallScreen ? FONT.xl : FONT.xxl,
    fontWeight: '700',
    color: COLORS.text,
    textAlign: 'center',
  },
  subtitle: {
    fontSize: FONT.sm,
    color: COLORS.textSecondary,
    textAlign: 'center',
    marginTop: SPACING.xs,
    marginBottom: SPACING.xl,
  },
  alert: {
    flexDirection: 'row',
    alignItems: 'center',
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
    marginLeft: SPACING.sm,
    flex: 1,
  },
  row: { flexDirection: 'row' },
  inputGroup: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: COLORS.inputBg,
    borderRadius: RADIUS.lg,
    borderWidth: 1.5,
    borderColor: COLORS.border,
    paddingHorizontal: SPACING.md,
    marginBottom: SPACING.md,
    height: isSmallScreen ? 46 : 52,
  },
  inputIcon: { marginRight: SPACING.sm },
  input: {
    flex: 1,
    fontSize: isSmallScreen ? FONT.base : FONT.lg,
    color: COLORS.text,
  },
  eyeBtn: { padding: SPACING.xs },
  btn: {
    backgroundColor: COLORS.primary,
    borderRadius: RADIUS.lg,
    height: isSmallScreen ? 46 : 52,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: SPACING.sm,
    ...SHADOWS.md,
  },
  btnText: {
    color: '#fff',
    fontWeight: '700',
    fontSize: isSmallScreen ? FONT.base : FONT.lg,
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'center',
    marginTop: SPACING.xl,
  },
  footerText: {
    color: COLORS.textSecondary,
    fontSize: FONT.sm,
  },
  link: {
    color: COLORS.primary,
    fontWeight: '700',
    fontSize: FONT.sm,
  },
});
