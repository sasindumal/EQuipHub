import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { equipmentAPI } from '../../lib/api';
import { Screen, SearchBar, Badge, EmptyState, BottomModal, InfoRow, Card, Button } from '../../components/UI';
import { COLORS, FONT, RADIUS, SHADOWS } from '../../lib/theme';

export default function EquipmentScreen({ navigation }) {
  const [data, setData]       = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch]   = useState('');
  const [selected, setSelected] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await equipmentAPI.getAllEquipment();
      const d = res.data?.data || res.data || [];
      setData(Array.isArray(d) ? d : (d.content || []));
    } catch { }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, []);

  const filtered = data.filter(e =>
    !search || [e.name, e.serialNumber, e.category, e.description].some(f => f?.toLowerCase().includes(search.toLowerCase()))
  );

  const renderItem = ({ item }) => {
    const isAvail = item.status === 'AVAILABLE';
    return (
      <TouchableOpacity style={[styles.eqCard, SHADOWS.sm, isAvail && { borderColor: COLORS.primary + '30' }]}
        onPress={() => setSelected(item)} activeOpacity={0.7}>
        {isAvail && <View style={styles.availBar} />}
        <View style={styles.eqTop}>
          <View style={[styles.eqIcon, { backgroundColor: (isAvail ? COLORS.primary : COLORS.muted) + '15' }]}>
            <Ionicons name="hardware-chip-outline" size={22} color={isAvail ? COLORS.primary : COLORS.muted} />
          </View>
          <Badge status={item.status} />
        </View>
        <Text style={styles.eqName} numberOfLines={1}>{item.name || item.equipmentName}</Text>
        {item.description ? <Text style={styles.eqDesc} numberOfLines={2}>{item.description}</Text> : null}
        <View style={styles.eqMeta}>
          {item.category ? <Text style={styles.eqCat}>{item.category}</Text> : null}
          {item.quantity != null ? <Text style={styles.eqQty}>Qty: {item.availableQuantity ?? item.quantity}</Text> : null}
        </View>
      </TouchableOpacity>
    );
  };

  return (
    <View style={{ flex: 1, backgroundColor: COLORS.background }}>
      <View style={{ padding: 16, paddingBottom: 8 }}>
        <SearchBar value={search} onChangeText={setSearch} placeholder="Search equipment…" />
      </View>
      <FlatList
        data={filtered}
        keyExtractor={item => String(item.equipmentId || item.id)}
        renderItem={renderItem}
        numColumns={2}
        columnWrapperStyle={{ paddingHorizontal: 12, gap: 10 }}
        contentContainerStyle={{ paddingBottom: 40, paddingTop: 8 }}
        refreshing={loading}
        onRefresh={load}
        ListEmptyComponent={<EmptyState icon="cube-outline" title="No equipment" message="Equipment catalog is empty" />}
      />

      <BottomModal visible={!!selected} onClose={() => setSelected(null)} title="Equipment Details">
        {selected && (
          <View>
            <InfoRow label="Name" value={selected.name || selected.equipmentName} />
            <InfoRow label="Serial Number" value={selected.serialNumber} />
            <InfoRow label="Category" value={selected.category} />
            <InfoRow label="Description" value={selected.description} />
            <InfoRow label="Status" value={selected.status} />
            <InfoRow label="Total Qty" value={selected.quantity} />
            <InfoRow label="Available" value={selected.availableQuantity} />
            <InfoRow label="Lab" value={selected.labName || selected.lab} />
            {selected.status === 'AVAILABLE' && (
              <Button title="Request This Equipment" icon="add-circle-outline"
                onPress={() => { setSelected(null); navigation.navigate('StudentNewRequest'); }}
                style={{ marginTop: 16 }} />
            )}
          </View>
        )}
      </BottomModal>
    </View>
  );
}

const styles = StyleSheet.create({
  eqCard: {
    flex: 1, backgroundColor: COLORS.white, borderRadius: RADIUS.lg,
    padding: 14, borderWidth: 1, borderColor: COLORS.cardBorder,
    marginBottom: 10, overflow: 'hidden', maxWidth: '50%',
  },
  availBar: { position: 'absolute', top: 0, left: 0, right: 0, height: 3, backgroundColor: COLORS.primary, borderTopLeftRadius: RADIUS.lg, borderTopRightRadius: RADIUS.lg },
  eqTop: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 },
  eqIcon: { width: 38, height: 38, borderRadius: RADIUS.sm, justifyContent: 'center', alignItems: 'center' },
  eqName: { fontSize: FONT.base, fontWeight: '700', color: COLORS.text, marginBottom: 4 },
  eqDesc: { fontSize: FONT.xs, color: COLORS.textSecondary, lineHeight: 16, marginBottom: 8 },
  eqMeta: { flexDirection: 'row', flexWrap: 'wrap', gap: 6 },
  eqCat: { fontSize: FONT.xs, color: COLORS.secondary, backgroundColor: COLORS.primary + '08', paddingHorizontal: 6, paddingVertical: 2, borderRadius: 4 },
  eqQty: { fontSize: FONT.xs, color: COLORS.textSecondary, fontWeight: '600' },
});
