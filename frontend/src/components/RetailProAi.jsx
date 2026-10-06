import React, { useEffect, useRef, useState } from 'react';
import { askRetailProAi } from '../api/aiApi';
import '../styles/ai-assistant.css';

const SUGGESTIONS = [
  'Which products should I reorder?',
  'Which products are likely to run out of stock?',
  'Which branch is performing best?',
  'What are my top-selling products?',
  'Which products are slow-moving?',
  'Which products are near expiry?',
  'How can I reduce dead stock?',
  "Give me a summary of today's business.",
];

const RetailProAi = () => {
  const [open, setOpen] = useState(false);
  const [question, setQuestion] = useState('');
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  const endRef = useRef(null);

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, open, loading]);

  const send = async (text) => {
    const next = (text || question).trim();
    if (!next || loading) return;
    setQuestion('');
    setMessages((prev) => [...prev, { role: 'user', text: next }]);
    setLoading(true);
    try {
      const data = await askRetailProAi(next);
      setMessages((prev) => [...prev, { role: 'ai', text: data.answer }]);
    } catch (err) {
      const message = err?.response?.data?.message || 'RetailPro AI could not answer right now.';
      setMessages((prev) => [...prev, { role: 'ai', text: message }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <button type="button" className="ai-fab" onClick={() => setOpen((v) => !v)}>
        {open ? 'Close' : 'RetailPro AI'}
      </button>
      {open && (
        <section className="ai-panel" aria-label="RetailPro AI assistant">
          <header className="ai-panel-header">
            <div>
              <h2>RetailPro AI</h2>
              <p>Answers use live store data. Numbers are never invented.</p>
            </div>
            <button type="button" className="ai-close" onClick={() => setOpen(false)}>✕</button>
          </header>
          <div className="ai-suggestions">
            {SUGGESTIONS.map((item) => (
              <button key={item} type="button" className="ai-chip" onClick={() => send(item)}>
                {item}
              </button>
            ))}
          </div>
          <div className="ai-messages">
            {messages.length === 0 && (
              <p className="ai-empty">Pick a question or type your own. If the database has no matching facts, I will say so.</p>
            )}
            {messages.map((msg, index) => (
              <div key={index} className={`ai-bubble ${msg.role === 'user' ? 'ai-user' : 'ai-bot'}`}>
                {formatAiText(msg.text)}
              </div>
            ))}
            {loading && <div className="ai-bubble ai-bot">Looking up live RetailPro data…</div>}
            <div ref={endRef} />
          </div>
          <form
            className="ai-compose"
            onSubmit={(e) => {
              e.preventDefault();
              send(question);
            }}
          >
            <input
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              placeholder="Ask about stock, sales, or branches…"
            />
            <button type="submit" className="btn-primary" disabled={loading || !question.trim()}>Send</button>
          </form>
        </section>
      )}
    </>
  );
};

function formatAiText(text) {
  if (!text) return text;
  return text
    .replace(/\*\*(.*?)\*\*/g, '$1')
    .replace(/^[\t ]*[-*] /gm, '• ');
}

export default RetailProAi;
