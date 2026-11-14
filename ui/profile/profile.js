(async function () {
    const status = document.getElementById('status');
    const form = document.getElementById('profile-form');

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
        ProfileForm.build(form, options, classes);
        if (profile) Form.write(form, profile);
        Form.steps(form);
        ProfileForm.saveOnSubmit(form, status, '/api/me/profile');
        form.hidden = false;
    } catch (error) {
        showStatus(status, error.message, true);
    }
})();
