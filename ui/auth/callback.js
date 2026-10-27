// ============================================================================
// SIGN-IN CALLBACK
// Cognito's hosted UI sends the user back here with ?code=&state=. Swap the
// code for tokens, record the sign-in with the backend (which creates the
// user on first sign-in), then go home.
// ============================================================================

(async function () {
    const status = document.getElementById('auth-status');
    const params = new URLSearchParams(window.location.search);

    try {
        if (params.has('error')) {
            throw new Error(params.get('error_description') || 'Sign-in was cancelled.');
        }
        await Auth.completeSignIn(params.get('code'), params.get('state'));

        const response = await Auth.callApi('/api/me', {method: 'PUT'});
        if (!response.ok) {
            Auth.forgetTokens();
            throw new Error('We could not finish signing you in. Please try again.');
        }
        window.location.replace('/');
    } catch (error) {
        status.textContent = error.message;
    }
})();
