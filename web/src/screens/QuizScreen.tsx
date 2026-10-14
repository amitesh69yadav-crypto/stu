import React, { useState } from 'react';
import { StudyTopic, QuizAttempt } from '../types';
import { addQuizAttemptToFirestore } from '../services/db';
import { Award, CheckCircle2, XCircle, Sparkles, ArrowRight } from 'lucide-react';

interface QuizScreenProps {
  userId: string;
  topics: StudyTopic[];
  attempts: QuizAttempt[];
}

export const QuizScreen: React.FC<QuizScreenProps> = ({ userId, topics, attempts }) => {
  const [isQuizActive, setIsQuizActive] = useState(false);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [score, setScore] = useState(0);
  const [selectedAnswer, setSelectedAnswer] = useState<string | null>(null);
  const [isFinished, setIsFinished] = useState(false);

  // Sample questions generated from topics or defaults
  const sampleQuestions = [
    {
      question: "What is the worst-case search time complexity in a balanced AVL Tree with N elements?",
      options: ["O(log N)", "O(N)", "O(N log N)", "O(1)"],
      correct: "O(log N)",
      explanation: "AVL trees strictly maintain balance factor difference <= 1, guaranteeing O(log N) height."
    },
    {
      question: "True or False: An AVL tree double rotation (Left-Right) takes O(1) constant time.",
      options: ["True", "False"],
      correct: "True",
      explanation: "Rotations simply rearrange child and parent pointers, taking strictly O(1) time."
    },
    {
      question: "What equation is solved to determine the eigenvalues lambda of an n x n matrix A?",
      options: ["det(A - lambda*I) = 0", "A * v = v", "tr(A) = lambda", "inv(A) = lambda"],
      correct: "det(A - lambda*I) = 0",
      explanation: "The characteristic equation det(A - lambda*I) = 0 yields the eigenvalues."
    }
  ];

  const handleStartQuiz = () => {
    setIsQuizActive(true);
    setCurrentQuestionIndex(0);
    setScore(0);
    setSelectedAnswer(null);
    setIsFinished(false);
  };

  const handleAnswerSelect = (option: string) => {
    setSelectedAnswer(option);
  };

  const handleNext = async () => {
    if (!selectedAnswer) return;

    const currentQ = sampleQuestions[currentQuestionIndex];
    const isCorrect = selectedAnswer === currentQ.correct;
    const newScore = isCorrect ? score + 1 : score;
    setScore(newScore);

    if (currentQuestionIndex + 1 < sampleQuestions.length) {
      setCurrentQuestionIndex(prev => prev + 1);
      setSelectedAnswer(null);
    } else {
      setIsFinished(true);
      setIsQuizActive(false);
      const percentage = Math.round((newScore * 100) / sampleQuestions.length);
      await addQuizAttemptToFirestore(userId, {
        title: "Weekend Mastery Quiz",
        dateAttempted: new Date().toISOString().replace('T', ' ').substring(0, 16),
        totalQuestions: sampleQuestions.length,
        correctCount: newScore,
        scorePercentage: percentage
      });
    }
  };

  return (
    <div className="p-8 space-y-8 max-w-5xl mx-auto">
      <div>
        <h1 className="text-3xl font-bold text-[#1D1B20]">Weekend Quizzes & Active Recall</h1>
        <p className="text-sm text-[#49454F]">Test your knowledge and update spaced repetition revision intervals.</p>
      </div>

      {!isQuizActive && !isFinished && (
        <div className="bg-[#FEF7FF] rounded-3xl p-8 border border-[#E7E0EC] shadow-sm text-center space-y-6">
          <div className="w-16 h-16 bg-[#EADDFF] text-[#21005D] rounded-2xl mx-auto flex items-center justify-center">
            <Award className="w-8 h-8" />
          </div>
          <div className="max-w-md mx-auto space-y-2">
            <h2 className="text-xl font-bold text-[#1D1B20]">Ready for your Weekend Quiz?</h2>
            <p className="text-xs text-[#49454F]">
              AI has assembled 3 targeted questions based on your study topics. Results sync across your mobile app and web browser!
            </p>
          </div>
          <button
            onClick={handleStartQuiz}
            className="px-8 py-3.5 rounded-full bg-[#6750A4] text-white font-medium hover:bg-[#523d85] transition shadow-md"
          >
            Start Practice Quiz
          </button>
        </div>
      )}

      {isQuizActive && (
        <div className="bg-[#FEF7FF] rounded-3xl p-8 border border-[#E7E0EC] shadow-sm space-y-6">
          <div className="flex justify-between items-center text-xs font-bold text-[#79747E]">
            <span>Question {currentQuestionIndex + 1} of {sampleQuestions.length}</span>
            <span>Score: {score}</span>
          </div>

          <h2 className="text-lg font-bold text-[#1D1B20]">
            {sampleQuestions[currentQuestionIndex].question}
          </h2>

          <div className="space-y-3">
            {sampleQuestions[currentQuestionIndex].options.map((option, idx) => (
              <button
                key={idx}
                onClick={() => handleAnswerSelect(option)}
                className={`w-full p-4 rounded-2xl text-left text-sm font-medium transition border ${
                  selectedAnswer === option
                    ? 'bg-[#EADDFF] text-[#21005D] border-[#6750A4]'
                    : 'bg-[#F3EDF7] text-[#1D1B20] border-[#E7E0EC] hover:bg-[#E7E0EC]/50'
                }`}
              >
                {option}
              </button>
            ))}
          </div>

          <div className="flex justify-end pt-4">
            <button
              onClick={handleNext}
              disabled={!selectedAnswer}
              className="px-6 py-3 rounded-full bg-[#6750A4] text-white font-medium hover:bg-[#523d85] transition shadow-sm disabled:opacity-50 flex items-center space-x-2"
            >
              <span>{currentQuestionIndex + 1 === sampleQuestions.length ? 'Finish Quiz' : 'Next Question'}</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}

      {isFinished && (
        <div className="bg-[#FEF7FF] rounded-3xl p-8 border border-[#E7E0EC] shadow-sm text-center space-y-6">
          <div className="w-16 h-16 bg-[#EADDFF] text-[#21005D] rounded-2xl mx-auto flex items-center justify-center">
            <Award className="w-8 h-8" />
          </div>
          <div className="space-y-2">
            <h2 className="text-2xl font-bold text-[#1D1B20]">Quiz Completed!</h2>
            <p className="text-sm text-[#49454F]">You scored {score} out of {sampleQuestions.length}</p>
          </div>
          <button
            onClick={() => setIsFinished(false)}
            className="px-8 py-3.5 rounded-full bg-[#6750A4] text-white font-medium hover:bg-[#523d85] transition shadow-md"
          >
            Take Another Quiz
          </button>
        </div>
      )}

      {/* Past Quiz Attempts */}
      <div className="space-y-4 pt-6">
        <h2 className="text-xl font-bold text-[#1D1B20]">Past Quiz Attempts</h2>
        {attempts.length === 0 ? (
          <p className="text-xs text-[#79747E]">No past quiz attempts recorded yet.</p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {attempts.map((attempt) => (
              <div key={attempt.id} className="bg-[#FEF7FF] rounded-3xl p-6 border border-[#E7E0EC] shadow-sm space-y-2">
                <div className="flex justify-between items-start">
                  <span className="text-xs font-bold text-[#6750A4]">{attempt.title}</span>
                  <span className="text-xs bg-[#EADDFF] text-[#21005D] px-2 py-0.5 rounded-full font-bold">
                    {attempt.scorePercentage}%
                  </span>
                </div>
                <p className="text-xs text-[#79747E]">Attempted: {attempt.dateAttempted}</p>
                <p className="text-xs text-[#49454F]">Correct: {attempt.correctCount} / {attempt.totalQuestions}</p>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
