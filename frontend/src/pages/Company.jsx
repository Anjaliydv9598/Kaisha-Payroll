import React, { useEffect, useState } from "react";
import "./Company.css";
import AdminLayout from "../pages/AdminLayout";

const API_URL = "http://localhost:8080/api/company";

const Company = () => {
    const token = localStorage.getItem("token");
    const role = localStorage.getItem("role");

    const isAdmin = role === "ADMIN";

    // =========================================================
    // COMPANY
    // =========================================================

    const [company, setCompany] = useState(null);
    const [companyFields, setCompanyFields] = useState([]);

    const [loading, setLoading] = useState(true);

    const [error, setError] = useState("");
    const [successMessage, setSuccessMessage] = useState("");

    // =========================================================
    // FIXED FIELD EDIT
    // =========================================================

    const [editingField, setEditingField] = useState(null);
    const [editValue, setEditValue] = useState("");

    // =========================================================
    // ADD DYNAMIC FIELD
    // =========================================================

    const [showAddModal, setShowAddModal] = useState(false);

    const [dynamicFieldName, setDynamicFieldName] = useState("");
    const [dynamicFieldValue, setDynamicFieldValue] = useState("");

    // =========================================================
    // EDIT DYNAMIC FIELD
    // =========================================================

    const [showEditModal, setShowEditModal] = useState(false);

    const [editingDynamicField, setEditingDynamicField] =
        useState(null);

    // =========================================================
    // LOAD DATA
    // =========================================================

    useEffect(() => {
        loadCompany();
    }, []);

    const loadCompany = async () => {
        try {
            setLoading(true);
            setError("");

            if (!token) {
                setError("Please login again.");
                return;
            }

            // =====================================================
            // LOAD COMPANY
            // =====================================================

            const companyResponse = await fetch(API_URL, {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });

            const companyData = await companyResponse.json();

            if (!companyResponse.ok) {
                throw new Error(
                    companyData.message ||
                    "Unable to load company information."
                );
            }

            setCompany(companyData);

            // =====================================================
            // LOAD DYNAMIC FIELDS
            // ADMIN ONLY
            // =====================================================

            if (isAdmin) {
                const fieldsResponse = await fetch(
                    `${API_URL}/fields`,
                    {
                        method: "GET",
                        headers: {
                            Authorization: `Bearer ${token}`,
                            "Content-Type": "application/json",
                        },
                    }
                );

                const fieldsData =
                    await fieldsResponse.json();

                if (!fieldsResponse.ok) {
                    throw new Error(
                        fieldsData.message ||
                        "Unable to load additional company fields."
                    );
                }

                setCompanyFields(
                    Array.isArray(fieldsData)
                        ? fieldsData
                        : []
                );
            }
        } catch (err) {
            console.error(
                "COMPANY LOAD ERROR:",
                err
            );

            setError(
                err.message ||
                "Something went wrong."
            );
        } finally {
            setLoading(false);
        }
    };

    // =========================================================
    // MESSAGES
    // =========================================================

    const clearMessages = () => {
        setError("");
        setSuccessMessage("");
    };

    const showSuccess = (message) => {
        setSuccessMessage(message);

        setTimeout(() => {
            setSuccessMessage("");
        }, 3000);
    };

    // =========================================================
    // FIXED FIELD
    // =========================================================

    const getDisplayName = (field) => {
        switch (field) {
            case "companyName":
                return "Company Name";

            case "address":
                return "Company Address / Location";

            case "panNo":
                return "PAN No.";

            case "tanNo":
                return "TAN No.";

            case "telephoneNumber":
                return "Telephone No.";

            default:
                return field;
        }
    };

    const startEditFixedField = (
        fieldName,
        value
    ) => {
        clearMessages();

        setEditingField(fieldName);
        setEditValue(value || "");
    };

    const cancelEditFixedField = () => {
        setEditingField(null);
        setEditValue("");
    };

    const saveFixedField = async () => {
        try {
            clearMessages();

            if (!company || !editingField) {
                return;
            }

            const updatedCompany = {
                companyName:
                    company.companyName || "",

                address:
                    company.address || "",

                telephoneNumber:
                    company.telephoneNumber || "",

                panNo:
                    company.panNo || "",

                tanNo:
                    company.tanNo || "",
            };

            // Update selected field

            if (editingField === "companyName") {
                updatedCompany.companyName =
                    editValue.trim();
            }

            if (editingField === "address") {
                updatedCompany.address =
                    editValue.trim();
            }

            if (editingField === "telephoneNumber") {
                updatedCompany.telephoneNumber =
                    editValue.trim();
            }

            if (editingField === "panNo") {
                updatedCompany.panNo =
                    editValue.trim().toUpperCase();
            }

            if (editingField === "tanNo") {
                updatedCompany.tanNo =
                    editValue.trim().toUpperCase();
            }

            // Validation

            if (
                editingField === "companyName" &&
                !updatedCompany.companyName
            ) {
                setError(
                    "Company Name is required."
                );
                return;
            }

            // =====================================================
            // UPDATE COMPANY
            // =====================================================

            const response = await fetch(API_URL, {
                method: "PUT",
                headers: {
                    Authorization:
                        `Bearer ${token}`,
                    "Content-Type":
                        "application/json",
                },
                body: JSON.stringify(
                    updatedCompany
                ),
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    "Unable to update company."
                );
            }

            setCompany(data);

            const displayName =
                getDisplayName(editingField);

            setEditingField(null);
            setEditValue("");

            showSuccess(
                `${displayName} updated successfully.`
            );
        } catch (err) {
            console.error(
                "COMPANY UPDATE ERROR:",
                err
            );

            setError(
                err.message ||
                "Unable to update company."
            );
        }
    };

    // =========================================================
    // ADD DYNAMIC FIELD
    // =========================================================

    const openAddModal = () => {
        clearMessages();

        setDynamicFieldName("");
        setDynamicFieldValue("");

        setShowAddModal(true);
    };

    const closeAddModal = () => {
        setShowAddModal(false);

        setDynamicFieldName("");
        setDynamicFieldValue("");
    };

    const addDynamicField = async () => {
        try {
            clearMessages();

            const fieldName =
                dynamicFieldName.trim();

            const fieldValue =
                dynamicFieldValue.trim();

            if (!fieldName) {
                setError(
                    "Field Name is required."
                );
                return;
            }

            if (!fieldValue) {
                setError(
                    "Value of Field is required."
                );
                return;
            }

            // =====================================================
            // SAVE TO DATABASE
            // =====================================================

            const response = await fetch(
                `${API_URL}/fields`,
                {
                    method: "POST",
                    headers: {
                        Authorization:
                            `Bearer ${token}`,
                        "Content-Type":
                            "application/json",
                    },
                    body: JSON.stringify({
                        fieldName,
                        fieldValue,
                    }),
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    "Unable to add company field."
                );
            }

            closeAddModal();

            await loadCompany();

            showSuccess(
                "Company field added successfully."
            );
        } catch (err) {
            console.error(
                "ADD COMPANY FIELD ERROR:",
                err
            );

            setError(
                err.message ||
                "Unable to add company field."
            );
        }
    };

    // =========================================================
    // EDIT DYNAMIC FIELD
    // =========================================================

    const openEditDynamicField = (field) => {
        clearMessages();

        setEditingDynamicField(field);

        setDynamicFieldName(
            field.fieldName || ""
        );

        setDynamicFieldValue(
            field.fieldValue || ""
        );

        setShowEditModal(true);
    };

    const closeEditModal = () => {
        setShowEditModal(false);

        setEditingDynamicField(null);

        setDynamicFieldName("");
        setDynamicFieldValue("");
    };

    const updateDynamicField = async () => {
        try {
            clearMessages();

            if (!editingDynamicField) {
                return;
            }

            const fieldName =
                dynamicFieldName.trim();

            const fieldValue =
                dynamicFieldValue.trim();

            if (!fieldName) {
                setError(
                    "Field Name is required."
                );
                return;
            }

            if (!fieldValue) {
                setError(
                    "Value of Field is required."
                );
                return;
            }

            // =====================================================
            // UPDATE DATABASE
            // =====================================================

            const response = await fetch(
                `${API_URL}/fields/${editingDynamicField.fieldId}`,
                {
                    method: "PUT",
                    headers: {
                        Authorization:
                            `Bearer ${token}`,
                        "Content-Type":
                            "application/json",
                    },
                    body: JSON.stringify({
                        fieldName,
                        fieldValue,
                    }),
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    "Unable to update company field."
                );
            }

            closeEditModal();

            await loadCompany();

            showSuccess(
                "Company field updated successfully."
            );
        } catch (err) {
            console.error(
                "UPDATE COMPANY FIELD ERROR:",
                err
            );

            setError(
                err.message ||
                "Unable to update company field."
            );
        }
    };

    // =========================================================
    // DELETE DYNAMIC FIELD
    // =========================================================

    const deleteDynamicField = async (field) => {
        try {
            clearMessages();

            const confirmed = window.confirm(
                `Are you sure you want to delete "${field.fieldName}"?`
            );

            if (!confirmed) {
                return;
            }

            // =====================================================
            // DELETE FROM DATABASE
            // =====================================================

            const response = await fetch(
                `${API_URL}/fields/${field.fieldId}`,
                {
                    method: "DELETE",
                    headers: {
                        Authorization:
                            `Bearer ${token}`,
                        "Content-Type":
                            "application/json",
                    },
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    "Unable to delete company field."
                );
            }

            await loadCompany();

            showSuccess(
                "Company field deleted successfully."
            );
        } catch (err) {
            console.error(
                "DELETE COMPANY FIELD ERROR:",
                err
            );

            setError(
                err.message ||
                "Unable to delete company field."
            );
        }
    };

    // =========================================================
    // FIXED FIELD UI
    // =========================================================

    const renderFixedField = (
        label,
        fieldName,
        value
    ) => {
        const isEditing =
            editingField === fieldName;

        return (
            <div className="company-field-row">

                <div className="company-field-label">
                    {label}
                </div>

                <div className="company-field-content">

                    {isEditing ? (
                        <input
                            type="text"
                            className="company-edit-input"
                            value={editValue}
                            onChange={(e) =>
                                setEditValue(
                                    e.target.value
                                )
                            }
                            autoFocus
                        />
                    ) : (
                        <span className="company-field-value">
                            {value || "—"}
                        </span>
                    )}

                </div>

                <div className="company-field-actions">

                    {isEditing ? (
                        <>
                            <button
                                type="button"
                                className="company-save-btn"
                                onClick={
                                    saveFixedField
                                }
                            >
                                Save
                            </button>

                            <button
                                type="button"
                                className="company-cancel-btn"
                                onClick={
                                    cancelEditFixedField
                                }
                            >
                                Cancel
                            </button>
                        </>
                    ) : (
                        <button
                            type="button"
                            className="company-edit-btn"
                            onClick={() =>
                                startEditFixedField(
                                    fieldName,
                                    value
                                )
                            }
                        >
                            Edit
                        </button>
                    )}

                </div>
            </div>
        );
    };

    // =========================================================
    // LOADING
    // =========================================================

    if (loading) {
        return (
            <div className="company-page">
                <div className="company-loading">
                    Loading company information...
                </div>
            </div>
        );
    }

    // =========================================================
    // NO COMPANY
    // =========================================================

    if (!company) {
        return (
            <div className="company-page">

                <div className="company-error">
                    {error ||
                        "Company information not found."}
                </div>

                <button
                    type="button"
                    className="company-retry-btn"
                    onClick={loadCompany}
                >
                    Retry
                </button>

            </div>
        );
    }

    // =========================================================
    // MAIN
    // =========================================================

    return (
        <AdminLayout>
        <div className="company-page">

            {/* =================================================
                HEADER
            ================================================= */}

            <div className="company-header">

                <div>
                    <h2>
                        Company Information
                    </h2>

                    <p>
                        View and manage company details
                    </p>
                </div>

                {isAdmin && (
                    <button
                        type="button"
                        className="company-add-btn"
                        onClick={openAddModal}
                    >
                        + Add
                    </button>
                )}

            </div>


            {/* =================================================
                ERROR
            ================================================= */}

            {error && (
                <div className="company-error-message">

                    <span>{error}</span>

                    <button
                        type="button"
                        onClick={() =>
                            setError("")
                        }
                    >
                        ×
                    </button>

                </div>
            )}


            {/* =================================================
                SUCCESS
            ================================================= */}

            {successMessage && (
                <div className="company-success-message">
                    {successMessage}
                </div>
            )}


            {/* =================================================
                STAFF
            ================================================= */}

            {!isAdmin ? (

                <div className="company-card staff-company-card">

                    <div className="company-card-title">
                        Company
                    </div>

                    <div className="company-field-row">

                        <div className="company-field-label">
                            Company Name
                        </div>

                        <div className="company-field-content">

                            <span className="company-field-value">
                                {company.companyName ||
                                    "—"}
                            </span>

                        </div>

                    </div>

                </div>

            ) : (

                /* =================================================
                   ADMIN
                ================================================= */

                <>

                    {/* =================================================
                        FIXED COMPANY DETAILS
                    ================================================= */}

                    <div className="company-card">

                        <div className="company-card-title">
                            Company Details
                        </div>


                        {/* COMPANY ID */}

                        <div className="company-field-row">

                            <div className="company-field-label">
                                Company ID
                            </div>

                            <div className="company-field-content">

                                <span className="company-field-value">
                                    {company.companyId ||
                                        "—"}
                                </span>

                            </div>

                            <div className="company-field-actions">

                                <span className="read-only-text">
                                    Read-only
                                </span>

                            </div>

                        </div>


                        {/* COMPANY NAME */}

                        {renderFixedField(
                            "Company Name",
                            "companyName",
                            company.companyName
                        )}


                        {/* ADDRESS */}

                        {renderFixedField(
                            "Company Address / Location",
                            "address",
                            company.address
                        )}


                        {/* PAN */}

                        {renderFixedField(
                            "PAN No.",
                            "panNo",
                            company.panNo
                        )}


                        {/* TAN */}

                        {renderFixedField(
                            "TAN No.",
                            "tanNo",
                            company.tanNo
                        )}


                        {/* TELEPHONE */}

                        {renderFixedField(
                            "Telephone No.",
                            "telephoneNumber",
                            company.telephoneNumber
                        )}

                    </div>


                    {/* =================================================
                        DYNAMIC FIELDS
                    ================================================= */}

                    <div className="company-card">

                        <div className="company-card-title">
                            Additional Company Information
                        </div>


                        {companyFields.length === 0 ? (

                            <div className="no-company-fields">
                                No additional company fields
                                have been added yet.
                            </div>

                        ) : (

                            companyFields.map(
                                (field) => (
                                    <div
                                        className="company-field-row dynamic-company-field"
                                        key={
                                            field.fieldId
                                        }
                                    >

                                        <div className="company-field-label">
                                            {field.fieldName}
                                        </div>

                                        <div className="company-field-content">

                                            <span className="company-field-value">
                                                {field.fieldValue ||
                                                    "—"}
                                            </span>

                                        </div>

                                        <div className="company-field-actions">

                                            <button
                                                type="button"
                                                className="company-edit-btn"
                                                onClick={() =>
                                                    openEditDynamicField(
                                                        field
                                                    )
                                                }
                                            >
                                                Edit
                                            </button>

                                            <button
                                                type="button"
                                                className="company-delete-btn"
                                                onClick={() =>
                                                    deleteDynamicField(
                                                        field
                                                    )
                                                }
                                            >
                                                Delete
                                            </button>

                                        </div>

                                    </div>
                                )
                            )

                        )}

                    </div>

                </>
            )}


            {/* =================================================
                ADD FIELD MODAL
            ================================================= */}

            {showAddModal && (

                <div
                    className="company-modal-overlay"
                    onClick={closeAddModal}
                >

                    <div
                        className="company-modal"
                        onClick={(e) =>
                            e.stopPropagation()
                        }
                    >

                        <div className="company-modal-header">

                            <h3>
                                Add Company Field
                            </h3>

                            <button
                                type="button"
                                className="company-modal-close"
                                onClick={closeAddModal}
                            >
                                ×
                            </button>

                        </div>


                        <div className="company-modal-body">

                            <div className="company-form-group">

                                <label>
                                    Field Name
                                    <span>*</span>
                                </label>

                                <input
                                    type="text"
                                    value={
                                        dynamicFieldName
                                    }
                                    onChange={(e) =>
                                        setDynamicFieldName(
                                            e.target.value
                                        )
                                    }
                                    placeholder="Enter field name"
                                />

                            </div>


                            <div className="company-form-group">

                                <label>
                                    Value of Field
                                    <span>*</span>
                                </label>

                                <input
                                    type="text"
                                    value={
                                        dynamicFieldValue
                                    }
                                    onChange={(e) =>
                                        setDynamicFieldValue(
                                            e.target.value
                                        )
                                    }
                                    placeholder="Enter value"
                                />

                            </div>

                        </div>


                        <div className="company-modal-footer">

                            <button
                                type="button"
                                className="company-cancel-btn"
                                onClick={
                                    closeAddModal
                                }
                            >
                                Cancel
                            </button>

                            <button
                                type="button"
                                className="company-save-btn"
                                onClick={
                                    addDynamicField
                                }
                            >
                                Save
                            </button>

                        </div>

                    </div>

                </div>
            )}


            {/* =================================================
                EDIT FIELD MODAL
            ================================================= */}

            {showEditModal && (

                <div
                    className="company-modal-overlay"
                    onClick={closeEditModal}
                >

                    <div
                        className="company-modal"
                        onClick={(e) =>
                            e.stopPropagation()
                        }
                    >

                        <div className="company-modal-header">

                            <h3>
                                Edit Company Field
                            </h3>

                            <button
                                type="button"
                                className="company-modal-close"
                                onClick={
                                    closeEditModal
                                }
                            >
                                ×
                            </button>

                        </div>


                        <div className="company-modal-body">

                            <div className="company-form-group">

                                <label>
                                    Field Name
                                    <span>*</span>
                                </label>

                                <input
                                    type="text"
                                    value={
                                        dynamicFieldName
                                    }
                                    onChange={(e) =>
                                        setDynamicFieldName(
                                            e.target.value
                                        )
                                    }
                                    placeholder="Enter field name"
                                />

                            </div>


                            <div className="company-form-group">

                                <label>
                                    Value of Field
                                    <span>*</span>
                                </label>

                                <input
                                    type="text"
                                    value={
                                        dynamicFieldValue
                                    }
                                    onChange={(e) =>
                                        setDynamicFieldValue(
                                            e.target.value
                                        )
                                    }
                                    placeholder="Enter value"
                                />

                            </div>

                        </div>


                        <div className="company-modal-footer">

                            <button
                                type="button"
                                className="company-cancel-btn"
                                onClick={
                                    closeEditModal
                                }
                            >
                                Cancel
                            </button>

                            <button
                                type="button"
                                className="company-save-btn"
                                onClick={
                                    updateDynamicField
                                }
                            >
                                Save
                            </button>

                        </div>

                    </div>

                </div>
            )}

        </div>
        </AdminLayout>
    );
};

export default Company;