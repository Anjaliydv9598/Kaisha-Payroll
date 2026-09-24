import {
    BrowserRouter,
    Routes,
    Route,
    Navigate
} from "react-router-dom";

import {
    useEffect,
    useState
} from "react";

import LoginSelection from "./pages/LoginSelection";
import AdminLogin from "./pages/AdminLogin";
import StaffLogin from "./pages/StaffLogin";

import Register from "./pages/Register";
import ForgotPassword from "./pages/ForgotPassword";

import AdminDashboard from "./pages/AdminDashboard";
import AdminModulePage from "./pages/AdminModulePage";
import Department from "./pages/Department";

import StaffDashboard from "./pages/StaffDashboard";
import Company from "./pages/Company";

import ProtectedRoute from "./components/ProtectedRoute";
import EmployeeDataCreate from "./pages/EmployeeDataCreate";
import Salary from "./pages/Salary";

import "./App.css";


// ======================================================
// ROOT
// ======================================================

function Root() {

    const [status, setStatus] =
        useState("checking");


    useEffect(() => {

        fetch(
            "http://localhost:8080/api/auth/registration-status"
        )
            .then(response => {

                if (!response.ok) {

                    throw new Error(
                        "Registration status API failed"
                    );
                }

                return response.json();
            })
            .then(data => {

                if (
                    data.registrationAvailable === true
                ) {

                    setStatus("register");

                } else {

                    setStatus("login");
                }

            })
            .catch(error => {

                console.error(
                    "Registration check failed:",
                    error
                );

                setStatus("login");
            });

    }, []);


    if (status === "checking") {

        return (
            <div
                style={{
                    minHeight: "100vh",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    fontFamily:
                        "Arial, Helvetica, sans-serif",
                    fontSize: "18px"
                }}
            >
                Checking application status...
            </div>
        );
    }


    /*
     * First-time application:
     * Show registration page.
     */
    if (status === "register") {

        return <Register />;
    }


    /*
     * Company already registered:
     * Show login selection.
     */
    return <LoginSelection />;
}


// ======================================================
// REGISTER GUARD
// ======================================================

function RegisterGuard() {

    const [status, setStatus] =
        useState("checking");


    useEffect(() => {

        fetch(
            "http://localhost:8080/api/auth/registration-status"
        )
            .then(response => {

                if (!response.ok) {

                    throw new Error(
                        "Registration status API failed"
                    );
                }

                return response.json();
            })
            .then(data => {

                if (
                    data.registrationAvailable === true
                ) {

                    setStatus("register");

                } else {

                    setStatus("login");
                }

            })
            .catch(error => {

                console.error(
                    "Registration status failed:",
                    error
                );

                setStatus("login");
            });

    }, []);


    if (status === "checking") {

        return (
            <div
                style={{
                    minHeight: "100vh",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    fontFamily:
                        "Arial, Helvetica, sans-serif",
                    fontSize: "18px"
                }}
            >
                Checking registration status...
            </div>
        );
    }


    if (status === "login") {

        return (
            <Navigate
                to="/login"
                replace
            />
        );
    }


    return <Register />;
}


// ======================================================
// APP
// ======================================================

function App() {

    return (
        <BrowserRouter>

            <Routes>

                {/* =================================================
                    ROOT
                ================================================= */}

                <Route
                    path="/"
                    element={<Root />}
                />


                {/* =================================================
                    LOGIN SELECTION
                ================================================= */}

                <Route
                    path="/login"
                    element={<LoginSelection />}
                />


                {/* =================================================
                    ADMIN LOGIN
                ================================================= */}

                <Route
                    path="/admin-login"
                    element={<AdminLogin />}
                />


                {/* =================================================
                    STAFF LOGIN
                ================================================= */}

                <Route
                    path="/staff-login"
                    element={<StaffLogin />}
                />


                {/* =================================================
                    REGISTER
                ================================================= */}

                <Route
                    path="/register"
                    element={<RegisterGuard />}
                />


                {/* =================================================
                    FORGOT PASSWORD
                ================================================= */}

                <Route
                    path="/forgot-password"
                    element={<ForgotPassword />}
                />


                {/* =================================================
                    ADMIN DASHBOARD
                ================================================= */}

                <Route
                    path="/admin-dashboard"
                    element={
                        <ProtectedRoute
                            requiredRole="ADMIN"
                        >
                            <AdminDashboard />
                        </ProtectedRoute>
                    }
                />


                {/* =================================================
                    ADMIN COMPANY
                ================================================= */}

                <Route
                    path="/admin/company"
                    element={
                        <ProtectedRoute
                            requiredRole="ADMIN"
                        >
                            <Company />
                        </ProtectedRoute>
                    }
                />


                {/* =================================================
                    ADMIN HELP & SUPPORT
                ================================================= */}

                <Route
                    path="/admin/help-support"
                    element={
                        <ProtectedRoute
                            requiredRole="ADMIN"
                        >
                            <AdminModulePage
                                title="Help & Support"
                                description="Manage staff and employee support queries."
                            />
                        </ProtectedRoute>
                    }
                />


                {/* =================================================
                    ADMIN DEPARTMENT
                ================================================= */}

                <Route
                    path="/admin/department"
                    element={
                        <ProtectedRoute
                            requiredRole="ADMIN"
                        >
                            <Department />
                        </ProtectedRoute>
                    }
                />


                {/* =================================================
                    ADMIN SALARY
                ================================================= */}

                <Route
                    path="/admin/salary"
                    element={
                        <ProtectedRoute
                            requiredRole="ADMIN"
                        >
                            <Salary />
                        </ProtectedRoute>
                    }
                />


                {/* =================================================
                    MASTER DATA
                ================================================= */}

                <Route
                    path="/admin/master-data"
                    element={
                        <ProtectedRoute
                            requiredRole="ADMIN"
                        >
                            <AdminModulePage
                                title="Master Data No. Series"
                                description="Manage employee and document number series."
                            />
                        </ProtectedRoute>
                    }
                />


                {/* =================================================
                    EMPLOYEE DATA CREATE
                    ADMIN + STAFF
                ================================================= */}

                <Route
                    path="/admin/employee-create"
                    element={
                        <ProtectedRoute>
                            <EmployeeDataCreate />
                        </ProtectedRoute>
                    }
                />


                {/* =================================================
                    SALARY COMPONENTS
                ================================================= */}

                <Route
                    path="/admin/salary-components"
                    element={
                        <ProtectedRoute
                            requiredRole="ADMIN"
                        >
                            <AdminModulePage
                                title="Salary Component of Every Employee"
                                description="Manage salary components for individual employees."
                            />
                        </ProtectedRoute>
                    }
                />


                {/* =================================================
                    ATTENDANCE
                ================================================= */}

                <Route
                    path="/admin/attendance"
                    element={
                        <ProtectedRoute
                            requiredRole="ADMIN"
                        >
                            <AdminModulePage
                                title="Attendance"
                                description="Manage employee attendance and punch records."
                            />
                        </ProtectedRoute>
                    }
                />


                {/* =================================================
                    STAFF DASHBOARD
                ================================================= */}

                <Route
                    path="/staff-dashboard"
                    element={
                        <ProtectedRoute
                            requiredRole="STAFF"
                        >
                            <StaffDashboard />
                        </ProtectedRoute>
                    }
                />


                {/* =================================================
                    UNKNOWN ROUTE
                ================================================= */}

                <Route
                    path="*"
                    element={
                        <Navigate
                            to="/"
                            replace
                        />
                    }
                />

            </Routes>

        </BrowserRouter>
    );
}


export default App;