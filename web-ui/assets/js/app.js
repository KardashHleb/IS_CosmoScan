(function () {
  "use strict";

  const DEFAULT_GATEWAY = "http://localhost:8090";
  const SUBMISSIONS_STORAGE_KEY = "cosmoscan_submissions";

  const els = {
    apiBase: document.getElementById("apiBaseUrl"),
    submitForm: document.getElementById("submitWorkForm"),
    studentName: document.getElementById("studentFullName"),
    workFile: document.getElementById("workFile"),
    submitResult: document.getElementById("submitResult"),
    submissionsHistory: document.getElementById("submissionsHistory"),
    submissionsTableBody: document.getElementById("submissionsTableBody"),
    workIdReports: document.getElementById("workIdReports"),
    btnFetchReports: document.getElementById("btnFetchReports"),
    reportsResult: document.getElementById("reportsResult"),
    workIdWordCloud: document.getElementById("workIdWordCloud"),
    btnWordCloud: document.getElementById("btnWordCloud"),
    wordCloudResult: document.getElementById("wordCloudResult"),
    wordCloudCanvas: document.getElementById("wordCloudCanvas"),
    wordCloudMessage: document.getElementById("wordCloudMessage"),
  };

  function gatewayUrl() {
    const raw = (els.apiBase?.value || DEFAULT_GATEWAY).trim().replace(/\/$/, "");
    return raw || DEFAULT_GATEWAY;
  }

  function swaggerLinks() {
    const g = gatewayUrl();
    return {
      submission: g + "/swagger/submission/swagger-ui/index.html",
      files: g + "/swagger/files/swagger-ui/index.html",
      analysis: g + "/swagger/analysis/swagger-ui/index.html",
    };
  }

  function setResult(panel, data, isError) {
    if (!panel) return;
    panel.classList.add("visible");
    panel.classList.toggle("error", !!isError);
    if (panel.tagName === "DETAILS") {
      panel.open = false;
    }
    const pre = panel.querySelector("pre");
    if (pre) {
      pre.textContent =
        typeof data === "string" ? data : JSON.stringify(data, null, 2);
    }
  }

  function formatInstant(value) {
    if (!value) return "—";
    try {
      return new Date(value).toLocaleString("ru-RU");
    } catch {
      return String(value);
    }
  }

  function escapeHtml(text) {
    return String(text)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  async function fetchJson(url, options) {
    const response = await fetch(url, options);
    const text = await response.text();
    let body;
    try {
      body = text ? JSON.parse(text) : null;
    } catch {
      body = text;
    }
    if (!response.ok) {
      const err = new Error(
        "HTTP " + response.status + ": " + (typeof body === "object" ? JSON.stringify(body) : text)
      );
      err.status = response.status;
      err.body = body;
      throw err;
    }
    return body;
  }

  function syncSwaggerHrefs() {
    const links = swaggerLinks();
    document.querySelectorAll("[data-swagger]").forEach(function (a) {
      const key = a.getAttribute("data-swagger");
      if (links[key]) {
        a.href = links[key];
        const span = a.querySelector(".link-url");
        if (span) span.textContent = links[key];
      }
    });
  }

  function loadSubmissions() {
    try {
      const raw = sessionStorage.getItem(SUBMISSIONS_STORAGE_KEY);
      const list = raw ? JSON.parse(raw) : [];
      return Array.isArray(list) ? list : [];
    } catch {
      return [];
    }
  }

  function saveSubmissions(list) {
    sessionStorage.setItem(SUBMISSIONS_STORAGE_KEY, JSON.stringify(list));
  }

  function fillWorkIdFields(workId) {
    if (!workId) return;
    if (els.workIdReports) {
      els.workIdReports.value = workId;
    }
    if (els.workIdWordCloud) {
      els.workIdWordCloud.value = workId;
    }
  }

  function addSubmission(entry) {
    const list = loadSubmissions();
    list.unshift(entry);
    saveSubmissions(list);
    renderSubmissionsTable();
  }

  function renderSubmissionsTable() {
    if (!els.submissionsTableBody) return;
    const list = loadSubmissions();
    if (els.submissionsHistory) {
      els.submissionsHistory.hidden = list.length === 0;
    }
    if (!list.length) {
      els.submissionsTableBody.innerHTML = "";
      return;
    }

    els.submissionsTableBody.innerHTML = list
      .map(function (row) {
        return (
          "<tr data-work-id=\"" +
          escapeHtml(row.id) +
          "\" tabindex=\"0\" role=\"button\" title=\"Подставить ID в UC-2 и UC-3\">" +
          "<td>" +
          escapeHtml(row.studentFullName) +
          "</td>" +
          "<td><code class=\"work-id-cell\">" +
          escapeHtml(row.id) +
          "</code></td>" +
          "<td>" +
          escapeHtml(row.originalFileName) +
          "</td>" +
          "<td>" +
          escapeHtml(row.submittedAt) +
          "</td>" +
          "</tr>"
        );
      })
      .join("");

    els.submissionsTableBody.querySelectorAll("tr[data-work-id]").forEach(function (tr) {
      tr.addEventListener("click", function () {
        const workId = tr.getAttribute("data-work-id");
        fillWorkIdFields(workId);
        tr.classList.add("is-selected");
        els.submissionsTableBody.querySelectorAll("tr.is-selected").forEach(function (other) {
          if (other !== tr) {
            other.classList.remove("is-selected");
          }
        });
      });
      tr.addEventListener("keydown", function (e) {
        if (e.key === "Enter" || e.key === " ") {
          e.preventDefault();
          tr.click();
        }
      });
    });
  }

  if (els.submitForm) {
    els.submitForm.addEventListener("submit", async function (e) {
      e.preventDefault();
      const name = els.studentName?.value?.trim();
      const file = els.workFile?.files?.[0];
      if (!name || !file) {
        setResult(els.submitResult, "Укажите ФИО и выберите файл.", true);
        return;
      }

      const formData = new FormData();
      formData.append("studentFullName", name);
      formData.append("file", file);

      const btn = els.submitForm.querySelector('button[type="submit"]');
      if (btn) btn.disabled = true;

      try {
        const data = await fetchJson(gatewayUrl() + "/works", {
          method: "POST",
          body: formData,
        });
        const summary = {
          id: data.id,
          studentFullName: data.studentFullName,
          submittedAt: formatInstant(data.submittedAt),
          originalFileName: data.originalFileName,
          fileId: data.fileId,
          technicalReport: data.technicalReport,
        };
        setResult(els.submitResult, summary, false);
        fillWorkIdFields(data.id);
        addSubmission({
          id: data.id,
          studentFullName: data.studentFullName,
          originalFileName: data.originalFileName,
          submittedAt: formatInstant(data.submittedAt),
        });
        if (els.workFile) {
          els.workFile.value = "";
        }
        document.getElementById("word-cloud-section")?.scrollIntoView({ behavior: "smooth" });
      } catch (err) {
        setResult(els.submitResult, err.body || err.message, true);
      } finally {
        if (btn) btn.disabled = false;
      }
    });
  }

  if (els.btnFetchReports) {
    els.btnFetchReports.addEventListener("click", async function () {
      const workId = els.workIdReports?.value?.trim();
      if (!workId) {
        setResult(els.reportsResult, "Введите UUID работы.", true);
        return;
      }
      els.btnFetchReports.disabled = true;
      try {
        const data = await fetchJson(
          gatewayUrl() + "/works/" + encodeURIComponent(workId) + "/reports"
        );
        setResult(els.reportsResult, data, false);
      } catch (err) {
        setResult(els.reportsResult, err.body || err.message, true);
      } finally {
        els.btnFetchReports.disabled = false;
      }
    });
  }

  function renderWordCloud(terms) {
    if (!els.wordCloudCanvas || typeof WordCloud === "undefined") return;
    const list = (terms || []).map(function (t) {
      return [t.text, t.weight];
    });
    if (!list.length) {
      els.wordCloudCanvas.style.display = "none";
      if (els.wordCloudMessage) {
        els.wordCloudMessage.style.display = "block";
        els.wordCloudMessage.textContent = "Нет терминов для отображения.";
      }
      return;
    }
    els.wordCloudCanvas.width = Math.min(800, els.wordCloudCanvas.parentElement?.clientWidth || 800);
    els.wordCloudCanvas.height = 360;
    els.wordCloudCanvas.style.display = "block";
    if (els.wordCloudMessage) els.wordCloudMessage.style.display = "none";

    WordCloud(els.wordCloudCanvas, {
      list: list,
      gridSize: 8,
      weightFactor: function (size) {
        return Math.pow(size, 0.65) * 6;
      },
      fontFamily: "Segoe UI, system-ui, sans-serif",
      color: function () {
        const wordCloudColorHues = [175, 200, 260, 300];
        const hue = wordCloudColorHues[Math.floor(Math.random() * wordCloudColorHues.length)];
        return "hsl(" + hue + ", 70%, 65%)";
      },
      rotateRatio: 0.35,
      rotationSteps: 2,
      backgroundColor: "rgba(10, 14, 26, 0)",
    });
  }

  if (els.btnWordCloud) {
    els.btnWordCloud.addEventListener("click", async function () {
      const workId = els.workIdWordCloud?.value?.trim();
      if (!workId) {
        setResult(els.wordCloudResult, "Введите UUID работы.", true);
        return;
      }
      els.btnWordCloud.disabled = true;
      els.btnWordCloud.textContent = "Загрузка…";
      try {
        const data = await fetchJson(
          gatewayUrl() + "/works/" + encodeURIComponent(workId) + "/word-cloud"
        );
        setResult(els.wordCloudResult, data, false);
        if (data.status === "READY" && data.terms?.length) {
          renderWordCloud(data.terms);
        } else {
          if (els.wordCloudCanvas) els.wordCloudCanvas.style.display = "none";
          if (els.wordCloudMessage) {
            els.wordCloudMessage.style.display = "block";
            els.wordCloudMessage.textContent =
              data.status === "NO_TEXT"
                ? "Текст в файле не извлечён — облако недоступно."
                : data.status === "FAILED"
                  ? "Ошибка при построении облака слов."
                  : "Статус: " + data.status;
          }
        }
      } catch (err) {
        setResult(els.wordCloudResult, err.body || err.message, true);
        if (els.wordCloudCanvas) els.wordCloudCanvas.style.display = "none";
        if (els.wordCloudMessage) els.wordCloudMessage.style.display = "none";
      } finally {
        els.btnWordCloud.disabled = false;
        els.btnWordCloud.textContent = "Получить облако слов";
      }
    });
  }

  if (els.apiBase) {
    els.apiBase.addEventListener("change", syncSwaggerHrefs);
    els.apiBase.addEventListener("blur", syncSwaggerHrefs);
  }

  syncSwaggerHrefs();
  renderSubmissionsTable();
})();
