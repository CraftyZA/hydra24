(async function () {
    const status = document.getElementById('status');

    function renderEntry(entry) {
        return el('a', {class: 'row-card', href: `/events/calendar/event.html?id=${entry.eventId}`},
            el('span', {class: 'row-title', text: entry.eventName}),
            el('span', {class: 'row-detail', text: `${Format.date(entry.startsOn)} - ${entry.venueName}`}),
            el('span', {class: 'row-detail', text: `${entry.className} - ${entry.attendance} - ${Format.money(entry.cost)}`}));
    }

    if (!await Auth.signedInEmail()) {
        showStatus(status, 'Log in to see your entries.');
        return;
    }

    try {
        const list = createPagedList(document.getElementById('entries'), renderEntry, 'You have not entered any events yet.');
        list.show(await Api.get('/api/me/entries'));
    } catch (error) {
        showStatus(status, error.message, true);
    }
})();
