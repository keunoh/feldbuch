import apiClient from "@/api/apiClient.js";

export async function getKnowledgeTree() {
  const response = await apiClient.get(
    "/knowledge/tree",
  );

  return response.data;
}

export async function getKnowledgeNotes(knowledgeId) {
  const response = await apiClient.get(
    `/knowledge/${knowledgeId}/notes`,
  );

  return response.data;
}

export async function getKnowledgeNote(noteId) {
  const response = await apiClient.get(
    `/knowledge/notes/${noteId}`
  );

  return response.data;
}
