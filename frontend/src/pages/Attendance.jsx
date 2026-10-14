import React, { useEffect, useMemo, useState } from "react";
import "./Attendance.css";

const API_URL = "http://localhost:8080/api/attendance";
const EMPLOYEE_API_URL = "http://localhost:8080/api/employees";

const STATUS_OPTIONS = [
    "PRESENT",
    "ABSENT",
    "HALF_DAY",
    "LEAVE",
    "HOLIDAY",
    "WEEK_OFF",
    "MISSING_PUNCH"
];

const formatStatus = (status) => {
    if (!status) return "—";

    return status
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(/\b\w/g, (letter) => letter.toUpperCase());
};

const formatTime = (time) => {
    if (!time) return "—";

    const parts = time.split(":");

    if (parts.length < 2) {
        return time;
    }

    let hour = Number(parts[0]);
    const minute = parts[1];

    const suffix = hour >= 12 ? "PM" : "AM";

    hour = hour % 12 || 12;

    return `${String(hour).padStart(2, "0")}:${minute} ${suffix}`;
};

const formatDate = (date) => {
    if (!date) return "—";

    const [year, month, day] = date.split("-");

    return `${day}/${month}/${year}`;
};

const getToday = () => {
    const date = new Date();

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
};

const getToken = () => {
    return localStorage.getItem("token");
};

const getStatusClass = (status) => {
    switch (status) {
        case "PRESENT":
            return "attendance-status present";

        case "ABSENT":
            return "attendance-status absent";

        case "HALF_DAY":
            return "attendance-status half-day";

        case "LEAVE":
            return "attendance-status leave";

        case "HOLIDAY":
            return "attendance-status holiday";

        case "WEEK_OFF":
            return "attendance-status week-off";

        case "MISSING_PUNCH":
            return "attendance-status missing-punch";

        default:
            return "attendance-status";
    }
};

function Attendance() {

    const [attendance, setAttendance] = useState([]);
    const [employees, setEmployees] = useState([]);

    const [loading, setLoading] = useState(true);
    const [employeeLoading, setEmployeeLoading] = useState(true);

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    const [search, setSearch] = useState("");
    const [departmentFilter, setDepartmentFilter] = useState("");
    const [statusFilter, setStatusFilter] = useState("");
    const [dateFilter, setDateFilter] = useState("");

    const [showModal, setShowModal] = useState(false);
    const [showViewModal, setShowViewModal] = useState(false);

    const [editingId, setEditingId] = useState(null);
    const [viewingRecord, setViewingRecord] = useState(null);

    const [deleteTarget, setDeleteTarget] = useState(null);

    const [saving, setSaving] = useState(false);
    const [deleting, setDeleting] = useState(false);

    const [form, setForm] = useState({
        employeeId: "",
        attendanceDate: getToday(),
        punchIn: "",
        punchOut: "",
        status: "PRESENT",
        correctionReason: ""
    });

    // ---------------------------------------------------------
    // LOAD ATTENDANCE
    // ---------------------------------------------------------

    const loadAttendance = async () => {

        const token = getToken();

        if (!token) {
            setError("Login session not found. Please login again.");
            setLoading(false);
            return;
        }

        setLoading(true);
        setError("");

        try {

            const response = await fetch(API_URL, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                    Accept: "application/json"
                }
            });

            if (!response.ok) {

                if (response.status === 401) {
                    throw new Error("Session expired. Please login again.");
                }

                if (response.status === 403) {
                    throw new Error(
                        "You do not have permission to view attendance."
                    );
                }

                throw new Error(
                    `Unable to load attendance. (${response.status})`
                );
            }

            const data = await response.json();

            setAttendance(Array.isArray(data) ? data : []);

        } catch (err) {

            console.error("LOAD ATTENDANCE ERROR:", err);

            setError(
                err.message || "Unable to load attendance."
            );

        } finally {

            setLoading(false);
        }
    };

    // ---------------------------------------------------------
    // LOAD EMPLOYEES
    // ---------------------------------------------------------

    const loadEmployees = async () => {

        const token = getToken();

        if (!token) {
            setEmployeeLoading(false);
            return;
        }

        setEmployeeLoading(true);

        try {

            const response = await fetch(
                EMPLOYEE_API_URL,
                {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${token}`,
                        Accept: "application/json"
                    }
                }
            );

            if (!response.ok) {
                throw new Error(
                    `Unable to load employees. (${response.status})`
                );
            }

            const data = await response.json();

            setEmployees(
                Array.isArray(data) ? data : []
            );

        } catch (err) {

            console.error(
                "LOAD EMPLOYEES ERROR:",
                err
            );

        } finally {

            setEmployeeLoading(false);
        }
    };

    useEffect(() => {

        loadAttendance();
        loadEmployees();

    }, []);

    // ---------------------------------------------------------
    // RESET FORM
    // ---------------------------------------------------------

    const resetForm = () => {

        setForm({
            employeeId: "",
            attendanceDate: getToday(),
            punchIn: "",
            punchOut: "",
            status: "PRESENT",
            correctionReason: ""
        });

        setEditingId(null);
    };

    // ---------------------------------------------------------
    // OPEN ADD
    // ---------------------------------------------------------

    const openAddModal = () => {

        clearMessages();

        resetForm();

        setShowModal(true);
    };

    // ---------------------------------------------------------
    // OPEN EDIT
    // ---------------------------------------------------------

    const openEditModal = (record) => {

        clearMessages();

        setEditingId(record.id);

        setForm({
            employeeId: record.employeeId || "",
            attendanceDate:
                record.attendanceDate || getToday(),

            punchIn:
                record.punchIn
                    ? record.punchIn.substring(0, 5)
                    : "",

            punchOut:
                record.punchOut
                    ? record.punchOut.substring(0, 5)
                    : "",

            status:
                record.status || "PRESENT",

            correctionReason:
                record.correctionReason || ""
        });

        setShowModal(true);
    };

    // ---------------------------------------------------------
    // VIEW
    // ---------------------------------------------------------

    const openViewModal = (record) => {

        clearMessages();

        setViewingRecord(record);

        setShowViewModal(true);
    };

    // ---------------------------------------------------------
    // FORM CHANGE
    // ---------------------------------------------------------

    const handleChange = (event) => {

        const {
            name,
            value
        } = event.target;

        setForm((previous) => ({
            ...previous,
            [name]: value
        }));
    };

    // ---------------------------------------------------------
    // SAVE
    // ---------------------------------------------------------

    const handleSubmit = async (event) => {

        event.preventDefault();

        clearMessages();

        const token = getToken();

        if (!token) {
            setError(
                "Login session not found. Please login again."
            );
            return;
        }

        if (!form.employeeId) {
            setError("Please select an employee.");
            return;
        }

        if (!form.attendanceDate) {
            setError("Please select an attendance date.");
            return;
        }

        if (
            form.punchIn &&
            form.punchOut &&
            form.punchOut < form.punchIn
        ) {
            setError(
                "Punch out cannot be before punch in."
            );
            return;
        }

        setSaving(true);

        const payload = {
            employeeId: form.employeeId,
            attendanceDate: form.attendanceDate,

            punchIn:
                form.punchIn
                    ? `${form.punchIn}:00`
                    : null,

            punchOut:
                form.punchOut
                    ? `${form.punchOut}:00`
                    : null,

            status: form.status,

            correctionReason:
                form.correctionReason.trim()
                    ? form.correctionReason.trim()
                    : null
        };

        try {

            const url = editingId
                ? `${API_URL}/${editingId}`
                : API_URL;

            const response = await fetch(
                url,
                {
                    method: editingId
                        ? "PUT"
                        : "POST",

                    headers: {
                        Authorization: `Bearer ${token}`,
                        Accept: "application/json",
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify(payload)
                }
            );

            if (!response.ok) {

                let message =
                    "Unable to save attendance.";

                try {

                    const body =
                        await response.json();

                    message =
                        body.message ||
                        body.error ||
                        message;

                } catch {
                    // response may not be JSON
                }

                throw new Error(
                    `${message} (${response.status})`
                );
            }

            await response.json();

            setShowModal(false);

            resetForm();

            setSuccess(
                editingId
                    ? "Attendance updated successfully."
                    : "Attendance created successfully."
            );

            await loadAttendance();

        } catch (err) {

            console.error(
                "SAVE ATTENDANCE ERROR:",
                err
            );

            setError(
                err.message ||
                "Unable to save attendance."
            );

        } finally {

            setSaving(false);
        }
    };

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------

    const handleDelete = async () => {

        if (!deleteTarget) {
            return;
        }

        const token = getToken();

        if (!token) {
            setError(
                "Login session not found. Please login again."
            );
            return;
        }

        setDeleting(true);
        clearMessages();

        try {

            const response = await fetch(
                `${API_URL}/${deleteTarget.id}`,
                {
                    method: "DELETE",
                    headers: {
                        Authorization: `Bearer ${token}`,
                        Accept: "application/json"
                    }
                }
            );

            if (!response.ok) {

                throw new Error(
                    `Unable to delete attendance. (${response.status})`
                );
            }

            setDeleteTarget(null);

            setSuccess(
                "Attendance deleted successfully."
            );

            await loadAttendance();

        } catch (err) {

            console.error(
                "DELETE ATTENDANCE ERROR:",
                err
            );

            setError(
                err.message ||
                "Unable to delete attendance."
            );

        } finally {

            setDeleting(false);
        }
    };

    // ---------------------------------------------------------
    // PUNCH IN
    // ---------------------------------------------------------

    const handlePunchIn = async (employeeId) => {

        if (!employeeId) {
            setError("Employee ID is required.");
            return;
        }

        const token = getToken();

        if (!token) {
            setError(
                "Login session not found. Please login again."
            );
            return;
        }

        clearMessages();

        try {

            const response = await fetch(
                `${API_URL}/punch-in/${encodeURIComponent(employeeId)}`,
                {
                    method: "POST",
                    headers: {
                        Authorization: `Bearer ${token}`,
                        Accept: "application/json"
                    }
                }
            );

            if (!response.ok) {

                let message =
                    "Unable to punch in.";

                try {

                    const body =
                        await response.json();

                    message =
                        body.message ||
                        body.error ||
                        message;

                } catch {
                    // ignore
                }

                throw new Error(
                    `${message} (${response.status})`
                );
            }

            setSuccess(
                `${employeeId} punched in successfully.`
            );

            await loadAttendance();

        } catch (err) {

            console.error(
                "PUNCH IN ERROR:",
                err
            );

            setError(
                err.message ||
                "Unable to punch in."
            );
        }
    };

    // ---------------------------------------------------------
    // PUNCH OUT
    // ---------------------------------------------------------

    const handlePunchOut = async (employeeId) => {

        if (!employeeId) {
            setError("Employee ID is required.");
            return;
        }

        const token = getToken();

        if (!token) {
            setError(
                "Login session not found. Please login again."
            );
            return;
        }

        clearMessages();

        try {

            const response = await fetch(
                `${API_URL}/punch-out/${encodeURIComponent(employeeId)}`,
                {
                    method: "POST",
                    headers: {
                        Authorization: `Bearer ${token}`,
                        Accept: "application/json"
                    }
                }
            );

            if (!response.ok) {

                let message =
                    "Unable to punch out.";

                try {

                    const body =
                        await response.json();

                    message =
                        body.message ||
                        body.error ||
                        message;

                } catch {
                    // ignore
                }

                throw new Error(
                    `${message} (${response.status})`
                );
            }

            setSuccess(
                `${employeeId} punched out successfully.`
            );

            await loadAttendance();

        } catch (err) {

            console.error(
                "PUNCH OUT ERROR:",
                err
            );

            setError(
                err.message ||
                "Unable to punch out."
            );
        }
    };

    // ---------------------------------------------------------
    // FILTER DATA
    // ---------------------------------------------------------

    const departments = useMemo(() => {

        const values = attendance
            .map((record) => record.department)
            .filter(Boolean);

        return [...new Set(values)].sort();

    }, [attendance]);

    const filteredAttendance = useMemo(() => {

        const searchValue =
            search.trim().toLowerCase();

        return attendance.filter((record) => {

            const employeeId =
                String(
                    record.employeeId || ""
                ).toLowerCase();

            const employeeName =
                String(
                    record.employeeName || ""
                ).toLowerCase();

            const department =
                String(
                    record.department || ""
                ).toLowerCase();

            const matchesSearch =
                !searchValue ||
                employeeId.includes(searchValue) ||
                employeeName.includes(searchValue);

            const matchesDepartment =
                !departmentFilter ||
                department ===
                departmentFilter.toLowerCase();

            const matchesStatus =
                !statusFilter ||
                record.status === statusFilter;

            const matchesDate =
                !dateFilter ||
                record.attendanceDate === dateFilter;

            return (
                matchesSearch &&
                matchesDepartment &&
                matchesStatus &&
                matchesDate
            );
        });

    }, [
        attendance,
        search,
        departmentFilter,
        statusFilter,
        dateFilter
    ]);

    // ---------------------------------------------------------
    // STATISTICS
    // ---------------------------------------------------------

    const statistics = useMemo(() => {

        const present =
            attendance.filter(
                (item) =>
                    item.status === "PRESENT"
            ).length;

        const absent =
            attendance.filter(
                (item) =>
                    item.status === "ABSENT"
            ).length;

        const missingPunch =
            attendance.filter(
                (item) =>
                    item.status === "MISSING_PUNCH"
            ).length;

        return {
            total: attendance.length,
            present,
            absent,
            missingPunch
        };

    }, [attendance]);

    // ---------------------------------------------------------
    // CLEAR MESSAGES
    // ---------------------------------------------------------

    const clearMessages = () => {
        setError("");
        setSuccess("");
    };

    // ---------------------------------------------------------
    // RENDER
    // ---------------------------------------------------------

    return (
        <div className="attendance-page">

            {/* HEADER */}

            <div className="attendance-breadcrumb">
                Administration
                <span>/</span>
                Attendance
            </div>

            <div className="attendance-header">

                <div>
                    <h1>Attendance</h1>

                    <p>
                        Manage employee attendance,
                        punch records and corrections.
                    </p>
                </div>

                <div className="attendance-header-actions">

                    <button
                        type="button"
                        className="attendance-refresh-btn"
                        onClick={() => {
                            clearMessages();
                            loadAttendance();
                        }}
                    >
                        ↻ Refresh
                    </button>

                    <button
                        type="button"
                        className="attendance-add-btn"
                        onClick={openAddModal}
                    >
                        <span>+</span>
                        Add Attendance
                    </button>

                </div>

            </div>

            {/* MESSAGES */}

            {success && (
                <div className="attendance-alert success">
                    <span>✓</span>
                    {success}
                    <button
                        type="button"
                        onClick={() => setSuccess("")}
                    >
                        ×
                    </button>
                </div>
            )}

            {error && (
                <div className="attendance-alert error">
                    <span>!</span>
                    {error}
                    <button
                        type="button"
                        onClick={() => setError("")}
                    >
                        ×
                    </button>
                </div>
            )}

            {/* STATISTICS */}

            <div className="attendance-stat-grid">

                <div className="attendance-stat-card">

                    <div className="stat-icon total">
                        👥
                    </div>

                    <div>
                        <div className="stat-label">
                            Attendance Records
                        </div>

                        <div className="stat-value">
                            {statistics.total}
                        </div>
                    </div>

                </div>

                <div className="attendance-stat-card">

                    <div className="stat-icon present">
                        ✓
                    </div>

                    <div>
                        <div className="stat-label">
                            Present
                        </div>

                        <div className="stat-value">
                            {statistics.present}
                        </div>
                    </div>

                </div>

                <div className="attendance-stat-card">

                    <div className="stat-icon absent">
                        ×
                    </div>

                    <div>
                        <div className="stat-label">
                            Absent
                        </div>

                        <div className="stat-value">
                            {statistics.absent}
                        </div>
                    </div>

                </div>

                <div className="attendance-stat-card">

                    <div className="stat-icon missing">
                        !
                    </div>

                    <div>
                        <div className="stat-label">
                            Missing Punch
                        </div>

                        <div className="stat-value">
                            {statistics.missingPunch}
                        </div>
                    </div>

                </div>

            </div>

            {/* TABLE CARD */}

            <div className="attendance-card">

                {/* FILTER BAR */}

                <div className="attendance-filter-bar">

                    <div className="attendance-search">

                        <span className="search-icon">
                            ⌕
                        </span>

                        <input
                            type="text"
                            placeholder="Search by employee ID or name"
                            value={search}
                            onChange={(event) =>
                                setSearch(
                                    event.target.value
                                )
                            }
                        />

                    </div>

                    <input
                        type="date"
                        className="attendance-date-filter"
                        value={dateFilter}
                        onChange={(event) =>
                            setDateFilter(
                                event.target.value
                            )
                        }
                    />

                    <select
                        value={departmentFilter}
                        onChange={(event) =>
                            setDepartmentFilter(
                                event.target.value
                            )
                        }
                    >
                        <option value="">
                            All Departments
                        </option>

                        {departments.map(
                            (department) => (
                                <option
                                    key={department}
                                    value={department}
                                >
                                    {department}
                                </option>
                            )
                        )}
                    </select>

                    <select
                        value={statusFilter}
                        onChange={(event) =>
                            setStatusFilter(
                                event.target.value
                            )
                        }
                    >
                        <option value="">
                            All Status
                        </option>

                        {STATUS_OPTIONS.map(
                            (status) => (
                                <option
                                    key={status}
                                    value={status}
                                >
                                    {formatStatus(status)}
                                </option>
                            )
                        )}
                    </select>

                </div>

                {/* TABLE */}

                <div className="attendance-table-wrapper">

                    <table className="attendance-table">

                        <thead>

                        <tr>

                            <th>EMPLOYEE</th>

                            <th>DEPARTMENT</th>

                            <th>DATE</th>

                            <th>PUNCH IN</th>

                            <th>PUNCH OUT</th>

                            <th>WORKING HOURS</th>

                            <th>STATUS</th>

                            <th>ACTION</th>

                        </tr>

                        </thead>

                        <tbody>

                        {loading ? (

                            <tr>

                                <td
                                    colSpan="8"
                                    className="attendance-empty"
                                >
                                    <div className="loading-spinner">
                                    </div>

                                    Loading attendance...
                                </td>

                            </tr>

                        ) : filteredAttendance.length === 0 ? (

                            <tr>

                                <td
                                    colSpan="8"
                                    className="attendance-empty"
                                >
                                    <div className="empty-icon">
                                        📅
                                    </div>

                                    <strong>
                                        No attendance records found
                                    </strong>

                                    <span>
                                            Add an attendance
                                            record or change
                                            your filters.
                                        </span>

                                </td>

                            </tr>

                        ) : (

                            filteredAttendance.map(
                                (record) => (

                                    <tr
                                        key={record.id}
                                    >

                                        <td>

                                            <div className="employee-cell">

                                                <div className="employee-avatar">
                                                    {(
                                                        record.employeeId ||
                                                        "E"
                                                    )
                                                        .charAt(0)
                                                        .toUpperCase()}
                                                </div>

                                                <div>

                                                    <div className="employee-id">
                                                        {record.employeeId ||
                                                            "—"}
                                                    </div>

                                                    <div className="employee-name">
                                                        {record.employeeName ||
                                                            "Employee"}
                                                    </div>

                                                </div>

                                            </div>

                                        </td>

                                        <td>

                                            {record.department ? (
                                                <span className="department-badge">
                                                        {
                                                            record.department
                                                        }
                                                    </span>
                                            ) : (
                                                <span className="dash">
                                                        —
                                                    </span>
                                            )}

                                        </td>

                                        <td>
                                            {formatDate(
                                                record.attendanceDate
                                            )}
                                        </td>

                                        <td>
                                            {formatTime(
                                                record.punchIn
                                            )}
                                        </td>

                                        <td>
                                            {formatTime(
                                                record.punchOut
                                            )}
                                        </td>

                                        <td>

                                            {record.workingHours !==
                                            null &&
                                            record.workingHours !==
                                            undefined
                                                ? `${record.workingHours} hrs`
                                                : "—"}

                                        </td>

                                        <td>

                                                <span
                                                    className={getStatusClass(
                                                        record.status
                                                    )}
                                                >

                                                    <span className="status-dot">
                                                    </span>

                                                    {formatStatus(
                                                        record.status
                                                    )}

                                                </span>

                                        </td>

                                        <td>

                                            <div className="action-buttons">

                                                <button
                                                    type="button"
                                                    className="view-btn"
                                                    onClick={() =>
                                                        openViewModal(
                                                            record
                                                        )
                                                    }
                                                >
                                                    View
                                                </button>

                                                <button
                                                    type="button"
                                                    className="edit-btn"
                                                    onClick={() =>
                                                        openEditModal(
                                                            record
                                                        )
                                                    }
                                                >
                                                    Edit
                                                </button>

                                                <button
                                                    type="button"
                                                    className="delete-btn"
                                                    onClick={() =>
                                                        setDeleteTarget(
                                                            record
                                                        )
                                                    }
                                                >
                                                    Delete
                                                </button>

                                            </div>

                                        </td>

                                    </tr>

                                )
                            )
                        )}

                        </tbody>

                    </table>

                </div>

                {/* TABLE FOOTER */}

                {!loading &&
                    filteredAttendance.length > 0 && (
                        <div className="attendance-table-footer">

                            Showing{" "}
                            <strong>
                                {filteredAttendance.length}
                            </strong>{" "}
                            of{" "}
                            <strong>
                                {attendance.length}
                            </strong>{" "}
                            attendance records

                        </div>
                    )}

            </div>

            {/* ADD / EDIT MODAL */}

            {showModal && (

                <div
                    className="attendance-modal-overlay"
                    onMouseDown={(event) => {

                        if (
                            event.target ===
                            event.currentTarget
                        ) {
                            setShowModal(false);
                        }

                    }}
                >

                    <div className="attendance-modal">

                        <div className="attendance-modal-header">

                            <div>

                                <h2>
                                    {editingId
                                        ? "Edit Attendance"
                                        : "Add Attendance"}
                                </h2>

                                <p>
                                    {editingId
                                        ? "Update the employee attendance record."
                                        : "Create a new employee attendance record."}
                                </p>

                            </div>

                            <button
                                type="button"
                                className="modal-close"
                                onClick={() =>
                                    setShowModal(false)
                                }
                            >
                                ×
                            </button>

                        </div>

                        <form
                            onSubmit={handleSubmit}
                            className="attendance-form"
                        >

                            <div className="form-grid">

                                <div className="form-group">

                                    <label>
                                        Employee
                                        <span>*</span>
                                    </label>

                                    <select
                                        name="employeeId"
                                        value={form.employeeId}
                                        onChange={handleChange}
                                        disabled={
                                            employeeLoading
                                        }
                                        required
                                    >

                                        <option value="">
                                            {employeeLoading
                                                ? "Loading employees..."
                                                : "Select Employee"}
                                        </option>

                                        {employees.map(
                                            (employee) => {

                                                const employeeId =
                                                    employee.employeeId;

                                                if (!employeeId) {
                                                    return null;
                                                }

                                                return (
                                                    <option
                                                        key={
                                                            employee.id ||
                                                            employeeId
                                                        }
                                                        value={
                                                            employeeId
                                                        }
                                                    >
                                                        {employeeId}
                                                        {employee.name
                                                            ? ` - ${employee.name}`
                                                            : employee.employeeName
                                                                ? ` - ${employee.employeeName}`
                                                                : ""}
                                                    </option>
                                                );
                                            }
                                        )}

                                    </select>

                                </div>

                                <div className="form-group">

                                    <label>
                                        Attendance Date
                                        <span>*</span>
                                    </label>

                                    <input
                                        type="date"
                                        name="attendanceDate"
                                        value={
                                            form.attendanceDate
                                        }
                                        onChange={handleChange}
                                        required
                                    />

                                </div>

                                <div className="form-group">

                                    <label>
                                        Punch In
                                    </label>

                                    <input
                                        type="time"
                                        name="punchIn"
                                        value={
                                            form.punchIn
                                        }
                                        onChange={handleChange}
                                    />

                                </div>

                                <div className="form-group">

                                    <label>
                                        Punch Out
                                    </label>

                                    <input
                                        type="time"
                                        name="punchOut"
                                        value={
                                            form.punchOut
                                        }
                                        onChange={handleChange}
                                    />

                                </div>

                                <div className="form-group">

                                    <label>
                                        Status
                                    </label>

                                    <select
                                        name="status"
                                        value={form.status}
                                        onChange={handleChange}
                                    >

                                        {STATUS_OPTIONS.map(
                                            (status) => (
                                                <option
                                                    key={status}
                                                    value={status}
                                                >
                                                    {formatStatus(
                                                        status
                                                    )}
                                                </option>
                                            )
                                        )}

                                    </select>

                                </div>

                                <div className="form-group full-width">

                                    <label>
                                        Correction Reason
                                    </label>

                                    <textarea
                                        name="correctionReason"
                                        value={
                                            form.correctionReason
                                        }
                                        onChange={handleChange}
                                        placeholder="Enter reason if attendance is being corrected..."
                                        rows="3"
                                    />

                                </div>

                            </div>

                            <div className="attendance-form-note">

                                <strong>
                                    Note:
                                </strong>{" "}
                                If Punch In or Punch Out is
                                missing, the record can be
                                corrected later by Admin.

                            </div>

                            <div className="attendance-modal-footer">

                                <button
                                    type="button"
                                    className="cancel-btn"
                                    onClick={() =>
                                        setShowModal(false)
                                    }
                                    disabled={saving}
                                >
                                    Cancel
                                </button>

                                <button
                                    type="submit"
                                    className="save-btn"
                                    disabled={saving}
                                >

                                    {saving
                                        ? "Saving..."
                                        : editingId
                                            ? "Update Attendance"
                                            : "Save Attendance"}

                                </button>

                            </div>

                        </form>

                    </div>

                </div>
            )}

            {/* VIEW MODAL */}

            {showViewModal &&
                viewingRecord && (

                    <div
                        className="attendance-modal-overlay"
                        onMouseDown={(event) => {

                            if (
                                event.target ===
                                event.currentTarget
                            ) {
                                setShowViewModal(false);
                            }

                        }}
                    >

                        <div className="attendance-modal view-modal">

                            <div className="attendance-modal-header">

                                <div>

                                    <h2>
                                        Attendance Details
                                    </h2>

                                    <p>
                                        Complete attendance
                                        information.
                                    </p>

                                </div>

                                <button
                                    type="button"
                                    className="modal-close"
                                    onClick={() =>
                                        setShowViewModal(
                                            false
                                        )
                                    }
                                >
                                    ×
                                </button>

                            </div>

                            <div className="attendance-details">

                                <div className="detail-item">
                                    <span>
                                        Employee ID
                                    </span>

                                    <strong>
                                        {viewingRecord.employeeId ||
                                            "—"}
                                    </strong>
                                </div>

                                <div className="detail-item">
                                    <span>
                                        Employee Name
                                    </span>

                                    <strong>
                                        {viewingRecord.employeeName ||
                                            "—"}
                                    </strong>
                                </div>

                                <div className="detail-item">
                                    <span>
                                        Department
                                    </span>

                                    <strong>
                                        {viewingRecord.department ||
                                            "—"}
                                    </strong>
                                </div>

                                <div className="detail-item">
                                    <span>
                                        Date
                                    </span>

                                    <strong>
                                        {formatDate(
                                            viewingRecord.attendanceDate
                                        )}
                                    </strong>
                                </div>

                                <div className="detail-item">
                                    <span>
                                        Punch In
                                    </span>

                                    <strong>
                                        {formatTime(
                                            viewingRecord.punchIn
                                        )}
                                    </strong>
                                </div>

                                <div className="detail-item">
                                    <span>
                                        Punch Out
                                    </span>

                                    <strong>
                                        {formatTime(
                                            viewingRecord.punchOut
                                        )}
                                    </strong>
                                </div>

                                <div className="detail-item">
                                    <span>
                                        Working Hours
                                    </span>

                                    <strong>
                                        {viewingRecord.workingHours !==
                                        null &&
                                        viewingRecord.workingHours !==
                                        undefined
                                            ? `${viewingRecord.workingHours} hrs`
                                            : "—"}
                                    </strong>
                                </div>

                                <div className="detail-item">
                                    <span>
                                        Status
                                    </span>

                                    <strong>

                                        <span
                                            className={getStatusClass(
                                                viewingRecord.status
                                            )}
                                        >
                                            <span className="status-dot">
                                            </span>

                                            {formatStatus(
                                                viewingRecord.status
                                            )}
                                        </span>

                                    </strong>
                                </div>

                                <div className="detail-item full-width">
                                    <span>
                                        Correction Reason
                                    </span>

                                    <strong>
                                        {viewingRecord.correctionReason ||
                                            "No correction reason"}
                                    </strong>
                                </div>

                            </div>

                            <div className="attendance-modal-footer">

                                <button
                                    type="button"
                                    className="cancel-btn"
                                    onClick={() =>
                                        setShowViewModal(
                                            false
                                        )
                                    }
                                >
                                    Close
                                </button>

                                <button
                                    type="button"
                                    className="save-btn"
                                    onClick={() => {

                                        setShowViewModal(
                                            false
                                        );

                                        openEditModal(
                                            viewingRecord
                                        );

                                    }}
                                >
                                    Edit Record
                                </button>

                            </div>

                        </div>

                    </div>
                )}

            {/* DELETE MODAL */}

            {deleteTarget && (

                <div
                    className="attendance-modal-overlay"
                    onMouseDown={(event) => {

                        if (
                            event.target ===
                            event.currentTarget
                        ) {
                            setDeleteTarget(null);
                        }

                    }}
                >

                    <div className="delete-modal">

                        <div className="delete-icon">
                            !
                        </div>

                        <h2>
                            Delete Attendance?
                        </h2>

                        <p>
                            Are you sure you want to
                            delete the attendance record
                            for{" "}
                            <strong>
                                {deleteTarget.employeeId}
                            </strong>{" "}
                            on{" "}
                            <strong>
                                {formatDate(
                                    deleteTarget.attendanceDate
                                )}
                            </strong>
                            ?
                        </p>

                        <div className="delete-modal-actions">

                            <button
                                type="button"
                                className="cancel-btn"
                                onClick={() =>
                                    setDeleteTarget(
                                        null
                                    )
                                }
                                disabled={deleting}
                            >
                                Cancel
                            </button>

                            <button
                                type="button"
                                className="confirm-delete-btn"
                                onClick={handleDelete}
                                disabled={deleting}
                            >
                                {deleting
                                    ? "Deleting..."
                                    : "Delete"}
                            </button>

                        </div>

                    </div>

                </div>
            )}

        </div>
    );
}

export default Attendance;