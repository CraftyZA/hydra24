// ============================================================================
// API
// Calls the backend and returns the parsed body, or null for an empty answer.
// A refused call rejects with an Error whose message can be shown to the user.
// Needs common/auth.js.
//   Api.getPublic(path)            no sign-in needed
//   Api.get(path)                  as the signed-in user
//   Api.send(method, path, body)   as the signed-in user; body is optional
// ============================================================================

const Api = (function () {
    const JSON_TYPE = 'application/json';

    function lastSegment(path) {
        return path.split('.').pop();
    }

    // {"error": "..."} from our own checks, {"violations": [...]} from validation.
    function errorMessage(body) {
        if (body && body.error) return body.error;
        if (body && body.violations) {
            return body.violations.map(function (v) { return `${lastSegment(v.field)}: ${v.message}`; }).join('. ');
        }
        return 'Something went wrong. Please try again.';
    }

    async function parse(response) {
        const text = await response.text();
        const body = text ? JSON.parse(text) : null;
        if (!response.ok) throw new Error(errorMessage(body));
        return body;
    }

    async function getPublic(path) {
        return parse(await fetch(AUTH_CONFIG.apiUrl + path, {headers: {Accept: JSON_TYPE}}));
    }

    async function send(method, path, body) {
        const request = {method: method, headers: {Accept: JSON_TYPE}};
        if (body !== undefined) {
            request.headers['Content-Type'] = JSON_TYPE;
            request.body = JSON.stringify(body);
        }
        return parse(await Auth.callApi(path, request));
    }

    return {
        getPublic: getPublic,
        get: function (path) { return send('GET', path); },
        send: send
    };
})();
