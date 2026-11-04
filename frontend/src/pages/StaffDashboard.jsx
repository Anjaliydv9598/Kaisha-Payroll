import React, { useEffect, useState } from "react";

import { staffApi } from "../services/staffApi";

import StaffOverview from "../components/staff/StaffOverview";
import StaffAttendance from "../components/staff/StaffAttendance";
import WorkJournal from "../components/staff/WorkJournal";
import WorkHighlights from "../components/staff/WorkHighlights";
import MonthlySummary from "../components/staff/MonthlySummary";
import StaffProfile from "../components/staff/StaffProfile";

import "../styles/StaffDashboard.css";

function StaffDashboard() {
    const [staff, setStaff] = useState(null);
    const [activeTab, setActiveTab] = useState("home");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const [entries, setEntries] = useState([]);
    const [attendance, setAttendance] = useState([]);

    const loadWorkEntries = async () => {
        const data = await staffApi.getWorkEntries();
        setEntries(Array.isArray(data) ? data : []);
    };

    const loadAttendance = async () => {
        const data = await staffApi.getMyAttendance();
        setAttendance(Array.isArray(data) ? data : []);
    };

    useEffect(() => {
        const loadDashboard = async () => {
            try {
                const [profile, workEntries, attendanceRecords] =
                    await Promise.all([
                        staffApi.getProfile(),
                        staffApi.getWorkEntries(),
                        staffApi.getMyAttendance(),
                    ]);

                if (profile.role !== "STAFF") {
                    throw new Error("This dashboard is only available to staff.");
                }

                setStaff(profile);
                setEntries(Array.isArray(workEntries) ? workEntries : []);
                setAttendance(
                    Array.isArray(attendanceRecords)
                        ? attendanceRecords
                        : []
                );
            } catch (err) {
                setError(err.message || "Unable to load dashboard.");
            } finally {
                setLoading(false);
            }
        };

        loadDashboard();
    }, []);

    const refreshEntries = async () => {
        try {
            await loadWorkEntries();
        } catch (err) {
            setError(err.message);
        }
    };

    const refreshAttendance = async () => {
        try {
            await loadAttendance();
        } catch (err) {
            setError(err.message);
        }
    };

    const navigation = [
        { id: "home", label: "Home", icon: "⌂" },
        { id: "attendance", label: "Attendance", icon: "◷" },
        { id: "journal", label: "Work Journal", icon: "▤" },
        { id: "highlights", label: "Highlights", icon: "★" },
        { id: "summary", label: "Monthly Summary", icon: "▥" },
        { id: "profile", label: "My Profile", icon: "◉" },
    ];

    if (loading) {
        return <div className="staff-loading">Loading your dashboard...</div>;
    }

    if (error && !staff) {
        return (
            <div className="staff-page">
                <div className="staff-alert error">{error}</div>
            </div>
        );
    }

    return (
        <div className="staff-dashboard">
            <aside className="staff-sidebar">
                <div className="staff-brand">
                    <h2>KAISHA</h2>
                    <span>PAYROLL</span>
                </div>

                <div className="staff-sidebar-user">
                    <div className="staff-avatar">
                        {(staff?.username || "S").charAt(0).toUpperCase()}
                    </div>

                    <div>
                        <strong>{staff?.username || "Staff"}</strong>
                        <small>Staff Account</small>
                    </div>
                </div>

                <nav className="staff-navigation">
                    {navigation.map((item) => (
                        <button
                            key={item.id}
                            className={
                                activeTab === item.id ? "active" : ""
                            }
                            onClick={() => setActiveTab(item.id)}
                        >
                            <span>{item.icon}</span>
                            {item.label}
                        </button>
                    ))}
                </nav>

                <button
                    className="staff-logout"
                    onClick={() => {
                        localStorage.removeItem("token");
                        window.location.href = "/login";
                    }}
                >
                    ↪ Logout
                </button>
            </aside>

            <main className="staff-main">
                {error && (
                    <div className="staff-alert error">{error}</div>
                )}

                {activeTab === "home" && (
                    <StaffOverview
                        staff={staff}
                        entries={entries}
                        attendance={attendance}
                        onNavigate={setActiveTab}
                    />
                )}

                {activeTab === "attendance" && (
                    <StaffAttendance
                        attendance={attendance}
                        onRefresh={refreshAttendance}
                    />
                )}

                {activeTab === "journal" && (
                    <WorkJournal
                        entries={entries}
                        onRefresh={refreshEntries}
                    />
                )}

                {activeTab === "highlights" && (
                    <WorkHighlights entries={entries} />
                )}

                {activeTab === "summary" && (
                    <MonthlySummary
                        entries={entries}
                        attendance={attendance}
                    />
                )}

                {activeTab === "profile" && (
                    <StaffProfile staff={staff} />
                )}
            </main>
        </div>
    );
}

export default StaffDashboard;