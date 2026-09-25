import React from "react";
import { NavLink } from "react-router-dom";
import "./AdminNavbar.css";

const AdminNavbar = () => {
    return (
        <nav className="admin-navbar">
            <div className="admin-navbar-left">
                <NavLink to="/admin/company" className="admin-nav-link">
                    Company
                </NavLink>

                <NavLink to="/admin/master-data" className="admin-nav-link">
                    Master Data
                </NavLink>

                <NavLink to="/admin/employee-create" className="admin-nav-link">
                    Employee
                </NavLink>

                <NavLink to="/admin/department" className="admin-nav-link">
                    Department
                </NavLink>

                <NavLink to="/admin/salary" className="admin-nav-link">
                    Salary
                </NavLink>

                <NavLink to="/admin/report" className="admin-nav-link">
                    Report
                </NavLink>

                <NavLink to="/admin/attendance" className="admin-nav-link">
                    Attendance
                </NavLink>

                <NavLink to="/admin/staff-id-password" className="admin-nav-link">
                    Staff ID & Password
                </NavLink>


                <NavLink to="/admin/downloads" className="admin-nav-link">
                    Downloads
                </NavLink>

                <NavLink to="/admin/help-support" className="admin-nav-link">
                    Help & Support
                </NavLink>
                <NavLink to="/admin/calendar" className="admin-nav-link">
                    Calendar
                </NavLink>


            </div>
        </nav>
    );
};

export default AdminNavbar;