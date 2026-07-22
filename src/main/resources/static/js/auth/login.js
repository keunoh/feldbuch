const loginButton =
    document.querySelector("#loginButton");

loginButton.addEventListener("click", login);

async function login() {

    const email =
        document.querySelector("#email").value;

    const password =
        document.querySelector("#password").value;

    try {
        const result = await api.post("/api/auth/login", {
            email,
            password
        });

        localStorage.setItem(
            "accessToken",
            result.data.accessToken
        );

        window.location.href = "/conversations";

    } catch (e) {
        alert(e.message);
    }
}