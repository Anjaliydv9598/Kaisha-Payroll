import React, { useEffect, useMemo, useState } from "react";
import "./Calendar.css";

const API_BASE_URL = "http://localhost:8080/api";

const EVENT_TYPES = [
    { value: "HOLIDAY", label: "Holiday" },
    { value: "LEAVE", label: "Leave" },
    { value: "WORKING_DAY", label: "Working Day" },
    { value: "CUSTOM", label: "Event" }
];

const MONTH_NAMES = [
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December"
];

const WEEK_DAYS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

function getToken() {
    return localStorage.getItem("token");
}

function formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
}

function parseDate(dateString) {
    if (!dateString) return new Date();

    const [year, month, day] = dateString.split("-").map(Number);

    return new Date(year, month - 1, day);
}

function startOfWeek(date) {
    const result = new Date(date);
    result.setHours(0, 0, 0, 0);

    result.setDate(result.getDate() - result.getDay());

    return result;
}

function addDays(date, amount) {
    const result = new Date(date);
    result.setDate(result.getDate() + amount);

    return result;
}

function isSameDate(first, second) {
    return (
        first.getFullYear() === second.getFullYear() &&
        first.getMonth() === second.getMonth() &&
        first.getDate() === second.getDate()
    );
}

function getDaysInMonth(year, month) {
    return new Date(year, month + 1, 0).getDate();
}

function getMonthCalendarDays(year, month) {
    const firstDay = new Date(year, month, 1);
    const start = new Date(firstDay);

    start.setDate(start.getDate() - start.getDay());

    const days = [];

    for (let i = 0; i < 42; i++) {
        days.push(addDays(start, i));
    }

    return days;
}

function getEventClass(type) {
    switch (type) {
        case "HOLIDAY":
            return "calendar-event holiday";

        case "LEAVE":
            return "calendar-event leave";

        case "WORKING_DAY":
            return "calendar-event working-day";

        default:
            return "calendar-event custom";
    }
}

function Calendar() {
    const today = new Date();

    const [currentDate, setCurrentDate] = useState(today);
    const [view, setView] = useState("month");

    const [events, setEvents] = useState([]);

    const [selectedDate, setSelectedDate] = useState(today);

    const [showEventModal, setShowEventModal] = useState(false);

    const [editingEvent, setEditingEvent] = useState(null);

    const [eventForm, setEventForm] = useState({
        eventDate: formatDate(today),
        title: "",
        eventType: "CUSTOM",
        description: ""
    });

    const [searchText, setSearchText] = useState("");
    const [eventTypeFilter, setEventTypeFilter] = useState("ALL");

    const [loading, setLoading] = useState(false);

    const [errorMessage, setErrorMessage] = useState("");

    // =====================================================
    // LOAD EVENTS
    // =====================================================

    useEffect(() => {
        loadEvents(currentDate.getFullYear());
    }, [currentDate.getFullYear()]);

    const loadEvents = async (year) => {
        try {
            setLoading(true);
            setErrorMessage("");

            const response = await fetch(
                `${API_BASE_URL}/calendar/year/${year}`,
                {
                    headers: {
                        Authorization: `Bearer ${getToken()}`
                    }
                }
            );

            if (!response.ok) {
                throw new Error("Unable to load calendar events.");
            }

            const data = await response.json();

            setEvents(Array.isArray(data) ? data : []);
        } catch (error) {
            console.error(error);
            setErrorMessage(error.message);
        } finally {
            setLoading(false);
        }
    };

    // =====================================================
    // FILTER EVENTS
    // =====================================================

    const filteredEvents = useMemo(() => {
        return events.filter((event) => {
            const matchesSearch =
                !searchText ||
                event.title
                    ?.toLowerCase()
                    .includes(searchText.toLowerCase()) ||
                event.description
                    ?.toLowerCase()
                    .includes(searchText.toLowerCase());

            const matchesType =
                eventTypeFilter === "ALL" ||
                event.eventType === eventTypeFilter;

            return matchesSearch && matchesType;
        });
    }, [events, searchText, eventTypeFilter]);

    // =====================================================
    // EVENTS FOR DATE
    // =====================================================

    const getEventsForDate = (date) => {
        const dateString = formatDate(date);

        return filteredEvents.filter(
            (event) => event.eventDate === dateString
        );
    };

    // =====================================================
    // SELECT DATE
    // =====================================================

    const handleDateClick = (date) => {
        setSelectedDate(date);

        setEventForm({
            eventDate: formatDate(date),
            title: "",
            eventType: "CUSTOM",
            description: ""
        });
    };

    // =====================================================
    // NEW EVENT
    // =====================================================

    const openNewEvent = (date = selectedDate) => {
        setEditingEvent(null);

        setEventForm({
            eventDate: formatDate(date),
            title: "",
            eventType: "CUSTOM",
            description: ""
        });

        setShowEventModal(true);
    };

    // =====================================================
    // EDIT EVENT
    // =====================================================

    const openEditEvent = (event) => {
        setEditingEvent(event);

        setEventForm({
            eventDate: event.eventDate,
            title: event.title || "",
            eventType: event.eventType || "CUSTOM",
            description: event.description || ""
        });

        setShowEventModal(true);
    };

    // =====================================================
    // CLOSE MODAL
    // =====================================================

    const closeModal = () => {
        setShowEventModal(false);
        setEditingEvent(null);
    };

    // =====================================================
    // FORM CHANGE
    // =====================================================

    const handleFormChange = (event) => {
        const { name, value } = event.target;

        setEventForm((previous) => ({
            ...previous,
            [name]: value
        }));
    };

    // =====================================================
    // SAVE EVENT
    // =====================================================

    const handleSaveEvent = async (event) => {
        event.preventDefault();

        if (!eventForm.title.trim()) {
            alert("Please enter event title.");
            return;
        }

        try {
            setLoading(true);

            const isEdit = Boolean(editingEvent);

            const url = isEdit
                ? `${API_BASE_URL}/calendar/${editingEvent.id}`
                : `${API_BASE_URL}/calendar`;

            const response = await fetch(url, {
                method: isEdit ? "PUT" : "POST",

                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${getToken()}`
                },

                body: JSON.stringify({
                    eventDate: eventForm.eventDate,
                    title: eventForm.title.trim(),
                    eventType: eventForm.eventType,
                    description:
                        eventForm.description.trim() || null
                })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message || "Unable to save calendar event."
                );
            }

            closeModal();

            await loadEvents(currentDate.getFullYear());
        } catch (error) {
            console.error(error);
            alert(error.message);
        } finally {
            setLoading(false);
        }
    };

    // =====================================================
    // DELETE EVENT
    // =====================================================

    const handleDeleteEvent = async (eventId) => {
        const confirmed = window.confirm(
            "Are you sure you want to delete this calendar event?"
        );

        if (!confirmed) return;

        try {
            setLoading(true);

            const response = await fetch(
                `${API_BASE_URL}/calendar/${eventId}`,
                {
                    method: "DELETE",

                    headers: {
                        Authorization: `Bearer ${getToken()}`
                    }
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message || "Unable to delete event."
                );
            }

            await loadEvents(currentDate.getFullYear());
        } catch (error) {
            console.error(error);
            alert(error.message);
        } finally {
            setLoading(false);
        }
    };

    // =====================================================
    // NAVIGATION
    // =====================================================

    const goPrevious = () => {
        const date = new Date(currentDate);

        if (view === "month") {
            date.setMonth(date.getMonth() - 1);
        } else if (view === "week") {
            date.setDate(date.getDate() - 7);
        } else if (view === "day") {
            date.setDate(date.getDate() - 1);
        } else {
            date.setFullYear(date.getFullYear() - 1);
        }

        setCurrentDate(date);
    };

    const goNext = () => {
        const date = new Date(currentDate);

        if (view === "month") {
            date.setMonth(date.getMonth() + 1);
        } else if (view === "week") {
            date.setDate(date.getDate() + 7);
        } else if (view === "day") {
            date.setDate(date.getDate() + 1);
        } else {
            date.setFullYear(date.getFullYear() + 1);
        }

        setCurrentDate(date);
    };

    const goToday = () => {
        const now = new Date();

        setCurrentDate(now);
        setSelectedDate(now);
    };

    // =====================================================
    // HEADER TITLE
    // =====================================================

    const getHeaderTitle = () => {
        if (view === "year") {
            return currentDate.getFullYear().toString();
        }

        if (view === "month") {
            return `${MONTH_NAMES[currentDate.getMonth()]} ${currentDate.getFullYear()}`;
        }

        if (view === "day") {
            return currentDate.toLocaleDateString("en-US", {
                day: "numeric",
                month: "long",
                year: "numeric"
            });
        }

        const start = startOfWeek(currentDate);
        const end = addDays(start, 6);

        if (start.getMonth() === end.getMonth()) {
            return `${MONTH_NAMES[start.getMonth()]} ${start.getFullYear()}`;
        }

        return `${MONTH_NAMES[start.getMonth()]} ${start.getFullYear()} - ${MONTH_NAMES[end.getMonth()]} ${end.getFullYear()}`;
    };

    // =====================================================
    // MONTH VIEW
    // =====================================================

    const renderMonthView = () => {
        const days = getMonthCalendarDays(
            currentDate.getFullYear(),
            currentDate.getMonth()
        );

        return (
            <div className="calendar-month-view">

                <div className="calendar-week-header">
                    {WEEK_DAYS.map((day) => (
                        <div
                            key={day}
                            className="calendar-week-day"
                        >
                            {day}
                        </div>
                    ))}
                </div>

                <div className="calendar-month-grid">

                    {days.map((date) => {
                        const dateEvents =
                            getEventsForDate(date);

                        const isCurrentMonth =
                            date.getMonth() ===
                            currentDate.getMonth();

                        const isToday =
                            isSameDate(date, today);

                        const isSelected =
                            isSameDate(
                                date,
                                selectedDate
                            );

                        return (
                            <div
                                key={formatDate(date)}
                                className={`calendar-day-cell
                                    ${
                                    !isCurrentMonth
                                        ? "other-month"
                                        : ""
                                }
                                    ${
                                    isToday
                                        ? "today-cell"
                                        : ""
                                }
                                    ${
                                    isSelected
                                        ? "selected-cell"
                                        : ""
                                }
                                `}
                                onClick={() =>
                                    handleDateClick(date)
                                }
                            >

                                <div className="calendar-day-number">
                                    {isToday ? (
                                        <span className="today-circle">
                                            {date.getDate()}
                                        </span>
                                    ) : (
                                        date.getDate()
                                    )}
                                </div>

                                <div className="calendar-events">

                                    {dateEvents
                                        .slice(0, 3)
                                        .map((event) => (
                                            <div
                                                key={event.id}
                                                className={getEventClass(
                                                    event.eventType
                                                )}
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    openEditEvent(
                                                        event
                                                    );
                                                }}
                                                title={
                                                    event.description ||
                                                    event.title
                                                }
                                            >
                                                {event.title}
                                            </div>
                                        ))}

                                    {dateEvents.length > 3 && (
                                        <div className="more-events">
                                            +
                                            {dateEvents.length -
                                                3}{" "}
                                            more
                                        </div>
                                    )}

                                </div>

                            </div>
                        );
                    })}

                </div>

            </div>
        );
    };

    // =====================================================
    // WEEK VIEW
    // =====================================================

    const renderWeekView = () => {
        const weekStart = startOfWeek(currentDate);

        const weekDays = Array.from(
            { length: 7 },
            (_, index) =>
                addDays(weekStart, index)
        );

        const hours = Array.from(
            { length: 12 },
            (_, index) => index + 9
        );

        return (
            <div className="calendar-week-view">

                <div className="week-columns">

                    <div className="week-time-column">
                        <div className="week-header-spacer" />

                        {hours.map((hour) => (
                            <div
                                key={hour}
                                className="week-time"
                            >
                                {hour <= 11
                                    ? `${hour} AM`
                                    : hour === 12
                                        ? "12 PM"
                                        : `${hour - 12} PM`}
                            </div>
                        ))}
                    </div>

                    {weekDays.map((date) => {
                        const dateEvents =
                            getEventsForDate(date);

                        return (
                            <div
                                key={formatDate(date)}
                                className="week-day-column"
                            >

                                <div
                                    className="week-day-header"
                                    onClick={() =>
                                        handleDateClick(
                                            date
                                        )
                                    }
                                >
                                    <span>
                                        {date.toLocaleDateString(
                                            "en-US",
                                            {
                                                weekday:
                                                    "short"
                                            }
                                        )}
                                    </span>

                                    <strong
                                        className={
                                            isSameDate(
                                                date,
                                                today
                                            )
                                                ? "week-today"
                                                : ""
                                        }
                                    >
                                        {date.getDate()}
                                    </strong>
                                </div>

                                <div className="week-events-area">

                                    {dateEvents.map(
                                        (event) => (
                                            <div
                                                key={
                                                    event.id
                                                }
                                                className={getEventClass(
                                                    event.eventType
                                                )}
                                                onClick={() =>
                                                    openEditEvent(
                                                        event
                                                    )
                                                }
                                            >
                                                {event.title}
                                            </div>
                                        )
                                    )}

                                    {hours.map(
                                        (hour) => (
                                            <div
                                                key={hour}
                                                className="week-hour-line"
                                                onDoubleClick={() =>
                                                    openNewEvent(
                                                        date
                                                    )
                                                }
                                            />
                                        )
                                    )}

                                </div>

                            </div>
                        );
                    })}

                </div>

            </div>
        );
    };

    // =====================================================
    // DAY VIEW
    // =====================================================

    const renderDayView = () => {
        const dateEvents =
            getEventsForDate(currentDate);

        const hours = Array.from(
            { length: 12 },
            (_, index) => index + 9
        );

        return (
            <div className="calendar-day-view">

                <div className="day-title">
                    <strong>
                        {currentDate.toLocaleDateString(
                            "en-US",
                            {
                                day: "numeric",
                                month: "long",
                                year: "numeric"
                            }
                        )}
                    </strong>

                    <span>
                        {currentDate.toLocaleDateString(
                            "en-US",
                            {
                                weekday: "long"
                            }
                        )}
                    </span>
                </div>

                <div className="day-schedule">

                    <div className="day-time-column">

                        <div className="all-day-row">
                            All-day
                        </div>

                        {hours.map((hour) => (
                            <div
                                key={hour}
                                className="day-time"
                            >
                                {hour <= 11
                                    ? `${hour} AM`
                                    : hour === 12
                                        ? "12 PM"
                                        : `${hour - 12} PM`}
                            </div>
                        ))}

                    </div>

                    <div className="day-events-column">

                        <div className="all-day-events">

                            {dateEvents.map(
                                (event) => (
                                    <div
                                        key={event.id}
                                        className={getEventClass(
                                            event.eventType
                                        )}
                                        onClick={() =>
                                            openEditEvent(
                                                event
                                            )
                                        }
                                    >
                                        {event.title}
                                    </div>
                                )
                            )}

                        </div>

                        {hours.map((hour) => (
                            <div
                                key={hour}
                                className="day-hour-row"
                                onDoubleClick={() =>
                                    openNewEvent(
                                        currentDate
                                    )
                                }
                            />
                        ))}

                    </div>

                </div>

            </div>
        );
    };

    // =====================================================
    // YEAR VIEW
    // =====================================================

    const renderYearView = () => {
        const months = Array.from(
            { length: 12 },
            (_, index) => index
        );

        return (
            <div className="calendar-year-view">

                {months.map((month) => {
                    const days =
                        getMonthCalendarDays(
                            currentDate.getFullYear(),
                            month
                        );

                    return (
                        <div
                            key={month}
                            className="year-month"
                        >

                            <div className="year-month-title">
                                {MONTH_NAMES[month]}
                            </div>

                            <div className="year-week-header">
                                {WEEK_DAYS.map(
                                    (day) => (
                                        <span
                                            key={day}
                                        >
                                            {day.charAt(
                                                0
                                            )}
                                        </span>
                                    )
                                )}
                            </div>

                            <div className="year-days">

                                {days.map(
                                    (date) => {
                                        const isMonth =
                                            date.getMonth() ===
                                            month;

                                        const isToday =
                                            isSameDate(
                                                date,
                                                today
                                            );

                                        const dateEvents =
                                            getEventsForDate(
                                                date
                                            );

                                        return (
                                            <span
                                                key={formatDate(
                                                    date
                                                )}
                                                className={`
                                                    ${
                                                    !isMonth
                                                        ? "year-other-day"
                                                        : ""
                                                }
                                                    ${
                                                    isToday
                                                        ? "year-today"
                                                        : ""
                                                }
                                                    ${
                                                    dateEvents.length >
                                                    0
                                                        ? "year-event-day"
                                                        : ""
                                                }
                                                `}
                                                onClick={() => {
                                                    if (
                                                        isMonth
                                                    ) {
                                                        setCurrentDate(
                                                            date
                                                        );
                                                        setSelectedDate(
                                                            date
                                                        );
                                                        setView(
                                                            "day"
                                                        );
                                                    }
                                                }}
                                            >
                                                {date.getDate()}
                                            </span>
                                        );
                                    }
                                )}

                            </div>

                        </div>
                    );
                })}

            </div>
        );
    };

    // =====================================================
    // RENDER
    // =====================================================

    return (
        <div className="calendar-page">

            {/* =================================================
                HEADER
            ================================================= */}

            <div className="calendar-topbar">

                <button
                    className="new-event-button"
                    onClick={() =>
                        openNewEvent(selectedDate)
                    }
                >
                    + New Event
                </button>

                <div className="calendar-view-switcher">

                    {["day", "week", "month", "year"].map(
                        (item) => (
                            <button
                                key={item}
                                className={
                                    view === item
                                        ? "active"
                                        : ""
                                }
                                onClick={() =>
                                    setView(item)
                                }
                            >
                                {item
                                        .charAt(0)
                                        .toUpperCase() +
                                    item.slice(1)}
                            </button>
                        )
                    )}

                </div>

                <div className="calendar-search">

                    <span>⌕</span>

                    <input
                        type="text"
                        placeholder="Search"
                        value={searchText}
                        onChange={(e) =>
                            setSearchText(
                                e.target.value
                            )
                        }
                    />

                </div>

            </div>

            {/* =================================================
                FILTERS
            ================================================= */}

            <div className="calendar-filterbar">

                <div className="filter-group">

                    <label>Search events</label>

                    <input
                        type="text"
                        placeholder="Search events..."
                        value={searchText}
                        onChange={(e) =>
                            setSearchText(
                                e.target.value
                            )
                        }
                    />

                </div>

                <div className="filter-group">

                    <label>Event Type</label>

                    <select
                        value={eventTypeFilter}
                        onChange={(e) =>
                            setEventTypeFilter(
                                e.target.value
                            )
                        }
                    >
                        <option value="ALL">
                            All
                        </option>

                        {EVENT_TYPES.map(
                            (type) => (
                                <option
                                    key={type.value}
                                    value={type.value}
                                >
                                    {type.label}
                                </option>
                            )
                        )}
                    </select>

                </div>

                <div className="calendar-legend">

                    <span>
                        <i className="legend-dot holiday-dot" />
                        Holiday
                    </span>

                    <span>
                        <i className="legend-dot leave-dot" />
                        Leave
                    </span>

                    <span>
                        <i className="legend-dot event-dot" />
                        Event
                    </span>

                    <span>
                        <i className="legend-dot working-dot" />
                        Working Day
                    </span>

                </div>

            </div>

            {/* =================================================
                CALENDAR HEADER
            ================================================= */}

            <div className="calendar-heading">

                <h1>
                    {getHeaderTitle()}
                </h1>

                <div className="calendar-navigation">

                    <button onClick={goPrevious}>
                        ‹
                    </button>

                    <button
                        className="today-button"
                        onClick={goToday}
                    >
                        Today
                    </button>

                    <button onClick={goNext}>
                        ›
                    </button>

                </div>

            </div>

            {/* =================================================
                ERROR
            ================================================= */}

            {errorMessage && (
                <div className="calendar-error">
                    {errorMessage}
                </div>
            )}

            {/* =================================================
                LOADING
            ================================================= */}

            {loading && (
                <div className="calendar-loading">
                    Loading...
                </div>
            )}

            {/* =================================================
                CALENDAR
            ================================================= */}

            <div className="calendar-container">

                {view === "month" &&
                    renderMonthView()}

                {view === "week" &&
                    renderWeekView()}

                {view === "day" &&
                    renderDayView()}

                {view === "year" &&
                    renderYearView()}

            </div>

            {/* =================================================
                SELECTED DATE PANEL
            ================================================= */}

            <div className="selected-date-panel">

                <div>
                    <span>
                        Selected Date
                    </span>

                    <strong>
                        {selectedDate.toLocaleDateString(
                            "en-US",
                            {
                                weekday: "long",
                                day: "numeric",
                                month: "long",
                                year: "numeric"
                            }
                        )}
                    </strong>
                </div>

                <button
                    onClick={() =>
                        openNewEvent(selectedDate)
                    }
                >
                    + Add Event
                </button>

            </div>

            {/* =================================================
                EVENT MODAL
            ================================================= */}

            {showEventModal && (
                <div
                    className="calendar-modal-overlay"
                    onClick={closeModal}
                >

                    <div
                        className="calendar-modal"
                        onClick={(e) =>
                            e.stopPropagation()
                        }
                    >

                        <div className="calendar-modal-header">

                            <div>
                                <h2>
                                    {editingEvent
                                        ? "Edit Event"
                                        : "New Event"}
                                </h2>

                                <p>
                                    Add or update calendar
                                    information.
                                </p>
                            </div>

                            <button
                                className="close-modal"
                                onClick={closeModal}
                            >
                                ×
                            </button>

                        </div>

                        <form
                            onSubmit={handleSaveEvent}
                        >

                            <div className="form-row">

                                <div className="form-field">

                                    <label>
                                        Date
                                    </label>

                                    <input
                                        type="date"
                                        name="eventDate"
                                        value={
                                            eventForm.eventDate
                                        }
                                        onChange={
                                            handleFormChange
                                        }
                                        required
                                    />

                                </div>

                                <div className="form-field">

                                    <label>
                                        Event Type
                                    </label>

                                    <select
                                        name="eventType"
                                        value={
                                            eventForm.eventType
                                        }
                                        onChange={
                                            handleFormChange
                                        }
                                    >
                                        {EVENT_TYPES.map(
                                            (type) => (
                                                <option
                                                    key={
                                                        type.value
                                                    }
                                                    value={
                                                        type.value
                                                    }
                                                >
                                                    {
                                                        type.label
                                                    }
                                                </option>
                                            )
                                        )}
                                    </select>

                                </div>

                            </div>

                            <div className="form-field">

                                <label>
                                    Title
                                </label>

                                <input
                                    type="text"
                                    name="title"
                                    placeholder="Enter event title"
                                    value={
                                        eventForm.title
                                    }
                                    onChange={
                                        handleFormChange
                                    }
                                    maxLength={150}
                                    required
                                />

                            </div>

                            <div className="form-field">

                                <label>
                                    Description
                                </label>

                                <textarea
                                    name="description"
                                    placeholder="Add notes, details or description..."
                                    value={
                                        eventForm.description
                                    }
                                    onChange={
                                        handleFormChange
                                    }
                                    rows={4}
                                />

                            </div>

                            <div className="modal-actions">

                                {editingEvent && (
                                    <button
                                        type="button"
                                        className="delete-event-button"
                                        onClick={() => {
                                            closeModal();
                                            handleDeleteEvent(
                                                editingEvent.id
                                            );
                                        }}
                                    >
                                        Delete
                                    </button>
                                )}

                                <div className="modal-right-actions">

                                    <button
                                        type="button"
                                        className="cancel-button"
                                        onClick={
                                            closeModal
                                        }
                                    >
                                        Cancel
                                    </button>

                                    <button
                                        type="submit"
                                        className="save-event-button"
                                        disabled={
                                            loading
                                        }
                                    >
                                        {editingEvent
                                            ? "Update Event"
                                            : "Save Event"}
                                    </button>

                                </div>

                            </div>

                        </form>

                    </div>

                </div>
            )}

        </div>
    );
}

export default Calendar;