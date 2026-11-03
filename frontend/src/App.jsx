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

import Report from "./pages/Report";
import StaffIdPassword from "./pages/StaffIdPassword";
import Calendar from "./pages/Calendar";
import Downloads from "./pages/Downloads";
import MasterDataNoSeries from "./pages/MasterDataNoSeries";
import Attendance from "./pages/Attendance";

import "./App.css";

function Root() {
    const [status, setStatus] = useState("checking");

    useEffect(() => {
        fetch(
            import.meta.env.VITE_API_BASE_URL + "/auth/registration-status"
        )
            .then(response => {
                if (!response.ok) {
                    throw new Error("Registration status API failed");
                }

                return response.json();
            })
            .then(data => {
                if (data.registrationAvailable === true) {
                    setStatus("register");
                } else {
                    setStatus("login");
                }
            })
            .catch(error => {
                console.error("Registration check failed:", error);
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
                    fontFamily: "Arial, Helvetica, sans-serif",
                    fontSize: "18px"
                }}
            >
                Checking application status...
            </div>
        );
    }

    if (status === "register") {
        return <Register />;
    }

    return <LoginSelection />;
}

function RegisterGuard() {
    const [status, setStatus] = useState("checking");

    useEffect(() => {
        fetch(
            import.meta.env.VITE_API_BASE_URL + "/auth/registration-status"
        )
            .then(response => {
                if (!response.ok) {
                    throw new Error("Registration status API failed");
                }

                return response.json();
            })
            .then(data => {
                if (data.registrationAvailable === true) {
                    setStatus("register");
                } else {
                    setStatus("login");
                }
            })
            .catch(error => {
                console.error("Registration status failed:", error);
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
                    fontFamily: "Arial, Helvetica, sans-serif",
                    fontSize: "18px"
                }}
            >
                Checking registration status...
            </div>
        );
    }

    if (status === "login") {
        return <Navigate to="/login" replace />;
    }

    return <Register />;
}

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route
                    path="/"
                    element={<Root />}
                />

                <Route
                    path="/login"
                    element={<LoginSelection />}
                />

                <Route
                    path="/admin-login"
                    element={<AdminLogin />}
                />

                <Route
                    path="/staff-login"
                    element={<StaffLogin />}
                />

                <Route
                    path="/register"
                    element={<RegisterGuard />}
                />

                <Route
                    path="/forgot-password"
                    element={<ForgotPassword />}
                />

                <Route
                    path="/admin-dashboard"
                    element={
                        <ProtectedRoute requiredRole="ADMIN">
                            <AdminDashboard />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/admin/company"
                    element={
                        <ProtectedRoute requiredRole="ADMIN">
                            <Company />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/admin/master-data"
                    element={
                        <ProtectedRoute requiredRole="ADMIN">
                            <MasterDataNoSeries />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/admin/employee-create"
                    element={
                        <ProtectedRoute requiredRole="ADMIN">
                            <EmployeeDataCreate />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/admin/department"
                    element={
                        <ProtectedRoute requiredRole="ADMIN">
                            <Department />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/admin/salary"
                    element={
                        <ProtectedRoute requiredRole="ADMIN">
                            <Salary />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/admin/report"
                    element={
                        <ProtectedRoute requiredRole="ADMIN">
                            <Report />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/admin/attendance"
                    element={
                        <ProtectedRoute requiredRole="ADMIN">
                            <Attendance />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/admin/staff-id-password"
                    element={
                        <ProtectedRoute requiredRole="ADMIN">
                            <StaffIdPassword />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/admin/calendar"
                    element={
                        <ProtectedRoute requiredRole="ADMIN">
                            <Calendar />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/admin/help-support"
                    element={
                        <ProtectedRoute requiredRole="ADMIN">
                            <AdminModulePage
                                title="Help & Support"
                                description="Manage staff and employee support queries."
                            />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/admin/downloads"
                    element={
                        <ProtectedRoute requiredRole="ADMIN">
                            <Downloads />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/admin/salary-components"
                    element={
                        <ProtectedRoute requiredRole="ADMIN">
                            <AdminModulePage
                                title="Salary Component of Every Employee"
                                description="Manage salary components for individual employees."
                            />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/staff-dashboard"
                    element={
                        <ProtectedRoute requiredRole="STAFF">
                            <StaffDashboard />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="*"
                    element={<Navigate to="/" replace />}
                />
            </Routes>
        </BrowserRouter>
    );
}

export default App;