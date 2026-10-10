import React, { useEffect, useState } from "react";
import AdminLayout from "../pages/AdminLayout";
import SalaryDownload
    from "../components/salary/SalaryDownload";

import SalaryEmployeeSelector
    from "../components/salary/SalaryEmployeeSelector";

import SalaryEarnings
    from "../components/salary/SalaryEarnings";

import SalaryDeductions
    from "../components/salary/SalaryDeductions";

import SalarySummary
    from "../components/salary/SalarySummary";

import SalaryTable
    from "../components/salary/SalaryTable";

import {
    getAllSalaryRecords,
    createSalary,
    updateSalary,
    deleteSalary
} from "../services/salaryServices";

import "../styles/Salary.css";


function Salary() {

    // =====================================================
    // STATE
    // =====================================================

    const [showDownload, setShowDownload] = useState(false);

    const [salaryRecords, setSalaryRecords] = useState([]);

    const [employees, setEmployees] = useState([]);

    const [searchTerm, setSearchTerm] = useState("");

    const [showForm, setShowForm] = useState(false);

    const [editingSalary, setEditingSalary] = useState(null);

    const [loading, setLoading] = useState(true);

    const [saving, setSaving] = useState(false);

    const [error, setError] = useState("");

    const [success, setSuccess] = useState("");


    const [formData, setFormData] = useState({
        employeeId: "",
        payPeriod: "",
        components: []
    });


    // =====================================================
    // LOAD EMPLOYEES
    // =====================================================

    const loadEmployees = async () => {

        try {

            const response = await fetch(
                "http://localhost:8080/api/employees",
                {
                    method: "GET",
                    headers: {
                        Accept: "application/json",
                        Authorization:
                            `Bearer ${localStorage.getItem("token")}`
                    }
                }
            );

            if (!response.ok) {
                throw new Error(
                    `Employee request failed: ${response.status}`
                );
            }

            const data = await response.json();

            console.log(
                "EMPLOYEES RECEIVED:",
                data
            );

            setEmployees(
                Array.isArray(data)
                    ? data
                    : data.content || data.employees || []
            );

        } catch (err) {

            console.error(
                "EMPLOYEE LOAD ERROR:",
                err
            );

        }
    };


    // =====================================================
    // LOAD SALARY
    // =====================================================

    const loadSalaryRecords = async () => {

        try {

            setLoading(true);
            setError("");

            const data =
                await getAllSalaryRecords();

            console.log(
                "SALARY RECORDS RECEIVED:",
                data
            );

            setSalaryRecords(
                Array.isArray(data)
                    ? data
                    : data.content || data.records || []
            );

        } catch (err) {

            console.error(
                "SALARY LOAD ERROR:",
                err
            );

            setError(
                err.message ||
                "Unable to load salary records."
            );

        } finally {

            setLoading(false);

        }
    };


    // =====================================================
    // INITIAL LOAD
    // =====================================================

    useEffect(() => {

        loadEmployees();
        loadSalaryRecords();

    }, []);


    // =====================================================
    // RESET MESSAGES
    // =====================================================

    const clearMessages = () => {
        setError("");
        setSuccess("");
    };


    // =====================================================
    // OPEN ADD FORM
    // =====================================================

    const handleAddSalary = () => {

        clearMessages();

        setEditingSalary(null);

        const now = Date.now();

        setFormData({
            employeeId: "",

            payPeriod:
                new Date()
                    .toISOString()
                    .slice(0, 7),

            components: [

                {
                    tempId: now,
                    componentName: "Basic Salary",
                    componentType: "EARNING",
                    amount: 0,
                    systemDefined: true
                },

                {
                    tempId: now + 1,
                    componentName: "HRA",
                    componentType: "EARNING",
                    amount: 0,
                    systemDefined: true
                },

                {
                    tempId: now + 2,
                    componentName: "Daily Allowance",
                    componentType: "EARNING",
                    amount: 0,
                    systemDefined: true
                },

                {
                    tempId: now + 3,
                    componentName: "Travel Allowance",
                    componentType: "EARNING",
                    amount: 0,
                    systemDefined: true
                },

                {
                    tempId: now + 4,
                    componentName: "Incentive",
                    componentType: "EARNING",
                    amount: 0,
                    systemDefined: true
                },

                {
                    tempId: now + 5,
                    componentName: "Bonus",
                    componentType: "EARNING",
                    amount: 0,
                    systemDefined: true
                },

                {
                    tempId: now + 6,
                    componentName: "Miscellaneous",
                    componentType: "EARNING",
                    amount: 0,
                    systemDefined: true
                },

                {
                    tempId: now + 7,
                    componentName: "PF",
                    componentType: "DEDUCTION",
                    amount: 0,
                    systemDefined: true
                },

                {
                    tempId: now + 8,
                    componentName: "Tax Liability",
                    componentType: "DEDUCTION",
                    amount: 0,
                    systemDefined: true
                }

            ]
        });

        setShowForm(true);
    };


    // =====================================================
    // OPEN EDIT FORM
    // =====================================================

    const handleEditSalary = (record) => {

        clearMessages();

        setEditingSalary(record);

        setFormData({

            employeeId:
                record.employeeId || "",

            payPeriod:
                record.payPeriod || "",

            components:
                (record.components || []).map(
                    (component, index) => ({
                        ...component,

                        tempId:
                            component.id ||
                            Date.now() + index
                    })
                )
        });

        setShowForm(true);
    };


    // =====================================================
    // CLOSE FORM
    // =====================================================

    const handleCloseForm = () => {

        if (saving) {
            return;
        }

        setShowForm(false);
        setEditingSalary(null);
    };


    // =====================================================
    // COMPONENT CHANGE
    // =====================================================

    const handleComponentChange = (
        component,
        field,
        value
    ) => {

        setFormData((previous) => ({

            ...previous,

            components:
                previous.components.map(
                    (item) => {

                        if (
                            item.tempId ===
                            component.tempId
                        ) {

                            return {
                                ...item,
                                [field]: value
                            };
                        }

                        return item;
                    }
                )
        }));
    };


    // =====================================================
    // ADD EARNING
    // =====================================================

    const handleAddEarning = () => {

        setFormData((previous) => ({

            ...previous,

            components: [

                ...previous.components,

                {
                    tempId:
                        Date.now() +
                        Math.random(),

                    componentName:
                        "New Earning",

                    componentType:
                        "EARNING",

                    amount: 0,

                    systemDefined: false
                }

            ]

        }));
    };


    // =====================================================
    // ADD DEDUCTION
    // =====================================================

    const handleAddDeduction = () => {

        setFormData((previous) => ({

            ...previous,

            components: [

                ...previous.components,

                {
                    tempId:
                        Date.now() +
                        Math.random(),

                    componentName:
                        "New Deduction",

                    componentType:
                        "DEDUCTION",

                    amount: 0,

                    systemDefined: false
                }

            ]

        }));
    };


    // =====================================================
    // DELETE COMPONENT
    // =====================================================

    const handleDeleteComponent = (component) => {

        const confirmed =
            window.confirm(
                `Delete "${component.componentName}"?`
            );

        if (!confirmed) {
            return;
        }

        setFormData((previous) => ({

            ...previous,

            components:
                previous.components.filter(
                    (item) =>
                        item.tempId !==
                        component.tempId
                )
        }));
    };


    // =====================================================
    // SAVE SALARY
    // =====================================================

    const handleSaveSalary = async (e) => {

        e.preventDefault();

        clearMessages();

        if (!formData.employeeId) {

            setError(
                "Please select an employee."
            );

            return;
        }

        if (!formData.payPeriod) {

            setError(
                "Please select a pay period."
            );

            return;
        }


        const invalidComponent =
            formData.components.find(
                (component) =>
                    !component.componentName ||
                    String(
                        component.componentName
                    ).trim() === ""
            );

        if (invalidComponent) {

            setError(
                "Every salary component must have a name."
            );

            return;
        }


        setSaving(true);


        const payload = {

            employeeId:
            formData.employeeId,

            payPeriod:
            formData.payPeriod,

            components:
                formData.components.map(
                    (component) => ({

                        id:
                            component.id || null,

                        componentName:
                        component.componentName,

                        componentType:
                        component.componentType,

                        amount:
                            Number(
                                component.amount || 0
                            ),

                        systemDefined:
                            Boolean(
                                component.systemDefined
                            )
                    })
                )
        };


        try {

            if (editingSalary) {

                await updateSalary(
                    editingSalary.salaryId,
                    payload
                );

                setSuccess(
                    "Salary record updated successfully."
                );

            } else {

                await createSalary(
                    payload
                );

                setSuccess(
                    "Salary record created successfully."
                );
            }


            setShowForm(false);

            setEditingSalary(null);

            await loadSalaryRecords();

        } catch (err) {

            console.error(
                "SAVE SALARY ERROR:",
                err
            );

            setError(
                err.message ||
                "Unable to save salary."
            );

        } finally {

            setSaving(false);

        }
    };


    // =====================================================
    // DELETE SALARY
    // =====================================================

    const handleDeleteSalary = async (record) => {

        const employeeName =
            record.employeeId ||
            "this employee";

        const confirmed =
            window.confirm(
                `Delete salary record for ${employeeName}?`
            );

        if (!confirmed) {
            return;
        }


        try {

            clearMessages();

            await deleteSalary(
                record.salaryId
            );

            setSuccess(
                "Salary record deleted successfully."
            );

            await loadSalaryRecords();

        } catch (err) {

            console.error(
                "DELETE SALARY ERROR:",
                err
            );

            setError(
                err.message ||
                "Unable to delete salary."
            );
        }
    };


    // =====================================================
    // CALCULATE FORM TOTALS
    // =====================================================

    const earnings =
        formData.components
            .filter(
                (item) =>
                    item.componentType ===
                    "EARNING"
            )
            .reduce(
                (sum, item) =>
                    sum +
                    Number(item.amount || 0),
                0
            );


    const deductions =
        formData.components
            .filter(
                (item) =>
                    item.componentType ===
                    "DEDUCTION"
            )
            .reduce(
                (sum, item) =>
                    sum +
                    Number(item.amount || 0),
                0
            );


    const netSalary =
        earnings - deductions;


    // =====================================================
    // RETURN
    // =====================================================

    return (
        <AdminLayout>

        <div className="salary-page">


            {/* =================================================
                PAGE HEADER
            ================================================= */}

            <div className="salary-page-header">

                <div>

                    <h1>
                        Salary
                    </h1>

                    <p>
                        Manage employee salary structures,
                        earnings, deductions and payroll records.
                    </p>

                </div>


                <div className="salary-header-actions">

                    <button
                        type="button"
                        className="download-salary-button"
                        onClick={() => {

                            clearMessages();

                            setShowDownload(true);

                        }}
                    >
                        <span>↓</span>
                        Download Salary
                    </button>


                    <button
                        type="button"
                        className="add-salary-button"
                        onClick={handleAddSalary}
                    >
                        <span>+</span>
                        Add Salary
                    </button>

                </div>

            </div>


            {/* =================================================
                MESSAGES
            ================================================= */}

            {error && (

                <div className="salary-alert salary-alert-error">

                    {error}

                </div>

            )}


            {success && (

                <div className="salary-alert salary-alert-success">

                    {success}

                </div>

            )}


            {/* =================================================
                SUMMARY
            ================================================= */}

            <SalarySummary
                records={salaryRecords}
            />


            {/* =================================================
                SEARCH
            ================================================= */}

            <div className="salary-toolbar">

                <div className="salary-search">

                    <span>
                        🔍
                    </span>

                    <input
                        type="text"
                        placeholder="Search employees..."
                        value={searchTerm}
                        onChange={(e) =>
                            setSearchTerm(
                                e.target.value
                            )
                        }
                    />

                </div>

            </div>


            {/* =================================================
                SALARY RECORDS
            ================================================= */}

            <div className="salary-records-card">

                <div className="salary-records-header">

                    <div>

                        <h2>
                            Salary Records
                        </h2>

                        <p>
                            Existing employee salary records.
                        </p>

                    </div>

                </div>


                {loading ? (

                    <div className="salary-loading">

                        Loading salary records...

                    </div>

                ) : (

                    <SalaryTable
                        records={salaryRecords}
                        employees={employees}
                        searchTerm={searchTerm}
                        onEdit={handleEditSalary}
                        onDelete={handleDeleteSalary}
                    />

                )}

            </div>


            {/* =================================================
                ADD / EDIT SALARY MODAL
            ================================================= */}

            {showForm && (

                <div className="salary-modal-overlay">

                    <div className="salary-modal">


                        <div className="salary-modal-header">

                            <div>

                                <h2>

                                    {editingSalary
                                        ? "Edit Salary"
                                        : "Add Salary"}

                                </h2>

                                <p>

                                    Enter employee salary
                                    structure and components.

                                </p>

                            </div>


                            <button
                                type="button"
                                className="modal-close-button"
                                onClick={handleCloseForm}
                                disabled={saving}
                            >
                                ×
                            </button>

                        </div>


                        <form
                            onSubmit={
                                handleSaveSalary
                            }
                        >


                            {/* =================================================
                                EMPLOYEE INFORMATION
                            ================================================= */}

                            <div className="salary-form-section">

                                <h3>
                                    Employee Information
                                </h3>


                                <SalaryEmployeeSelector

                                    employees={
                                        employees
                                    }

                                    employeeId={
                                        formData.employeeId
                                    }

                                    onChange={(value) =>

                                        setFormData(
                                            (previous) => ({
                                                ...previous,
                                                employeeId:
                                                value
                                            })
                                        )

                                    }

                                    disabled={
                                        saving ||
                                        Boolean(
                                            editingSalary
                                        )
                                    }

                                />


                                <div className="salary-form-grid">


                                    <div className="salary-form-field">

                                        <label>
                                            Employee ID
                                        </label>

                                        <input
                                            type="text"
                                            value={
                                                formData.employeeId
                                            }
                                            readOnly
                                        />

                                    </div>


                                    <div className="salary-form-field">

                                        <label>
                                            Pay Period
                                        </label>

                                        <input
                                            type="month"
                                            value={
                                                formData.payPeriod
                                            }
                                            onChange={(e) =>

                                                setFormData(
                                                    (previous) => ({
                                                        ...previous,
                                                        payPeriod:
                                                        e.target.value
                                                    })
                                                )

                                            }
                                            disabled={
                                                saving
                                            }
                                        />

                                    </div>

                                </div>

                            </div>


                            {/* =================================================
                                EARNINGS
                            ================================================= */}

                            <SalaryEarnings

                                components={
                                    formData.components
                                }

                                onChange={
                                    handleComponentChange
                                }

                                onAdd={
                                    handleAddEarning
                                }

                                onDelete={
                                    handleDeleteComponent
                                }

                                disabled={
                                    saving
                                }

                            />


                            {/* =================================================
                                DEDUCTIONS
                            ================================================= */}

                            <SalaryDeductions

                                components={
                                    formData.components
                                }

                                onChange={
                                    handleComponentChange
                                }

                                onAdd={
                                    handleAddDeduction
                                }

                                onDelete={
                                    handleDeleteComponent
                                }

                                disabled={
                                    saving
                                }

                            />


                            {/* =================================================
                                FORM SUMMARY
                            ================================================= */}

                            <div className="salary-form-summary">


                                <div>

                                    <span>
                                        Total Earnings
                                    </span>

                                    <strong>

                                        ₹
                                        {earnings.toLocaleString(
                                            "en-IN",
                                            {
                                                maximumFractionDigits: 2
                                            }
                                        )}

                                    </strong>

                                </div>


                                <div>

                                    <span>
                                        Total Deductions
                                    </span>

                                    <strong>

                                        ₹
                                        {deductions.toLocaleString(
                                            "en-IN",
                                            {
                                                maximumFractionDigits: 2
                                            }
                                        )}

                                    </strong>

                                </div>


                                <div className="net-summary">

                                    <span>
                                        Net Salary
                                    </span>

                                    <strong>

                                        ₹
                                        {netSalary.toLocaleString(
                                            "en-IN",
                                            {
                                                maximumFractionDigits: 2
                                            }
                                        )}

                                    </strong>

                                </div>

                            </div>


                            {/* =================================================
                                FORM BUTTONS
                            ================================================= */}

                            <div className="salary-modal-actions">


                                <button
                                    type="button"
                                    className="cancel-salary-button"
                                    onClick={
                                        handleCloseForm
                                    }
                                    disabled={
                                        saving
                                    }
                                >
                                    Cancel
                                </button>


                                <button
                                    type="submit"
                                    className="save-salary-button"
                                    disabled={
                                        saving
                                    }
                                >

                                    {saving

                                        ? "Saving..."

                                        : editingSalary
                                            ? "Update Salary"
                                            : "Save Salary"}

                                </button>

                            </div>


                        </form>

                    </div>

                </div>

            )}


            {/* =================================================
                DOWNLOAD SALARY MODAL
            ================================================= */}

            {showDownload && (

                <SalaryDownload

                    employees={
                        employees
                    }

                    salaryRecords={
                        salaryRecords
                    }

                    onClose={() =>
                        setShowDownload(false)
                    }

                    onSuccess={(message) => {

                        setShowDownload(false);

                        setError("");

                        setSuccess(
                            message ||
                            "Salary downloaded successfully."
                        );

                    }}

                    onError={(message) => {

                        setError(
                            message ||
                            "Unable to download salary."
                        );

                    }}

                />

            )}

        </div>
        </AdminLayout>

    );
}


export default Salary;