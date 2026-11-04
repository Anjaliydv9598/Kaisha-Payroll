
const API_URL =
    import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

async function request(endpoint, options = {}) {
    const token = localStorage.getItem("token");

    if (!token) {
        throw new Error("Your session has expired. Please log in again.");
    }

    const response = await fetch(`${API_URL}${endpoint}`, {
        ...options,
        headers: {
            Authorization: `Bearer ${token}`,
            ...(options.body ? { "Content-Type": "application/json" } : {}),
            ...options.headers,
        },
    });

    const responseText = await response.text();
    let data = null;

    if (responseText) {
        try {
            data = JSON.parse(responseText);
        } catch {
            data = responseText;
        }
    }

    if (!response.ok) {
        const message =
            typeof data === "object" && data !== null
                ? data.message || data.error || "Request failed."
                : data || "Request failed.";

        throw new Error(message);
    }

    return data;
}

export const staffApi = {
    getProfile: () => request("/me"),

    // Work Journal
    getWorkEntries: () => request("/work-journal/entries"),

    getHighlights: () => request("/work-journal/highlights"),

    createWorkEntry: (entry) =>
        request("/work-journal/entries", {
            method: "POST",
            body: JSON.stringify(entry),
        }),

    updateWorkEntry: (id, entry) =>
        request(`/work-journal/entries/${id}`, {
            method: "PUT",
            body: JSON.stringify(entry),
        }),

    deleteWorkEntry: (id) =>
        request(`/work-journal/entries/${id}`, {
            method: "DELETE",
        }),

    // Staff Attendance
    getMyAttendance: () =>
        request("/attendance/staff/my-attendance"),

    punchIn: () =>
        request("/attendance/staff/punch-in", {
            method: "POST",
        }),

    punchOut: () =>
        request("/attendance/staff/punch-out", {
            method: "POST",
        }),
};
