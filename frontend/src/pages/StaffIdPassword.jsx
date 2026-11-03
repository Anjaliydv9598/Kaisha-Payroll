
import React, { useEffect, useState } from "react";
import "./StaffIdPassword.css";
import AdminLayout from "../pages/AdminLayout";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const STAFF_API = `${API_BASE_URL}/auth/staff`;

const getToken = () => localStorage.getItem("token");

const StaffIdPassword = () => {

    const [employees, setEmployees] = useState([]);

    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    const [showForm, setShowForm] = useState(false);

    const [formData, setFormData] = useState({
        employeeId: "",
        username: "",
        email: "",
        mobileNumber: "",
        password: "",
        confirmPassword: "",
    });

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    // ==========================================
    // 1. LOAD EMPLOYEES
    // ==========================================

    const loadEmployees = async () => {

        try {
            setLoading(true);

            const response = await fetch(
                `${STAFF_API}/employees`,
                {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${getToken()}`,
                    },
                }
            );

            const data = await response.json().catch(() => []);

            if (!response.ok) {
                throw new Error(
                    data.message || "Unable to load employees."
                );
            }

            setEmployees(Array.isArray(data) ? data : []);

            setError("");

        } catch (err) {

            setError(
                err.message || "Unable to load employees."
            );

        } finally {

            setLoading(false);

        }
    };

    useEffect(() => {
        loadEmployees();
    }, []);

    // ==========================================
    // 2. HANDLE FORM INPUT
    // ==========================================

    const handleChange = (event) => {

        const { name, value } = event.target;

        setFormData((previous) => ({
            ...previous,
            [name]: value,
        }));
    };

    // ==========================================
    // 3. RESET FORM
    // ==========================================

    const resetForm = () => {

        setFormData({
            employeeId: "",
            username: "",
            email: "",
            mobileNumber: "",
            password: "",
            confirmPassword: "",
        });

        setError("");
    };

    // ==========================================
    // 4. OPEN CREATE STAFF FORM
    // ==========================================

    const openCreateForm = () => {

        resetForm();
        setSuccess("");
        setShowForm(true);
    };

    // ==========================================
    // 5. CREATE STAFF LOGIN
    // ==========================================

    const handleCreateStaff = async (event) => {

        event.preventDefault();

        setError("");
        setSuccess("");

        if (!formData.employeeId) {
            setError("Please select an employee.");
            return;
        }

        if (
            !formData.username.trim() ||
            !formData.email.trim() ||
            !formData.mobileNumber.trim() ||
            !formData.password ||
            !formData.confirmPassword
        ) {
            setError("Please fill all required fields.");
            return;
        }

        if (formData.password !== formData.confirmPassword) {
            setError("Password and confirm password do not match.");
            return;
        }

        try {

            setSaving(true);

            const response = await fetch(
                STAFF_API,
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${getToken()}`,
                    },

                    body: JSON.stringify({

                        employeeId: formData.employeeId,

                        username: formData.username.trim(),

                        email: formData.email.trim(),

                        mobileNumber: formData.mobileNumber.trim(),

                        password: formData.password,

                        confirmPassword: formData.confirmPassword,

                    }),
                }
            );

            const data = await response.json().catch(() => ({}));

            if (!response.ok) {

                throw new Error(
                    data.message ||
                    data.detail ||
                    "Unable to create staff account."
                );
            }

            setSuccess("Staff login created successfully.");

            resetForm();

            setShowForm(false);

            // Refresh employee login status
            await loadEmployees();

        } catch (err) {

            setError(
                err.message ||
                "Unable to create staff account."
            );

        } finally {

            setSaving(false);

        }
    };

    // ==========================================
    // 6. AVAILABLE EMPLOYEES
    // ==========================================

    const availableEmployees = employees.filter(
        (employee) => !employee.hasLogin
    );

    const createdCount = employees.filter(
        (employee) => employee.hasLogin
    ).length;

    // ==========================================
    // 7. UI
    // ==========================================

    return (

        <AdminLayout>

            <div className="staff-page">

                {/* PAGE HEADER */}

                <div className="staff-header">

                    <div>

                        <h1>Staff ID & Password</h1>

                        <p>
                            Connect existing employees with staff login accounts.
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
                                    <th>Login Status</th>
                                    <th>Action</th>
                                </tr>

                                </thead>

                                <tbody>

                                {employees.map((employee) => (

                                    <tr key={employee.id}>

                                        <td>
                                            <strong>
                                                {employee.employeeId}
                                            </strong>
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
                                                    onClick={() => {
                                                        resetForm();

                                                        setFormData((previous) => ({
                                                            ...previous,
                                                            employeeId: employee.employeeId,
                                                        }));

                                                        setShowForm(true);
                                                    }}
                                                >
                                                    Create Staff
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

                {/* CREATE STAFF MODAL */}

                {showForm && (

                    <div className="staff-modal-overlay">

                        <div className="staff-modal">

                            <div className="staff-modal-header">

                                <div>

                                    <h2>Create Staff Account</h2>

                                    <p>
                                        Create login credentials for an existing employee.
                                    </p>

                                </div>

                                <button
                                    type="button"
                                    className="staff-close-button"
                                    onClick={() => {
                                        setShowForm(false);
                                        resetForm();
                                    }}
                                >
                                    ×
                                </button>

                            </div>

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

                                    {/* EMPLOYEE DROPDOWN */}

                                    <div className="staff-field">

                                        <label>
                                            Select Employee *
                                        </label>

                                        <select
                                            name="employeeId"
                                            value={formData.employeeId}
                                            onChange={handleChange}
                                            required
                                        >

                                            <option value="">
                                                Select Employee ID
                                            </option>

                                            {availableEmployees.map((employee) => (

                                                <option
                                                    key={employee.id}
                                                    value={employee.employeeId}
                                                >
                                                    {employee.employeeId}
                                                </option>

                                            ))}

                                        </select>

                                    </div>

                                    {/* USERNAME */}

                                    <div className="staff-field">

                                        <label>
                                            Username *
                                        </label>

                                        <input
                                            type="text"
                                            name="username"
                                            value={formData.username}
                                            onChange={handleChange}
                                            placeholder="Enter username"
                                            required
                                        />

                                    </div>

                                    {/* EMAIL */}

                                    <div className="staff-field">

                                        <label>
                                            Email *
                                        </label>

                                        <input
                                            type="email"
                                            name="email"
                                            value={formData.email}
                                            onChange={handleChange}
                                            placeholder="Enter staff email"
                                            required
                                        />

                                    </div>

                                    {/* MOBILE */}

                                    <div className="staff-field">

                                        <label>
                                            Mobile Number *
                                        </label>

                                        <input
                                            type="tel"
                                            name="mobileNumber"
                                            value={formData.mobileNumber}
                                            onChange={handleChange}
                                            placeholder="Enter mobile number"
                                            required
                                        />

                                    </div>

                                    {/* PASSWORD */}

                                    <div className="staff-field">

                                        <label>
                                            Password *
                                        </label>

                                        <input
                                            type="password"
                                            name="password"
                                            value={formData.password}
                                            onChange={handleChange}
                                            placeholder="Enter password"
                                            required
                                        />

                                    </div>

                                    {/* CONFIRM PASSWORD */}

                                    <div className="staff-field">

                                        <label>
                                            Confirm Password *
                                        </label>

                                        <input
                                            type="password"
                                            name="confirmPassword"
                                            value={formData.confirmPassword}
                                            onChange={handleChange}
                                            placeholder="Confirm password"
                                            required
                                        />

                                    </div>

                                </div>

                                {/* FORM ACTIONS */}

                                <div className="staff-form-actions">

                                    <button
                                        type="button"
                                        className="staff-cancel-button"
                                        onClick={() => {
                                            setShowForm(false);
                                            resetForm();
                                        }}
                                        disabled={saving}
                                    >
                                        Cancel
                                    </button>

                                    <button
                                        type="submit"
                                        className="staff-save-button"
                                        disabled={
                                            saving ||
                                            availableEmployees.length === 0
                                        }
                                    >

                                        {saving
                                            ? "Creating..."
                                            : "Create Staff"}

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
