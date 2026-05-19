package module.bussiness.ai.response_dto;

import module.core.common.BaseResponse;

public class ChatResponseDto extends BaseResponse {
    private String responseText;

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }
}
