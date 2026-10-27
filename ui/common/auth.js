// ============================================================================
// AUTH
// Register and sign in on the Cognito hosted UI (authorization code + PKCE).
// Tokens are kept in localStorage and the ID token is refreshed when it
// expires. Load it in <head>, without defer, after common/auth-config.js
// (AUTH_CONFIG, written by deploy.sh from Terraform outputs).
//   Auth.register(), Auth.login()     go to the hosted sign-up / sign-in page
//   Auth.completeSignIn(code, state)  on the callback page: code -> tokens
//   Auth.signedInEmail()              Promise of the user's email, or null
//   Auth.callApi(path, options)       fetch from the API with the ID token
//   Auth.signOut()                    end the session here and on the hosted UI
//   Auth.forgetTokens()               drop the tokens in this browser only
// ============================================================================

const Auth = (function () {
    const TOKENS_KEY = 'hydra24.tokens';
    const PKCE_KEY = 'hydra24.pkce';
    const CALLBACK_PATH = '/auth/callback.html';
    const EXPIRY_MARGIN_MS = 60 * 1000;

    function redirectUri() {
        return window.location.origin + CALLBACK_PATH;
    }

    function base64Url(bytes) {
        return btoa(String.fromCharCode.apply(null, bytes))
            .replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
    }

    function randomString() {
        return base64Url(crypto.getRandomValues(new Uint8Array(32)));
    }

    async function sha256(text) {
        const digest = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(text));
        return base64Url(new Uint8Array(digest));
    }

    async function goToHostedPage(page) {
        const verifier = randomString();
        const state = randomString();
        sessionStorage.setItem(PKCE_KEY, JSON.stringify({verifier: verifier, state: state}));

        const params = new URLSearchParams({
            response_type: 'code',
            client_id: AUTH_CONFIG.clientId,
            redirect_uri: redirectUri(),
            scope: 'openid email',
            state: state,
            code_challenge: await sha256(verifier),
            code_challenge_method: 'S256'
        });
        window.location.assign(`${AUTH_CONFIG.authUrl}/${page}?${params}`);
    }

    // Resolves to the token response, or null when Cognito refuses the grant.
    // Rejects only on a network failure.
    async function requestTokens(grant) {
        const response = await fetch(`${AUTH_CONFIG.authUrl}/oauth2/token`, {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: new URLSearchParams(Object.assign({client_id: AUTH_CONFIG.clientId}, grant))
        });
        return response.ok ? response.json() : null;
    }

    function saveTokens(response, refreshToken) {
        localStorage.setItem(TOKENS_KEY, JSON.stringify({
            idToken: response.id_token,
            refreshToken: response.refresh_token || refreshToken,
            expiresAt: Date.now() + response.expires_in * 1000
        }));
    }

    function loadTokens() {
        try {
            return JSON.parse(localStorage.getItem(TOKENS_KEY));
        } catch (e) {
            return null;
        }
    }

    function forgetTokens() {
        localStorage.removeItem(TOKENS_KEY);
    }

    // Revokes the refresh token and ends the hosted UI session too, so the
    // next Login asks for the password again. Lands back on the home page.
    async function signOut() {
        const tokens = loadTokens();
        forgetTokens();
        if (tokens && tokens.refreshToken) {
            try {
                await fetch(`${AUTH_CONFIG.authUrl}/oauth2/revoke`, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                    body: new URLSearchParams({client_id: AUTH_CONFIG.clientId, token: tokens.refreshToken})
                });
            } catch (e) {
                // Offline: the token is gone from this browser either way.
            }
        }
        const params = new URLSearchParams({client_id: AUTH_CONFIG.clientId, logout_uri: window.location.origin + '/'});
        window.location.assign(`${AUTH_CONFIG.authUrl}/logout?${params}`);
    }

    // The ID token's claims. Display only: the backend verifies the signature.
    function claims(idToken) {
        const payload = idToken.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
        const bytes = Uint8Array.from(atob(payload), function (c) { return c.charCodeAt(0); });
        return JSON.parse(new TextDecoder().decode(bytes));
    }

    async function refresh(tokens) {
        try {
            const response = await requestTokens({grant_type: 'refresh_token', refresh_token: tokens.refreshToken});
            if (!response) {
                forgetTokens();
                return null;
            }
            saveTokens(response, tokens.refreshToken);
            return loadTokens();
        } catch (e) {
            return null; // Offline: keep the tokens and try again next time.
        }
    }

    // Current tokens, refreshed if the ID token has (nearly) expired; null if signed out.
    async function validTokens() {
        const tokens = loadTokens();
        if (!tokens || !tokens.idToken) return null;
        if (tokens.expiresAt - EXPIRY_MARGIN_MS > Date.now()) return tokens;
        return refresh(tokens);
    }

    async function completeSignIn(code, state) {
        const pkce = JSON.parse(sessionStorage.getItem(PKCE_KEY));
        sessionStorage.removeItem(PKCE_KEY);
        if (!pkce || !code || pkce.state !== state) {
            throw new Error('This sign-in link has expired. Please try again.');
        }
        const response = await requestTokens({
            grant_type: 'authorization_code',
            code: code,
            redirect_uri: redirectUri(),
            code_verifier: pkce.verifier
        });
        if (!response) throw new Error('Sign-in failed. Please try again.');
        saveTokens(response);
    }

    async function signedInEmail() {
        const tokens = await validTokens();
        return tokens ? claims(tokens.idToken).email : null;
    }

    async function callApi(path, options) {
        const tokens = await validTokens();
        if (!tokens) throw new Error('You are not signed in.');
        const request = Object.assign({}, options);
        request.headers = Object.assign({}, request.headers, {Authorization: `Bearer ${tokens.idToken}`});
        return fetch(AUTH_CONFIG.apiUrl + path, request);
    }

    return {
        register: function () { return goToHostedPage('signup'); },
        login: function () { return goToHostedPage('login'); },
        completeSignIn: completeSignIn,
        signedInEmail: signedInEmail,
        callApi: callApi,
        signOut: signOut,
        forgetTokens: forgetTokens
    };
})();
