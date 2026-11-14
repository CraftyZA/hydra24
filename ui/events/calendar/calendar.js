(async function () {
    const status = document.getElementById('status');
    const typeFilter = document.getElementById('type-filter');
    const whenFilter = document.getElementById('when-filter');
    const MS_PER_DAY = 24 * 60 * 60 * 1000;

    let events = [];
    let typeLabels = {};

    function renderEvent(event) {
        return el('a', {class: 'row-card', href: `/events/calendar/event.html?id=${event.id}`},
            el('span', {class: 'row-title', text: event.name}),
            el('span', {class: 'row-detail', text: `${Format.dateRange(event.startsOn, event.days)} - ${typeLabels[event.eventType]}`}));
    }

    const list = createPagedList(document.getElementById('events'), renderEvent, 'No events to show.');

    function hasEnded(event) {
        const lastDay = new Date(`${event.startsOn}T23:59:59`).getTime() + (event.days - 1) * MS_PER_DAY;
        return lastDay < Date.now();
    }

    function showFiltered() {
        const wantPast = whenFilter.value === 'past';
        const chosen = events.filter(function (event) {
            const typeMatches = !typeFilter.value || event.eventType === typeFilter.value;
            return typeMatches && hasEnded(event) === wantPast;
        });
        list.show(wantPast ? chosen.reverse() : chosen);
    }

    try {
        const [options, loaded] = await Promise.all([Api.getPublic('/api/options'), Api.getPublic('/api/events')]);
        events = loaded;
        options.eventTypes.forEach(function (type) { typeLabels[type.value] = type.label; });
        Form.fillSelect(typeFilter, options.eventTypes, 'All');
        typeFilter.addEventListener('change', showFiltered);
        whenFilter.addEventListener('change', showFiltered);
        showFiltered();
    } catch (error) {
        showStatus(status, error.message, true);
    }
})();
