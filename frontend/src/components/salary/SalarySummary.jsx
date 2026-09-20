import React from "react";

function SalarySummary({ records = [] }) {

    const totalPayroll = records.reduce(
        (sum, record) =>
            sum + Number(record.netSalary || 0),
        0
    );

    const averageSalary =
        records.length > 0
            ? totalPayroll / records.length
            : 0;

    return (
        <div className="salary-summary-grid">

            <div className="salary-summary-card">

                <div className="summary-icon employee-icon">
                    👥
                </div>

                <div>
                    <span className="summary-label">
                        Total Employees
                    </span>

                    <strong>
                        {records.length}
                    </strong>

                    <small>
                        Salary records
                    </small>
                </div>

            </div>


            <div className="salary-summary-card">

                <div className="summary-icon payroll-icon">
                    ₹
                </div>

                <div>
                    <span className="summary-label">
                        Total Payroll
                    </span>

                    <strong>
                        ₹{totalPayroll.toLocaleString("en-IN", {
                        maximumFractionDigits: 2
                    })}
                    </strong>

                    <small>
                        Current records
                    </small>
                </div>

            </div>


            <div className="salary-summary-card">

                <div className="summary-icon average-icon">
                    ▣
                </div>

                <div>
                    <span className="summary-label">
                        Average Salary
                    </span>

                    <strong>
                        ₹{averageSalary.toLocaleString("en-IN", {
                        maximumFractionDigits: 2
                    })}
                    </strong>

                    <small>
                        Per employee
                    </small>
                </div>

            </div>


            <div className="salary-summary-card">

                <div className="summary-icon period-icon">
                    📅
                </div>

                <div>
                    <span className="summary-label">
                        Salary Period
                    </span>

                    <strong>
                        {records.length > 0
                            ? records[0].payPeriod || "—"
                            : "—"}
                    </strong>

                    <small>
                        Current period
                    </small>
                </div>

            </div>

        </div>
    );
}

export default SalarySummary;