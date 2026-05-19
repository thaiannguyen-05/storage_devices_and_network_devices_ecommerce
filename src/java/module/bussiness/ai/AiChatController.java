package module.bussiness.ai;

import common.annotation.Public;
import common.controller.BaseController;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import module.bussiness.ai.dto.ChatRequestDto;
import module.bussiness.ai.response_dto.ChatResponseDto;
import module.bussiness.ai.response_dto.ChatStreamChunk;

@Public
@WebServlet(name = "AiChat", urlPatterns = {"/ai-chat"})
public class AiChatController extends BaseController {
    private static final Jsonb JSONB = JsonbBuilder.create();

    private final AiChatService aiChatService = new AiChatService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("chat-stream".equals(action)) {
            handleStreamChat(req, res);
            return;
        }
        if ("chat".equals(action)) {
            handleChat(req, res);
            return;
        }
        sendError(res, HttpServletResponse.SC_BAD_REQUEST, "Unsupported action");
    }

    private void handleChat(HttpServletRequest req, HttpServletResponse res) throws IOException {
        ChatRequestDto dto;
        try {
            dto = readRequest(req);
        } catch (RuntimeException ex) {
            sendError(res, HttpServletResponse.SC_BAD_REQUEST, "Noi dung gui len khong hop le.");
            return;
        }
        ChatResponseDto result = aiChatService.chat(dto, getCurrentUserId(req));
        res.setStatus(result.isSuccess() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
        sendJson(res, result);
    }

    private void handleStreamChat(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("text/event-stream");
        res.setCharacterEncoding("UTF-8");
        res.setHeader("Cache-Control", "no-cache");
        res.setHeader("Connection", "keep-alive");
        res.setHeader("X-Accel-Buffering", "no");

        ChatRequestDto dto;
        try {
            dto = readRequest(req);
        } catch (RuntimeException ex) {
            writeStreamChunk(res.getWriter(), new ChatStreamChunk("", true, "Noi dung gui len khong hop le."));
            return;
        }

        PrintWriter out = res.getWriter();
        try {
            aiChatService.streamChat(dto, getCurrentUserId(req), chunk -> {
                writeStreamChunk(out, chunk);
                return !chunk.isDone() && !out.checkError();
            });
        } finally {
            out.close();
        }
    }

    private ChatRequestDto readRequest(HttpServletRequest req) throws IOException {
        return JSONB.fromJson(req.getReader(), ChatRequestDto.class);
    }

    private void writeStreamChunk(PrintWriter out, ChatStreamChunk chunk) {
        out.write("data: ");
        out.write(JSONB.toJson(chunk));
        out.write("\n\n");
        out.flush();
    }
}
