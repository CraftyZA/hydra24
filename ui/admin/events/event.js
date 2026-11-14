(function () {
    const status = document.getElementById('status');
    const form = document.getElementById('event-form');
    const id = AdminPage.queryId();
    const eventPath = `/api/events/${encodeURIComponent(id)}`;
    const imagePath = `/api/admin/events/${encodeURIComponent(id)}/image`;
    const IMAGE_MAX_SIDE = 1600;
    const imagePreview = document.getElementById('image-preview');
    const imageFile = document.getElementById('image-file');
    const imageRemove = document.getElementById('image-remove');

    function showEntrantsLink() {
        const link = document.getElementById('entrants');
        link.href = `/admin/events/entrants.html?id=${encodeURIComponent(id)}`;
        link.hidden = false;
    }

    function showImage(path) {
        imagePreview.hidden = !path;
        imageRemove.hidden = !path;
        if (path) imagePreview.src = path;
    }

    async function changeImage(change, doneMessage) {
        try {
            await change();
            showImage((await Api.getPublic(eventPath)).imagePath);
            showStatus(status, doneMessage);
        } catch (error) {
            showStatus(status, error.message, true);
        }
    }

    async function uploadImage() {
        const file = imageFile.files[0];
        imageFile.value = '';
        if (!file) return;
        showStatus(status, 'Saving image...');
        const blob = await ImageUpload.shrinkToJpeg(file, IMAGE_MAX_SIDE);
        const upload = await Api.send('POST', `${imagePath}/upload`, {size: blob.size});
        await ImageUpload.put(upload, blob);
        await Api.send('PUT', imagePath, {key: upload.key});
    }

    // An image needs the event's id, so a new event gets one after its first save.
    function wireImage(event) {
        document.getElementById('image-note').hidden = true;
        document.getElementById('image-choose').hidden = false;
        showImage(event.imagePath);
        imageFile.addEventListener('change', function () { changeImage(uploadImage, 'Image saved.'); });
        Form.confirmingButton(imageRemove, function () {
            changeImage(function () { return Api.send('DELETE', imagePath); }, 'Image removed.');
        });
    }

    AdminPage.run(status, async function () {
        const options = await Api.getPublic('/api/options');
        Form.fillSelect(form.elements.eventType, options.eventTypes);
        if (id) {
            const event = await Api.getPublic(eventPath);
            Form.write(form, event);
            showEntrantsLink();
            wireImage(event);
        }

        Form.steps(form);
        AdminPage.wireEditor({
            form: form,
            status: status,
            deleteButton: document.getElementById('delete'),
            id: id,
            apiPath: '/api/admin/events',
            listPage: '/admin/events/events.html',
            read: function () { return Form.read(form); }
        });
        form.hidden = false;
    });
})();
