import React, { useEffect, useState } from "react";
import "./Department.css";

const API_BASE = "http://localhost:8080/api";

function Department() {
    const token = localStorage.getItem("token");
    const role = localStorage.getItem("role");

    const isAdmin = role === "ADMIN";
    const isStaff = role === "STAFF";

    // =========================================================
    // DEPARTMENT STATE
    // =========================================================

    const [departments, setDepartments] = useState([]);
    const [selectedDepartment, setSelectedDepartment] =
        useState(null);

    // =========================================================
    // POSITION STATE
    // =========================================================

    const [positions, setPositions] = useState([]);

    // =========================================================
    // LOADING / ERROR / SUCCESS
    // =========================================================

    const [loading, setLoading] = useState(false);
    const [positionsLoading, setPositionsLoading] =
        useState(false);

    const [error, setError] = useState("");
    const [successMessage, setSuccessMessage] =
        useState("");

    // =========================================================
    // DEPARTMENT MODAL
    // =========================================================

    const [showDepartmentModal, setShowDepartmentModal] =
        useState(false);

    const [editingDepartment, setEditingDepartment] =
        useState(null);

    const [departmentName, setDepartmentName] =
        useState("");

    // =========================================================
    // POSITION MODAL
    // =========================================================

    const [showPositionModal, setShowPositionModal] =
        useState(false);

    const [editingPosition, setEditingPosition] =
        useState(null);

    const [positionName, setPositionName] =
        useState("");

    // =========================================================
    // STAFF REQUEST MODAL
    // =========================================================

    const [showRequestModal, setShowRequestModal] =
        useState(false);

    const [requestType, setRequestType] =
        useState("");

    const [requestDepartment, setRequestDepartment] =
        useState(null);

    const [requestPosition, setRequestPosition] =
        useState(null);

    const [requestName, setRequestName] =
        useState("");

    const [requestReason, setRequestReason] =
        useState("");

    // =========================================================
    // ADMIN PENDING REQUESTS
    // =========================================================

    const [pendingRequests, setPendingRequests] =
        useState([]);

    // =========================================================
    // LOAD DEPARTMENTS
    // =========================================================

    const loadDepartments = async () => {
        try {
            setLoading(true);
            setError("");

            const response = await fetch(
                `${API_BASE}/departments`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    typeof data === "string"
                        ? data
                        : "Failed to load departments."
                );
            }

            setDepartments(data);

            return data;
        } catch (err) {
            console.error(
                "Load departments error:",
                err
            );

            setError(
                err.message ||
                "Failed to load departments."
            );

            return [];
        } finally {
            setLoading(false);
        }
    };

    // =========================================================
    // LOAD POSITIONS
    // =========================================================

    const loadPositions = async (department) => {
        if (!department) {
            setPositions([]);
            return [];
        }

        try {
            setPositionsLoading(true);
            setError("");

            const response = await fetch(
                `${API_BASE}/positions/department/${department.departmentId}`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    typeof data === "string"
                        ? data
                        : "Failed to load positions."
                );
            }

            setPositions(data);

            return data;
        } catch (err) {
            console.error(
                "Load positions error:",
                err
            );

            setError(
                err.message ||
                "Failed to load positions."
            );

            setPositions([]);

            return [];
        } finally {
            setPositionsLoading(false);
        }
    };

    // =========================================================
    // LOAD ADMIN REQUESTS
    // =========================================================

    const loadPendingRequests = async () => {
        if (!isAdmin) {
            return [];
        }

        try {
            const response = await fetch(
                `${API_BASE}/department-requests`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    typeof data === "string"
                        ? data
                        : "Failed to load requests."
                );
            }

            setPendingRequests(data);

            return data;
        } catch (err) {
            console.error(
                "Load pending requests error:",
                err
            );

            setError(
                err.message ||
                "Failed to load requests."
            );

            return [];
        }
    };

    // =========================================================
    // INITIAL LOAD
    // =========================================================

    useEffect(() => {
        if (!token) {
            setError(
                "You are not logged in. Please login again."
            );
            return;
        }

        loadDepartments();

        if (isAdmin) {
            loadPendingRequests();
        }
    }, []);

    // =========================================================
    // SELECT DEPARTMENT - VIEW
    // =========================================================

    const selectDepartment = async (department) => {
        setSelectedDepartment(department);

        setSuccessMessage("");
        setError("");

        await loadPositions(department);
    };

    // =========================================================
    // VIEW DEPARTMENT
    // =========================================================

    const viewDepartment = async (department) => {
        await selectDepartment(department);
    };

    // =========================================================
    // VIEW POSITION
    // =========================================================

    const viewPosition = (position) => {
        alert(
            `Position: ${position.positionName}\nDepartment: ${
                selectedDepartment?.departmentName || ""
            }`
        );
    };

    // =========================================================
    // OPEN ADD DEPARTMENT
    // =========================================================

    const openAddDepartment = () => {
        setEditingDepartment(null);
        setDepartmentName("");

        setError("");
        setSuccessMessage("");

        setShowDepartmentModal(true);
    };

    // =========================================================
    // OPEN EDIT DEPARTMENT
    // =========================================================

    const openEditDepartment = (department) => {
        setEditingDepartment(department);

        setDepartmentName(
            department.departmentName
        );

        setError("");
        setSuccessMessage("");

        setShowDepartmentModal(true);
    };

    // =========================================================
    // CLOSE DEPARTMENT MODAL
    // =========================================================

    const closeDepartmentModal = () => {
        setShowDepartmentModal(false);

        setEditingDepartment(null);
        setDepartmentName("");
    };

    // =========================================================
    // SAVE DEPARTMENT - ADMIN
    // =========================================================

    const saveDepartment = async (e) => {
        e.preventDefault();

        if (!departmentName.trim()) {
            alert("Department name is required.");
            return;
        }

        try {
            setError("");
            setSuccessMessage("");

            const url = editingDepartment
                ? `${API_BASE}/departments/${editingDepartment.departmentId}`
                : `${API_BASE}/departments`;

            const method = editingDepartment
                ? "PUT"
                : "POST";

            const response = await fetch(url, {
                method,
                headers: {
                    "Content-Type":
                        "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    departmentName:
                        departmentName.trim(),
                }),
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    typeof data === "string"
                        ? data
                        : "Failed to save department."
                );
            }

            const message = editingDepartment
                ? "Department updated successfully."
                : "Department added successfully.";

            closeDepartmentModal();

            setSuccessMessage(message);

            const freshDepartments =
                await loadDepartments();

            if (selectedDepartment) {
                const updatedSelected =
                    freshDepartments.find(
                        (d) =>
                            d.departmentId ===
                            selectedDepartment.departmentId
                    );

                if (updatedSelected) {
                    setSelectedDepartment(
                        updatedSelected
                    );

                    await loadPositions(
                        updatedSelected
                    );
                }
            }
        } catch (err) {
            console.error(
                "Save department error:",
                err
            );

            alert(
                err.message ||
                "Failed to save department."
            );
        }
    };

    // =========================================================
    // DELETE DEPARTMENT - ADMIN
    // =========================================================

    const deleteDepartment = async (department) => {
        const confirmed = window.confirm(
            `Delete department "${department.departmentName}"?`
        );

        if (!confirmed) {
            return;
        }

        try {
            setError("");
            setSuccessMessage("");

            const response = await fetch(
                `${API_BASE}/departments/${department.departmentId}`,
                {
                    method: "DELETE",
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            const data = await response.text();

            if (!response.ok) {
                throw new Error(
                    data ||
                    "Failed to delete department."
                );
            }

            if (
                selectedDepartment &&
                selectedDepartment.departmentId ===
                department.departmentId
            ) {
                setSelectedDepartment(null);
                setPositions([]);
            }

            setSuccessMessage(
                data ||
                "Department deleted successfully."
            );

            await loadDepartments();
        } catch (err) {
            console.error(
                "Delete department error:",
                err
            );

            alert(
                err.message ||
                "Failed to delete department."
            );
        }
    };

    // =========================================================
    // OPEN ADD POSITION
    // =========================================================

    const openAddPosition = () => {
        if (!selectedDepartment) {
            alert(
                "Please select a department first."
            );
            return;
        }

        setEditingPosition(null);
        setPositionName("");

        setError("");
        setSuccessMessage("");

        setShowPositionModal(true);
    };

    // =========================================================
    // OPEN EDIT POSITION
    // =========================================================

    const openEditPosition = (position) => {
        setEditingPosition(position);

        setPositionName(
            position.positionName
        );

        setError("");
        setSuccessMessage("");

        setShowPositionModal(true);
    };

    // =========================================================
    // CLOSE POSITION MODAL
    // =========================================================

    const closePositionModal = () => {
        setShowPositionModal(false);

        setEditingPosition(null);
        setPositionName("");
    };

    // =========================================================
    // SAVE POSITION - ADMIN
    // =========================================================

    const savePosition = async (e) => {
        e.preventDefault();

        if (!positionName.trim()) {
            alert("Position name is required.");
            return;
        }

        if (
            !editingPosition &&
            !selectedDepartment
        ) {
            alert(
                "Please select a department."
            );
            return;
        }

        try {
            setError("");
            setSuccessMessage("");

            const url = editingPosition
                ? `${API_BASE}/positions/${editingPosition.positionId}`
                : `${API_BASE}/positions/department/${selectedDepartment.departmentId}`;

            const method = editingPosition
                ? "PUT"
                : "POST";

            const response = await fetch(url, {
                method,
                headers: {
                    "Content-Type":
                        "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    positionName:
                        positionName.trim(),
                }),
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    typeof data === "string"
                        ? data
                        : "Failed to save position."
                );
            }

            const message = editingPosition
                ? "Position updated successfully."
                : "Position added successfully.";

            closePositionModal();

            setSuccessMessage(message);

            await loadPositions(
                selectedDepartment
            );
        } catch (err) {
            console.error(
                "Save position error:",
                err
            );

            alert(
                err.message ||
                "Failed to save position."
            );
        }
    };

    // =========================================================
    // DELETE POSITION - ADMIN
    // =========================================================

    const deletePosition = async (position) => {
        const confirmed = window.confirm(
            `Delete position "${position.positionName}"?`
        );

        if (!confirmed) {
            return;
        }

        try {
            setError("");
            setSuccessMessage("");

            const response = await fetch(
                `${API_BASE}/positions/${position.positionId}`,
                {
                    method: "DELETE",
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            // Backend returns plain text
            const data = await response.text();

            if (!response.ok) {
                throw new Error(
                    data ||
                    "Failed to delete position."
                );
            }

            setPositions(
                (prevPositions) =>
                    prevPositions.filter(
                        (p) =>
                            p.positionId !==
                            position.positionId
                    )
            );

            setSuccessMessage(
                data ||
                "Position deleted successfully."
            );
        } catch (err) {
            console.error(
                "Delete position error:",
                err
            );

            alert(
                err.message ||
                "Failed to delete position."
            );
        }
    };

    // =========================================================
    // OPEN STAFF REQUEST MODAL
    // =========================================================

    const openRequestModal = (
        type,
        department = null,
        position = null
    ) => {
        setRequestType(type);

        setRequestDepartment(
            department
        );

        setRequestPosition(position);

        setRequestReason("");

        if (type === "EDIT_DEPARTMENT") {
            setRequestName(
                department?.departmentName ||
                ""
            );
        } else if (
            type === "EDIT_POSITION"
        ) {
            setRequestName(
                position?.positionName ||
                ""
            );
        } else {
            setRequestName("");
        }

        setError("");
        setSuccessMessage("");

        setShowRequestModal(true);
    };

    // =========================================================
    // CLOSE REQUEST MODAL
    // =========================================================

    const closeRequestModal = () => {
        setShowRequestModal(false);

        setRequestType("");
        setRequestDepartment(null);
        setRequestPosition(null);
        setRequestName("");
        setRequestReason("");
    };

    // =========================================================
    // REQUEST DESCRIPTION
    // =========================================================

    const getRequestDescription = () => {
        switch (requestType) {
            case "ADD_DEPARTMENT":
                return "Request to add a new department.";

            case "EDIT_DEPARTMENT":
                return `Request to rename department "${requestDepartment?.departmentName || ""}".`;

            case "ADD_POSITION":
                return `Request to add a position in "${requestDepartment?.departmentName || ""}".`;

            case "EDIT_POSITION":
                return `Request to rename position "${requestPosition?.positionName || ""}".`;

            case "DELETE_POSITION":
                return `Request to delete position "${requestPosition?.positionName || ""}".`;

            default:
                return "";
        }
    };

    // =========================================================
    // SUBMIT STAFF REQUEST
    // =========================================================

    const submitStaffRequest = async (e) => {
        e.preventDefault();

        if (
            requestType !==
            "DELETE_POSITION" &&
            !requestName.trim()
        ) {
            alert(
                "Please enter the requested name."
            );
            return;
        }

        try {
            setError("");
            setSuccessMessage("");

            const body = {
                requestType,
                requestedName:
                    requestName.trim() ||
                    null,
                reason:
                    requestReason.trim() ||
                    null,
                departmentId: null,
                positionId: null,
            };

            // =================================================
            // DEPARTMENT / ADD POSITION
            // =================================================

            if (
                requestType ===
                "EDIT_DEPARTMENT" ||
                requestType ===
                "ADD_POSITION"
            ) {
                if (!requestDepartment) {
                    throw new Error(
                        "Department is required."
                    );
                }

                body.departmentId =
                    requestDepartment.departmentId;
            }

            // =================================================
            // POSITION REQUEST
            // =================================================

            if (
                requestType ===
                "EDIT_POSITION" ||
                requestType ===
                "DELETE_POSITION"
            ) {
                if (!requestPosition) {
                    throw new Error(
                        "Position is required."
                    );
                }

                body.positionId =
                    requestPosition.positionId;
            }

            // =================================================
            // ADD DEPARTMENT
            // =================================================

            if (
                requestType ===
                "ADD_DEPARTMENT"
            ) {
                body.departmentId = null;
                body.positionId = null;
            }

            const response = await fetch(
                `${API_BASE}/department-requests`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type":
                            "application/json",
                        Authorization:
                            `Bearer ${token}`,
                    },
                    body: JSON.stringify(body),
                }
            );

            const data =
                await response.json();

            if (!response.ok) {
                throw new Error(
                    typeof data === "string"
                        ? data
                        : "Failed to submit request."
                );
            }

            closeRequestModal();

            setSuccessMessage(
                "Request sent to ADMIN successfully."
            );
        } catch (err) {
            console.error(
                "Submit request error:",
                err
            );

            alert(
                err.message ||
                "Failed to submit request."
            );
        }
    };

    // =========================================================
    // APPROVE REQUEST
    // =========================================================

    const approveRequest = async (
        request
    ) => {
        const confirmed =
            window.confirm(
                "Approve this request?"
            );

        if (!confirmed) {
            return;
        }

        try {
            setError("");
            setSuccessMessage("");

            const response = await fetch(
                `${API_BASE}/department-requests/${request.requestId}/approve`,
                {
                    method: "PUT",
                    headers: {
                        Authorization:
                            `Bearer ${token}`,
                    },
                }
            );

            const data =
                await response.json();

            if (!response.ok) {
                throw new Error(
                    typeof data === "string"
                        ? data
                        : "Failed to approve request."
                );
            }

            setSuccessMessage(
                "Request approved successfully."
            );

            await loadPendingRequests();

            const freshDepartments =
                await loadDepartments();

            if (selectedDepartment) {
                const updatedSelected =
                    freshDepartments.find(
                        (d) =>
                            d.departmentId ===
                            selectedDepartment.departmentId
                    );

                if (updatedSelected) {
                    setSelectedDepartment(
                        updatedSelected
                    );

                    await loadPositions(
                        updatedSelected
                    );
                } else {
                    setSelectedDepartment(
                        null
                    );

                    setPositions([]);
                }
            }
        } catch (err) {
            console.error(
                "Approve request error:",
                err
            );

            alert(
                err.message ||
                "Failed to approve request."
            );
        }
    };

    // =========================================================
    // REJECT REQUEST
    // =========================================================

    const rejectRequest = async (
        request
    ) => {
        const confirmed =
            window.confirm(
                "Reject this request?"
            );

        if (!confirmed) {
            return;
        }

        try {
            setError("");
            setSuccessMessage("");

            const response = await fetch(
                `${API_BASE}/department-requests/${request.requestId}/reject`,
                {
                    method: "PUT",
                    headers: {
                        Authorization:
                            `Bearer ${token}`,
                    },
                }
            );

            const data =
                await response.json();

            if (!response.ok) {
                throw new Error(
                    typeof data === "string"
                        ? data
                        : "Failed to reject request."
                );
            }

            setSuccessMessage(
                "Request rejected successfully."
            );

            await loadPendingRequests();
        } catch (err) {
            console.error(
                "Reject request error:",
                err
            );

            alert(
                err.message ||
                "Failed to reject request."
            );
        }
    };

    // =========================================================
    // REQUEST TYPE LABEL
    // =========================================================

    const getRequestTypeLabel = (
        type
    ) => {
        switch (type) {
            case "ADD_DEPARTMENT":
                return "Add Department";

            case "EDIT_DEPARTMENT":
                return "Edit Department";

            case "ADD_POSITION":
                return "Add Position";

            case "EDIT_POSITION":
                return "Edit Position";

            case "DELETE_POSITION":
                return "Delete Position";

            default:
                return type;
        }
    };

    // =========================================================
    // RENDER
    // =========================================================

    return (
        <div className="department-page">

            {/* ================================================= */}
            {/* HEADER */}
            {/* ================================================= */}

            <div className="department-header">

                <div>
                    <h1>Department</h1>

                    <p>
                        Manage departments and positions
                    </p>
                </div>

                {/* ADMIN */}

                {isAdmin && (
                    <button
                        className="department-add-btn"
                        onClick={
                            openAddDepartment
                        }
                    >
                        + Add Department
                    </button>
                )}

                {/* STAFF */}

                {isStaff && (
                    <button
                        className="department-request-btn"
                        onClick={() =>
                            openRequestModal(
                                "ADD_DEPARTMENT"
                            )
                        }
                    >
                        + Request Department
                    </button>
                )}

            </div>

            {/* ================================================= */}
            {/* ERROR */}
            {/* ================================================= */}

            {error && (
                <div className="department-error">
                    {error}
                </div>
            )}

            {/* ================================================= */}
            {/* SUCCESS */}
            {/* ================================================= */}

            {successMessage && (
                <div className="department-success">
                    {successMessage}
                </div>
            )}

            {/* ================================================= */}
            {/* ADMIN REQUEST PANEL */}
            {/* ================================================= */}

            {isAdmin &&
                pendingRequests.length > 0 && (
                    <div className="request-panel">

                        <div className="request-panel-header">

                            <div>
                                <h2>
                                    Pending Requests
                                </h2>

                                <p>
                                    Staff requests waiting
                                    for your approval
                                </p>
                            </div>

                            <span className="request-count">
                                {
                                    pendingRequests.length
                                }
                            </span>

                        </div>

                        <div className="request-list">

                            {pendingRequests.map(
                                (request) => (
                                    <div
                                        className="request-card"
                                        key={
                                            request.requestId
                                        }
                                    >

                                        <div className="request-card-info">

                                            <h3>
                                                {getRequestTypeLabel(
                                                    request.requestType
                                                )}
                                            </h3>

                                            <p>
                                                <strong>
                                                    Requested by:
                                                </strong>{" "}
                                                {
                                                    request.requestedBy
                                                }
                                            </p>

                                            {request.departmentName && (
                                                <p>
                                                    <strong>
                                                        Department:
                                                    </strong>{" "}
                                                    {
                                                        request.departmentName
                                                    }
                                                </p>
                                            )}

                                            {request.positionName && (
                                                <p>
                                                    <strong>
                                                        Position:
                                                    </strong>{" "}
                                                    {
                                                        request.positionName
                                                    }
                                                </p>
                                            )}

                                            {request.oldName && (
                                                <p>
                                                    <strong>
                                                        Current:
                                                    </strong>{" "}
                                                    {
                                                        request.oldName
                                                    }
                                                </p>
                                            )}

                                            {request.requestedName && (
                                                <p>
                                                    <strong>
                                                        Requested:
                                                    </strong>{" "}
                                                    {
                                                        request.requestedName
                                                    }
                                                </p>
                                            )}

                                            {request.reason && (
                                                <p>
                                                    <strong>
                                                        Reason:
                                                    </strong>{" "}
                                                    {
                                                        request.reason
                                                    }
                                                </p>
                                            )}

                                        </div>

                                        <div className="request-actions">

                                            <button
                                                className="approve-btn"
                                                onClick={() =>
                                                    approveRequest(
                                                        request
                                                    )
                                                }
                                            >
                                                Accept
                                            </button>

                                            <button
                                                className="reject-btn"
                                                onClick={() =>
                                                    rejectRequest(
                                                        request
                                                    )
                                                }
                                            >
                                                Reject
                                            </button>

                                        </div>

                                    </div>
                                )
                            )}

                        </div>
                    </div>
                )}

            {/* ================================================= */}
            {/* MAIN CONTENT */}
            {/* ================================================= */}

            <div className="department-content">

                {/* ================================================= */}
                {/* DEPARTMENT LIST */}
                {/* ================================================= */}

                <div className="department-list-card">

                    <div className="section-title">

                        <h2>
                            Departments
                        </h2>

                    </div>

                    {loading ? (
                        <div className="empty-state">
                            <p>
                                Loading departments...
                            </p>
                        </div>
                    ) : departments.length === 0 ? (
                        <div className="empty-state">

                            <p>
                                No departments found.
                            </p>

                            {isAdmin && (
                                <button
                                    onClick={
                                        openAddDepartment
                                    }
                                >
                                    Add Department
                                </button>
                            )}

                            {isStaff && (
                                <button
                                    onClick={() =>
                                        openRequestModal(
                                            "ADD_DEPARTMENT"
                                        )
                                    }
                                >
                                    Request Department
                                </button>
                            )}

                        </div>
                    ) : (
                        <div className="department-list">

                            {departments.map(
                                (department) => {

                                    const selected =
                                        selectedDepartment &&
                                        selectedDepartment.departmentId ===
                                        department.departmentId;

                                    return (
                                        <div
                                            key={
                                                department.departmentId
                                            }
                                            className={`department-item ${
                                                selected
                                                    ? "selected"
                                                    : ""
                                            }`}
                                            onClick={() =>
                                                selectDepartment(
                                                    department
                                                )
                                            }
                                        >

                                            <div className="department-item-name">

                                                <span>
                                                    {
                                                        department.departmentName
                                                    }
                                                </span>

                                            </div>

                                            {/* ================================================= */}
                                            {/* DEPARTMENT ACTIONS */}
                                            {/* ================================================= */}

                                            <div className="department-item-actions">

                                                {/* VIEW */}
                                                <button
                                                    className="view-btn"
                                                    onClick={(e) => {
                                                        e.stopPropagation();

                                                        viewDepartment(
                                                            department
                                                        );
                                                    }}
                                                >
                                                    View
                                                </button>

                                                {/* ADMIN */}
                                                {isAdmin && (
                                                    <>
                                                        <button
                                                            className="edit-btn"
                                                            onClick={(e) => {
                                                                e.stopPropagation();

                                                                openEditDepartment(
                                                                    department
                                                                );
                                                            }}
                                                        >
                                                            Edit
                                                        </button>

                                                        <button
                                                            className="delete-btn"
                                                            onClick={(e) => {
                                                                e.stopPropagation();

                                                                deleteDepartment(
                                                                    department
                                                                );
                                                            }}
                                                        >
                                                            Delete
                                                        </button>
                                                    </>
                                                )}

                                                {/* STAFF */}
                                                {isStaff && (
                                                    <button
                                                        className="request-small-btn"
                                                        onClick={(e) => {
                                                            e.stopPropagation();

                                                            openRequestModal(
                                                                "EDIT_DEPARTMENT",
                                                                department
                                                            );
                                                        }}
                                                    >
                                                        Request Edit
                                                    </button>
                                                )}

                                            </div>

                                        </div>
                                    );
                                }
                            )}

                        </div>
                    )}

                </div>

                {/* ================================================= */}
                {/* POSITION LIST */}
                {/* ================================================= */}

                <div className="position-list-card">

                    <div className="position-header">

                        <div>

                            <h2>
                                Positions
                            </h2>

                            {selectedDepartment && (
                                <p>
                                    {
                                        selectedDepartment.departmentName
                                    }
                                </p>
                            )}

                        </div>

                        {/* ADMIN ADD POSITION */}

                        {isAdmin &&
                            selectedDepartment && (
                                <button
                                    className="position-add-btn"
                                    onClick={
                                        openAddPosition
                                    }
                                >
                                    + Add Position
                                </button>
                            )}

                        {/* STAFF ADD POSITION */}

                        {isStaff &&
                            selectedDepartment && (
                                <button
                                    className="position-request-btn"
                                    onClick={() =>
                                        openRequestModal(
                                            "ADD_POSITION",
                                            selectedDepartment
                                        )
                                    }
                                >
                                    + Request Position
                                </button>
                            )}

                    </div>

                    {/* ================================================= */}
                    {/* NO DEPARTMENT SELECTED */}
                    {/* ================================================= */}

                    {!selectedDepartment ? (
                        <div className="empty-state">

                            <p>
                                Select a department to
                                view positions.
                            </p>

                        </div>

                    ) : positionsLoading ? (

                        <div className="empty-state">

                            <p>
                                Loading positions...
                            </p>

                        </div>

                    ) : positions.length === 0 ? (

                        <div className="empty-state">

                            <p>
                                No positions found in this
                                department.
                            </p>

                            {isAdmin && (
                                <button
                                    onClick={
                                        openAddPosition
                                    }
                                >
                                    Add Position
                                </button>
                            )}

                            {isStaff && (
                                <button
                                    onClick={() =>
                                        openRequestModal(
                                            "ADD_POSITION",
                                            selectedDepartment
                                        )
                                    }
                                >
                                    Request Position
                                </button>
                            )}

                        </div>

                    ) : (

                        <div className="position-list">

                            {positions.map(
                                (position) => (
                                    <div
                                        className="position-item"
                                        key={
                                            position.positionId
                                        }
                                    >

                                        <div className="position-name">
                                            {
                                                position.positionName
                                            }
                                        </div>

                                        {/* ================================================= */}
                                        {/* POSITION ACTIONS */}
                                        {/* ================================================= */}

                                        <div className="position-actions">

                                            {/* VIEW */}
                                            <button
                                                className="view-btn"
                                                onClick={() =>
                                                    viewPosition(
                                                        position
                                                    )
                                                }
                                            >
                                                View
                                            </button>

                                            {/* ADMIN */}
                                            {isAdmin && (
                                                <>
                                                    <button
                                                        className="edit-btn"
                                                        onClick={() =>
                                                            openEditPosition(
                                                                position
                                                            )
                                                        }
                                                    >
                                                        Edit
                                                    </button>

                                                    <button
                                                        className="delete-btn"
                                                        onClick={() =>
                                                            deletePosition(
                                                                position
                                                            )
                                                        }
                                                    >
                                                        Delete
                                                    </button>
                                                </>
                                            )}

                                            {/* STAFF */}
                                            {isStaff && (
                                                <>
                                                    <button
                                                        className="request-small-btn"
                                                        onClick={() =>
                                                            openRequestModal(
                                                                "EDIT_POSITION",
                                                                selectedDepartment,
                                                                position
                                                            )
                                                        }
                                                    >
                                                        Request Edit
                                                    </button>

                                                    <button
                                                        className="delete-request-btn"
                                                        onClick={() =>
                                                            openRequestModal(
                                                                "DELETE_POSITION",
                                                                selectedDepartment,
                                                                position
                                                            )
                                                        }
                                                    >
                                                        Request Delete
                                                    </button>
                                                </>
                                            )}

                                        </div>

                                    </div>
                                )
                            )}

                        </div>

                    )}

                </div>

            </div>

            {/* ========================================================= */}
            {/* ADMIN DEPARTMENT MODAL */}
            {/* ========================================================= */}

            {showDepartmentModal && (
                <div className="modal-overlay">

                    <div className="modal-box">

                        <div className="modal-header">

                            <h2>
                                {editingDepartment
                                    ? "Edit Department"
                                    : "Add Department"}
                            </h2>

                            <button
                                className="modal-close"
                                onClick={
                                    closeDepartmentModal
                                }
                            >
                                ×
                            </button>

                        </div>

                        <form
                            onSubmit={
                                saveDepartment
                            }
                        >

                            <div className="form-group">

                                <label>
                                    Department Name
                                </label>

                                <input
                                    type="text"
                                    value={
                                        departmentName
                                    }
                                    onChange={(e) =>
                                        setDepartmentName(
                                            e.target.value
                                        )
                                    }
                                    placeholder="Enter department name"
                                    autoFocus
                                />

                            </div>

                            <div className="modal-actions">

                                <button
                                    type="button"
                                    className="cancel-btn"
                                    onClick={
                                        closeDepartmentModal
                                    }
                                >
                                    Cancel
                                </button>

                                <button
                                    type="submit"
                                    className="save-btn"
                                >
                                    {editingDepartment
                                        ? "Update"
                                        : "Add"}
                                </button>

                            </div>

                        </form>

                    </div>

                </div>
            )}

            {/* ========================================================= */}
            {/* ADMIN POSITION MODAL */}
            {/* ========================================================= */}

            {showPositionModal && (
                <div className="modal-overlay">

                    <div className="modal-box">

                        <div className="modal-header">

                            <h2>
                                {editingPosition
                                    ? "Edit Position"
                                    : "Add Position"}
                            </h2>

                            <button
                                className="modal-close"
                                onClick={
                                    closePositionModal
                                }
                            >
                                ×
                            </button>

                        </div>

                        <form
                            onSubmit={
                                savePosition
                            }
                        >

                            <div className="form-group">

                                <label>
                                    Position Name
                                </label>

                                <input
                                    type="text"
                                    value={
                                        positionName
                                    }
                                    onChange={(e) =>
                                        setPositionName(
                                            e.target.value
                                        )
                                    }
                                    placeholder="Enter position name"
                                    autoFocus
                                />

                            </div>

                            <div className="modal-actions">

                                <button
                                    type="button"
                                    className="cancel-btn"
                                    onClick={
                                        closePositionModal
                                    }
                                >
                                    Cancel
                                </button>

                                <button
                                    type="submit"
                                    className="save-btn"
                                >
                                    {editingPosition
                                        ? "Update"
                                        : "Add"}
                                </button>

                            </div>

                        </form>

                    </div>

                </div>
            )}

            {/* ========================================================= */}
            {/* STAFF REQUEST MODAL */}
            {/* ========================================================= */}

            {showRequestModal && (
                <div className="modal-overlay">

                    <div className="modal-box request-modal">

                        <div className="modal-header">

                            <h2>
                                Send Request
                            </h2>

                            <button
                                className="modal-close"
                                onClick={
                                    closeRequestModal
                                }
                            >
                                ×
                            </button>

                        </div>

                        <div className="request-description">
                            {getRequestDescription()}
                        </div>

                        <form
                            onSubmit={
                                submitStaffRequest
                            }
                        >

                            {/* NAME */}

                            {requestType !==
                                "DELETE_POSITION" && (
                                    <div className="form-group">

                                        <label>
                                            Requested Name
                                        </label>

                                        <input
                                            type="text"
                                            value={
                                                requestName
                                            }
                                            onChange={(e) =>
                                                setRequestName(
                                                    e.target.value
                                                )
                                            }
                                            placeholder="Enter requested name"
                                            autoFocus
                                        />

                                    </div>
                                )}

                            {/* REASON */}

                            <div className="form-group">

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
                                    placeholder="Enter reason (optional)"
                                    rows="4"
                                />

                            </div>

                            {/* ACTIONS */}

                            <div className="modal-actions">

                                <button
                                    type="button"
                                    className="cancel-btn"
                                    onClick={
                                        closeRequestModal
                                    }
                                >
                                    Cancel
                                </button>

                                <button
                                    type="submit"
                                    className="save-btn"
                                >
                                    Send Request
                                </button>

                            </div>

                        </form>

                    </div>

                </div>
            )}

        </div>
    );
}

export default Department;