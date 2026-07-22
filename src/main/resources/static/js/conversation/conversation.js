const newChatButton =
    document.querySelector(".new-chat-button");

newChatButton.addEventListener("click", createConversation);

async function createConversation() {
    try {
        const result = await api.post(
            "/api/conversations",
            {
                title: "새 대화"
            }
        );

        console.log(result);
    } catch (e) {
        console.error(e);
    }
}