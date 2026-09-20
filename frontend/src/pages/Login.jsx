import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

function Login() {
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

        const identifier = formData.identifier.trim();
        const password = formData.password;

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
                "http://localhost:8080/api/auth/login",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "Accept": "application/json"
                    },
                    body: JSON.stringify({
                        identifier: identifier,
                        password: password
                    })
                }
            );

            const data = await response.json();

            console.log("LOGIN STATUS:", response.status);
            console.log("LOGIN RESPONSE:", data);
            console.log("TOKEN RECEIVED:", !!data.token);
            console.log("USERNAME RECEIVED:", data.username);
            console.log("ROLE RECEIVED:", data.role);

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    "Invalid email or password."
                );
            }

            if (!data.token) {
                throw new Error(
                    "Login successful, but authentication token was not received from the server."
                );
            }

            if (!data.username) {
                throw new Error(
                    "Login successful, but username was not received from the server."
                );
            }

            if (!data.role) {
                throw new Error(
                    "Login successful, but user role was not received from the server."
                );
            }

            localStorage.setItem("token", data.token);
            localStorage.setItem("username", data.username);
            localStorage.setItem("role", data.role);

            console.log(
                "TOKEN SAVED:",
                !!localStorage.getItem("token")
            );

            console.log(
                "USERNAME SAVED:",
                localStorage.getItem("username")
            );

            console.log(
                "ROLE SAVED:",
                localStorage.getItem("role")
            );

            if (data.role === "ADMIN") {
                navigate("/admin-dashboard", {
                    replace: true
                });
                return;
            }

            if (data.role === "STAFF") {
                navigate("/staff-dashboard", {
                    replace: true
                });
                return;
            }

            localStorage.removeItem("token");
            localStorage.removeItem("username");
            localStorage.removeItem("role");

            setError("Invalid user role.");

        } catch (err) {
            console.error("LOGIN ERROR:", err);

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

                {/* ==============================
                    HEADER
                ============================== */}

                <div className="auth-header">

                    <div className="brand-mark">
                        K
                    </div>

                    <h1>
                        Kaisha Payroll
                    </h1>

                    <p>
                        Login to your account
                    </p>

                </div>


                {/* ==============================
                    LOGIN FORM
                ============================== */}

                <form
                    onSubmit={handleSubmit}
                    autoComplete="off"
                >

                    {/* EMAIL */}

                    <div className="form-group">

                        <label htmlFor="identifier">
                            Email
                        </label>

                        <input
                            id="identifier"
                            name="identifier"
                            type="email"
                            placeholder="Enter your email"
                            value={formData.identifier}
                            onChange={handleChange}
                            disabled={loading}
                            autoComplete="new-email"
                        />

                    </div>


                    {/* PASSWORD */}

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


                    {/* FORGOT PASSWORD */}

                    <div className="forgot-password">

                        <Link to="/forgot-password">
                            Forgot Password?
                        </Link>

                    </div>


                    {/* HELP & SUPPORT */}

                    <div className="help-support-link">

                        <button
                            type="button"
                            className="help-support-button"
                            onClick={() => navigate("/help")}
                            disabled={loading}
                        >
                            Help & Support
                        </button>

                    </div>


                    {/* ERROR MESSAGE */}

                    {error && (
                        <div className="error-message">
                            {error}
                        </div>
                    )}


                    {/* LOGIN BUTTON */}

                    <button
                        type="submit"
                        className="primary-button"
                        disabled={loading}
                    >
                        {loading
                            ? "Logging in..."
                            : "Login"
                        }
                    </button>

                </form>

            </div>

        </div>
    );
}

export default Login;