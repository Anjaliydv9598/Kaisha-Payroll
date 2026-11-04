import React, { useMemo, useState } from "react";

function MonthlySummary({ entries, attendance }) {
    const [selectedMonth, setSelectedMonth] = useState(
        new Date().toISOString().slice(0, 7)
    );

    const summary = useMemo(() => {
        const monthlyEntries = entries.filter(
            (entry) => entry.workDate?.slice(0, 7) === selectedMonth
        );

        const monthlyAttendance = attendance.filter((record) => {
            const date = record.attendanceDate || record.date;
            return date?.slice(0, 7) === selectedMonth;
        });

        const highlights = monthlyEntries.filter(
            (entry) => entry.highlight
        );

        return {
            totalEntries: monthlyEntries.length,
            totalHighlights: highlights.length,
            totalAttendance: monthlyAttendance.length,
            entries: monthlyEntries,
            highlights,
        };
    }, [entries, attendance, selectedMonth]);

    return (
        <div className="staff-content">
            <header className="staff-page-header">
                <div>
                    <h1>Monthly Summary</h1>
                    <p>Review your work activities and attendance.</p>
                </div>
            </header>

            <section className="staff-section">
                <div className="staff-month-selector">
                    <label htmlFor="summary-month">Select Month</label>

                    <input
                        id="summary-month"
                        type="month"
                        value={selectedMonth}
                        onChange={(event) =>
                            setSelectedMonth(event.target.value)
                        }
                    />
                </div>
            </section>

            <section className="staff-stats-grid">
                <div className="staff-stat-card">
                    <span>Work Entries</span>
                    <h2>{summary.totalEntries}</h2>
                </div>

                <div className="staff-stat-card">
                    <span>Highlights</span>
                    <h2>{summary.totalHighlights}</h2>
                </div>

                <div className="staff-stat-card">
                    <span>Attendance Records</span>
                    <h2>{summary.totalAttendance}</h2>
                </div>
            </section>

            <section className="staff-section">
                <h2>Monthly Work Records</h2>

                {summary.entries.length === 0 ? (
                    <div className="staff-empty">
                        No work records found for this month.
                    </div>
                ) : (
                    <div className="staff-entry-list">
                        {summary.entries.map((entry) => (
                            <article key={entry.id} className="staff-entry">
                                <div>
                                    <strong>{entry.workDate}</strong>
                                    <p>{entry.workDescription}</p>

                                    {entry.highlight && (
                                        <span className="staff-highlight-tag">
                                            ★ {entry.highlightCategory || "Highlight"}
                                        </span>
                                    )}
                                </div>
                            </article>
                        ))}
                    </div>
                )}
            </section>

            <section className="staff-section">
                <h2>Annual Appraisal Preparation</h2>

                <p className="staff-muted">
                    Your saved work entries and highlights can be used as
                    supporting information for your annual appraisal.
                </p>

                <div className="staff-appraisal-card">
                    <div>
                        <strong>Work entries recorded</strong>
                        <span>{entries.length}</span>
                    </div>

                    <div>
                        <strong>Highlights recorded</strong>
                        <span>
                            {entries.filter((entry) => entry.highlight).length}
                        </span>
                    </div>
                </div>

                <p className="staff-muted">
                    This is a summary of recorded data, not an official
                    appraisal or performance rating.
                </p>
            </section>
        </div>
    );
}

export default MonthlySummary;