import React, { useCallback, useEffect, useMemo, useState } from "react";
import "./LeaveManagement.css";

const API_URL = `${import.meta.env.VITE_API_BASE_URL}/leaves`;

const getToken = () => localStorage.getItem("token");

const formatDate = (date) => {
    if (!date) return "—";

    const parsedDate = new Date(`${date}T00:00:00`);

    return Number.isNaN(parsedDate.getTime())
        ? date
        : parsedDate.toLocaleDateString("en-IN", {
            day: "2-digit",
            month: "short",
            year: "numeric",
        });
};

const formatDateTime = (dateTime) => {
    if (!dateTime) return "—";

    const parsedDate = new Date(dateTime);

    return Number.isNaN(parsedDate.getTime())
        ? dateTime
        : parsedDate.toLocaleString("en-IN", {
            day: "2-digit",
            month: "short",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
        });
};

const getErrorMessage = async (response) => {
    try {
        const data = await response.json();
        return data.message || data.error || "Something went wrong.";
    } catch {
        return `Request failed with status ${response.status}.`;
    }
};

function LeaveManagement() {
    const [leaves, setLeaves] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [filter, setFilter] = useState("ALL");
    const [search, setSearch] = useState("");

    const [selectedLeave, setSelectedLeave] = useState(null);
    const [decision, setDecision] = useState("");
    const [adminRemark, setAdminRemark] = useState("");
    const [saving, setSaving] = useState(false);
    const [actionError, setActionError] = useState("");

    const loadLeaves = useCallback(async () => {
        setLoading(true);
        setError("");

        try {
            const token = getToken();

            if (!token) {
                throw new Error("Your session has expired. Please log in again.");
            }

            const response = await fetch(API_URL, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });

            if (!response.ok) {
                throw new Error(await getErrorMessage(response));
            }

            const data = await response.json();
            setLeaves(Array.isArray(data) ? data : []);
        } catch (err) {
            setError(err.message || "Unable to load leave requests.");
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadLeaves();
    }, [loadLeaves]);

    const counts = useMemo(() => {
        return {
            total: leaves.length,
            pending: leaves.filter((leave) => leave.status === "PENDING").length,
            approved: leaves.filter((leave) => leave.status === "APPROVED").length,
            rejected: leaves.filter((leave) => leave.status === "REJECTED").length,
        };
    }, [leaves]);

    const filteredLeaves = useMemo(() => {
        const searchValue = search.trim().toLowerCase();

        return leaves.filter((leave) => {
            const matchesStatus =
                filter === "ALL" || leave.status === filter;

            const matchesSearch =
                !searchValue ||
                String(leave.employeeId || "").toLowerCase().includes(searchValue) ||
                String(leave.reason || "").toLowerCase().includes(searchValue) ||
                String(leave.leaveType || "").toLowerCase().includes(searchValue);

            return matchesStatus && matchesSearch;
        });
    }, [leaves, filter, search]);

    const openDecisionModal = (leave, selectedDecision) => {
        setSelectedLeave(leave);
        setDecision(selectedDecision);
        setAdminRemark("");
        setActionError("");
    };

    const closeDecisionModal = () => {
        if (saving) return;

        setSelectedLeave(null);
        setDecision("");
        setAdminRemark("");
        setActionError("");
    };

    const submitDecision = async (event) => {
        event.preventDefault();

        if (!selectedLeave || !decision) return;

        setSaving(true);
        setActionError("");

        try {
            const token = getToken();

            if (!token) {
                throw new Error("Your session has expired. Please log in again.");
            }

            const response = await fetch(
                `${API_URL}/${selectedLeave.id}/decision`,
                {
                    method: "PUT",
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        status: decision,
                        adminRemark: adminRemark.trim(),
                    }),
                }
            );

            if (!response.ok) {
                throw new Error(await getErrorMessage(response));
            }

            const updatedLeave = await response.json();

            setLeaves((previousLeaves) =>
                previousLeaves.map((leave) =>
                    leave.id === updatedLeave.id ? updatedLeave : leave
                )
            );

            closeDecisionModal();
        } catch (err) {
            setActionError(err.message || "Unable to update leave request.");
        } finally {
            setSaving(false);
        }
    };

    const getStatusClass = (status) => {
        switch (status) {
            case "APPROVED":
                return "approved";
            case "REJECTED":
                return "rejected";
            default:
                return "pending";
        }
    };

    return (
        <div className="leave-management">
            <div className="leave-page-header">
                <div>
                    <h2>Leave Management</h2>
                    <p>View and manage employee leave requests.</p>
                </div>

                <button
                    type="button"
                    className="leave-refresh-btn"
                    onClick={loadLeaves}
                    disabled={loading}
                >
                    {loading ? "Loading..." : "↻ Refresh"}
                </button>
            </div>

            <div className="leave-summary-grid">
                <div className="leave-summary-card">
                    <span>Total Requests</span>
                    <strong>{counts.total}</strong>
                </div>

                <div className="leave-summary-card pending-card">
                    <span>Pending</span>
                    <strong>{counts.pending}</strong>
                </div>

                <div className="leave-summary-card approved-card">
                    <span>Approved</span>
                    <strong>{counts.approved}</strong>
                </div>

                <div className="leave-summary-card rejected-card">
                    <span>Rejected</span>
                    <strong>{counts.rejected}</strong>
                </div>
            </div>

            <div className="leave-toolbar">
                <div className="leave-filter-buttons">
                    {["ALL", "PENDING", "APPROVED", "REJECTED"].map((status) => (
                        <button
                            type="button"
                            key={status}
                            className={filter === status ? "active" : ""}
                            onClick={() => setFilter(status)}
                        >
                            {status === "ALL" ? "All Requests" : status}
                        </button>
                    ))}
                </div>

                <input
                    type="search"
                    placeholder="Search employee ID, reason, type..."
                    value={search}
                    onChange={(event) => setSearch(event.target.value)}
                    aria-label="Search leave requests"
                />
            </div>

            {error && (
                <div className="leave-error-message">
                    {error}
                    <button type="button" onClick={loadLeaves}>
                        Try again
                    </button>
                </div>
            )}

            {loading ? (
                <div className="leave-empty-state">Loading leave requests...</div>
            ) : !error && filteredLeaves.length === 0 ? (
                <div className="leave-empty-state">
                    <h3>No leave requests found</h3>
                    <p>
                        {leaves.length === 0
                            ? "There are no leave requests yet."
                            : "Try changing your search or status filter."}
                    </p>
                </div>
            ) : (
                !error && (
                    <div className="leave-table-wrapper">
                        <table className="leave-table">
                            <thead>
                            <tr>
                                <th>Request ID</th>
                                <th>Employee ID</th>
                                <th>Leave Dates</th>
                                <th>Type</th>
                                <th>Reason</th>
                                <th>Applied On</th>
                                <th>Status</th>
                                <th>Admin Remark</th>
                                <th>Action</th>
                            </tr>
                            </thead>

                            <tbody>
                            {filteredLeaves.map((leave) => (
                                <tr key={leave.id}>
                                    <td>#{leave.id}</td>

                                    <td className="leave-employee-id">
                                        {leave.employeeId}
                                    </td>

                                    <td>
                                        <div className="leave-date-range">
                                            <span>{formatDate(leave.startDate)}</span>
                                            <small>to</small>
                                            <span>{formatDate(leave.endDate)}</span>
                                        </div>
                                    </td>

                                    <td>
                                            <span
                                                className={`leave-type ${String(
                                                    leave.leaveType || ""
                                                ).toLowerCase()}`}
                                            >
                                                {leave.leaveType || "—"}
                                            </span>
                                    </td>

                                    <td className="leave-reason-cell">
                                        {leave.reason || "—"}
                                    </td>

                                    <td>{formatDateTime(leave.createdAt)}</td>

                                    <td>
                                            <span
                                                className={`leave-status ${getStatusClass(
                                                    leave.status
                                                )}`}
                                            >
                                                {leave.status}
                                            </span>
                                    </td>

                                    <td className="leave-remark-cell">
                                        {leave.adminRemark || "—"}
                                    </td>

                                    <td>
                                        {leave.status === "PENDING" ? (
                                            <div className="leave-action-buttons">
                                                <button
                                                    type="button"
                                                    className="leave-approve-btn"
                                                    onClick={() =>
                                                        openDecisionModal(
                                                            leave,
                                                            "APPROVED"
                                                        )
                                                    }
                                                >
                                                    Approve
                                                </button>

                                                <button
                                                    type="button"
                                                    className="leave-reject-btn"
                                                    onClick={() =>
                                                        openDecisionModal(
                                                            leave,
                                                            "REJECTED"
                                                        )
                                                    }
                                                >
                                                    Reject
                                                </button>
                                            </div>
                                        ) : (
                                            <span className="leave-no-action">
                                                    Completed
                                                </span>
                                        )}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )
            )}

            {selectedLeave && (
                <div
                    className="leave-modal-overlay"
                    onMouseDown={(event) => {
                        if (event.target === event.currentTarget) {
                            closeDecisionModal();
                        }
                    }}
                >
                    <div
                        className="leave-decision-modal"
                        role="dialog"
                        aria-modal="true"
                        aria-labelledby="leave-decision-title"
                    >
                        <div className="leave-modal-header">
                            <div>
                                <h3 id="leave-decision-title">
                                    {decision === "APPROVED"
                                        ? "Approve Leave Request"
                                        : "Reject Leave Request"}
                                </h3>
                                <p>Request #{selectedLeave.id}</p>
                            </div>

                            <button
                                type="button"
                                className="leave-modal-close"
                                onClick={closeDecisionModal}
                                disabled={saving}
                                aria-label="Close"
                            >
                                ×
                            </button>
                        </div>

                        <div className="leave-modal-details">
                            <p>
                                <strong>Employee ID:</strong>{" "}
                                {selectedLeave.employeeId}
                            </p>
                            <p>
                                <strong>Leave Type:</strong>{" "}
                                {selectedLeave.leaveType}
                            </p>
                            <p>
                                <strong>From:</strong>{" "}
                                {formatDate(selectedLeave.startDate)}
                            </p>
                            <p>
                                <strong>To:</strong>{" "}
                                {formatDate(selectedLeave.endDate)}
                            </p>
                            <p>
                                <strong>Reason:</strong>{" "}
                                {selectedLeave.reason}
                            </p>
                        </div>

                        <form onSubmit={submitDecision}>
                            <label htmlFor="leave-admin-remark">
                                Admin Remark
                            </label>

                            <textarea
                                id="leave-admin-remark"
                                rows="4"
                                placeholder="Enter your remarks..."
                                value={adminRemark}
                                onChange={(event) =>
                                    setAdminRemark(event.target.value)
                                }
                                disabled={saving}
                            />

                            {actionError && (
                                <div className="leave-error-message">
                                    {actionError}
                                </div>
                            )}

                            <div className="leave-modal-actions">
                                <button
                                    type="button"
                                    className="leave-cancel-btn"
                                    onClick={closeDecisionModal}
                                    disabled={saving}
                                >
                                    Cancel
                                </button>

                                <button
                                    type="submit"
                                    className={
                                        decision === "APPROVED"
                                            ? "leave-confirm-approve"
                                            : "leave-confirm-reject"
                                    }
                                    disabled={saving}
                                >
                                    {saving
                                        ? "Saving..."
                                        : decision === "APPROVED"
                                            ? "Confirm Approval"
                                            : "Confirm Rejection"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}

export default LeaveManagement;