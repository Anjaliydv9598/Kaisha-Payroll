import React from "react";

function WorkHighlights({ entries }) {
    const highlights = entries.filter((entry) => entry.highlight);

    return (
        <div className="staff-content">
            <header className="staff-page-header">
                <div>
                    <h1>My Highlights</h1>
                    <p>Your important achievements and contributions.</p>
                </div>
            </header>

            <section className="staff-stats-grid">
                <div className="staff-stat-card">
                    <span>Total Highlights</span>
                    <h2>{highlights.length}</h2>
                </div>

                <div className="staff-stat-card">
                    <span>Total Work Entries</span>
                    <h2>{entries.length}</h2>
                </div>
            </section>

            <section className="staff-section">
                <h2>Achievement Records</h2>

                {highlights.length === 0 ? (
                    <div className="staff-empty">
                        No highlights recorded yet. Mark important work
                        entries as highlights in your Work Journal.
                    </div>
                ) : (
                    <div className="staff-entry-list">
                        {highlights.map((entry) => (
                            <article key={entry.id} className="staff-entry">
                                <div>
                                    <div className="staff-entry-top">
                                        <strong>{entry.workDate}</strong>
                                        <span className="staff-highlight-tag">
                                            ★ {entry.highlightCategory || "Achievement"}
                                        </span>
                                    </div>

                                    <p>{entry.workDescription}</p>
                                </div>
                            </article>
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
}

export default WorkHighlights;