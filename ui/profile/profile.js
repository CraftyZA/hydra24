(async function () {
    const status = document.getElementById('status');
    const form = document.getElementById('profile-form');
    const BLANK = 'Choose...';

    function fillChoices(options, classes) {
        Form.fillSelect(form.elements.gender, options.genders, BLANK);
        Form.fillSelect(form.elements.preferredClassId, classes.map(function (c) {
            return {value: c.id, label: c.name};
        }), BLANK);
    }

    async function save(event) {
        event.preventDefault();
        try {
            await Api.send('PUT', '/api/me/profile', Form.read(form));
            showStatus(status, 'Profile saved.');
        } catch (error) {
            showStatus(status, error.message, true);
        }
    }

    if (!await Auth.signedInEmail()) {
        showStatus(status, 'Log in or register to set up your profile.');
        return;
    }

    try {
        const [options, classes, profile] = await Promise.all([
            Api.getPublic('/api/options'),
            Api.getPublic('/api/classes'),
            Api.get('/api/me/profile')
        ]);
        fillChoices(options, classes);
        if (profile) Form.write(form, profile);
        Form.steps(form);
        form.addEventListener('submit', save);
        form.hidden = false;
    } catch (error) {
        showStatus(status, error.message, true);
    }
})();
