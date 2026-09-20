import React from "react";

function SalaryTable({
                         records = [],
                         employees = [],
                         searchTerm = "",
                         onEdit,
                         onDelete
                     }) {

    const getEmployee = (employeeId) => {
        return employees.find(
            (employee) =>
                String(
                    employee.employeeId ||
                    employee.empId ||
                    employee.id ||
                    ""
                ) === String(employeeId)
        );
    };

    const getEmployeeName = (employeeId) => {
        const employee = getEmployee(employeeId);

        if (!employee) {
            return employeeId || "Unknown";
        }

        return (
            employee.name ||
            employee.employeeName ||
            employee.fullName ||
            `${employee.firstName || ""} ${employee.lastName || ""}`.trim() ||
            employeeId
        );
    };

    const getDepartment = (employeeId) => {
        const employee = getEmployee(employeeId);

        if (!employee) {
            return "—";
        }

        if (employee.department) {
            return employee.department;
        }

        if (Array.isArray(employee.fields)) {
            const field = employee.fields.find(
                (item) =>
                    String(item.fieldName || "").toLowerCase() ===
                    "department"
            );

            return field?.fieldValue || "—";
        }

        return "—";
    };


    const getBasicSalary = (record) => {

        const component = record.components?.find(
            (item) =>
                String(item.componentName || "")
                    .toLowerCase()
                    .trim() === "basic salary"
        );

        return Number(component?.amount || 0);
    };


    const getAllowances = (record) => {

        return (record.components || [])
            .filter(
                (item) =>
                    item.componentType === "EARNING" &&
                    String(item.componentName || "")
                        .toLowerCase()
                        .trim() !== "basic salary"
            )
            .reduce(
                (sum, item) =>
                    sum + Number(item.amount || 0),
                0
            );
    };


    const getDeductions = (record) => {

        return (record.components || [])
            .filter(
                (item) =>
                    item.componentType === "DEDUCTION"
            )
            .reduce(
                (sum, item) =>
                    sum + Number(item.amount || 0),
                0
            );
    };


    const filteredRecords = records.filter((record) => {

        const employeeId =
            String(record.employeeId || "").toLowerCase();

        const employeeName =
            getEmployeeName(record.employeeId)
                .toLowerCase();

        const department =
            getDepartment(record.employeeId)
                .toLowerCase();

        const search =
            searchTerm.toLowerCase().trim();

        if (!search) {
            return true;
        }

        return (
            employeeId.includes(search) ||
            employeeName.includes(search) ||
            department.includes(search)
        );
    });


    if (records.length === 0) {

        return (
            <div className="salary-empty-state">

                <div className="empty-icon">
                    ₹
                </div>

                <h3>
                    No salary records found
                </h3>

                <p>
                    Create a salary record to see it here.
                </p>

            </div>
        );
    }


    return (
        <div className="salary-table-wrapper">

            <table className="salary-table">

                <thead>
                <tr>
                    <th>EMPLOYEE</th>
                    <th>DEPARTMENT</th>
                    <th>BASIC SALARY</th>
                    <th>ALLOWANCES</th>
                    <th>DEDUCTIONS</th>
                    <th>NET SALARY</th>
                    <th>ACTIONS</th>
                </tr>
                </thead>

                <tbody>

                {filteredRecords.map((record) => {

                    const basic =
                        getBasicSalary(record);

                    const allowances =
                        getAllowances(record);

                    const deductions =
                        getDeductions(record);

                    const net =
                        Number(record.netSalary || 0);

                    return (
                        <tr key={record.salaryId}>

                            <td>

                                <div className="employee-cell">

                                    <div className="employee-avatar">
                                        {getEmployeeName(
                                            record.employeeId
                                        )
                                            .charAt(0)
                                            .toUpperCase()}
                                    </div>

                                    <div>

                                        <strong>
                                            {getEmployeeName(
                                                record.employeeId
                                            )}
                                        </strong>

                                        <span>
                                                {record.employeeId}
                                            </span>

                                    </div>

                                </div>

                            </td>


                            <td>
                                {getDepartment(
                                    record.employeeId
                                )}
                            </td>


                            <td>
                                ₹{basic.toLocaleString("en-IN", {
                                maximumFractionDigits: 2
                            })}
                            </td>


                            <td>
                                ₹{allowances.toLocaleString("en-IN", {
                                maximumFractionDigits: 2
                            })}
                            </td>


                            <td>
                                ₹{deductions.toLocaleString("en-IN", {
                                maximumFractionDigits: 2
                            })}
                            </td>


                            <td>
                                <strong className="net-salary">
                                    ₹{net.toLocaleString("en-IN", {
                                    maximumFractionDigits: 2
                                })}
                                </strong>
                            </td>


                            <td>

                                <div className="table-actions">

                                    <button
                                        type="button"
                                        className="edit-action"
                                        onClick={() =>
                                            onEdit(record)
                                        }
                                        title="Edit salary"
                                    >
                                        ✎
                                    </button>

                                    <button
                                        type="button"
                                        className="delete-action"
                                        onClick={() =>
                                            onDelete(record)
                                        }
                                        title="Delete salary"
                                    >
                                        🗑
                                    </button>

                                </div>

                            </td>

                        </tr>
                    );
                })}

                </tbody>

            </table>


            {filteredRecords.length === 0 && (
                <div className="salary-no-search-result">
                    No employees match your search.
                </div>
            )}

        </div>
    );
}

export default SalaryTable;