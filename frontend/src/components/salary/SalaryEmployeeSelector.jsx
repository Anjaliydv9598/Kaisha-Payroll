import React from "react";

function SalaryEmployeeSelector({
                                    employees = [],
                                    employeeId,
                                    onChange,
                                    disabled = false
                                }) {

    const getEmployeeId = (employee) => {
        return (
            employee.employeeId ||
            employee.empId ||
            employee.id ||
            employee.employeeID ||
            ""
        );
    };

    const getEmployeeName = (employee) => {
        return (
            employee.name ||
            employee.employeeName ||
            employee.fullName ||
            `${employee.firstName || ""} ${employee.lastName || ""}`.trim() ||
            "Unnamed Employee"
        );
    };

    const getDepartment = (employee) => {
        if (employee.department) {
            return employee.department;
        }

        if (Array.isArray(employee.fields)) {
            const departmentField = employee.fields.find(
                (field) =>
                    String(field.fieldName || "").toLowerCase() ===
                    "department"
            );

            return departmentField?.fieldValue || "";
        }

        return "";
    };

    return (
        <div className="salary-employee-selector">

            <label htmlFor="salaryEmployee">
                Employee
            </label>

            <select
                id="salaryEmployee"
                value={employeeId}
                onChange={(e) => onChange(e.target.value)}
                disabled={disabled}
            >
                <option value="">
                    Select employee
                </option>

                {employees.map((employee) => {
                    const id = getEmployeeId(employee);
                    const name = getEmployeeName(employee);
                    const department = getDepartment(employee);

                    return (
                        <option
                            key={id}
                            value={id}
                        >
                            {id} - {name}
                            {department
                                ? ` - ${department}`
                                : ""}
                        </option>
                    );
                })}
            </select>

        </div>
    );
}

export default SalaryEmployeeSelector;