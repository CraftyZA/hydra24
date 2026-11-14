(function () {
    const id = encodeURIComponent(AdminPage.queryId());

    AdminPage.run(document.getElementById('status'), async function () {
        const list = createPagedList(document.getElementById('entries'), renderEntryRow, 'No entries yet.');
        const [member, entries] = await Promise.all([
            Api.get(`/api/admin/members/${id}`),
            Api.get(`/api/admin/members/${id}/entries`)
        ]);
        document.getElementById('heading').textContent = `${MemberText.name(member)}: ${entries.length} entries`;
        list.show(entries);
    });
})();
