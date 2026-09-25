import React, { useEffect, useMemo, useState } from "react";
import "./Report.css";

const API_BASE_URL = "http://localhost:8080/api";

const getToken = () => localStorage.getItem("token");

const Report = () => {
    const [payPeriod, setPayPeriod] = useState(
        new Date().toISOString().slice(0, 7)
    );

    const [payrollRecords, setPayrollRecords] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const [reportType, setReportType] = useState("PAYROLL");
    const [search, setSearch] = useState("");

    const loadPayroll = async () => {
        try {
            setLoading(true);
            setError("");

            const token = getToken();

            const response = await fetch(
                `${API_BASE_URL}/payroll/period/${payPeriod}`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            if (!response.ok) {
                throw new Error("Unable to load payroll report.");
            }

            const data = await response.json();

            setPayrollRecords(Array.isArray(data) ? data : []);
        } catch (err) {
            setError(err.message || "Failed to load report.");
            setPayrollRecords([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadPayroll();
    }, [payPeriod]);

    const filteredRecords = useMemo(() => {
        const keyword = search.trim().toLowerCase();

        if (!keyword) {
            return payrollRecords;
        }

        return payrollRecords.filter((record) => {
            return (
                String(record.employeeId || "")
                    .toLowerCase()
                    .includes(keyword) ||
                String(record.status || "")
                    .toLowerCase()
                    .includes(keyword)
            );
        });
    }, [payrollRecords, search]);

    const summary = useMemo(() => {
        const gross = filteredRecords.reduce(
            (total, item) => total + Number(item.grossSalary || 0),
            0
        );

        const deductions = filteredRecords.reduce(
            (total, item) => total + Number(item.totalDeductions || 0),
            0
        );

        const net = filteredRecords.reduce(
            (total, item) => total + Number(item.netSalary || 0),
            0
        );

        return {
            employees: filteredRecords.length,
            gross,
            deductions,
            net,
        };
    }, [filteredRecords]);

    const formatAmount = (value) => {
        return Number(value || 0).toLocaleString("en-IN", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2,
        });
    };

    const formatMonth = (value) => {
        if (!value) return "";

        const date = new Date(`${value}-01`);

        return date.toLocaleDateString("en-IN", {
            month: "long",
            year: "numeric",
        });
    };

    const exportCsv = () => {
        if (filteredRecords.length === 0) {
            alert("No records available for export.");
            return;
        }

        const headers = [
            "Employee ID",
            "Pay Period",
            "Gross Salary",
            "Total Deductions",
            "Net Salary",
            "Status",
        ];

        const rows = filteredRecords.map((record) => [
            record.employeeId || "",
            record.payPeriod || payPeriod,
            record.grossSalary || 0,
            record.totalDeductions || 0,
            record.netSalary || 0,
            record.status || "",
        ]);

        const csv = [
            headers,
            ...rows,
        ]
            .map((row) =>
                row
                    .map((value) =>
                        `"${String(value).replace(/"/g, '""')}"`
                    )
                    .join(",")
            )
            .join("\n");

        const blob = new Blob([csv], {
            type: "text/csv;charset=utf-8;",
        });

        const url = window.URL.createObjectURL(blob);

        const link = document.createElement("a");
        link.href = url;
        link.download = `Payroll_Report_${payPeriod}.csv`;

        document.body.appendChild(link);
        link.click();
        link.remove();

        window.URL.revokeObjectURL(url);
    };

    return (
        <div className="report-page">

            <div className="report-header">
                <div>
                    <h1>Reports</h1>
                    <p>
                        View and manage payroll reports.
                    </p>
                </div>

                <button
                    className="report-export-button"
                    onClick={exportCsv}
                >
                    Export CSV
                </button>
            </div>

            <div className="report-filter-card">

                <div className="report-filter-group">
                    <label>Report Type</label>

                    <select
                        value={reportType}
                        onChange={(e) => setReportType(e.target.value)}
                    >
                        <option value="PAYROLL">
                            Payroll Report
                        </option>

                        <option value="SALARY">
                            Salary Report
                        </option>

                        <option value="ATTENDANCE">
                            Attendance Report
                        </option>
                    </select>
                </div>

                <div className="report-filter-group">
                    <label>Period</label>

                    <input
                        type="month"
                        value={payPeriod}
                        onChange={(e) => setPayPeriod(e.target.value)}
                    />
                </div>

                <div className="report-filter-group report-search-group">
                    <label>Search Employee</label>

                    <input
                        type="text"
                        placeholder="Search Employee ID..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </div>

            </div>

            <div className="report-summary-grid">

                <div className="report-summary-card">
                    <span className="report-summary-label">
                        Employees
                    </span>

                    <strong>
                        {summary.employees}
                    </strong>
                </div>

                <div className="report-summary-card">
                    <span className="report-summary-label">
                        Gross Salary
                    </span>

                    <strong>
                        ₹{formatAmount(summary.gross)}
                    </strong>
                </div>

                <div className="report-summary-card">
                    <span className="report-summary-label">
                        Total Deductions
                    </span>

                    <strong>
                        ₹{formatAmount(summary.deductions)}
                    </strong>
                </div>

                <div className="report-summary-card">
                    <span className="report-summary-label">
                        Net Salary
                    </span>

                    <strong>
                        ₹{formatAmount(summary.net)}
                    </strong>
                </div>

            </div>

            <div className="report-table-card">

                <div className="report-table-header">
                    <div>
                        <h2>
                            {reportType === "PAYROLL"
                                ? "Payroll Report"
                                : reportType === "SALARY"
                                    ? "Salary Report"
                                    : "Attendance Report"}
                        </h2>

                        <span>
                            {formatMonth(payPeriod)}
                        </span>
                    </div>

                    <span className="report-record-count">
                        {filteredRecords.length} records
                    </span>
                </div>

                {loading ? (
                    <div className="report-state">
                        Loading report...
                    </div>
                ) : error ? (
                    <div className="report-state report-error">
                        {error}
                    </div>
                ) : filteredRecords.length === 0 ? (
                    <div className="report-state">
                        No payroll records found for this period.
                    </div>
                ) : (
                    <div className="report-table-wrapper">
                        <table className="report-table">

                            <thead>
                            <tr>
                                <th>Employee ID</th>
                                <th>Pay Period</th>
                                <th>Gross Salary</th>
                                <th>Deductions</th>
                                <th>Net Salary</th>
                                <th>Status</th>
                            </tr>
                            </thead>

                            <tbody>
                            {filteredRecords.map((record) => (
                                <tr key={record.payrollId}>

                                    <td>
                                        <strong>
                                            {record.employeeId}
                                        </strong>
                                    </td>

                                    <td>
                                        {record.payPeriod}
                                    </td>

                                    <td>
                                        ₹{formatAmount(
                                        record.grossSalary
                                    )}
                                    </td>

                                    <td>
                                        ₹{formatAmount(
                                        record.totalDeductions
                                    )}
                                    </td>

                                    <td className="report-net">
                                        ₹{formatAmount(
                                        record.netSalary
                                    )}
                                    </td>

                                    <td>
                                            <span
                                                className={`report-status ${String(
                                                    record.status || ""
                                                ).toLowerCase()}`}
                                            >
                                                {record.status || "PROCESSED"}
                                            </span>
                                    </td>

                                </tr>
                            ))}
                            </tbody>

                        </table>
                    </div>
                )}

            </div>

        </div>
    );
};

export default Report;