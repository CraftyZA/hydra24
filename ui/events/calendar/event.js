(async function () {
    const status = document.getElementById('status');
    const entrySummary = document.getElementById('entry-summary');
    const entryStatus = document.getElementById('entry-status');
    const entryForm = document.getElementById('entry-form');
    const entrySubmit = document.getElementById('entry-submit');
    const withdrawButton = document.getElementById('withdraw');
    const eventId = encodeURIComponent(new URLSearchParams(window.location.search).get('id'));
    const entryPath = `/api/events/${eventId}/entry`;

    function renderFacts(event, typeLabel) {
        const lines = [`${Format.dateRange(event.startsOn, event.days)} - ${typeLabel}`];
        if (event.entryClose) lines.push(`Entries close ${Format.dateTime(event.entryClose)}`);
        document.getElementById('facts').replaceChildren(...lines.map(function (line) {
            return el('p', {class: 'long-text', text: line});
        }));
    }

    function showImage(event) {
        if (!event.imagePath) return;
        const image = document.getElementById('event-image');
        image.src = event.imagePath;
        image.alt = event.name;
        image.hidden = false;
    }

    function renderLongTexts(event) {
        const sections = [['Itinerary', event.itinerary], ['Accommodation', event.accommodation], ['Catering', event.catering]];
        const panels = sections
            .filter(function ([, text]) { return text; })
            .map(function ([title, text]) {
                return el('section', {class: 'panel'}, el('h2', {text: title}), el('p', {class: 'long-text', text: text}));
            });
        document.getElementById('long-texts').replaceChildren(...panels);
    }

    function describeEntry(entry) {
        return `You are entered: ${entry.className}, ${entry.attendanceLabel}, ${Format.money(entry.cost)}.`;
    }

    function showEntry(entry) {
        entrySummary.textContent = entry ? describeEntry(entry) : '';
        entrySummary.hidden = !entry;
        entrySubmit.textContent = entry ? 'Update entry' : 'Enter';
        withdrawButton.hidden = !entry;
    }

    async function refreshEntry() {
        const entry = await Api.get(entryPath);
        showEntry(entry);
        return entry;
    }

    async function submitEntry(event) {
        event.preventDefault();
        try {
            await Api.send('PUT', entryPath, Form.read(entryForm));
            await refreshEntry();
            showStatus(entryStatus, 'Your entry is saved.');
        } catch (error) {
            showStatus(entryStatus, error.message, true);
        }
    }

    async function withdraw() {
        try {
            await Api.send('DELETE', entryPath);
            await refreshEntry();
            showStatus(entryStatus, 'You have withdrawn from this event.');
        } catch (error) {
            showStatus(entryStatus, error.message, true);
        }
    }

    function showEntryForm(event, classes, entry, profile) {
        Form.fillSelect(entryForm.elements.classId, classes.map(function (c) {
            return {value: c.id, label: `${c.name} - ${Format.money(c.cost)}`};
        }), 'Choose...');
        Form.fillSelect(entryForm.elements.attendance, event.attendanceOptions);
        Form.write(entryForm, entry || {classId: profile.preferredClassId, attendance: event.attendanceOptions[0].value});
        entryForm.addEventListener('submit', submitEntry);
        Form.confirmingButton(withdrawButton, withdraw);
        entryForm.hidden = false;
    }

    async function setUpEntry(event) {
        if (!await Auth.signedInEmail()) {
            showStatus(entryStatus, event.entriesOpen ? 'Log in or register to enter.' : 'Entries are closed.');
            return;
        }
        const [entry, profile, classes] = await Promise.all([
            refreshEntry(),
            Api.get('/api/me/profile'),
            Api.getPublic(`/api/events/${eventId}/classes`)
        ]);
        if (!event.entriesOpen) {
            showStatus(entryStatus, 'Entries are closed.');
        } else if (!profile) {
            entryStatus.replaceChildren('Complete your ', el('a', {href: '/profile/profile.html', text: 'profile'}), ' to enter.');
            entryStatus.hidden = false;
        } else {
            showEntryForm(event, classes, entry, profile);
        }
    }

    try {
        const [event, options] = await Promise.all([
            Api.getPublic(`/api/events/${eventId}`),
            Api.getPublic('/api/options')
        ]);
        const type = options.eventTypes.find(function (t) { return t.value === event.eventType; });
        document.getElementById('event-name').textContent = event.name;
        document.title = `${event.name} - Hydra 24`;
        showImage(event);
        renderFacts(event, type.label);
        renderLongTexts(event);
        document.getElementById('event').hidden = false;
        await setUpEntry(event);
    } catch (error) {
        showStatus(status, error.message, true);
    }
})();
