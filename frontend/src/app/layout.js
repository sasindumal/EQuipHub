import "./globals.css";
import { AuthProvider } from "@/lib/auth";

export const metadata = {
  title: "EQuipHub — Equipment Request Management",
  description: "A comprehensive web-based platform for managing equipment requests across university departments.",
  keywords: "equipment, management, university, lab, borrowing",
};

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <head>
        <link rel="icon" href="/logo.png" />
      </head>
      <body>
        <AuthProvider>
          {children}
        </AuthProvider>
      </body>
    </html>
  );
}
