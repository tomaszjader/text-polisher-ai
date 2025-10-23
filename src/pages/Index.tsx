import { useEffect, useState } from "react";
import TextCorrector from "@/components/TextCorrector";
import { App as CapacitorApp } from '@capacitor/app';

const Index = () => {
  const [sharedText, setSharedText] = useState<string>("");

  useEffect(() => {
    // Handle incoming text from Android Intent (when text is selected and shared)
    const handleAppUrl = () => {
      CapacitorApp.addListener('appUrlOpen', (event: any) => {
        const url = event.url;
        // Extract text from URL if it exists
        if (url) {
          try {
            const urlParams = new URLSearchParams(url.split('?')[1]);
            const text = urlParams.get('text');
            if (text) {
              setSharedText(decodeURIComponent(text));
            }
          } catch (error) {
            console.error('Error parsing URL:', error);
          }
        }
      });
    };

    handleAppUrl();

    return () => {
      CapacitorApp.removeAllListeners();
    };
  }, []);

  return <TextCorrector initialText={sharedText} />;
};

export default Index;
