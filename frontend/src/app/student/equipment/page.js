'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { equipmentAPI } from '@/lib/api';
import {
    HiOutlineDesktopComputer,
    HiOutlineSearch,
    HiOutlineRefresh,
    HiOutlineEye,
    HiOutlinePlusCircle,
} from 'react-icons/hi';

const STATUS_COLORS = {
    AVAILABLE:   { badge: 'badge-success', label: 'Available',   color: '#7091E6' },
    IN_USE:      { badge: 'badge-warning', label: 'In Use',      color: '#8697C4' },
    MAINTENANCE: { badge: 'badge-danger',  label: 'Maintenance', color: '#3D52A0' },
    INACTIVE:    { badge: 'badge-muted',   label: 'Inactive',    color: '#ADBBDA' },
    BORROWED:    { badge: 'badge-warning', label: 'Borrowed',    color: '#8697C4' },
    DAMAGED:     { badge: 'badge-danger',  label: 'Damaged',     color: '#3D52A0' },
};

const CATEGORIES = [
    'ALL',
    'MEASUREMENT_INSTRUMENTS',
    'POWER_SUPPLY',
    'DEVELOPMENT_TOOLS',
    'COMPONENTS',
    'SPECIALIZED_LAB_EQUIPMENT',
    'COMPUTER_EQUIPMENT',
    'NETWORK_EQUIPMENT',
];

const formatCategory = (c) => {
    if (c === 'ALL') return 'All Categories';
    return c.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase()).toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
};

export default function StudentEquipmentCatalog() {
    const [equipment, setEquipment]   = useState([]);
    const [loading, setLoading]       = useState(true);
    const [error, setError]           = useState(null);
    const [search, setSearch]         = useState('');
    const [filterStatus, setFilter]   = useState('ALL');
    const [filterCat, setFilterCat]   = useState('ALL');
    const [viewItem, setViewItem]     = useState(null);

    useEffect(() => { load(); }, []);

    const load = async () => {
        setLoading(true); setError(null);
        try {
            const res = await equipmentAPI.getAllEquipment();
            const data = res.data?.data || res.data || [];
            const list = Array.isArray(data) ? data : (data.content || []);
            setEquipment(list);
        } catch (e) {
            setError('Failed to load equipment catalog.');
        } finally { setLoading(false); }
    };

    const filtered = equipment.filter((e) => {
        const matchSearch = !search ||
            e.name?.toLowerCase().includes(search.toLowerCase()) ||
            e.serialNumber?.toLowerCase().includes(search.toLowerCase()) ||
            e.category?.toLowerCase().includes(search.toLowerCase()) ||
            e.description?.toLowerCase().includes(search.toLowerCase());
        const matchStatus = filterStatus === 'ALL' || e.status === filterStatus;
        const matchCat    = filterCat === 'ALL' || e.category === filterCat;
        return matchSearch && matchStatus && matchCat;
    });

    const availableCount = equipment.filter(e => e.status === 'AVAILABLE').length;
    const inUseCount     = equipment.filter(e => ['IN_USE', 'BORROWED'].includes(e.status)).length;
    const totalCount     = equipment.length;

    return (
        <DashboardLayout pageTitle="Equipment Catalog" pageSubtitle="Browse available equipment for borrowing">
            {error && <div className="alert alert-danger" style={{ marginBottom: 16 }}>{error}</div>}

            {/* Summary stats */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: 16, marginBottom: 24 }}>
                {[
                    { label: 'Total Equipment', value: totalCount,     color: 'var(--primary)' },
                    { label: 'Available',        value: availableCount, color: 'var(--primary-light)' },
                    { label: 'In Use',           value: inUseCount,     color: 'var(--secondary)' },
                ].map(s => (
                    <div key={s.label} className="content-card" style={{ padding: 20 }}>
                        <div style={{ fontSize: 32, fontWeight: 800, color: s.color }}>{loading ? '—' : s.value}</div>
                        <div style={{ fontSize: 13, color: 'var(--secondary)', marginTop: 4 }}>{s.label}</div>
                    </div>
                ))}
            </div>

            {/* Toolbar */}
            <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', marginBottom: 16, alignItems: 'center' }}>
                <div style={{ position: 'relative', flex: 1, minWidth: 200 }}>
                    <HiOutlineSearch style={{
                        position: 'absolute', left: 10, top: '50%',
                        transform: 'translateY(-50%)', color: 'var(--secondary)',
                    }} />
                    <input
                        className="form-input"
                        style={{ paddingLeft: 32 }}
                        placeholder="Search equipment by name, serial, category…"
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </div>
                <select className="form-input" style={{ width: 160 }}
                    value={filterStatus} onChange={(e) => setFilter(e.target.value)}>
                    <option value="ALL">All Statuses</option>
                    <option value="AVAILABLE">Available</option>
                    <option value="IN_USE">In Use</option>
                    <option value="MAINTENANCE">Maintenance</option>
                </select>
                <select className="form-input" style={{ width: 200 }}
                    value={filterCat} onChange={(e) => setFilterCat(e.target.value)}>
                    {CATEGORIES.map(c => (
                        <option key={c} value={c}>{formatCategory(c)}</option>
                    ))}
                </select>
                <button className="btn btn-outline btn-sm" onClick={load}
                    style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <HiOutlineRefresh /> Refresh
                </button>
            </div>

            {/* Equipment Grid / Cards */}
            <div className="content-card">
                <div className="content-card-header">
                    <h2 className="content-card-title">Equipment List</h2>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                        <span style={{ fontSize: 13, color: 'var(--secondary)' }}>{!loading && `${filtered.length} items`}</span>
                        <Link href="/student/requests/new" className="btn btn-primary btn-sm"
                            style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                            <HiOutlinePlusCircle /> New Request
                        </Link>
                    </div>
                </div>

                {/* Card grid view */}
                <div style={{ padding: 24 }}>
                    {loading ? (
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: 16 }}>
                            {[...Array(6)].map((_, i) => (
                                <div key={i} style={{
                                    padding: 20, borderRadius: 'var(--radius-md)',
                                    border: '1px solid rgba(173, 187, 218, 0.12)',
                                    background: 'rgba(255,255,255,0.5)',
                                }}>
                                    <div className="skeleton" style={{ width: '80%', height: 20, marginBottom: 10 }} />
                                    <div className="skeleton" style={{ width: '60%', height: 14, marginBottom: 8 }} />
                                    <div className="skeleton" style={{ width: '40%', height: 14, marginBottom: 16 }} />
                                    <div className="skeleton" style={{ width: 80, height: 24, borderRadius: 12 }} />
                                </div>
                            ))}
                        </div>
                    ) : filtered.length > 0 ? (
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: 16 }}>
                            {filtered.map((item) => {
                                const sc = STATUS_COLORS[item.status] || { badge: 'badge-muted', label: item.status, color: '#ADBBDA' };
                                const isAvailable = item.status === 'AVAILABLE';
                                return (
                                    <div key={item.equipmentId || item.id} style={{
                                        padding: 20,
                                        borderRadius: 'var(--radius-md)',
                                        border: `1px solid ${isAvailable ? 'rgba(112, 145, 230, 0.2)' : 'rgba(173, 187, 218, 0.12)'}`,
                                        background: isAvailable ? 'rgba(255,255,255,0.7)' : 'rgba(255,255,255,0.45)',
                                        transition: 'all 0.2s ease',
                                        cursor: 'pointer',
                                        position: 'relative',
                                        overflow: 'hidden',
                                    }}
                                    onClick={() => setViewItem(item)}
                                    onMouseEnter={e => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = 'var(--hover-shadow)'; }}
                                    onMouseLeave={e => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = 'none'; }}
                                    >
                                        {isAvailable && (
                                            <div style={{
                                                position: 'absolute', top: 0, left: 0, right: 0, height: 3,
                                                background: 'var(--primary-gradient)',
                                            }} />
                                        )}
                                        <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: 8 }}>
                                            <div style={{
                                                width: 42, height: 42, borderRadius: 'var(--radius-sm)',
                                                background: `${sc.color}15`, color: sc.color,
                                                display: 'flex', alignItems: 'center', justifyContent: 'center',
                                                fontSize: 20,
                                            }}>
                                                <HiOutlineDesktopComputer />
                                            </div>
                                            <span className={`badge ${sc.badge}`}>{sc.label}</span>
                                        </div>
                                        <div style={{ fontWeight: 700, fontSize: 15, marginBottom: 4, color: 'var(--text)' }}>
                                            {item.name || item.equipmentName}
                                        </div>
                                        {item.description && (
                                            <div style={{ fontSize: 12, color: 'var(--secondary)', marginBottom: 8, lineHeight: 1.4, overflow: 'hidden', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical' }}>
                                                {item.description}
                                            </div>
                                        )}
                                        <div style={{ display: 'flex', gap: 12, fontSize: 12, color: 'var(--secondary)', marginTop: 8 }}>
                                            {item.category && (
                                                <span style={{ padding: '2px 8px', background: 'rgba(61, 82, 160, 0.06)', borderRadius: 4 }}>
                                                    {item.category}
                                                </span>
                                            )}
                                            {item.quantity != null && (
                                                <span>Qty: <strong>{item.availableQuantity ?? item.quantity}</strong></span>
                                            )}
                                        </div>
                                        {item.serialNumber && (
                                            <div style={{ fontSize: 11, color: 'var(--muted)', marginTop: 6, fontFamily: 'monospace' }}>
                                                SN: {item.serialNumber}
                                            </div>
                                        )}
                                    </div>
                                );
                            })}
                        </div>
                    ) : (
                        <div className="empty-state">
                            <HiOutlineDesktopComputer className="empty-state-icon" />
                            <div className="empty-state-title">No equipment found</div>
                            <div className="empty-state-text">
                                {search || filterStatus !== 'ALL' || filterCat !== 'ALL'
                                    ? 'No equipment matches your filters. Try adjusting your search.'
                                    : 'Equipment catalog is currently empty.'}
                            </div>
                        </div>
                    )}
                </div>
            </div>

            {/* View Detail Modal */}
            {viewItem && (
                <div className="modal-overlay" onClick={() => setViewItem(null)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 520 }}>
                        <div className="modal-header">
                            <h2>Equipment Details</h2>
                            <button className="modal-close" onClick={() => setViewItem(null)}>✕</button>
                        </div>
                        <div className="modal-body">
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                                {[
                                    { label: 'Name',          value: viewItem.name || viewItem.equipmentName },
                                    { label: 'Serial Number', value: viewItem.serialNumber || '—' },
                                    { label: 'Category',      value: viewItem.category || '—' },
                                    { label: 'Description',   value: viewItem.description || '—' },
                                    { label: 'Status',        value: STATUS_COLORS[viewItem.status]?.label || viewItem.status },
                                    { label: 'Total Qty',     value: viewItem.quantity ?? '—' },
                                    { label: 'Available Qty', value: viewItem.availableQuantity ?? '—' },
                                    { label: 'Lab',           value: viewItem.labName || viewItem.lab || '—' },
                                ].map(({ label, value }) => (
                                    <div key={label} style={{
                                        display: 'flex', justifyContent: 'space-between',
                                        padding: '8px 12px', background: 'var(--bg-light)',
                                        borderRadius: 'var(--radius-sm)',
                                    }}>
                                        <span style={{ color: 'var(--secondary)', fontSize: 13 }}>{label}</span>
                                        <span style={{ fontWeight: 600, fontSize: 13, textAlign: 'right', maxWidth: '60%' }}>{value ?? '—'}</span>
                                    </div>
                                ))}
                            </div>
                        </div>
                        <div className="modal-footer">
                            {viewItem.status === 'AVAILABLE' && (
                                <Link href="/student/requests/new" className="btn btn-primary btn-sm"
                                    style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <HiOutlinePlusCircle /> Request This
                                </Link>
                            )}
                            <button className="btn btn-outline" onClick={() => setViewItem(null)}>Close</button>
                        </div>
                    </div>
                </div>
            )}
        </DashboardLayout>
    );
}
