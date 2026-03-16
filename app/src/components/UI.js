import React from 'react';
import { View, Text, TouchableOpacity, TextInput, ActivityIndicator, StyleSheet, ScrollView, RefreshControl, Modal, Pressable, Dimensions } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { COLORS, SHADOWS, RADIUS, FONT, SPACING } from '../lib/theme';

const { width } = Dimensions.get('window');
const isSmallScreen = width < 375;

// ─── Status Badge ─────────────────────────────────────────
export function Badge({ status, label, style }) {
  const sc = STATUS_COLORS[status] || { bg: '#F1F5F9', text: '#94A3B8', label: status || '—' };
  return (
    <View style={[{ backgroundColor: sc.bg, paddingHorizontal: 10, paddingVertical: 4, borderRadius: RADIUS.full }, style]}>
      <Text style={{ color: sc.text, fontSize: FONT.xs, fontWeight: '600' }}>{label || sc.label}</Text>
    </View>
  );
}

// ─── Stat Card ────────────────────────────────────────────
export function StatCard({ label, value, icon, color, onPress }) {
  const Wrapper = onPress ? TouchableOpacity : View;
  return (
    <Wrapper onPress={onPress} activeOpacity={0.7} style={[styles.statCard, SHADOWS.md]}>
      <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }}>
        <View>
          <Text style={styles.statValue}>{value ?? '—'}</Text>
          <Text style={styles.statLabel}>{label}</Text>
        </View>
        {icon && (
          <View style={[styles.statIcon, { backgroundColor: (color || COLORS.primary) + '20' }]}>
            <Ionicons name={icon} size={22} color={color || COLORS.primary} />
          </View>
        )}
      </View>
    </Wrapper>
  );
}

// ─── Content Card ─────────────────────────────────────────
export function Card({ title, subtitle, children, style, headerRight }) {
  return (
    <View style={[styles.card, SHADOWS.glass, style]}>
      {title && (
        <View style={styles.cardHeader}>
          <View>
            <Text style={styles.cardTitle}>{title}</Text>
            {subtitle && <Text style={styles.cardSubtitle}>{subtitle}</Text>}
          </View>
          {headerRight}
        </View>
      )}
      <View style={styles.cardBody}>{children}</View>
    </View>
  );
}

// ─── Search Bar ───────────────────────────────────────────
export function SearchBar({ value, onChangeText, placeholder }) {
  return (
    <View style={styles.searchBar}>
      <Ionicons name="search-outline" size={18} color={COLORS.secondary} />
      <TextInput
        style={styles.searchInput}
        placeholder={placeholder || 'Search…'}
        placeholderTextColor={COLORS.muted}
        value={value}
        onChangeText={onChangeText}
      />
      {value ? (
        <TouchableOpacity onPress={() => onChangeText('')}>
          <Ionicons name="close-circle" size={18} color={COLORS.muted} />
        </TouchableOpacity>
      ) : null}
    </View>
  );
}

// ─── Primary Button ───────────────────────────────────────
export function Button({ title, onPress, loading, disabled, variant = 'primary', icon, style }) {
  const isPrimary = variant === 'primary';
  const isDanger  = variant === 'danger';
  const isOutline = variant === 'outline';
  return (
    <TouchableOpacity
      onPress={onPress}
      disabled={loading || disabled}
      activeOpacity={0.7}
      style={[
        styles.btn,
        isPrimary && { backgroundColor: COLORS.primary },
        isDanger  && { backgroundColor: COLORS.danger },
        isOutline && { backgroundColor: 'transparent', borderWidth: 1.5, borderColor: COLORS.primary },
        (loading || disabled) && { opacity: 0.5 },
        style,
      ]}
    >
      {loading ? (
        <ActivityIndicator color={isOutline ? COLORS.primary : '#fff'} size="small" />
      ) : (
        <View style={{ flexDirection: 'row', alignItems: 'center', gap: 6 }}>
          {icon && <Ionicons name={icon} size={18} color={isOutline ? COLORS.primary : '#fff'} />}
          <Text style={[styles.btnText, isOutline && { color: COLORS.primary }]}>{title}</Text>
        </View>
      )}
    </TouchableOpacity>
  );
}

// ─── Empty State ──────────────────────────────────────────
export function EmptyState({ icon, title, message }) {
  return (
    <View style={styles.empty}>
      <View style={styles.emptyIconWrap}>
        <Ionicons name={icon || 'file-tray-outline'} size={40} color={COLORS.secondary} />
      </View>
      <Text style={styles.emptyTitle}>{title || 'Nothing here'}</Text>
      {message && <Text style={styles.emptyMsg}>{message}</Text>}
    </View>
  );
}

// ─── Screen Wrapper ────────────────────────────────────────
export function Screen({ children, refreshing, onRefresh, style }) {
  return (
    <ScrollView
      style={[{ flex: 1, backgroundColor: COLORS.background }, style]}
      contentContainerStyle={{ padding: SPACING.lg, paddingBottom: 40 }}
      refreshControl={onRefresh ? <RefreshControl refreshing={refreshing || false} onRefresh={onRefresh} tintColor={COLORS.primary} /> : undefined}
      showsVerticalScrollIndicator={false}
    >
      {children}
    </ScrollView>
  );
}

// ─── Loading Screen ───────────────────────────────────────
export function LoadingScreen() {
  return (
    <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: COLORS.background }}>
      <ActivityIndicator size="large" color={COLORS.primary} />
    </View>
  );
}

// ─── Info Row ─────────────────────────────────────────────
export function InfoRow({ label, value }) {
  return (
    <View style={styles.infoRow}>
      <Text style={styles.infoLabel}>{label}</Text>
      <Text style={styles.infoValue}>{value ?? '—'}</Text>
    </View>
  );
}

// ─── Bottom Sheet / Modal ─────────────────────────────────
export function BottomModal({ visible, onClose, title, children }) {
  return (
    <Modal visible={visible} transparent animationType="slide" onRequestClose={onClose}>
      <Pressable style={styles.modalOverlay} onPress={onClose}>
        <Pressable style={styles.modalContent} onPress={() => {}}>
          <View style={styles.modalHandle} />
          <View style={styles.modalHeader}>
            <Text style={styles.modalTitle}>{title}</Text>
            <TouchableOpacity onPress={onClose}>
              <Ionicons name="close" size={24} color={COLORS.text} />
            </TouchableOpacity>
          </View>
          <ScrollView showsVerticalScrollIndicator={false} style={{ maxHeight: 500 }}>
            {children}
          </ScrollView>
        </Pressable>
      </Pressable>
    </Modal>
  );
}

// ─── Styles ───────────────────────────────────────────────
const styles = StyleSheet.create({
  statCard: {
    backgroundColor: COLORS.glass.heavy,
    borderRadius: RADIUS.xl,
    padding: SPACING.lg,
    borderWidth: 1,
    borderColor: COLORS.borderLight,
  },
  statValue: { fontSize: isSmallScreen ? FONT.xxl : 32, fontWeight: '800', color: COLORS.primary },
  statLabel: { fontSize: FONT.sm, color: COLORS.textSecondary, marginTop: 2 },
  statIcon: {
    width: 48, height: 48, borderRadius: RADIUS.lg,
    justifyContent: 'center', alignItems: 'center',
  },
  card: {
    backgroundColor: COLORS.glass.heavy,
    borderRadius: RADIUS.xl,
    borderWidth: 1,
    borderColor: COLORS.borderLight,
    overflow: 'hidden',
  },
  cardHeader: {
    flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center',
    paddingHorizontal: SPACING.lg, paddingVertical: SPACING.md,
    borderBottomWidth: 1, borderBottomColor: COLORS.border,
  },
  cardTitle: { fontSize: FONT.lg, fontWeight: '700', color: COLORS.text },
  cardSubtitle: { fontSize: FONT.xs, color: COLORS.textSecondary, marginTop: 2 },
  cardBody: { padding: SPACING.lg },
  searchBar: {
    flexDirection: 'row', alignItems: 'center',
    backgroundColor: COLORS.inputBg,
    borderRadius: RADIUS.lg,
    paddingHorizontal: SPACING.md, paddingVertical: SPACING.sm,
    borderWidth: 1.5, borderColor: COLORS.border,
    gap: SPACING.sm,
  },
  searchInput: { flex: 1, fontSize: FONT.base, color: COLORS.text, padding: 0 },
  btn: {
    paddingVertical: SPACING.md, paddingHorizontal: SPACING.xl,
    borderRadius: RADIUS.lg,
    alignItems: 'center', justifyContent: 'center',
    minHeight: 50,
    ...SHADOWS.sm,
  },
  btnText: { color: '#fff', fontWeight: '700', fontSize: FONT.base },
  empty: { alignItems: 'center', justifyContent: 'center', paddingVertical: 40 },
  emptyIconWrap: {
    width: 80, height: 80,
    borderRadius: 40,
    backgroundColor: COLORS.glass.medium,
    justifyContent: 'center', alignItems: 'center',
    marginBottom: SPACING.md,
  },
  emptyTitle: { fontSize: FONT.lg, fontWeight: '600', color: COLORS.textSecondary, marginTop: SPACING.sm },
  emptyMsg: { fontSize: FONT.sm, color: COLORS.muted, marginTop: SPACING.xs, textAlign: 'center' },
  infoRow: {
    flexDirection: 'row', justifyContent: 'space-between',
    paddingVertical: SPACING.md, paddingHorizontal: SPACING.md,
    backgroundColor: COLORS.inputBg,
    borderRadius: RADIUS.md,
    marginBottom: SPACING.sm,
  },
  infoLabel: { fontSize: FONT.sm, color: COLORS.textSecondary },
  infoValue: { fontSize: FONT.sm, fontWeight: '600', color: COLORS.text, flexShrink: 1, textAlign: 'right', maxWidth: '55%' },
  modalOverlay: {
    flex: 1, justifyContent: 'flex-end',
    backgroundColor: 'rgba(61,82,160,0.4)',
  },
  modalContent: {
    backgroundColor: COLORS.glass.heavy,
    borderTopLeftRadius: RADIUS.xxl, borderTopRightRadius: RADIUS.xxl,
    paddingHorizontal: SPACING.xl, paddingBottom: 30,
    maxHeight: '85%',
    borderWidth: 1,
    borderColor: COLORS.borderLight,
  },
  modalHandle: {
    width: 40, height: 4, borderRadius: 2,
    backgroundColor: COLORS.muted,
    alignSelf: 'center', marginTop: SPACING.sm, marginBottom: SPACING.sm,
  },
  modalHeader: {
    flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center',
    paddingVertical: SPACING.md,
  },
  modalTitle: { fontSize: FONT.xl, fontWeight: '700', color: COLORS.text },
});
