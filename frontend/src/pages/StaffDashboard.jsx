import React, { useEffect, useState } from "react";

const API_URL = "http://localhost:8080/api";

function StaffDashboard() {
    const [staff, setStaff] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const fetchStaffDetails = async () => {
            try {
                const token = localStorage.getItem("token");

                if (!token) {
                    setError("You are not logged in.");
                    setLoading(false);
                    return;
                }

                const response = await fetch(`${API_URL}/me`, {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json",
                    },
                });

                if (!response.ok) {
                    throw new Error("Failed to load staff details.");
                }

                const data = await response.json();

                setStaff(data);
            } catch (err) {
                console.error("Staff dashboard error:", err);
                setError(err.message || "Unable to load staff details.");
            } finally {
                setLoading(false);
            }
        };

        fetchStaffDetails();
    }, []);

    if (loading) {
        return (
            <div className="dashboard-container">
                <h2>Loading...</h2>
            </div>
        );
    }

    if (error) {
        return (
            <div className="dashboard-container">
                <h2>Staff Dashboard</h2>
                <p>{error}</p>
            </div>
        );
    }

    return (
        <div className="dashboard-container">
            <div className="dashboard-header">
                <h1>Staff Dashboard</h1>

                <p>
                    Welcome,{" "}
                    <strong>
                        {staff?.username || staff?.email || "Staff"}
                    </strong>
                </p>
            </div>

            <div className="company-info-card">
                <h2>Company Information</h2>

                <div className="company-info-item">
                    <span>Company Name</span>
                    <strong>
                        {staff?.companyName || "Not available"}
                    </strong>
                </div>
            </div>
        </div>
    );
}

export default StaffDashboard;