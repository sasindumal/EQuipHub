'use client';

import DashboardLayout from '@/components/layouts/DashboardLayout';
import { HiOutlineDesktopComputer } from 'react-icons/hi';

export default function DeptEquipmentPage() {
    return (
        <DashboardLayout pageTitle="Equipment" pageSubtitle="Manage department equipment">
            <div className="content-card">
                <div className="content-card-body">
                    <div className="empty-state">
                        <HiOutlineDesktopComputer className="empty-state-icon" />
                        <div className="empty-state-title">Equipment Management Coming Soon</div>
                        <div className="empty-state-text">
                            You'll be able to manage department equipment, track availability, and monitor equipment status here.
                        </div>
                    </div>
                </div>
            </div>
        </DashboardLayout>
    );
}
