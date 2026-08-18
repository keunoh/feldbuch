package io.github.kaltz.feldbuch.rag.context;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KnowledgeContextBuilder {

    public String build(List<Document> documents) {

        if (documents == null || documents.isEmpty()) {

            return "";
        }

        StringBuilder context = new StringBuilder();

        for (int i = 0; i < documents.size(); i++) {

            Document document = documents.get(i);

            context.append("[지식 %d]".formatted(i + 1));

            context.append(System.lineSeparator());

            context.append(document.getText());

            if (i < documents.size() - 1) {
                context.append(System.lineSeparator());
                context.append(System.lineSeparator());
            }
        }

        return context.toString();
    }
}
