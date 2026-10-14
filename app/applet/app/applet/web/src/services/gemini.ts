const MODEL = "gemini-2.5-flash";

export const callGeminiTutor = async (prompt: string, systemInstruction?: string): Promise<string> => {
  const customKey = typeof window !== 'undefined' ? localStorage.getItem('custom_gemini_api_key') || '' : '';
  const envKey = import.meta.env.VITE_GEMINI_API_KEY || '';
  const apiKey = (customKey || envKey).trim();

  if (!apiKey) {
    return `### StudyPilot AI Tutor (Offline Academic Mode)

I am operating in offline study assistant mode. To enable live Gemini AI generation:
1. Set \`VITE_GEMINI_API_KEY\` in your environment or Secrets panel.
2. Or configure your API key in the Settings section.

**Study Tip**: Break your problem down into fundamental equations and step-by-step proofs before checking solutions!`;
  }

  try {
    const url = `https://generativelanguage.googleapis.com/v1beta/models/${MODEL}:generateContent?key=${apiKey}`;
    
    const body: any = {
      contents: [
        {
          parts: [{ text: prompt }]
        }
      ]
    };

    if (systemInstruction) {
      body.systemInstruction = {
        parts: [{ text: systemInstruction }]
      };
    }

    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(body)
    });

    const data = await response.json();

    if (data.candidates && data.candidates[0]?.content?.parts[0]?.text) {
      return data.candidates[0].content.parts[0].text;
    } else {
      throw new Error(data.error?.message || "Failed to generate response from Gemini AI.");
    }
  } catch (error: any) {
    console.error("Gemini API Error:", error);
    return `AI Tutor Notice: ${error.message || "Unable to reach Gemini API. Please verify your API key."}`;
  }
};
