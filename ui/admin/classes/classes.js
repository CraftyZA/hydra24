(function () {
    function renderClass(item) {
        const title = item.enabled ? item.name : `${item.name} (disabled)`;
        return el('a', {class: 'row-card', href: `/admin/classes/class.html?id=${item.id}`},
            el('span', {class: 'row-title', text: title}),
            el('span', {class: 'row-detail', text: item.description}),
            el('span', {class: 'row-detail', text: Format.money(item.cost)}));
    }

    AdminPage.run(document.getElementById('status'), async function () {
        const list = createPagedList(document.getElementById('classes'), renderClass, 'No classes yet.');
        list.show(await Api.get('/api/admin/classes'));
    });
})();
