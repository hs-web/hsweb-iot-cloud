importResource("/admin/css/common.css");

importMiniui(function () {
    mini.parse();
    require(["request", "message", "miniui-tools"], function (request, message, tools) {
        require(["pages/form/designer-drag/parser"], function (Parser) {
            require(["pages/form/designer-drag/components-default", "css!pages/form/designer-drag/defaults"], function () {
                require(["pages/form/designer-drag/file-upload-2"], function (uploader) {

                    var uploaderContainer = $(".file-upload-container");
                    var button = uploaderContainer.find("#file-upload-button");
                    var multiple = false;

                    uploaderContainer.find(".clear-file").on("click", function () {
                        uploaderContainer.find(".file-queue").children().remove();
                    });
                    if (multiple) {
                        uploaderContainer.addClass("multiple");
                    }

                    function initUploader(uploader) {
                        button.removeClass('webuploader-container')
                            .html("选择文件");
                        var uploaderInstance;

                        uploaderInstance = uploader.initUploader({
                            container: uploaderContainer,
                            // accept: "image",
                            pick: {
                                id: "#" + button.attr("id"),
                                multiple: multiple
                            },
                            process: function (file, percentage) {
                                var percent = percentage === 0 ? "0" : (percentage * 100).toFixed(1);
                                uploaderContainer.find(".file_" + file.id + " .file-upload-process").css("width", percent + "%");
                                uploaderContainer.find(".file_" + file.id + " .file-upload-percent").text(percentage >= 1 ? "等待服务器响应" : percent + "%");

                            },
                            callback: function (file, info) {
                                uploaderContainer.find(".file_" + file.id + " .file-upload-percent").text("上传成功");
                                uploaderContainer.find(".file_" + file.id).attr("fileData", typeof info === 'string' ? info : JSON.stringify(info));
                            },
                            uploadError: function (file, response) {
                                uploaderInstance.removeFile(file);
                                uploaderContainer.find(".file_" + file.id + " .file-upload-percent").text(response.message);
                                uploaderContainer.find(".file_" + file.id + " .file-upload-process").addClass("error");
                            },
                            onAdd: function (file) {
                                if (!multiple) {
                                    uploaderContainer.find(".file-queue").children().remove();
                                }
                                var template = [
                                    "<div class=\"file_" + file.id + " file-info\">",
                                    "<div class=\"file-operation\">" +
                                    "<i class=\"remove-file fa fa-times text-danger\" style=\"cursor: pointer\"></i>" +
                                    "</div>",
                                    "<div class=\"file-name\">",
                                    file.name,
                                    "</div>",
                                    "<div class=\"file-process\">",
                                    "<div class=\"file-upload-process\"></div>",
                                    "<div class=\"file-upload-percent\">准备上传</div>",
                                    "</div>"
                                    , "</div>"
                                ];

                                var fileInfo = $(template.join(""));
                                uploaderContainer.find(".file-queue").append(fileInfo);

                                fileInfo
                                    .find(".remove-file")
                                    .on("click", function () {
                                        uploaderInstance.removeFile(file);
                                        fileInfo.remove();
                                    });
                                var width = fileInfo.width();
                                var height = fileInfo.height() - 20;

                                uploaderInstance.makeThumb(file, function (error, src) {
                                    if (error) {
                                        return;
                                    }
                                    var name = $(fileInfo).find(".file-name");

                                    var img = $("<img>")
                                        .attr("src", src);
                                    img.css({
                                        width: width + "px",
                                        height: height + "px"
                                    });
                                    name.css({
                                        width: width + "px",
                                        height: height + "px"
                                    });
                                    name.text("").append(img);
                                }, width * 2, height * 2);

                            }
                        });
                    }

                    initUploader(uploader);
                })

                // var config = {};
                // config.html = $("#form").html();
                // config.components = JSON.parse($("#form-components").html());
                // config.javascript = $("#form-on-init").html();
                // //使用组件id作为表单控件的name
                // config.useIdForName = $("#form").attr("useIdForName") === 'true';
                // var parser = window.parser = new Parser(config);
                //
                // // parser.render($("#form"));
                //
                // $("#form").fadeIn(200);
                // mini.parse();
                // $(".get-form-data").on("click", function () {
                //     message.alert(JSON.stringify(parser.getData(false)));
                // });
                // if (formOnLoad) {
                //     formOnLoad(parser);
                // }
            });
        });
    });
});