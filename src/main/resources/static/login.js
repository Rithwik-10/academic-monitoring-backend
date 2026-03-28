const loginForm = document.getElementById("loginForm");
const loadingOverlay = document.getElementById("loadingOverlay");
const toastContainer = document.getElementById("toastContainer");

document.addEventListener("DOMContentLoaded", () => {
    resetAuthSession();
    loginForm.addEventListener("submit", handleLogin);
});

function resetAuthSession() {
    localStorage.removeItem("ams_token");
    localStorage.removeItem("ams_username");
    localStorage.removeItem("ams_role");
}

async function handleLogin(event) {
    event.preventDefault();
    try {
        setLoading(true);
        const response = await request("/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                username: document.getElementById("loginUsername").value,
                password: document.getElementById("loginPassword").value
            })
        });
        localStorage.setItem("ams_token", response.token);
        localStorage.setItem("ams_username", response.username);
        localStorage.setItem("ams_role", response.role);
        window.location.href = "/dashboard.html";
    } catch (error) {
        toast(error.message, "error");
    } finally {
        setLoading(false);
    }
}

function setLoading(isLoading) {
    loadingOverlay.classList.toggle("hidden", !isLoading);
}

function toast(message, type) {
    const toastEl = document.createElement("div");
    toastEl.className = `toast ${type}`;
    toastEl.textContent = message;
    toastContainer.appendChild(toastEl);
    setTimeout(() => toastEl.remove(), 2600);
}

async function request(url, options = {}) {
    const response = await fetch(url, options);
    if (!response.ok) {
        let errorMessage = "Request failed";
        try {
            const errorBody = await response.json();
            errorMessage = errorBody.message || errorMessage;
        } catch (error) {
            errorMessage = response.statusText || errorMessage;
        }
        throw new Error(errorMessage);
    }
    return response.json();
}
