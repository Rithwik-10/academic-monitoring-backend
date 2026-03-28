const recordsTableBody = document.getElementById("recordsTableBody");
const averageAhiValue = document.getElementById("averageAhiValue");
const studentCountValue = document.getElementById("studentCountValue");
const recordCountValue = document.getElementById("recordCountValue");
const atRiskPercentageValue = document.getElementById("atRiskPercentageValue");
const avgAttendanceValue = document.getElementById("avgAttendanceValue");
const lowAverageValue = document.getElementById("lowAverageValue");
const goodAverageValue = document.getElementById("goodAverageValue");
const highAchieversValue = document.getElementById("highAchieversValue");
const lowAverageText = document.getElementById("lowAverageText");
const goodAverageText = document.getElementById("goodAverageText");
const highAchieversText = document.getElementById("highAchieversText");
const studentSelect = document.getElementById("studentId");
const aiStudentSelect = document.getElementById("aiStudentSelect");
const comparisonStudentSelect = document.getElementById("comparisonStudentSelect");
const aiSummary = document.getElementById("aiSummary");
const aiSummarySource = document.getElementById("aiSummarySource");
const aiCacheStatus = document.getElementById("aiCacheStatus");
const aiConfidenceValue = document.getElementById("aiConfidenceValue");
const aiRiskLevel = document.getElementById("aiRiskLevel");
const aiPriorityLevel = document.getElementById("aiPriorityLevel");
const aiReasons = document.getElementById("aiReasons");
const aiRecommendations = document.getElementById("aiRecommendations");
const predictionRiskScore = document.getElementById("predictionRiskScore");
const predictionRiskFlag = document.getElementById("predictionRiskFlag");
const predictionPriority = document.getElementById("predictionPriority");
const predictionMessage = document.getElementById("predictionMessage");
const aiChatQuestion = document.getElementById("aiChatQuestion");
const aiChatAnswer = document.getElementById("aiChatAnswer");
const topRiskList = document.getElementById("topRiskList");
const insightsAverageAhi = document.getElementById("insightsAverageAhi");
const insightsAtRiskPercentage = document.getElementById("insightsAtRiskPercentage");
const insightsMostCommonIssue = document.getElementById("insightsMostCommonIssue");
const insightsCriticalStudents = document.getElementById("insightsCriticalStudents");
const messageBox = document.getElementById("messageBox");
const recordForm = document.getElementById("recordForm");
const formMode = document.getElementById("formMode");
const insightSidebar = document.getElementById("insightSidebar");
const navSidebar = document.getElementById("navSidebar");
const sidebarOverlay = document.getElementById("sidebarOverlay");
const statusSummaryText = document.getElementById("statusSummaryText");
const statusLegend = document.getElementById("statusLegend");
const alertsList = document.getElementById("alertsList");
const paginationInfo = document.getElementById("paginationInfo");
const loadingOverlay = document.getElementById("loadingOverlay");
const toastContainer = document.getElementById("toastContainer");
const authStatus = document.getElementById("authStatus");
const themeSelect = document.getElementById("themeSelect");
const appBody = document.getElementById("appBody");

let studentsCache = [];
let recordsCache = [];
let currentPage = 0;
let currentSize = 10;
let totalPages = 1;
let activeFilters = {};

document.addEventListener("DOMContentLoaded", async () => {
    if (!window.location.hash) {
        window.scrollTo(0, 0);
    }
    applyTheme(localStorage.getItem("ams_theme") || "light");
    bindEvents();
    updateAuthUi();

    if (!isAuthenticated()) {
        window.location.replace("/");
        return;
    }

    await initializeDashboard();
});

function bindEvents() {
    recordForm.addEventListener("submit", handleFormSubmit);
    document.getElementById("resetFormBtn").addEventListener("click", resetForm);
    document.getElementById("refreshBtn").addEventListener("click", refreshDashboard);
    document.getElementById("lookupBtn").addEventListener("click", lookupByStudentId);
    document.getElementById("filterBtn").addEventListener("click", filterByStatusOnly);
    document.getElementById("nameSearchBtn").addEventListener("click", runNamedSearch);
    document.getElementById("advancedFilterBtn").addEventListener("click", runAdvancedFilters);
    document.getElementById("clearFilterBtn").addEventListener("click", clearFilters);
    document.getElementById("deleteBtn").addEventListener("click", deleteByStudentId);
    document.getElementById("openSidebarBtn").addEventListener("click", openInsightsSidebar);
    document.getElementById("closeSidebarBtn").addEventListener("click", closeInsightsSidebar);
    document.getElementById("sidebarToggleBtn").addEventListener("click", toggleNavSidebar);
    document.getElementById("analyzeAiBtn").addEventListener("click", () => analyzeStudentWithAi());
    document.getElementById("predictAiBtn").addEventListener("click", () => predictStudentWithAi());
    document.getElementById("askAiBtn").addEventListener("click", askAiQuestion);
    document.getElementById("reloadTopRiskBtn").addEventListener("click", () => loadTopRiskStudents());
    document.getElementById("sidebarExportBtn").addEventListener("click", exportCsv);
    document.getElementById("sidebarInsightsBtn").addEventListener("click", () => {
        closeNavSidebar();
        openInsightsSidebar();
    });
    document.getElementById("logoutBtn").addEventListener("click", handleLogout);
    document.getElementById("prevPageBtn").addEventListener("click", () => changePage(-1));
    document.getElementById("nextPageBtn").addEventListener("click", () => changePage(1));
    themeSelect.addEventListener("change", (event) => applyTheme(event.target.value));
    sidebarOverlay.addEventListener("click", closeAllPanels);
    comparisonStudentSelect.addEventListener("change", async () => {
        renderComparisonChart();
        await renderTrendChart();
    });
    studentSelect.addEventListener("change", syncSelectionState);
    aiStudentSelect.addEventListener("change", async () => {
        syncSelectionStateFromAi();
        await analyzeStudentWithAi(true);
        await predictStudentWithAi(true);
    });
    document.querySelectorAll(".sidebar-link").forEach((link) => {
        link.addEventListener("click", closeNavSidebar);
    });
}

function isAuthenticated() {
    return Boolean(localStorage.getItem("ams_token"));
}

async function initializeDashboard() {
    setLoading(true);
    try {
        await refreshDashboard();
    } finally {
        setLoading(false);
    }
}

function applyTheme(theme) {
    const safeTheme = theme === "dark" ? "dark" : "light";
    document.documentElement.setAttribute("data-theme", safeTheme);
    if (appBody) appBody.setAttribute("data-theme", safeTheme);
    localStorage.setItem("ams_theme", safeTheme);
    if (themeSelect) themeSelect.value = safeTheme;
}

async function refreshDashboard() {
    if (!isAuthenticated()) {
        window.location.replace("/");
        return;
    }
    setLoading(true);
    try {
        await Promise.all([
            loadStudents(),
            loadRecordsForMetrics(),
            loadAverageAhi(),
            loadAiInsights(),
            loadTablePage(),
            loadAlerts(),
            loadTopRiskStudents(true)
        ]);
        await initializeAiPanels();
    } finally {
        setLoading(false);
    }
}

async function loadStudents() {
    const students = await request("/api/students");
    studentsCache = students;
    studentSelect.innerHTML = '<option value="">Select a student</option>';
    aiStudentSelect.innerHTML = '<option value="">Select a student</option>';
    comparisonStudentSelect.innerHTML = "";

    students.forEach((student) => {
        const option = document.createElement("option");
        option.value = student.id;
        option.textContent = `${student.id} - ${student.name} (${student.rollNumber})`;
        studentSelect.appendChild(option);

        const compareOption = document.createElement("option");
        compareOption.value = student.id;
        compareOption.textContent = `${student.name} (${student.rollNumber})`;
        comparisonStudentSelect.appendChild(compareOption);

        const aiOption = document.createElement("option");
        aiOption.value = student.id;
        aiOption.textContent = `${student.name} (${student.rollNumber})`;
        aiStudentSelect.appendChild(aiOption);
    });

    studentCountValue.textContent = students.length;
    if (students.length > 0 && !comparisonStudentSelect.value) {
        comparisonStudentSelect.value = String(students[0].id);
    }
    if (students.length > 0 && !aiStudentSelect.value) {
        aiStudentSelect.value = String(students[0].id);
    }
}

async function loadRecordsForMetrics() {
    const records = await request("/api/records");
    recordsCache = records;
    recordCountValue.textContent = records.length;
    renderKpis(records);
    renderStatusPieChart();
    renderComparisonChart();
    renderWorstStudentsChart();
    renderCohortSafetyChart();
    await renderTrendChart();
}

async function loadTablePage() {
    const url = Object.keys(activeFilters).length
        ? buildFilterUrl(activeFilters, currentPage, currentSize)
        : `/api/records?page=${currentPage}&size=${currentSize}`;
    const pageData = await request(url);
    const pageRecords = pageData.content || [];
    totalPages = Math.max(pageData.totalPages || 1, 1);
    paginationInfo.textContent = `Page ${currentPage + 1} of ${totalPages}`;
    renderRecords(pageRecords);
}

async function loadAverageAhi() {
    const average = await request("/api/records/average-ahi");
    averageAhiValue.textContent = Number(average || 0).toFixed(2);
}

async function loadAiInsights() {
    const insights = await request("/api/ai/insights");
    insightsAverageAhi.textContent = formatNumber(insights.averageAhi);
    insightsAtRiskPercentage.textContent = `${Number(insights.atRiskPercentage || 0).toFixed(2)}%`;
    insightsMostCommonIssue.textContent = insights.mostCommonIssue || "No common issue detected";
    insightsCriticalStudents.textContent = String(insights.criticalStudents ?? 0);
}

async function loadAlerts() {
    const alerts = await request("/api/alerts");
    alertsList.innerHTML = alerts.length
        ? alerts.map((alert) => `
            <div class="alert-item">
                <strong>${alert.student?.name ?? "Student"}</strong>
                <p>${alert.message}</p>
                <span>${formatDate(alert.createdAt)}</span>
            </div>`).join("")
        : '<div class="empty-state small-empty">No recent alerts.</div>';
}

async function handleFormSubmit(event) {
    event.preventDefault();
    if (!ensureAuthenticatedAction()) return;

    const studentId = studentSelect.value;
    const payload = {
        student: { id: Number(studentId) },
        attendancePercentage: Number(document.getElementById("attendancePercentage").value),
        internalMarks: Number(document.getElementById("internalMarks").value),
        assignmentsSubmitted: Number(document.getElementById("assignmentsSubmitted").value),
        totalAssignments: Number(document.getElementById("totalAssignments").value)
    };
    const mode = formMode.value;
    const endpoint = mode === "update" ? `/api/records/student/${studentId}` : "/api/records";
    const method = mode === "update" ? "PUT" : "POST";

    try {
        setLoading(true);
        const savedRecord = await request(endpoint, {
            method,
            headers: authJsonHeaders(),
            body: JSON.stringify(payload)
        });
        showMessage(`${mode === "update" ? "Updated" : "Created"} record successfully.`, "success");
        toast(`${mode === "update" ? "Record updated" : "Record saved"}`, "success");
        resetForm();
        await refreshDashboard();
        if (savedRecord?.student?.id) {
            comparisonStudentSelect.value = String(savedRecord.student.id);
        }
    } catch (error) {
        showMessage(error.message, "error");
        toast(error.message, "error");
    } finally {
        setLoading(false);
    }
}

function handleLogout() {
    localStorage.removeItem("ams_token");
    localStorage.removeItem("ams_username");
    localStorage.removeItem("ams_role");
    updateAuthUi();
    closeNavSidebar();
    closeInsightsSidebar();
    window.location.replace("/");
}

function updateAuthUi() {
    const username = localStorage.getItem("ams_username");
    const role = localStorage.getItem("ams_role");
    authStatus.textContent = username ? `${username} (${role})` : "";
}

async function lookupByStudentId() {
    const studentId = document.getElementById("lookupStudentId").value;
    if (!studentId) {
        toast("Enter a student id to search.", "error");
        return;
    }
    try {
        const record = await request(`/api/records/student/${studentId}`);
        renderRecords([record]);
        paginationInfo.textContent = `Showing student ${studentId}`;
        comparisonStudentSelect.value = String(studentId);
        renderComparisonChart();
        await renderTrendChart();
        toast(`Showing record for student ${studentId}`, "success");
    } catch (error) {
        toast(error.message, "error");
    }
}

function filterByStatusOnly() {
    const status = document.getElementById("statusFilter").value;
    activeFilters = status ? { status } : {};
    currentPage = 0;
    loadTablePage().then(() => toast(status ? `Filtered by ${status}` : "Showing all records", "success"));
}

function runNamedSearch() {
    const name = document.getElementById("nameFilter").value.trim();
    activeFilters = name ? { ...activeFilters, name } : removeFilterKey("name");
    currentPage = 0;
    loadTablePage().then(() => toast(name ? `Searching "${name}"` : "Name filter cleared", "success"));
}

function runAdvancedFilters() {
    activeFilters = {
        ...activeFilters,
        status: document.getElementById("statusFilter").value || undefined,
        name: document.getElementById("nameFilter").value.trim() || undefined,
        minAhi: valueOrUndefined("minAhiFilter"),
        maxAhi: valueOrUndefined("maxAhiFilter"),
        attendanceBelow: valueOrUndefined("attendanceBelowFilter"),
        internalMarksBelow: valueOrUndefined("internalBelowFilter"),
        predictedRisk: document.getElementById("predictedRiskFilter").checked ? true : undefined,
        interventionRequired: document.getElementById("interventionFilter").checked ? true : undefined
    };
    activeFilters = Object.fromEntries(
        Object.entries(activeFilters).filter(([, value]) => value !== undefined && value !== "")
    );
    currentPage = 0;
    loadTablePage().then(() => toast("Advanced filters applied", "success"));
}

function clearFilters() {
    activeFilters = {};
    ["statusFilter", "nameFilter", "minAhiFilter", "maxAhiFilter", "attendanceBelowFilter", "internalBelowFilter"]
        .forEach((id) => {
            document.getElementById(id).value = "";
        });
    document.getElementById("predictedRiskFilter").checked = false;
    document.getElementById("interventionFilter").checked = false;
    currentPage = 0;
    loadTablePage().then(() => toast("Filters cleared", "success"));
}

async function deleteByStudentId() {
    if (!ensureAuthenticatedAction()) return;
    const studentId = document.getElementById("deleteStudentId").value;
    if (!studentId) {
        toast("Enter a student id to delete.", "error");
        return;
    }
    if (!window.confirm(`Delete academic record for student id ${studentId}?`)) return;

    try {
        await request(`/api/records/student/${studentId}`, {
            method: "DELETE",
            headers: authHeaders()
        });
        toast("Deleted successfully", "success");
        await refreshDashboard();
    } catch (error) {
        toast(error.message, "error");
    }
}

async function exportCsv() {
    if (!ensureAuthenticatedAction()) return;
    try {
        setLoading(true);
        const response = await fetch("/api/records/export", { headers: authHeaders() });
        if (!response.ok) {
            throw new Error("Export failed");
        }
        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        link.download = "academic-records.csv";
        link.click();
        URL.revokeObjectURL(url);
        toast("CSV exported", "success");
    } catch (error) {
        toast(error.message, "error");
    } finally {
        setLoading(false);
    }
}

async function intervene(recordId, currentlyRequired) {
    if (!ensureAuthenticatedAction()) return;
    const notes = window.prompt(
        "Enter intervention notes",
        currentlyRequired ? "Reviewed and followed up" : "Needs intervention"
    );
    if (notes === null) return;
    try {
        await request(`/api/records/${recordId}/intervene`, {
            method: "PUT",
            headers: authJsonHeaders(),
            body: JSON.stringify({
                interventionRequired: !currentlyRequired,
                interventionNotes: notes
            })
        });
        toast(currentlyRequired ? "Marked as reviewed" : "Needs intervention", "success");
        await refreshDashboard();
    } catch (error) {
        toast(error.message, "error");
    }
}

async function initializeAiPanels() {
    if (!studentsCache.length) {
        renderAiAnalysisPlaceholder();
        renderPredictionPlaceholder();
        return;
    }

    const fallbackId = studentSelect.value || aiStudentSelect.value || String(studentsCache[0].id);
    aiStudentSelect.value = fallbackId;

    await loadAiRecordSnapshot(fallbackId);
    await analyzeStudentWithAi(true);
    await predictStudentWithAi(true);
}

async function loadAiRecordSnapshot(studentId) {
    if (!studentId) {
        aiCacheStatus.textContent = "No Student";
        aiCacheStatus.className = "inline-badge neutral-badge";
        aiSummarySource.textContent = "Choose a student to inspect the saved AI summary.";
        aiPriorityLevel.textContent = "LOW";
        aiPriorityLevel.className = "priority-pill priority-low";
        return null;
    }

    try {
        const record = await request(`/api/records/student/${studentId}`);
        const hasCachedSummary = Boolean(record.aiSummary && record.aiSummary.trim());
        aiCacheStatus.textContent = hasCachedSummary ? "Cached Summary" : "Fresh Summary";
        aiCacheStatus.className = `inline-badge ${hasCachedSummary ? "success-badge" : "neutral-badge"}`;
        aiSummarySource.textContent = hasCachedSummary
            ? "This summary is already saved on the academic record and can be reused."
            : "No saved summary was found, so a fresh AI summary will be generated.";
        updatePriorityBadge(record.priorityLevel);
        return record;
    } catch (error) {
        aiCacheStatus.textContent = "Unavailable";
        aiCacheStatus.className = "inline-badge neutral-badge";
        aiSummarySource.textContent = "Academic record details are unavailable for this student right now.";
        updatePriorityBadge("LOW");
        return null;
    }
}

async function analyzeStudentWithAi(silent = false) {
    const studentId = getSelectedAiStudentId();
    if (!studentId) {
        if (!silent) toast("Select a student to analyze.", "error");
        return;
    }

    try {
        const analysis = await request(`/api/ai/analyze/${studentId}`);
        await loadAiRecordSnapshot(studentId);
        aiSummary.textContent = analysis.summary || "No summary available.";
        aiRiskLevel.textContent = analysis.riskLevel || "UNKNOWN";
        aiRiskLevel.className = `risk-level-pill ${riskLevelClass(analysis.riskLevel)}`;
        aiConfidenceValue.textContent = formatConfidence(analysis.confidence);

        const reasons = Array.isArray(analysis.reasons) && analysis.reasons.length
            ? analysis.reasons
            : [{ type: "STABLE", message: "No specific issues detected." }];
        aiReasons.innerHTML = reasons.map((reason) => `
            <li class="reason-item">
                <span class="reason-tag">${reason.type || "GENERAL"}</span>
                <span>${reason.message || "No details available."}</span>
            </li>
        `).join("");

        const recommendations = Array.isArray(analysis.recommendations) && analysis.recommendations.length
            ? analysis.recommendations
            : ["No recommendations returned."];
        aiRecommendations.innerHTML = recommendations.map((item) => `<li>${item}</li>`).join("");

        if (!silent) {
            toast("AI analysis generated", "success");
        }
    } catch (error) {
        renderAiAnalysisError(error.message);
        if (!silent) {
            toast(error.message, "error");
        }
    }
}

async function predictStudentWithAi(silent = false) {
    const studentId = getSelectedAiStudentId();
    if (!studentId) {
        if (!silent) toast("Select a student to predict.", "error");
        return;
    }

    try {
        const prediction = await request(`/api/ai/predict/${studentId}`);
        predictionRiskScore.textContent = formatNumber(prediction.riskScore);
        predictionRiskFlag.textContent = prediction.predictedRisk ? "Yes" : "No";
        predictionRiskFlag.className = prediction.predictedRisk ? "prediction-flag danger-text" : "prediction-flag success-text";
        predictionPriority.textContent = prediction.priorityLevel || "LOW";
        predictionMessage.textContent = prediction.message || "Prediction completed.";
        updatePriorityBadge(prediction.priorityLevel || "LOW");

        if (!silent) {
            toast("Prediction updated", "success");
        }
    } catch (error) {
        renderPredictionError(error.message);
        if (!silent) {
            toast(error.message, "error");
        }
    }
}

async function askAiQuestion() {
    const question = aiChatQuestion.value.trim();
    if (!question) {
        toast("Enter a question for the assistant.", "error");
        return;
    }

    aiChatAnswer.textContent = "Thinking...";
    try {
        const response = await request("/api/ai/chat", {
            method: "POST",
            headers: authJsonHeaders(),
            body: JSON.stringify({ question })
        });
        aiChatAnswer.textContent = response.answer || "No answer returned.";
        toast("Assistant responded", "success");
    } catch (error) {
        aiChatAnswer.textContent = error.message;
        toast(error.message, "error");
    }
}

async function loadTopRiskStudents(silent = false) {
    try {
        const topRiskStudents = await request("/api/ai/top-risk");
        if (!topRiskStudents.length) {
            topRiskList.innerHTML = '<div class="empty-state small-empty">No risk data available.</div>';
            return;
        }

        topRiskList.innerHTML = topRiskStudents.map((student, index) => `
            <article class="top-risk-item">
                <div class="top-risk-rank">#${index + 1}</div>
                <div class="top-risk-content">
                    <strong>${student.studentName}</strong>
                    <span>${student.rollNumber}</span>
                </div>
                <div class="top-risk-metrics">
                    <span>Risk ${formatNumber(student.riskScore)}</span>
                    <span>AHI ${formatNumber(student.ahi)}</span>
                    <span class="priority-pill ${priorityClass(student.priorityLevel)}">${student.priorityLevel || "LOW"}</span>
                    <span class="status-pill ${statusClass(student.status)}">${student.status}</span>
                </div>
            </article>
        `).join("");
    } catch (error) {
        topRiskList.innerHTML = `<div class="empty-state small-empty">${error.message}</div>`;
        if (!silent) {
            toast(error.message, "error");
        }
    }
}

function renderRecords(records) {
    if (!records || !records.length) {
        recordsTableBody.innerHTML = '<tr><td colspan="14" class="empty-state">No academic records found.</td></tr>';
        return;
    }

    recordsTableBody.innerHTML = records.map((record) => `
        <tr>
            <td>${record.id ?? "-"}</td>
            <td>${record.student?.name ?? "-"}</td>
            <td>${record.student?.rollNumber ?? "-"}</td>
            <td>${formatNumber(record.attendancePercentage)}</td>
            <td>${formatNumber(record.internalMarks)}</td>
            <td>${record.assignmentsSubmitted ?? "-"}</td>
            <td>${record.totalAssignments ?? "-"}</td>
            <td>${formatNumber(record.ahi)}</td>
            <td><span class="status-pill ${statusClass(record.status)}">${record.status ?? "-"}</span></td>
            <td>${record.predictedRisk ? '<span class="warning-pill">Likely AT_RISK</span>' : "-"}</td>
            <td><span class="priority-pill ${priorityClass(record.priorityLevel)}">${record.priorityLevel ?? "LOW"}</span></td>
            <td>${record.interventionRequired ? '<span class="danger-pill">Needs Intervention</span>' : '<span class="success-pill">Stable</span>'}</td>
            <td>${formatDate(record.lastReviewedAt)}</td>
            <td>
                <div class="action-stack">
                    <button class="secondary-btn small-btn" onclick="intervene(${record.id}, ${record.interventionRequired})">
                        ${record.interventionRequired ? "Mark Reviewed" : "Intervene"}
                    </button>
                </div>
            </td>
        </tr>`).join("");
}

function renderKpis(records) {
    if (!records.length) {
        atRiskPercentageValue.textContent = "0%";
        avgAttendanceValue.textContent = "0.00";
        lowAverageValue.textContent = "0";
        goodAverageValue.textContent = "0";
        highAchieversValue.textContent = "0";
        lowAverageText.textContent = "No students in this section";
        goodAverageText.textContent = "No students in this section";
        highAchieversText.textContent = "No students in this section";
        return;
    }

    const lowAverage = records.filter((record) => Number(record.ahi || 0) < 50);
    const goodAverage = records.filter((record) => Number(record.ahi || 0) >= 50 && Number(record.ahi || 0) < 75);
    const highAchievers = records.filter((record) => Number(record.ahi || 0) >= 75);
    const notSafe = records.filter((record) => record.status === "AT_RISK" || record.status === "INTERVENTION_REQUIRED");
    const avgAttendance = records.reduce((sum, record) => sum + Number(record.attendancePercentage || 0), 0) / records.length;

    atRiskPercentageValue.textContent = `${((notSafe.length / records.length) * 100).toFixed(1)}%`;
    avgAttendanceValue.textContent = avgAttendance.toFixed(2);
    lowAverageValue.textContent = lowAverage.length;
    goodAverageValue.textContent = goodAverage.length;
    highAchieversValue.textContent = highAchievers.length;
    lowAverageText.textContent = summariseNames(lowAverage);
    goodAverageText.textContent = summariseNames(goodAverage);
    highAchieversText.textContent = summariseNames(highAchievers);
}

function summariseNames(records) {
    if (!records.length) return "No students in this section";
    const names = records.slice(0, 3).map((record) => record.student?.name).filter(Boolean);
    return names.join(", ") + (records.length > 3 ? ` +${records.length - 3} more` : "");
}

function renderStatusPieChart() {
    const canvas = document.getElementById("statusPieChart");
    const context = canvas.getContext("2d");
    context.clearRect(0, 0, canvas.width, canvas.height);
    statusLegend.innerHTML = "";

    const counts = {
        COMPLIANT: recordsCache.filter((record) => record.status === "COMPLIANT").length,
        AT_RISK: recordsCache.filter((record) => record.status === "AT_RISK").length,
        INTERVENTION_REQUIRED: recordsCache.filter((record) => record.status === "INTERVENTION_REQUIRED").length
    };
    const total = Object.values(counts).reduce((sum, count) => sum + count, 0);
    if (!total) {
        statusSummaryText.textContent = "No records available";
        return;
    }

    statusSummaryText.textContent = `${total} records visualized`;
    const slices = [
        { key: "COMPLIANT", color: "#0f766e" },
        { key: "AT_RISK", color: "#d97706" },
        { key: "INTERVENTION_REQUIRED", color: "#b42318" }
    ];
    let startAngle = -Math.PI / 2;

    slices.forEach((slice) => {
        const value = counts[slice.key];
        if (!value) return;
        const angle = (value / total) * Math.PI * 2;
        context.beginPath();
        context.moveTo(120, 120);
        context.arc(120, 120, 86, startAngle, startAngle + angle);
        context.closePath();
        context.fillStyle = slice.color;
        context.fill();
        startAngle += angle;

        const legendItem = document.createElement("div");
        legendItem.className = "legend-item";
        legendItem.innerHTML = `<span class="legend-swatch" style="background:${slice.color}"></span>${slice.key} (${value})`;
        statusLegend.appendChild(legendItem);
    });

    context.beginPath();
    context.arc(120, 120, 42, 0, Math.PI * 2);
    context.fillStyle = getComputedStyle(document.documentElement).getPropertyValue("--surface-strong").trim();
    context.fill();
}

function renderComparisonChart() {
    const canvas = document.getElementById("comparisonBarChart");
    const context = canvas.getContext("2d");
    context.clearRect(0, 0, canvas.width, canvas.height);
    if (!recordsCache.length) return;

    const selectedId = Number(comparisonStudentSelect.value || recordsCache[0].student.id);
    const record = recordsCache.find((item) => item.student?.id === selectedId) || recordsCache[0];
    const cohortAverage = recordsCache.reduce((sum, item) => sum + Number(item.ahi || 0), 0) / recordsCache.length;

    drawTwoBars(context, [
        { label: "Student AHI", value: Number(record.ahi || 0), color: "#0f766e" },
        { label: "Cohort Avg", value: cohortAverage, color: "#d97706" }
    ], record.student?.name || "Selected Student");
}

function renderWorstStudentsChart() {
    const canvas = document.getElementById("worstStudentsChart");
    const context = canvas.getContext("2d");
    context.clearRect(0, 0, canvas.width, canvas.height);
    const worst = [...recordsCache].sort((first, second) => Number(first.ahi || 0) - Number(second.ahi || 0)).slice(0, 10);
    if (!worst.length) return;

    const ink = getComputedStyle(document.documentElement).getPropertyValue("--ink").trim();
    worst.forEach((record, index) => {
        const y = 24 + index * 22;
        const width = Math.max(30, Number(record.ahi || 0) * 2.1);
        context.fillStyle = index < 3 ? "#b42318" : "#d97706";
        context.fillRect(96, y, width, 14);
        context.fillStyle = ink;
        context.font = "12px Segoe UI";
        context.fillText(record.student?.name?.slice(0, 10) || "Student", 10, y + 11);
        context.fillText(Number(record.ahi || 0).toFixed(1), 102 + width, y + 11);
    });
}

function renderCohortSafetyChart() {
    const canvas = document.getElementById("cohortSafetyChart");
    const context = canvas.getContext("2d");
    if (!canvas || !context) return;

    context.clearRect(0, 0, canvas.width, canvas.height);
    const totalStudents = studentsCache.length;
    if (!totalStudents) return;

    const safeStudents = recordsCache.filter((record) => record.status === "COMPLIANT").length;
    const notSafeStudents = Math.max(totalStudents - safeStudents, 0);
    const bars = [
        { label: "Total Students", value: totalStudents, color: "#4f46e5" },
        { label: "Safe", value: safeStudents, color: "#0f766e" },
        { label: "Needs Attention", value: notSafeStudents, color: "#b42318" }
    ];
    const ink = getComputedStyle(document.documentElement).getPropertyValue("--ink").trim();
    const originY = 200;
    const maxHeight = 120;
    const maxValue = Math.max(...bars.map((bar) => bar.value), 1);

    context.strokeStyle = "rgba(148, 163, 184, 0.3)";
    context.beginPath();
    context.moveTo(40, originY);
    context.lineTo(620, originY);
    context.stroke();

    bars.forEach((bar, index) => {
        const barWidth = 98;
        const gap = 90;
        const x = 70 + index * (barWidth + gap);
        const height = (bar.value / maxValue) * maxHeight;
        const y = originY - height;

        context.fillStyle = bar.color;
        context.fillRect(x, y, barWidth, height);
        context.fillStyle = ink;
        context.font = "bold 15px Segoe UI";
        context.textAlign = "center";
        context.fillText(String(bar.value), x + barWidth / 2, y - 10);
        context.font = "13px Segoe UI";
        context.fillText(bar.label, x + barWidth / 2, originY + 24);
    });

    context.textAlign = "left";
}

async function renderTrendChart() {
    const canvas = document.getElementById("trendLineChart");
    const context = canvas.getContext("2d");
    context.clearRect(0, 0, canvas.width, canvas.height);
    if (!recordsCache.length) return;

    const selectedId = Number(comparisonStudentSelect.value || recordsCache[0].student.id);
    const record = recordsCache.find((item) => item.student?.id === selectedId) || recordsCache[0];
    const history = await request(`/api/records/${record.id}/history`);
    if (!history.length) return;

    const values = history.map((item) => Number(item.ahi || 0));
    const max = Math.max(...values, 100);
    const min = Math.min(...values, 0);
    const ink = getComputedStyle(document.documentElement).getPropertyValue("--ink").trim();

    context.strokeStyle = "#0f766e";
    context.lineWidth = 3;
    context.beginPath();
    values.forEach((value, index) => {
        const x = 28 + (index * (260 / Math.max(values.length - 1, 1)));
        const y = 180 - ((value - min) / Math.max(max - min, 1)) * 130;
        if (index === 0) context.moveTo(x, y);
        else context.lineTo(x, y);
    });
    context.stroke();

    context.fillStyle = ink;
    context.font = "12px Segoe UI";
    context.fillText(record.student?.name || "Student", 12, 16);
}

function drawTwoBars(context, bars, title) {
    const originX = 46;
    const originY = 200;
    const barWidth = 72;
    const maxHeight = 140;
    const ink = getComputedStyle(document.documentElement).getPropertyValue("--ink").trim();

    context.fillStyle = ink;
    context.font = "bold 14px Segoe UI";
    context.fillText(title, 16, 18);
    context.strokeStyle = "rgba(148, 163, 184, 0.3)";
    context.beginPath();
    context.moveTo(originX - 18, originY);
    context.lineTo(280, originY);
    context.stroke();

    bars.forEach((bar, index) => {
        const height = (Math.min(bar.value, 100) / 100) * maxHeight;
        const x = originX + index * 120;
        const y = originY - height;

        context.fillStyle = bar.color;
        context.fillRect(x, y, barWidth, height);
        context.fillStyle = ink;
        context.font = "bold 13px Segoe UI";
        context.textAlign = "center";
        context.fillText(bar.value.toFixed(2), x + barWidth / 2, y - 8);
        context.font = "12px Segoe UI";
        context.fillText(bar.label, x + barWidth / 2, originY + 18);
        context.textAlign = "left";
    });
}

function changePage(delta) {
    const nextPage = currentPage + delta;
    if (nextPage < 0 || nextPage >= totalPages) return;
    currentPage = nextPage;
    loadTablePage();
}

function toggleNavSidebar() {
    const shouldOpen = !navSidebar.classList.contains("open");
    if (shouldOpen) {
        navSidebar.classList.add("open");
        sidebarOverlay.classList.remove("hidden");
        return;
    }
    closeNavSidebar();
}

function closeNavSidebar() {
    navSidebar.classList.remove("open");
    syncOverlayVisibility();
}

function openInsightsSidebar() {
    insightSidebar.classList.add("open");
    sidebarOverlay.classList.remove("hidden");
}

function closeInsightsSidebar() {
    insightSidebar.classList.remove("open");
    syncOverlayVisibility();
}

function closeAllPanels() {
    closeNavSidebar();
    closeInsightsSidebar();
    syncOverlayVisibility();
}

function syncOverlayVisibility() {
    const anyPanelOpen = navSidebar.classList.contains("open")
        || insightSidebar.classList.contains("open");
    sidebarOverlay.classList.toggle("hidden", !anyPanelOpen);
}

function syncSelectionState() {
    if (studentSelect.value) {
        comparisonStudentSelect.value = studentSelect.value;
        aiStudentSelect.value = studentSelect.value;
    }
}

function syncSelectionStateFromAi() {
    if (aiStudentSelect.value) {
        comparisonStudentSelect.value = aiStudentSelect.value;
    }
}

function resetForm() {
    recordForm.reset();
}

function removeFilterKey(key) {
    const updated = { ...activeFilters };
    delete updated[key];
    return updated;
}

function valueOrUndefined(id) {
    const value = document.getElementById(id).value;
    return value === "" ? undefined : Number(value);
}

function buildFilterUrl(filters, page, size) {
    const params = new URLSearchParams({ page, size });
    Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== "") {
            params.append(key, value);
        }
    });
    return `/api/records/filter?${params.toString()}`;
}

function statusClass(status) {
    return `status-${String(status || "").toLowerCase().replaceAll("_", "-")}`;
}

function formatNumber(value) {
    if (value === null || value === undefined || Number.isNaN(Number(value))) return "-";
    return Number(value).toFixed(2);
}

function formatConfidence(value) {
    if (value === null || value === undefined || Number.isNaN(Number(value))) return "0.00";
    const safeValue = Math.max(0, Math.min(1, Number(value)));
    return safeValue.toFixed(2);
}

function formatDate(value) {
    if (!value) return "-";
    return new Date(value).toLocaleString();
}

function riskLevelClass(riskLevel) {
    const normalised = String(riskLevel || "").toUpperCase();
    if (normalised === "HIGH") return "high";
    if (normalised === "MEDIUM") return "medium";
    if (normalised === "LOW") return "low";
    return "neutral";
}

function priorityClass(priorityLevel) {
    const normalised = String(priorityLevel || "").toUpperCase();
    if (normalised === "HIGH") return "priority-high";
    if (normalised === "MEDIUM") return "priority-medium";
    return "priority-low";
}

function updatePriorityBadge(priorityLevel) {
    const safePriority = String(priorityLevel || "LOW").toUpperCase();
    aiPriorityLevel.textContent = safePriority;
    aiPriorityLevel.className = `priority-pill ${priorityClass(safePriority)}`;
}

function getSelectedAiStudentId() {
    return aiStudentSelect.value || studentSelect.value || comparisonStudentSelect.value;
}

function renderAiAnalysisPlaceholder() {
    aiSummary.textContent = "Choose a student and run analysis to view the AI summary.";
    aiSummarySource.textContent = "Saved summaries are reused from the academic record when available.";
    aiCacheStatus.textContent = "Waiting";
    aiCacheStatus.className = "inline-badge neutral-badge";
    aiConfidenceValue.textContent = "0.00";
    aiRiskLevel.textContent = "Waiting";
    aiRiskLevel.className = "risk-level-pill neutral";
    aiPriorityLevel.textContent = "LOW";
    aiPriorityLevel.className = "priority-pill priority-low";
    aiReasons.innerHTML = "<li>No structured reasons available yet.</li>";
    aiRecommendations.innerHTML = "<li>Recommendations will appear here.</li>";
}

function renderAiAnalysisError(message) {
    aiSummary.textContent = message;
    aiSummarySource.textContent = "Unable to read the saved AI summary state.";
    aiCacheStatus.textContent = "Unavailable";
    aiCacheStatus.className = "inline-badge neutral-badge";
    aiConfidenceValue.textContent = "0.00";
    aiRiskLevel.textContent = "Unavailable";
    aiRiskLevel.className = "risk-level-pill neutral";
    aiPriorityLevel.textContent = "LOW";
    aiPriorityLevel.className = "priority-pill priority-low";
    aiReasons.innerHTML = "<li>Unable to generate structured reasons right now.</li>";
    aiRecommendations.innerHTML = "<li>Unable to generate recommendations right now.</li>";
}

function renderPredictionPlaceholder() {
    predictionRiskScore.textContent = "0.00";
    predictionRiskFlag.textContent = "No";
    predictionRiskFlag.className = "prediction-flag";
    predictionPriority.textContent = "LOW";
    predictionMessage.textContent = "Prediction details will appear here.";
}

function renderPredictionError(message) {
    predictionRiskScore.textContent = "0.00";
    predictionRiskFlag.textContent = "Unavailable";
    predictionRiskFlag.className = "prediction-flag danger-text";
    predictionPriority.textContent = "LOW";
    predictionMessage.textContent = message;
}

function showMessage(message, type) {
    messageBox.textContent = message;
    messageBox.className = `message-box ${type}`;
}

function toast(message, type) {
    const toastEl = document.createElement("div");
    toastEl.className = `toast ${type}`;
    toastEl.textContent = message;
    toastContainer.appendChild(toastEl);
    setTimeout(() => toastEl.remove(), 2600);
}

function ensureAuthenticatedAction() {
    if (!isAuthenticated()) {
        window.location.replace("/");
        return false;
    }
    return true;
}

function setLoading(isLoading) {
    loadingOverlay.classList.toggle("hidden", !isLoading);
}

function authHeaders() {
    const token = localStorage.getItem("ams_token");
    return token ? { Authorization: `Bearer ${token}` } : {};
}

function authJsonHeaders() {
    return { "Content-Type": "application/json", ...authHeaders() };
}

async function request(url, options = {}) {
    const requestOptions = { ...options };
    const headers = { ...(options.headers || {}) };
    const token = localStorage.getItem("ams_token");

    if (token && !headers.Authorization && !url.includes("/api/auth/login")) {
        headers.Authorization = `Bearer ${token}`;
    }

    requestOptions.headers = headers;

    const response = await fetch(url, requestOptions);
    if (!response.ok) {
        let errorMessage = "Request failed";
        try {
            const errorBody = await response.json();
            errorMessage = errorBody.message || errorMessage;
        } catch (error) {
            errorMessage = response.statusText || errorMessage;
        }

        if (response.status === 401) {
            handleLogout();
            throw new Error("Session expired. Please log in again.");
        }

        throw new Error(errorMessage);
    }
    if (response.status === 204) return null;
    return response.json();
}

window.intervene = intervene;
