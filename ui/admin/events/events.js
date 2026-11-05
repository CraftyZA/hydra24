(function () {
    function renderEvent(event) {
        return el('a', {class: 'row-card', href: `/admin/events/event.html?id=${event.id}`},
            el('span', {class: 'row-title', text: event.name}),
            el('span', {class: 'row-detail', text: Format.dateRange(event.startsOn, event.days)}),
            el('span', {class: 'row-detail', text: event.venueName}));
    }

    AdminPage.run(document.getElementById('status'), async function () {
        const list = createPagedList(document.getElementById('events'), renderEvent, 'No events yet.');
        const events = await Api.getPublic('/api/events');
        list.show(events.reverse());
    });
})();
