// ============================================================================
// PAGED LIST
// A list that never scrolls: it shows as many rows as fit the space it has, with
// Previous / Next for the rest, and re-fits when the screen changes.
// Needs common/dom.js and a .paged-list element with a set height.
//   const list = createPagedList(element, renderRow, emptyMessage);
//   list.show(items);
// renderRow(item) returns an element for the row. Rows must all be one height.
// ============================================================================

function createPagedList(container, renderRow, emptyMessage) {
    const rows = el('ul', {class: 'paged-list-rows'});
    const previous = el('button', {class: 'btn btn-small', type: 'button', text: 'Previous'});
    const next = el('button', {class: 'btn btn-small', type: 'button', text: 'Next'});
    const position = el('span', {class: 'pager-position'});
    const pager = el('div', {class: 'pager'}, previous, position, next);
    container.replaceChildren(rows, pager);

    let items = [];
    let page = 0;
    let rowsPerPage = 1;

    function rowElement(item) {
        return el('li', {class: 'paged-list-row'}, renderRow(item));
    }

    function measureRowsPerPage() {
        const sample = rowElement(items[0]);
        rows.replaceChildren(sample);
        const gap = parseFloat(getComputedStyle(rows).rowGap) || 0;
        const fit = Math.floor((rows.clientHeight + gap) / (sample.offsetHeight + gap));
        return Math.max(1, fit);
    }

    function render() {
        if (items.length === 0) {
            rows.replaceChildren(el('li', {class: 'paged-list-empty', text: emptyMessage}));
            pager.hidden = true;
            return;
        }
        pager.hidden = false;
        rowsPerPage = measureRowsPerPage();
        pager.hidden = items.length <= rowsPerPage;

        const pageCount = Math.ceil(items.length / rowsPerPage);
        page = Math.min(page, pageCount - 1);
        const start = page * rowsPerPage;
        rows.replaceChildren(...items.slice(start, start + rowsPerPage).map(rowElement));

        position.textContent = `${page + 1} / ${pageCount}`;
        previous.disabled = page === 0;
        next.disabled = page === pageCount - 1;
    }

    previous.addEventListener('click', function () { page -= 1; render(); });
    next.addEventListener('click', function () { page += 1; render(); });
    new ResizeObserver(function () { render(); }).observe(container);

    return {
        show: function (newItems) {
            items = newItems;
            page = 0;
            render();
        }
    };
}
