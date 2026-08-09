const ACCESS_TOKEN_KEY = 'accessToken';
const REFRESH_TOKEN_KEY = 'refreshToken';
const USER_ID_KEY = 'userId';

/**
 * Access Token 저장
 */
export function saveAccessToken(token) {
  localStorage.setItem(
    ACCESS_TOKEN_KEY,
    token,
  );
}

/**
 * Access Token 조회
 */
export function getAccessToken() {
  return localStorage.getItem(
    ACCESS_TOKEN_KEY,
  );
}

/**
 * Refresh Token 저장
 */
export function saveRefreshToken(token) {
  localStorage.setItem(
    REFRESH_TOKEN_KEY,
    token,
  );
}

/**
 * Refresh Token 조회
 */
export function getRefreshToken() {
  return localStorage.getItem(
    REFRESH_TOKEN_KEY,
  );
}

/**
 * 사용자 ID 저장
 */
export function saveUserId(userId) {
  localStorage.setItem(
    USER_ID_KEY,
    userId,
  );
}

/**
 * 사용자 ID 조회
 */
export function getUserId() {
  return localStorage.getItem(
    USER_ID_KEY,
  );
}

/**
 * 로그인 여부 확인
 */
export function isAuthenticated() {
  return !!getAccessToken();
}

/**
 * 로그아웃
 */
export function logout() {
  localStorage.removeItem(
    ACCESS_TOKEN_KEY,
  );

  localStorage.removeItem(
    REFRESH_TOKEN_KEY,
  );

  localStorage.removeItem(
    USER_ID_KEY,
  );
}
