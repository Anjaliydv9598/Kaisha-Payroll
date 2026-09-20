import { useState } from "react";
import { useNavigate } from "react-router-dom";

function ForgotPassword() {
    const navigate = useNavigate();

    const [step, setStep] = useState(1);

    const [email, setEmail] = useState("");
    const [otp, setOtp] = useState("");

    const [resetToken, setResetToken] = useState("");

    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    // -----------------------------
    // STEP 1 - SEND OTP
    // -----------------------------
    const handleSendOtp = async (e) => {
        e.preventDefault();

        setMessage("");
        setError("");

        if (!email.trim()) {
            setError("Please enter your email address.");
            return;
        }

        setLoading(true);

        try {
            const response = await fetch(
                "http://localhost:8080/api/auth/forgot-password/send-otp",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        identifier: email.trim(),
                    }),
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message || "Unable to send OTP."
                );
            }

            setMessage(
                "OTP has been sent to your email address."
            );

            setStep(2);
        } catch (err) {
            setError(
                err.message || "Something went wrong."
            );
        } finally {
            setLoading(false);
        }
    };

    // -----------------------------
    // STEP 2 - VERIFY OTP
    // -----------------------------
    const handleVerifyOtp = async (e) => {
        e.preventDefault();

        setMessage("");
        setError("");

        if (!/^\d{6}$/.test(otp)) {
            setError("Please enter a valid 6-digit OTP.");
            return;
        }

        setLoading(true);

        try {
            const response = await fetch(
                "http://localhost:8080/api/auth/forgot-password/verify-otp",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        identifier: email.trim(),
                        otp: otp,
                    }),
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message || "Invalid OTP."
                );
            }

            setResetToken(data.resetToken);

            setMessage(
                "OTP verified successfully."
            );

            setStep(3);
        } catch (err) {
            setError(
                err.message || "OTP verification failed."
            );
        } finally {
            setLoading(false);
        }
    };

    // -----------------------------
    // STEP 3 - RESET PASSWORD
    // -----------------------------
    const handleResetPassword = async (e) => {
        e.preventDefault();

        setMessage("");
        setError("");

        if (newPassword.length < 8) {
            setError(
                "Password must contain at least 8 characters."
            );
            return;
        }

        if (newPassword !== confirmPassword) {
            setError(
                "Password and confirm password do not match."
            );
            return;
        }

        setLoading(true);

        try {
            const response = await fetch(
                "http://localhost:8080/api/auth/forgot-password/reset",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        resetToken: resetToken,
                        newPassword: newPassword,
                        confirmPassword: confirmPassword,
                    }),
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message || "Unable to reset password."
                );
            }

            setMessage(
                "Password reset successful. Redirecting to Login..."
            );

            setNewPassword("");
            setConfirmPassword("");
            setOtp("");
            setResetToken("");

            setTimeout(() => {
                navigate("/login", {
                    replace: true,
                });
            }, 1000);
        } catch (err) {
            setError(
                err.message || "Password reset failed."
            );
        } finally {
            setLoading(false);
        }
    };

    // -----------------------------
    // CHANGE EMAIL
    // -----------------------------
    const handleChangeEmail = () => {
        setStep(1);

        setEmail("");
        setOtp("");
        setResetToken("");

        setNewPassword("");
        setConfirmPassword("");

        setMessage("");
        setError("");
    };

    return (
        <div className="auth-page">
            <div className="auth-card">

                <div className="auth-header">
                    <h1>Forgot Password?</h1>

                    <p>
                        Reset your Kaisha Payroll password
                        securely using your email.
                    </p>
                </div>

                {/* ERROR */}
                {error && (
                    <div className="error-message">
                        {error}
                    </div>
                )}

                {/* SUCCESS */}
                {message && (
                    <div className="success-message">
                        {message}
                    </div>
                )}

                {/* -------------------------------- */}
                {/* STEP 1 - EMAIL */}
                {/* -------------------------------- */}

                {step === 1 && (
                    <form
                        onSubmit={handleSendOtp}
                        autoComplete="off"
                    >
                        <div className="form-group">
                            <label htmlFor="email">
                                Email Address
                            </label>

                            <input
                                id="email"
                                type="email"
                                value={email}
                                onChange={(e) =>
                                    setEmail(e.target.value)
                                }
                                placeholder="Enter your registered email"
                                autoComplete="off"
                            />
                        </div>

                        <button
                            type="submit"
                            disabled={loading}
                        >
                            {loading
                                ? "Sending OTP..."
                                : "Send OTP"}
                        </button>

                        <div className="auth-footer">
                            <button
                                type="button"
                                className="link-button"
                                onClick={() =>
                                    navigate("/login")
                                }
                            >
                                Back to Login
                            </button>
                        </div>
                    </form>
                )}

                {/* -------------------------------- */}
                {/* STEP 2 - OTP */}
                {/* -------------------------------- */}

                {step === 2 && (
                    <form
                        onSubmit={handleVerifyOtp}
                        autoComplete="off"
                    >
                        <div className="form-group">
                            <label htmlFor="otp">
                                Enter OTP
                            </label>

                            <input
                                id="otp"
                                type="text"
                                inputMode="numeric"
                                maxLength="6"
                                value={otp}
                                onChange={(e) =>
                                    setOtp(
                                        e.target.value.replace(
                                            /\D/g,
                                            ""
                                        )
                                    )
                                }
                                placeholder="Enter 6-digit OTP"
                                autoComplete="one-time-code"
                            />

                            <small>
                                OTP sent to {email}
                            </small>
                        </div>

                        <button
                            type="submit"
                            disabled={loading}
                        >
                            {loading
                                ? "Verifying..."
                                : "Verify OTP"}
                        </button>

                        <button
                            type="button"
                            className="secondary-button"
                            onClick={handleChangeEmail}
                        >
                            Change Email
                        </button>
                    </form>
                )}

                {/* -------------------------------- */}
                {/* STEP 3 - NEW PASSWORD */}
                {/* -------------------------------- */}

                {step === 3 && (
                    <form
                        onSubmit={handleResetPassword}
                        autoComplete="off"
                    >
                        <div className="form-group">
                            <label htmlFor="newPassword">
                                New Password
                            </label>

                            <input
                                id="newPassword"
                                type="password"
                                value={newPassword}
                                onChange={(e) =>
                                    setNewPassword(
                                        e.target.value
                                    )
                                }
                                placeholder="Enter new password"
                                autoComplete="new-password"
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="confirmPassword">
                                Confirm Password
                            </label>

                            <input
                                id="confirmPassword"
                                type="password"
                                value={confirmPassword}
                                onChange={(e) =>
                                    setConfirmPassword(
                                        e.target.value
                                    )
                                }
                                placeholder="Confirm new password"
                                autoComplete="new-password"
                            />
                        </div>

                        <small>
                            Password must contain at least
                            8 characters.
                        </small>

                        <button
                            type="submit"
                            disabled={loading}
                        >
                            {loading
                                ? "Resetting Password..."
                                : "Reset Password"}
                        </button>
                    </form>
                )}

            </div>
        </div>
    );
}

export default ForgotPassword;