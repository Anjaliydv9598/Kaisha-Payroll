import React from "react";

function StaffProfile({ staff }) {
    const profileFields = [
        ["Username / Employee Login ID", staff?.username],
        ["Email Address", staff?.email],
        ["Mobile Number", staff?.mobileNumber],
        ["Role", staff?.role],
        ["Company ID", staff?.companyId],
        ["Company Name", staff?.companyName],
        ["Location", staff?.location],
    ];

    return (
        <div className="staff-content">
            <header className="staff-page-header">
                <div>
                    <h1>My Profile</h1>
                    <p>View your account and company information.</p>
                </div>
            </header>

            <section className="staff-section">
                <div className="staff-profile-heading">
                    <div className="staff-profile-avatar">
                        {(staff?.username || "S").charAt(0).toUpperCase()}
                    </div>

                    <div>
                        <h2>{staff?.username || "Staff"}</h2>
                        <span>{staff?.role || "STAFF"}</span>
                    </div>
                </div>

                <div className="staff-profile-grid">
                    {profileFields.map(([label, value]) => (
                        <div className="staff-profile-field" key={label}>
                            <span>{label}</span>
                            <strong>{value || "Not available"}</strong>
                        </div>
                    ))}
                </div>
            </section>
        </div>
    );
}

export default StaffProfile;