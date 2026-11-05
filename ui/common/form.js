// ============================================================================
// FORM
// Forms whose inputs are named after the JSON fields. Needs common/dom.js.
//   Form.fillSelect(select, options, blankLabel)  options are {value, label}
//   Form.write(form, data)                        put data into the inputs
//   Form.read(form)                               inputs back to data
//   Form.steps(form)                              one <fieldset> on screen at a time
//   Form.confirmingButton(button, action)         second tap runs the action
//   Number inputs read as numbers and blank inputs as null.
// ============================================================================

const Form = (function () {
    const CONFIRM_LABEL = 'Tap again to confirm';
    const CONFIRM_WINDOW_MS = 4000;

    function fillSelect(select, options, blankLabel) {
        const items = options.map(function (option) {
            return el('option', {value: option.value, text: option.label});
        });
        if (blankLabel !== undefined) items.unshift(el('option', {value: '', text: blankLabel}));
        select.replaceChildren(...items);
    }

    function namedInputs(form) {
        return Array.from(form.elements).filter(function (input) { return input.name; });
    }

    function write(form, data) {
        namedInputs(form).forEach(function (input) {
            const value = data[input.name];
            if (input.type === 'checkbox') {
                input.checked = Boolean(value);
            } else {
                input.value = value === null || value === undefined ? '' : value;
            }
        });
    }

    function readInput(input) {
        if (input.type === 'checkbox') return input.checked;
        if (input.value.trim() === '') return null;
        return input.type === 'number' ? Number(input.value) : input.value.trim();
    }

    function read(form) {
        const data = {};
        namedInputs(form).forEach(function (input) { data[input.name] = readInput(input); });
        return data;
    }

    // Long forms are split so each part fits a phone screen. Going forward checks
    // the current part, so the submit on the last part never lands on a hidden field.
    // Needs in the form: <fieldset> parts with a <legend>, buttons with data-step-back
    // and data-step-next, and a submit button.
    function steps(form) {
        const parts = Array.from(form.querySelectorAll('fieldset'));
        const back = form.querySelector('[data-step-back]');
        const next = form.querySelector('[data-step-next]');
        const submit = form.querySelector('[type="submit"]');
        const progress = form.querySelector('[data-step-progress]');
        let current = 0;

        function show(index) {
            current = index;
            parts.forEach(function (part, i) { part.hidden = i !== index; });
            back.hidden = index === 0;
            next.hidden = index === parts.length - 1;
            submit.hidden = index !== parts.length - 1;
            progress.textContent = `Step ${index + 1} of ${parts.length}`;
        }

        function currentPartIsValid() {
            return Array.from(parts[current].elements).every(function (input) { return input.reportValidity(); });
        }

        function goForward() {
            if (currentPartIsValid()) show(current + 1);
        }

        // Enter would submit from any part, past fields not yet filled in.
        form.addEventListener('keydown', function (event) {
            const isEnterInField = event.key === 'Enter' && event.target.tagName === 'INPUT';
            if (isEnterInField && current < parts.length - 1) {
                event.preventDefault();
                goForward();
            }
        });
        back.addEventListener('click', function () { show(current - 1); });
        next.addEventListener('click', goForward);
        show(0);
    }

    function confirmingButton(button, action) {
        const label = button.textContent;
        let armed = false;
        let timer;

        function disarm() {
            armed = false;
            button.textContent = label;
        }

        button.addEventListener('click', function () {
            if (armed) {
                clearTimeout(timer);
                disarm();
                action();
                return;
            }
            armed = true;
            button.textContent = CONFIRM_LABEL;
            timer = setTimeout(disarm, CONFIRM_WINDOW_MS);
        });
    }

    return {fillSelect: fillSelect, write: write, read: read, steps: steps, confirmingButton: confirmingButton};
})();
