package ua.nure.finance.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.nure.finance.service.AiAnalysisService.AiAnalysisException;
import ua.nure.finance.service.AiAnalysisService.ChatTurn;
import ua.nure.finance.service.PersonalFinanceAssistantService;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AssistantController.class)
class AssistantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PersonalFinanceAssistantService assistantService;

    @Test
    void pageStartsWithWelcomeMessage() throws Exception {
        mockMvc.perform(get("/assistant"))
                .andExpect(status().isOk())
                .andExpect(view().name("assistant"))
                .andExpect(content().string(containsString("Я ваш інтелектуальний фінансовий помічник")));
    }

    @Test
    void chatMessageUsesAiAndReturnsFormattedAnswer() throws Exception {
        when(assistantService.answer(eq(List.of(new ChatTurn("user", "Попереднє питання."))),
                eq("Скільки було витрат?")))
                .thenReturn("**Ваші витрати — 500.**\n\n- Їжа: 300\n- Транспорт: 200");

        mockMvc.perform(post("/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Скільки було витрат?","history":[
                                  {"role":"user","content":"Попереднє питання."}
                                ]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answerText", containsString("Ваші витрати — 500.")))
                .andExpect(jsonPath("$.answerHtml", containsString("<strong>Ваші витрати — 500.</strong>")))
                .andExpect(jsonPath("$.answerHtml", containsString("<li>Їжа: 300</li>")));
        verify(assistantService).answer(eq(List.of(new ChatTurn("user", "Попереднє питання."))),
                eq("Скільки було витрат?"));
    }

    @Test
    void assistantMarkdownRendersTablesAndEscapesHtml() throws Exception {
        when(assistantService.answer(anyList(), eq("Покажи таблицю.")))
                .thenReturn("| Категорія | Сума |\n| --- | ---: |\n| Їжа | 300 |\n\n<script>alert(1)</script>");

        mockMvc.perform(post("/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Покажи таблицю.","history":[]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answerHtml", containsString("<table>")))
                .andExpect(jsonPath("$.answerHtml", containsString("<th>Категорія</th>")))
                .andExpect(jsonPath("$.answerHtml", containsString("&lt;script&gt;")))
                .andExpect(jsonPath("$.answerHtml", org.hamcrest.Matchers.not(containsString("<script>"))));
    }

    @Test
    void emptyMessageIsRejectedWithoutCallingAi() throws Exception {
        mockMvc.perform(post("/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"  ","history":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Введіть запитання."));
        verify(assistantService, never()).answer(anyList(), eq(""));
    }

    @Test
    void aiConfigurationErrorsAreReturnedToChat() throws Exception {
        when(assistantService.answer(anyList(), eq("Підсумуй мої фінанси.")))
                .thenThrow(new AiAnalysisException("AI API не налаштовано."));

        mockMvc.perform(post("/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Підсумуй мої фінанси.","history":[]}
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("AI API не налаштовано."));
    }
}
