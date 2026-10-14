import React, { useState } from 'react';
import { callGeminiTutor } from '../services/gemini';
import { MessageSquareText, Send, Sparkles, Bot, User } from 'lucide-react';

interface Message {
  role: 'user' | 'model';
  text: string;
}

export const AiTutorScreen: React.FC = () => {
  const [messages, setMessages] = useState<Message[]>([
    { role: 'model', text: 'Hello! I am your Gemini AI Study Tutor. Ask me any academic question, request explanations of complex topics, or paste study material for summarization!' }
  ]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [persona, setPersona] = useState('Friendly Academic Coach');

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim() || isLoading) return;

    const userMsg = input.trim();
    setInput('');
    setMessages(prev => [...prev, { role: 'user', text: userMsg }]);
    setIsLoading(true);

    const systemPrompt = `You are StudyPilot AI, an expert academic tutor. Persona: ${persona}. Provide clear, well-structured, formatted answers using Markdown.`;
    const fullPrompt = messages.map(m => `${m.role}: ${m.text}`).join('\n') + `\nuser: ${userMsg}`;

    const responseText = await callGeminiTutor(fullPrompt, systemPrompt);
    setMessages(prev => [...prev, { role: 'model', text: responseText }]);
    setIsLoading(false);
  };

  return (
    <div className="p-8 h-[calc(100vh-2rem)] flex flex-col max-w-5xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-3xl font-bold text-[#1D1B20]">Gemini AI Tutor</h1>
          <p className="text-sm text-[#49454F]">Instant answers, concept explanations, and study guidance.</p>
        </div>
        <select
          value={persona}
          onChange={(e) => setPersona(e.target.value)}
          className="px-4 py-2.5 rounded-full bg-[#FEF7FF] border border-[#79747E]/30 text-xs font-semibold text-[#1D1B20] shadow-sm"
        >
          <option value="Friendly Academic Coach">Friendly Academic Coach</option>
          <option value="Strict & Precise Professor">Strict & Precise Professor</option>
          <option value="Socratic Guide (Asks guiding questions)">Socratic Guide</option>
        </select>
      </div>

      {/* Chat Container */}
      <div className="flex-1 bg-[#FEF7FF] rounded-3xl border border-[#E7E0EC] shadow-sm p-6 overflow-y-auto space-y-6 mb-4">
        {messages.map((msg, index) => (
          <div key={index} className={`flex items-start space-x-3 ${msg.role === 'user' ? 'flex-row-reverse space-x-reverse' : ''}`}>
            <div className={`w-9 h-9 rounded-2xl flex items-center justify-center shrink-0 ${
              msg.role === 'user' ? 'bg-[#6750A4] text-white' : 'bg-[#EADDFF] text-[#21005D]'
            }`}>
              {msg.role === 'user' ? <User className="w-5 h-5" /> : <Bot className="w-5 h-5" />}
            </div>
            <div className={`max-w-[75%] p-4 rounded-2xl text-sm leading-relaxed ${
              msg.role === 'user' ? 'bg-[#6750A4] text-white rounded-tr-none' : 'bg-[#F3EDF7] text-[#1D1B20] rounded-tl-none'
            }`}>
              <div className="whitespace-pre-wrap">{msg.text}</div>
            </div>
          </div>
        ))}
        {isLoading && (
          <div className="flex items-center space-x-3">
            <div className="w-9 h-9 rounded-2xl bg-[#EADDFF] text-[#21005D] flex items-center justify-center">
              <Bot className="w-5 h-5 animate-pulse" />
            </div>
            <div className="bg-[#F3EDF7] px-4 py-3 rounded-2xl text-xs text-[#79747E] animate-pulse">
              Gemini AI is thinking...
            </div>
          </div>
        )}
      </div>

      {/* Input Bar */}
      <form onSubmit={handleSend} className="flex items-center space-x-3 bg-[#FEF7FF] p-3 rounded-full border border-[#E7E0EC] shadow-sm">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Ask a question about your study topics..."
          className="flex-1 px-4 py-2 bg-transparent text-sm text-[#1D1B20] focus:outline-none"
        />
        <button
          type="submit"
          disabled={isLoading || !input.trim()}
          className="p-3 rounded-full bg-[#6750A4] text-white hover:bg-[#523d85] transition shadow-md disabled:opacity-50"
        >
          <Send className="w-5 h-5" />
        </button>
      </form>
    </div>
  );
};
