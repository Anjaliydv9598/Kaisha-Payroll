import React, { useMemo, useState } from "react";
import "./Calendar.css";

const MONTHS = [
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
    "December",
];

const WEEK_DAYS = ["S", "M", "T", "W", "T", "F", "S"];

const EVENT_TYPES = [
    "HOLIDAY",
    "LEAVE",
    "WORKING_DAY",
    "CUSTOM",
];

function Calendar() {
    const today = new Date();

    const [selectedYear, setSelectedYear] = useState(
        today.getFullYear()
    );

    const [events, setEvents] = useState([
        {
            id: 1,
            date: `${today.getFullYear()}-${String(
                today.getMonth() + 1
            ).padStart(2, "0")}-${String(today.getDate()).padStart(
                2,
                "0"
            )}`,
            title: "Today",
            type: "CUSTOM",
            description: "Current date",
        },
    ]);

    const [showModal, setShowModal] = useState(false);

    const [selectedDate, setSelectedDate] = useState("");

    const [editingEvent, setEditingEvent] = useState(null);

    const [formData, setFormData] = useState({
        title: "",
        type: "HOLIDAY",
        description: "",
    });

    const formatDate = (date) => {
        return `${date.getFullYear()}-${String(
            date.getMonth() + 1
        ).padStart(2, "0")}-${String(date.getDate()).padStart(
            2,
            "0"
        )}`;
    };

    const isToday = (date) => {
        return formatDate(date) === formatDate(today);
    };

    const getEventsForDate = (date) => {
        const dateString = formatDate(date);

        return events.filter(
            (event) => event.date === dateString
        );
    };

    const getMonthDays = (year, monthIndex) => {
        const firstDay = new Date(
            year,
            monthIndex,
            1
        ).getDay();

        const daysInMonth = new Date(
            year,
            monthIndex + 1,
            0
        ).getDate();

        const previousMonthDays = new Date(
            year,
            monthIndex,
            0
        ).getDate();

        const days = [];

        // Previous month
        for (let i = firstDay - 1; i >= 0; i--) {
            days.push({
                day: previousMonthDays - i,
                currentMonth: false,
                date: new Date(
                    year,
                    monthIndex - 1,
                    previousMonthDays - i
                ),
            });
        }

        // Current month
        for (let day = 1; day <= daysInMonth; day++) {
            days.push({
                day,
                currentMonth: true,
                date: new Date(
                    year,
                    monthIndex,
                    day
                ),
            });
        }

        // Next month
        let nextDay = 1;

        while (days.length < 42) {
            days.push({
                day: nextDay,
                currentMonth: false,
                date: new Date(
                    year,
                    monthIndex + 1,
                    nextDay
                ),
            });

            nextDay++;
        }

        return days;
    };

    const monthsData = useMemo(() => {
        return MONTHS.map((monthName, monthIndex) => ({
            monthName,
            monthIndex,
            days: getMonthDays(
                selectedYear,
                monthIndex
            ),
        }));
    }, [selectedYear]);

    const previousYear = () => {
        setSelectedYear((year) => year - 1);
    };

    const nextYear = () => {
        setSelectedYear((year) => year + 1);
    };

    const goToCurrentYear = () => {
        setSelectedYear(today.getFullYear());
    };

    const openAddEvent = (date) => {
        setSelectedDate(formatDate(date));

        setFormData({
            title: "",
            type: "HOLIDAY",
            description: "",
        });

        setEditingEvent(null);
        setShowModal(true);
    };

    const openEditEvent = (event) => {
        setSelectedDate(event.date);

        setFormData({
            title: event.title,
            type: event.type,
            description: event.description || "",
        });

        setEditingEvent(event);
        setShowModal(true);
    };

    const closeModal = () => {
        setShowModal(false);
        setEditingEvent(null);
    };

    const handleChange = (event) => {
        const { name, value } = event.target;

        setFormData((previous) => ({
            ...previous,
            [name]: value,
        }));
    };

    const handleSave = (event) => {
        event.preventDefault();

        if (!formData.title.trim()) {
            alert("Please enter event name.");
            return;
        }

        if (!selectedDate) {
            alert("Please select a date.");
            return;
        }

        if (editingEvent) {
            setEvents((previous) =>
                previous.map((item) =>
                    item.id === editingEvent.id
                        ? {
                            ...item,
                            date: selectedDate,
                            title: formData.title.trim(),
                            type: formData.type,
                            description:
                                formData.description.trim(),
                        }
                        : item
                )
            );
        } else {
            const newEvent = {
                id: Date.now(),
                date: selectedDate,
                title: formData.title.trim(),
                type: formData.type,
                description:
                    formData.description.trim(),
            };

            setEvents((previous) => [
                ...previous,
                newEvent,
            ]);
        }

        closeModal();
    };

    const handleDelete = (eventId) => {
        const confirmed = window.confirm(
            "Are you sure you want to delete this event?"
        );

        if (!confirmed) {
            return;
        }

        setEvents((previous) =>
            previous.filter(
                (event) => event.id !== eventId
            )
        );
    };

    const getEventClass = (type) => {
        switch (type) {
            case "HOLIDAY":
                return "mini-event holiday";

            case "LEAVE":
                return "mini-event leave";

            case "WORKING_DAY":
                return "mini-event working";

            default:
                return "mini-event custom";
        }
    };

    return (
        <div className="calendar-page">

            {/* HEADER */}
            <div className="calendar-page-header">

                <div className="calendar-title-section">
                    <h1>Calendar</h1>

                    <p>
                        Manage holidays, leaves and important
                        company dates.
                    </p>
                </div>

                <div className="calendar-header-actions">

                    <button
                        className="year-navigation-button"
                        onClick={previousYear}
                    >
                        ‹
                    </button>

                    <button
                        className="today-button"
                        onClick={goToCurrentYear}
                    >
                        Today
                    </button>

                    <button
                        className="year-navigation-button"
                        onClick={nextYear}
                    >
                        ›
                    </button>

                    <button
                        className="add-event-button"
                        onClick={() =>
                            openAddEvent(today)
                        }
                    >
                        + Add Event
                    </button>

                </div>
            </div>

            {/* VIEW CONTROLS */}
            <div className="calendar-view-toolbar">

                <div className="view-switcher">

                    <button className="view-button">
                        Day
                    </button>

                    <button className="view-button">
                        Week
                    </button>

                    <button className="view-button">
                        Month
                    </button>

                    <button className="view-button active">
                        Year
                    </button>

                </div>

                <div className="calendar-search">

                    <span>⌕</span>

                    <input
                        type="text"
                        placeholder="Search"
                    />

                </div>

            </div>

            {/* YEAR */}
            <div className="calendar-year-heading">
                {selectedYear}
            </div>

            {/* 12 MONTHS */}
            <div className="year-calendar-grid">

                {monthsData.map((month) => (

                    <div
                        className="mini-month"
                        key={month.monthIndex}
                    >

                        <div className="mini-month-title">
                            {month.monthName}
                        </div>

                        <div className="mini-week-header">

                            {WEEK_DAYS.map(
                                (day, index) => (
                                    <div
                                        key={`${day}-${index}`}
                                        className="mini-week-day"
                                    >
                                        {day}
                                    </div>
                                )
                            )}

                        </div>

                        <div className="mini-days-grid">

                            {month.days.map(
                                (calendarDay, index) => {

                                    const dayEvents =
                                        getEventsForDate(
                                            calendarDay.date
                                        );

                                    return (
                                        <div
                                            key={`${formatDate(
                                                calendarDay.date
                                            )}-${index}`}
                                            className={`mini-day ${
                                                !calendarDay.currentMonth
                                                    ? "other-month"
                                                    : ""
                                            } ${
                                                isToday(
                                                    calendarDay.date
                                                )
                                                    ? "today"
                                                    : ""
                                            }`}
                                            onDoubleClick={() =>
                                                openAddEvent(
                                                    calendarDay.date
                                                )
                                            }
                                            title={
                                                dayEvents.length >
                                                0
                                                    ? dayEvents
                                                        .map(
                                                            (
                                                                event
                                                            ) =>
                                                                event.title
                                                        )
                                                        .join(
                                                            ", "
                                                        )
                                                    : ""
                                            }
                                        >

                                            <span>
                                                {
                                                    calendarDay.day
                                                }
                                            </span>

                                            {dayEvents.length >
                                                0 && (
                                                    <div className="event-indicator">
                                                        {dayEvents.map(
                                                            (
                                                                event
                                                            ) => (
                                                                <span
                                                                    key={
                                                                        event.id
                                                                    }
                                                                    className={getEventClass(
                                                                        event.type
                                                                    )}
                                                                    onClick={(
                                                                        e
                                                                    ) => {
                                                                        e.stopPropagation();
                                                                        openEditEvent(
                                                                            event
                                                                        );
                                                                    }}
                                                                />
                                                            )
                                                        )}
                                                    </div>
                                                )}

                                        </div>
                                    );
                                }
                            )}

                        </div>

                    </div>
                ))}

            </div>

            {/* EVENTS */}
            <div className="calendar-events-section">

                <div className="events-section-header">

                    <div>
                        <h2>Calendar Events</h2>

                        <p>
                            Holidays, leaves and custom
                            dates for {selectedYear}.
                        </p>
                    </div>

                    <button
                        className="add-event-small-button"
                        onClick={() =>
                            openAddEvent(today)
                        }
                    >
                        + Add Event
                    </button>

                </div>

                {events.filter((event) =>
                    event.date.startsWith(
                        String(selectedYear)
                    )
                ).length === 0 ? (

                    <div className="no-events">
                        No events added for {selectedYear}.
                    </div>

                ) : (

                    <div className="events-table-wrapper">

                        <table className="events-table">

                            <thead>
                            <tr>
                                <th>Date</th>
                                <th>Event</th>
                                <th>Type</th>
                                <th>Description</th>
                                <th>Action</th>
                            </tr>
                            </thead>

                            <tbody>

                            {events
                                .filter((event) =>
                                    event.date.startsWith(
                                        String(
                                            selectedYear
                                        )
                                    )
                                )
                                .sort((a, b) =>
                                    a.date.localeCompare(
                                        b.date
                                    )
                                )
                                .map((event) => (

                                    <tr key={event.id}>

                                        <td>
                                            {event.date}
                                        </td>

                                        <td className="event-name">
                                            {event.title}
                                        </td>

                                        <td>
                                                <span
                                                    className={`event-badge ${event.type.toLowerCase()}`}
                                                >
                                                    {event.type.replace(
                                                        "_",
                                                        " "
                                                    )}
                                                </span>
                                        </td>

                                        <td>
                                            {event.description ||
                                                "-"}
                                        </td>

                                        <td>

                                            <div className="event-actions">

                                                <button
                                                    className="edit-button"
                                                    onClick={() =>
                                                        openEditEvent(
                                                            event
                                                        )
                                                    }
                                                >
                                                    Edit
                                                </button>

                                                <button
                                                    className="delete-button"
                                                    onClick={() =>
                                                        handleDelete(
                                                            event.id
                                                        )
                                                    }
                                                >
                                                    Delete
                                                </button>

                                            </div>

                                        </td>

                                    </tr>

                                ))}

                            </tbody>

                        </table>

                    </div>

                )}

            </div>

            {/* MODAL */}
            {showModal && (

                <div
                    className="calendar-modal-overlay"
                    onClick={closeModal}
                >

                    <div
                        className="calendar-modal"
                        onClick={(event) =>
                            event.stopPropagation()
                        }
                    >

                        <div className="calendar-modal-header">

                            <div>
                                <h2>
                                    {editingEvent
                                        ? "Edit Calendar Event"
                                        : "Add Calendar Event"}
                                </h2>

                                <p>
                                    Add a holiday, leave or
                                    custom date.
                                </p>
                            </div>

                            <button
                                className="modal-close"
                                onClick={closeModal}
                            >
                                ×
                            </button>

                        </div>

                        <form onSubmit={handleSave}>

                            <div className="form-group">

                                <label>
                                    Date
                                </label>

                                <input
                                    type="date"
                                    value={selectedDate}
                                    onChange={(event) =>
                                        setSelectedDate(
                                            event.target.value
                                        )
                                    }
                                    required
                                />

                            </div>

                            <div className="form-group">

                                <label>
                                    Event Name
                                </label>

                                <input
                                    type="text"
                                    name="title"
                                    value={formData.title}
                                    onChange={handleChange}
                                    placeholder="e.g. Independence Day"
                                    required
                                />

                            </div>

                            <div className="form-group">

                                <label>
                                    Event Type
                                </label>

                                <select
                                    name="type"
                                    value={formData.type}
                                    onChange={handleChange}
                                >

                                    {EVENT_TYPES.map(
                                        (type) => (
                                            <option
                                                key={type}
                                                value={type}
                                            >
                                                {type.replace(
                                                    "_",
                                                    " "
                                                )}
                                            </option>
                                        )
                                    )}

                                </select>

                            </div>

                            <div className="form-group">

                                <label>
                                    Description
                                </label>

                                <textarea
                                    name="description"
                                    value={
                                        formData.description
                                    }
                                    onChange={handleChange}
                                    placeholder="Enter description..."
                                    rows="4"
                                />

                            </div>

                            <div className="modal-actions">

                                <button
                                    type="button"
                                    className="cancel-button"
                                    onClick={closeModal}
                                >
                                    Cancel
                                </button>

                                <button
                                    type="submit"
                                    className="save-button"
                                >
                                    {editingEvent
                                        ? "Update Event"
                                        : "Save Event"}
                                </button>

                            </div>

                        </form>

                    </div>

                </div>

            )}

        </div>
    );
}

export default Calendar;