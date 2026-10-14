const API_KEY = import.meta.env.VITE_GEMINI_API_KEY;
const MODEL = "gemini-3.5-flash";

export const callGeminiTutor = async (prompt: string, systemInstruction?: string): Promise<string> => {
  if (!API_KEY) {
    console.error("VITE_GEMINI_API_KEY is not configured in environment variables.");
    return "AI Tutor Error: VITE_GEMINI_API_KEY is not configured. Please set it in your environment or Secrets panel.";
  }

  try {
    const url = `https://generativelanguage.googleapis.com/v1beta/models/${MODEL}:generateContent?key=${API_KEY}`;
    
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
    return `AI Tutor Error: ${error.message || "Unable to reach Gemini API."}`;
  }
};
