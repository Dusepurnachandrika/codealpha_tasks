import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class AiChatbot {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        server.createContext("/", new FrontendHandler());
        server.createContext("/api/chat", new ChatHandler());
        
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("AI Chatbot Web Server started! Open http://localhost:" + PORT + " in your browser.");
    }

    // --- NLP / Rule-Based Logic Engine ---
    public static String getBotResponse(String userInput) {
        String input = userInput.toLowerCase().replaceAll("[^a-z0-9\\s]", "").trim();
        
        // 1. Greetings
        if (input.matches("\\b(hi|hello|hey|greetings|howdy)\\b.*")) {
            return "Hello! I am an AI Chatbot. How can I help you today?";
        }
        if (input.matches(".*\\b(how are you|how do you do)\\b.*")) {
            return "I'm functioning at optimal efficiency! Thanks for asking. How can I assist you?";
        }

        // 2. Identity / Information
        if (input.matches(".*\\b(who are you|what are you|your name)\\b.*")) {
            return "I am a Java-based AI Assistant created to help answer your questions.";
        }
        if (input.matches(".*\\b(what is java|java)\\b.*")) {
            return "Java is a high-level, class-based, object-oriented programming language designed to have as few implementation dependencies as possible.";
        }

        // 3. Technical FAQs
        if (input.matches(".*\\b(what is machine learning|machine learning|ml)\\b.*")) {
            return "Machine Learning is a subset of AI that involves training algorithms to learn patterns from data without being explicitly programmed.";
        }
        if (input.matches(".*\\b(what is nlp|natural language processing)\\b.*")) {
            return "Natural Language Processing (NLP) enables computers to understand, interpret, and manipulate human language.";
        }
        if (input.matches(".*\\b(what is ai|artificial intelligence)\\b.*")) {
            return "Artificial Intelligence is the simulation of human intelligence processes by machines, especially computer systems.";
        }

        // 4. Farewells
        if (input.matches(".*\\b(bye|goodbye|cya|see you)\\b.*")) {
            return "Goodbye! Feel free to chat again if you have more questions.";
        }

        // 5. Fallback Response
        return "I'm not quite sure I understand. Could you rephrase your question or ask me about Java, AI, or NLP?";
    }

    // --- Handlers ---
    static class FrontendHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!t.getRequestMethod().equals("GET")) {
                t.sendResponseHeaders(405, -1);
                return;
            }
            
            String response = getHtml();
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            t.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            t.sendResponseHeaders(200, bytes.length);
            OutputStream os = t.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    static class ChatHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            // Setup simple CORS
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            t.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if (t.getRequestMethod().equals("OPTIONS")) {
                t.sendResponseHeaders(204, -1);
                return;
            }

            if (!t.getRequestMethod().equals("POST")) {
                t.sendResponseHeaders(405, -1);
                return;
            }

            // Read the POST body
            InputStream is = t.getRequestBody();
            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name()).useDelimiter("\\A");
            String body = scanner.hasNext() ? scanner.next() : "";
            
            // Extract "message" from JSON payload manually (to avoid adding external dependencies like Gson/Jackson)
            String message = extractJsonString(body, "message");
            if (message == null) message = "";

            // Pass input through our NLP Engine
            String reply = getBotResponse(message);

            // Escape response for JSON correctly
            reply = reply.replace("\"", "\\\"").replace("\n", "\\n");
            
            // Construct JSON response
            String jsonResponse = "{\"reply\": \"" + reply + "\"}";
            byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            
            t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            t.sendResponseHeaders(200, bytes.length);
            OutputStream os = t.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    // Helper method to extract a string value from a simple JSON object
    private static String extractJsonString(String json, String key) {
        String keyPattern = "\"" + key + "\":";
        int keyIndex = json.indexOf(keyPattern);
        if (keyIndex == -1) return null;
        
        int valueStart = json.indexOf("\"", keyIndex + keyPattern.length()) + 1;
        int valueEnd = json.indexOf("\"", valueStart);
        if (valueStart > 0 && valueEnd > valueStart) {
            return json.substring(valueStart, valueEnd);
        }
        return null;
    }

    // --- HTML / CSS / JS Frontend ---
    private static String getHtml() {
        return "<!DOCTYPE html>\n" +
               "<html lang=\"en\">\n" +
               "<head>\n" +
               "    <meta charset=\"UTF-8\">\n" +
               "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
               "    <title>AI Assistant</title>\n" +
               "    <link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap\" rel=\"stylesheet\">\n" +
               "    <style>\n" +
               "        * { box-sizing: border-box; margin: 0; padding: 0; }\n" +
               "        body {\n" +
               "            font-family: 'Inter', sans-serif;\n" +
               "            background: linear-gradient(135deg, #0f172a, #1e293b);\n" +
               "            height: 100vh;\n" +
               "            display: flex;\n" +
               "            justify-content: center;\n" +
               "            align-items: center;\n" +
               "            color: #f8fafc;\n" +
               "        }\n" +
               "        .chat-container {\n" +
               "            width: 100%;\n" +
               "            max-width: 450px;\n" +
               "            height: 85vh;\n" +
               "            background: rgba(30, 41, 59, 0.7);\n" +
               "            backdrop-filter: blur(20px);\n" +
               "            border-radius: 24px;\n" +
               "            border: 1px solid rgba(255, 255, 255, 0.1);\n" +
               "            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);\n" +
               "            display: flex;\n" +
               "            flex-direction: column;\n" +
               "            overflow: hidden;\n" +
               "        }\n" +
               "        .chat-header {\n" +
               "            padding: 20px;\n" +
               "            background: rgba(255, 255, 255, 0.03);\n" +
               "            border-bottom: 1px solid rgba(255, 255, 255, 0.08);\n" +
               "            text-align: center;\n" +
               "            font-weight: 600;\n" +
               "            font-size: 1.25rem;\n" +
               "            letter-spacing: 0.5px;\n" +
               "        }\n" +
               "        .chat-header span { color: #38bdf8; }\n" +
               "        .chat-messages {\n" +
               "            flex: 1;\n" +
               "            padding: 20px;\n" +
               "            overflow-y: auto;\n" +
               "            display: flex;\n" +
               "            flex-direction: column;\n" +
               "            gap: 16px;\n" +
               "        }\n" +
               "        .message {\n" +
               "            max-width: 82%;\n" +
               "            padding: 14px 18px;\n" +
               "            border-radius: 20px;\n" +
               "            font-size: 0.95rem;\n" +
               "            line-height: 1.5;\n" +
               "            animation: fadeIn 0.3s cubic-bezier(0.4, 0, 0.2, 1) forwards;\n" +
               "        }\n" +
               "        @keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }\n" +
               "        .bot-msg {\n" +
               "            align-self: flex-start;\n" +
               "            background: rgba(255, 255, 255, 0.08);\n" +
               "            border-bottom-left-radius: 4px;\n" +
               "        }\n" +
               "        .user-msg {\n" +
               "            align-self: flex-end;\n" +
               "            background: linear-gradient(135deg, #0ea5e9, #3b82f6);\n" +
               "            border-bottom-right-radius: 4px;\n" +
               "            color: #fff;\n" +
               "        }\n" +
               "        .chat-input-container {\n" +
               "            padding: 20px;\n" +
               "            background: rgba(15, 23, 42, 0.4);\n" +
               "            border-top: 1px solid rgba(255, 255, 255, 0.05);\n" +
               "            display: flex;\n" +
               "            gap: 12px;\n" +
               "        }\n" +
               "        .chat-input {\n" +
               "            flex: 1;\n" +
               "            background: rgba(255, 255, 255, 0.05);\n" +
               "            border: 1px solid rgba(255, 255, 255, 0.1);\n" +
               "            padding: 14px 20px;\n" +
               "            border-radius: 100px;\n" +
               "            color: #f8fafc;\n" +
               "            font-size: 0.95rem;\n" +
               "            font-family: inherit;\n" +
               "            outline: none;\n" +
               "            transition: all 0.3s ease;\n" +
               "        }\n" +
               "        .chat-input:focus {\n" +
               "            border-color: #38bdf8;\n" +
               "            background: rgba(255, 255, 255, 0.08);\n" +
               "        }\n" +
               "        .chat-input::placeholder {\n" +
               "            color: #94a3b8;\n" +
               "        }\n" +
               "        .send-btn {\n" +
               "            background: #38bdf8;\n" +
               "            border: none;\n" +
               "            width: 48px;\n" +
               "            height: 48px;\n" +
               "            border-radius: 50%;\n" +
               "            display: flex;\n" +
               "            justify-content: center;\n" +
               "            align-items: center;\n" +
               "            cursor: pointer;\n" +
               "            transition: all 0.2s ease;\n" +
               "        }\n" +
               "        .send-btn:hover {\n" +
               "            transform: scale(1.05);\n" +
               "            background: #0ea5e9;\n" +
               "            box-shadow: 0 4px 12px rgba(14, 165, 233, 0.3);\n" +
               "        }\n" +
               "        .send-btn svg {\n" +
               "            width: 20px;\n" +
               "            height: 20px;\n" +
               "            fill: #0f172a;\n" +
               "            margin-left: 2px;\n" +
               "        }\n" +
               "        /* Custom Scrollbar */\n" +
               "        ::-webkit-scrollbar { width: 6px; }\n" +
               "        ::-webkit-scrollbar-track { background: transparent; }\n" +
               "        ::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.15); border-radius: 3px; }\n" +
               "        ::-webkit-scrollbar-thumb:hover { background: rgba(255,255,255,0.25); }\n" +
               "        \n" +
               "        .typing-indicator {\n" +
               "            display: flex;\n" +
               "            gap: 6px;\n" +
               "            padding: 16px 20px;\n" +
               "            background: rgba(255, 255, 255, 0.05);\n" +
               "            border-radius: 20px;\n" +
               "            border-bottom-left-radius: 4px;\n" +
               "            align-self: flex-start;\n" +
               "            width: fit-content;\n" +
               "        }\n" +
               "        .dot {\n" +
               "            width: 6px;\n" +
               "            height: 6px;\n" +
               "            background: #94a3b8;\n" +
               "            border-radius: 50%;\n" +
               "            animation: bounce 1.4s infinite ease-in-out both;\n" +
               "        }\n" +
               "        .dot:nth-child(1) { animation-delay: -0.32s; }\n" +
               "        .dot:nth-child(2) { animation-delay: -0.16s; }\n" +
               "        @keyframes bounce {\n" +
               "            0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }\n" +
               "            40% { transform: scale(1); opacity: 1; }\n" +
               "        }\n" +
               "    </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "\n" +
               "    <div class=\"chat-container\">\n" +
               "        <div class=\"chat-header\">\n" +
               "            AI <span>Assistant</span>\n" +
               "        </div>\n" +
               "        <div class=\"chat-messages\" id=\"chatBox\">\n" +
               "            <div class=\"message bot-msg\">Hello! I'm your AI assistant. I can answer FAQs about Java, NLP, and Machine Learning. How can I help you today?</div>\n" +
               "        </div>\n" +
               "        <form class=\"chat-input-container\" id=\"chatForm\">\n" +
               "            <input type=\"text\" class=\"chat-input\" id=\"userInput\" placeholder=\"Ask me anything...\" autocomplete=\"off\">\n" +
               "            <button type=\"submit\" class=\"send-btn\" aria-label=\"Send Message\">\n" +
               "                <svg viewBox=\"0 0 24 24\">\n" +
               "                    <path d=\"M2.01 21L23 12 2.01 3 2 10l15 2-15 2z\"/>\n" +
               "                </svg>\n" +
               "            </button>\n" +
               "        </form>\n" +
               "    </div>\n" +
               "\n" +
               "    <script>\n" +
               "        const form = document.getElementById('chatForm');\n" +
               "        const input = document.getElementById('userInput');\n" +
               "        const chatBox = document.getElementById('chatBox');\n" +
               "\n" +
               "        function appendMessage(text, isUser) {\n" +
               "            const div = document.createElement('div');\n" +
               "            div.className = 'message ' + (isUser ? 'user-msg' : 'bot-msg');\n" +
               "            div.textContent = text;\n" +
               "            chatBox.appendChild(div);\n" +
               "            chatBox.scrollTop = chatBox.scrollHeight;\n" +
               "        }\n" +
               "\n" +
               "        function showTyping() {\n" +
               "            const div = document.createElement('div');\n" +
               "            div.className = 'typing-indicator';\n" +
               "            div.id = 'typingIndicator';\n" +
               "            div.innerHTML = '<div class=\"dot\"></div><div class=\"dot\"></div><div class=\"dot\"></div>';\n" +
               "            chatBox.appendChild(div);\n" +
               "            chatBox.scrollTop = chatBox.scrollHeight;\n" +
               "        }\n" +
               "\n" +
               "        function removeTyping() {\n" +
               "            const indicator = document.getElementById('typingIndicator');\n" +
               "            if (indicator) indicator.remove();\n" +
               "        }\n" +
               "\n" +
               "        form.addEventListener('submit', async (e) => {\n" +
               "            e.preventDefault();\n" +
               "            const msg = input.value.trim();\n" +
               "            if (!msg) return;\n" +
               "\n" +
               "            appendMessage(msg, true);\n" +
               "            input.value = '';\n" +
               "            showTyping();\n" +
               "\n" +
               "            // Simulate slight network delay for better UX\n" +
               "            setTimeout(async () => {\n" +
               "                try {\n" +
               "                    const response = await fetch('/api/chat', {\n" +
               "                        method: 'POST',\n" +
               "                        headers: { 'Content-Type': 'application/json' },\n" +
               "                        body: JSON.stringify({ message: msg })\n" +
               "                    });\n" +
               "                    \n" +
               "                    const data = await response.json();\n" +
               "                    removeTyping();\n" +
               "                    appendMessage(data.reply, false);\n" +
               "                } catch (err) {\n" +
               "                    removeTyping();\n" +
               "                    appendMessage(\"Sorry, I couldn't connect to the server.\", false);\n" +
               "                }\n" +
               "            }, 500 + Math.random() * 500);\n" +
               "        });\n" +
               "    </script>\n" +
               "</body>\n" +
               "</html>";
    }
}
