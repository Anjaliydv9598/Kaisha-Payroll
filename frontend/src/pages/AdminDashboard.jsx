import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import "./AdminDashboard.css";

function AdminDashboard() {

    const navigate = useNavigate();

    const [admin, setAdmin] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const token = localStorage.getItem("token");


    /*
     * Dashboard modules
     */

    const modules = [
        {
            id: "company",
            title: "Company",
            description:
                "Manage company information and settings.",
            icon: "🏢",
            path: "/admin/company"
        },

        {
            id: "help-support",
            title: "Help & Support",
            description:
                "View and manage staff support queries.",
            icon: "✉",
            path: "/admin/help-support"
        },

        {
            id: "department",
            title: "Department",
            description:
                "Create and manage company departments.",
            icon: "▦",
            path: "/admin/department"
        },

        {
            id: "salary",
            title: "Salary",
            description:
                "Manage payroll and salary processing.",
            icon: "₹",
            path: "/admin/salary"
        },

        {
            id: "master-data",
            title: "Master Data No. Series",
            description:
                "Manage employee and document number series.",
            icon: "#",
            path: "/admin/master-data"
        },

        {
            id: "employee-create",
            title: "Employee Data Create",
            description:
                "Create and manage employee records.",
            icon: "👤",
            path: "/admin/employee-create"
        },

        {
            id: "salary-components",
            title: "Salary Component of Every Employee",
            description:
                "Manage individual employee salary components.",
            icon: "₹",
            path: "/admin/salary-components"
        },

        {
            id: "attendance",
            title: "Attendance",
            description:
                "Manage employee attendance and punch records.",
            icon: "✓",
            path: "/admin/attendance"
        }
    ];


    /*
     * Load admin information
     */

    useEffect(() => {

        if (!token) {

            navigate("/login", {
                replace: true
            });

            return;
        }


        const loadAdmin = async () => {

            try {

                setLoading(true);
                setError("");


                const response = await fetch(
                    "http://localhost:8080/api/me",
                    {
                        method: "GET",
                        headers: {
                            Authorization:
                                `Bearer ${token}`
                        }
                    }
                );


                if (response.status === 401) {

                    localStorage.clear();

                    navigate("/login", {
                        replace: true
                    });

                    return;
                }


                if (!response.ok) {

                    throw new Error(
                        "Unable to load admin information."
                    );
                }


                const data =
                    await response.json();

                setAdmin(data);

            } catch (err) {

                setError(
                    err.message ||
                    "Unable to connect to server."
                );

            } finally {

                setLoading(false);

            }
        };


        loadAdmin();

    }, [navigate, token]);


    /*
     * Logout
     */

    const handleLogout = () => {

        localStorage.removeItem("token");
        localStorage.removeItem("username");
        localStorage.removeItem("role");

        navigate("/login", {
            replace: true
        });
    };


    /*
     * Module click
     */

    const handleModuleClick = (path) => {

        navigate(path);

    };


    /*
     * Loading
     */

    if (loading) {

        return (
            <div className="admin-loading">

                <div className="loading-box">

                    <div className="loading-spinner"></div>

                    <p>
                        Loading Admin Dashboard...
                    </p>

                </div>

            </div>
        );
    }


    return (
        <div className="admin-dashboard">


            {/* HEADER */}

            <header className="dashboard-header">

                <div className="dashboard-brand">

                    <div className="dashboard-brand-mark">
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


                <div className="dashboard-header-right">

                    <div className="admin-user">

                        <span className="admin-user-icon">
                            A
                        </span>

                        <div>

                            <strong>
                                {admin?.email ||
                                    "Administrator"}
                            </strong>

                            <small>
                                ADMIN
                            </small>

                        </div>

                    </div>


                    <button
                        type="button"
                        className="dashboard-logout"
                        onClick={handleLogout}
                    >
                        Logout
                    </button>

                </div>

            </header>


            {/* ERROR */}

            {error && (

                <div className="dashboard-error">

                    {error}

                </div>

            )}


            {/* MAIN */}

            <main className="dashboard-main">


                {/* WELCOME */}

                <section className="dashboard-welcome">

                    <div>

                        <h2>
                            Welcome to Admin Dashboard
                        </h2>

                        <p>
                            Manage your company,
                            employees, payroll and
                            attendance from one place.
                        </p>

                    </div>

                </section>


                {/* COMPANY INFORMATION */}

                {admin && (

                    <section className="company-overview">

                        <div className="section-heading">

                            <div>

                                <h2>
                                    Company
                                </h2>

                                <p>
                                    Current company account
                                    information
                                </p>

                            </div>

                            <button
                                type="button"
                                className="small-action-button"
                                onClick={() =>
                                    navigate(
                                        "/admin/company"
                                    )
                                }
                            >
                                Open Company
                            </button>

                        </div>


                        <div className="company-overview-grid">

                            <div className="company-info-item">

                                <span>
                                    Company ID
                                </span>

                                <strong>
                                    {admin.companyId ||
                                        "-"}
                                </strong>

                            </div>


                            <div className="company-info-item">

                                <span>
                                    Company Name
                                </span>

                                <strong>
                                    {admin.companyName ||
                                        "-"}
                                </strong>

                            </div>


                            <div className="company-info-item">

                                <span>
                                    Location
                                </span>

                                <strong>
                                    {admin.location ||
                                        "-"}
                                </strong>

                            </div>


                            <div className="company-info-item">

                                <span>
                                    Admin Email
                                </span>

                                <strong>
                                    {admin.email ||
                                        "-"}
                                </strong>

                            </div>

                        </div>

                    </section>

                )}


                {/* MODULES */}

                <section className="modules-section">

                    <div className="section-heading">

                        <div>

                            <h2>
                                Administration
                            </h2>

                            <p>
                                Select a section to
                                manage your payroll
                                system.
                            </p>

                        </div>

                    </div>


                    <div className="module-grid">

                        {modules.map((module) => (

                            <button
                                key={module.id}
                                type="button"
                                className="module-card"
                                onClick={() =>
                                    handleModuleClick(
                                        module.path
                                    )
                                }
                            >

                                <div className="module-icon">
                                    {module.icon}
                                </div>


                                <div className="module-content">

                                    <h3>
                                        {module.title}
                                    </h3>

                                    <p>
                                        {module.description}
                                    </p>

                                </div>


                                <span className="module-arrow">
                                    →
                                </span>

                            </button>

                        ))}


                        {/* ADD */}

                        <button
                            type="button"
                            className="module-card add-module-card"
                            onClick={() => {

                                alert(
                                    "Additional modules will be added here."
                                );

                            }}
                        >

                            <div className="module-icon add-icon">
                                +
                            </div>


                            <div className="module-content">

                                <h3>
                                    Add +
                                </h3>

                                <p>
                                    Add more administration
                                    modules in the future.
                                </p>

                            </div>


                            <span className="module-arrow">
                                →
                            </span>

                        </button>

                    </div>

                </section>


            </main>


            {/* FOOTER */}

            <footer className="dashboard-footer">

                <p>
                    Kaisha Payroll
                </p>

                <span>
                    Admin Management System
                </span>

            </footer>

        </div>
    );
}

export default AdminDashboard;