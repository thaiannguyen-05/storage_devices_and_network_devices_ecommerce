package module.bussiness.ai.dto;

import java.util.ArrayList;
import java.util.List;

public class ChatRequestDto {
    private String prompt;
    private List<MessageDto> chatHistory;
    private boolean stream;

    public ChatRequestDto() {
        this.chatHistory = new ArrayList<MessageDto>();
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public List<MessageDto> getChatHistory() {
        return chatHistory;
    }

    public void setChatHistory(List<MessageDto> chatHistory) {
        this.chatHistory = chatHistory;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }
}
