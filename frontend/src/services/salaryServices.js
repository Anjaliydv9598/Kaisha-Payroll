const API_BASE_URL = "http://localhost:8080/api";

const getToken = () => localStorage.getItem("token");

const getHeaders = () => ({
    "Content-Type": "application/json",
    Accept: "application/json",
    Authorization: `Bearer ${getToken()}`
});

const handleResponse = async (response) => {
    const text = await response.text();

    let data = null;

    try {
        data = text ? JSON.parse(text) : null;
    } catch {
        data = text;
    }

    if (!response.ok) {
        throw new Error(
            data?.message ||
            data?.error ||
            `Request failed with status ${response.status}`
        );
    }

    return data;
};


// =====================================================
// GET ALL SALARY RECORDS
// =====================================================

export const getAllSalaryRecords = async () => {
    const response = await fetch(`${API_BASE_URL}/salary`, {
        method: "GET",
        headers: getHeaders()
    });

    return handleResponse(response);
};


// =====================================================
// GET SALARY BY ID
// =====================================================

export const getSalaryById = async (salaryId) => {
    const response = await fetch(
        `${API_BASE_URL}/salary/${salaryId}`,
        {
            method: "GET",
            headers: getHeaders()
        }
    );

    return handleResponse(response);
};


// =====================================================
// GET SALARY BY EMPLOYEE
// =====================================================

export const getSalaryByEmployee = async (employeeId) => {
    const response = await fetch(
        `${API_BASE_URL}/salary/employee/${encodeURIComponent(employeeId)}`,
        {
            method: "GET",
            headers: getHeaders()
        }
    );

    return handleResponse(response);
};


// =====================================================
// GET SALARY BY PERIOD
// =====================================================

export const getSalaryByPeriod = async (payPeriod) => {
    const response = await fetch(
        `${API_BASE_URL}/salary/period/${encodeURIComponent(payPeriod)}`,
        {
            method: "GET",
            headers: getHeaders()
        }
    );

    return handleResponse(response);
};


// =====================================================
// CREATE SALARY
// =====================================================

export const createSalary = async (salaryData) => {
    const response = await fetch(`${API_BASE_URL}/salary`, {
        method: "POST",
        headers: getHeaders(),
        body: JSON.stringify(salaryData)
    });

    return handleResponse(response);
};


// =====================================================
// UPDATE SALARY
// =====================================================

export const updateSalary = async (salaryId, salaryData) => {
    const response = await fetch(
        `${API_BASE_URL}/salary/${salaryId}`,
        {
            method: "PUT",
            headers: getHeaders(),
            body: JSON.stringify(salaryData)
        }
    );

    return handleResponse(response);
};


// =====================================================
// DELETE SALARY
// =====================================================

export const deleteSalary = async (salaryId) => {
    const response = await fetch(
        `${API_BASE_URL}/salary/${salaryId}`,
        {
            method: "DELETE",
            headers: getHeaders()
        }
    );

    return handleResponse(response);
};

// =====================================================
// DOWNLOAD SALARY / PAYROLL
// =====================================================

export const downloadSalary = async ({
                                         payPeriod,
                                         scope,
                                         employeeIds = [],
                                         fromRecord = null,
                                         toRecord = null,
                                         format
                                     }) => {

    const params = new URLSearchParams();

    params.append("payPeriod", payPeriod);
    params.append("scope", scope);
    params.append("format", format);

    if (employeeIds.length > 0) {
        employeeIds.forEach((employeeId) => {
            params.append("employeeIds", employeeId);
        });
    }

    if (fromRecord !== null && fromRecord !== "") {
        params.append("fromRecord", fromRecord);
    }

    if (toRecord !== null && toRecord !== "") {
        params.append("toRecord", toRecord);
    }

    const response = await fetch(
        `${API_BASE_URL}/payroll/download?${params.toString()}`,
        {
            method: "POST",
            headers: {
                Authorization: `Bearer ${getToken()}`
            }
        }
    );

    if (!response.ok) {

        const text = await response.text();

        let message = "Unable to download salary.";

        try {
            const data = text
                ? JSON.parse(text)
                : null;

            message =
                data?.message ||
                data?.error ||
                message;

        } catch {
            if (text) {
                message = text;
            }
        }

        throw new Error(message);
    }

    const blob = await response.blob();

    let extension = "pdf";

    if (format === "EXCEL") {
        extension = "xlsx";
    }

    if (format === "CSV") {
        extension = "csv";
    }

    if (format === "ZIP") {
        extension = "zip";
    }

    const fileName =
        `Payroll_${payPeriod}.${extension}`;

    const url =
        window.URL.createObjectURL(blob);

    const link =
        document.createElement("a");

    link.href = url;
    link.download = fileName;

    document.body.appendChild(link);

    link.click();

    link.remove();

    window.URL.revokeObjectURL(url);
};