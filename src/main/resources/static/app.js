const messagesEl = document.getElementById("messages");
const form = document.getElementById("chat-form");
const input = document.getElementById("input");
const sendBtn = document.getElementById("send");

const SUGGESTIONS = ["Lisboa", "São Paulo", "Recife", "Tóquio", "Santa Cruz"];

/* ---------- helpers de DOM (nunca usamos innerHTML: nada digitado vira HTML) ---------- */
function el(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined && text !== null) node.textContent = text;
    return node;
}

function scrollDown() {
    messagesEl.scrollTop = messagesEl.scrollHeight;
}

function append(node) {
    messagesEl.appendChild(node);
    scrollDown();
    return node;
}

function addBubble(text, kind) {
    return append(el("div", "bubble " + kind, text));
}

function addTyping() {
    const bubble = el("div", "bubble bot typing");
    bubble.append(el("span"), el("span"), el("span"));
    return append(bubble);
}

/* ---------- botões (sugestões e desambiguação) ---------- */
function addChoices(items, { stacked = false } = {}) {
    const group = el("div", "choices" + (stacked ? " stacked" : ""));
    items.forEach(({ label, query }) => {
        const btn = el("button", "choice", label);
        btn.type = "button";
        btn.addEventListener("click", () => {
            group.querySelectorAll("button").forEach((b) => (b.disabled = true));
            sendMessage(query);
        });
        group.appendChild(btn);
    });
    return append(group);
}

/* ---------- cartão do tempo ---------- */
const val = (v, suffix = "") => (v === null || v === undefined ? "--" : v + suffix);

function stat(label, value) {
    const box = el("div", "wc-stat");
    box.append(el("div", "label", label), el("div", "value", value));
    return box;
}

function dayLabel(day, index) {
    if (index === 0) return "Hoje";
    if (index === 1) return "Amanhã";
    return day.dayOfWeek.replace("-feira", "");
}

function addWeatherCard(weather) {
    const { location, current, daily } = weather;
    const card = el("article", "weather-card");

    const place = [location.city, location.state, location.country]
        .filter((p) => p && p.trim())
        .join(", ");

    const top = el("div", "wc-top");
    top.append(el("div", "wc-place", place), el("div", "wc-sub", "Fuso horário: " + location.timezone));
    const now = el("div", "wc-now");
    now.append(
        el("div", "wc-emoji", current.emoji),
        el("div", "wc-temp", val(current.temperature, "°")),
        el("div", "wc-desc", current.description)
    );
    top.appendChild(now);
    card.appendChild(top);

    const stats = el("div", "wc-stats");
    stats.append(
        stat("🤔 Sensação", val(current.apparentTemperature, "°C")),
        stat("💧 Umidade", val(current.humidity, "%")),
        stat("💨 Vento", val(current.windSpeed, " km/h")),
        stat("🌧️ Chuva", val(current.rainProbability, "%")),
        stat("🌅 Nascer", val(current.sunrise)),
        stat("🌇 Pôr do sol", val(current.sunset))
    );
    card.appendChild(stats);

    const days = el("div", "wc-days");
    days.appendChild(el("h3", null, "Próximos dias"));

    const mins = daily.map((d) => d.temperatureMin).filter((n) => n !== null);
    const maxs = daily.map((d) => d.temperatureMax).filter((n) => n !== null);
    const lo = Math.min(...mins);
    const hi = Math.max(...maxs);
    const span = Math.max(hi - lo, 1);

    daily.forEach((d, i) => {
        const row = el("div", "wc-day");
        row.title = d.description;

        const range = el("div", "range");
        if (d.temperatureMin !== null && d.temperatureMax !== null) {
            const bar = el("i");
            bar.style.left = ((d.temperatureMin - lo) / span) * 100 + "%";
            bar.style.right = ((hi - d.temperatureMax) / span) * 100 + "%";
            range.appendChild(bar);
        }

        row.append(
            el("span", "name", dayLabel(d, i)),
            el("span", "emoji", d.emoji),
            el("span", "min", val(d.temperatureMin, "°")),
            range,
            el("span", "max", val(d.temperatureMax, "°"))
        );
        // chance de chuva no tooltip da linha (mantém a grade enxuta)
        if (d.rainProbability !== null && d.rainProbability !== undefined) {
            row.title = d.description + " · chuva: " + d.rainProbability + "%";
        }
        days.appendChild(row);
    });
    card.appendChild(days);

    if (current.emoji === "🌙") card.classList.add("night");
    return append(card);
}

/* ---------- renderização da resposta do bot ---------- */
function renderBotResponse(data) {
    switch (data.type) {
        case "WEATHER":
            if (data.weather) {
                addWeatherCard(data.weather);
            } else {
                addBubble(data.message, "bot");
            }
            break;
        case "DISAMBIGUATION":
            addBubble("📍 Encontrei várias cidades com esse nome. Qual delas?", "bot");
            if (data.options && data.options.length) {
                addChoices(data.options, { stacked: true });
            } else {
                addBubble(data.message, "bot");
            }
            break;
        case "WELCOME":
            addBubble(data.message, "bot");
            addChoices(SUGGESTIONS.map((c) => ({ label: c, query: c })));
            break;
        default: // NOT_FOUND e qualquer tipo futuro
            addBubble(data.message, "bot");
    }
}

/* ---------- chamadas à API ---------- */
async function loadWelcome() {
    try {
        const res = await fetch("/api/chat/welcome");
        renderBotResponse(await res.json());
    } catch (e) {
        addBubble("Não consegui conectar ao servidor.", "bot error");
    }
}

async function sendMessage(text) {
    addBubble(text, "user");
    const typing = addTyping();
    sendBtn.disabled = true;

    try {
        const res = await fetch("/api/chat", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ message: text })
        });
        const data = await res.json();
        typing.remove();

        if (!res.ok) {
            // Erros do GlobalExceptionHandler (400, 503...) trazem "message"
            addBubble("⚠️ " + (data.message || "Ocorreu um erro."), "bot error");
        } else {
            renderBotResponse(data);
        }
    } catch (e) {
        typing.remove();
        addBubble("⚠️ Não foi possível falar com o servidor.", "bot error");
    } finally {
        sendBtn.disabled = false;
        input.focus();
    }
}

form.addEventListener("submit", (event) => {
    event.preventDefault();
    const text = input.value.trim();
    if (!text) return;
    input.value = "";
    sendMessage(text);
});

loadWelcome();
