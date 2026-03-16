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
    HiOutlineExclamationCircle,
    HiOutlineCheckCircle,
    HiOutlineSwitchHorizontal,
    HiOutlineArrowCircleDown,
    HiOutlineBan,
} from 'react-icons/hi';
import './dashboard.css';

// ─── System Admin ────────────────────────────────────────────
const sysAdminNav = [
    {
        title: 'MAIN',
        items: [
            { label: 'Dashboard',    href: '/admin',               icon: HiOutlineHome           },
            { label: 'Departments',  href: '/admin/departments',    icon: HiOutlineOfficeBuilding },
            { label: 'Users',        href: '/admin/users',          icon: HiOutlineUsers          },
        ],
    },
    {
        title: 'SYSTEM',
        items: [
            { label: 'Configuration', href: '/admin/configuration', icon: HiOutlineCog            },
            { label: 'Analytics',     href: '/admin/analytics',     icon: HiOutlineChartBar       },
        ],
    },
];

// ─── Department Admin ─────────────────────────────────────────
const deptAdminNav = [
    {
        title: 'DEPARTMENT',
        items: [
            { label: 'Dashboard', href: '/department-admin',           icon: HiOutlineHome             },
            { label: 'Staff',     href: '/department-admin/staff',     icon: HiOutlineUserGroup        },
            { label: 'Students',  href: '/department-admin/students',  icon: HiOutlineAcademicCap      },
        ],
    },
    {
        title: 'MANAGEMENT',
        items: [
            { label: 'Equipment', href: '/department-admin/equipment', icon: HiOutlineDesktopComputer   },
            { label: 'Requests',  href: '/department-admin/requests',  icon: HiOutlineClipboardList     },
            { label: 'Penalties', href: '/department-admin/penalties', icon: HiOutlineExclamationCircle },
            { label: 'Settings',  href: '/department-admin/settings',  icon: HiOutlineCog               },
        ],
    },
];

// ─── Technical Officer ────────────────────────────────────────
const technicalOfficerNav = [
    {
        title: 'OVERVIEW',
        items: [
            { label: 'Dashboard',       href: '/technical-officer',          icon: HiOutlineHome             },
        ],
    },
    {
        title: 'EQUIPMENT OPS',
        items: [
            { label: 'Issue Equipment', href: '/technical-officer/issue',   icon: HiOutlineSwitchHorizontal },
            { label: 'Process Return',  href: '/technical-officer/returns', icon: HiOutlineArrowCircleDown  },
        ],
    },
];

// ─── Lecturer / HOD ───────────────────────────────────────────
const lecturerNav = [
    {
        title: 'APPROVALS',
        items: [
            { label: 'Approval Queue', href: '/lecturer', icon: HiOutlineCheckCircle },
        ],
    },
];

// ─── Student ──────────────────────────────────────────────────
const studentNav = [
    {
        title: 'MY PORTAL',
        items: [
            { label: 'Dashboard',   href: '/student',           icon: HiOutlineHome          },
            { label: 'My Requests', href: '/student/requests',  icon: HiOutlineClipboardList },
            { label: 'Penalties',   href: '/student/penalties', icon: HiOutlineBan           },
        ],
    },
];

// ─── Role → nav config map ────────────────────────────────────
const ROLE_CONFIG = {
    SYSTEMADMIN:       { nav: sysAdminNav,        label: 'System Admin',       rootHref: '/admin'             },
    DEPARTMENTADMIN:   { nav: deptAdminNav,        label: 'Dept Admin',         rootHref: '/department-admin'  },
    HEADOFDEPARTMENT:  { nav: deptAdminNav,        label: 'Head of Department', rootHref: '/department-admin'  },
    TECHNICALOFFICER:  { nav: technicalOfficerNav, label: 'Technical Officer',  rootHref: '/technical-officer' },
    LECTURER:          { nav: lecturerNav,         label: 'Lecturer',           rootHref: '/lecturer'          },
    APPOINTEDLECTURER: { nav: lecturerNav,         label: 'Appointed Lecturer', rootHref: '/lecturer'          },
    INSTRUCTOR:        { nav: lecturerNav,         label: 'Instructor',         rootHref: '/lecturer'          },
    STUDENT:           { nav: studentNav,          label: 'Student',            rootHref: '/student'           },
};

// Hrefs that need exact-match active detection (dashboard roots)
const EXACT_MATCH_HREFS = new Set([
    '/admin',
    '/department-admin',
    '/technical-officer',
    '/lecturer',
    '/student',
]);

export default function DashboardLayout({ children, pageTitle, pageSubtitle }) {
    const pathname = usePathname();
    const { user, logout } = useAuth();
    const [sidebarOpen, setSidebarOpen] = useState(false);

    // Resolve nav + label by user role; fall back to path-based detection
    const roleKey    = user?.role?.toUpperCase();
    const roleConfig = ROLE_CONFIG[roleKey] || (
        pathname.startsWith('/admin')             ? ROLE_CONFIG.SYSTEMADMIN      :
        pathname.startsWith('/technical-officer') ? ROLE_CONFIG.TECHNICALOFFICER :
        pathname.startsWith('/lecturer')          ? ROLE_CONFIG.LECTURER         :
        pathname.startsWith('/student')           ? ROLE_CONFIG.STUDENT          :
                                                    ROLE_CONFIG.DEPARTMENTADMIN
    );

    const navSections = roleConfig.nav;
    const roleLabel   = roleConfig.label;

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
                                const isActive = EXACT_MATCH_HREFS.has(item.href)
                                    ? pathname === item.href
                                    : pathname === item.href || pathname.startsWith(item.href + '/');
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
