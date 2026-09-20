import { Navigate } from "react-router-dom";

function ProtectedRoute({
                            children,
                            requiredRole
                        }) {

    const token =
        localStorage.getItem("token");

    const role =
        localStorage.getItem("role");

    // No login
    if (!token) {
        return (
            <Navigate
                to="/login"
                replace
            />
        );
    }

    // Wrong role
    if (
        requiredRole &&
        role !== requiredRole
    ) {

        if (role === "ADMIN") {
            return (
                <Navigate
                    to="/admin-dashboard"
                    replace
                />
            );
        }

        if (role === "STAFF") {
            return (
                <Navigate
                    to="/staff-dashboard"
                    replace
                />
            );
        }

        localStorage.clear();

        return (
            <Navigate
                to="/login"
                replace
            />
        );
    }

    return children;
}

export default ProtectedRoute;