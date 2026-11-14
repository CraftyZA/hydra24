(async function () {
    const status = document.getElementById('status');

    if (!await Auth.signedInEmail()) {
        showStatus(status, 'Log in to see your entries.');
        return;
    }

    try {
        const list = createPagedList(document.getElementById('entries'), renderEntryRow, 'You have not entered any events yet.');
        list.show(await Api.get('/api/me/entries'));
    } catch (error) {
        showStatus(status, error.message, true);
    }
})();
