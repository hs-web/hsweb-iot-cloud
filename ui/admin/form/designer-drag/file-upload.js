(function () {
    var maxFileSize = 100 * 1024 * 1024;
    var BASE_PATH = window.BASE_PATH || '/';

    function bytesToSize(bytes) {
        if (bytes === 0) return '0 B';
        if (bytes < 1024) return bytes + 'b';
        var k = 1024, // or 1024
            sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'],
            i = Math.floor(Math.log(bytes) / Math.log(k));
        return (bytes / Math.pow(k, i)).toPrecision(3) + ' ' + sizes[i];
    }

    function FileUploader(WebUploader) {
        this.initUploader = function (uploadButton, uploadCallback, onUpload) {
            var uploader = WebUploader.create({
                swf: BASE_PATH + 'plugins/webuploader/Uploader.swf',
                //文件上传地址
                server: BASE_PATH + "api/file-server/file/upload-static",
                pick: {
                    id: uploadButton,
                    multiple: false
                },
                compress: false,
                // dnd: document.body,
                // paste: document.body,
                // accept: accepts[accept],
                resize: false
            });

            require(["storejs"], function (storejs) {
                uploader.on('uploadBeforeSend', function (e, param, headers) {
                    var token = storejs.get("iot-cloud-user");
                    if (token) {
                        headers['iot-cloud-user'] = token;
                    }
                });
            });
            if (onUpload) {
                uploader.on("uploadProgress", onUpload);
            }
            uploader.on('fileQueued', function (file) {
                if (maxFileSize <= file.size) {
                    uploader.removeFile(file.id);
                    message.showTips("文件大小不能超过:" + bytesToSize(maxFileSize));
                    return;
                }
                uploader.upload();
            });
            uploader.on('uploadSuccess', function (file, response) {
                if (response.status === 200 && response.result) {
                    uploadCallback(response.result);
                }
            });
            uploader.on('uploadError', function (file, response) {
                if (response.status === 200 && response.result) {
                    uploadCallback(response.result);
                } else {
                    alert(response.message);
                }
            });
        }
    }

    if (window.define) {
        define(["plugin/webuploader/webuploader.min"], function (WebUploader) {
            return new FileUploader(WebUploader);
        });
    }

    window.FileUploader = new FileUploader(window.WebUploader);
})();

