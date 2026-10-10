import React, { useEffect, useMemo, useState } from "react";
import "./Downloads.css";
import AdminLayout from "../pages/AdminLayout";
const API_BASE_URL = "http://localhost:8080/api";

const DOCUMENTS = [
    {
        id: "PAYROLL_SLIP",
        label: "Payroll Slip",
    },
    {
        id: "OFFER_LETTER",
        label: "Offer Letter",
    },
    {
        id: "APPOINTMENT_LETTER",
        label: "Appointment Letter",
    },
    {
        id: "PF_CHALLAN",
        label: "PF Challan",
    },
    {
        id: "INCOME_TAX_DOCUMENT",
        label: "Income Tax Document",
    },
];

const getToken = () => localStorage.getItem("token");

const Downloads = () => {
    const [employees, setEmployees] = useState([]);

    const [scope, setScope] = useState("ALL");

    const [selectedEmployees, setSelectedEmployees] = useState([]);

    const [employeeSearch, setEmployeeSearch] = useState("");

    const [documentTypes, setDocumentTypes] = useState([]);

    const [period, setPeriod] = useState(
        new Date().toISOString().slice(0, 7)
    );

    const [fromRecord, setFromRecord] = useState("");
    const [toRecord, setToRecord] = useState("");

    const [downloading, setDownloading] = useState(false);

    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    useEffect(() => {
        loadEmployees();
    }, []);

    const loadEmployees = async () => {
        try {
            const response = await fetch(
                `${API_BASE_URL}/employees`,
                {
                    headers: {
                        Authorization: `Bearer ${getToken()}`,
                    },
                }
            );

            if (!response.ok) {
                throw new Error("Unable to load employees.");
            }

            const data = await response.json();

            setEmployees(Array.isArray(data) ? data : []);
        } catch (err) {
            setError(
                err.message ||
                "Unable to load employees."
            );
        }
    };

    const filteredEmployees = useMemo(() => {
        const keyword = employeeSearch
            .trim()
            .toLowerCase();

        if (!keyword) {
            return employees;
        }

        return employees.filter((employee) => {
            return (
                String(employee.empId || employee.employeeId || "")
                    .toLowerCase()
                    .includes(keyword) ||
                String(employee.name || "")
                    .toLowerCase()
                    .includes(keyword)
            );
        });
    }, [employees, employeeSearch]);

    const getEmployeeId = (employee) => {
        return employee.empId || employee.employeeId || employee.id;
    };

    const getEmployeeName = (employee) => {
        return employee.name || employee.employeeName || "Employee";
    };

    const toggleEmployee = (employeeId) => {
        setSelectedEmployees((previous) => {
            if (previous.includes(employeeId)) {
                return previous.filter(
                    (id) => id !== employeeId
                );
            }

            return [...previous, employeeId];
        });
    };

    const toggleDocument = (documentId) => {
        setDocumentTypes((previous) => {
            if (previous.includes(documentId)) {
                return previous.filter(
                    (id) => id !== documentId
                );
            }

            return [...previous, documentId];
        });
    };

    const selectAllEmployees = () => {
        setSelectedEmployees(
            filteredEmployees.map(getEmployeeId)
        );
    };

    const clearEmployees = () => {
        setSelectedEmployees([]);
    };

    const handleDownload = async () => {
        setMessage("");
        setError("");

        if (documentTypes.length === 0) {
            setError(
                "Please select at least one document type."
            );
            return;
        }

        if (
            scope === "SELECTED" &&
            selectedEmployees.length === 0
        ) {
            setError(
                "Please select at least one employee."
            );
            return;
        }

        if (scope === "RANGE") {
            if (!fromRecord || !toRecord) {
                setError(
                    "Please enter From and To record numbers."
                );
                return;
            }

            if (
                Number(fromRecord) <= 0 ||
                Number(toRecord) <= 0 ||
                Number(fromRecord) > Number(toRecord)
            ) {
                setError(
                    "Please enter a valid record range."
                );
                return;
            }
        }

        try {
            setDownloading(true);

            /*
             * This endpoint is intentionally prepared for the
             * new Downloads module.
             *
             * Backend implementation will handle:
             * Payroll Slip
             * Offer Letter
             * Appointment Letter
             * PF Challan
             * Income Tax Document
             */

            const response = await fetch(
                `${API_BASE_URL}/downloads`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${getToken()}`,
                    },
                    body: JSON.stringify({
                        period,
                        scope,
                        employeeIds:
                            scope === "SELECTED"
                                ? selectedEmployees
                                : [],
                        fromRecord:
                            scope === "RANGE"
                                ? Number(fromRecord)
                                : null,
                        toRecord:
                            scope === "RANGE"
                                ? Number(toRecord)
                                : null,
                        documentTypes,
                    }),
                }
            );

            if (!response.ok) {
                const data =
                    await response.json().catch(() => ({}));

                throw new Error(
                    data.message ||
                    "Download service is not available yet."
                );
            }

            const blob = await response.blob();

            const url =
                window.URL.createObjectURL(blob);

            const link =
                document.createElement("a");

            link.href = url;
            link.download =
                `Downloads_${period}.zip`;

            document.body.appendChild(link);

            link.click();

            link.remove();

            window.URL.revokeObjectURL(url);

            setMessage(
                "Documents downloaded successfully."
            );
        } catch (err) {
            setError(
                err.message ||
                "Unable to download documents."
            );
        } finally {
            setDownloading(false);
        }
    };

    return (
        <AdminLayout>
        <div className="downloads-page">

            <div className="downloads-header">

                <div>
                    <h1>Downloads</h1>

                    <p>
                        Download payroll and employee documents.
                    </p>
                </div>

            </div>

            {message && (
                <div className="downloads-success">
                    {message}
                </div>
            )}

            {error && (
                <div className="downloads-error">
                    {error}
                </div>
            )}

            <div className="downloads-card">

                <div className="downloads-section">

                    <h2>
                        Select Employee
                    </h2>

                    <div className="downloads-scope-options">

                        <label className="downloads-radio-option">

                            <input
                                type="radio"
                                name="scope"
                                value="ALL"
                                checked={scope === "ALL"}
                                onChange={() =>
                                    setScope("ALL")
                                }
                            />

                            <span>
                                All Employees
                            </span>

                        </label>

                        <label className="downloads-radio-option">

                            <input
                                type="radio"
                                name="scope"
                                value="SELECTED"
                                checked={scope === "SELECTED"}
                                onChange={() =>
                                    setScope("SELECTED")
                                }
                            />

                            <span>
                                Selected Employees
                            </span>

                        </label>

                        <label className="downloads-radio-option">

                            <input
                                type="radio"
                                name="scope"
                                value="RANGE"
                                checked={scope === "RANGE"}
                                onChange={() =>
                                    setScope("RANGE")
                                }
                            />

                            <span>
                                Record Range
                            </span>

                        </label>

                    </div>

                </div>

                {scope === "SELECTED" && (
                    <div className="downloads-selected-section">

                        <div className="downloads-search-row">

                            <input
                                type="text"
                                placeholder="Search Employee ID / Name"
                                value={employeeSearch}
                                onChange={(e) =>
                                    setEmployeeSearch(
                                        e.target.value
                                    )
                                }
                            />

                            <button
                                type="button"
                                onClick={selectAllEmployees}
                            >
                                Select All
                            </button>

                            <button
                                type="button"
                                onClick={clearEmployees}
                            >
                                Clear
                            </button>

                        </div>

                        <div className="downloads-employee-list">

                            {filteredEmployees.length === 0 ? (
                                <div className="downloads-empty">
                                    No employees found.
                                </div>
                            ) : (
                                filteredEmployees.map(
                                    (employee) => {
                                        const id =
                                            getEmployeeId(
                                                employee
                                            );

                                        return (
                                            <label
                                                key={id}
                                                className="downloads-employee-item"
                                            >

                                                <input
                                                    type="checkbox"
                                                    checked={selectedEmployees.includes(
                                                        id
                                                    )}
                                                    onChange={() =>
                                                        toggleEmployee(
                                                            id
                                                        )
                                                    }
                                                />

                                                <span>
                                                    {id} -{" "}
                                                    {getEmployeeName(
                                                        employee
                                                    )}
                                                </span>

                                            </label>
                                        );
                                    }
                                )
                            )}

                        </div>

                    </div>
                )}

                {scope === "RANGE" && (
                    <div className="downloads-range-section">

                        <div className="downloads-field">

                            <label>
                                From Record
                            </label>

                            <input
                                type="number"
                                min="1"
                                value={fromRecord}
                                onChange={(e) =>
                                    setFromRecord(
                                        e.target.value
                                    )
                                }
                                placeholder="1"
                            />

                        </div>

                        <div className="downloads-field">

                            <label>
                                To Record
                            </label>

                            <input
                                type="number"
                                min="1"
                                value={toRecord}
                                onChange={(e) =>
                                    setToRecord(
                                        e.target.value
                                    )
                                }
                                placeholder="10"
                            />

                        </div>

                    </div>
                )}

                <div className="downloads-section">

                    <h2>
                        Document Type
                    </h2>

                    <div className="downloads-document-list">

                        {DOCUMENTS.map((document) => (
                            <label
                                key={document.id}
                                className="downloads-document-item"
                            >

                                <input
                                    type="checkbox"
                                    checked={documentTypes.includes(
                                        document.id
                                    )}
                                    onChange={() =>
                                        toggleDocument(
                                            document.id
                                        )
                                    }
                                />

                                <span>
                                    {document.label}
                                </span>

                            </label>
                        ))}

                    </div>

                </div>

                <div className="downloads-section">

                    <h2>
                        Period
                    </h2>

                    <div className="downloads-period">

                        <input
                            type="month"
                            value={period}
                            onChange={(e) =>
                                setPeriod(
                                    e.target.value
                                )
                            }
                        />

                    </div>

                </div>

                <div className="downloads-footer">

                    <div className="downloads-summary">

                        <span>
                            {documentTypes.length} document type(s)
                        </span>

                        <span>
                            {scope === "ALL"
                                ? "All employees"
                                : scope === "SELECTED"
                                    ? `${selectedEmployees.length} employee(s)`
                                    : `Records ${fromRecord || "-"} to ${toRecord || "-"}`}
                        </span>

                    </div>

                    <button
                        className="downloads-button"
                        onClick={handleDownload}
                        disabled={downloading}
                    >
                        {downloading
                            ? "Preparing..."
                            : "Download"}
                    </button>

                </div>

            </div>

        </div>
        </AdminLayout>
    );
};

export default Downloads;