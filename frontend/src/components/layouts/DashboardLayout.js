'use client';

import { useState } from 'react';
import { usePathname } from 'next/navigation';
import Link from 'next/link';
import Image from 'next/image';
import { useAuth } from '@/lib/auth';
import {
    HiOutlineHome,
    HiOutlineOfficeBuilding,
    HiOutlineUsers,
    HiOutlineCog,
    HiOutlineLogout,
    HiOutlineMenu,
    HiOutlineX,
    HiOutlineChartBar,
    HiOutlineClipboardList,
    HiOutlineUserGroup,
    HiOutlineAcademicCap,
    HiOutlineDesktopComputer,
} from 'react-icons/hi';
import './dashboard.css';

const sysAdminNav = [
    {
        title: 'MAIN',
        items: [
            { label: 'Dashboard', href: '/admin', icon: HiOutlineHome },
            { label: 'Departments', href: '/admin/departments', icon: HiOutlineOfficeBuilding },
            { label: 'Users', href: '/admin/users', icon: HiOutlineUsers },
        ],
    },
    {
        title: 'SYSTEM',
        items: [
            { label: 'Configuration', href: '/admin/configuration', icon: HiOutlineCog },
            { label: 'Analytics', href: '/admin/analytics', icon: HiOutlineChartBar },
        ],
    },
];

const deptAdminNav = [
    {
        title: 'DEPARTMENT',
        items: [
            { label: 'Dashboard', href: '/department-admin', icon: HiOutlineHome },
            { label: 'Staff', href: '/department-admin/staff', icon: HiOutlineUserGroup },
            { label: 'Students', href: '/department-admin/students', icon: HiOutlineAcademicCap },
        ],
    },
    {
        title: 'MANAGEMENT',
        items: [
            { label: 'Equipment', href: '/department-admin/equipment', icon: HiOutlineDesktopComputer },
            { label: 'Requests', href: '/department-admin/requests', icon: HiOutlineClipboardList },
            { label: 'Settings', href: '/department-admin/settings', icon: HiOutlineCog },
        ],
    },
];

export default function DashboardLayout({ children, pageTitle, pageSubtitle }) {
    const pathname = usePathname();
    const { user, logout } = useAuth();
    const [sidebarOpen, setSidebarOpen] = useState(false);

    const isAdmin = pathname.startsWith('/admin');
    const navSections = isAdmin ? sysAdminNav : deptAdminNav;
    const roleLabel = isAdmin ? 'System Admin' : 'Dept Admin';

    const getInitials = () => {
        if (!user) return '?';
        return `${(user.firstName || '')[0] || ''}${(user.lastName || '')[0] || ''}`.toUpperCase();
    };

    return (
        <div className="dashboard-layout">
            {/* Overlay for mobile */}
            <div
                className={`sidebar-overlay ${sidebarOpen ? 'visible' : ''}`}
                onClick={() => setSidebarOpen(false)}
            />

            {/* Sidebar */}
            <aside className={`sidebar ${sidebarOpen ? 'open' : ''}`}>
                <div className="sidebar-header">
                    <Image src="/logo.png" alt="EQuipHub" width={40} height={40} className="sidebar-logo" />
                    <div className="sidebar-brand">
                        <span className="sidebar-brand-name">EQuipHub</span>
                        <span className="sidebar-brand-role">{roleLabel}</span>
                    </div>
                    <button
                        className="topbar-hamburger"
                        onClick={() => setSidebarOpen(false)}
                        style={{ marginLeft: 'auto', display: sidebarOpen ? 'flex' : 'none' }}
                    >
                        <HiOutlineX />
                    </button>
                </div>

                <nav className="sidebar-nav">
                    {navSections.map((section) => (
                        <div key={section.title} className="sidebar-section">
                            <div className="sidebar-section-title">{section.title}</div>
                            {section.items.map((item) => {
                                const isActive = pathname === item.href ||
                                    (item.href !== '/admin' && item.href !== '/department-admin' && pathname.startsWith(item.href));
                                return (
                                    <Link
                                        key={item.href}
                                        href={item.href}
                                        className={`sidebar-item ${isActive ? 'active' : ''}`}
                                        onClick={() => setSidebarOpen(false)}
                                    >
                                        <item.icon className="sidebar-item-icon" />
                                        {item.label}
                                    </Link>
                                );
                            })}
                        </div>
                    ))}
                </nav>

                <div className="sidebar-footer">
                    <div className="sidebar-user">
                        <div className="sidebar-avatar">{getInitials()}</div>
                        <div className="sidebar-user-info">
                            <div className="sidebar-user-name">
                                {user?.firstName} {user?.lastName}
                            </div>
                            <div className="sidebar-user-email">{user?.email}</div>
                        </div>
                    </div>
                    <button className="sidebar-logout" onClick={logout}>
                        <HiOutlineLogout />
                        Sign Out
                    </button>
                </div>
            </aside>

            {/* Topbar */}
            <header className="topbar">
                <div className="topbar-left">
                    <button className="topbar-hamburger" onClick={() => setSidebarOpen(true)}>
                        <HiOutlineMenu />
                    </button>
                    <div>
                        <h1 className="topbar-title">{pageTitle || 'Dashboard'}</h1>
                        {pageSubtitle && <p className="topbar-subtitle">{pageSubtitle}</p>}
                    </div>
                </div>
                <div className="topbar-right">
                    <div className="sidebar-avatar hide-mobile" style={{ width: 34, height: 34, fontSize: 'var(--font-size-xs)' }}>
                        {getInitials()}
                    </div>
                </div>
            </header>

            {/* Main */}
            <main className="main-content">
                {children}
            </main>
        </div>
    );
}
