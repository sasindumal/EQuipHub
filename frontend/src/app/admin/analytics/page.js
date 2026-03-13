'use client';

import { useState, useEffect, useMemo } from 'react';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { adminAPI, userAPI } from '@/lib/api';
import {
    HiOutlineUsers,
    HiOutlineOfficeBuilding,
    HiOutlineUserGroup,
    HiOutlineAcademicCap,
    HiOutlineRefresh,
    HiOutlineChartBar,
    HiOutlineTrendingUp,
    HiOutlineIdentification,
} from 'react-icons/hi';

// ─── tiny pure-CSS bar chart ──────────────────────────────────────────────────
function BarChart({ data, valueKey, labelKey, color = 'var(--primary)' }) {
    const max = Math.max(...data.map((d) => d[valueKey]), 1);
    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {data.map((row) => (
                <div key={row[labelKey]} style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    <span style={{
                        width: 130, fontSize: 13, color: 'var(--secondary)',
                        whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis',
                        flexShrink: 0,
                    }}>
                        {row[labelKey]}
                    </span>
                    <div style={{
                        flex: 1, background: 'var(--border)', borderRadius: 4,
                        height: 20, overflow: 'hidden',
                    }}>
                        <div style={{
                            width: `${(row[valueKey] / max) * 100}%`,
                            background: color, height: '100%', borderRadius: 4,
                            transition: 'width 0.6s ease',
                            minWidth: row[valueKey] > 0 ? 4 : 0,
                        }} />
                    </div>
                    <span style={{ width: 28, textAlign: 'right', fontSize: 13, fontWeight: 600 }}>
                        {row[valueKey]}
                    </span>
                </div>
            ))}
        </div>
    );
}

// ─── donut chart (SVG) ────────────────────────────────────────────────────────
function DonutChart({ slices }) {
    const total = slices.reduce((s, d) => s + d.value, 0);
    if (total === 0) return <p style={{ color: 'var(--secondary)', fontSize: 13 }}>No data</p>;
    const r = 60, cx = 80, cy = 80, strokeW = 22;
    const circ = 2 * Math.PI * r;
    let offset = 0;
    const paths = slices.map((s) => {
        const dash = (s.value / total) * circ;
        const el = (
            <circle
                key={s.label}
                cx={cx} cy={cy} r={r}
                fill="none"
                stroke={s.color}
                strokeWidth={strokeW}
                strokeDasharray={`${dash} ${circ - dash}`}
                strokeDashoffset={-offset}
                style={{ transform: 'rotate(-90deg)', transformOrigin: `${cx}px ${cy}px` }}
            />
        );
        offset += dash;
        return el;
    });
    return (
        <div style={{ display: 'flex', alignItems: 'center', gap: 24, flexWrap: 'wrap' }}>
            <svg width={160} height={160} viewBox="0 0 160 160">
                {paths}
                <text x={cx} y={cy + 6} textAnchor="middle" fontSize={18} fontWeight={700}
                    fill="var(--foreground)">{total}</text>
            </svg>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                {slices.map((s) => (
                    <div key={s.label} style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <span style={{
                            width: 12, height: 12, borderRadius: '50%',
                            background: s.color, flexShrink: 0,
                        }} />
                        <span style={{ fontSize: 13 }}>
                            {s.label} — <strong>{s.value}</strong>
                            <span style={{ color: 'var(--secondary)', marginLeft: 4 }}>
                                ({total > 0 ? ((s.value / total) * 100).toFixed(1) : 0}%)
                            </span>
                        </span>
                    </div>
                ))}
            </div>
        </div>
    );
}

// ─── stat card ────────────────────────────────────────────────────────────────
function StatCard({ label, value, icon: Icon, color, loading }) {
    return (
        <div className="stat-card">
            <div className="stat-card-header">
                <span className="stat-card-label">{label}</span>
                <div className={`stat-card-icon ${color}`}><Icon /></div>
            </div>
            <div className="stat-card-value">
                {loading
                    ? <div className="skeleton" style={{ width: 60, height: 36 }} />
                    : value}
            </div>
        </div>
    );
}

// ─── colour palette ───────────────────────────────────────────────────────────
const ROLE_COLORS = {
    SYSTEMADMIN:      '#6366f1',
    DEPARTMENTADMIN:  '#3b82f6',
    HEADOFDEPARTMENT: '#10b981',
    LECTURER:         '#f59e0b',
    INSTRUCTOR:       '#8b5cf6',
    APPOINTEDLECTURER:'#ec4899',
    TECHNICALOFFICER: '#14b8a6',
    STUDENT:          '#64748b',
};

const ROLE_LABELS = {
    SYSTEMADMIN:       'System Admin',
    DEPARTMENTADMIN:   'Dept Admin',
    HEADOFDEPARTMENT:  'Head of Dept',
    LECTURER:          'Lecturer',
    INSTRUCTOR:        'Instructor',
    APPOINTEDLECTURER: 'Appointed Lec',
    TECHNICALOFFICER:  'Technical Officer',
    STUDENT:           'Student',
};

const STATUS_COLORS = {
    ACTIVE:    '#10b981',
    PENDING:   '#f59e0b',
    SUSPENDED: '#ef4444',
    INACTIVE:  '#94a3b8',
};

// ─── main page ────────────────────────────────────────────────────────────────
export default function AnalyticsPage() {
    const [loading, setLoading]               = useState(true);
    const [refreshing, setRefreshing]         = useState(false);
    const [lastUpdated, setLastUpdated]       = useState(null);

    const [departments, setDepartments]       = useState([]);
    const [users, setUsers]                   = useState([]);
    const [staff, setStaff]                   = useState([]);
    const [students, setStudents]             = useState([]);
    const [error, setError]                   = useState(null);

    const load = async (isRefresh = false) => {
        isRefresh ? setRefreshing(true) : setLoading(true);
        setError(null);
        try {
            const [deptRes, usersRes, staffRes, studentsRes] = await Promise.allSettled([
                adminAPI.getAllDepartments(),
                userAPI.getAllUsers(),
                userAPI.getAllStaff(),
                userAPI.getAllStudents(),
            ]);

            const safe = (res, ...keys) => {
                if (res.status !== 'fulfilled') return [];
                let d = res.value.data;
                for (const k of keys) d = d?.[k];
                return Array.isArray(d) ? d : [];
            };

            setDepartments(safe(deptRes,  'data', 'departments') || safe(deptRes,  'data'));
            setUsers(       safe(usersRes, 'data', 'users')       || safe(usersRes, 'data'));
            setStaff(       safe(staffRes, 'data', 'staff')       || safe(staffRes, 'data'));
            setStudents(    safe(studentsRes, 'data', 'students') || safe(studentsRes, 'data'));
            setLastUpdated(new Date());
        } catch (e) {
            console.error('Analytics load error:', e);
            setError('Failed to load analytics data. Please try again.');
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    };

    useEffect(() => { load(); }, []);

    // ── derived analytics ──────────────────────────────────────────────────────
    const allUsers = useMemo(() => [...new Map(
        [...users, ...staff, ...students].map((u) => [u.userId || u.id, u])
    ).values()], [users, staff, students]);

    const roleDistribution = useMemo(() => {
        const counts = {};
        allUsers.forEach((u) => { counts[u.role] = (counts[u.role] || 0) + 1; });
        return Object.entries(counts)
            .sort((a, b) => b[1] - a[1])
            .map(([role, value]) => ({
                label: ROLE_LABELS[role] || role,
                value,
                color: ROLE_COLORS[role] || '#94a3b8',
            }));
    }, [allUsers]);

    const statusDistribution = useMemo(() => {
        const counts = {};
        allUsers.forEach((u) => { counts[u.status] = (counts[u.status] || 0) + 1; });
        return Object.entries(counts).map(([status, value]) => ({
            label: status,
            value,
            color: STATUS_COLORS[status] || '#94a3b8',
        }));
    }, [allUsers]);

    const usersPerDept = useMemo(() => {
        if (!departments.length || !allUsers.length) return [];
        const deptMap = {};
        departments.forEach((d) => {
            deptMap[d.departmentId || d.id] = { label: d.code || d.name, value: 0 };
        });
        allUsers.forEach((u) => {
            const key = u.departmentId;
            if (key && deptMap[key]) deptMap[key].value++;
        });
        return Object.values(deptMap)
            .filter((d) => d.value > 0)
            .sort((a, b) => b.value - a.value)
            .slice(0, 10);
    }, [departments, allUsers]);

    const activeDepts  = departments.filter((d) => d.active !== false && d.status !== 'INACTIVE').length;
    const activeUsers  = allUsers.filter((u) => u.status === 'ACTIVE').length;
    const pendingUsers = allUsers.filter((u) => u.status === 'PENDING').length;

    const statCards = [
        { label: 'Total Users',         value: allUsers.length,     icon: HiOutlineUsers,          color: 'purple' },
        { label: 'Active Users',         value: activeUsers,         icon: HiOutlineTrendingUp,     color: 'green'  },
        { label: 'Pending Verification', value: pendingUsers,        icon: HiOutlineClock ?? HiOutlineIdentification, color: 'orange' },
        { label: 'Active Departments',   value: activeDepts,         icon: HiOutlineOfficeBuilding, color: 'blue'   },
        { label: 'Staff Members',        value: staff.length,        icon: HiOutlineUserGroup,      color: 'blue'   },
        { label: 'Students',             value: students.length,     icon: HiOutlineAcademicCap,    color: 'purple' },
    ];

    return (
        <DashboardLayout pageTitle="Analytics" pageSubtitle="System-wide usage statistics and reports">

            {/* toolbar */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                <p style={{ fontSize: 13, color: 'var(--secondary)', margin: 0 }}>
                    {lastUpdated
                        ? `Last updated: ${lastUpdated.toLocaleTimeString()}`
                        : 'Loading data…'}
                </p>
                <button
                    className="btn btn-outline btn-sm"
                    onClick={() => load(true)}
                    disabled={refreshing}
                    style={{ display: 'flex', alignItems: 'center', gap: 6 }}
                >
                    <HiOutlineRefresh style={{ animation: refreshing ? 'spin 1s linear infinite' : 'none' }} />
                    {refreshing ? 'Refreshing…' : 'Refresh'}
                </button>
            </div>

            {error && (
                <div className="alert alert-danger" style={{ marginBottom: 16 }}>{error}</div>
            )}

            {/* stat cards */}
            <div className="stats-grid" style={{ marginBottom: 24 }}>
                {statCards.map((s) => (
                    <StatCard key={s.label} {...s} loading={loading} />
                ))}
            </div>

            {/* row 1: role donut + status donut */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: 20, marginBottom: 20 }}>

                <div className="content-card">
                    <div className="content-card-header">
                        <h2 className="content-card-title">Users by Role</h2>
                    </div>
                    <div className="content-card-body">
                        {loading
                            ? <div className="skeleton" style={{ width: '100%', height: 160 }} />
                            : roleDistribution.length > 0
                                ? <DonutChart slices={roleDistribution} />
                                : <p style={{ color: 'var(--secondary)', fontSize: 13 }}>No user data</p>}
                    </div>
                </div>

                <div className="content-card">
                    <div className="content-card-header">
                        <h2 className="content-card-title">Users by Status</h2>
                    </div>
                    <div className="content-card-body">
                        {loading
                            ? <div className="skeleton" style={{ width: '100%', height: 160 }} />
                            : statusDistribution.length > 0
                                ? <DonutChart slices={statusDistribution} />
                                : <p style={{ color: 'var(--secondary)', fontSize: 13 }}>No user data</p>}
                    </div>
                </div>
            </div>

            {/* row 2: users per department bar chart */}
            <div className="content-card" style={{ marginBottom: 20 }}>
                <div className="content-card-header">
                    <h2 className="content-card-title">Users per Department</h2>
                </div>
                <div className="content-card-body">
                    {loading
                        ? <div className="skeleton" style={{ width: '100%', height: 180 }} />
                        : usersPerDept.length > 0
                            ? <BarChart data={usersPerDept} labelKey="label" valueKey="value" color="var(--primary)" />
                            : <p style={{ color: 'var(--secondary)', fontSize: 13 }}>No department/user data available</p>}
                </div>
            </div>

            {/* row 3: full user table */}
            <div className="content-card">
                <div className="content-card-header">
                    <h2 className="content-card-title">All Users</h2>
                    <span style={{ fontSize: 13, color: 'var(--secondary)' }}>
                        {!loading && `${allUsers.length} total`}
                    </span>
                </div>
                <div className="table-container">
                    <table className="table">
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Email</th>
                                <th>Role</th>
                                <th>Department</th>
                                <th>Status</th>
                                <th>Joined</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                [...Array(5)].map((_, i) => (
                                    <tr key={i}>
                                        {[140, 200, 90, 120, 70, 90].map((w, j) => (
                                            <td key={j}><div className="skeleton" style={{ width: w, height: 16 }} /></td>
                                        ))}
                                    </tr>
                                ))
                            ) : allUsers.length > 0 ? (
                                allUsers.map((u) => {
                                    const dept = departments.find(
                                        (d) => (d.departmentId || d.id) === u.departmentId
                                    );
                                    return (
                                        <tr key={u.userId || u.id}>
                                            <td style={{ fontWeight: 600 }}>
                                                {u.firstName} {u.lastName}
                                            </td>
                                            <td style={{ color: 'var(--secondary)', fontSize: 13 }}>{u.email}</td>
                                            <td>
                                                <span className="badge badge-info" style={{
                                                    background: ROLE_COLORS[u.role] + '22',
                                                    color: ROLE_COLORS[u.role],
                                                    border: `1px solid ${ROLE_COLORS[u.role]}44`,
                                                }}>
                                                    {ROLE_LABELS[u.role] || u.role}
                                                </span>
                                            </td>
                                            <td style={{ fontSize: 13, color: 'var(--secondary)' }}>
                                                {dept ? (dept.code || dept.name) : '—'}
                                            </td>
                                            <td>
                                                <span className={`badge ${
                                                    u.status === 'ACTIVE'    ? 'badge-success' :
                                                    u.status === 'PENDING'   ? 'badge-warning' :
                                                    u.status === 'SUSPENDED' ? 'badge-danger'  : 'badge-muted'
                                                }`}>{u.status}</span>
                                            </td>
                                            <td style={{ fontSize: 12, color: 'var(--secondary)' }}>
                                                {u.createdAt
                                                    ? new Date(u.createdAt).toLocaleDateString()
                                                    : '—'}
                                            </td>
                                        </tr>
                                    );
                                })
                            ) : (
                                <tr>
                                    <td colSpan={6} style={{ textAlign: 'center', padding: 40, color: 'var(--secondary)' }}>
                                        No users found
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            <style>{`
                @keyframes spin { to { transform: rotate(360deg); } }
            `}</style>
        </DashboardLayout>
    );
}
