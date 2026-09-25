import React, { useEffect, useState } from "react";
import "./StaffIdPassword.css";

const API_BASE_URL = "http://localhost:8080/api";

const getToken = () => localStorage.getItem("token");

const StaffIdPassword = () => {
    const [staff, setStaff] = useState([]);

    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    const [showForm, setShowForm] = useState(false);

    const [formData, setFormData] = useState({
        username: "",
        email: "",
        mobileNumber: "",
        password: "",
        confirmPassword: "",
    });

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    const loadStaff = async () => {
        try {
            setLoading(true);

            const response = await fetch(
                `${API_BASE_URL}/auth/staff`,
                {
                    headers: {
                        Authorization: `Bearer ${getToken()}`,
                    },
                }
            );

            if (!response.ok) {
                throw new Error("Unable to load staff accounts.");
            }

            const data = await response.json();

            setStaff(Array.isArray(data) ? data : []);
        } catch (err) {
            setError(
                err.message ||
                "Unable to load staff accounts."
            );
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadStaff();
    }, []);

    const handleChange = (event) => {
        const { name, value } = event.target;

        setFormData((previous) => ({
            ...previous,
            [name]: value,
        }));
    };

    const resetForm = () => {
        setFormData({
            username: "",
            email: "",
            mobileNumber: "",
            password: "",
            confirmPassword: "",
        });

        setError("");
        setSuccess("");
    };

    const handleCreateStaff = async (event) => {
        event.preventDefault();

        setError("");
        setSuccess("");

        if (
            !formData.username ||
            !formData.email ||
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
                `${API_BASE_URL}/auth/staff`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${getToken()}`,
                    },
                    body: JSON.stringify({
                        username: formData.username,
                        email: formData.email,
                        mobileNumber: formData.mobileNumber,
                        password: formData.password,
                        confirmPassword: formData.confirmPassword,
                        role: "STAFF",
                    }),
                }
            );

            const data = await response.json().catch(() => ({}));

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    "Unable to create staff account."
                );
            }

            setSuccess(
                "Staff account created successfully."
            );

            resetForm();
            setShowForm(false);

            await loadStaff();
        } catch (err) {
            setError(
                err.message ||
                "Unable to create staff account."
            );
        } finally {
            setSaving(false);
        }
    };

    return (
        <div className="staff-page">

            <div className="staff-header">

                <div>
                    <h1>Staff ID & Password</h1>

                    <p>
                        Create staff ID, password & reset password.
                    </p>
                </div>

                <button
                    className="staff-add-button"
                    onClick={() => {
                        resetForm();
                        setShowForm(true);
                    }}
                >
                    + Create Staff
                </button>

            </div>

            {success && (
                <div className="staff-success">
                    {success}
                </div>
            )}

            {error && !showForm && (
                <div className="staff-error">
                    {error}
                </div>
            )}

            <div className="staff-card">

                <div className="staff-card-header">
                    <div>
                        <h2>Staff Accounts</h2>
                        <p>
                            Manage staff login accounts.
                        </p>
                    </div>

                    <span>
                        {staff.length} accounts
                    </span>
                </div>

                {loading ? (
                    <div className="staff-state">
                        Loading staff accounts...
                    </div>
                ) : staff.length === 0 ? (
                    <div className="staff-state">
                        No staff accounts found.
                    </div>
                ) : (
                    <div className="staff-table-wrapper">

                        <table className="staff-table">

                            <thead>
                            <tr>
                                <th>Staff ID</th>
                                <th>Email</th>
                                <th>Mobile</th>
                                <th>Role</th>
                                <th>Status</th>
                                <th>Action</th>
                            </tr>
                            </thead>

                            <tbody>

                            {staff.map((item) => (
                                <tr key={item.id || item.username}>

                                    <td>
                                        <strong>
                                            {item.username}
                                        </strong>
                                    </td>

                                    <td>
                                        {item.email || "-"}
                                    </td>

                                    <td>
                                        {item.mobileNumber || "-"}
                                    </td>

                                    <td>
                                            <span className="staff-role">
                                                STAFF
                                            </span>
                                    </td>

                                    <td>
                                            <span
                                                className={
                                                    item.active
                                                        ? "staff-status active"
                                                        : "staff-status inactive"
                                                }
                                            >
                                                {item.active
                                                    ? "Active"
                                                    : "Inactive"}
                                            </span>
                                    </td>

                                    <td>
                                        <button
                                            className="staff-reset-button"
                                            type="button"
                                        >
                                            Reset Password
                                        </button>
                                    </td>

                                </tr>
                            ))}

                            </tbody>

                        </table>

                    </div>
                )}

            </div>

            {showForm && (
                <div className="staff-modal-overlay">

                    <div className="staff-modal">

                        <div className="staff-modal-header">

                            <div>
                                <h2>Create Staff Account</h2>

                                <p>
                                    Create a new STAFF login account.
                                </p>
                            </div>

                            <button
                                className="staff-close-button"
                                onClick={() => setShowForm(false)}
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

                                <div className="staff-field">

                                    <label>
                                        Staff ID *
                                    </label>

                                    <input
                                        name="username"
                                        value={formData.username}
                                        onChange={handleChange}
                                        placeholder="Enter staff ID"
                                    />

                                </div>

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
                                    />

                                </div>

                                <div className="staff-field">

                                    <label>
                                        Mobile Number
                                    </label>

                                    <input
                                        name="mobileNumber"
                                        value={formData.mobileNumber}
                                        onChange={handleChange}
                                        placeholder="Enter mobile number"
                                    />

                                </div>

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
                                    />

                                </div>

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
                                    />

                                </div>

                            </div>

                            <div className="staff-form-actions">

                                <button
                                    type="button"
                                    className="staff-cancel-button"
                                    onClick={() => setShowForm(false)}
                                >
                                    Cancel
                                </button>

                                <button
                                    type="submit"
                                    className="staff-save-button"
                                    disabled={saving}
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
    );
};

export default StaffIdPassword;