import React, { useState } from "react";

import { downloadSalary } from "../../services/salaryServices";

function SalaryDownload({
                            employees = [],
                            salaryRecords = [],
                            onClose,
                            onSuccess,
                            onError
                        }) {

    const [payPeriod, setPayPeriod] =
        useState(
            new Date()
                .toISOString()
                .slice(0, 7)
        );

    const [scope, setScope] =
        useState("ALL");

    const [selectedEmployees, setSelectedEmployees] =
        useState([]);

    const [employeeSearch, setEmployeeSearch] =
        useState("");

    const [fromRecord, setFromRecord] =
        useState("");

    const [toRecord, setToRecord] =
        useState("");

    const [format, setFormat] =
        useState("PDF");

    const [downloading, setDownloading] =
        useState(false);


    // =====================================================
    // FILTER EMPLOYEES
    // =====================================================

    const filteredEmployees =
        employees.filter((employee) => {

            const employeeId =
                employee.employeeId ||
                employee.empId ||
                employee.id ||
                "";

            const employeeName =
                employee.name ||
                employee.employeeName ||
                "";

            const search =
                employeeSearch
                    .trim()
                    .toLowerCase();

            if (!search) {
                return true;
            }

            return (
                String(employeeId)
                    .toLowerCase()
                    .includes(search) ||
                String(employeeName)
                    .toLowerCase()
                    .includes(search)
            );
        });


    // =====================================================
    // EMPLOYEE ID
    // =====================================================

    const getEmployeeId = (employee) => {

        return (
            employee.employeeId ||
            employee.empId ||
            employee.id ||
            ""
        );
    };


    // =====================================================
    // EMPLOYEE NAME
    // =====================================================

    const getEmployeeName = (employee) => {

        return (
            employee.name ||
            employee.employeeName ||
            "Unknown Employee"
        );
    };


    // =====================================================
    // TOGGLE EMPLOYEE
    // =====================================================

    const handleEmployeeToggle = (employeeId) => {

        setSelectedEmployees((previous) => {

            if (previous.includes(employeeId)) {

                return previous.filter(
                    (id) => id !== employeeId
                );
            }

            return [
                ...previous,
                employeeId
            ];
        });
    };


    // =====================================================
    // SELECT ALL
    // =====================================================

    const handleSelectAll = () => {

        const ids =
            filteredEmployees
                .map(getEmployeeId)
                .filter(Boolean);

        setSelectedEmployees(ids);
    };


    // =====================================================
    // CLEAR SELECTION
    // =====================================================

    const handleClearSelection = () => {

        setSelectedEmployees([]);
    };


    // =====================================================
    // DOWNLOAD
    // =====================================================

    const handleDownload = async () => {

        if (!payPeriod) {

            onError?.(
                "Please select a pay period."
            );

            return;
        }


        if (
            scope === "SELECTED" &&
            selectedEmployees.length === 0
        ) {

            onError?.(
                "Please select at least one employee."
            );

            return;
        }


        if (scope === "RANGE") {

            if (
                !fromRecord ||
                !toRecord
            ) {

                onError?.(
                    "Please enter both From and To record numbers."
                );

                return;
            }

            if (
                Number(fromRecord) < 1 ||
                Number(toRecord) < 1
            ) {

                onError?.(
                    "Record numbers must be greater than zero."
                );

                return;
            }

            if (
                Number(fromRecord) >
                Number(toRecord)
            ) {

                onError?.(
                    "From record cannot be greater than To record."
                );

                return;
            }
        }


        try {

            setDownloading(true);

            await downloadSalary({
                payPeriod,
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
                format
            });

            onSuccess?.(
                "Salary downloaded successfully."
            );

            onClose?.();

        } catch (error) {

            console.error(
                "SALARY DOWNLOAD ERROR:",
                error
            );

            onError?.(
                error.message ||
                "Unable to download salary."
            );

        } finally {

            setDownloading(false);
        }
    };


    return (
        <div className="salary-download-overlay">

            <div className="salary-download-modal">

                {/* =================================================
                    HEADER
                ================================================= */}

                <div className="salary-download-header">

                    <div>

                        <h2>
                            Download Salary
                        </h2>

                        <p>
                            Download salary and payroll
                            records for selected employees.
                        </p>

                    </div>

                    <button
                        type="button"
                        className="salary-download-close"
                        onClick={onClose}
                        disabled={downloading}
                    >
                        ×
                    </button>

                </div>


                {/* =================================================
                    BODY
                ================================================= */}

                <div className="salary-download-body">

                    {/* PAY PERIOD */}

                    <div className="salary-download-field">

                        <label>
                            Pay Period
                        </label>

                        <input
                            type="month"
                            value={payPeriod}
                            onChange={(e) =>
                                setPayPeriod(
                                    e.target.value
                                )
                            }
                            disabled={downloading}
                        />

                    </div>


                    {/* SCOPE */}

                    <div className="salary-download-field">

                        <label>
                            Download For
                        </label>

                        <div className="salary-download-options">

                            <label className="salary-radio-option">

                                <input
                                    type="radio"
                                    name="downloadScope"
                                    value="ALL"
                                    checked={
                                        scope === "ALL"
                                    }
                                    onChange={() => {
                                        setScope("ALL");
                                        setSelectedEmployees([]);
                                    }}
                                    disabled={downloading}
                                />

                                <span>
                                    All Employees
                                </span>

                            </label>


                            <label className="salary-radio-option">

                                <input
                                    type="radio"
                                    name="downloadScope"
                                    value="SELECTED"
                                    checked={
                                        scope === "SELECTED"
                                    }
                                    onChange={() =>
                                        setScope("SELECTED")
                                    }
                                    disabled={downloading}
                                />

                                <span>
                                    Selected Employees
                                </span>

                            </label>


                            <label className="salary-radio-option">

                                <input
                                    type="radio"
                                    name="downloadScope"
                                    value="RANGE"
                                    checked={
                                        scope === "RANGE"
                                    }
                                    onChange={() =>
                                        setScope("RANGE")
                                    }
                                    disabled={downloading}
                                />

                                <span>
                                    Custom Record Range
                                </span>

                            </label>

                        </div>

                    </div>


                    {/* SELECTED EMPLOYEES */}

                    {scope === "SELECTED" && (

                        <div className="salary-download-selection">

                            <div className="salary-download-selection-header">

                                <label>
                                    Select Employees
                                </label>

                                <span>
                                    {selectedEmployees.length}
                                    {" "}
                                    selected
                                </span>

                            </div>


                            <input
                                type="text"
                                className="salary-download-search"
                                placeholder="Search employee ID or name..."
                                value={employeeSearch}
                                onChange={(e) =>
                                    setEmployeeSearch(
                                        e.target.value
                                    )
                                }
                                disabled={downloading}
                            />


                            <div className="salary-download-selection-actions">

                                <button
                                    type="button"
                                    onClick={
                                        handleSelectAll
                                    }
                                    disabled={downloading}
                                >
                                    Select All
                                </button>

                                <button
                                    type="button"
                                    onClick={
                                        handleClearSelection
                                    }
                                    disabled={downloading}
                                >
                                    Clear
                                </button>

                            </div>


                            <div className="salary-download-employee-list">

                                {filteredEmployees.length === 0 ? (

                                    <div className="salary-download-empty">
                                        No employees found.
                                    </div>

                                ) : (

                                    filteredEmployees.map(
                                        (employee) => {

                                            const id =
                                                getEmployeeId(
                                                    employee
                                                );

                                            const name =
                                                getEmployeeName(
                                                    employee
                                                );

                                            return (

                                                <label
                                                    key={id}
                                                    className="salary-download-employee"
                                                >

                                                    <input
                                                        type="checkbox"
                                                        checked={
                                                            selectedEmployees.includes(
                                                                id
                                                            )
                                                        }
                                                        onChange={() =>
                                                            handleEmployeeToggle(
                                                                id
                                                            )
                                                        }
                                                        disabled={
                                                            downloading
                                                        }
                                                    />

                                                    <span>

                                                        <strong>
                                                            {id}
                                                        </strong>

                                                        {" - "}

                                                        {name}

                                                    </span>

                                                </label>

                                            );
                                        }
                                    )

                                )}

                            </div>

                        </div>
                    )}


                    {/* RECORD RANGE */}

                    {scope === "RANGE" && (

                        <div className="salary-download-range">

                            <div>

                                <label>
                                    From Record
                                </label>

                                <input
                                    type="number"
                                    min="1"
                                    placeholder="1"
                                    value={fromRecord}
                                    onChange={(e) =>
                                        setFromRecord(
                                            e.target.value
                                        )
                                    }
                                    disabled={downloading}
                                />

                            </div>


                            <div>

                                <label>
                                    To Record
                                </label>

                                <input
                                    type="number"
                                    min="1"
                                    placeholder={
                                        salaryRecords.length ||
                                        "10"
                                    }
                                    value={toRecord}
                                    onChange={(e) =>
                                        setToRecord(
                                            e.target.value
                                        )
                                    }
                                    disabled={downloading}
                                />

                            </div>

                        </div>
                    )}


                    {/* FORMAT */}

                    <div className="salary-download-field">

                        <label>
                            Download Format
                        </label>

                        <select
                            value={format}
                            onChange={(e) =>
                                setFormat(
                                    e.target.value
                                )
                            }
                            disabled={downloading}
                        >

                            <option value="PDF">
                                PDF Payslip
                            </option>

                            <option value="EXCEL">
                                Excel Payroll Report
                            </option>

                            <option value="CSV">
                                CSV Payroll Report
                            </option>

                            <option value="ZIP">
                                ZIP - Individual Payslips
                            </option>

                        </select>

                    </div>

                </div>


                {/* =================================================
                    FOOTER
                ================================================= */}

                <div className="salary-download-footer">

                    <button
                        type="button"
                        className="salary-download-cancel"
                        onClick={onClose}
                        disabled={downloading}
                    >
                        Cancel
                    </button>

                    <button
                        type="button"
                        className="salary-download-submit"
                        onClick={handleDownload}
                        disabled={downloading}
                    >

                        {downloading
                            ? "Preparing..."
                            : "↓ Download Salary"}

                    </button>

                </div>

            </div>

        </div>
    );
}

export default SalaryDownload;