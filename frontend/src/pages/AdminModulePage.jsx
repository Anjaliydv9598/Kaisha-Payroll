import { useNavigate } from "react-router-dom";

import "./AdminModulePage.css";

function AdminModulePage({
                             title,
                             description
                         }) {

    const navigate = useNavigate();

    const handleBack = () => {

        navigate("/admin-dashboard");

    };


    return (
        <div className="admin-module-page">


            {/* HEADER */}

            <header className="module-page-header">

                <div className="module-page-brand">

                    <div className="module-page-brand-mark">
                        K
                    </div>

                    <div>

                        <h1>
                            Kaisha Payroll
                        </h1>

                        <p>
                            Admin Dashboard
                        </p>

                    </div>

                </div>


                <button
                    type="button"
                    className="module-back-button"
                    onClick={handleBack}
                >
                    ← Dashboard
                </button>

            </header>


            {/* CONTENT */}

            <main className="module-page-main">

                <div className="module-page-card">


                    <div className="module-large-icon">
                        +
                    </div>


                    <h2>
                        {title}
                    </h2>


                    <p className="module-description">
                        {description}
                    </p>


                    <div className="module-coming-soon">

                        <strong>
                            Module Ready
                        </strong>

                        <p>
                            The structure for this
                            module is ready.
                            Functionality will be
                            implemented next.
                        </p>

                    </div>


                    <button
                        type="button"
                        className="module-page-dashboard-button"
                        onClick={handleBack}
                    >
                        Back to Admin Dashboard
                    </button>


                </div>

            </main>

        </div>
    );
}

export default AdminModulePage;