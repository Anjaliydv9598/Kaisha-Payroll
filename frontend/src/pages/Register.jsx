import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

function Register() {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        email: "",
        companyId: "",
        password: "",
        confirmPassword: "",
        companyName: "",
        location: "",
        telephoneNumber: ""
    });

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [loading, setLoading] = useState(false);
    const [registered, setRegistered] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;

        setFormData((previousData) => ({
            ...previousData,
            [name]: value
        }));

        setError("");
    };

    const validateForm = () => {
        if (!formData.email.trim()) {
            return "Please enter your email.";
        }

        if (!formData.companyId.trim()) {
            return "Please create your Company ID.";
        }

        if (!formData.password) {
            return "Please create your password.";
        }

        if (!formData.confirmPassword) {
            return "Please confirm your password.";
        }

        if (formData.password !== formData.confirmPassword) {
            return "Passwords do not match.";
        }

        if (!formData.companyName.trim()) {
            return "Please enter your company name.";
        }

        if (!formData.location.trim()) {
            return "Please enter your company location.";
        }

        if (!formData.telephoneNumber.trim()) {
            return "Please enter your company telephone number.";
        }

        return "";
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        setError("");
        setSuccess("");

        const validationError = validateForm();

        if (validationError) {
            setError(validationError);
            return;
        }

        setLoading(true);

        try {
            const response = await fetch(
                "http://localhost:8080/api/auth/register",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify(formData)
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message || "Registration failed."
                );
            }

            /*
             * Registration is NOT login.
             * Do not save JWT here.
             */

            setSuccess(
                "Registration successful. Your admin account has been created."
            );

            setRegistered(true);

            /*
             * Clear password values from frontend state.
             */

            setFormData((previousData) => ({
                ...previousData,
                password: "",
                confirmPassword: ""
            }));

            /*
             * Redirect to Login.
             */

            setTimeout(() => {
                navigate("/login", {
                    replace: true
                });
            }, 1800);

        } catch (err) {
            setError(
                err.message ||
                "Unable to connect to the server."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-page">

            <div className="auth-card register-card">

                {/* HEADER */}

                <div className="auth-header">

                    <div className="brand-mark">
                        K
                    </div>

                    <h1>
                        Kaisha Payroll
                    </h1>

                    <p>
                        Create your company admin account
                    </p>

                </div>


                <form
                    onSubmit={handleSubmit}
                    autoComplete="off"
                >

                    {/* ACCOUNT INFORMATION */}

                    <div className="section-heading">

                        <h2>
                            Account Information
                        </h2>

                    </div>


                    {/* EMAIL */}

                    <div className="form-group">

                        <label htmlFor="email">
                            Email
                        </label>

                        <input
                            id="email"
                            name="email"
                            type="email"
                            placeholder="Enter your email"
                            value={formData.email}
                            onChange={handleChange}
                            disabled={
                                loading ||
                                registered
                            }
                            autoComplete="off"
                        />

                    </div>


                    {/* COMPANY ID */}

                    <div className="form-group">

                        <label htmlFor="companyId">
                            Create Company ID
                        </label>

                        <input
                            id="companyId"
                            name="companyId"
                            type="text"
                            placeholder="Example: KAI001"
                            value={formData.companyId}
                            onChange={handleChange}
                            disabled={
                                loading ||
                                registered
                            }
                            maxLength={50}
                            autoComplete="off"
                        />

                        <small className="field-note">
                            Create your own unique Company ID.
                            This ID is not generated by the system.
                        </small>

                    </div>


                    {/* PASSWORD */}

                    <div className="form-group">

                        <label htmlFor="password">
                            Create Password
                        </label>

                        <input
                            id="password"
                            name="password"
                            type="password"
                            placeholder="Create your password"
                            value={formData.password}
                            onChange={handleChange}
                            disabled={
                                loading ||
                                registered
                            }
                            autoComplete="new-password"
                        />

                    </div>


                    {/* CONFIRM PASSWORD */}

                    <div className="form-group">

                        <label htmlFor="confirmPassword">
                            Confirm Password
                        </label>

                        <input
                            id="confirmPassword"
                            name="confirmPassword"
                            type="password"
                            placeholder="Confirm your password"
                            value={formData.confirmPassword}
                            onChange={handleChange}
                            disabled={
                                loading ||
                                registered
                            }
                            autoComplete="new-password"
                        />

                    </div>


                    {/* COMPANY */}

                    <div className="section-heading company-section">

                        <h2>
                            Company
                        </h2>

                    </div>


                    {/* COMPANY NAME */}

                    <div className="form-group">

                        <label htmlFor="companyName">
                            Company Name
                        </label>

                        <input
                            id="companyName"
                            name="companyName"
                            type="text"
                            placeholder="Enter company name"
                            value={formData.companyName}
                            onChange={handleChange}
                            disabled={
                                loading ||
                                registered
                            }
                            autoComplete="off"
                        />

                    </div>


                    {/* LOCATION */}

                    <div className="form-group">

                        <label htmlFor="location">
                            Location
                        </label>

                        <input
                            id="location"
                            name="location"
                            type="text"
                            placeholder="Enter company location"
                            value={formData.location}
                            onChange={handleChange}
                            disabled={
                                loading ||
                                registered
                            }
                            autoComplete="off"
                        />

                    </div>


                    {/* TELEPHONE */}

                    <div className="form-group">

                        <label htmlFor="telephoneNumber">
                            Telephone Number
                        </label>

                        <input
                            id="telephoneNumber"
                            name="telephoneNumber"
                            type="tel"
                            placeholder="Enter company telephone number"
                            value={formData.telephoneNumber}
                            onChange={handleChange}
                            disabled={
                                loading ||
                                registered
                            }
                            inputMode="tel"
                            autoComplete="off"
                        />

                    </div>


                    {/* ERROR */}

                    {error && (
                        <div className="error-message">
                            {error}
                        </div>
                    )}


                    {/* SUCCESS */}

                    {success && (
                        <div className="success-message">

                            <strong>
                                {success}
                            </strong>

                            <span>
                                Redirecting to Login...
                            </span>

                        </div>
                    )}


                    {/* REGISTER BUTTON */}

                    <button
                        type="submit"
                        className="primary-button"
                        disabled={
                            loading ||
                            registered
                        }
                    >

                        {loading
                            ? "Creating Account..."
                            : registered
                                ? "Registration Complete"
                                : "Register"
                        }

                    </button>

                </form>


                {/* LOGIN LINK */}

                <div className="auth-footer">

                    <span>
                        Already have an account?
                    </span>

                    <Link
                        to="/login"
                        className="secondary-button"
                    >
                        Login
                    </Link>

                </div>

            </div>

        </div>
    );
}

export default Register;