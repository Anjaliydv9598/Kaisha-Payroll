
import React, { useState } from "react";
import { staffApi } from "../../services/staffApi";

function getLocalDate() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
}

function formatTime(time) {
    if (!time) return "--";

    return time.slice(0, 5);
}

function formatWorkingHours(hours) {
    if (hours === null || hours === undefined) return "--";

    return `${Number(hours).toFixed(2)} hrs`;
}

function StaffAttendance({ attendance = [], onRefresh }) {
    const [processing, setProcessing] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    const today = getLocalDate();

    const todayRecord = attendance.find(
        (record) => record.attendanceDate === today
    );

    const hasPunchedIn = Boolean(todayRecord?.punchIn);
    const hasPunchedOut = Boolean(todayRecord?.punchOut);

    const handlePunch = async (type) => {
        if (processing) return;

        try {
            setProcessing(true);
            setError("");
            setSuccess("");

            if (type === "in") {
                await staffApi.punchIn();
                setSuccess("Punch-in recorded successfully.");
            } else {
                await staffApi.punchOut();
                setSuccess("Punch-out recorded successfully.");
            }

            if (onRefresh) {
                await onRefresh();
            }
        } catch (err) {
            setError(err.message || "Unable to update attendance.");
        } finally {
            setProcessing(false);
        }
    };

    return (
        <div className="staff-content">

            <header className="staff-page-header">
                <div>
                    <h1>My Attendance</h1>
                    <p>Manage and view your attendance records.</p>
                </div>
            </header>

            {error && (
                <div className="staff-alert error">
                    {error}
                </div>
            )}

            {success && (
                <div className="staff-alert success">
                    {success}
                </div>
            )}

            {/* Today's Attendance */}

            <section className="staff-section">
                <h2>Today's Attendance</h2>

                <div className="staff-attendance-card">

                    <div>
                        <span>Date</span>
                        <strong>{today}</strong>
                    </div>

                    <div>
                        <span>Punch In</span>
                        <strong>
                            {formatTime(todayRecord?.punchIn)}
                        </strong>
                    </div>

                    <div>
                        <span>Punch Out</span>
                        <strong>
                            {formatTime(todayRecord?.punchOut)}
                        </strong>
                    </div>

                    <div>
                        <span>Working Hours</span>
                        <strong>
                            {formatWorkingHours(todayRecord?.workingHours)}
                        </strong>
                    </div>

                    <div>
                        <span>Status</span>
                        <strong>
                            {todayRecord?.status || "Not recorded"}
                        </strong>
                    </div>

                </div>

                {/* Punch Actions */}

                <div className="staff-attendance-actions">

                    <button
                        type="button"
                        className="staff-primary-button"
                        onClick={() => handlePunch("in")}
                        disabled={processing || hasPunchedIn}
                    >
                        {processing
                            ? "Please wait..."
                            : hasPunchedIn
                                ? "Already Punched In"
                                : "Punch In"}
                    </button>

                    <button
                        type="button"
                        className="staff-secondary-button"
                        onClick={() => handlePunch("out")}
                        disabled={
                            processing ||
                            !hasPunchedIn ||
                            hasPunchedOut
                        }
                    >
                        {processing
                            ? "Please wait..."
                            : hasPunchedOut
                                ? "Already Punched Out"
                                : "Punch Out"}
                    </button>

                </div>
            </section>

            {/* Attendance History */}

            <section className="staff-section">
                <h2>Attendance History</h2>

                {attendance.length === 0 ? (
                    <div className="staff-empty">
                        No attendance records found.
                    </div>
                ) : (
                    <div className="staff-table-wrapper">

                        <table className="staff-data-table">

                            <thead>
                            <tr>
                                <th>Date</th>
                                <th>Punch In</th>
                                <th>Punch Out</th>
                                <th>Working Hours</th>
                                <th>Status</th>
                            </tr>
                            </thead>

                            <tbody>
                            {attendance.map((record) => (
                                <tr key={record.id}>
                                    <td>
                                        {record.attendanceDate || "--"}
                                    </td>

                                    <td>
                                        {formatTime(record.punchIn)}
                                    </td>

                                    <td>
                                        {formatTime(record.punchOut)}
                                    </td>

                                    <td>
                                        {formatWorkingHours(record.workingHours)}
                                    </td>

                                    <td>
                                        {record.status || "--"}
                                    </td>
                                </tr>
                            ))}
                            </tbody>

                        </table>

                    </div>
                )}
            </section>

        </div>
    );
}

export default StaffAttendance;
