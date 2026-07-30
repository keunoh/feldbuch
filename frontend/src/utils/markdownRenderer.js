import {marked} from "marked";
import hljs from "highlight.js";
import DOMPurify from "dompurify";

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;")
}

function extractCodeInformation(tokenOrCode, infostring) {
  const isTokenObject =
    typeof tokenOrCode === 'object'
    && tokenOrCode !== null;

  const code = isTokenObject
    ? tokenOrCode.text
    : tokenOrCode;

  const languageValue = isTokenObject
    ? tokenOrCode.lang
    : infostring;

  const language = languageValue
      ?.trim()
      .split(/\s+/)[0]
    || 'text';

  return {
    code: code ?? "",
    language
  };
}

function highlightCode(code, language) {
  if (hljs.getLanguage(language)) {
    return hljs.highlight(code, {
      language,
      ignoreIllegals: true
    }).value;
  }

  if (language === "text" || language === "plaintext") {
    return escapeHtml(code);
  }

  return hljs.highlightAuto(code).value;
}

function createMarkdownRenderer() {
  const renderer = new marked.Renderer();

  renderer.code = function (tokenOrCode, infostring) {
    const {code, language} = extractCodeInformation(
      tokenOrCode,
      infostring
    );

    const escapedLanguage = escapeHtml(language);
    const highlightedCode = highlightCode(code, language);
    const encodedCode = encodeURIComponent(code);

    return `
      <div class="code-block">
        <div class="code-block-header">
          <span class="code-language">
            ${escapedLanguage.toUpperCase()}
          </span>

          <button
            type="button"
            class="code-copy-button"
            data-code="${encodedCode}"
            aria-label="코드 복사"
          >
            COPY
          </button>
        </div>

        <pre><code class="hljs language-${escapedLanguage}">${highlightedCode}</code>
        </pre>
      </div>
    `;
  };

  return renderer;
}

const markdownRenderer = createMarkdownRenderer();

export function renderMarkdown(content) {
  const html = marked.parse(content ?? "", {
    renderer: markdownRenderer
  });

  return DOMPurify.sanitize(html);
}

export function renderPlainText(content) {
  return DOMPurify.sanitize(content ?? "");
}
