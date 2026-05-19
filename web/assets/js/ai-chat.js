(function () {
    "use strict";

    function initAiChat() {
        const widget = document.querySelector("[data-chat-widget]");
        if (!widget) {
            return;
        }

        const toggleBtn = widget.querySelector("[data-chat-toggle]");
        const panel = widget.querySelector("[data-chat-panel]");
        const closeBtn = widget.querySelector("[data-chat-close]");
        const endpoint = window.AI_CHAT_ENDPOINT || widget.dataset.endpoint || "";
        const csrfToken = window.AI_CHAT_CSRF_TOKEN || widget.dataset.csrfToken || "";
        const form = widget.querySelector("[data-chat-form]");
        const input = widget.querySelector("[data-chat-input]");
        const sendButton = widget.querySelector("[data-chat-send]");
        const stopButton = widget.querySelector("[data-chat-stop]");
        const status = widget.querySelector("[data-chat-status]");
        const messages = widget.querySelector("#chatMessages");

        const history = [];
        let abortController = null;
        let activeReplyBubble = null;

        function togglePanel() {
            const isOpen = widget.classList.toggle("is-open");
            panel.hidden = !isOpen;
            if (isOpen) {
                input.focus();
            }
        }

        function closePanel() {
            widget.classList.remove("is-open");
            panel.hidden = true;
        }

        toggleBtn.addEventListener("click", togglePanel);
        if (closeBtn) {
            closeBtn.addEventListener("click", closePanel);
        }

        function setBusy(isBusy) {
            sendButton.disabled = isBusy;
            input.disabled = isBusy;
            stopButton.hidden = !isBusy;
        }

        function setStatus(message, isError) {
            status.textContent = message || "";
            status.classList.toggle("is-error", Boolean(isError));
        }

        function autoGrow() {
            input.style.height = "auto";
            input.style.height = Math.min(input.scrollHeight, 120) + "px";
        }

        function scrollToBottom() {
            messages.scrollTop = messages.scrollHeight;
        }

        function appendMessage(role, text) {
            const item = document.createElement("article");
            item.className = "chat-message " + (role === "user" ? "chat-message--user" : "chat-message--model");

            const bubble = document.createElement("div");
            bubble.className = "chat-bubble";
            bubble.textContent = text || "";

            item.appendChild(bubble);
            messages.appendChild(item);
            scrollToBottom();
            return bubble;
        }

        function startReplyPlaceholder() {
            const bubble = appendMessage("model", "");
            bubble.classList.add("is-loading");
            bubble.innerHTML = "<span class=\"chat-typing\"><span></span><span></span><span></span></span>";
            activeReplyBubble = bubble;
        }

        function appendToken(token) {
            if (!activeReplyBubble) {
                startReplyPlaceholder();
            }
            if (activeReplyBubble.classList.contains("is-loading")) {
                activeReplyBubble.classList.remove("is-loading");
                activeReplyBubble.textContent = "";
            }
            activeReplyBubble.textContent += token;
            scrollToBottom();
        }

        function finalizeReply() {
            const finalText = activeReplyBubble ? activeReplyBubble.textContent.trim() : "";
            if (finalText) {
                history.push({ role: "model", content: finalText });
            } else if (activeReplyBubble) {
                activeReplyBubble.closest(".chat-message").remove();
                activeReplyBubble = null;
            }
        }

        function showReplyText(text) {
            if (!activeReplyBubble) {
                startReplyPlaceholder();
            }
            if (activeReplyBubble.classList.contains("is-loading")) {
                activeReplyBubble.classList.remove("is-loading");
            }
            activeReplyBubble.textContent = text || "";
            scrollToBottom();
        }

        async function fallbackChat(prompt, requestHistory) {
            const response = await fetch(endpoint + "?action=chat", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "X-CSRF-Token": csrfToken
                },
                body: JSON.stringify({
                    prompt: prompt,
                    chatHistory: requestHistory,
                    stream: false
                })
            });

            const payload = await response.json();
            if (!response.ok || !payload.success) {
                throw new Error(payload.errorMessage || "Không thể nhận phản hồi từ trợ lý AI.");
            }

            showReplyText(payload.responseText || "");
            finalizeReply();
            setStatus("Streaming lỗi, đã chuyển sang chế độ thường.");
        }

        async function streamChat(prompt, requestHistory) {
            abortController = new AbortController();
            setBusy(true);
            setStatus("Đang trả lời...");
            startReplyPlaceholder();

            try {
                const response = await fetch(endpoint + "?action=chat-stream", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "X-CSRF-Token": csrfToken
                    },
                    body: JSON.stringify({
                        prompt: prompt,
                        chatHistory: requestHistory,
                        stream: true
                    }),
                    signal: abortController.signal
                });

                if (!response.ok || !response.body) {
                    throw new Error("Yêu cầu streaming thất bại.");
                }

                const reader = response.body.getReader();
                const decoder = new TextDecoder();
                let buffer = "";
                let receivedDone = false;

                while (true) {
                    const chunk = await reader.read();
                    if (chunk.done) {
                        break;
                    }

                    buffer += decoder.decode(chunk.value, { stream: true });
                    const events = buffer.split("\n\n");
                    buffer = events.pop() || "";

                    for (let i = 0; i < events.length; i += 1) {
                        const lines = events[i].split("\n");
                        for (let j = 0; j < lines.length; j += 1) {
                            const line = lines[j];
                            if (!line.startsWith("data:")) {
                                continue;
                            }
                            const payload = JSON.parse(line.slice(5).trim());
                            if (payload.error) {
                                throw new Error(payload.error);
                            }
                            if (payload.token) {
                                appendToken(payload.token);
                            }
                            if (payload.done) {
                                receivedDone = true;
                            }
                        }
                    }

                    if (receivedDone) {
                        break;
                    }
                }

                finalizeReply();
                setStatus("");
            } catch (error) {
                if (error.name === "AbortError") {
                    finalizeReply();
                    setStatus("Đã dừng phản hồi.");
                } else if ((error.message || "").indexOf("API key") !== -1
                        || (error.message || "").indexOf("quota") !== -1
                        || (error.message || "").indexOf("billing") !== -1) {
                    if (activeReplyBubble) {
                        activeReplyBubble.closest(".chat-message").remove();
                        activeReplyBubble = null;
                    }
                    setStatus(error.message || "Không thể kết nối tới trợ lý AI.", true);
                } else {
                    try {
                        await fallbackChat(prompt, requestHistory);
                    } catch (fallbackError) {
                        if (activeReplyBubble) {
                            activeReplyBubble.closest(".chat-message").remove();
                            activeReplyBubble = null;
                        }
                        setStatus(fallbackError.message || error.message || "Không thể kết nối tới trợ lý AI.", true);
                    }
                }
            } finally {
                abortController = null;
                setBusy(false);
                input.disabled = false;
                input.focus();
                autoGrow();
            }
        }

        form.addEventListener("submit", async function (event) {
            event.preventDefault();
            const prompt = input.value.trim();
            if (!prompt || abortController) {
                return;
            }

            appendMessage("user", prompt);
            const requestHistory = history.slice(-20);
            history.push({ role: "user", content: prompt });
            input.value = "";
            autoGrow();
            await streamChat(prompt, requestHistory);
        });

        stopButton.addEventListener("click", function () {
            if (abortController) {
                abortController.abort();
            }
        });

        input.addEventListener("input", autoGrow);
        input.addEventListener("keydown", function (event) {
            if (event.key === "Enter" && !event.shiftKey) {
                event.preventDefault();
                form.requestSubmit();
            }
        });

        autoGrow();
    }

    document.addEventListener("DOMContentLoaded", initAiChat);
})();
