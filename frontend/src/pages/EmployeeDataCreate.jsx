import React, { useEffect, useMemo, useState } from "react";
import "./EmployeeDataCreate.css";
import AdminLayout from "../pages/AdminLayout";

const API_BASE = "http://localhost:8080/api";

export default function EmployeeDataCreate() {
    const token = localStorage.getItem("token");
    const role = localStorage.getItem("role");

    const isAdmin = role === "ADMIN";
    const isStaff = role === "STAFF";

    const [employees, setEmployees] = useState([]);
    const [departments, setDepartments] = useState([]);
    const [employeeFields, setEmployeeFields] = useState([]);

    const [selectedEmployee, setSelectedEmployee] = useState(null);

    const [loading, setLoading] = useState(false);
    const [profileLoading, setProfileLoading] = useState(false);

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    const [searchText, setSearchText] = useState("");
    const [departmentFilter, setDepartmentFilter] = useState("ALL");
    const [statusFilter, setStatusFilter] = useState("ALL");

    const [showProfile, setShowProfile] = useState(false);
    const [showAddEmployee, setShowAddEmployee] = useState(false);

    const [showFieldModal, setShowFieldModal] = useState(false);
    const [editingField, setEditingField] = useState(null);

    const [showPrefixModal, setShowPrefixModal] = useState(false);
    const [prefix, setPrefix] = useState("");

    const [showRequests, setShowRequests] = useState(false);
    const [requests, setRequests] = useState([]);

    const [fieldName, setFieldName] = useState("");
    const [fieldValue, setFieldValue] = useState("");

    const [requestReason, setRequestReason] = useState("");

    // ============================================================
    // AUTH HEADER
    // ============================================================

    const headers = {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`
    };

    // ============================================================
    // LOAD DATA
    // ============================================================

    useEffect(() => {
        loadEmployees();
        loadDepartments();

        if (isAdmin) {
            loadRequests();
        }
    }, []);

    // ============================================================
    // LOAD EMPLOYEES
    // ============================================================

    const loadEmployees = async () => {
        try {
            setLoading(true);
            setError("");

            const response = await fetch(
                `${API_BASE}/employees`,
                {
                    headers
                }
            );

            if (!response.ok) {
                throw new Error("Unable to load employees");
            }

            const data = await response.json();

            const employeeList = Array.isArray(data)
                ? data
                : [];

            /*
             * Load fields for every employee so that
             * the overview table can display dynamic columns.
             */
            const employeesWithFields =
                await Promise.all(
                    employeeList.map(async (employee) => {
                        try {
                            const fieldResponse =
                                await fetch(
                                    `${API_BASE}/employees/${encodeURIComponent(
                                        employee.employeeId
                                    )}/fields`,
                                    {
                                        headers
                                    }
                                );

                            if (!fieldResponse.ok) {
                                return {
                                    ...employee,
                                    fields: []
                                };
                            }

                            const fields =
                                await fieldResponse.json();

                            return {
                                ...employee,
                                fields: Array.isArray(fields)
                                    ? fields
                                    : []
                            };
                        } catch {
                            return {
                                ...employee,
                                fields: []
                            };
                        }
                    })
                );

            setEmployees(employeesWithFields);
        } catch (err) {
            setError(
                err.message ||
                "Unable to load employees"
            );
        } finally {
            setLoading(false);
        }
    };

    // ============================================================
    // LOAD DEPARTMENTS
    // ============================================================

    const loadDepartments = async () => {
        try {
            const response = await fetch(
                `${API_BASE}/departments`,
                {
                    headers
                }
            );

            if (!response.ok) {
                return;
            }

            const data = await response.json();

            if (Array.isArray(data)) {
                setDepartments(data);
            }
        } catch {
            /*
             * Department loading failure should not
             * stop employee page from loading.
             */
        }
    };

    // ============================================================
    // LOAD SINGLE EMPLOYEE FIELDS
    // ============================================================

    const loadEmployeeFields = async (employeeId) => {
        try {
            setProfileLoading(true);
            setError("");

            const response = await fetch(
                `${API_BASE}/employees/${encodeURIComponent(
                    employeeId
                )}/fields`,
                {
                    headers
                }
            );

            if (!response.ok) {
                throw new Error(
                    "Unable to load employee details"
                );
            }

            const data = await response.json();

            setEmployeeFields(
                Array.isArray(data)
                    ? data
                    : []
            );
        } catch (err) {
            setError(
                err.message ||
                "Unable to load employee details"
            );

            setEmployeeFields([]);
        } finally {
            setProfileLoading(false);
        }
    };

    // ============================================================
    // OPEN EMPLOYEE PROFILE
    // ============================================================

    const openProfile = async (employee) => {
        setSelectedEmployee(employee);
        setShowProfile(true);

        await loadEmployeeFields(
            employee.employeeId
        );
    };

    // ============================================================
    // CLOSE PROFILE
    // ============================================================

    const closeProfile = () => {
        setShowProfile(false);
        setSelectedEmployee(null);
        setEmployeeFields([]);
        setError("");
    };

    // ============================================================
    // DYNAMIC FIELD NAMES
    // ============================================================

    const allFieldNames = useMemo(() => {
        const names = new Set();

        employees.forEach((employee) => {
            if (Array.isArray(employee.fields)) {
                employee.fields.forEach((field) => {
                    if (
                        field.fieldName &&
                        field.fieldName.trim()
                    ) {
                        names.add(
                            field.fieldName.trim()
                        );
                    }
                });
            }
        });

        return Array.from(names);
    }, [employees]);

    // ============================================================
    // GET FIELD VALUE FOR TABLE
    // ============================================================

    const getFieldValue = (
        employee,
        name
    ) => {
        const field =
            employee.fields?.find(
                (item) =>
                    item.fieldName?.toLowerCase() ===
                    name.toLowerCase()
            );

        return field?.fieldValue || "—";
    };

    // ============================================================
    // DEPARTMENT VALUE
    // ============================================================

    const getDepartmentName = (employee) => {
        const departmentField =
            employee.fields?.find(
                (field) =>
                    field.fieldName?.toLowerCase() ===
                    "department"
            );

        return (
            departmentField?.fieldValue ||
            "—"
        );
    };

    // ============================================================
    // FILTERED EMPLOYEES
    // ============================================================

    const filteredEmployees = useMemo(() => {
        return employees.filter((employee) => {
            const search =
                searchText
                    .trim()
                    .toLowerCase();

            const matchesSearch =
                !search ||
                employee.employeeId
                    ?.toLowerCase()
                    .includes(search) ||
                employee.fields?.some(
                    (field) =>
                        field.fieldName
                            ?.toLowerCase()
                            .includes(search) ||
                        field.fieldValue
                            ?.toLowerCase()
                            .includes(search)
                );

            const department =
                getDepartmentName(employee);

            const matchesDepartment =
                departmentFilter === "ALL" ||
                department === departmentFilter;

            const matchesStatus =
                statusFilter === "ALL" ||
                (
                    statusFilter === "ACTIVE" &&
                    employee.active
                ) ||
                (
                    statusFilter === "INACTIVE" &&
                    !employee.active
                );

            return (
                matchesSearch &&
                matchesDepartment &&
                matchesStatus
            );
        });
    }, [
        employees,
        searchText,
        departmentFilter,
        statusFilter
    ]);

    // ============================================================
    // COUNTS
    // ============================================================

    const totalEmployees =
        employees.length;

    const activeEmployees =
        employees.filter(
            (employee) => employee.active
        ).length;

    const inactiveEmployees =
        employees.filter(
            (employee) => !employee.active
        ).length;

    // ============================================================
    // CREATE EMPLOYEE
    // ============================================================

    const createEmployee = async () => {
        try {
            setError("");
            setSuccess("");

            const response = await fetch(
                `${API_BASE}/employees`,
                {
                    method: "POST",
                    headers
                }
            );

            const data =
                await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    "Unable to create employee"
                );
            }

            setSuccess(
                `Employee ${data.employeeId} created successfully`
            );

            setShowAddEmployee(false);

            await loadEmployees();
        } catch (err) {
            setError(
                err.message ||
                "Unable to create employee"
            );
        }
    };

    // ============================================================
    // OPEN ADD FIELD
    // ============================================================

    const openAddField = () => {
        setEditingField(null);
        setFieldName("");
        setFieldValue("");
        setRequestReason("");

        setShowFieldModal(true);
    };

    // ============================================================
    // OPEN EDIT FIELD
    // ============================================================

    const openEditField = (field) => {
        setEditingField(field);

        setFieldName(
            field.fieldName || ""
        );

        setFieldValue(
            field.fieldValue || ""
        );

        setRequestReason("");

        setShowFieldModal(true);
    };

    // ============================================================
    // SAVE FIELD
    // ============================================================

    const saveField = async () => {
        if (!fieldName.trim()) {
            setError(
                "Field name is required"
            );
            return;
        }

        if (!selectedEmployee) {
            return;
        }

        try {
            setError("");
            setSuccess("");

            // ====================================================
            // ADMIN DIRECT CHANGE
            // ====================================================

            if (isAdmin) {
                let response;

                if (editingField) {
                    response = await fetch(
                        `${API_BASE}/employees/${encodeURIComponent(
                            selectedEmployee.employeeId
                        )}/fields/${editingField.fieldId}`,
                        {
                            method: "PUT",
                            headers,
                            body: JSON.stringify({
                                fieldName:
                                    fieldName.trim(),
                                fieldValue:
                                fieldValue
                            })
                        }
                    );
                } else {
                    response = await fetch(
                        `${API_BASE}/employees/${encodeURIComponent(
                            selectedEmployee.employeeId
                        )}/fields`,
                        {
                            method: "POST",
                            headers,
                            body: JSON.stringify({
                                fieldName:
                                    fieldName.trim(),
                                fieldValue:
                                fieldValue
                            })
                        }
                    );
                }

                const data =
                    await response.json();

                if (!response.ok) {
                    throw new Error(
                        data.message ||
                        "Unable to save field"
                    );
                }

                setSuccess(
                    editingField
                        ? "Employee field updated successfully"
                        : "Employee field added successfully"
                );

                setShowFieldModal(false);

                await loadEmployeeFields(
                    selectedEmployee.employeeId
                );

                await loadEmployees();

                return;
            }

            // ====================================================
            // STAFF REQUEST
            // ====================================================

            const response = await fetch(
                `${API_BASE}/employee-change-requests/${encodeURIComponent(
                    selectedEmployee.employeeId
                )}`,
                {
                    method: "POST",
                    headers,
                    body: JSON.stringify({
                        requestType:
                            editingField
                                ? "EDIT_FIELD"
                                : "ADD_FIELD",

                        fieldId:
                            editingField
                                ? String(
                                    editingField.fieldId
                                )
                                : undefined,

                        fieldName:
                            fieldName.trim(),

                        fieldValue:
                        fieldValue,

                        reason:
                            requestReason.trim()
                    })
                }
            );

            const data =
                await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    "Unable to create request"
                );
            }

            setSuccess(
                "Change request submitted for ADMIN approval"
            );

            setShowFieldModal(false);
        } catch (err) {
            setError(
                err.message ||
                "Unable to save field"
            );
        }
    };

    // ============================================================
    // DELETE FIELD
    // ============================================================

    const deleteField = async (field) => {
        if (!selectedEmployee) {
            return;
        }

        const confirmed =
            window.confirm(
                `Delete "${field.fieldName}"?`
            );

        if (!confirmed) {
            return;
        }

        try {
            setError("");
            setSuccess("");

            // ====================================================
            // ADMIN DIRECT DELETE
            // ====================================================

            if (isAdmin) {
                const response =
                    await fetch(
                        `${API_BASE}/employees/${encodeURIComponent(
                            selectedEmployee.employeeId
                        )}/fields/${field.fieldId}`,
                        {
                            method: "DELETE",
                            headers
                        }
                    );

                const text =
                    await response.text();

                let data = {};

                try {
                    data = text
                        ? JSON.parse(text)
                        : {};
                } catch {
                    data = {};
                }

                if (!response.ok) {
                    throw new Error(
                        data.message ||
                        text ||
                        "Unable to delete field"
                    );
                }

                setSuccess(
                    "Employee field deleted successfully"
                );

                await loadEmployeeFields(
                    selectedEmployee.employeeId
                );

                await loadEmployees();

                return;
            }

            // ====================================================
            // STAFF DELETE REQUEST
            // ====================================================

            const reason =
                window.prompt(
                    "Enter reason for deleting this field:"
                );

            if (reason === null) {
                return;
            }

            const response =
                await fetch(
                    `${API_BASE}/employee-change-requests/${encodeURIComponent(
                        selectedEmployee.employeeId
                    )}`,
                    {
                        method: "POST",
                        headers,
                        body: JSON.stringify({
                            requestType:
                                "DELETE_FIELD",

                            fieldId:
                                String(
                                    field.fieldId
                                ),

                            reason:
                                reason.trim()
                        })
                    }
                );

            const data =
                await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    "Unable to submit delete request"
                );
            }

            setSuccess(
                "Delete request submitted for ADMIN approval"
            );
        } catch (err) {
            setError(
                err.message ||
                "Unable to delete field"
            );
        }
    };

    // ============================================================
    // OPEN PREFIX MODAL
    // ============================================================

    const openPrefixModal = () => {
        if (!selectedEmployee) {
            return;
        }

        const currentId =
            selectedEmployee.employeeId;

        const currentPrefix =
            currentId.replace(
                /\d+$/,
                ""
            );

        setPrefix(currentPrefix);
        setRequestReason("");

        setShowPrefixModal(true);
    };

    // ============================================================
    // CHANGE PREFIX
    // ============================================================

    const changePrefix = async () => {
        if (!selectedEmployee) {
            return;
        }

        const newPrefix =
            prefix
                .trim()
                .toUpperCase();

        if (!newPrefix) {
            setError(
                "Prefix is required"
            );
            return;
        }

        if (!/^[A-Z]+$/.test(newPrefix)) {
            setError(
                "Prefix can contain letters only"
            );
            return;
        }

        try {
            setError("");
            setSuccess("");

            // ====================================================
            // ADMIN DIRECT CHANGE
            // ====================================================

            if (isAdmin) {
                const response =
                    await fetch(
                        `${API_BASE}/employees/${encodeURIComponent(
                            selectedEmployee.employeeId
                        )}/prefix`,
                        {
                            method: "PUT",
                            headers,
                            body: JSON.stringify({
                                prefix:
                                newPrefix
                            })
                        }
                    );

                const data =
                    await response.json();

                if (!response.ok) {
                    throw new Error(
                        data.message ||
                        "Unable to change employee ID"
                    );
                }

                setSuccess(
                    `Employee ID changed to ${data.employeeId}`
                );

                setShowPrefixModal(false);

                await loadEmployees();

                /*
                 * Keep the currently opened profile
                 * synchronized with the new ID.
                 */
                setSelectedEmployee((previous) => {
                    if (!previous) {
                        return previous;
                    }

                    return {
                        ...previous,
                        employeeId:
                        data.employeeId
                    };
                });

                return;
            }

            // ====================================================
            // STAFF REQUEST
            // ====================================================

            const response =
                await fetch(
                    `${API_BASE}/employee-change-requests/${encodeURIComponent(
                        selectedEmployee.employeeId
                    )}`,
                    {
                        method: "POST",
                        headers,
                        body: JSON.stringify({
                            requestType:
                                "EDIT_EMPLOYEE_ID_PREFIX",

                            fieldName:
                            newPrefix,

                            reason:
                                requestReason.trim()
                        })
                    }
                );

            const data =
                await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    "Unable to submit prefix request"
                );
            }

            setSuccess(
                "Employee ID prefix change request submitted for ADMIN approval"
            );

            setShowPrefixModal(false);
        } catch (err) {
            setError(
                err.message ||
                "Unable to change employee ID"
            );
        }
    };

    // ============================================================
    // LOAD ADMIN REQUESTS
    // ============================================================

    const loadRequests = async () => {
        if (!isAdmin) {
            return;
        }

        try {
            const response =
                await fetch(
                    `${API_BASE}/employee-change-requests`,
                    {
                        headers
                    }
                );

            if (!response.ok) {
                return;
            }

            const data =
                await response.json();

            setRequests(
                Array.isArray(data)
                    ? data
                    : []
            );
        } catch {
            setRequests([]);
        }
    };

    // ============================================================
    // APPROVE REQUEST
    // ============================================================

    const approveRequest = async (
        requestId
    ) => {
        try {
            const response =
                await fetch(
                    `${API_BASE}/employee-change-requests/${requestId}/approve`,
                    {
                        method: "PUT",
                        headers
                    }
                );

            const data =
                await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    "Unable to approve request"
                );
            }

            setSuccess(
                "Employee change request approved"
            );

            await loadRequests();
            await loadEmployees();
        } catch (err) {
            setError(
                err.message ||
                "Unable to approve request"
            );
        }
    };

    // ============================================================
    // REJECT REQUEST
    // ============================================================

    const rejectRequest = async (
        requestId
    ) => {
        try {
            const response =
                await fetch(
                    `${API_BASE}/employee-change-requests/${requestId}/reject`,
                    {
                        method: "PUT",
                        headers
                    }
                );

            const data =
                await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    "Unable to reject request"
                );
            }

            setSuccess(
                "Employee change request rejected"
            );

            await loadRequests();
        } catch (err) {
            setError(
                err.message ||
                "Unable to reject request"
            );
        }
    };

    // ============================================================
    // REQUEST LABEL
    // ============================================================

    const requestTypeLabel = (
        type
    ) => {
        switch (type) {
            case "ADD_FIELD":
                return "Add Field";

            case "EDIT_FIELD":
                return "Edit Field";

            case "DELETE_FIELD":
                return "Delete Field";

            case "EDIT_EMPLOYEE_ID_PREFIX":
                return "Change Employee ID";

            default:
                return type || "Change";
        }
    };

    // ============================================================
    // FORMAT VALUE
    // ============================================================

    const displayValue = (
        value
    ) => {
        if (
            value === null ||
            value === undefined ||
            value === ""
        ) {
            return "—";
        }

        return value;
    };

    // ============================================================
    // RENDER
    // ============================================================

    return (
        <AdminLayout>
        <div className="employee-page">

            {/* ====================================================
                HEADER
            ==================================================== */}

            <div className="employee-page-header">

                <div>

                    <div className="breadcrumb">
                        Administration
                        <span>/</span>
                        Employee Management
                    </div>

                    <h1>
                        Employee Management
                    </h1>

                    <p>
                        Manage employee records,
                        departments and employment information.
                    </p>

                </div>

                {isAdmin && (
                    <div className="header-actions">

                        <button
                            className="secondary-button"
                            onClick={() => {
                                setShowRequests(true);
                                loadRequests();
                            }}
                        >
                            Requests

                            {requests.length > 0 && (
                                <span className="request-count">
                                    {requests.length}
                                </span>
                            )}
                        </button>

                        <button
                            className="primary-button"
                            onClick={() =>
                                setShowAddEmployee(true)
                            }
                        >
                            <span>+</span>
                            Add Employee
                        </button>

                    </div>
                )}

            </div>

            {/* ====================================================
                ALERTS
            ==================================================== */}

            {error && (
                <div className="alert alert-error">

                    <span>!</span>

                    <div>
                        {error}
                    </div>

                    <button
                        onClick={() =>
                            setError("")
                        }
                    >
                        ×
                    </button>

                </div>
            )}

            {success && (
                <div className="alert alert-success">

                    <span>✓</span>

                    <div>
                        {success}
                    </div>

                    <button
                        onClick={() =>
                            setSuccess("")
                        }
                    >
                        ×
                    </button>

                </div>
            )}

            {/* ====================================================
                SUMMARY CARDS
            ==================================================== */}

            <div className="summary-grid">

                <div className="summary-card">

                    <div className="summary-icon">
                        👥
                    </div>

                    <div>
                        <span className="summary-label">
                            Total Employees
                        </span>

                        <strong>
                            {totalEmployees}
                        </strong>
                    </div>

                </div>

                <div className="summary-card">

                    <div className="summary-icon">
                        ✓
                    </div>

                    <div>
                        <span className="summary-label">
                            Active Employees
                        </span>

                        <strong>
                            {activeEmployees}
                        </strong>
                    </div>

                </div>

                <div className="summary-card">

                    <div className="summary-icon">
                        ○
                    </div>

                    <div>
                        <span className="summary-label">
                            Inactive Employees
                        </span>

                        <strong>
                            {inactiveEmployees}
                        </strong>
                    </div>

                </div>

            </div>

            {/* ====================================================
                TABLE CARD
            ==================================================== */}

            <div className="employee-table-card">

                {/* TOOLBAR */}

                <div className="table-toolbar">

                    <div className="search-wrapper">

                        <span className="search-icon">
                            ⌕
                        </span>

                        <input
                            type="text"
                            placeholder="Search by employee ID or name"
                            value={searchText}
                            onChange={(e) =>
                                setSearchText(
                                    e.target.value
                                )
                            }
                        />

                        {searchText && (
                            <button
                                className="clear-search"
                                onClick={() =>
                                    setSearchText("")
                                }
                            >
                                ×
                            </button>
                        )}

                    </div>

                    <div className="filter-group">

                        <select
                            value={
                                departmentFilter
                            }
                            onChange={(e) =>
                                setDepartmentFilter(
                                    e.target.value
                                )
                            }
                        >

                            <option value="ALL">
                                All Departments
                            </option>

                            {departments.map(
                                (department) => {

                                    const name =
                                        department.departmentName ||
                                        department.name;

                                    if (!name) {
                                        return null;
                                    }

                                    return (
                                        <option
                                            key={
                                                department.departmentId ||
                                                name
                                            }
                                            value={name}
                                        >
                                            {name}
                                        </option>
                                    );
                                }
                            )}

                        </select>

                        <select
                            value={
                                statusFilter
                            }
                            onChange={(e) =>
                                setStatusFilter(
                                    e.target.value
                                )
                            }
                        >

                            <option value="ALL">
                                All Status
                            </option>

                            <option value="ACTIVE">
                                Active
                            </option>

                            <option value="INACTIVE">
                                Inactive
                            </option>

                        </select>

                    </div>

                </div>

                {/* TABLE */}

                <div className="table-container">

                    {loading ? (

                        <div className="table-state">

                            <div className="spinner"></div>

                            Loading employee records...

                        </div>

                    ) : filteredEmployees.length === 0 ? (

                        <div className="table-state empty-state">

                            <div className="empty-icon">
                                👤
                            </div>

                            <h3>
                                No employees found
                            </h3>

                            <p>
                                {employees.length === 0
                                    ? "Create your first employee to get started."
                                    : "Try changing your search or filters."
                                }
                            </p>

                            {isAdmin &&
                                employees.length === 0 && (
                                    <button
                                        className="primary-button"
                                        onClick={() =>
                                            setShowAddEmployee(
                                                true
                                            )
                                        }
                                    >
                                        + Add Employee
                                    </button>
                                )}

                        </div>

                    ) : (

                        <table className="employee-table">

                            <thead>

                            <tr>

                                <th className="checkbox-column">
                                    <input
                                        type="checkbox"
                                        disabled
                                    />
                                </th>

                                <th>
                                    Employee
                                </th>

                                <th>
                                    Department
                                </th>

                                {allFieldNames
                                    .filter(
                                        (name) =>
                                            name.toLowerCase() !==
                                            "department" &&
                                            name.toLowerCase() !==
                                            "name"
                                    )
                                    .map((name) => (
                                        <th key={name}>
                                            {name}
                                        </th>
                                    ))}

                                <th>
                                    Status
                                </th>

                                <th className="actions-column">
                                    Action
                                </th>

                            </tr>

                            </thead>

                            <tbody>

                            {filteredEmployees.map(
                                (employee) => {

                                    const department =
                                        getDepartmentName(
                                            employee
                                        );

                                    const employeeName =
                                        employee.fields?.find(
                                            (field) =>
                                                field.fieldName?.toLowerCase() ===
                                                "name"
                                        )?.fieldValue ||
                                        "Employee";

                                    const avatarLetter =
                                        employeeName
                                            ?.charAt(0)
                                            ?.toUpperCase() ||
                                        employee.employeeId
                                            ?.charAt(0);

                                    return (

                                        <tr
                                            key={
                                                employee.employeeId
                                            }
                                        >

                                            <td>
                                                <input
                                                    type="checkbox"
                                                />
                                            </td>

                                            <td>

                                                <div className="employee-cell">

                                                    <div className="employee-avatar">
                                                        {
                                                            avatarLetter
                                                        }
                                                    </div>

                                                    <div>

                                                        <button
                                                            className="employee-id-link"
                                                            onClick={() =>
                                                                openProfile(
                                                                    employee
                                                                )
                                                            }
                                                        >
                                                            {
                                                                employee.employeeId
                                                            }
                                                        </button>

                                                        <div className="employee-name">
                                                            {
                                                                employeeName
                                                            }
                                                        </div>

                                                    </div>

                                                </div>

                                            </td>

                                            <td>

                                                <span className="department-badge">
                                                    {
                                                        department
                                                    }
                                                </span>

                                            </td>

                                            {allFieldNames
                                                .filter(
                                                    (name) =>
                                                        name.toLowerCase() !==
                                                        "department" &&
                                                        name.toLowerCase() !==
                                                        "name"
                                                )
                                                .map((name) => (

                                                    <td
                                                        key={
                                                            name
                                                        }
                                                    >

                                                        <span className="table-value">
                                                            {
                                                                getFieldValue(
                                                                    employee,
                                                                    name
                                                                )
                                                            }
                                                        </span>

                                                    </td>

                                                ))}

                                            <td>

                                                <span
                                                    className={
                                                        employee.active
                                                            ? "status-badge active"
                                                            : "status-badge inactive"
                                                    }
                                                >

                                                    <span className="status-dot"></span>

                                                    {employee.active
                                                        ? "Active"
                                                        : "Inactive"}

                                                </span>

                                            </td>

                                            <td>

                                                <button
                                                    className="view-button"
                                                    onClick={() =>
                                                        openProfile(
                                                            employee
                                                        )
                                                    }
                                                >
                                                    View
                                                </button>

                                            </td>

                                        </tr>

                                    );
                                }
                            )}

                            </tbody>

                        </table>

                    )}

                </div>

                {/* TABLE FOOTER */}

                {!loading &&
                    filteredEmployees.length > 0 && (

                        <div className="table-footer">

                            <span>
                                Showing{" "}
                                <strong>
                                    {
                                        filteredEmployees.length
                                    }
                                </strong>{" "}
                                of{" "}
                                <strong>
                                    {employees.length}
                                </strong>{" "}
                                employees
                            </span>

                            <div className="pagination">

                                <button disabled>
                                    ‹
                                </button>

                                <button className="active-page">
                                    1
                                </button>

                                <button disabled>
                                    ›
                                </button>

                            </div>

                        </div>

                    )}

            </div>

            {/* ====================================================
                EMPLOYEE PROFILE
            ==================================================== */}

            {showProfile &&
                selectedEmployee && (

                    <div className="drawer-overlay">

                        <div className="employee-drawer">

                            <div className="drawer-header">

                                <div>

                                    <button
                                        className="back-button"
                                        onClick={
                                            closeProfile
                                        }
                                    >
                                        ←
                                    </button>

                                    <span className="drawer-title">
                                        Employee Profile
                                    </span>

                                </div>

                                <button
                                    className="close-button"
                                    onClick={
                                        closeProfile
                                    }
                                >
                                    ×
                                </button>

                            </div>

                            <div className="drawer-content">

                                {/* PROFILE HEADER */}

                                <div className="profile-header">

                                    <div className="large-avatar">

                                        {employeeFields
                                                .find(
                                                    (field) =>
                                                        field.fieldName?.toLowerCase() ===
                                                        "name"
                                                )
                                                ?.fieldValue
                                                ?.charAt(0)
                                                ?.toUpperCase()
                                            ||
                                            selectedEmployee.employeeId?.charAt(
                                                0
                                            )}

                                    </div>

                                    <div className="profile-main">

                                        <h2>

                                            {employeeFields
                                                    .find(
                                                        (field) =>
                                                            field.fieldName?.toLowerCase() ===
                                                            "name"
                                                    )
                                                    ?.fieldValue
                                                ||
                                                "Employee"}

                                        </h2>

                                        <div className="profile-id">

                                            {
                                                selectedEmployee.employeeId
                                            }

                                            <span
                                                className={
                                                    selectedEmployee.active
                                                        ? "status-badge active"
                                                        : "status-badge inactive"
                                                }
                                            >

                                                <span className="status-dot"></span>

                                                {selectedEmployee.active
                                                    ? "Active"
                                                    : "Inactive"}

                                            </span>

                                        </div>

                                        <div className="profile-department">

                                            <span>
                                                Department
                                            </span>

                                            <strong>
                                                {
                                                    getDepartmentName(
                                                        {
                                                            fields:
                                                            employeeFields
                                                        }
                                                    )
                                                }
                                            </strong>

                                        </div>

                                    </div>

                                </div>

                                {/* ADMIN ACTIONS */}

                                {isAdmin && (

                                    <div className="profile-actions">

                                        <button
                                            className="secondary-button"
                                            onClick={
                                                openPrefixModal
                                            }
                                        >
                                            Change ID Prefix
                                        </button>

                                        <button
                                            className="primary-button"
                                            onClick={
                                                openAddField
                                            }
                                        >
                                            + Add Field
                                        </button>

                                    </div>

                                )}

                                {/* STAFF INFO */}

                                {isStaff && (

                                    <div className="staff-info-box">

                                        <span>
                                            ℹ
                                        </span>

                                        <p>
                                            Employee information is
                                            controlled by ADMIN.
                                            Changes require approval
                                            where permitted.
                                        </p>

                                    </div>

                                )}

                                {/* EMPLOYEE INFORMATION */}

                                <section className="profile-section">

                                    <div className="section-heading">

                                        <div>

                                            <h3>
                                                Employee Information
                                            </h3>

                                            <p>
                                                Basic employment details
                                            </p>

                                        </div>

                                    </div>

                                    <div className="details-grid">

                                        <div className="detail-item">

                                            <span>
                                                Employee ID
                                            </span>

                                            <strong>
                                                {
                                                    selectedEmployee.employeeId
                                                }
                                            </strong>

                                        </div>

                                        {employeeFields
                                            .filter(
                                                (field) =>
                                                    field.fieldName?.toLowerCase() ===
                                                    "name"
                                            )
                                            .map((field) => (

                                                <div
                                                    className="detail-item"
                                                    key={
                                                        field.fieldId
                                                    }
                                                >

                                                    <span>
                                                        {
                                                            field.fieldName
                                                        }
                                                    </span>

                                                    <strong>
                                                        {
                                                            displayValue(
                                                                field.fieldValue
                                                            )
                                                        }
                                                    </strong>

                                                </div>

                                            ))}

                                        <div className="detail-item">

                                            <span>
                                                Department
                                            </span>

                                            <strong>
                                                {
                                                    getDepartmentName(
                                                        {
                                                            fields:
                                                            employeeFields
                                                        }
                                                    )
                                                }
                                            </strong>

                                        </div>

                                    </div>

                                </section>

                                {/* OTHER FIELDS */}

                                <section className="profile-section">

                                    <div className="section-heading">

                                        <div>

                                            <h3>
                                                Employee Details
                                            </h3>

                                            <p>
                                                Additional employee
                                                information
                                            </p>

                                        </div>

                                    </div>

                                    {profileLoading ? (

                                        <div className="profile-loading">
                                            Loading details...
                                        </div>

                                    ) : employeeFields.length === 0 ? (

                                        <div className="no-fields">
                                            No additional information
                                            available.
                                        </div>

                                    ) : (

                                        <div className="field-list">

                                            {employeeFields
                                                .filter(
                                                    (field) =>
                                                        field.fieldName?.toLowerCase() !==
                                                        "name" &&
                                                        field.fieldName?.toLowerCase() !==
                                                        "department"
                                                )
                                                .map((field) => (

                                                    <div
                                                        className="field-row"
                                                        key={
                                                            field.fieldId
                                                        }
                                                    >

                                                        <div className="field-information">

                                                            <span className="field-name">
                                                                {
                                                                    field.fieldName
                                                                }
                                                            </span>

                                                            <span className="field-value">
                                                                {
                                                                    displayValue(
                                                                        field.fieldValue
                                                                    )
                                                                }
                                                            </span>

                                                        </div>

                                                        {isAdmin ? (

                                                            <div className="field-actions">

                                                                <button
                                                                    onClick={() =>
                                                                        openEditField(
                                                                            field
                                                                        )
                                                                    }
                                                                >
                                                                    Edit
                                                                </button>

                                                                <button
                                                                    className="delete-action"
                                                                    onClick={() =>
                                                                        deleteField(
                                                                            field
                                                                        )
                                                                    }
                                                                >
                                                                    Delete
                                                                </button>

                                                            </div>

                                                        ) : (

                                                            <span className="protected-label">
                                                                Protected
                                                            </span>

                                                        )}

                                                    </div>

                                                ))}

                                        </div>

                                    )}

                                </section>

                            </div>

                        </div>

                    </div>

                )}

            {/* ====================================================
                ADD EMPLOYEE MODAL
            ==================================================== */}

            {showAddEmployee && (

                <div className="modal-overlay">

                    <div className="modal-card small-modal">

                        <div className="modal-header">

                            <div>

                                <h2>
                                    Add Employee
                                </h2>

                                <p>
                                    Create a new employee record.
                                </p>

                            </div>

                            <button
                                className="close-button"
                                onClick={() =>
                                    setShowAddEmployee(
                                        false
                                    )
                                }
                            >
                                ×
                            </button>

                        </div>

                        <div className="modal-content">

                            <div className="auto-id-box">

                                <span>
                                    Employee ID
                                </span>

                                <strong>
                                    Automatically generated
                                </strong>

                                <small>
                                    The system will generate the
                                    next available Employee ID.
                                </small>

                            </div>

                            <p className="modal-note">
                                Example: E001, E002, E003...
                                You can change the ID prefix
                                after creating the employee.
                            </p>

                        </div>

                        <div className="modal-footer">

                            <button
                                className="secondary-button"
                                onClick={() =>
                                    setShowAddEmployee(
                                        false
                                    )
                                }
                            >
                                Cancel
                            </button>

                            <button
                                className="primary-button"
                                onClick={
                                    createEmployee
                                }
                            >
                                Create Employee
                            </button>

                        </div>

                    </div>

                </div>

            )}

            {/* ====================================================
                FIELD MODAL
            ==================================================== */}

            {showFieldModal && (

                <div className="modal-overlay">

                    <div className="modal-card">

                        <div className="modal-header">

                            <div>

                                <h2>
                                    {editingField
                                        ? "Edit Employee Field"
                                        : "Add Employee Field"}
                                </h2>

                                <p>
                                    {isAdmin
                                        ? "Update employee information."
                                        : "Submit a change request for ADMIN approval."}
                                </p>

                            </div>

                            <button
                                className="close-button"
                                onClick={() =>
                                    setShowFieldModal(
                                        false
                                    )
                                }
                            >
                                ×
                            </button>

                        </div>

                        <div className="modal-content">

                            <label>
                                Field Name
                            </label>

                            <input
                                type="text"
                                value={fieldName}
                                onChange={(e) =>
                                    setFieldName(
                                        e.target.value
                                    )
                                }
                                placeholder="e.g. PAN, Bank Account, Address"
                                disabled={
                                    isStaff &&
                                    editingField === null
                                }
                            />

                            <label>
                                Field Value
                            </label>

                            <textarea
                                value={fieldValue}
                                onChange={(e) =>
                                    setFieldValue(
                                        e.target.value
                                    )
                                }
                                placeholder="Enter employee information"
                                rows="4"
                            />

                            {isStaff && (
                                <>
                                    <label>
                                        Reason
                                    </label>

                                    <textarea
                                        value={
                                            requestReason
                                        }
                                        onChange={(e) =>
                                            setRequestReason(
                                                e.target.value
                                            )
                                        }
                                        placeholder="Explain why this change is required"
                                        rows="3"
                                    />

                                    <div className="approval-note">
                                        This change will remain
                                        pending until ADMIN approves
                                        the request.
                                    </div>
                                </>
                            )}

                        </div>

                        <div className="modal-footer">

                            <button
                                className="secondary-button"
                                onClick={() =>
                                    setShowFieldModal(
                                        false
                                    )
                                }
                            >
                                Cancel
                            </button>

                            <button
                                className="primary-button"
                                onClick={
                                    saveField
                                }
                            >
                                {isAdmin
                                    ? "Save Changes"
                                    : "Submit Request"}
                            </button>

                        </div>

                    </div>

                </div>

            )}

            {/* ====================================================
                PREFIX MODAL
            ==================================================== */}

            {showPrefixModal &&
                selectedEmployee && (

                    <div className="modal-overlay">

                        <div className="modal-card small-modal">

                            <div className="modal-header">

                                <div>

                                    <h2>
                                        Change Employee ID Prefix
                                    </h2>

                                    <p>
                                        Update only the prefix.
                                    </p>

                                </div>

                                <button
                                    className="close-button"
                                    onClick={() =>
                                        setShowPrefixModal(
                                            false
                                        )
                                    }
                                >
                                    ×
                                </button>

                            </div>

                            <div className="modal-content">

                                <div className="current-id-box">

                                    <span>
                                        Current Employee ID
                                    </span>

                                    <strong>
                                        {
                                            selectedEmployee.employeeId
                                        }
                                    </strong>

                                </div>

                                <label>
                                    New Prefix
                                </label>

                                {/* =================================================
                                    FIXED PREFIX INPUT
                                    ================================================= */}

                                <input
                                    type="text"
                                    maxLength="10"
                                    value={prefix}
                                    onChange={(e) => {
                                        setPrefix(
                                            e.target.value.replace(
                                                /[^a-zA-Z]/g,
                                                ""
                                            )
                                        );
                                    }}
                                    placeholder="E"
                                />

                                <div className="id-preview">

                                    <span>
                                        Preview
                                    </span>

                                    <strong>

                                        {prefix.toUpperCase()}

                                        {
                                            selectedEmployee.employeeId
                                                .match(
                                                    /\d+$/
                                                )?.[0]
                                        }

                                    </strong>

                                </div>

                                {isStaff && (
                                    <>
                                        <label>
                                            Reason
                                        </label>

                                        <textarea
                                            value={
                                                requestReason
                                            }
                                            onChange={(e) =>
                                                setRequestReason(
                                                    e.target.value
                                                )
                                            }
                                            placeholder="Reason for changing the employee ID prefix"
                                            rows="3"
                                        />

                                        <div className="approval-note">
                                            The Employee ID will not
                                            change until ADMIN approves
                                            this request.
                                        </div>
                                    </>
                                )}

                            </div>

                            <div className="modal-footer">

                                <button
                                    className="secondary-button"
                                    onClick={() =>
                                        setShowPrefixModal(
                                            false
                                        )
                                    }
                                >
                                    Cancel
                                </button>

                                <button
                                    className="primary-button"
                                    onClick={
                                        changePrefix
                                    }
                                >
                                    {isAdmin
                                        ? "Save Changes"
                                        : "Submit Request"}
                                </button>

                            </div>

                        </div>

                    </div>

                )}

            {/* ====================================================
                ADMIN REQUESTS
            ==================================================== */}

            {showRequests && isAdmin && (

                <div className="modal-overlay">

                    <div className="modal-card requests-modal">

                        <div className="modal-header">

                            <div>

                                <h2>
                                    Employee Change Requests
                                </h2>

                                <p>
                                    Review pending STAFF requests.
                                </p>

                            </div>

                            <button
                                className="close-button"
                                onClick={() =>
                                    setShowRequests(
                                        false
                                    )
                                }
                            >
                                ×
                            </button>

                        </div>

                        <div className="requests-content">

                            {requests.length === 0 ? (

                                <div className="no-requests">

                                    <div>
                                        ✓
                                    </div>

                                    <h3>
                                        No pending requests
                                    </h3>

                                    <p>
                                        All employee change
                                        requests have been reviewed.
                                    </p>

                                </div>

                            ) : (

                                requests.map(
                                    (request) => (

                                        <div
                                            className="request-card"
                                            key={
                                                request.requestId
                                            }
                                        >

                                            <div className="request-top">

                                                <div>

                                                    <span className="request-type">
                                                        {
                                                            requestTypeLabel(
                                                                request.requestType
                                                            )
                                                        }
                                                    </span>

                                                    <h3>
                                                        {
                                                            request.employeeId
                                                        }
                                                    </h3>

                                                </div>

                                                <span className="pending-badge">
                                                    Pending
                                                </span>

                                            </div>

                                            <div className="request-details">

                                                <div>

                                                    <span>
                                                        Requested By
                                                    </span>

                                                    <strong>
                                                        {
                                                            request.requestedBy ||
                                                            "Staff"
                                                        }
                                                    </strong>

                                                </div>

                                                {request.requestType ===
                                                "EDIT_EMPLOYEE_ID_PREFIX" ? (

                                                    <div>

                                                        <span>
                                                            Employee ID
                                                        </span>

                                                        <strong>

                                                            {
                                                                request.oldFieldName
                                                            }

                                                            →

                                                            {
                                                                request.fieldName
                                                            }

                                                            {
                                                                request.employeeId
                                                                    ?.match(
                                                                        /\d+$/
                                                                    )?.[0]
                                                            }

                                                        </strong>

                                                    </div>

                                                ) : (

                                                    <div>

                                                        <span>
                                                            Field
                                                        </span>

                                                        <strong>
                                                            {
                                                                request.fieldName ||
                                                                request.oldFieldName ||
                                                                "—"
                                                            }
                                                        </strong>

                                                    </div>

                                                )}

                                                {request.fieldValue && (
                                                    <div>

                                                        <span>
                                                            Requested Value
                                                        </span>

                                                        <strong>
                                                            {
                                                                request.fieldValue
                                                            }
                                                        </strong>

                                                    </div>
                                                )}

                                            </div>

                                            {request.reason && (

                                                <div className="request-reason">

                                                    <span>
                                                        Reason
                                                    </span>

                                                    <p>
                                                        {
                                                            request.reason
                                                        }
                                                    </p>

                                                </div>

                                            )}

                                            <div className="request-actions">

                                                <button
                                                    className="reject-button"
                                                    onClick={() =>
                                                        rejectRequest(
                                                            request.requestId
                                                        )
                                                    }
                                                >
                                                    Reject
                                                </button>

                                                <button
                                                    className="approve-button"
                                                    onClick={() =>
                                                        approveRequest(
                                                            request.requestId
                                                        )
                                                    }
                                                >
                                                    Approve
                                                </button>

                                            </div>

                                        </div>

                                    )
                                )

                            )}

                        </div>

                    </div>

                </div>

            )}

        </div>
        </AdminLayout>

    );
}