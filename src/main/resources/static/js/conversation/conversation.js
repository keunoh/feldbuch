let currentConversationId = null;

const newChatButton =
    document.querySelector(".new-chat-button");

const messageInput =
    document.querySelector(".chat-input textarea");

const sendButton =
    document.getElementById("send-button");

document.addEventListener("DOMContentLoaded", initialize);

async function initialize() {
    newChatButton.addEventListener("click", createConversation);

    sendButton.addEventListener("click", sendMessage);

    await loadConversations();
}

async function loadConversations() {

    const result = await api.get("/api/conversations");

    console.log("loadConversations() = " + result.data);

    renderConversationList(result.data);
}

async function createConversation() {
    try {
        const result = await api.post(
            "/api/conversations",
            {
                title: "새 대화"
            }
        );

        await loadConversations();

        console.log("createConversation() = " + result);
    } catch (e) {
        console.error(e);
    }
}

function renderConversationList(conversations) {

    console.log("renderConversationList(conversations) = ", conversations);

    const list = document.getElementById("conversation-list");

    list.innerHTML = "";

    conversations.forEach(conversation => {
        const li = document.createElement("li");

        li.textContent = conversation.title;
        li.dataset.id = conversation.id;

        li.addEventListener("click", () => {
            selectConversation(conversation.id);
        })

        list.appendChild(li);
    });
}

async function selectConversation(conversationId) {
    currentConversationId = conversationId;

    console.log("현재 선택된 대화 =", currentConversationId);

    await loadMessages(conversationId);
}

async function loadMessages(conversationId) {

    const result = await api.get(
        `/api/conversations/${conversationId}/messages`
    );

    console.log(result.data);

    renderMessages(result.data);
}

function renderMessages(messages) {

    const container =
        document.querySelector(".messages");

    container.innerHTML = "";

    messages.forEach(message => {

        const div = document.createElement("div");

        div.classList.add("message");

        div.classList.add(message.role.toLowerCase());

        div.textContent = message.content;

        container.appendChild(div);
    })
}

async function sendMessage() {
    if (!currentConversationId) {
        alert("대화를 먼저 선택하세요.");
        return;
    }

    const message = messageInput.value.trim();

    if (!message) {
        return;
    }

    try {
        await api.post(
            `/api/conversations/${currentConversationId}/chat`,
            {
                message: message
            }
        );

        messageInput.value = "";

        await loadMessages(currentConversationId);

        await loadConversations();

    } catch (e) {
        console.error(e);
        alert("메시지 전송에 실패했습니다.")
    }

}