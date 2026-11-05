(function () {
    const status = document.getElementById('status');
    const form = document.getElementById('event-form');
    const id = AdminPage.queryId();

    function showEntrantsLink() {
        const link = document.getElementById('entrants');
        link.href = `/admin/events/entrants.html?id=${encodeURIComponent(id)}`;
        link.hidden = false;
    }

    AdminPage.run(status, async function () {
        const options = await Api.getPublic('/api/options');
        Form.fillSelect(form.elements.eventType, options.eventTypes);
        if (id) {
            Form.write(form, await Api.getPublic(`/api/events/${encodeURIComponent(id)}`));
            showEntrantsLink();
        }

        Form.steps(form);
        AdminPage.wireEditor({
            form: form,
            status: status,
            deleteButton: document.getElementById('delete'),
            id: id,
            apiPath: '/api/admin/events',
            listPage: '/admin/events/events.html',
            read: function () { return Form.read(form); }
        });
        form.hidden = false;
    });
})();
