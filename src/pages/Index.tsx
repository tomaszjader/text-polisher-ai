import { useEffect, useState } from "react";
import TextCorrector from "@/components/TextCorrector";
import { App as CapacitorApp } from '@capacitor/app';

const Index = () => {
  const [sharedText, setSharedText] = useState<string>("");

  useEffect(() => {
    // Read text from initial URL query when app first loads
    try {
      const initialParams = new URLSearchParams(window.location.search);
      const initialText = initialParams.get('text');
      if (initialText) {
        setSharedText(decodeURIComponent(initialText));
      }
    } catch (error) {
      console.error('Error parsing initial URL:', error);
    }

    // Handle incoming deep link events while app is running
    const sub = CapacitorApp.addListener('appUrlOpen', (event: any) => {
      const url = event.url;
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

    return () => {
      sub.remove();
      CapacitorApp.removeAllListeners();
    };
  }, []);

  return <TextCorrector initialText={sharedText} />;
};

export default Index;
