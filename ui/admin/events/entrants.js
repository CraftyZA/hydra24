(function () {
    const id = encodeURIComponent(AdminPage.queryId());

    function renderEntrant(entrant) {
        return el('div', {class: 'row-card'},
            el('span', {class: 'row-title', text: `#${entrant.profileId} ${entrant.firstName} ${entrant.lastName}`}),
            el('span', {class: 'row-detail', text: `${entrant.className} - ${entrant.attendance} - ${Format.money(entrant.cost)}`}),
            el('span', {class: 'row-detail', text: entrant.cellphone}));
    }

    AdminPage.run(document.getElementById('status'), async function () {
        const list = createPagedList(document.getElementById('entrants'), renderEntrant, 'No entries yet.');
        const [event, entrants] = await Promise.all([
            Api.getPublic(`/api/events/${id}`),
            Api.get(`/api/admin/events/${id}/entries`)
        ]);
        document.getElementById('heading').textContent = `${event.name}: ${entrants.length} entered`;
        list.show(entrants);
    });
})();
