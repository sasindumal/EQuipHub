import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, Alert } from 'react-native';
import { requestAPI } from '../../lib/api';
import { SearchBar, Badge, EmptyState, BottomModal, InfoRow, Button } from '../../components/UI';
import { COLORS, FONT, RADIUS, SHADOWS } from '../../lib/theme';

export default function RequestsScreen({ navigation }) {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading]   = useState(true);
  const [search, setSearch]     = useState('');
  const [selected, setSelected] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await requestAPI.getMyRequests();
      const d = res.data?.data?.requests || res.data?.requests || res.data || [];
      setRequests(Array.isArray(d) ? d : []);
    } catch { }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, []);

  const handleCancel = async (id) => {
    Alert.alert('Cancel Request', 'Are you sure?', [
      { text: 'No' },
      { text: 'Yes', style: 'destructive', onPress: async () => {
        try { await requestAPI.cancelRequest(id); setSelected(null); load(); }
        catch (e) { Alert.alert('Error', e.response?.data?.message || 'Failed'); }
      }},
    ]);
  };

  const filtered = requests.filter(r => {
    const txt = `${r.requestId || ''} ${r.equipmentName || ''} ${r.purpose || ''}`.toLowerCase();
    return !search || txt.includes(search.toLowerCase());
  });

  const renderItem = ({ item }) => (
    <TouchableOpacity style={[styles.row, SHADOWS.sm]} onPress={() => setSelected(item)} activeOpacity={0.7}>
      <View style={{ flex: 1 }}>
        <Text style={styles.reqId}>{item.requestId || item.id}</Text>
        <Text style={styles.reqPurpose} numberOfLines={1}>{item.purpose || item.equipmentName || '—'}</Text>
        <Text style={styles.reqDate}>
          {item.borrowDate ? new Date(item.borrowDate).toLocaleDateString() : (item.fromDateTime ? new Date(item.fromDateTime).toLocaleDateString() : '—')}
        </Text>
      </View>
      <Badge status={item.status} />
    </TouchableOpacity>
  );

  return (
    <View style={{ flex: 1, backgroundColor: COLORS.background }}>
      <View style={{ padding: 16, paddingBottom: 8 }}>
        <SearchBar value={search} onChangeText={setSearch} placeholder="Search requests…" />
      </View>
      <FlatList
        data={filtered}
        keyExtractor={item => String(item.requestId || item.id)}
        renderItem={renderItem}
        contentContainerStyle={{ paddingHorizontal: 16, paddingBottom: 40 }}
        refreshing={loading}
        onRefresh={load}
        ListEmptyComponent={<EmptyState icon="document-text-outline" title="No requests" message="Create your first request" />}
      />

      <BottomModal visible={!!selected} onClose={() => setSelected(null)} title="Request Details">
        {selected && (
          <View>
            <InfoRow label="Request ID" value={selected.requestId || selected.id} />
            <InfoRow label="Equipment" value={selected.equipmentName || selected.items?.[0]?.equipmentName} />
            <InfoRow label="Status" value={selected.status} />
            <InfoRow label="Purpose" value={selected.purpose} />
            <InfoRow label="Borrow Date" value={selected.borrowDate ? new Date(selected.borrowDate).toLocaleDateString() : '—'} />
            <InfoRow label="Return Date" value={selected.returnDate ? new Date(selected.returnDate).toLocaleDateString() : '—'} />
            <InfoRow label="Created" value={selected.createdAt ? new Date(selected.createdAt).toLocaleString() : '—'} />

            {selected.rejectionReason && (
              <View style={{ backgroundColor: COLORS.dangerLight, padding: 12, borderRadius: RADIUS.sm, marginTop: 12 }}>
                <Text style={{ fontSize: FONT.xs, fontWeight: '600', color: COLORS.danger }}>Rejection Reason</Text>
                <Text style={{ fontSize: FONT.sm, color: COLORS.text, marginTop: 4 }}>{selected.rejectionReason}</Text>
              </View>
            )}

            {['DRAFT', 'PENDING', 'PENDINGAPPROVAL'].includes(selected.status) && (
              <Button title="Cancel Request" variant="danger" icon="close-circle-outline"
                onPress={() => handleCancel(selected.requestId || selected.id)}
                style={{ marginTop: 16 }} />
            )}
          </View>
        )}
      </BottomModal>

      <TouchableOpacity style={styles.fab} onPress={() => navigation.navigate('StudentNewRequest')} activeOpacity={0.8}>
        <Text style={styles.fabIcon}>+</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    backgroundColor: COLORS.white, borderRadius: RADIUS.lg,
    padding: 16, marginBottom: 10, borderWidth: 1, borderColor: COLORS.cardBorder,
    flexDirection: 'row', alignItems: 'center',
  },
  reqId: { fontSize: FONT.sm, fontWeight: '700', color: COLORS.text },
  reqPurpose: { fontSize: FONT.xs, color: COLORS.textSecondary, marginTop: 2 },
  reqDate: { fontSize: FONT.xs, color: COLORS.muted, marginTop: 4 },
  fab: {
    position: 'absolute', bottom: 24, right: 20,
    width: 56, height: 56, borderRadius: 28,
    backgroundColor: COLORS.primary,
    justifyContent: 'center', alignItems: 'center',
    elevation: 6, shadowColor: COLORS.primary, shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.3, shadowRadius: 8,
  },
  fabIcon: { color: '#fff', fontSize: 28, fontWeight: '300', marginTop: -2 },
});
