// ============================================================================
// ADMIN PAGE
// What the admin pages share. Needs common/auth.js, api.js, dom.js and form.js.
//   AdminPage.run(status, load)      runs load() for admins, shows errors in status.
//                                    Only decides what to show: the backend checks.
//   AdminPage.queryId()              the ?id= of this page, or null for a new item
//   AdminPage.wireEditor(settings)   save and delete on an edit page:
//     {form, status, deleteButton, id, apiPath, listPage, read}
// ============================================================================

const AdminPage = (function () {
    async function run(status, load) {
        try {
            if (!await Auth.isAdmin()) {
                showStatus(status, 'This page is for admins.', true);
                return;
            }
            await load();
        } catch (error) {
            showStatus(status, error.message, true);
        }
    }

    function queryId() {
        return new URLSearchParams(window.location.search).get('id');
    }

    function wireEditor(settings) {
        const itemPath = `${settings.apiPath}/${encodeURIComponent(settings.id)}`;

        async function perform(call) {
            try {
                await call();
                window.location.assign(settings.listPage);
            } catch (error) {
                showStatus(settings.status, error.message, true);
            }
        }

        settings.form.addEventListener('submit', function (event) {
            event.preventDefault();
            perform(function () {
                return settings.id
                    ? Api.send('PUT', itemPath, settings.read())
                    : Api.send('POST', settings.apiPath, settings.read());
            });
        });

        if (settings.id) {
            settings.deleteButton.hidden = false;
            Form.confirmingButton(settings.deleteButton, function () {
                perform(function () { return Api.send('DELETE', itemPath); });
            });
        }
    }

    return {run: run, queryId: queryId, wireEditor: wireEditor};
})();
