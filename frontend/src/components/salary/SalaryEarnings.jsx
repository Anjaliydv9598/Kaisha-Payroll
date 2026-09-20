import React from "react";

function SalaryEarnings({
                            components,
                            onChange,
                            onAdd,
                            onDelete,
                            disabled = false
                        }) {

    const earnings = components.filter(
        (component) =>
            component.componentType === "EARNING"
    );

    return (
        <div className="salary-component-section">

            <div className="salary-component-header">

                <div>
                    <h3>Earnings</h3>
                    <p>
                        Salary and allowance components
                    </p>
                </div>

                <button
                    type="button"
                    className="component-add-button"
                    onClick={onAdd}
                    disabled={disabled}
                >
                    + Add
                </button>

            </div>

            <div className="salary-component-list">

                {earnings.length === 0 && (
                    <div className="empty-component">
                        No earning components added.
                    </div>
                )}

                {earnings.map((component) => (

                    <div
                        className="salary-component-row"
                        key={component.tempId || component.id}
                    >

                        <div className="component-name-area">

                            <input
                                type="text"
                                value={component.componentName}
                                onChange={(e) =>
                                    onChange(
                                        component,
                                        "componentName",
                                        e.target.value
                                    )
                                }
                                placeholder="Component name"
                                disabled={
                                    disabled ||
                                    component.systemDefined
                                }
                            />

                            {component.systemDefined && (
                                <span className="system-badge">
                                    System
                                </span>
                            )}

                        </div>

                        <div className="component-amount-area">

                            <span>₹</span>

                            <input
                                type="number"
                                min="0"
                                step="0.01"
                                value={component.amount}
                                onChange={(e) =>
                                    onChange(
                                        component,
                                        "amount",
                                        e.target.value
                                    )
                                }
                                placeholder="0.00"
                                disabled={disabled}
                            />

                        </div>

                        <button
                            type="button"
                            className="component-delete-button"
                            onClick={() => onDelete(component)}
                            disabled={disabled}
                            title="Delete"
                        >
                            ×
                        </button>

                    </div>

                ))}

            </div>

        </div>
    );
}

export default SalaryEarnings;