package com.mar.CRUD_SERVICE.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class TopicAnalysisService {

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openAiApiUrl;

    @Value("${openai.model:gpt-4o-mini}")
    private String openAiModel;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Gửi nội dung bài viết tới OpenAI để phân tích và trả về danh sách tên chủ đề (topic) ngắn gọn.
     * Nếu có lỗi (chưa cấu hình API key, lỗi mạng, ...) sẽ trả về danh sách rỗng để hệ thống vẫn hoạt động bình thường.
     */
    public List<String> extractTopicsFromContent(String content) {
        if (content == null || content.isBlank()) {
            return Collections.emptyList();
        }
        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            // chưa cấu hình API key -> bỏ qua phân tích AI
            return Collections.emptyList();
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            String prompt = "Hãy đọc nội dung bài viết sau và trả về danh sách tối đa 5 chủ đề (topic) ngắn gọn, dạng từ khoá không có dấu cách thừa, ví dụ: bongda, dulich, congnghe.\n"
                    + "Chỉ trả về JSON array các chuỗi, không giải thích thêm.\n\nNội dung:\n"
                    + content;

            Map<String, Object> message = Map.of(
                    "role", "user",
                    "content", prompt
            );

            Map<String, Object> body = Map.of(
                    "model", openAiModel,
                    "messages", List.of(message),
                    "temperature", 0.3
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(openAiApiUrl, request, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                System.err.println("API trả về lỗi hoặc rỗng: " + response.getStatusCode());
                return Collections.emptyList();
            }

            System.out.println("API Response: " + response.getBody());

            // tuỳ vào cấu trúc trả về, ta cố gắng lấy nội dung text đầu tiên
            Object choicesObj = response.getBody().get("choices");
            if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
                return Collections.emptyList();
            }
            Object first = choices.get(0);
            if (!(first instanceof Map<?, ?> firstMap)) {
                return Collections.emptyList();
            }
            Object messageObj = firstMap.get("message");
            if (!(messageObj instanceof Map<?, ?> msgMap)) {
                return Collections.emptyList();
            }
            Object contentObj = msgMap.get("content");
            if (!(contentObj instanceof String text)) {
                return Collections.emptyList();
            }

            // text kỳ vọng là JSON array, nhưng Gemini hay trả về kèm ```json
            text = text.trim();
            if (text.startsWith("```json")) {
                text = text.substring(7);
            }
            if (text.startsWith("```")) {
                text = text.substring(3);
            }
            if (text.endsWith("```")) {
                text = text.substring(0, text.length() - 3);
            }
            text = text.trim();

            List<String> topics = new ArrayList<>();
            if (text.startsWith("[") && text.endsWith("]")) {
                text = text.substring(1, text.length() - 1);
            }
            for (String raw : text.split("[,\n]")) {
                String t = raw.replace("\"", "").replace("'", "").trim();
                if (!t.isEmpty()) {
                    topics.add(t);
                }
            }
            return topics;
        } catch (Exception e) {
            // Nếu có bất kỳ lỗi nào, in ra console để debug
            System.err.println("Lỗi khi gọi API AI: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}

