// ============================================================================
// IMAGE UPLOAD
// Shrinks a photo in the browser and PUTs it to a presigned URL. Phone photos are
// often several MB; shrunk, they are a few hundred KB.
//   ImageUpload.shrinkToJpeg(file, maxSide)  Promise of a JPEG Blob, at most maxSide
//                                            pixels wide and high
//   ImageUpload.put(upload, blob)            PUT to {uploadUrl, contentType}
// Both reject with an Error whose message can be shown to the user.
// ============================================================================

const ImageUpload = (function () {
    const JPEG_QUALITY = 0.85;

    async function readImage(file) {
        try {
            return await createImageBitmap(file);
        } catch (e) {
            throw new Error('That file could not be read as an image. Try a JPEG or PNG.');
        }
    }

    async function shrinkToJpeg(file, maxSide) {
        const image = await readImage(file);
        const scale = Math.min(1, maxSide / Math.max(image.width, image.height));
        const canvas = document.createElement('canvas');
        canvas.width = Math.round(image.width * scale);
        canvas.height = Math.round(image.height * scale);
        canvas.getContext('2d').drawImage(image, 0, 0, canvas.width, canvas.height);
        image.close();
        return new Promise(function (resolve, reject) {
            canvas.toBlob(function (blob) {
                if (blob) resolve(blob);
                else reject(new Error('That image could not be converted. Try another one.'));
            }, 'image/jpeg', JPEG_QUALITY);
        });
    }

    async function put(upload, blob) {
        const response = await fetch(upload.uploadUrl, {
            method: 'PUT',
            headers: {'Content-Type': upload.contentType},
            body: blob
        });
        if (!response.ok) throw new Error('The image upload failed. Please try again.');
    }

    return {shrinkToJpeg: shrinkToJpeg, put: put};
})();
