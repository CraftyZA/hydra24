(function () {
    const status = document.getElementById('status');
    const form = document.getElementById('class-form');
    const costs = document.getElementById('costs');
    const id = AdminPage.queryId();
    const API_PATH = '/api/admin/classes';

    // These inputs have no name: they travel together in eventTypeCosts.
    function addEventTypeCostFields(eventTypes) {
        eventTypes.forEach(function (type) {
            const input = el('input', {type: 'number', min: '0', step: '0.01'});
            input.dataset.eventType = type.value;
            costs.append(el('label', {class: 'field', text: `${type.label} price (R)`}, input));
        });
    }

    function eventTypeCostInputs() {
        return Array.from(costs.querySelectorAll('[data-event-type]'));
    }

    function readClass() {
        const eventTypeCosts = {};
        eventTypeCostInputs()
            .filter(function (input) { return input.value !== ''; })
            .forEach(function (input) { eventTypeCosts[input.dataset.eventType] = Number(input.value); });
        return Object.assign(Form.read(form), {eventTypeCosts: eventTypeCosts});
    }

    function showClass(item) {
        Form.write(form, item);
        eventTypeCostInputs().forEach(function (input) {
            const cost = item.eventTypeCosts[input.dataset.eventType];
            input.value = cost === undefined ? '' : cost;
        });
    }

    AdminPage.run(status, async function () {
        const options = await Api.getPublic('/api/options');
        Form.fillSelect(form.elements.classType, options.classTypes);
        addEventTypeCostFields(options.eventTypes);
        if (id) showClass(await Api.get(`${API_PATH}/${encodeURIComponent(id)}`));

        Form.steps(form);
        AdminPage.wireEditor({
            form: form,
            status: status,
            deleteButton: document.getElementById('delete'),
            id: id,
            apiPath: API_PATH,
            listPage: '/admin/classes/classes.html',
            read: readClass
        });
        form.hidden = false;
    });
})();
