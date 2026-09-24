import { useNavigate } from "react-router-dom";
import "./LoginSelection.css";

function LoginSelection() {

    const navigate = useNavigate();

    return (
        <div className="login-selection-page">

            <div className="login-selection-card">

                {/* LOGO */}

                <div className="selection-brand-mark">
                    K
                </div>


                {/* TITLE */}

                <h1 className="selection-title">
                    KAISHA <span>PAYROLL</span>
                </h1>

                <div className="selection-title-line"></div>

                <p className="selection-subtitle">
                    Select your account
                </p>


                {/* ADMIN LOGIN */}

                <button
                    type="button"
                    className="account-login-button admin-login-button"
                    onClick={() => navigate("/admin-login")}
                >

                    <span className="account-icon">
                        👤⚙
                    </span>

                    <span className="account-divider"></span>

                    <span className="account-login-text">
                        ADMIN LOGIN
                    </span>

                    <span className="account-arrow">
                        →
                    </span>

                </button>


                {/* STAFF LOGIN */}

                <button
                    type="button"
                    className="account-login-button staff-login-button"
                    onClick={() => navigate("/staff-login")}
                >

                    <span className="account-icon">
                        👥
                    </span>

                    <span className="account-divider"></span>

                    <span className="account-login-text">
                        STAFF LOGIN
                    </span>

                    <span className="account-arrow">
                        →
                    </span>

                </button>


                {/* HELP & SUPPORT */}

                <button
                    type="button"
                    className="selection-help-button"
                    onClick={() => {
                        window.location.href =
                            "mailto:support@kaishapayroll.com?subject=Kaisha Payroll Help & Support";
                    }}
                >

                    <span className="help-icon">
                        🎧
                    </span>

                    <span>
                        Help & Support
                    </span>

                </button>

            </div>

        </div>
    );
}

export default LoginSelection;