package ua.nure.finance.controller;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import ua.nure.finance.service.AiAnalysisService.AiAnalysisException;
import ua.nure.finance.service.AiAnalysisService.ChatTurn;
import ua.nure.finance.service.PersonalFinanceAssistantService;

import java.util.List;

@Controller
public class AssistantController {

    private static final int MAX_MESSAGE_LENGTH = 2000;
    private static final int MAX_CONVERSATION_MESSAGES = 40;
    private static final String WELCOME_MESSAGE = "Вітаю! Я ваш інтелектуальний фінансовий помічник. "
            + "Можу відповісти на запитання про доходи, витрати, активи та план оптимізації. "
            + "Чим можу допомогти?";

    private final Parser markdownParser = Parser.builder()
            .extensions(List.<Extension>of(TablesExtension.create()))
            .build();
    private final HtmlRenderer markdownRenderer = HtmlRenderer.builder()
            .extensions(List.<Extension>of(TablesExtension.create()))
            .escapeHtml(true)
            .build();
    private final PersonalFinanceAssistantService assistantService;

    public AssistantController(PersonalFinanceAssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @GetMapping("/assistant")
    public String getAssistant(Model model) {
        model.addAttribute("messages", List.of(new RenderedChatMessage("assistant",
                markdownRenderer.render(markdownParser.parse(WELCOME_MESSAGE)))));
        return "assistant";
    }

    @PostMapping(value = "/assistant/chat", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        if (request == null || request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest().body(new ChatResponse(null, null, "Введіть запитання."));
        }
        String question = request.message().trim();
        if (question.length() > MAX_MESSAGE_LENGTH) {
            return ResponseEntity.badRequest().body(new ChatResponse(null, null,
                    "Запитання не може бути довшим за %d символів.".formatted(MAX_MESSAGE_LENGTH)));
        }

        List<ChatTurn> history = request.history() == null ? List.of() : request.history().stream()
                .filter(turn -> turn != null && isAllowedRole(turn.role()) && turn.content() != null)
                .map(turn -> new ChatTurn(turn.role(), turn.content()))
                .toList();
        if (history.size() > MAX_CONVERSATION_MESSAGES) {
            history = history.subList(history.size() - MAX_CONVERSATION_MESSAGES, history.size());
        }

        try {
            String answer = assistantService.answer(history, question);
            String answerHtml = markdownRenderer.render(markdownParser.parse(answer));
            return ResponseEntity.ok(new ChatResponse(answer, answerHtml, null));
        } catch (AiAnalysisException exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ChatResponse(null, null, exception.getMessage()));
        }
    }

    private boolean isAllowedRole(String role) {
        return "user".equals(role) || "assistant".equals(role);
    }

    public record ChatRequest(String message, List<ChatTurn> history) {
    }

    public record ChatResponse(String answerText, String answerHtml, String error) {
    }

    public record RenderedChatMessage(String role, String contentHtml) {
    }
}
