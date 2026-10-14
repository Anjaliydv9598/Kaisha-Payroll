import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

function AdminLogin() {

    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        identifier: "",
        password: ""
    });

    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {

        const { name, value } = e.target;

        setFormData((previousData) => ({
            ...previousData,
            [name]: value
        }));

        setError("");
    };


    const handleSubmit = async (e) => {

        e.preventDefault();

        setError("");

        const identifier =
            formData.identifier.trim();

        const password =
            formData.password;

        if (!identifier) {

            setError("Please enter your email.");

            return;
        }

        if (!password) {

            setError("Please enter your password.");

            return;
        }

        setLoading(true);

        try {

            const response = await fetch(
                "http://localhost:8080/api/auth/admin-login",
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json",
                        "Accept": "application/json"
                    },

                    body: JSON.stringify({
                        identifier,
                        password
                    })
                }
            );

            const data = await response.json();

            if (!response.ok) {

                throw new Error(
                    data.message ||
                    "Invalid admin email or password."
                );
            }


            if (!data.token) {

                throw new Error(
                    "Login successful, but authentication token was not received."
                );
            }


            if (data.role !== "ADMIN") {

                throw new Error(
                    "This account is not an ADMIN account."
                );
            }


            localStorage.setItem(
                "token",
                data.token
            );

            localStorage.setItem(
                "username",
                data.username
            );

            localStorage.setItem(
                "role",
                data.role
            );


            navigate(
                "/admin-dashboard",
                {
                    replace: true
                }
            );

        } catch (err) {

            console.error(
                "ADMIN LOGIN ERROR:",
                err
            );

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

            <div className="auth-card login-card">

                <div className="auth-header">

                    <div className="brand-mark">
                        K
                    </div>

                    <h1>
                        Kaisha Payroll
                    </h1>

                    <p>
                        Admin Login
                    </p>

                </div>


                <form
                    onSubmit={handleSubmit}
                    autoComplete="off"
                >

                    <div className="form-group">

                        <label htmlFor="identifier">
                            Admin Email
                        </label>

                        <input
                            id="identifier"
                            name="identifier"
                            type="email"
                            placeholder="Enter admin email"
                            value={formData.identifier}
                            onChange={handleChange}
                            disabled={loading}
                            autoComplete="new-email"
                        />

                    </div>


                    <div className="form-group">

                        <label htmlFor="password">
                            Password
                        </label>

                        <input
                            id="password"
                            name="password"
                            type="password"
                            placeholder="Enter your password"
                            value={formData.password}
                            onChange={handleChange}
                            disabled={loading}
                            autoComplete="new-password"
                        />

                    </div>


                    <div className="forgot-password">

                        <Link to="/forgot-password">
                            Forgot Password?
                        </Link>

                    </div>


                    {error && (
                        <div className="error-message">
                            {error}
                        </div>
                    )}


                    <button
                        type="submit"
                        className="primary-button"
                        disabled={loading}
                    >
                        {loading
                            ? "Logging in..."
                            : "Admin Login"
                        }
                    </button>

                </form>


                <div className="auth-footer">

                    <button
                        type="button"
                        className="secondary-button"
                        onClick={() => navigate("/login")}
                    >
                        ← Back
                    </button>

                </div>

            </div>

        </div>
    );
}

export default AdminLogin;