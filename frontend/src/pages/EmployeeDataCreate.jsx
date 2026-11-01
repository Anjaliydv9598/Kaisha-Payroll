
import React, {
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";

import "./EmployeeDataCreate.css";
import AdminLayout from "../pages/AdminLayout";

const API_BASE = import.meta.env.VITE_API_BASE_URL;

const EMPTY_DETAILS = {
    fullName: "",
    email: "",
    phone: "",
    designation: "",
    joiningDate: "",
    salary: "",
};

export default function EmployeeDataCreate() {
    const [employees, setEmployees] = useState([]);
    const [departments, setDepartments] = useState([]);
    const [department, setDepartment] = useState("");
    const [employeeId, setEmployeeId] = useState("");

    const [details, setDetails] = useState(EMPTY_DETAILS);

    const [editing, setEditing] = useState(false);
    const [editingDepartment, setEditingDepartment] = useState("");

    const [loading, setLoading] = useState(false);
    const [departmentsLoading, setDepartmentsLoading] = useState(false);
    const [saving, setSaving] = useState(false);

    const [error, setError] = useState("");
    const [message, setMessage] = useState("");

    const token = localStorage.getItem("token");

    const headers = useMemo(
        () => ({
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
        }),
        [token]
    );

    const request = useCallback(
        async (url, options = {}) => {
            const response = await fetch(`${API_BASE}${url}`, {
                ...options,
                headers: {
                    ...headers,
                    ...(options.headers || {}),
                },
            });

            const contentType =
                response.headers.get("content-type") || "";

            const result =
                response.status === 204
                    ? null
                    : contentType.includes("application/json")
                        ? await response.json()
                        : await response.text();

            if (!response.ok) {
                const reason =
                    typeof result === "string"
                        ? result
                        : result?.message ||
                        result?.error ||
                        `Request failed (${response.status})`;

                throw new Error(reason);
            }

            return result;
        },
        [headers]
    );

    /* ========================================================
       LOAD ACTIVE DEPARTMENTS FROM NUMBER SERIES
       ======================================================== */

    const loadDepartments = useCallback(async () => {
        setDepartmentsLoading(true);

        try {
            const response = await request("/employee-number-series");

            const list = Array.isArray(response)
                ? response
                : response?.content ||
                response?.data ||
                response?.items ||
                response?.series ||
                [];

            const activeDepartments = list
                .filter((item) => {
                    return (
                        item.active === true ||
                        item.active === "true"
                    );
                })
                .map((item) => {
                    const name =
                        item.department ||
                        item.departmentName ||
                        item.name;

                    if (!name) {
                        return null;
                    }

                    return {
                        id: String(
                            item.id ??
                            item.seriesId ??
                            name
                        ),
                        name: String(name).trim(),
                    };
                })
                .filter(Boolean);

            const uniqueDepartments = Array.from(
                new Map(
                    activeDepartments.map((item) => [
                        item.name.toLowerCase(),
                        item,
                    ])
                ).values()
            );

            setDepartments(uniqueDepartments);

            setDepartment((previous) => {
                const exists = uniqueDepartments.some(
                    (item) => item.name === previous
                );

                return exists
                    ? previous
                    : uniqueDepartments[0]?.name || "";
            });
        } catch (err) {
            setDepartments([]);
            setDepartment("");

            setError(
                err.message || "Could not load active departments."
            );
        } finally {
            setDepartmentsLoading(false);
        }
    }, [request]);

    /* ========================================================
       LOAD EMPLOYEE RECORDS
       ======================================================== */

    const loadEmployees = useCallback(async () => {
        setLoading(true);

        try {
            const result = await request("/employees");

            const list = Array.isArray(result)
                ? result
                : result?.content ||
                result?.data ||
                result?.items ||
                [];

            setEmployees(list);
        } catch (err) {
            setError(
                err.message || "Could not load employee records."
            );
        } finally {
            setLoading(false);
        }
    }, [request]);

    useEffect(() => {
        loadDepartments();
        loadEmployees();
    }, [loadDepartments, loadEmployees]);

    /* ========================================================
       RESET FORM
       ======================================================== */

    const resetForm = () => {
        setEmployeeId("");
        setDetails({ ...EMPTY_DETAILS });
        setEditing(false);
        setEditingDepartment("");
    };

    /* ========================================================
       RESERVE EXISTING EMPLOYEE ID
       ======================================================== */

    const handleReserve = async (event) => {
        event.preventDefault();

        setError("");
        setMessage("");

        if (!department) {
            setError("Please select a department.");
            return;
        }

        setSaving(true);

        try {
            const result = await request("/employees/reserve", {
                method: "POST",
                body: JSON.stringify({ department }),
            });

            const reservedId =
                result?.employeeId ??
                result?.empId ??
                result?.id;

            if (reservedId == null) {
                throw new Error(
                    "The backend did not return an employee ID."
                );
            }

            setEditing(false);
            setEditingDepartment("");
            setEmployeeId(String(reservedId));
            setDetails({ ...EMPTY_DETAILS });

            if (result?.department) {
                setDepartment(result.department);
            }

            setMessage(
                `Existing Employee ID ${reservedId} reserved successfully.`
            );

            await loadEmployees();
        } catch (err) {
            setError(
                err.message || "Could not reserve employee ID."
            );
        } finally {
            setSaving(false);
        }
    };

    /* ========================================================
       FORM FIELD CHANGES
       ======================================================== */

    const handleDetailChange = (event) => {
        const { name, value } = event.target;

        setDetails((previous) => ({
            ...previous,
            [name]: value,
        }));
    };

    /* ========================================================
       VALIDATE AND BUILD EMPLOYEE PAYLOAD
       ======================================================== */

    const buildPayload = () => {
        if (!details.fullName.trim()) {
            throw new Error("Please enter the employee's full name.");
        }

        if (!details.email.trim()) {
            throw new Error("Please enter the employee's email.");
        }

        if (!details.phone.trim()) {
            throw new Error("Please enter the employee's phone number.");
        }

        if (
            details.salary.trim() &&
            (!Number.isFinite(Number(details.salary)) ||
                Number(details.salary) < 0)
        ) {
            throw new Error("Please enter a valid salary.");
        }

        return {
            fullName: details.fullName.trim(),
            email: details.email.trim(),
            phone: details.phone.trim(),
            designation: details.designation.trim(),
            joiningDate: details.joiningDate || null,
            salary: details.salary.trim()
                ? Number(details.salary)
                : null,
        };
    };

    /* ========================================================
       SAVE NEW EMPLOYEE DETAILS
       ======================================================== */

    const handleComplete = async (event) => {
        event.preventDefault();

        setError("");
        setMessage("");

        if (!employeeId) {
            setError("Reserve an employee ID first.");
            return;
        }

        if (editing) {
            setError("Please finish or cancel the current edit first.");
            return;
        }

        let payload;

        try {
            payload = buildPayload();
        } catch (err) {
            setError(err.message);
            return;
        }

        setSaving(true);

        try {
            await request(
                `/employees/${encodeURIComponent(employeeId)}/complete`,
                {
                    method: "PUT",
                    body: JSON.stringify(payload),
                }
            );

            setMessage(
                `Employee ${employeeId} details saved successfully.`
            );

            resetForm();
            await loadEmployees();
        } catch (err) {
            setError(
                err.message || "Could not save employee details."
            );
        } finally {
            setSaving(false);
        }
    };

    /* ========================================================
       EMPLOYEE STATUS
       ======================================================== */

    const isCompleted = (employee) =>
        employee.detailsCompleted === true ||
        employee.detailsCompleted === "true";

    /* ========================================================
       GET DEPARTMENT NAME
       ======================================================== */

    const getDepartment = (employee) => {
        const value =
            employee.departmentName ||
            employee.department?.departmentName ||
            employee.department?.name ||
            employee.department ||
            "—";

        return typeof value === "object"
            ? value.name || "—"
            : value;
    };

    /* ========================================================
       EDIT EXISTING EMPLOYEE
       ======================================================== */

    const handleEdit = (employee) => {
        setError("");
        setMessage("");

        if (!isCompleted(employee)) {
            setError("Complete the employee details before editing.");
            return;
        }

        const id =
            employee.employeeId ??
            employee.empId;

        if (!id) {
            setError("Employee ID is missing.");
            return;
        }

        const recordDepartment = getDepartment(employee);

        setEmployeeId(String(id));
        setDepartment(recordDepartment);
        setEditingDepartment(recordDepartment);

        setDetails({
            fullName: employee.fullName || "",
            email: employee.email || "",
            phone: employee.phone || "",
            designation: employee.designation || "",
            joiningDate: employee.joiningDate
                ? String(employee.joiningDate).substring(0, 10)
                : "",
            salary:
                employee.salary !== null &&
                employee.salary !== undefined
                    ? String(employee.salary)
                    : "",
        });

        setEditing(true);

        window.scrollTo({
            top: 0,
            behavior: "smooth",
        });
    };

    /* ========================================================
       UPDATE EXISTING EMPLOYEE DETAILS
       ======================================================== */

    const handleUpdate = async (event) => {
        event.preventDefault();

        setError("");
        setMessage("");

        if (!employeeId || !editing) {
            setError("Select an employee to edit first.");
            return;
        }

        let payload;

        try {
            payload = buildPayload();
        } catch (err) {
            setError(err.message);
            return;
        }

        setSaving(true);

        try {
            await request(
                `/employees/${encodeURIComponent(employeeId)}/details`,
                {
                    method: "PUT",
                    body: JSON.stringify(payload),
                }
            );

            setMessage(
                `Employee ${employeeId} updated successfully.`
            );

            resetForm();
            await loadEmployees();
        } catch (err) {
            setError(
                err.message || "Could not update employee details."
            );
        } finally {
            setSaving(false);
        }
    };

    /* ========================================================
       REFRESH
       ======================================================== */

    const handleRefresh = () => {
        setError("");
        setMessage("");

        loadDepartments();
        loadEmployees();
    };

    /* ========================================================
       RENDER
       ======================================================== */

    return (
        <AdminLayout>
            <div className="employee-create-page">

                {/* PAGE HEADER */}

                <div className="employee-create-header">
                    <div>
                        <h1>Employee Data Create</h1>

                        <p>
                            Reserve an existing employee ID and
                            complete or update employee information.
                        </p>
                    </div>

                    <button
                        type="button"
                        className="employee-refresh-btn"
                        onClick={handleRefresh}
                        disabled={loading || departmentsLoading}
                    >
                        {loading || departmentsLoading
                            ? "Refreshing..."
                            : "Refresh"}
                    </button>
                </div>

                {/* ALERTS */}

                {error && (
                    <div
                        className="employee-alert employee-alert-error"
                        role="alert"
                    >
                        {error}
                    </div>
                )}

                {message && (
                    <div
                        className="employee-alert employee-alert-success"
                        role="status"
                    >
                        {message}
                    </div>
                )}

                {/* RESERVE EMPLOYEE ID */}

                <section className="employee-create-card">
                    <h2>1. Reserve Employee ID</h2>

                    <form onSubmit={handleReserve}>
                        <div className="employee-form-grid">

                            <div className="employee-form-group">
                                <label htmlFor="department">
                                    Department *
                                </label>

                                <select
                                    id="department"
                                    value={department}
                                    onChange={(event) => {
                                        setDepartment(event.target.value);
                                        setEmployeeId("");
                                        setDetails({ ...EMPTY_DETAILS });
                                        setEditing(false);
                                        setEditingDepartment("");
                                        setMessage("");
                                    }}
                                    disabled={
                                        departmentsLoading ||
                                        saving ||
                                        editing ||
                                        departments.length === 0
                                    }
                                    required
                                >
                                    <option value="">
                                        {departmentsLoading
                                            ? "Loading departments..."
                                            : departments.length === 0
                                                ? "No active departments available"
                                                : "Select department"}
                                    </option>

                                    {departments.map((item) => (
                                        <option
                                            key={item.id}
                                            value={item.name}
                                        >
                                            {item.name}
                                        </option>
                                    ))}
                                </select>

                                <small>
                                    Only departments with active number
                                    series are displayed.
                                </small>
                            </div>

                            <div className="employee-form-group">
                                <label htmlFor="reservedEmployeeId">
                                    {editing
                                        ? "Employee ID (Editing)"
                                        : "Reserved Employee ID"}
                                </label>

                                <input
                                    id="reservedEmployeeId"
                                    value={employeeId}
                                    placeholder="ID will appear here"
                                    readOnly
                                />

                                <small>
                                    {editing
                                        ? "Employee ID cannot be changed while editing."
                                        : "Reserve an existing ID before entering employee details."}
                                </small>
                            </div>
                        </div>

                        {!editing && (
                            <button
                                type="submit"
                                className="employee-primary-btn"
                                disabled={
                                    saving ||
                                    departmentsLoading ||
                                    !department
                                }
                            >
                                {saving
                                    ? "Reserving..."
                                    : "Reserve Employee ID"}
                            </button>
                        )}

                        {editing && (
                            <p className="employee-form-hint">
                                You are editing an existing employee.
                                Reserving another ID is disabled.
                            </p>
                        )}
                    </form>
                </section>

                {/* EMPLOYEE DETAILS FORM */}

                <section className="employee-create-card">
                    <h2>
                        {editing
                            ? "2. Edit Employee Details"
                            : "2. Complete Employee Details"}
                    </h2>

                    {editing && (
                        <div className="employee-edit-info">
                            <strong>Editing Employee:</strong> {employeeId}
                            <span>
                                Department: {editingDepartment}
                            </span>
                        </div>
                    )}

                    <form
                        onSubmit={editing ? handleUpdate : handleComplete}
                    >
                        <div className="employee-form-grid">

                            <div className="employee-form-group">
                                <label htmlFor="fullName">
                                    Full Name *
                                </label>

                                <input
                                    id="fullName"
                                    name="fullName"
                                    value={details.fullName}
                                    onChange={handleDetailChange}
                                    placeholder="Enter full name"
                                    required
                                />
                            </div>

                            <div className="employee-form-group">
                                <label htmlFor="email">Email *</label>

                                <input
                                    id="email"
                                    name="email"
                                    type="email"
                                    value={details.email}
                                    onChange={handleDetailChange}
                                    placeholder="employee@example.com"
                                    required
                                />
                            </div>

                            <div className="employee-form-group">
                                <label htmlFor="phone">Phone *</label>

                                <input
                                    id="phone"
                                    name="phone"
                                    type="tel"
                                    value={details.phone}
                                    onChange={handleDetailChange}
                                    placeholder="Enter phone number"
                                    required
                                />
                            </div>

                            <div className="employee-form-group">
                                <label htmlFor="designation">
                                    Designation
                                </label>

                                <input
                                    id="designation"
                                    name="designation"
                                    value={details.designation}
                                    onChange={handleDetailChange}
                                    placeholder="Enter designation"
                                />
                            </div>

                            <div className="employee-form-group">
                                <label htmlFor="joiningDate">
                                    Joining Date
                                </label>

                                <input
                                    id="joiningDate"
                                    name="joiningDate"
                                    type="date"
                                    value={details.joiningDate}
                                    onChange={handleDetailChange}
                                />
                            </div>

                            <div className="employee-form-group">
                                <label htmlFor="salary">
                                    Monthly Salary
                                </label>

                                <input
                                    id="salary"
                                    name="salary"
                                    type="number"
                                    min="0"
                                    step="0.01"
                                    value={details.salary}
                                    onChange={handleDetailChange}
                                    placeholder="Enter salary"
                                />
                            </div>
                        </div>

                        <div className="employee-form-actions">

                            <button
                                type="button"
                                className="employee-secondary-btn"
                                onClick={resetForm}
                                disabled={saving}
                            >
                                {editing ? "Cancel Edit" : "Clear"}
                            </button>

                            <button
                                type="submit"
                                className="employee-primary-btn"
                                disabled={saving || !employeeId}
                            >
                                {saving
                                    ? editing
                                        ? "Updating..."
                                        : "Saving..."
                                    : editing
                                        ? "Update Employee"
                                        : "Save Employee Details"}
                            </button>
                        </div>

                        {!employeeId && (
                            <small className="employee-form-hint">
                                Reserve an employee ID before saving details.
                            </small>
                        )}
                    </form>
                </section>

                {/* EMPLOYEE RECORDS TABLE */}

                <section className="employee-create-card">

                    <div className="employee-table-header">
                        <div>
                            <h2>Employee Records</h2>

                            <p>
                                Review, edit and manage employee information.
                            </p>
                        </div>

                        <span className="employee-record-count">
                            {employees.length} records
                        </span>
                    </div>

                    {loading ? (
                        <p className="employee-empty-state">
                            Loading employee records...
                        </p>
                    ) : employees.length === 0 ? (
                        <p className="employee-empty-state">
                            No employee records found.
                        </p>
                    ) : (
                        <div className="employee-table-wrapper">
                            <table className="employee-table">

                                <thead>
                                <tr>
                                    <th>Employee ID</th>
                                    <th>Department</th>
                                    <th>Full Name</th>
                                    <th>Email</th>
                                    <th>Phone</th>
                                    <th>Designation</th>
                                    <th>Status</th>
                                    <th>Action</th>
                                </tr>
                                </thead>

                                <tbody>
                                {employees.map((employee, index) => {
                                    const id =
                                        employee.employeeId ??
                                        employee.empId ??
                                        employee.id ??
                                        `record-${index}`;

                                    const completed =
                                        isCompleted(employee);

                                    return (
                                        <tr key={id}>
                                            <td>{id}</td>

                                            <td>
                                                {getDepartment(employee)}
                                            </td>

                                            <td>
                                                {employee.fullName || "—"}
                                            </td>

                                            <td>
                                                {employee.email || "—"}
                                            </td>

                                            <td>
                                                {employee.phone || "—"}
                                            </td>

                                            <td>
                                                {employee.designation || "—"}
                                            </td>

                                            <td>
                                                    <span
                                                        className={
                                                            completed
                                                                ? "employee-status employee-status-complete"
                                                                : "employee-status employee-status-pending"
                                                        }
                                                    >
                                                        {completed
                                                            ? "Completed"
                                                            : "Pending details"}
                                                    </span>
                                            </td>

                                            <td>
                                                {completed ? (
                                                    <button
                                                        type="button"
                                                        className="employee-edit-btn"
                                                        onClick={() =>
                                                            handleEdit(employee)
                                                        }
                                                        disabled={saving}
                                                    >
                                                        Edit
                                                    </button>
                                                ) : (
                                                    <span className="employee-action-unavailable">
                                                            Not available
                                                        </span>
                                                )}
                                            </td>
                                        </tr>
                                    );
                                })}
                                </tbody>
                            </table>
                        </div>
                    )}
                </section>
            </div>
        </AdminLayout>
    );
}
