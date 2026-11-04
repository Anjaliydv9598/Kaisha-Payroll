import React from "react";

function StaffOverview({ staff, entries, attendance, onNavigate }) {
    const today = new Date().toISOString().slice(0, 10);

    const todayAttendance = attendance.find(
        (item) =>
            (item.attendanceDate || item.date) === today
    );

    const highlights = entries.filter((entry) => entry.highlight);

    return (
        <div className="staff-content">
            <header className="staff-page-header">
                <div>
                    <h1>Welcome, {staff?.username || "Staff"}!</h1>
                    <p>Here's your work overview.</p>
                </div>

                <span className="staff-active-badge">● Active</span>
            </header>

            <section className="staff-welcome-card">
                <div>
                    <span>YOUR WORKSPACE</span>
                    <h2>Make today productive.</h2>
                    <p>
                        Keep your attendance updated and record your daily
                        achievements.
                    </p>
                </div>

                <button onClick={() => onNavigate("journal")}>
                    + Add Work Entry
                </button>
            </section>

            <section className="staff-stats-grid">
                <div className="staff-stat-card">
                    <span>Today's Attendance</span>
                    <h2>{todayAttendance ? "Recorded" : "Not recorded"}</h2>
                    <small>
                        {todayAttendance?.status || "Today's attendance"}
                    </small>
                </div>

                <div className="staff-stat-card">
                    <span>Monthly Attendance Records</span>
                    <h2>{attendance.length}</h2>
                    <small>Records loaded</small>
                </div>

                <div className="staff-stat-card">
                    <span>Total Work Entries</span>
                    <h2>{entries.length}</h2>
                    <small>Saved journal entries</small>
                </div>

                <div className="staff-stat-card">
                    <span>Highlights</span>
                    <h2>{highlights.length}</h2>
                    <small>Recorded achievements</small>
                </div>
            </section>

            <section className="staff-section">
                <div className="staff-section-heading">
                    <h2>Quick Access</h2>
                </div>

                <div className="staff-quick-grid">
                    <button onClick={() => onNavigate("profile")}>
                        <span>◉</span>
                        My Profile
                    </button>

                    <button onClick={() => onNavigate("attendance")}>
                        <span>◷</span>
                        Attendance
                    </button>

                    <button onClick={() => onNavigate("journal")}>
                        <span>▤</span>
                        Work Journal
                    </button>

                    <button onClick={() => onNavigate("highlights")}>
                        <span>★</span>
                        Highlights
                    </button>

                    <button onClick={() => onNavigate("summary")}>
                        <span>▥</span>
                        Monthly Summary
                    </button>
                </div>
            </section>

            <section className="staff-section">
                <div className="staff-section-heading">
                    <h2>Recent Work Entries</h2>
                    <button onClick={() => onNavigate("journal")}>
                        View All
                    </button>
                </div>

                {entries.length === 0 ? (
                    <div className="staff-empty">
                        No work entries have been added yet.
                    </div>
                ) : (
                    <div className="staff-entry-list">
                        {entries.slice(0, 5).map((entry) => (
                            <article key={entry.id} className="staff-entry">
                                <div>
                                    <strong>{entry.workDate}</strong>
                                    <p>{entry.workDescription}</p>
                                </div>

                                {entry.highlight && (
                                    <span className="staff-highlight-tag">
                                        ★ Highlight
                                    </span>
                                )}
                            </article>
                        ))}
                    </div>
                )}
            </section>

            <section className="staff-section">
                <h2>Company Information</h2>

                <div className="staff-company-card">
                    <div>
                        <span>Company Name</span>
                        <strong>{staff?.companyName || "Not available"}</strong>
                    </div>

                    <div>
                        <span>Company ID</span>
                        <strong>{staff?.companyId || "Not available"}</strong>
                    </div>

                    <div>
                        <span>Employee Username</span>
                        <strong>{staff?.username || "Not available"}</strong>
                    </div>
                </div>
            </section>
        </div>
    );
}

export default StaffOverview;