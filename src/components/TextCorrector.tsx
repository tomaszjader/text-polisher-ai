import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Card } from "@/components/ui/card";
import { toast } from "sonner";
import { Loader2, Copy, Settings, Wand2 } from "lucide-react";

interface TextCorrectorProps {
  initialText?: string;
}

const TextCorrector = ({ initialText = "" }: TextCorrectorProps) => {
  const [inputText, setInputText] = useState(initialText);
  const [correctedText, setCorrectedText] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [apiKey, setApiKey] = useState("");
  const [showSettings, setShowSettings] = useState(false);

  useEffect(() => {
    const savedKey = localStorage.getItem("openai_api_key");
    if (savedKey) {
      setApiKey(savedKey);
    } else {
      setShowSettings(true);
    }
  }, []);

  useEffect(() => {
    if (initialText) {
      setInputText(initialText);
    }
  }, [initialText]);

  const saveApiKey = () => {
    if (!apiKey.trim()) {
      toast.error("Podaj klucz API");
      return;
    }
    localStorage.setItem("openai_api_key", apiKey.trim());
    setShowSettings(false);
    toast.success("Klucz API zapisany");
  };

  const correctText = async () => {
    if (!inputText.trim()) {
      toast.error("Wpisz tekst do poprawy");
      return;
    }

    if (!apiKey) {
      toast.error("Najpierw ustaw klucz API");
      setShowSettings(true);
      return;
    }

    setIsLoading(true);
    setCorrectedText("");

    try {
      const response = await fetch("https://api.openai.com/v1/chat/completions", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${apiKey}`,
        },
        body: JSON.stringify({
          model: "gpt-4o-mini",
          messages: [
            {
              role: "system",
              content: "Jesteś asystentem korygującym błędy ortograficzne i gramatyczne w języku polskim. Popraw tekst zachowując jego oryginalny styl i znaczenie. Zwróć tylko poprawiony tekst bez dodatkowych komentarzy.",
            },
            {
              role: "user",
              content: inputText,
            },
          ],
          temperature: 0.3,
          max_tokens: 2000,
        }),
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error?.message || "Błąd API");
      }

      const data = await response.json();
      const corrected = data.choices[0]?.message?.content || "";
      setCorrectedText(corrected);
      toast.success("Tekst poprawiony!");
    } catch (error) {
      console.error("Error:", error);
      toast.error(error instanceof Error ? error.message : "Nie udało się poprawić tekstu");
    } finally {
      setIsLoading(false);
    }
  };

  const copyToClipboard = async () => {
    if (!correctedText) return;
    
    try {
      await navigator.clipboard.writeText(correctedText);
      toast.success("Skopiowano do schowka");
    } catch (error) {
      toast.error("Nie udało się skopiować");
    }
  };

  if (showSettings) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center p-4">
        <Card className="w-full max-w-md p-6 space-y-4">
          <div className="space-y-2">
            <h2 className="text-2xl font-bold text-foreground">Ustawienia API</h2>
            <p className="text-sm text-muted-foreground">
              Wprowadź swój klucz API OpenAI. Zostanie zapisany lokalnie na urządzeniu.
            </p>
          </div>
          <Textarea
            placeholder="sk-..."
            value={apiKey}
            onChange={(e) => setApiKey(e.target.value)}
            className="min-h-[100px] font-mono text-sm"
          />
          <Button onClick={saveApiKey} className="w-full bg-gradient-primary">
            Zapisz klucz
          </Button>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background p-4 pb-20">
      <div className="max-w-2xl mx-auto space-y-4 pt-6">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-3xl font-bold bg-gradient-primary bg-clip-text text-transparent">
            Korektor
          </h1>
          <Button
            variant="outline"
            size="icon"
            onClick={() => setShowSettings(true)}
          >
            <Settings className="h-5 w-5" />
          </Button>
        </div>

        <Card className="p-4 space-y-4 shadow-soft">
          <div className="space-y-2">
            <label className="text-sm font-medium text-foreground">
              Tekst do poprawy
            </label>
            <Textarea
              placeholder="Wklej lub wpisz tekst z błędami..."
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              className="min-h-[150px] resize-none"
            />
          </div>

          <Button
            onClick={correctText}
            disabled={isLoading || !inputText.trim()}
            className="w-full bg-gradient-primary hover:opacity-90 transition-smooth"
          >
            {isLoading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Poprawiam...
              </>
            ) : (
              <>
                <Wand2 className="mr-2 h-4 w-4" />
                Popraw tekst
              </>
            )}
          </Button>
        </Card>

        {correctedText && (
          <Card className="p-4 space-y-4 shadow-soft animate-in fade-in slide-in-from-bottom-4 duration-500">
            <div className="flex items-center justify-between">
              <label className="text-sm font-medium text-foreground">
                Poprawiony tekst
              </label>
              <Button
                variant="ghost"
                size="sm"
                onClick={copyToClipboard}
              >
                <Copy className="mr-2 h-4 w-4" />
                Kopiuj
              </Button>
            </div>
            <div className="p-4 bg-secondary rounded-lg">
              <p className="text-foreground whitespace-pre-wrap">{correctedText}</p>
            </div>
          </Card>
        )}
      </div>
    </div>
  );
};

export default TextCorrector;
