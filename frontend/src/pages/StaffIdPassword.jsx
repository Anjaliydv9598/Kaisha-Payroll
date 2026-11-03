
import React, { useEffect, useState } from "react";
import "./StaffIdPassword.css";
import AdminLayout from "../pages/AdminLayout";
import { useNavigate } from "react-router-dom";

// ==========================================
// API CONFIGURATION
// ==========================================

const API_BASE_URL = (
    import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api"
).replace(/\/+$/, "");

const STAFF_API = `${API_BASE_URL}/auth/staff`;

const getToken = () => localStorage.getItem("token");

// ==========================================
// RESPONSE HANDLER
// ==========================================

const readResponse = async (response) => {
    const text = await response.text();

    if (!text) return {};

    try {
        return JSON.parse(text);
    } catch {
        return { message: text };
    }
};

const getErrorMessage = (data, fallback) => {
    if (typeof data === "string") return data;

    return (
        data?.message ||
        data?.detail ||
        data?.error ||
        fallback
    );
};

// ==========================================
// MAIN COMPONENT
// ==========================================

const StaffIdPassword = () => {

    const navigate = useNavigate();

    // Employee data
    const [employees, setEmployees] = useState([]);

    // Loading states
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    // Modal state
    const [showForm, setShowForm] = useState(false);

    // Password visibility
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);

    // Form data
    const [formData, setFormData] = useState({
        employeeId: "",
        password: "",
        confirmPassword: ""
    });

    // Messages
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    // ==========================================
    // 1. LOAD EXISTING EMPLOYEES
    // ==========================================

    const loadEmployees = async () => {

        try {

            setLoading(true);

            const token = getToken();

            if (!token) {
                throw new Error(
                    "Authentication token not found. Please log in again."
                );
            }

            const response = await fetch(
                `${STAFF_API}/employees`,
                {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                }
            );

            const data = await readResponse(response);

            if (!response.ok) {
                throw new Error(
                    getErrorMessage(data, "Unable to load employees.")
                );
            }

            setEmployees(Array.isArray(data) ? data : []);
            setError("");

        } catch (err) {

            setError(err.message || "Unable to load employees.");

        } finally {

            setLoading(false);

        }
    };

    useEffect(() => {
        loadEmployees();
    }, []);

    // ==========================================
    // 2. SELECTED EMPLOYEE
    // ==========================================

    const selectedEmployee = employees.find(
        employee =>
            employee.employeeId === formData.employeeId
    );

    // ==========================================
    // 3. HANDLE INPUT CHANGES
    // ==========================================

    const handleChange = (event) => {

        const { name, value } = event.target;

        setFormData(previous => ({
            ...previous,
            [name]: value
        }));
    };

    // ==========================================
    // 4. RESET FORM
    // ==========================================

    const resetForm = () => {

        setFormData({
            employeeId: "",
            password: "",
            confirmPassword: ""
        });

        setShowPassword(false);
        setShowConfirmPassword(false);

        setError("");
    };

    // ==========================================
    // 5. NAVIGATE TO MASTER DATA
    // ==========================================

    const openCreateForm = () => {

        navigate("/admin/master-data", {
            state: {
                openAddSeries: true
            }
        });
    };

    // ==========================================
    // 6. OPEN STAFF LOGIN FORM
    // ==========================================

    const openStaffForm = (employeeId) => {

        resetForm();

        setSuccess("");

        setFormData({
            employeeId,
            password: "",
            confirmPassword: ""
        });

        setShowForm(true);
    };

    // ==========================================
    // 7. CLOSE STAFF LOGIN FORM
    // ==========================================

    const closeStaffForm = () => {

        if (saving) return;

        setShowForm(false);

        resetForm();
    };

    // ==========================================
    // 8. CREATE STAFF LOGIN
    // ==========================================

    const handleCreateStaff = async (event) => {

        event.preventDefault();

        setError("");
        setSuccess("");

        // Employee validation
        if (!formData.employeeId) {

            setError("Employee ID is required.");

            return;
        }

        // Password validation
        if (!formData.password || !formData.confirmPassword) {

            setError("Please enter and confirm the password.");

            return;
        }

        if (formData.password.length < 8) {

            setError("Password must contain at least 8 characters.");

            return;
        }

        if (formData.password !== formData.confirmPassword) {

            setError("Password and confirm password do not match.");

            return;
        }

        // Employee login validation
        if (!selectedEmployee) {

            setError("Selected employee was not found.");

            return;
        }

        if (selectedEmployee.hasLogin) {

            setError("This employee already has a login account.");

            return;
        }

        try {

            setSaving(true);

            const token = getToken();

            if (!token) {

                throw new Error(
                    "Authentication token not found. Please log in again."
                );
            }

            // Send only Employee ID and password details.
            // Employee details are retrieved by the backend.

            const response = await fetch(
                STAFF_API,
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`
                    },

                    body: JSON.stringify({
                        employeeId: formData.employeeId,
                        password: formData.password,
                        confirmPassword: formData.confirmPassword
                    })
                }
            );

            const data = await readResponse(response);

            if (!response.ok) {

                throw new Error(
                    getErrorMessage(
                        data,
                        "Unable to create staff account."
                    )
                );
            }

            // Success
            const createdEmployeeId = formData.employeeId;

            setSuccess(
                `Login credentials created successfully for ${createdEmployeeId}.`
            );

            setShowForm(false);

            resetForm();

            // Refresh employee login status
            await loadEmployees();

        } catch (err) {

            setError(
                err.message || "Unable to create staff account."
            );

        } finally {

            setSaving(false);

        }
    };

    // ==========================================
    // 9. EMPLOYEE LOGIN COUNTS
    // ==========================================

    const createdCount = employees.filter(
        employee => employee.hasLogin
    ).length;

    // ==========================================
    // 10. UI
    // ==========================================

    return (

        <AdminLayout>

            <div className="staff-page">

                {/* PAGE HEADER */}

                <div className="staff-header">

                    <div>
                        <h1>Staff ID & Password</h1>

                        <p>
                            Create login credentials for existing employees.
                        </p>
                    </div>

                    <button
                        type="button"
                        className="staff-add-button"
                        onClick={openCreateForm}
                    >
                        + Create Staff
                    </button>

                </div>

                {/* SUCCESS MESSAGE */}

                {success && (
                    <div className="staff-success">
                        {success}
                    </div>
                )}

                {/* ERROR MESSAGE */}

                {error && !showForm && (
                    <div className="staff-error">
                        {error}
                    </div>
                )}

                {/* EMPLOYEE ACCOUNT CARD */}

                <div className="staff-card">

                    <div className="staff-card-header">

                        <div>
                            <h2>Employee Login Accounts</h2>

                            <p>
                                Manage login access for existing employees.
                            </p>
                        </div>

                        <span>
                            {createdCount} / {employees.length} created
                        </span>

                    </div>

                    {/* LOADING */}

                    {loading ? (

                        <div className="staff-state">
                            Loading employees...
                        </div>

                    ) : employees.length === 0 ? (

                        <div className="staff-state">
                            No active employees found.
                        </div>

                    ) : (

                        <div className="staff-table-wrapper">

                            <table className="staff-table">

                                <thead>
                                <tr>
                                    <th>Employee ID</th>
                                    <th>Employee Name</th>
                                    <th>Email</th>
                                    <th>Mobile Number</th>
                                    <th>Login Status</th>
                                    <th>Action</th>
                                </tr>
                                </thead>

                                <tbody>

                                {employees.map(employee => (

                                    <tr key={employee.id}>

                                        <td>
                                            <strong>
                                                {employee.employeeId}
                                            </strong>
                                        </td>

                                        <td>
                                            {employee.employeeName || "Not Available"}
                                        </td>

                                        <td>
                                            {employee.email || "Not Available"}
                                        </td>

                                        <td>
                                            {employee.mobileNumber || "Not Available"}
                                        </td>

                                        <td>

                                                <span
                                                    className={
                                                        employee.hasLogin
                                                            ? "staff-status active"
                                                            : "staff-status inactive"
                                                    }
                                                >
                                                    {employee.hasLogin
                                                        ? "Already Created"
                                                        : "Not Created"}
                                                </span>

                                        </td>

                                        <td>

                                            {employee.hasLogin ? (

                                                <span className="staff-role">
                                                        Login Exists
                                                    </span>

                                            ) : (

                                                <button
                                                    type="button"
                                                    className="staff-reset-button"
                                                    onClick={() =>
                                                        openStaffForm(
                                                            employee.employeeId
                                                        )
                                                    }
                                                >
                                                    Create Login
                                                </button>

                                            )}

                                        </td>

                                    </tr>

                                ))}

                                </tbody>

                            </table>

                        </div>

                    )}

                </div>

                {/* CREATE STAFF LOGIN MODAL */}

                {showForm && (

                    <div className="staff-modal-overlay">

                        <div className="staff-modal">

                            {/* MODAL HEADER */}

                            <div className="staff-modal-header">

                                <div>
                                    <h2>Create Staff Login</h2>

                                    <p>
                                        Set login credentials for an existing employee.
                                    </p>
                                </div>

                                <button
                                    type="button"
                                    className="staff-close-button"
                                    onClick={closeStaffForm}
                                    disabled={saving}
                                >
                                    ×
                                </button>

                            </div>

                            {/* FORM */}

                            <form
                                className="staff-form"
                                onSubmit={handleCreateStaff}
                            >

                                {error && (
                                    <div className="staff-form-error">
                                        {error}
                                    </div>
                                )}

                                <div className="staff-form-grid">

                                    {/* EMPLOYEE ID */}

                                    <div className="staff-field">

                                        <label>Employee ID</label>

                                        <input
                                            type="text"
                                            value={formData.employeeId}
                                            readOnly
                                        />

                                    </div>

                                    {/* EMPLOYEE NAME */}

                                    <div className="staff-field">

                                        <label>Employee Name</label>

                                        <input
                                            type="text"
                                            value={
                                                selectedEmployee?.employeeName || ""
                                            }
                                            readOnly
                                        />

                                    </div>

                                    {/* EMAIL */}

                                    <div className="staff-field">

                                        <label>Email</label>

                                        <input
                                            type="email"
                                            value={
                                                selectedEmployee?.email || ""
                                            }
                                            readOnly
                                        />

                                    </div>

                                    {/* MOBILE NUMBER */}

                                    <div className="staff-field">

                                        <label>Mobile Number</label>

                                        <input
                                            type="text"
                                            value={
                                                selectedEmployee?.mobileNumber || ""
                                            }
                                            readOnly
                                        />

                                    </div>

                                    {/* LOGIN USERNAME */}

                                    <div className="staff-field">

                                        <label>Login Username</label>

                                        <input
                                            type="text"
                                            value={formData.employeeId}
                                            readOnly
                                        />

                                        <small>
                                            Employee ID will be used as the login username.
                                        </small>

                                    </div>

                                    {/* PASSWORD WITH SHOW/HIDE */}

                                    <div className="staff-field">

                                        <label>Password *</label>

                                        <div className="staff-password-wrapper">

                                            <input
                                                type={
                                                    showPassword
                                                        ? "text"
                                                        : "password"
                                                }
                                                name="password"
                                                value={formData.password}
                                                onChange={handleChange}
                                                placeholder="Enter password"
                                                autoComplete="new-password"
                                                required
                                            />

                                            <button
                                                type="button"
                                                className="staff-password-toggle"
                                                onClick={() =>
                                                    setShowPassword(previous => !previous)
                                                }
                                                aria-label={
                                                    showPassword
                                                        ? "Hide password"
                                                        : "Show password"
                                                }
                                            >
                                                {showPassword ? "Hide" : "Show"}
                                            </button>

                                        </div>

                                    </div>

                                    {/* CONFIRM PASSWORD WITH SHOW/HIDE */}

                                    <div className="staff-field">

                                        <label>Confirm Password *</label>

                                        <div className="staff-password-wrapper">

                                            <input
                                                type={
                                                    showConfirmPassword
                                                        ? "text"
                                                        : "password"
                                                }
                                                name="confirmPassword"
                                                value={formData.confirmPassword}
                                                onChange={handleChange}
                                                placeholder="Confirm password"
                                                autoComplete="new-password"
                                                required
                                            />

                                            <button
                                                type="button"
                                                className="staff-password-toggle"
                                                onClick={() =>
                                                    setShowConfirmPassword(previous => !previous)
                                                }
                                                aria-label={
                                                    showConfirmPassword
                                                        ? "Hide confirm password"
                                                        : "Show confirm password"
                                                }
                                            >
                                                {showConfirmPassword ? "Hide" : "Show"}
                                            </button>

                                        </div>

                                    </div>

                                </div>

                                {/* FORM ACTIONS */}

                                <div className="staff-form-actions">

                                    <button
                                        type="button"
                                        className="staff-cancel-button"
                                        onClick={closeStaffForm}
                                        disabled={saving}
                                    >
                                        Cancel
                                    </button>

                                    <button
                                        type="submit"
                                        className="staff-save-button"
                                        disabled={
                                            saving ||
                                            !selectedEmployee ||
                                            selectedEmployee.hasLogin
                                        }
                                    >
                                        {saving
                                            ? "Creating..."
                                            : "Create Staff Login"}
                                    </button>

                                </div>

                            </form>

                        </div>

                    </div>

                )}

            </div>

        </AdminLayout>

    );
};

export default StaffIdPassword;
