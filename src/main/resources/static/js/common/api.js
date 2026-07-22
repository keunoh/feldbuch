async function get(url) {

}

const api = {
    async post(url, body) {

        const token = localStorage.getItem("accessToken");

        const headers = {
            "Content-Type": "application/json"
        };

        if (token) {
            headers.Authorization = `Bearer ${token}`;
        }

        const response = await fetch(url, {
            method: "POST",
            headers,
            body: JSON.stringify(body)
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.message ?? "API 요청 실패");
        }

        return result;
    }
}


