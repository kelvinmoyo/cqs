const API_BASE_URL = "https://your-render-service.onrender.com";
const QUIZ_SECONDS = 5 * 60;
const QUESTION_COUNT = 10;
const OPTION_KEYS = ["A", "B", "C", "D"];

const form = document.querySelector("#quiz-form");
const questionList = document.querySelector("#question-list");
const nameInput = document.querySelector("#student-name");
const timerElement = document.querySelector("#timer");
const messageElement = document.querySelector("#form-message");
const submitButton = document.querySelector("#submit-button");
const demoBanner = document.querySelector("#demo-banner");
const readyNotice = document.querySelector("#ready-notice");
const modeLabel = document.querySelector("#mode-label");
const scorePanel = document.querySelector("#score-panel");
const resultsBody = document.querySelector("#results-body");
const resultCount = document.querySelector("#result-count");

let questions = [];
let dataSource = "demo";
let serverReady = false;
let quizStarted = false;
let submitted = false;
let secondsRemaining = QUIZ_SECONDS;
let timerHandle;
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
      radio.addEventListener("change", () => {
        if (!quizStarted) {
          quizStarted = true;
          messageElement.textContent = "Your round is in progress.";
        }
      });
      const text = document.createElement("span");
      text.textContent = `${key}. ${question[`option${key}`]}`;
      label.append(radio, text);
      options.append(label);
    });
    questionList.append(fieldset);
  });
}

function resetQuiz(nextQuestions, source) {
  clearInterval(timerHandle);
  questions = nextQuestions;
  dataSource = source;
  quizStarted = false;
  submitted = false;
  secondsRemaining = QUIZ_SECONDS;
  form.hidden = false;
  form.reset();
  submitButton.disabled = false;
  scorePanel.hidden = true;
  demoBanner.hidden = source !== "demo" || serverReady;
  readyNotice.hidden = !(serverReady && source === "demo");
  modeLabel.textContent = source === "demo" ? "DEMO SESSION" : "LIVE SESSION";
  messageElement.textContent = "Your five-minute round starts now.";
  renderQuestions();
  updateTimer();
  timerHandle = setInterval(tick, 1000);
}

function updateTimer() {
  const minutes = Math.floor(secondsRemaining / 60).toString().padStart(2, "0");
  const seconds = (secondsRemaining % 60).toString().padStart(2, "0");
  timerElement.textContent = `${minutes}:${seconds}`;
  timerElement.dateTime = `PT${Math.floor(secondsRemaining / 60)}M${secondsRemaining % 60}S`;
  timerElement.classList.toggle("is-low", secondsRemaining <= 30);
}

function tick() {
  if (submitted) return;
  secondsRemaining = Math.max(0, secondsRemaining - 1);
  updateTimer();
  if (secondsRemaining === 0) {
    messageElement.textContent = "Time is up. Your answers are being submitted.";
    submitQuiz(true);
  }
}

function selectedAnswers() {
  return questions.map((question) => ({
    questionId: question.id,
    selected: form.querySelector(`input[name="question-${question.id}"]:checked`)?.value ?? null
  }));
}

async function submitQuiz(automatic = false) {
  if (submitted) return;
  if (!nameInput.value.trim()) {
    messageElement.textContent = "Enter your name before submitting.";
    nameInput.focus();
    return;
  }
  submitted = true;
  clearInterval(timerHandle);
  submitButton.disabled = true;
  const answers = selectedAnswers();
  let response;
  if (dataSource === "live") {
    try {
      const httpResponse = await fetch(`${API_BASE_URL}/api/quiz/submit`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ studentName: nameInput.value.trim(), answers })
      });
      if (!httpResponse.ok) throw new Error("Quiz submission failed");
      response = await httpResponse.json();
    } catch {
      submitted = false;
      submitButton.disabled = false;
      timerHandle = setInterval(tick, 1000);
      messageElement.textContent = "Could not reach the server. Your answers are still here; try again.";
      return;
    }
  } else {
    const feedback = answers.map((answer) => {
      const question = questions.find((item) => item.id === answer.questionId);
      return { questionId: question.id, correct: answer.selected === question.correctOption, correctOption: question.correctOption };
    });
    const score = feedback.filter((item) => item.correct).length;
    response = { score, total: answers.length, percentage: score * 100 / answers.length, feedback };
    demoResults.unshift({ studentName: nameInput.value.trim(), score, total: answers.length, takenAt: new Date().toISOString() });
  }
  if (automatic) messageElement.textContent = "Time is up. Unanswered questions counted as incorrect.";
  else messageElement.textContent = "Your answers have been submitted.";
  showScore(response, answers);
  form.hidden = true;
  if (dataSource === "live") await loadResults();
  else renderResults(demoResults);
}

function showScore(response, answers) {
  document.querySelector("#score-value").textContent = `${response.score} / ${response.total}`;
  document.querySelector("#score-percent").textContent = `${Math.round(response.percentage)}%`;
  document.querySelector("#score-copy").textContent = response.score === response.total
    ? "Excellent work. Every answer is correct."
    : `${response.total - response.score} ${response.total - response.score === 1 ? "answer" : "answers"} to revisit.`;
  const feedbackList = document.querySelector("#feedback-list");
  feedbackList.replaceChildren();
  response.feedback.forEach((item, index) => {
    const question = questions.find((candidate) => candidate.id === item.questionId);
    const selected = answers[index].selected;
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
}

function renderResults(results) {
  resultCount.textContent = String(results.length);
  resultsBody.replaceChildren();
  if (!results.length) {
    const row = document.createElement("tr");
    row.innerHTML = '<td class="empty-state" colspan="3">No results yet</td>';
    resultsBody.append(row);
    return;
  }
  results.slice(0, 8).forEach((result) => {
    const row = document.createElement("tr");
    const name = document.createElement("td");
    name.textContent = result.studentName;
    const score = document.createElement("td");
    score.textContent = `${result.score}/${result.total}`;
    const date = document.createElement("td");
    date.textContent = result.takenAt ? new Date(result.takenAt).toLocaleDateString() : "-";
    row.append(name, score, date);
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

async function startLiveQuiz() {
  const response = await fetch(`${API_BASE_URL}/api/questions?count=${QUESTION_COUNT}`);
  if (!response.ok) throw new Error("Questions unavailable");
  const liveQuestions = await response.json();
  if (!liveQuestions.length) throw new Error("No questions available");
  resetQuiz(liveQuestions, "live");
  await loadResults();
}

async function pollHealth() {
  try {
    const response = await fetch(`${API_BASE_URL}/health`, { cache: "no-store" });
    if (!response.ok) return;
    serverReady = true;
    if (dataSource === "demo" && !quizStarted && !submitted) {
      await startLiveQuiz();
    } else if (dataSource === "demo") {
      demoBanner.hidden = true;
      readyNotice.hidden = false;
    }
  } catch {
    // Keep the sample quiz available while the service starts.
  }
}

form.addEventListener("submit", (event) => {
  event.preventDefault();
  submitQuiz(false);
});
nameInput.addEventListener("input", () => {
  if (nameInput.value.trim()) {
    quizStarted = true;
    messageElement.textContent = "Your round is in progress.";
  }
});
document.querySelector("#restart-live").addEventListener("click", async () => {
  try {
    await startLiveQuiz();
  } catch {
    messageElement.textContent = "The live quiz is not available yet. Try again shortly.";
  }
});

resetQuiz(randomQuestions(window.DEMO_QUESTIONS), "demo");
renderResults(demoResults);
pollHealth();
setInterval(pollHealth, 3000);