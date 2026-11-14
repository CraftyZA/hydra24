// ============================================================================
// ENTRY ROW
// One of a member's event entries as a paged-list row, linking to the event.
// Needs common/dom.js and common/format.js.
//   renderEntryRow(entry)    entry as /api/me/entries returns it
// ============================================================================

function renderEntryRow(entry) {
    return el('a', {class: 'row-card', href: `/events/calendar/event.html?id=${entry.eventId}`},
        el('span', {class: 'row-title', text: entry.eventName}),
        el('span', {class: 'row-detail', text: Format.date(entry.startsOn)}),
        el('span', {class: 'row-detail', text: `${entry.className} - ${entry.attendance} - ${Format.money(entry.cost)}`}));
}
