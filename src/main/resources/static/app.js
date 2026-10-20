const scanForm = document.getElementById("scanForm");
const contentInput = document.getElementById("content");
const scanButton = document.getElementById("scanButton");
const message = document.getElementById("message");
const charCount = document.getElementById("charCount");

const resultSection = document.getElementById("resultSection");
const scoreElement = document.getElementById("score");
const riskLevelElement = document.getElementById("riskLevel");
const reasonsElement = document.getElementById("reasons");

const historyList = document.getElementById("historyList");

contentInput.addEventListener("input", () => {
    charCount.textContent = `${contentInput.value.length} / 5000`;
});

scanForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const content = contentInput.value.trim();

    if (!content) {
        message.textContent = "Please enter a message or URL.";
        return;
    }

    if (content.length > 5000) {
        message.textContent = "Input must be 5000 characters or fewer.";
        return;
    }

    scanButton.disabled = true;
    scanButton.textContent = "Analyzing...";
    message.textContent = "";

    try {
        const response = await fetch("/api/scan", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ content })
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(
                errorData.message || "Unable to analyze the content."
            );
        }

        const result = await response.json();

        displayResult(result);
        message.textContent = "Analysis completed successfully.";
        message.style.color = "#16804a";

        await loadDashboard();

    } catch (error) {
        message.style.color = "#b54708";
        message.textContent =
            error.message === "Failed to fetch"
                ? "Cannot connect to the server. Check that ScamShield is running."
                : error.message;

    } finally {
        scanButton.disabled = false;
        scanButton.textContent = "Analyze content →";
    }
});

function displayResult(result) {
    resultSection.classList.remove("hidden");

    scoreElement.textContent = result.score;
    riskLevelElement.textContent = result.riskLevel;

    riskLevelElement.className = "risk-badge";

    if (result.riskLevel === "HIGH") {
        riskLevelElement.classList.add("high");
    } else if (result.riskLevel === "MEDIUM") {
        riskLevelElement.classList.add("medium");
    }

    reasonsElement.replaceChildren();

    for (const reason of result.reasons || []) {
        const item = document.createElement("li");
        item.textContent = reason;
        reasonsElement.appendChild(item);
    }

    resultSection.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}

async function loadDashboard() {
    await Promise.all([
        loadAnalytics(),
        loadHistory()
    ]);
}

async function loadAnalytics() {
    try {
        const response = await fetch("/api/analytics");

        if (!response.ok) {
            throw new Error("Could not load analytics.");
        }

        const data = await response.json();

        document.getElementById("totalScans").textContent =
            data.total ?? 0;

        document.getElementById("highScans").textContent =
            data.high ?? 0;

        document.getElementById("mediumScans").textContent =
            data.medium ?? 0;

        document.getElementById("lowScans").textContent =
            data.low ?? 0;

    } catch (error) {
        console.error("Analytics error:", error);
    }
}

async function loadHistory() {
    try {
        const response = await fetch("/api/history");

        if (!response.ok) {
            throw new Error("Could not load scan history.");
        }

        const scans = await response.json();

        historyList.replaceChildren();

        if (scans.length === 0) {
            const emptyMessage = document.createElement("p");
            emptyMessage.className = "empty-history";
            emptyMessage.textContent = "No scans yet. Analyze your first message above.";
            historyList.appendChild(emptyMessage);
            return;
        }

        for (const scan of scans) {
            const item = document.createElement("div");
            item.className = "history-item";

            const details = document.createElement("div");
            details.className = "history-content";

            const text = document.createElement("p");
            text.className = "history-text";
            text.textContent = scan.content;

            const time = document.createElement("p");
            time.className = "history-time";
            time.textContent = `Scanned: ${scan.scannedAt}`;

            details.appendChild(text);
            details.appendChild(time);

            const risk = document.createElement("span");
            risk.className = `history-risk ${String(scan.riskLevel).toLowerCase()}`;
            risk.textContent = `${scan.riskLevel} · ${scan.score}/100`;

            item.appendChild(details);
            item.appendChild(risk);
            historyList.appendChild(item);
        }

    } catch (error) {
        historyList.textContent = "Unable to load scan history.";
        console.error("History error:", error);
    }
}

document.getElementById("refreshButton").addEventListener("click", loadDashboard);

// Load saved dashboard data when the page opens.
loadDashboard();
