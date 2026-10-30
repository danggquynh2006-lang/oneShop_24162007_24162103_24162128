(() => {
    const root = document.querySelector('[data-beauty-assistant]');
    if (!root) return;
    const toggle = root.querySelector('[data-assistant-toggle]');
    const panel = root.querySelector('[data-assistant-panel]');
    const close = root.querySelector('[data-assistant-close]');
    const messages = root.querySelector('[data-assistant-messages]');
    const form = root.querySelector('[data-assistant-form]');
    const input = root.querySelector('[data-assistant-input]');
    const send = root.querySelector('[data-assistant-send]');
    const status = root.querySelector('[data-assistant-status]');
    const history = [];
    let configured = false;

    function addMessage(text, role, error = false) {
        const bubble = document.createElement('div');
        bubble.className = `beauty-assistant-message ${role}${error ? ' error' : ''}`;
        const displayText = role === 'bot'
            ? text.replace(/\*\*(.+?)\*\*/g, '$1').replace(/^\s*[-*]\s+/gm, '• ')
            : text;
        bubble.textContent = displayText;
        messages.appendChild(bubble);
        messages.scrollTop = messages.scrollHeight;
        return bubble;
    }

    async function loadStatus() {
        try {
            const response = await fetch('/api/beauty-assistant/status');
            const data = await response.json();
            configured = Boolean(data.configured);
            status.textContent = configured
                ? 'Gemini đang sẵn sàng · Gợi ý dựa trên sản phẩm của OneShop'
                : 'Chưa cấu hình Gemini API key trên máy chủ';
        } catch (_) {
            status.textContent = 'Không kiểm tra được trạng thái kết nối Gemini';
        }
    }

    function openPanel() {
        panel.hidden = false;
        toggle.setAttribute('aria-expanded', 'true');
        input.focus();
    }

    toggle.addEventListener('click', () => panel.hidden ? openPanel() : closePanel());
    close.addEventListener('click', closePanel);
    function closePanel() {
        panel.hidden = true;
        toggle.setAttribute('aria-expanded', 'false');
    }

    root.querySelectorAll('[data-assistant-prompt]').forEach(button => {
        button.addEventListener('click', () => {
            input.value = button.dataset.assistantPrompt;
            input.focus();
        });
    });

    form.addEventListener('submit', async event => {
        event.preventDefault();
        const message = input.value.trim();
        if (!message || send.disabled) return;
        addMessage(message, 'user');
        input.value = '';
        send.disabled = true;
        send.innerHTML = '<span class="spinner-border spinner-border-sm" aria-label="Đang gửi"></span>';
        const waiting = addMessage('Đang xem sản phẩm phù hợp…', 'bot');
        try {
            const response = await fetch('/api/beauty-assistant/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message, history })
            });
            const data = await response.json();
            waiting.remove();
            if (!response.ok) {
                addMessage(data.error || 'Trợ lý chưa trả lời được. Bạn thử lại nhé.', 'bot', true);
                return;
            }
            addMessage(data.reply, 'bot');
            history.push({ role: 'user', text: message }, { role: 'model', text: data.reply });
            if (history.length > 12) history.splice(0, history.length - 12);
        } catch (_) {
            waiting.remove();
            addMessage('Mất kết nối tới OneShop. Bạn kiểm tra mạng rồi thử lại nhé.', 'bot', true);
        } finally {
            send.disabled = false;
            send.innerHTML = '<i class="bi bi-send-fill" aria-hidden="true"></i>';
            input.focus();
        }
    });

    input.addEventListener('keydown', event => {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            form.requestSubmit();
        }
    });
    loadStatus();
})();
