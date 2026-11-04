import React, { useState } from "react";
import { staffApi } from "../../services/staffApi";

const initialForm = {
    workDate: new Date().toISOString().slice(0, 10),
    workDescription: "",
    highlight: false,
    highlightCategory: "",
};

function WorkJournal({ entries, onRefresh }) {
    const [form, setForm] = useState(initialForm);
    const [editingId, setEditingId] = useState(null);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    const handleChange = (event) => {
        const { name, value, type, checked } = event.target;

        setForm((previous) => ({
            ...previous,
            [name]: type === "checkbox" ? checked : value,
        }));
    };

    const resetForm = () => {
        setForm(initialForm);
        setEditingId(null);
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        setError("");
        setSuccess("");

        if (!form.workDescription.trim()) {
            setError("Please enter your work description.");
            return;
        }

        try {
            setSaving(true);

            const payload = {
                ...form,
                workDescription: form.workDescription.trim(),
                highlightCategory: form.highlight
                    ? form.highlightCategory.trim()
                    : null,
            };

            if (editingId) {
                await staffApi.updateWorkEntry(editingId, payload);
                setSuccess("Work entry updated successfully.");
            } else {
                await staffApi.createWorkEntry(payload);
                setSuccess("Work entry saved successfully.");
            }

            resetForm();
            await onRefresh();
        } catch (err) {
            setError(err.message);
        } finally {
            setSaving(false);
        }
    };

    const handleEdit = (entry) => {
        setEditingId(entry.id);

        setForm({
            workDate: entry.workDate,
            workDescription: entry.workDescription,
            highlight: entry.highlight,
            highlightCategory: entry.highlightCategory || "",
        });

        setError("");
        setSuccess("");
        window.scrollTo({ top: 0, behavior: "smooth" });
    };

    const handleDelete = async (id) => {
        if (!window.confirm("Are you sure you want to delete this entry?")) {
            return;
        }

        try {
            setError("");
            await staffApi.deleteWorkEntry(id);
            setSuccess("Work entry deleted successfully.");
            await onRefresh();
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <div className="staff-content">
            <header className="staff-page-header">
                <div>
                    <h1>My Work Journal</h1>
                    <p>Record your daily activities and achievements.</p>
                </div>
            </header>

            {error && <div className="staff-alert error">{error}</div>}
            {success && <div className="staff-alert success">{success}</div>}

            <section className="staff-section">
                <h2>{editingId ? "Edit Work Entry" : "Add Daily Work Entry"}</h2>

                <form className="staff-journal-form" onSubmit={handleSubmit}>
                    <div className="staff-form-field">
                        <label>Work Date</label>
                        <input
                            type="date"
                            name="workDate"
                            value={form.workDate}
                            max={new Date().toISOString().slice(0, 10)}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="staff-form-field">
                        <label>Work Description</label>
                        <textarea
                            name="workDescription"
                            value={form.workDescription}
                            onChange={handleChange}
                            placeholder="Describe the tasks you completed today..."
                            maxLength={5000}
                            rows={5}
                            required
                        />
                        <small>
                            {form.workDescription.length}/5000 characters
                        </small>
                    </div>

                    <label className="staff-checkbox">
                        <input
                            type="checkbox"
                            name="highlight"
                            checked={form.highlight}
                            onChange={handleChange}
                        />
                        Mark this entry as a highlight
                    </label>

                    {form.highlight && (
                        <div className="staff-form-field">
                            <label>Highlight Category</label>
                            <input
                                type="text"
                                name="highlightCategory"
                                value={form.highlightCategory}
                                onChange={handleChange}
                                placeholder="Example: Achievement, Project, Training"
                                maxLength={100}
                            />
                        </div>
                    )}

                    <div className="staff-form-actions">
                        {editingId && (
                            <button
                                type="button"
                                className="staff-secondary-button"
                                onClick={resetForm}
                            >
                                Cancel Edit
                            </button>
                        )}

                        <button
                            type="submit"
                            className="staff-primary-button"
                            disabled={saving}
                        >
                            {saving
                                ? "Saving..."
                                : editingId
                                    ? "Update Entry"
                                    : "Save Entry"}
                        </button>
                    </div>
                </form>
            </section>

            <section className="staff-section">
                <h2>My Work Records ({entries.length})</h2>

                {entries.length === 0 ? (
                    <div className="staff-empty">
                        You haven't added any work records yet.
                    </div>
                ) : (
                    <div className="staff-entry-list">
                        {entries.map((entry) => (
                            <article key={entry.id} className="staff-entry">
                                <div className="staff-entry-body">
                                    <div className="staff-entry-top">
                                        <strong>{entry.workDate}</strong>

                                        {entry.highlight && (
                                            <span className="staff-highlight-tag">
                                                ★ {entry.highlightCategory || "Highlight"}
                                            </span>
                                        )}
                                    </div>

                                    <p>{entry.workDescription}</p>

                                    <small>
                                        Saved:{" "}
                                        {entry.createdAt
                                            ? new Date(entry.createdAt).toLocaleString()
                                            : "Not available"}
                                    </small>
                                </div>

                                <div className="staff-entry-actions">
                                    <button
                                        onClick={() => handleEdit(entry)}
                                    >
                                        Edit
                                    </button>

                                    <button
                                        className="danger"
                                        onClick={() => handleDelete(entry.id)}
                                    >
                                        Delete
                                    </button>
                                </div>
                            </article>
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
}

export default WorkJournal;