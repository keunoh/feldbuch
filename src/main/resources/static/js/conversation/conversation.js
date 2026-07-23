const newChatButton =
    document.querySelector(".new-chat-button");

newChatButton.addEventListener("click", createConversation);

document.addEventListener("DOMContentLoaded", initialize);

async function initialize() {
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

        list.appendChild(li);
    });
}