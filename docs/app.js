const API_BASE_URL = "https://cqs.onrender.com";
const DEFAULT_QUIZ_SECONDS = 5 * 60;
const QUESTION_COUNT = 10;
const OPTION_KEYS = ["A", "B", "C", "D"];
const timerParameter = Number(new URLSearchParams(window.location.search).get("timer"));
const QUIZ_SECONDS = Number.isInteger(timerParameter) && timerParameter > 0 ? timerParameter : DEFAULT_QUIZ_SECONDS;

const form = document.querySelector("#quiz-form");
const questionList = document.querySelector("#question-list");
const nameInput = document.querySelector("#student-name");
const introPanel = document.querySelector("#cover-sheet");
const beginButton = document.querySelector("#begin-button");
const timerElement = document.querySelector("#timer");
const timerLabel = document.querySelector("#timer-label");
const timerAnnouncement = document.querySelector("#timer-announcement");
const messageElement = document.querySelector("#form-message");
const submitButton = document.querySelector("#submit-button");
const retryButton = document.querySelector("#retry-button");
const newPaperButton = document.querySelector("#new-paper-button");
const demoBanner = document.querySelector("#demo-banner");
const readyNotice = document.querySelector("#ready-notice");
const modeLabel = document.querySelector("#mode-label");
const scorePanel = document.querySelector("#score-panel");
const resultsBody = document.querySelector("#results-body");
const resultCount = document.querySelector("#result-count");

let questions = [];
let dataSource = "demo";
let serverReady = false;
let state = "intro";
let deadline = null;
let startedAt = null;
let elapsedSeconds = 0;
let previousRemaining = QUIZ_SECONDS;
let submissionAnswers = null;
let submissionAutomatic = false;
let timerHandle = null;
let healthPollHandle = null;
let liveLoadInProgress = false;
const demoResults = [];

function shuffled(items) {
  const copy = [...items];
  for (let index = copy.length - 1; index > 0; index--) {
    const swapIndex = Math.floor(Math.random() * (index + 1));
    [copy[index], copy[swapIndex]] = [copy[swapIndex], copy[index]];
  }
  return copy;
}

function randomQuestions(items) {
  return shuffled(items).slice(0, Math.min(QUESTION_COUNT, items.length));
}

function renderQuestions() {
  questionList.replaceChildren();
  questions.forEach((question, index) => {
    const fieldset = document.createElement("fieldset");
    fieldset.className = "question";
    fieldset.style.animationDelay = `${Math.min(index * 28, 250)}ms`;
    fieldset.innerHTML = `<legend class="question-title"><span class="question-number">${String(index + 1).padStart(2, "0")}</span><span></span></legend><div class="options"></div>`;
    fieldset.querySelector("legend span:last-child").textContent = question.text;
    const options = fieldset.querySelector(".options");
    OPTION_KEYS.forEach((key) => {
      const label = document.createElement("label");
      label.className = "option";
      const radio = document.createElement("input");
      radio.type = "radio";
      radio.name = `question-${question.id}`;
      radio.value = key;
      const bubble = document.createElement("span");
      bubble.className = "answer-bubble";
      bubble.setAttribute("aria-hidden", "true");
      bubble.textContent = key;
      const text = document.createElement("span");
      text.className = "option-text";
      text.textContent = question[`option${key}`];
      label.append(radio, bubble, text);
      options.append(label);
    });
    questionList.append(fieldset);
  });
  updateProgress();
}

function stopTimer() {
  if (timerHandle !== null) {
    clearInterval(timerHandle);
    timerHandle = null;
  }
}

function startTimer() {
  stopTimer();
  startedAt = Date.now();
  deadline = startedAt + QUIZ_SECONDS * 1000;
  previousRemaining = QUIZ_SECONDS;
  timerAnnouncement.textContent = "";
  updateTimer();
  timerHandle = setInterval(updateTimer, 1000);
}

function formatTime(seconds) {
  const minutes = Math.floor(seconds / 60).toString().padStart(2, "0");
  const remainder = (seconds % 60).toString().padStart(2, "0");
  return `${minutes}:${remainder}`;
}

function updateTimer() {
  if (state !== "running" || deadline === null) return;
  const secondsRemaining = Math.max(0, Math.ceil((deadline - Date.now()) / 1000));
  timerElement.textContent = formatTime(secondsRemaining);
  timerElement.dateTime = `PT${Math.floor(secondsRemaining / 60)}M${secondsRemaining % 60}S`;
  timerElement.classList.toggle("is-low", secondsRemaining <= 60);
  const crossedThresholds = [60, 30, 10].filter((threshold) => previousRemaining > threshold && secondsRemaining <= threshold);
  if (crossedThresholds.length) {
    timerAnnouncement.textContent = crossedThresholds.map((threshold) => threshold === 60 ? "1 minute remaining" : `${threshold} seconds remaining`).join(". ");
  }
  previousRemaining = secondsRemaining;
  if (secondsRemaining === 0) {
    messageElement.textContent = "Time is up. Your answers are being submitted.";
    submitQuiz(true);
  }
}

function freezeTimer() {
  elapsedSeconds = Math.min(QUIZ_SECONDS, Math.floor((Date.now() - startedAt) / 1000));
  timerLabel.textContent = "TIME USED";
  timerElement.textContent = formatTime(elapsedSeconds);
  timerElement.dateTime = `PT${Math.floor(elapsedSeconds / 60)}M${elapsedSeconds % 60}S`;
}

function updateProgress() {
  const answered = questions.filter((question) => form.querySelector(`input[name="question-${question.id}"]:checked`)).length;
  const progress = document.querySelector("#progress-count");
  if (progress) progress.textContent = `Answered ${answered} of ${questions.length}`;
  const tally = document.querySelector("#progress-tally");
  if (tally) tally.textContent = "| ".repeat(answered).trim();
}

function selectedAnswers() {
  return questions.map((question) => ({
    questionId: question.id,
    selected: form.querySelector(`input[name="question-${question.id}"]:checked`)?.value ?? null
  }));
}

function setMode(source) {
  dataSource = source;
  modeLabel.textContent = source === "demo" ? "Sample paper" : "Live";
  demoBanner.hidden = source !== "demo" || serverReady;
  readyNotice.hidden = !(serverReady && source === "demo");
}

function resetQuiz(nextQuestions, source) {
  stopTimer();
  questions = nextQuestions;
  state = "intro";
  deadline = null;
  startedAt = null;
  elapsedSeconds = 0;
  previousRemaining = QUIZ_SECONDS;
  submissionAnswers = null;
  submissionAutomatic = false;
  form.reset();
  nameInput.value = "";
  form.hidden = true;
  introPanel.hidden = false;
  scorePanel.hidden = true;
  submitButton.disabled = false;
  retryButton.hidden = true;
  newPaperButton.hidden = true;
  timerLabel.textContent = "TIME ALLOWED";
  timerElement.textContent = formatTime(QUIZ_SECONDS);
  timerElement.dateTime = `PT${Math.floor(QUIZ_SECONDS / 60)}M${QUIZ_SECONDS % 60}S`;
  timerElement.classList.remove("is-low");
  messageElement.textContent = "";
  setMode(source);
  renderQuestions();
}

function beginQuiz() {
  const studentName = nameInput.value.trim();
  if (studentName.length < 1 || studentName.length > 50) {
    nameInput.setCustomValidity("Enter a name between 1 and 50 characters.");
    nameInput.reportValidity();
    return;
  }
  nameInput.setCustomValidity("");
  nameInput.value = studentName;
  state = "running";
  introPanel.hidden = true;
  form.hidden = false;
  timerLabel.textContent = "TIME LEFT";
  messageElement.textContent = "Answer all questions. Unanswered questions score zero.";
  startTimer();
}

function showScore(response, answers) {
  document.querySelector("#score-value").textContent = `${response.score} / ${response.total}`;
  document.querySelector("#score-percent").textContent = `${Math.round(response.percentage)}%`;
  const percentage = response.percentage;
  document.querySelector("#score-copy").textContent = percentage >= 90
    ? "Excellent work."
    : percentage >= 60 ? "Good. Review the marked ones." : "Revise and try again.";
  const feedbackList = document.querySelector("#feedback-list");
  feedbackList.replaceChildren();
  response.feedback.forEach((item, index) => {
    const question = questions.find((candidate) => candidate.id === item.questionId);
    const selected = answers.find((answer) => answer.questionId === item.questionId)?.selected;
    const row = document.createElement("li");
    row.className = item.correct ? "correct" : "incorrect";
    const title = document.createElement("strong");
    title.textContent = `${String(index + 1).padStart(2, "0")}  ${question.text}`;
    const detail = document.createElement("span");
    detail.className = "feedback-result";
    detail.textContent = item.correct
      ? `Correct: ${item.correctOption}. ${question[`option${item.correctOption}`]}`
      : `Your answer: ${selected ?? "No answer"} · Correct: ${item.correctOption}. ${question[`option${item.correctOption}`]}`;
    row.append(title, detail);
    feedbackList.append(row);
  });
  scorePanel.hidden = false;
  newPaperButton.hidden = false;
}

function renderResults(results) {
  resultCount.textContent = String(results.length);
  resultsBody.replaceChildren();
  if (!results.length) {
    const row = document.createElement("tr");
    row.innerHTML = '<td class="empty-state" colspan="5">No results yet</td>';
    resultsBody.append(row);
    return;
  }
  results.slice(0, 8).forEach((result, index) => {
    const row = document.createElement("tr");
    const values = [index + 1, result.studentName, `${result.score}/${result.total}`, `${Math.round(result.score * 100 / result.total)}%`, result.takenAt ? new Date(result.takenAt).toLocaleDateString() : "-"];
    values.forEach((value) => {
      const cell = document.createElement("td");
      cell.textContent = String(value);
      row.append(cell);
    });
    resultsBody.append(row);
  });
}

async function loadResults() {
  try {
    const response = await fetch(`${API_BASE_URL}/api/results`);
    if (response.ok) renderResults(await response.json());
  } catch {
    renderResults(demoResults);
  }
}

async function sendSubmission(automatic, answers) {
  try {
    let response;
    if (dataSource === "live") {
      const httpResponse = await fetch(`${API_BASE_URL}/api/quiz/submit`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ studentName: nameInput.value, answers })
      });
      if (!httpResponse.ok) throw new Error("Quiz submission failed");
      response = await httpResponse.json();
    } else {
      const feedback = answers.map((answer) => {
        const question = questions.find((item) => item.id === answer.questionId);
        return { questionId: question.id, correct: answer.selected === question.correctOption, correctOption: question.correctOption };
      });
      const score = feedback.filter((item) => item.correct).length;
      response = { score, total: answers.length, percentage: score * 100 / answers.length, feedback };
      demoResults.unshift({ studentName: nameInput.value, score, total: answers.length, takenAt: new Date().toISOString() });
    }
    state = "submitted";
    messageElement.textContent = automatic ? "Time is up. Unanswered questions counted as incorrect." : "Your answers have been submitted.";
    form.hidden = true;
    showScore(response, answers);
    if (dataSource === "live") await loadResults();
    else renderResults(demoResults);
  } catch {
    state = "error";
    submitButton.disabled = true;
    retryButton.hidden = false;
    messageElement.textContent = "Could not reach the server. Your answers are still here; retry when ready.";
  }
}

async function submitQuiz(automatic = false) {
  stopTimer();
  if (state !== "running") return;
  state = "submitting";
  submitButton.disabled = true;
  retryButton.hidden = true;
  freezeTimer();
  submissionAutomatic = automatic;
  submissionAnswers = selectedAnswers();
  await sendSubmission(submissionAutomatic, submissionAnswers);
}

async function retrySubmission() {
  if (state !== "error") return;
  state = "submitting";
  retryButton.hidden = true;
  messageElement.textContent = "Retrying submission.";
  await sendSubmission(submissionAutomatic, submissionAnswers);
}

async function startLiveQuiz() {
  if (liveLoadInProgress) return;
  liveLoadInProgress = true;
  try {
    const response = await fetch(`${API_BASE_URL}/api/questions?count=${QUESTION_COUNT}`);
    if (!response.ok) throw new Error("Questions unavailable");
    const liveQuestions = await response.json();
    if (!liveQuestions.length) throw new Error("No questions available");
    resetQuiz(liveQuestions.slice(0, QUESTION_COUNT), "live");
    await loadResults();
  } finally {
    liveLoadInProgress = false;
  }
}

async function beginNewPaper() {
  if (serverReady && dataSource === "demo") {
    try {
      await startLiveQuiz();
      return;
    } catch {
      setMode("demo");
    }
  }
  resetQuiz(dataSource === "demo" ? randomQuestions(window.DEMO_QUESTIONS) : questions, dataSource);
}

async function pollHealth() {
  try {
    const response = await fetch(`${API_BASE_URL}/health`, { cache: "no-store" });
    if (!response.ok) return;
    serverReady = true;
    if (dataSource === "demo" && state === "intro") {
      try {
        await startLiveQuiz();
      } catch {
        setMode("demo");
        readyNotice.hidden = false;
      }
    } else if (dataSource === "demo") {
      demoBanner.hidden = true;
      readyNotice.hidden = false;
    }
  } catch {
    // Keep the sample paper available while the service starts.
  }
}

beginButton.addEventListener("click", beginQuiz);
form.addEventListener("submit", (event) => {
  event.preventDefault();
  submitQuiz(false);
});
retryButton.addEventListener("click", retrySubmission);
newPaperButton.addEventListener("click", beginNewPaper);
nameInput.addEventListener("input", () => nameInput.setCustomValidity(""));
form.addEventListener("change", updateProgress);
document.querySelector("#restart-live").addEventListener("click", async () => {
  if (state !== "intro" && state !== "submitted") return;
  try {
    await startLiveQuiz();
  } catch {
    messageElement.textContent = "The live quiz is not available yet. Try again shortly.";
  }
});
document.addEventListener("visibilitychange", () => {
  if (state === "running") updateTimer();
});
window.addEventListener("pagehide", () => {
  stopTimer();
  if (healthPollHandle !== null) {
    clearInterval(healthPollHandle);
    healthPollHandle = null;
  }
});

document.querySelector("#paper-date").textContent = new Intl.DateTimeFormat(undefined, { dateStyle: "long" }).format(new Date());
resetQuiz(randomQuestions(window.DEMO_QUESTIONS), "demo");
renderResults(demoResults);
pollHealth();
healthPollHandle = setInterval(pollHealth, 3000);