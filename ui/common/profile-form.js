// ============================================================================
// PROFILE FORM
// The profile fields, shared by a member's own profile page and the admin's
// member page. Needs common/dom.js, api.js and form.js.
//   ProfileForm.build(form, options, classes)  adds the fields
//   ProfileForm.saveOnSubmit(form, status, apiPath)  submit PUTs them to apiPath
// build adds one <fieldset> per step before the form's .form-buttons. options is
// /api/options and classes is /api/classes. Call Form.steps(form) after it.
// ============================================================================

const ProfileForm = (function () {
    const PHONE_PATTERN = '\\+?[0-9 ]{9,20}';
    const NAME_MAX_LENGTH = 100;
    const BLANK = 'Choose...';

    function field(label, control) {
        return el('label', {class: 'field'}, label, control);
    }

    function textInput(name, autocomplete) {
        return el('input', {name: name, required: true, maxLength: NAME_MAX_LENGTH, autocomplete: autocomplete || ''});
    }

    function phoneInput(name, autocomplete) {
        return el('input', {name: name, type: 'tel', required: true, pattern: PHONE_PATTERN, autocomplete: autocomplete || ''});
    }

    function select(name, choices, required) {
        const control = el('select', {name: name, required: required});
        Form.fillSelect(control, choices, BLANK);
        return control;
    }

    function step(legend, ...fields) {
        return el('fieldset', {}, el('legend', {text: legend}), ...fields);
    }

    function build(form, options, classes) {
        const classChoices = classes.map(function (c) { return {value: c.id, label: c.name}; });
        form.querySelector('.form-buttons').before(
            step('Personal details',
                field('First name', textInput('firstName', 'given-name')),
                field('Last name', textInput('lastName', 'family-name')),
                field('Cellphone', phoneInput('cellphone', 'tel')),
                field('Date of birth', el('input', {name: 'dateOfBirth', type: 'date', required: true, autocomplete: 'bday'})),
                field('Gender', select('gender', options.genders, true))),
            step('Class and emergency contact',
                field('Preferred class', select('preferredClassId', classChoices, false)),
                field('Emergency contact name', textInput('emergencyName')),
                field('Emergency contact number', phoneInput('emergencyNo'))));
    }

    function saveOnSubmit(form, status, apiPath) {
        form.addEventListener('submit', async function (event) {
            event.preventDefault();
            try {
                await Api.send('PUT', apiPath, Form.read(form));
                showStatus(status, 'Profile saved.');
            } catch (error) {
                showStatus(status, error.message, true);
            }
        });
    }

    return {build: build, saveOnSubmit: saveOnSubmit};
})();
