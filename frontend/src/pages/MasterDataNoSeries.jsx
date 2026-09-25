import React, { useEffect, useState } from "react";
import "./MasterDataNoSeries.css";

const API_URL = "http://localhost:8080/api/employee-number-series";

function MasterDataNoSeries() {

    const [series, setSeries] = useState([]);

    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    const [showForm, setShowForm] = useState(false);

    const [editingId, setEditingId] = useState(null);

    const [showDeleteModal, setShowDeleteModal] =
        useState(false);

    const [deleteTarget, setDeleteTarget] =
        useState(null);

    const [formData, setFormData] = useState({
        department: "",
        prefix: "",
        startNumber: "001",
        endNumber: "1000",
        numberLength: "3",
        active: true
    });


    // =====================================================
    // TOKEN
    // =====================================================

    const getToken = () => {

        return localStorage.getItem("token");

    };


    // =====================================================
    // LOAD SERIES
    // =====================================================

    const loadSeries = async () => {

        try {

            setLoading(true);
            setError("");

            const token = getToken();

            const response = await fetch(
                API_URL,
                {
                    method: "GET",

                    headers: {
                        Authorization:
                            `Bearer ${token}`,

                        Accept:
                            "application/json"
                    }
                }
            );


            if (!response.ok) {

                const text =
                    await response.text();

                throw new Error(
                    text ||
                    "Unable to load number series."
                );
            }


            const data =
                await response.json();


            setSeries(
                Array.isArray(data)
                    ? data
                    : []
            );

        } catch (err) {

            console.error(
                "LOAD NUMBER SERIES ERROR:",
                err
            );

            setError(
                err.message ||
                "Unable to load number series."
            );

        } finally {

            setLoading(false);

        }
    };


    useEffect(() => {

        loadSeries();

    }, []);


    // =====================================================
    // RESET FORM
    // =====================================================

    const resetForm = () => {

        setFormData({
            department: "",
            prefix: "",
            startNumber: "001",
            endNumber: "1000",
            numberLength: "3",
            active: true
        });

        setEditingId(null);

    };


    // =====================================================
    // OPEN ADD
    // =====================================================

    const handleAdd = () => {

        setError("");
        setSuccess("");

        resetForm();

        setShowForm(true);

    };


    // =====================================================
    // OPEN EDIT
    // =====================================================

    const handleEdit = (item) => {

        setError("");
        setSuccess("");

        setEditingId(
            item.id
        );

        setFormData({

            department:
                item.department || "",

            prefix:
                item.prefix || "",

            startNumber:
                formatNumber(
                    item.startNumber,
                    item.numberLength || 3
                ),

            endNumber:
                formatNumber(
                    item.endNumber,
                    item.numberLength || 3
                ),

            numberLength:
                String(
                    item.numberLength || 3
                ),

            active:
                item.active !== false
        });

        setShowForm(true);

    };


    // =====================================================
    // FORMAT NUMBER
    // =====================================================

    const formatNumber = (
        value,
        length
    ) => {

        if (
            value === null ||
            value === undefined ||
            value === ""
        ) {

            return "";

        }

        return String(value)
            .padStart(
                Number(length) || 3,
                "0"
            );

    };


    // =====================================================
    // FORM CHANGE
    // =====================================================

    const handleChange = (event) => {

        const {
            name,
            value,
            type,
            checked
        } = event.target;


        setFormData(
            previous => ({

                ...previous,

                [name]:
                    type === "checkbox"
                        ? checked
                        : value

            })
        );

    };


    // =====================================================
    // PREVIEW
    // =====================================================

    const getPreview = () => {

        const prefix =
            formData.prefix
                .trim()
                .toUpperCase();


        const length =
            Number(
                formData.numberLength
            ) || 3;


        const start =
            formData.startNumber
                ? String(
                    formData.startNumber
                ).replace(/\D/g, "")
                : "0";


        const end =
            formData.endNumber
                ? String(
                    formData.endNumber
                ).replace(/\D/g, "")
                : "0";


        return (
            `${prefix}${start.padStart(length, "0")} → ` +
            `${prefix}${end.padStart(length, "0")}`
        );

    };


    // =====================================================
    // SAVE / UPDATE
    // =====================================================

    const handleSubmit = async (event) => {

        event.preventDefault();

        setError("");
        setSuccess("");


        const department =
            formData.department.trim();


        const prefix =
            formData.prefix
                .trim()
                .toUpperCase();


        const startNumber =
            Number(
                String(
                    formData.startNumber
                ).replace(/\D/g, "")
            );


        const endNumber =
            Number(
                String(
                    formData.endNumber
                ).replace(/\D/g, "")
            );


        const numberLength =
            Number(
                formData.numberLength
            );


        // =================================================
        // VALIDATION
        // =================================================

        if (!department) {

            setError(
                "Department is required."
            );

            return;
        }


        if (!prefix) {

            setError(
                "Prefix is required."
            );

            return;
        }


        if (
            !Number.isInteger(
                startNumber
            )
        ) {

            setError(
                "Starting number is required."
            );

            return;
        }


        if (
            !Number.isInteger(
                endNumber
            )
        ) {

            setError(
                "Ending number is required."
            );

            return;
        }


        if (
            startNumber < 0 ||
            endNumber < 0
        ) {

            setError(
                "Numbers cannot be negative."
            );

            return;
        }


        if (
            endNumber < startNumber
        ) {

            setError(
                "Ending number must be greater than or equal to starting number."
            );

            return;
        }


        if (
            !Number.isInteger(
                numberLength
            ) ||
            numberLength < 1 ||
            numberLength > 10
        ) {

            setError(
                "Number length must be between 1 and 10."
            );

            return;
        }


        // =================================================
        // PAYLOAD
        // =================================================

        const payload = {

            department,

            prefix,

            startNumber,

            endNumber,

            numberLength,

            active:
            formData.active
        };


        try {

            setSaving(true);


            const token =
                getToken();


            const response =
                await fetch(

                    editingId
                        ? `${API_URL}/${editingId}`
                        : API_URL,

                    {

                        method:
                            editingId
                                ? "PUT"
                                : "POST",

                        headers: {

                            Authorization:
                                `Bearer ${token}`,

                            "Content-Type":
                                "application/json",

                            Accept:
                                "application/json"
                        },

                        body:
                            JSON.stringify(
                                payload
                            )
                    }
                );


            if (!response.ok) {

                let message =
                    "Unable to save number series.";

                try {

                    const data =
                        await response.json();

                    message =
                        data.message ||
                        message;

                } catch {

                    const text =
                        await response.text();

                    if (text) {
                        message = text;
                    }

                }

                throw new Error(
                    message
                );
            }


            setSuccess(

                editingId
                    ? "Number series updated successfully."
                    : "Number series created successfully."

            );


            setShowForm(false);

            resetForm();

            await loadSeries();

        } catch (err) {

            console.error(
                "SAVE NUMBER SERIES ERROR:",
                err
            );

            setError(
                err.message ||
                "Unable to save number series."
            );

        } finally {

            setSaving(false);

        }

    };


    // =====================================================
    // OPEN DELETE
    // =====================================================

    const handleDeleteClick = (item) => {

        setDeleteTarget(item);

        setShowDeleteModal(true);

        setError("");

        setSuccess("");

    };


    // =====================================================
    // DELETE
    // =====================================================

    const handleDelete = async () => {

        if (!deleteTarget) {
            return;
        }


        try {

            setSaving(true);

            setError("");


            const token =
                getToken();


            const response =
                await fetch(

                    `${API_URL}/${deleteTarget.id}`,

                    {

                        method: "DELETE",

                        headers: {

                            Authorization:
                                `Bearer ${token}`
                        }
                    }
                );


            if (!response.ok) {

                let message =
                    "Unable to delete number series.";

                try {

                    const data =
                        await response.json();

                    message =
                        data.message ||
                        message;

                } catch {

                    const text =
                        await response.text();

                    if (text) {
                        message = text;
                    }

                }

                throw new Error(
                    message
                );
            }


            setSuccess(
                "Number series deleted successfully."
            );


            setShowDeleteModal(false);

            setDeleteTarget(null);

            await loadSeries();

        } catch (err) {

            console.error(
                "DELETE NUMBER SERIES ERROR:",
                err
            );

            setError(
                err.message ||
                "Unable to delete number series."
            );

        } finally {

            setSaving(false);

        }

    };


    // =====================================================
    // DISPLAY NUMBER
    // =====================================================

    const displayNumber = (
        value,
        length = 3
    ) => {

        if (
            value === null ||
            value === undefined
        ) {

            return "-";

        }

        return String(value)
            .padStart(
                Number(length) || 3,
                "0"
            );

    };


    // =====================================================
    // NEXT ID
    // =====================================================

    const getNextId = (item) => {

        if (
            item.nextNumber === null ||
            item.nextNumber === undefined
        ) {

            return `${item.prefix}${displayNumber(
                item.startNumber,
                item.numberLength
            )}`;

        }


        return `${item.prefix}${displayNumber(
            item.nextNumber,
            item.numberLength
        )}`;

    };


    // =====================================================
    // RENDER
    // =====================================================

    return (

        <div className="master-series-page">


            {/* =================================================
                HEADER
            ================================================= */}

            <div className="master-series-header">

                <div>

                    <h1>
                        MASTER DATA NO. SERIES
                    </h1>

                    <p>
                        Manage employee number series
                    </p>

                </div>


                <button
                    className="add-series-button"
                    onClick={handleAdd}
                >
                    + Add Series
                </button>

            </div>


            {/* =================================================
                SUCCESS
            ================================================= */}

            {success && (

                <div className="master-success">
                    {success}
                </div>

            )}


            {/* =================================================
                ERROR
            ================================================= */}

            {error && (

                <div className="master-error">
                    {error}
                </div>

            )}


            {/* =================================================
                ADD / EDIT FORM
            ================================================= */}

            {showForm && (

                <div className="series-form-card">

                    <div className="series-form-header">

                        <div>

                            <h2>
                                {editingId
                                    ? "Edit Number Series"
                                    : "Add Number Series"}
                            </h2>

                            <p>
                                Configure the employee ID range.
                            </p>

                        </div>


                        <button
                            className="close-form-button"
                            onClick={() => {
                                setShowForm(false);
                                resetForm();
                            }}
                        >
                            ×
                        </button>

                    </div>


                    <form
                        onSubmit={
                            handleSubmit
                        }
                    >

                        <div className="series-form-grid">


                            {/* DEPARTMENT */}

                            <div className="form-field">

                                <label>
                                    Department
                                </label>

                                <input
                                    name="department"
                                    value={
                                        formData.department
                                    }
                                    onChange={
                                        handleChange
                                    }
                                    placeholder="HR"
                                />

                            </div>


                            {/* PREFIX */}

                            <div className="form-field">

                                <label>
                                    Prefix
                                </label>

                                <input
                                    name="prefix"
                                    value={
                                        formData.prefix
                                    }
                                    onChange={
                                        handleChange
                                    }
                                    placeholder="HR"
                                />

                            </div>


                            {/* START */}

                            <div className="form-field">

                                <label>
                                    Starting Number
                                </label>

                                <input
                                    name="startNumber"
                                    value={
                                        formData.startNumber
                                    }
                                    onChange={
                                        handleChange
                                    }
                                    inputMode="numeric"
                                    placeholder="001"
                                />

                            </div>


                            {/* END */}

                            <div className="form-field">

                                <label>
                                    Ending Number
                                </label>

                                <input
                                    name="endNumber"
                                    value={
                                        formData.endNumber
                                    }
                                    onChange={
                                        handleChange
                                    }
                                    inputMode="numeric"
                                    placeholder="1000"
                                />

                            </div>


                            {/* LENGTH */}

                            <div className="form-field">

                                <label>
                                    Number Length
                                </label>

                                <input
                                    name="numberLength"
                                    value={
                                        formData.numberLength
                                    }
                                    onChange={
                                        handleChange
                                    }
                                    type="number"
                                    min="1"
                                    max="10"
                                />

                            </div>


                            {/* STATUS */}

                            <div className="form-field">

                                <label>
                                    Status
                                </label>

                                <select
                                    name="active"
                                    value={
                                        formData.active
                                            ? "true"
                                            : "false"
                                    }
                                    onChange={(e) =>
                                        setFormData(
                                            previous => ({
                                                ...previous,
                                                active:
                                                    e.target.value ===
                                                    "true"
                                            })
                                        )
                                    }
                                >

                                    <option value="true">
                                        Active
                                    </option>

                                    <option value="false">
                                        Inactive
                                    </option>

                                </select>

                            </div>

                        </div>


                        {/* PREVIEW */}

                        <div className="series-preview">

                            <span>
                                Preview
                            </span>

                            <strong>
                                {getPreview()}
                            </strong>

                        </div>


                        {/* BUTTONS */}

                        <div className="series-form-actions">

                            <button
                                type="button"
                                className="cancel-series-button"
                                onClick={() => {
                                    setShowForm(false);
                                    resetForm();
                                }}
                            >
                                Cancel
                            </button>


                            <button
                                type="submit"
                                className="save-series-button"
                                disabled={saving}
                            >
                                {saving
                                    ? "Saving..."
                                    : editingId
                                        ? "Update Series"
                                        : "Save Series"}
                            </button>

                        </div>

                    </form>

                </div>

            )}


            {/* =================================================
                SERIES LIST
            ================================================= */}

            <div className="series-section">

                <div className="section-heading">

                    <h2>
                        Employee Number Series
                    </h2>

                    <span>
                        {series.length} series
                    </span>

                </div>


                {loading ? (

                    <div className="series-empty">
                        Loading number series...
                    </div>

                ) : series.length === 0 ? (

                    <div className="series-empty">

                        <div className="empty-icon">
                            #
                        </div>

                        <h3>
                            No number series configured
                        </h3>

                        <p>
                            Add a department number series
                            to start generating employee IDs.
                        </p>

                        <button
                            className="add-series-button"
                            onClick={handleAdd}
                        >
                            + Add Series
                        </button>

                    </div>

                ) : (

                    <div className="series-grid">

                        {series.map((item) => (

                            <div
                                className="series-card"
                                key={item.id}
                            >

                                {/* CARD HEADER */}

                                <div className="series-card-header">

                                    <div>

                                        <h3>
                                            {item.department}
                                        </h3>

                                        <span className="prefix-badge">
                                            {item.prefix}
                                        </span>

                                    </div>


                                    <div className="series-card-actions">

                                        <button
                                            className="edit-series-button"
                                            onClick={() =>
                                                handleEdit(item)
                                            }
                                        >
                                            Edit
                                        </button>


                                        <button
                                            className="delete-series-button"
                                            onClick={() =>
                                                handleDeleteClick(item)
                                            }
                                        >
                                            Delete
                                        </button>

                                    </div>

                                </div>


                                {/* DETAILS */}

                                <div className="series-details">


                                    <div className="series-detail">

                                        <span>
                                            Department
                                        </span>

                                        <strong>
                                            {item.department}
                                        </strong>

                                    </div>


                                    <div className="series-detail">

                                        <span>
                                            Prefix
                                        </span>

                                        <strong>
                                            {item.prefix}
                                        </strong>

                                    </div>


                                    <div className="series-detail">

                                        <span>
                                            Starting Number
                                        </span>

                                        <strong>
                                            {displayNumber(
                                                item.startNumber,
                                                item.numberLength
                                            )}
                                        </strong>

                                    </div>


                                    <div className="series-detail">

                                        <span>
                                            Ending Number
                                        </span>

                                        <strong>
                                            {displayNumber(
                                                item.endNumber,
                                                item.numberLength
                                            )}
                                        </strong>

                                    </div>


                                    <div className="series-detail next-id-detail">

                                        <span>
                                            Next ID
                                        </span>

                                        <strong>
                                            {getNextId(item)}
                                        </strong>

                                    </div>


                                    <div className="series-detail">

                                        <span>
                                            Status
                                        </span>

                                        <strong
                                            className={
                                                item.active
                                                    ? "status-active"
                                                    : "status-inactive"
                                            }
                                        >

                                            <span className="status-dot">
                                                ●
                                            </span>

                                            {item.active
                                                ? "Active"
                                                : "Inactive"}

                                        </strong>

                                    </div>

                                </div>


                                {/* PREVIEW */}

                                <div className="card-preview">

                                    <span>
                                        Preview
                                    </span>

                                    <strong>
                                        {item.prefix}
                                        {displayNumber(
                                            item.startNumber,
                                            item.numberLength
                                        )}
                                        {" → "}
                                        {item.prefix}
                                        {displayNumber(
                                            item.endNumber,
                                            item.numberLength
                                        )}
                                    </strong>

                                </div>

                            </div>

                        ))}

                    </div>

                )}

            </div>


            {/* =================================================
                DELETE MODAL
            ================================================= */}

            {showDeleteModal &&
                deleteTarget && (

                    <div className="delete-overlay">

                        <div className="delete-modal">

                            <div className="delete-icon">
                                !
                            </div>


                            <h2>
                                Delete Number Series?
                            </h2>


                            <p>
                                Are you sure you want to delete
                                the following number series?
                            </p>


                            <div className="delete-summary">

                                <strong>
                                    {deleteTarget.department}
                                </strong>

                                <span>
                                {deleteTarget.prefix}
                                    {displayNumber(
                                        deleteTarget.startNumber,
                                        deleteTarget.numberLength
                                    )}
                                    {" → "}
                                    {deleteTarget.prefix}
                                    {displayNumber(
                                        deleteTarget.endNumber,
                                        deleteTarget.numberLength
                                    )}
                            </span>

                            </div>


                            <div className="delete-actions">

                                <button
                                    className="cancel-delete-button"
                                    onClick={() => {
                                        setShowDeleteModal(false);
                                        setDeleteTarget(null);
                                    }}
                                >
                                    Cancel
                                </button>


                                <button
                                    className="confirm-delete-button"
                                    onClick={
                                        handleDelete
                                    }
                                    disabled={saving}
                                >
                                    {saving
                                        ? "Deleting..."
                                        : "Delete"}
                                </button>

                            </div>

                        </div>

                    </div>

                )}

        </div>

    );

}

export default MasterDataNoSeries;